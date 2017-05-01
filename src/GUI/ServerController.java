/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import peer.GUIManager;

/**
 * FXML Controller class
 *
 * @author prashanth
 */
public class ServerController implements Initializable {
    @FXML
    private ListView connectionlogs;
    @FXML
    private ListView serverpeerlist;
    
    private GUIManager deChat;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    public void appendtoPeerlist(String peer){
        serverpeerlist.getItems().add(peer);
    }
    
    public void setEngine(GUIManager deChat) {
        this.deChat=deChat;
    }

    public void appendtoLogs(String username) {
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                connectionlogs.getItems().add(username);
                connectionlogs.scrollTo(connectionlogs.getItems().size()-1);            
            }
        });
        
    }
    
}
