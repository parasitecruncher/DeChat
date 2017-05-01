/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peer;

import common.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import peer.engine.text.TextEngine;
import peer.engine.file.FileEngine;
import peer.engine.request.RequestEngine;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author root
 */
public class ChatEngine {

    Peer user;
    Peer uptodatePeer; //Peer who has most recenty contacted the rendezvous server.
    
    HashMap<String, Peer> peerMap;  
    HashMap<String, Group> groupMap;//Map of all groups the user is registered to 
    TextEngine tEngine;
    FileEngine fEngine;
    RequestEngine rEngine;
    GUIManager guiManager;
    
    public ChatEngine(String username,GUIManager guiManager) {
        uptodatePeer = null;
        peerMap = new HashMap<String, Peer>();
        groupMap = new HashMap<String,Group>();
        tEngine = new TextEngine();
        fEngine = new FileEngine();
        rEngine = new RequestEngine(this);
        
        tEngine.start();
        fEngine.start();
        rEngine.start();
        // Create an new Peer and assign it to ChatEngine object
        this.user = new Peer(this.getSystemInetAddress(), username);
        // Get other peer details from server
        this.requestPeerMapFromServer();
    }

    private String getSystemInetAddress() {
        String ip = "0.0.0.0";
        // Try to get wlan0 Interface
        NetworkInterface netIface;
        try {
            for(Enumeration<NetworkInterface> netIfaces = NetworkInterface.getNetworkInterfaces(); netIfaces.hasMoreElements();) {
                netIface = netIfaces.nextElement();
                
                if(netIface.isLoopback() || netIface.isVirtual() || !netIface.isUp()) {
                    // Ignore these interfaces
                    continue;
                }
                
                // Got the right interface (Assuming only one connected interface is present)
                for (Enumeration<InetAddress> iAddrs = netIface.getInetAddresses(); iAddrs.hasMoreElements();) {
                    InetAddress iAddr = iAddrs.nextElement();
                    if (iAddr instanceof Inet4Address) {
                        // Set this as IP
                        ip = iAddr.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(ChatEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Selected " + ip + " as the host IP Address");
        return ip;
    }

    
    //update from RENDEZVOUS SERVER
    public void requestPeerMapFromServer() {
        try {
            
            Socket sock = new Socket(Constants.RENDEZOUS_SERVER_ADDRESS, Constants.RENDEZOUS_SERVER_PORT);
            ObjectOutputStream t_out = new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream t_in   = new ObjectInputStream(sock.getInputStream());
            // Send query
            t_out.writeObject(new Query("ALL", this.user));
            t_out.flush();
            // Receive response
            HashMap<String, Peer> recvMap = (HashMap<String, Peer>)t_in.readObject();
            user.updateTIMESTAMP((long)t_in.readLong());
            
            System.err.println("Abpout to print recieved map");
            printMap(recvMap);
            // Update the current map
            //this.peerMap.clear();
            //this.peerMap.putAll(recvMap);
            replacePeerMap(recvMap);
            System.err.println("printing updated map");
            guiManager.client_updatePeerList(peerMap);
            
            printMap(this.peerMap);
            t_out.close();
            t_in.close();
            sock.close();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ChatEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    //Generic internal method to send to all peers in peerMap
    public void _sendToAll(Message requestMsg){
        Iterator it = peerMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Peer tempDestPeer = (Peer)pair.getValue();
            rEngine.sendRequest(tempDestPeer,requestMsg);
            }
    }
    
    public void requestTIMESTAMPFromAll(){
     //Send msg "REQUEST_TIMESTAMP" to every peer in the peerMap
        _sendToAll(getMessagePacket("REQUEST_TIMESTAMP"));    
    }
    public void requestPeerMapFromBestPeer(){
     //assumes 'requestTIMESTAMPFromAll' has been already called 10 seconds ago
     rEngine.sendRequest(uptodatePeer,getMessagePacket("REQUEST_PEERMAP"));
    }
    public void requestDelete(){
     //send msg "DEL" to evry peer in the peerMap  
        _sendToAll(getMessagePacket("DEL"));
    }
    
    public Boolean sendMsg(String username, String msg) {
        Peer targetPeer = this.peerMap.get(username);
        if(targetPeer == null) {
            // Peer details not available
            this.requestPeerMapFromServer();
        }
        if(this.peerMap.get(username) != null) {
         targetPeer = this.peerMap.get(username);
         Boolean sent = tEngine.sendMsg(targetPeer, getMessagePacket(msg));   
         if (sent==true){
            logmessage(username,msg);
            try {
                guiManager.clientController.chatLog();
            } catch (IOException ex) {
                Logger.getLogger(ChatEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;

         }
         return false;
        }
        else {
            System.out.println("Peer currently not connected");
            return false;
        }
    }
    private void logmessage(String username, String msg) {
        String filename = username+".txt";
        String data="sent"+":"+username+":"+msg+":"+LocalDateTime.now().toString();
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            File file = new File(filename);
            if (!file.exists()) {
                    file.createNewFile();
            }
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(data);
            System.out.println("Logged Message");
        } catch (IOException e) {
                e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                        bw.close();
                if (fw != null)
                        fw.close();
            } catch (IOException ex) {
                    ex.printStackTrace();
            }
        }
    }
    
    public Boolean sendFile(String username,String pathToFile){      
        Peer targetPeer = this.peerMap.get(username);
        if(targetPeer == null) {
            // Peer details not available
            this.requestPeerMapFromServer();
        }
        if(this.peerMap.get(username) != null) {
         targetPeer = this.peerMap.get(username);
         Path p = Paths.get(pathToFile);
         File file = p.toFile();
         if(file.exists()){
             System.err.println("File exists");
         }
         else{
             System.err.println("File not there");
         }
         
         try{
         fEngine.sendFile(targetPeer, pathToFile);   
         }catch(Exception e){
             e.printStackTrace();
         }
         
         return true;
        }
        else {
            System.out.println("Peer currently not connected");
            return false;
        }
    }

    public Message getMessagePacket(String msg){
        Message msgPkt = new Message(this.user.username,msg);
        return msgPkt;
    }
    
    public Message getGroupMessagePacket(String msg,String groupName){
        Message msgPkt = new Message(this.user.username,msg,groupName);
        return msgPkt;
    }
    
    public static void printMap(Map mp) {
    Iterator it = mp.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry pair = (Map.Entry)it.next();
        System.out.println(pair.getKey() + " = " + pair.getValue());
        //it.remove(); // avoids a ConcurrentModificationException
    }
}

    
    public void replacePeerMap(HashMap newPeerMap){
        this.peerMap.clear();
        this.peerMap = (HashMap < String, Peer >)newPeerMap.clone();
    }
    public HashMap getPeerMap(){
        return peerMap;
    }
    public void popFromPeerMap(String username){
        peerMap.remove(username);
    }
    
    public long getUptodatePeerTIMESTAMP(){
        return uptodatePeer.getTIMESTAMP();
    }
    public long getMyTIMESTAMP(){
        return user.getTIMESTAMP();
    }
    
    public Peer getPeerByUsername(String username){
        return peerMap.get(username);
    }
    
    public void setUptodatePeer(Peer newUptodatePeer){
        uptodatePeer = newUptodatePeer;
    }
    
    //**********************GroupChat**********************
    public void createGroup(String groupName){
        Group newGroup = new Group(groupName);
        newGroup.addMember(user);
        groupMap.put(groupName, newGroup);        
    }
    public void addMemberToGroup(Peer newMember, String groupName){
        groupMap.get(groupName).addMember(newMember);
    }
    /*
    first, this user creates a group,..then adds members one by one (addMemberToGroup())
    after all members have been selected, he clicks OK.
    Then invite() distributes the member list of this group to all members in the group
    */
    public void invite(String groupName){
     rEngine.sendGroupObject(groupMap.get(groupName));
    }
    
    public void addToGroupMap(Group newGroup){
        groupMap.put(newGroup.getGroupName(), newGroup);
    }
    
    
    public void sendMsgToGroup(String msg, String groupName){
        ArrayList<Peer> memberList = groupMap.get(groupName).getMemberList();
        Message grpMsgPkt = getGroupMessagePacket(msg, groupName);
        for(Peer member:memberList){
            tEngine.sendMsg(member, grpMsgPkt);
        }
    }
}
