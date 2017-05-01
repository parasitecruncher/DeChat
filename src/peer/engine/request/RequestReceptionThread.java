/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peer.engine.request;

import java.net.DatagramSocket;
import common.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import peer.ChatEngine;
/**
 *
 * @author root
 */

/*
Maintains 2 sockets for reception.
UDP socket: to recieve requests for REQUEST_TIMESTAMP, REQUEST_PEERMAP, DEL (ONLY RECEPTION)
TCP socket: to recieve PEERMAP, TIMESTAMP ,etc ..things that need reliability.
*/
public class RequestReceptionThread extends Thread{
    ChatEngine CE;
    
    DatagramSocket recvSocket;
    DatagramPacket incomingRequestpkt;
    
    ServerSocket serverSocket;//Only for Listening
    
    //for recieving requests
    ByteArrayInputStream binp;
    ObjectInputStream inp;
    byte[] buffer;
    
    //for responding to requests
    ObjectOutputStream out;
    
    public RequestReceptionThread(ChatEngine CE){
        this.CE = CE;
        try {
            recvSocket = new DatagramSocket(Constants.MISC_UDP_RECV_PORT);
            serverSocket = new ServerSocket(Constants.MISC_TCP_SERVER_PORT);
            buffer = new byte[65536];
            incomingRequestpkt = new DatagramPacket(buffer, buffer.length);
        } catch (Exception ex) {
            Logger.getLogger(RequestReceptionThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        new Thread(){
            @Override
            public void run() {
                while(true){
                    try {
                        Socket socket = serverSocket.accept();
                        new TCPDataReceptionThread(socket,CE).start();
                        
                    } catch (IOException ex) {
                        Logger.getLogger(RequestReceptionThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
            }
            
        }.start();
    }
    
    
    public void run(){
        while(true){
            try {
                recvSocket.receive(incomingRequestpkt);
                buffer = incomingRequestpkt.getData();
                binp = new ByteArrayInputStream(buffer);
                inp = new ObjectInputStream(binp);
                Message recievedRequest = (Message)inp.readObject();
                handleRequest(recievedRequest);
            } catch (Exception ex) {
                Logger.getLogger(RequestReceptionThread.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }
    }
    
    
    
    public void handleRequest(Message request) throws IOException{
         String destip     = CE.getPeerByUsername(request.getUsername()).ip;
         Socket sendSocket = new Socket(destip,Constants.MISC_TCP_SERVER_PORT);
         out = new ObjectOutputStream(sendSocket.getOutputStream());
        
         switch(request.getMsg()){
            case "REQUEST_TIMESTAMP":{                                 
                                        out.writeObject(CE.getMessagePacket("TIMESTAMP"));
                                        out.flush();
                                        out.writeObject(new ReliableData("TIMESTAMP",CE.getMyTIMESTAMP()));
                                        out.flush();            
                                     }
            case "REQUEST_PEERMAP":{
                                        out.writeObject(CE.getMessagePacket("PEERMAP"));
                                        out.flush();
                                        out.writeObject(new ReliableData("PEERMAP",CE.getPeerMap()));
                                        out.flush();
                                   }
            case "DEL":           {
                                        CE.popFromPeerMap(request.getUsername());
                                  }
        
            }
         out.close();
         sendSocket.close();
    }
    
}
