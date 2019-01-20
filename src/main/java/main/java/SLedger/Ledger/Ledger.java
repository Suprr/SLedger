package main.java.SLedger.Ledger;

import com.fasterxml.jackson.core.JsonFactory;
import main.java.SLedger.Main;
import main.java.SLedger.Server.Client;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.java.SLedger.Server.Opcode;
import main.java.SLedger.Server.Server;
import org.apache.http.client.utils.URIBuilder;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

//Used to manage Trustlines for the current user on FakeChain
public class Ledger {

    //A instance of a logger use for logging.
    private final static Logger LOGGER = Logger.getLogger(Ledger.class.getName());

    private volatile Queue<main.java.SLedger.Ledger.Transaction> transactions;
    private volatile List<Trustline> trustlines;
    private volatile User user;
    private volatile int total;

    public Ledger() {
        this.transactions = new LinkedList<>();
        this.trustlines = new ArrayList<>();
        total = 0;
    }

    public void assignCurrentUser(String[] args, String port) throws Exception {
        if (args.length == 4) {
            String name = args[0];
            String pkey = args[1];
            String prkey = args[2];
            int startAmount = Integer.parseInt(args[3]);
            String ip = User.grabIp();
            try {
                user = new User(name, ip, port, pkey, prkey, 0);

                //add the user to the blockchain
                User.addUserAPI(new User(name, ip, port, pkey, prkey, startAmount), port);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } else {
            System.out.println("Invalid input arguments, restart the program with correct arguments");
        }
    }

    //establish a trustline between two peers on FakeChain
    public void createTrustline(String peerName, String ip, String port, CountDownLatch latch) throws IOException, URISyntaxException, InterruptedException {

        //create user to be assigned to a trustline
        User userFromApi = getUserAPI(peerName);
        userFromApi.setBalance(0);
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
                    new Client(newLine, Opcode.CLIENT_CONNECTING + "");
                    latch.countDown();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            Thread thread = new Thread(r);
            thread.start();
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
        if (users != null) {
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
            } else {
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
//        System.out.println(json);

        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        JsonNode rootNode = mapper.readTree(json);
        List<User> users = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();
        while (fieldsIterator.hasNext()) {
            String pubkey = "";
            int amount = 0;
            String ip = "";
            String name = "";
            String port = "";

            Map.Entry<String, JsonNode> field = fieldsIterator.next();
//            System.out.println("Key: " + field.getKey() + "\tValue:" + field.getValue());
            pubkey = field.getKey() + "";
            //inner nested
            JsonFactory innerfactory = new JsonFactory();
            ObjectMapper innermapper = new ObjectMapper(innerfactory);

            String innerarray = field.getValue() + "";
//            System.out.println(innerarray);
            JsonNode innerrootNode = innermapper.readTree(innerarray);

            Iterator<Map.Entry<String, JsonNode>> innerfieldsInterator = innerrootNode.fields();
            while (innerfieldsInterator.hasNext()) {
                Map.Entry<String, JsonNode> innerfield = innerfieldsInterator.next();
//                System.out.println("Key: " + innerfield.getKey() + "\tValue:" + innerfield.getValue());

                if ((innerfield.getKey() + "").equals("amount")) {
                    amount = Integer.parseInt(innerfield.getValue() + "");
                }

                //innerinner nested
                JsonFactory innerinnerfactory = new JsonFactory();
                ObjectMapper innerinnermapper = new ObjectMapper(innerinnerfactory);

                String innerinnerarray = innerfield.getValue() + "";
                JsonNode innerinnerrootNode = innerinnermapper.readTree(innerinnerarray);

                Iterator<Map.Entry<String, JsonNode>> innerinnerfieldsInterator = innerinnerrootNode.fields();
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
            User userToAdd = new User(name, ip, port, pubkey, amount);
//            System.out.println(userToAdd.toString());
            users.add(userToAdd);
        }
        return users;
    }

    //establish a trustline object between two unique identities
    public void receiveTrustline(String peerName) throws IOException, URISyntaxException {
        User userFromApi = getUserAPI(peerName);
        userFromApi.setBalance(0);
        boolean found = true;
        if (found && userFromApi != null) {
            for (Trustline t : trustlines) {
                if (t.getReceiver().getName().equals(peerName)) {
                    System.out.println("Trustline already created with " + peerName);
                    return;
                }
            }
            // fetch user if they exist in blockchain
            Trustline newLine = new Trustline(user, userFromApi);
            addToLines(newLine);
            System.out.println("Trustline with " + capitalize(userFromApi.getName()) + " established.");
        } else {
            System.out.println("This person does not exist on the blockchain");
        }
    }

    //create a transaction to be added to the queue of transactions
    public boolean receieveTransaction(String candidate, int amount) throws IOException, URISyntaxException {
        Trustline line = null;
        boolean trustlineFlag = false;

        //find if trustline for chosen candidate exists
        for (Trustline tmp : trustlines) {
            if (tmp.getReceiver().getName().equals(candidate)) {
                line = tmp;
                //candidate found
                trustlineFlag = true;
                break;
            }
        }

        //does user exist in api
        boolean found = false;
        if (getUserAPI(candidate) != null) {
            found = true;
        }

        // we have a trustline and they exist in the api
        if (found && trustlineFlag) {
            User sender = line.getReceiver();

            // simply add the transaction... sender already verified trustline balances
            sender.setBalance(sender.getBalance() + amount);
            user.setBalance(user.getBalance() - amount);
            updateTotal(amount);

            Transaction newTrans = new Transaction(user, sender, line, amount);
            addToTransactions(newTrans);
            return true;
        } else {
            LOGGER.info("Transaction failed user does not exist");
            return false;
        }
    }

    //check if trustline balance will exceed 100 needed for settlement
    public int verifyBalance(User user, int amount) {
        int receiverbal = user.getBalance();
        if (receiverbal + amount > 100) {
            return receiverbal + amount - 100;
        } else return 0;
    }

    @SuppressWarnings("Duplicates")
    //create a transaction to be added to the queue of transactions
    public boolean createTransaction(String candidate, int amount, CountDownLatch latch) throws InterruptedException {
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
            User sender = line.getSender();

            // check threshold
            int newbalance = verifyBalance(sender, amount);
            // we have not reached our credit limit of 100, simply add the transaction
            if (newbalance == 0) {
                Transaction newTrans = new Transaction(user, line.getReceiver(), line, amount);
                Trustline finalLine = line;

                Runnable r = () -> {
                    try {
                        new Client(finalLine, newTrans, Opcode.CLIENT_PAYMENT + "|" + amount);
                        latch.countDown();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
                Thread tranasction = new Thread(r);
                tranasction.start();

                return true;
            } else {
                Transaction newTrans = new Transaction(user, line.getReceiver(), line, newbalance);
                Trustline finalLine = line;

                Runnable r = () -> {
                    try {
                        new Client(finalLine, newTrans, Opcode.CLIENT_SETTLE + "|" + newbalance);
                        new Client(finalLine, newTrans, Opcode.CLIENT_PAYMENT + "|" + newbalance);

                        latch.countDown();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
                Thread tranasction = new Thread(r);
                tranasction.start();

                return true;
            }

        } else {
            LOGGER.info("Transaction failed - no TrustLine found");
            return false;
        }
    }

    public static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    public synchronized void updateTotal(int amount) {
        total += amount;
    }

    //lists all balances available
    public void balance() {
        for (Trustline t : trustlines) {
            String peer = capitalize(t.getReceiver().getName());
            int balance = t.getReceiver().getBalance();
            System.out.print(peer + ": " + balance);
        }
        System.out.println("\nTotal: " + total+ "\n");
    }

    private void payFakeChain() {
    }

    public synchronized Queue<Transaction> getTransactions() {
        return transactions;
    }

    public synchronized List<Trustline> getTrustlines() {
        return trustlines;
    }

    public synchronized void addToLines(Trustline t) {
        getTrustlines().add(t);
    }

    public synchronized void addToTransactions(Transaction t) {
        getTransactions().add(t);
    }

    // clear out old transactions
    public synchronized void settle(Trustline trustline) {
        String sender = trustline.getSender().getName();
        String receiver = trustline.getReceiver().getName();
        for (Transaction t : transactions) {
            if (t.getReceiver().getName().equals(receiver) &&
                    t.getSender().getName().equals(sender)) {
                // update the total so it doesnt reflect the payments that were settled
                updateTotal(t.getAmount());
                // update the senders balance
                t.getSender().setBalance(t.getSender().getBalance() + t.getAmount());
                t.getReceiver().setBalance(t.getReceiver().getBalance() - t.getAmount());

                removeTransaction(t);
            }
        }
    }

    public synchronized void removeTransaction(Transaction t) {
        transactions.remove(t);
    }

}
