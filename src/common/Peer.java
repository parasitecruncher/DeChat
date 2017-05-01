/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package common;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author root
 */
public class Peer implements Serializable {
    public String ip;
    public String username;
    private long TIMESTAMP; //of last access to rendezvuos server
    
    public Peer(String ip, String username){
        this.ip   = ip;
        this.username = username;
        TIMESTAMP = (long)0.00;
    }
    public void updateTIMESTAMP(long newTS){
        TIMESTAMP = newTS;
    }
    public long getTIMESTAMP(){
        return TIMESTAMP;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Peer other = (Peer) obj;
        if (!Objects.equals(this.ip, other.ip)) {
            return false;
        }
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Peer " + username + " (" + ip + ")";
    }
    
    
}
