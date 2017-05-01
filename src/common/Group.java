/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author root
 */
public class Group implements Serializable{
    private String groupName;
    ArrayList<Peer> members;
    
    public Group(String groupname){
        this.groupName = groupname;
    }
    public void addMember(Peer newMember){
        this.members.add(newMember);
    }
    
    public String getGroupName(){
        return groupName;
    }
    public ArrayList<Peer> getMemberList(){
        return members;
    }
    
}
