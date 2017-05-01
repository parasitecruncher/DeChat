/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peer.engine.text;

import common.Constants;
import common.Message;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.time.LocalDateTime;

/**
 *
 * @author root
 */
public class TextReceptionThread extends Thread {
    protected Socket clientsocket;
    InputStream inp;
    BufferedReader binp;
    ObjectInputStream objReader;
    String msg;
    
    
    public TextReceptionThread(Socket clientSocket){
        this.clientsocket = clientSocket;
        System.err.println("recieved a new request....in TestReceptionThread");

    }
     private void logmessage(String username, String msg,String type) {
        String filename = username+".txt";
        String data=type+":"+username+":"+msg+":"+LocalDateTime.now().toString();
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
    
    public void run(){
        System.err.println("inside RUN() of TextReceptionThread.()");

        inp  = null;
        binp = null;
        msg = null;
        try{
            inp = clientsocket.getInputStream();
          //  binp = new BufferedReader(new InputStreamReader(inp));
            objReader = new ObjectInputStream(inp);
        }catch(IOException ioe){
            System.err.println("ERROR: While MsgReceptionThread run()");
            ioe.printStackTrace();
        }
        
        while(true){
            try{
                //get recieved msg
                Message msgPkt = (Message)objReader.readObject();
                System.err.println("recieved a new MESSAGE!!!!YAY");

                //dump this msg assoiating it to who sent it , in DB maybe
                
                //WHen the user at the other end deliberately sends a message to serverscoket of some other peer ,then force him to send
                //"END" message to this peer ,so that this guy's TCP connection can be closed.
               /* if(msg.equals("END")){
                    binp.close();
                    inp.close();
                    clientsocket.close();                    
                    return;
                }*/
               if(msgPkt.getGroupName().equals(Constants.DEFAULT_GROUP_NAME)){
                   //This is one-to-one chat
                   System.out.println("message recieved:"+msgPkt.getUsername()+" SAYS:"+msgPkt.getMsg());
                   logmessage(msgPkt.getUsername(), msgPkt.getMsg(), Constants.RECIEVE_MESSAGE);
               }
               else{
                   //This is GROUP CHAT message
                   System.out.println("GROUP MSG RECIEVED: ::GROUP_NAME:"+msgPkt.getGroupName()+"     ::BY:+"+msgPkt.getUsername()+"      "+msgPkt.getMsg());
               }
            }catch(Exception ioe){
                System.err.println("ERROR: while recieving messages.MsgReceptionThread  ");
            }
        }
    }
    
    
}