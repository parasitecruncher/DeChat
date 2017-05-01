/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.Serializable;

/**
 *
 * @author root
 */
public class Query implements Serializable {

    public String type;
    public String username;
    public Peer sender;

    public Query(String type, Peer peer) {
        this.type = type;
        this.sender = peer;
    }

    public Query(String type, String username, Peer peer) {
        this.type = type;
        this.username = username;
        this.sender = peer;
    }
}
