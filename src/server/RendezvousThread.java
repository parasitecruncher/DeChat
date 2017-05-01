/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.Query;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import common.Peer;
import peer.GUIManager;

/**
 *
 * @author root
 */
public class RendezvousThread extends Thread {

    public Socket csock;
    public HashMap<String, Peer> peerMap;
    private ObjectOutputStream out;
    private GUIManager guiManager;

    public RendezvousThread(Socket csock, HashMap<String, Peer> peerMap,GUIManager guiManager) {
        this.csock = csock;
        this.guiManager=guiManager;
        this.peerMap = peerMap;
        try {
            out = new ObjectOutputStream(csock.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(RendezvousThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void run(){
        try {
            // Get the query
            
            Query query = (Query) new ObjectInputStream(csock.getInputStream()).readObject();
            guiManager.serverAppendLoggerLabel(query.sender.username,query.type);

            // Add/update peer
            updateMainMap(query.sender);
            // Respond to the query
            respondToQuery(query);
            // Close the socket
            out.close();
            csock.close();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(RendezvousThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateMainMap(Peer peer) {
        // Check if the peer is already there
        Peer savedPeer = this.peerMap.get(peer.username);
        if (savedPeer == null || !savedPeer.equals(peer)) {
            // Get lock
            // --got lock
            this.peerMap.put(peer.username, peer);
            guiManager.serverAppendPeerlistview(peer);
        }
    }

    public void respondToQuery(Query query) {
        /*
        Queries:
        ALL     : Return entire PeerMap
        DEL     : Remove the peer
        USER    : Return details of user with username 'USER'; return NULL if None
         */
        switch (query.type) {
            case "ALL": {
                try {
                    // Send the peerMap
                    out.writeObject(this.peerMap);
                    out.flush();
                    //Send TIMESTAMP
                    out.writeLong(System.currentTimeMillis());
                    out.flush();
                } catch (IOException ex) {
                    Logger.getLogger(RendezvousThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
            case "USER": {
                try {
                    new ObjectOutputStream(this.csock.getOutputStream()).writeObject(peerMap.get(query.username));
                } catch (IOException ex) {
                    Logger.getLogger(RendezvousThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
            case "DEL": {
                this.peerMap.remove(query.sender.username);
                System.out.println("Peer " + query.sender + " left");
                
            }
        }
    }

}
