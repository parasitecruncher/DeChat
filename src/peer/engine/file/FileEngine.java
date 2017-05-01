/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peer.engine.file;

import common.Constants;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.File;
import common.*;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import peer.engine.text.TextReceptionThread;

/**
 *
 * @author root
 */

/*ASSUMPTIONS : the other person tells me what file to send using text msg.

note the convention:
--->"SERVER SOCKET" for a peer is used only to RECIEVE the file .
--->a new "SOCKET" is created to SEND file  to other peer

*/

public class FileEngine {
    ServerSocket FServerSocket; //Listens ...i.e only to RECIEVE file
    Socket FSocket; // Only to SEND file
    
    public FileEngine(){
        try {
            FServerSocket = new ServerSocket(Constants.FILE_SERVER_PORT);            
        } catch (IOException ex) {
            Logger.getLogger(FileEngine.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    //--Start Listening 
    public void start(){
        System.err.println("about to start the FILE  listerning thread");
        new Thread(){
            @Override
            public void run() {                
                while(true){
                    try{
                        Socket recvSocket = FServerSocket.accept();
                        new FileReceptionThread(recvSocket).start();
                       }
                    catch(IOException ioe){
                        System.err.println("ERROR: FileEngine , start().");
                        ioe.printStackTrace();
                       }
                }
            }                       
        }.start();   
    }
        
    
    
    
    public Boolean sendFile(Peer destPeer,String pathToFile){
        Path path = Paths.get(pathToFile);
        File fileToSend = path.toFile();
        if(!fileToSend.exists())
        {
            return false;
        }
        try {
            FSocket = new Socket(destPeer.ip,Constants.FILE_SERVER_PORT);
            FileInputStream fis = new FileInputStream(fileToSend);
            BufferedOutputStream out = new BufferedOutputStream(FSocket.getOutputStream());
            byte[] buffer = new byte[4096];
            
            //First message to be sent.. before actual file transfer
            //this msg includes the FILENAME that the other person uses to save the incoming file
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(fileToSend.getName());
            

            //----file transfer----
            while(fis.read(buffer)!=-1)
            {
                out.write(buffer, 0, buffer.length);
                System.err.println("Byte sent...-->");
                out.flush();
            }
            //---file tranfser done----
            out.flush();
            out.close();
            oos.close();
            fis.close();
            FSocket.close();
            
        } catch (Exception ex) {
            Logger.getLogger(FileEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        
        return true;
    }
    
    
    
    
}
