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

    private String name;
    private String pubkey;
    private String privkey;
    private String ip;
    private String port;
    private double balance;

    //for current user
    public User(String name,String ip, String port, String pubkey, String privkey, double balance) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.pubkey = pubkey;
        this.privkey = privkey;
        this.balance = balance;
    }

    //create new "account" to be associated with a given trustline object
    public User(String name, String ip, String port, String pubkey, double balance) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.pubkey = pubkey;
        this.privkey = "";
        this.balance = balance;
    }

    /*grab current user ip
    adopted from https://www.geeksforgeeks.org/java-program-find-ip-address-computer/ */
    public static String grabIp() {
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

    public String getName() {
        return name;
    }

    public void setName(String candidate) {
        this.name = candidate;
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
    public void addUserAPI(User user, String port) throws IOException, URISyntaxException {
            String urlstring = "ec2-34-222-59-29.us-west-2.compute.amazonaws.com";
            URIBuilder builder = new URIBuilder()
                    .setScheme("http")
                    .setPort(5000)
                    .setHost(urlstring);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode json = mapper.createObjectNode();

            json.put("ip", user.getIp());
            json.put("port", port);

            URI uri = builder.build();
            urlstring = uri.toString()
                    + "/add_user?"
                    + "candidate=" + "rizzo"
                    + "&public_key=" + user.getPubkey()
                    + "&amount=" + user.getBalance()
                    + "&private_key=" + user.getPrivkey()
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
                System.out.println("User " + user.getPubkey() + " created and registered on FakeChain!");
            }
        }

    @Override
    public String toString() {
        return "User{" +
                "name=" + name +
                ", pubkey=" + pubkey  +
                ", privkey=" + privkey  +
                ", ip=" + ip +
                ", port=" + port +
                ", balance=" + balance +
                '}';
    }

}
