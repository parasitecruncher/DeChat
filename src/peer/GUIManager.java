/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peer;

import GUI.ClientController;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import server.RendezousServer;
import GUI.ClientController;
import GUI.ServerController;
import common.Constants;
import common.Constants.MODE;
import common.Peer;
import java.util.HashMap;
import java.util.Optional;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import server.RendezvousThread;
/**
 *
 * @author root
 */
public class GUIManager extends Application{
    FXMLLoader loader;
    AnchorPane anchorPane;
    ClientController clientController;
    ServerController serverController;
    Scene scene;
    public ChatEngine chatEngine;
    @Override
    public void start(Stage primaryStage) throws FileNotFoundException, IOException {
        MODE mode = modeSelectionDialog();
        if(null == mode){
            //Cancel
        }
        else switch (mode) {
            case CLIENT:
                
                initCE();//Dialog to get username from dialog and initiating CE
                loader = new FXMLLoader();
                anchorPane = (AnchorPane)loader.load(getClass().getResource("/GUI/Client.fxml").openStream());
                clientController=(ClientController)loader.getController();
                clientController.setEngine(this,primaryStage);
                scene = new Scene(anchorPane);
                primaryStage.setTitle("DeCHAT");
                primaryStage.setScene(scene);
                primaryStage.show();
                break;
            case SERVER:
                server.RendezousServer.startServer(this);
                loader = new FXMLLoader();
                //FileInputStream fxmlStream = new FileInputStream(view);
                anchorPane = (AnchorPane)loader.load(getClass().getResource("/GUI/Server.fxml").openStream());
                serverController=(ServerController)loader.getController();
                serverController.setEngine(this);
                scene = new Scene(anchorPane);
                primaryStage.setTitle("DeCHAT");
                primaryStage.setScene(scene);
                primaryStage.show();
                break;
            default:
                break;
        }
        
    }
    //Called before launch of the Application
    public MODE modeSelectionDialog(){
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("DeCHAT");
        alert.setHeaderText("Choose Mode");
        ButtonType serverbutton = new ButtonType("Server");
        ButtonType clientbutton = new ButtonType("Client");
        ButtonType cancel = new ButtonType("Cancel",ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(serverbutton,clientbutton,cancel);
        Optional<ButtonType> result=alert.showAndWait();
        if(result.get()==serverbutton){
            return MODE.SERVER;
        }
        else if(result.get()==clientbutton){
            return MODE.CLIENT;
        }
        else{
            return MODE.CANCEL;
        }
    }
    
    public void client_SendText(String username, String text) {
        //TODO : Send test to username
        chatEngine.sendMsg(username, text);
    }
    public void serverAppendPeerlistview(Peer peer){
        serverController.appendtoPeerlist(peer.username+" @ "+peer.ip);
        //TODO
    }
    public void serverAppendLoggerLabel(String username,String requestType){
        serverController.appendtoLogs("USERNAME:"+username+" "
                + "REQUEST TYPE:"+requestType);
        //TODO
    }
    public void client_updatePeerList(HashMap<String,Peer> peerMap) {
        clientController.updatePeerList(peerMap);
        // TODO: Update peerlist when user updates from peer with best timestamp
    }
    
    public void signin(String username){
        //GET USERNAME FROM GUI
        chatEngine = new ChatEngine(username,this);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);

        //if server launch server else launch client
        
    }

    public void update() {
        Task task = new Task<Void>() {
                @Override
                public Void call() {
                    try {
                        chatEngine.requestTIMESTAMPFromAll();
                        Thread.sleep(10000);
                        chatEngine.requestPeerMapFromBestPeer();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        task.setOnSucceeded(taskFinishEvent -> clientController.updatePeerList(chatEngine.getPeerMap()));

        // TODO: Update peerlist when user updates from peer with best timestamp
    }

    private void initCE() {
        TextInputDialog dialog =new TextInputDialog("Enter Username");
        dialog.setTitle("DeChat");
        dialog.setContentText("Please enter your Username");
        Optional<String> result=dialog.showAndWait();
        if(result.isPresent()){
            signin(result.get());
        }
    }

    
}