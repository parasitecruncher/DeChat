/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peer.engine.request;

import common.*;
import peer.ChatEngine;

import java.net.DatagramSocket;
import common.Constants;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import peer.engine.file.FileReceptionThread;
/**
 *
 * @author root
 */
public class RequestEngine {
    ChatEngine CE;
    DatagramSocket sendSocket;
    ByteArrayOutputStream bout;
    ObjectOutputStream out;
    byte[] buffer;
    
    public RequestEngine(ChatEngine CE){
        this.CE = CE;
        try {
            sendSocket = new DatagramSocket(Constants.MISC_UDP_SEND_PORT);
        } catch (SocketException ex) {
            Logger.getLogger(RequestEngine.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    //--Start Listening 
    public void start(){
        System.err.println("about to start the REQUEST  listerning thread");
        new RequestReceptionThread(CE).start();   
    }
    
    
    /*
    The requestMsg must contain the "msg" attribute as either
    REQUEST_TIMESTAMP
    REQUEST_PEERMAP
    DEL
    */
    public void sendRequest(Peer destPeer,Message requestMsg)
    {
        try {            
            bout = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bout);
            out.writeObject(requestMsg);
        } catch (IOException ex) {
            Logger.getLogger(RequestEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        buffer = bout.toByteArray();
        try{
        InetAddress ip = InetAddress.getByName(destPeer.ip);
        DatagramPacket requestPkt = new DatagramPacket(buffer,buffer.length,ip,Constants.MISC_UDP_RECV_PORT);
        sendSocket.send(requestPkt);
        bout.close();
        out.close();
        sendSocket.close();
        }catch(Exception e){
            e.printStackTrace();
        }         
    }
    
    /*
    Multicast Group object to all members in group
    */
    public void sendGroupObject(Group group){
        ArrayList<Peer> memberList = group.getMemberList();
        Socket sendSock;
        for(Peer member:memberList){
            try {
                sendSock = new Socket(member.ip,Constants.MISC_TCP_SERVER_PORT);
                ObjectOutputStream tempout = new ObjectOutputStream(sendSock.getOutputStream());
                tempout.writeObject(new Message("GROUP_OBJECT"));
                tempout.flush();
                tempout.writeObject(new ReliableData("GROUP_OBJECT",group));
                tempout.flush();
                tempout.close();
                
            } catch (IOException ex) {
                Logger.getLogger(RequestEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
        
    
}

