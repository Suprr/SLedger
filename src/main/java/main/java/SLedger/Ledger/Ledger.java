package main.java.SLedger.Ledger;

import com.fasterxml.jackson.core.JsonFactory;
import main.java.SLedger.Main;
import main.java.SLedger.Server.Client;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.java.SLedger.Server.Opcode;
import main.java.SLedger.Server.Server;
import org.apache.http.client.utils.URIBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

import java.security.PrivateKey;
import java.util.*;
import java.util.logging.Logger;

//Used to settle outstanding balances for the current user and FakeChain
public class Ledger {
    //A instance of a logger use for logging.
    private final static Logger LOGGER = Logger.getLogger(Ledger.class.getName());

    private static Server server;
    private Queue<main.java.SLedger.Ledger.Transaction> transactions;
    private List<Trustline> trustlines;
    private User user;
    private double total;

    public Ledger() {
        this.transactions = new LinkedList<>();
        this.trustlines = new ArrayList<>();
        this.server = Main.getServer();
        total = 0;
    }

    public void assignCurrentUser(String[] args, String port) throws Exception {
        if (args.length == 4) {
            String name = args[0];
            String pkey = args[1];
            String prkey = args[2];
            double startAmount = Double.parseDouble(args[3]);
            String ip = User.grabIp();
            try {
                user = new User(name, ip, port, pkey, prkey, startAmount);

                //add the user to the blockchain
                user.addUserAPI(user, port);
            }catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }else {
            System.out.println("Invalid input arguments, restart the program with correct arguments");
        }
    }

    //establish a trustline between two peers on FakeChain
    public void createTrustline(String peerName, String ip, String port) throws IOException, URISyntaxException {
        //create user to be assigned to a trustline

        User userFromApi = getUserAPI(peerName);
        if (userFromApi != null) {
            for (Trustline t : trustlines) {
                if (t.getReceiver().getName().equals(peerName)) {
                    System.out.println("Trustline already created with " + peerName);
                    return;
                }
            }

            Trustline newLine = new Trustline(user, userFromApi);

            Runnable r = () -> {
                try {
                    new Client(newLine, Opcode.CLIENT_CONNECTING+"");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
        } else {
            System.out.println("This person does not exist on the blockchain");
        }
    }

    //check if a user exists on blockchain
    public User getUserAPI(String name) throws IOException, URISyntaxException {
        User userFromApi = null;
        String urlstring = "ec2-34-222-59-29.us-west-2.compute.amazonaws.com";
        URIBuilder builder = new URIBuilder()
                .setScheme("http")
                .setPort(5000)
                .setHost(urlstring);

        URI uri = builder.build();
        urlstring = uri.toString()
                + "/get_users?"
                + "candidate=" + "rizzo";

        //send request
        URL url = new URL(urlstring);
//        System.out.println(urlstring);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(
                con.getInputStream()));

        String inputLine = in.readLine();
        in.close();

        //parse json
        List<User> users = parse(inputLine);
        boolean found = false;
        if(users!=null){
            for (User tmp : users) {
                if (tmp.getName().equals(name)) {
                    userFromApi = tmp;
                    found = true;
                    break;
                }
            }

            if (found) {
                con.disconnect();
//                System.out.println(userFromApi);
                return userFromApi;
            }else {
                LOGGER.info("Failed to find user in FakeChain");
                con.disconnect();
                return null;
            }
        } else {
            LOGGER.info("Failed to retrieve any users from FakeChain");
            con.disconnect();
            return null;
        }
    }


    public String stripquotes(String txt) {
        if (txt.length() >= 2 && txt.charAt(0) == '"' && txt.charAt(txt.length() - 1) == '"') {
            txt = txt.substring(1, txt.length() - 1);
        }
        return txt;
    }

    //{"pubkey2": {"amount": 99, "peering_info": {"ip": "ipaddress", "name": "bob", "port": "port"}}}
    public List<User> parse(String json) throws IOException {
        System.out.println(json);

        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        JsonNode rootNode = mapper.readTree(json);
        List<User> users =  new ArrayList<>();
        Iterator<Map.Entry<String,JsonNode>> fieldsIterator = rootNode.fields();
        while (fieldsIterator.hasNext()) {
            String pubkey = "";
            double amount = 0;
            String ip = "";
            String name = "";
            String port = "";

            Map.Entry<String,JsonNode> field = fieldsIterator.next();
//            System.out.println("Key: " + field.getKey() + "\tValue:" + field.getValue());
            pubkey = field.getKey() + "";
            //inner nested
            JsonFactory innerfactory = new JsonFactory();
            ObjectMapper innermapper = new ObjectMapper(innerfactory);

            String innerarray = field.getValue() + "";
//            System.out.println(innerarray);
            JsonNode innerrootNode = innermapper.readTree(innerarray);

            Iterator<Map.Entry<String,JsonNode>> innerfieldsInterator = innerrootNode.fields();
//            System.out.println("FIRST INNER");
            while (innerfieldsInterator.hasNext()) {
                Map.Entry<String,JsonNode> innerfield = innerfieldsInterator.next();
//                System.out.println("Key: " + innerfield.getKey() + "\tValue:" + innerfield.getValue());

                if ((innerfield.getKey()+"").equals("amount")){
                    amount = Double.parseDouble(innerfield.getValue()+"");
                }

                //innerinner nested
                JsonFactory innerinnerfactory = new JsonFactory();
                ObjectMapper innerinnermapper = new ObjectMapper(innerinnerfactory);

                String innerinnerarray = innerfield.getValue() + "";
//                System.out.println(innerinnerarray);
                JsonNode innerinnerrootNode = innerinnermapper.readTree(innerinnerarray);

                Iterator<Map.Entry<String,JsonNode>> innerinnerfieldsInterator = innerinnerrootNode.fields();
//                System.out.println("SECOND INNER");
                while (innerinnerfieldsInterator.hasNext()) {
                    Map.Entry<String, JsonNode> innerinnerfield = innerinnerfieldsInterator.next();
//                    System.out.println("Key: " + innerinnerfield.getKey() + "\tValue:" + innerinnerfield.getValue());

                    switch ((innerinnerfield.getKey())) {
                        case "name":
                            name = stripquotes(innerinnerfield.getValue() + "");
                            break;
                        case "ip":
                            ip = stripquotes(innerinnerfield.getValue() + "");
                            break;
                        case "port":
                            port = stripquotes(innerinnerfield.getValue() + "");
                            break;
                    }
                }
            }
//            System.out.println(name + " " + ip + " "+ port + " "+ pubkey+ " " + amount);
            User userToAdd = new User(name, ip, port, pubkey, amount);
//            System.out.println(userToAdd.toString());
            users.add(userToAdd);
        }

//        for (User user : users){
//            System.out.println(user.toString());
//        }
        return users;
    }

    //establish a trustline object between two unique identities
    public void receiveTrustline(String peerName) throws IOException, URISyntaxException {
//        boolean found = getUserAPI(peerName);
        boolean found = true;
        if (found) {
            for (Trustline t : trustlines) {
                if (t.getReceiver().getName().equals(peerName)) {
                    System.out.println("Trustline already created with " + peerName);
                    return;
                }
            }

            String response = getUserAPIstring(peerName);
            ObjectMapper map = new ObjectMapper();
            JsonNode json = map.readTree(response);
//            System.out.println(json.toString());
            json.get("ip");
            json.get("port");
//            create user to be assigned to a trustline
//            User peer = new User(peerName, ip, pubkey, port);
//
//            Trustline newLine = new Trustline(user, peer);
//            trustlines.add(newLine);
        } else {
            System.out.println("This person does not exist on the blockchain");
        }
    }

    //fetch user from FakeChain and return its stringified json response for parsing
    public String getUserAPIstring(String candidate) throws IOException, URISyntaxException {
        String urlstring = "ec2-34-222-59-29.us-west-2.compute.amazonaws.com";
        URIBuilder builder = new URIBuilder()
                .setScheme("http")
                .setPort(5000)
                .setHost(urlstring);

        URI uri = builder.build();
        urlstring = uri.toString()
                + "/get_users?"
                + "candidate=" + candidate;

        //send request
        URL url = new URL(urlstring);
//            System.out.println(urlstring);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(
                con.getInputStream()));

        String inputLine = in.readLine();

        in.close();

        if (inputLine.length() > 5) {
            con.disconnect();
            return inputLine;
        } else {
            LOGGER.info("Failed to find user in FakeChain");
            con.disconnect();
            return null;
        }
    }

    //create a transaction to be added to the queue of transactions
    public boolean receieveTransaction(String candidate, double amount) throws IOException, URISyntaxException {
        Trustline line = null;
        boolean flag = false;

        //find if trustline for chosen candidate exists
        for (Trustline tmp : trustlines) {
            if (tmp.getReceiver().getName().equals(candidate)) {
                line = tmp;
                //candidate found
                flag = true;
                break;
            }
        }

//        boolean found = getUserAPI(candidate);
        boolean found = true;
        if (found && flag) {
            User sender = line.getSender();

            double newbalance = verifyBalance(sender, amount);
            //we have not reached our credit limit of 100, simply add the transaction
            if (newbalance == 0) {
                sender.setBalance(sender.getBalance() - amount);
                updateTotal(amount);

                Transaction newTrans = new Transaction(user, sender, line, amount);
                transactions.add(newTrans);
                return true;

            } else {
//                    settleBalance(line);
                //reset receiver balance to remainder of settlement {100-remainder}
                sender.setBalance(newbalance);
                //update users total to account for settlement
                updateTotal(100);
                return true;
            }
        } else {
            LOGGER.info("Transaction Failed");
            return false;
        }
    }

    //check if trustline balance will exceed 100 needed for settlement
    public double verifyBalance(User user, double amount) {
        double receiverbal = user.getBalance();
        if (receiverbal + amount > 100) {
            return receiverbal + amount - 100;
        } else return 0;
    }

    @SuppressWarnings("Duplicates")
    //create a transaction to be added to the queue of transactions
    public boolean createTransaction(String candidate, double amount){
        Trustline line = null;
        boolean flag = false;

        //find if trustline for chosen candidate exists
        for (Trustline tmp : trustlines) {
            if (tmp.getReceiver().getName().equals(candidate)) {
                line = tmp;
                //candidate found
                flag = true;
                break;
            }
        }

        // Trustline exists for this candidate
        if (flag) {
            User receiver = line.getReceiver();

            // check threshold
            double newbalance = verifyBalance(receiver, amount);
            // we have not reached our credit limit of 100, simply add the transaction
            if (newbalance == 0) {
                Transaction newTrans = new Transaction(user, receiver, line, amount);
                Trustline finalLine = line;

                Runnable r = () -> {
                    try {
                        new Client(finalLine, newTrans,Opcode.CLIENT_PAYMENT+"|"+amount);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

                return true;
            } else {
                Transaction newTrans = new Transaction(user, receiver, line, newbalance);
                Trustline finalLine = line;

                Runnable r = () -> {
                    try {
                        new Client(finalLine, newTrans,Opcode.CLIENT_SETTLE+"|"+newbalance);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

                return true;
            }

        } else {
            LOGGER.info("Transaction failed - no TrustLine found");
            return false;
        }
    }

    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    public synchronized void updateTotal(double amount) {
        total += amount;
    }

    //lists all balances available
    public void balance() {
        for (Trustline t : trustlines) {
            String peer = capitalize(t.getReceiver().getName());
            double balance = t.getReceiver().getBalance();
            System.out.print(peer + ": " + balance);
        }
        System.out.println("Total: " + total);
    }

    private void payFakeChain() {
    }

    public synchronized Queue<Transaction> getTransactions() {
        return transactions;
    }

    public synchronized List<Trustline> getTrustlines() {
        return trustlines;
    }

    public synchronized void addToLines(Trustline t){
        getTrustlines().add(t);
    }

    public synchronized void addToTransactions(Transaction t){
        getTransactions().add(t);
    }

    // clear out old transactions
    public synchronized void settle(Trustline trustline) {
        String sender = trustline.getSender().getName();
        String receiver = trustline.getReceiver().getName();
        for(Transaction t : transactions){
            if (t.getReceiver().getName().equals(receiver) &&
                    t.getSender().getName().equals(sender)){
                // update the total so it doesnt reflect the payments that were settled
                updateTotal(t.getAmount());
                transactions.remove(t);
            }
        }
    }
}