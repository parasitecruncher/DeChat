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
public class Constants {
    public static final String RENDEZOUS_SERVER_ADDRESS = "0.0.0.0";
    public static final int RENDEZOUS_SERVER_PORT = 33000;
    public static final int TEXT_SERVER_PORT = 33001;
    public static final int FILE_SERVER_PORT = 33002;
    public static final int MISC_TCP_SERVER_PORT = 33007;
    public static final int MISC_UDP_RECV_PORT = 33008;
    public static final int MISC_UDP_SEND_PORT = 33009;
    public static final String SEND_MESSAGE = "SENT";
    public static final String RECIEVE_MESSAGE = "RECIEVED";
    public static final String DEFAULT_MSG_STRING = "Hello there.";
    public static final String DEFAULT_GROUP_NAME = "NONE";
    public static enum MODE{
        SERVER,
        CLIENT,
        CANCEL
    };

}
