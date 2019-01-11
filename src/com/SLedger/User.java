package com.SLedger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

/*
*URL params*
**candidate**: access key given at top of prompt
**public_key**: node name
**amount**: initial starting funds
**private_key**: secret key to submit payments to FakeChain
**peering_info**: custom JSON object of your design containing information
used to connect to other users in the network

**example response**: "success"
 */
public class User {
//    A instance of a logger use for logging.
    private final static Logger LOGGER = Logger.getLogger(User.class.getName());

    private String candidate;
    private String pubkey;
    private String privkey;
    private String ip;
    private String port;
    private double balance;

    //create new "account" to be associated with a given trustline object
    public User(String name, String ip, String pubkey, String port){
        this.candidate = name;
        this.ip = ip;
        this.port = port;
        this.pubkey = pubkey;
        balance = 0;
    }

//    //create new "account" to be associated with a given trustline object for CURRENT USER
//    public User(String name, String pubkey, String privkey){
//        this.candidate = name;
//        String ip = grabIp();
//        if(!ip.equals("error")){
//            this.ip = grabIp();
//            this.port = "3001";
//        }
//
//        this.pubkey = pubkey;
//        this.privkey = privkey;
//        balance = 0;
//    }

    /*grab current user ip
    adopted from https://www.geeksforgeeks.org/java-program-find-ip-address-computer/ */
    public String grabIp(){
        // Find public IP address
        String systemipaddress = "";
        try{
            URL url_name = new URL("http://bot.whatismyipaddress.com");
            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));

            // reads system IPAddress
            systemipaddress = sc.readLine().trim();
        }
        catch (Exception e){
            systemipaddress = "error";
            LOGGER.info("Could not fetch this systems ip address");

        }
//        System.out.println("Public IP Address: " + systemipaddress +"\n");
        return systemipaddress;
    }

    public void update(double amount){
        balance += amount;
    }


    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
    }

    public String getPrivkey() {
        return privkey;
    }

    public void setPrivkey(String privkey) {
        this.privkey = privkey;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public synchronized void setBalance(double balance) {
        this.balance = balance;
    }

    public synchronized double getBalance() {
        return balance;
    }
}
