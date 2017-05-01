/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import common.Peer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import peer.GUIManager;
/**
 * FXML Controller class
 *
 * @author prashanth
 */
public class ClientController implements Initializable {
    @FXML
    private ListView peerlist;
    @FXML
    private ListView chatlogs;
    @FXML
    private ScrollBar peerlistscroll;
    @FXML
    private TextField textfield;
    @FXML
    private Button send;
    @FXML
    private Button fileButton;
    
    @FXML
    private Button update;
    private GUIManager dchat;
    Stage primaryStage;
    ObservableList<String> peers = FXCollections.<String>observableArrayList();

    /**
     * Initializes the controller class.
     */
    
    @FXML
    public void sendText(){
        String text=textfield.getText();
        textfield.clear();
        dchat.client_SendText(peers.get(peerlist.getSelectionModel().getSelectedIndex()),text);
        
    }
    @FXML
    public void chatLog() throws UnsupportedEncodingException, IOException{
        //TODO : Display Selected Peers ChatLogs
        String username = peers.get(peerlist.getSelectionModel().getSelectedIndex());
        File history = new File(username+".txt");
        if(history.exists()){
            FileInputStream fis;
            try {
                fis = new FileInputStream(username+".");
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-16"));
                String line = reader.readLine();

                while (line != null) {
                    chatlogs.getItems().add(line);
                    line = reader.readLine();
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            
        }

    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        peerlist.setItems(peers);
    }
    @FXML
    public void handleMouseClick(){
        System.out.println("clicked on " + peerlist.getSelectionModel().getSelectedItem());
        try {
            chatLog();
        } catch (IOException ex) {
            Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void setlist(ArrayList<String> items){
        peers.addAll(items);
        peerlist.setItems(peers);
    }
    public void updatePeerList(HashMap<String,Peer> peerMap) {
        peerlist.refresh();
        ArrayList<String> peersAL = new ArrayList(peerMap.keySet());
        peers.clear();
        peers.addAll(peersAL);
        peerlist.refresh();
    }
    
    @FXML
    public void update(){
        dchat.update();
    }
    
    @FXML
    public void sendFile(){
        FileChooser fileChooser = new FileChooser();

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            System.out.println(file.getAbsolutePath());
            dchat.chatEngine.sendFile(peers.get(peerlist.getSelectionModel().getSelectedIndex()),
                    file.getAbsolutePath());
        }
    }

    public void setEngine(GUIManager dchat,Stage primaryStage) {
        this.primaryStage=primaryStage;
        this.dchat=dchat;
    }
}
