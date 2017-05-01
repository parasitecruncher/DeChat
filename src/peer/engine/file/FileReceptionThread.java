/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peer.engine.file;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author root
 */
public class FileReceptionThread  extends Thread{
    protected Socket recvSocket;
    FileOutputStream fos;
    BufferedInputStream inp;
    byte[] buffer;
    
    public FileReceptionThread(Socket recvSocket){
        this.recvSocket = recvSocket;
        try {
            inp = new BufferedInputStream(recvSocket.getInputStream());
            buffer = new byte[4096];
            ObjectInputStream ois = new ObjectInputStream(inp);
            String filename = (String)ois.readObject();
            fos = new FileOutputStream(new File(filename));
        } catch (Exception ex) {
            Logger.getLogger(FileReceptionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    //------------RUN()---------------------------------
    public void run(){
        try {
            while(inp.read(buffer)!=-1)
            {
             fos.write(buffer);
                System.err.println("byte recieved..");
            }
            fos.close();
            inp.close();
            recvSocket.close();
            
        } catch (IOException ex) {
            Logger.getLogger(FileReceptionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //-----------------------------------------------
    
     
}
