/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.Constants;
import common.Peer;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import peer.GUIManager;

/**
 *
 * @author root
 */
public class RendezousServer {

    public static ServerSocket serverSocket;
    public static HashMap<String, Peer> mainPeerMap;
    public static void startServer(GUIManager guiManager) {
        try {
            mainPeerMap = new HashMap<String, Peer>();
            System.out.println("Initialized Main Peer Map...");
            serverSocket = new ServerSocket(Constants.RENDEZOUS_SERVER_PORT);
            System.out.println("Started server at port " + Constants.RENDEZOUS_SERVER_PORT + "...");
            new Thread(){
                public void run(){
                    while (true) {
                        Socket csock;
                        try {
                            csock = serverSocket.accept();
                            System.out.println(csock.getInetAddress().getHostAddress()+" ");
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    guiManager.serverAppendLoggerLabel(csock.getInetAddress().getHostAddress()+"","CONECTED");
                                }                            
                            });
                        new RendezvousThread(csock, mainPeerMap,guiManager).start();                
                        } catch (IOException ex) {
                            Logger.getLogger(RendezousServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                    }
                }
            }.start();
            // Keep listening and passing sockets to threads
            

        } catch (IOException ex) {
            Logger.getLogger(RendezousServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
