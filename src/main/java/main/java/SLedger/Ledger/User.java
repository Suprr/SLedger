package main.java.SLedger.Ledger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.utils.URIBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.logging.Logger;

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
    public User(String name, String ip, String pubkey, String port) {
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
    public String grabIp() {
        // Find public IP address
        String systemipaddress = "";
        try {
            URL url_name = new URL("http://bot.whatismyipaddress.com");
            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));

            // reads system IPAddress
            systemipaddress = sc.readLine().trim();
        } catch (Exception e) {
            systemipaddress = "error";
            LOGGER.info("Could not fetch this systems ip address");

        }
//        System.out.println("Public IP Address: " + systemipaddress +"\n");
        return systemipaddress;
    }

    public void update(double amount) {
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

    //inserts a user into the blockchain
    public void addUserAPI(String[] args, String port) throws IOException, URISyntaxException {
        if (args.length == 5) {
            String urlstring = "ec2-34-222-59-29.us-west-2.compute.amazonaws.com";
            URIBuilder builder = new URIBuilder()
                    .setScheme("http")
                    .setPort(5000)
                    .setHost(urlstring);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode json = mapper.createObjectNode();

            json.put("ip", args[4]);
            json.put("port", port);

            URI uri = builder.build();
            urlstring = uri.toString()
                    + "/add_user?"
                    + "candidate=" + args[0]
                    + "&public_key=" + args[1]
                    + "&amount=" + args[2]
                    + "&private_key=" + args[3]
                    + "&peering_info=" + URLEncoder.encode(mapper.writeValueAsString(json), "UTF-8");


//            System.out.println(URLEncoder.encode(mapper.writeValueAsString(json), "UTF-8"));

//            System.out.println(urlstring);
            //send request
            URL url = new URL(urlstring);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("GET");

            int status = con.getResponseCode();
            if (status > 299) {
                LOGGER.info("Failed to publish user to api");
                con.disconnect();
            } else {
                System.out.println("User " + args[0] + " created and registered on FakeChain!");
            }
        } else {
            System.out.println("Not enough input arguments.");
        }
    }
}
