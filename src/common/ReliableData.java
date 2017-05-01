/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

/**
 *
 * @author root
 */
public class ReliableData {
    public String type;
    private Object data;
    
    /*
    type: "TIMESTAMP", "PEERMAP", "GROUP_OBJECT"
    */
    public ReliableData(String type,Object data){
        this.type = type;
        this.data = data;
    }      
    public Object getData(){
        return data;
    }
    
    
}
