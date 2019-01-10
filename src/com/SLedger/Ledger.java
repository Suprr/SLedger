package com.SLedger;

import org.apache.http.client.utils.URIBuilder;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.logging.Logger;

//Used to settle outstanding balances for the current user and FakeChain
public class Ledger {
    //A instance of a logger use for logging.
    private final static Logger LOGGER = Logger.getLogger(Ledger.class.getName());
    Queue<Transaction> transactions;
    List<Trustline> trustlines;
    User user;
    double total;

    public Ledger (){
        transactions = new LinkedList<>();
        total = 0;
    }

    public void assignCurrentUser(String[] args) throws Exception {
        String candidate = args[0];
        File tmpDirpk = new File("KeyPair/privateKey");
        boolean existpk = tmpDirpk.exists();
        File tmpDirprk = new File("KeyPair/privateKey");
        boolean existsprk = tmpDirpk.exists();

        //the current user has keys, must already be on fakechain
        if(existpk && existsprk){
            AsymmetricCryptography ac = new AsymmetricCryptography();
            PrivateKey privateKey = ac.getPrivate("KeyPair/privateKey");
            PublicKey publicKey = ac.getPublic("KeyPair/publicKey");

            String pkey = Keys.savePublicKey(publicKey);
            String prkey = Keys.savePrivateKey(privateKey);

            user = new User(candidate, pkey , prkey, null);

            //add the user to the blockchain
            addUserAPI(args);
        }

        //the current user has no keys, must not exist. Need to create keypair and save to file
        else{
            GenerateKeys gk;
            try {
                gk = new GenerateKeys(1024);
                gk.createKeys();

                //save keys for future SLedger use
                gk.writeToFile("KeyPair/publicKey", gk.getPublicKey().getEncoded());
                gk.writeToFile("KeyPair/privateKey", gk.getPrivateKey().getEncoded());

//                System.out.println( gk.getPublicKey().getEncoded());

                String pkey = Keys.savePublicKey(gk.getPublicKey());
                String prkey = Keys.savePrivateKey(gk.getPrivateKey());
                user = new User(candidate, pkey, prkey, null);

                //add the user to the blockchain
                addUserAPI(args);
            } catch (NoSuchAlgorithmException e) {
                System.err.println(e.getMessage());
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    //inserts a user into the blockchain
    public void addUserAPI(String[] args) throws IOException, URISyntaxException {
        String urlstring = "ec2-34-222-59-29.us-west-2.compute.amazonaws.com";
        URIBuilder builder = new URIBuilder()
                .setScheme("http")
                .setPort(5000)
                .setHost(urlstring);

        URI uri = builder.build();
        urlstring = uri.toString()
                        +"/add_user?"
                        +"candidate="+args[0]
                        +"&public_key="+args[1]
                        +"&amount="+args[2]
                        +"&private_key="+args[3]
                        +"&peering_info="+args[4];

        //send request
        URL url = new URL(urlstring);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("GET");

        int status = con.getResponseCode();
        if (status > 299) {
            LOGGER.info("Failed to publish user to api");
            con.disconnect();
        }
    }

    //establish a trustline object between two unique identities
    public Trustline createTrustline(String peerName, String pubkey, String ip){
        //create user to be assigned to a trustline
        User peer = new User(peerName, ip, pubkey);
        return new Trustline(user,peer);
    }

    @SuppressWarnings("Duplicates")
    //create a transaction to be added to the queue of transactions
    public boolean receieveTransaction(String candidate , double amount) {
        Trustline line = null;
        boolean flag = false;

        //find if trustline for chosen candidate exists
        for (Trustline tmp : trustlines) {
            if (tmp.getReceiver().getCandidate() == candidate) {
                line = tmp;
                //candidate found
                flag = true;
                break;
            }
        }
        if (flag == true) {
            User sender = line.getSender();
            User receiver = line.getReceiver();

            sender.setBalance(sender.getBalance() - amount);
            receiver.setBalance(receiver.getBalance() + amount);
            total += amount;

            Transaction newTrans = new Transaction(user, receiver, line, amount);
            line.updateBal(receiver, amount);
            transactions.add(newTrans);
            return true;
        } else {
            LOGGER.info("Transaction Failed");
            return false;
        }
    }

    //check if trustline balance will exceed 100 needed for settlement
    public double verifyBalance(Trustline line, double amount){
        double trustlineBal = line.getBalance();
        if(trustlineBal + amount > 100){
            return trustlineBal + amount - 100;
        }else return 0;
    }

    @SuppressWarnings("Duplicates")
    //create a transaction to be added to the queue of transactions
        public boolean createTransaction(String candidate , double amount){
            Trustline line = null;
            boolean flag = false;

            //find if trustline for chosen candidate exists
            for(Trustline tmp : trustlines){
                if(tmp.getReceiver().getCandidate() == candidate){
                    line = tmp;
                    //candidate found
                    flag = true;
                    break;
                }
            }
            if(flag== true){
                User sender = line.getSender();
                User receiver = line.getReceiver();

                double newbalance = verifyBalance(line, amount);
                //we have not reached our credit limit of 100, simply add the transaction
                if(newbalance==0){
                    total -= amount;
                    sender.setBalance(sender.getBalance()-amount);
                    receiver.setBalance(receiver.getBalance()+amount);
                    total -= amount;

                    Transaction newTrans = new Transaction(user, receiver, line, amount);
                    line.updateBal(receiver, amount);
                    transactions.add(newTrans);
                }else{
                    settleBalances();
                    line.setBalance(newbalance);
                }
                return true;
            }else{
                LOGGER.info("Transaction Failed");
                return false;
            }

        }

    private void settleBalances() {
        for(Transaction t: transactions){
//            t.get
        }
    }
}
