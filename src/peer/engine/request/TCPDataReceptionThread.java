/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peer.engine.request;

import common.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import peer.ChatEngine;

/**
 *
 * @author root
 */
public class TCPDataReceptionThread extends Thread{
    Socket recvSocket;
    ChatEngine CE;

    public TCPDataReceptionThread(Socket socket,ChatEngine CE) {
        this.recvSocket = socket;
        this.CE = CE;        
    }
    
    public void run(){
        try {
            ObjectInputStream inp = new ObjectInputStream(new BufferedInputStream(recvSocket.getInputStream()));
            Message headerMsg     = (Message)inp.readObject();
            ReliableData recvdData = (ReliableData)inp.readObject();
            switch(recvdData.type){
                case "PEERMAP":{
                                    HashMap newPeerMap = (HashMap<String,Peer>)recvdData.getData();
                                    CE.replacePeerMap(newPeerMap);                    
                               }
                case "TIMESTAMP":{  
                                    Long newTS = (Long)recvdData.getData();
                                    if (newTS > CE.getUptodatePeerTIMESTAMP()){
                                        Peer newUptodatePeer = CE.getPeerByUsername(headerMsg.getUsername());
                                        newUptodatePeer.updateTIMESTAMP(newTS);
                                        CE.setUptodatePeer(newUptodatePeer);
                                      }
                                 }
                case "GROUP_MEMBER_LIST":{
                                            Group recvdGroup = (Group)recvdData.getData();
                                            CE.addToGroupMap(recvdGroup);
                                         }
                
            }
            inp.close();
            recvSocket.close();            
        } catch (Exception ex) {
            Logger.getLogger(TCPDataReceptionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   
}
