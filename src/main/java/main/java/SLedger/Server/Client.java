package main.java.SLedger.Server;

import main.java.SLedger.Ledger.Ledger;
import main.java.SLedger.Ledger.Transaction;
import main.java.SLedger.Ledger.Trustline;
import main.java.SLedger.Ledger.User;
import org.apache.http.client.utils.URIBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

import static main.java.SLedger.Ledger.Ledger.capitalize;

public class Client {
    private Transaction transaction;

    private String serverAddress;

    // TCP Components
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private volatile Ledger ledger;
    private volatile Trustline trustline;
    private volatile User peer;
    private volatile User user;

    public Client(Trustline line, String command) throws IOException {
        this.user = line.getSender();
        this.peer = line.getReceiver();
        this.trustline = line;
        this.ledger = Server.getLedger();
        if (initHostName()) {
            runClient(command);// have fun
        }else{
            System.out.println(peer.getName() + " could not be reached.");
        }
    }

    public Client(Trustline line, Transaction transaction, String command) throws IOException {
        this.user = line.getSender();
        this.peer = line.getReceiver();
        this.trustline = line;
        this.transaction = transaction;
        this.ledger = Server.getLedger();
        if (initHostName()) {
            runClient(command);// have fun
        }else{
            System.out.println(peer.getName() + " could not be reached.");
        }
    }


    public boolean initHostName() throws IOException {
        //replace host name with your computer name or IP address
//            String host = InetAddress.getLocalHost().getHostAddress();
        serverAddress = peer.getIp();
        if (serverAddress == null)
            System.out.println("Invalid IP for peer on this Trustline");

        serverAddress = serverAddress.trim();
        if (serverAddress.length() == 0)// empty field
        {
            System.out.println("Server IP Address or Name can't be blank.");
            initHostName();
            return false;
        }
        System.out.println("Trying to connect with " + capitalize(peer.getName())+ " at \n"
                + serverAddress + ":" + peer.getPort()+"\n");

//            // create socket over NON LAN
//            InetAddress inetAddress = InetAddress.getByName(serverAddress);
//            if (!inetAddress.isReachable(5000))// 5 sec
//            {
//                System.out
//                        .println("Error! Unable establish trustline, " + peer.getName()+ " is not online.");
//                return false;
//            }

        initPortNo();
        return true;
    }

    public void initPortNo() {
        try {

            String portNo = peer.getPort();

            portNo = portNo.trim();
            if (portNo.length() == 0)// empty field
            {
                System.out.println("Server port No can't be blank.");
                initPortNo();
                return;
            }
//            System.out.println("Trying to connect with server...\nServer Port No:" + portNo);
            int port = Integer.parseInt(portNo);

            socket = new Socket(InetAddress.getByName(serverAddress), port);
            //timeout if no connection
            socket.setSoTimeout(10000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

        } catch (IOException e) {
            System.out.println("IO Exception:\n" + e);
            initPortNo();
        }
    }

    public void sendTrustline() {
        String name = user.getName();
        
        // sending opcode first then sending current users name to the server
        out.println(Opcode.CLIENT_CONNECTING);
        out.println(name);
        out.flush();
    }

    public void sendPayment(String amount){
        // sending opcode first then sending current users name to the server
        out.println(Opcode.CLIENT_PAYMENT);
        out.println(user.getName());
        out.println(amount);
        out.flush();
    }

    public void runClient(String command) {
        String [] arrOfStr = command.split("\\|");
        if (arrOfStr.length > 0) {
            int opcode = Integer.parseInt(arrOfStr[0]);

            try {
                switch (opcode) {
                    case Opcode.CLIENT_CONNECTING:
                        // connect to peers server to establish trustline
                        sendTrustline();
                        // wait 60 seconds for a response then resend
                        socket.setSoTimeout(60000);

                        // check if peer accepted or declined Trustline
                        try {
                            int result = Integer.parseInt(in.readLine());
//                            System.out.println("Response from peer: " + result);
                            if (result== Opcode.CLIENT_ACCPETED){
                                ledger.addToLines(trustline);
                                System.out.println("Trustline with " + peer.getName() + " started!");
                            }else if (result == Opcode.CLIENT_REJECT){
                                System.out.println("Trustline with " + peer.getName() + " rejected.");
                            }
                        } catch (SocketTimeoutException ste) {
                            System.out.println("Peer did not respond for 60 seconds, Trustline not established.");
                        }
                        break;
                    case Opcode.CLIENT_PAYMENT:
                        // pay someone on a trustline
                        sendPayment(arrOfStr[1]);
                        // wait 10 seconds for a response then resend
                        socket.setSoTimeout(10000);

                        // wait for ACK from server
                        try {
                            int result = Integer.parseInt(in.readLine());
                            System.out.println("Response from peer: " + result);
                            if (result== Opcode.PAYMENT_RECEIVED){
                                user.setBalance(user.getBalance() + transaction.getAmount());
                                peer.setBalance(peer.getBalance() - transaction.getAmount());
                                ledger.updateTotal(-1 * transaction.getAmount());
                                ledger.addToTransactions(transaction);

                                // inform current user their payment has sent and been received by peer
//                                System.out.println("Sent");
                                System.out.println("Payment of " + arrOfStr[1] + " to " + peer.getName() + " sent!");
                            }
                        } catch (SocketTimeoutException ste) {
                            System.out.println("Peer did not respond for 60 seconds, payment not sent.\nAttempting to resend...");
                            runClient(command);
                        }

                        break;
                    case Opcode.CLIENT_SETTLE:
                        try {
                            // pay the peer on FakeChain amount of threshold
                            settleBalance("100",transaction.getAmount());
                            // wait 5 seconds for a response then resend
                            socket.setSoTimeout(5000);

                            // wait for ACK from server
                            int result = Integer.parseInt(in.readLine());
                            System.out.println("Response from peer: " + result);
                            if (result== Opcode.SETTLE_RECEIVED){
                                // remove old transactions
                                ledger.settle(trustline);

//                                runClient(Opcode.CLIENT_PAYMENT+"|"+transaction.getAmount());

//                                double newbalance = Double.parseDouble(arrOfStr[1]);
//                                // update current users balance to subtract remainder of transaction
////                                user.setBalance(user.getBalance() - newbalance);
//
//                                // update current total
//                                ledger.updateTotal(-1 * newbalance);
//                                //reset receiver balance to remainder of settlement {100-remainder}
//                                peer.setBalance(newbalance);
//
//                                // add remainder transaction if any
//                                ledger.addToTransactions(transaction);
//                                // inform current user their payment has sent and been received by peer
////                                System.out.println("Sent");
//                                System.out.println("Payment of " + arrOfStr[1] + " to " + peer.getName() + " sent!");
                            }
                        } catch (SocketTimeoutException | URISyntaxException ste) {
                            System.out.println("Peer did not respond for 60 seconds, payment not sent.\nAttempting to resend...");
                            runClient(command);
                        }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null)
                        in.close();
                    if (out != null)
                        out.close();
                    if (socket != null)
                        socket.close();
                } catch (IOException e){
                }
            }
        }else{
            System.out.println("Invalid command length");
        }
    }

    public void settleBalance(String amount, int transactionAmount) throws IOException, URISyntaxException {
        Trustline t = trustline;
        User receiver = t.getReceiver();
        User sender = t.getSender();
        String senderPKey = sender.getPubkey();
        String receiverPKey = receiver.getPubkey();
        String senderPrKey = receiver.getPrivkey();

        payUserAPI(senderPKey, receiverPKey, senderPrKey, amount);

        // sending opcode first then sending current users name to the server
        out.println(Opcode.CLIENT_SETTLE);
        out.println(user.getName());
        out.println(transactionAmount);
        out.flush();
    }

    //User balance in API does not update!
    //inserts a user into the blockchain
    public void payUserAPI(String senderPKey, String receiverPKey, String senderPrKey, String amount) throws IOException, URISyntaxException {
        String urlstring = "ec2-34-222-59-29.us-west-2.compute.amazonaws.com";
        URIBuilder builder = new URIBuilder()
                .setScheme("http")
                .setPort(5000)
                .setHost(urlstring);

        /*
        /pay_user
                        *URL params*
         **candidate**: access key given at top of prompt
         **sender**: public_key of sending node
         **receiver**: public_key of receiving node
         **private_key**: private_key of sender to authorize payment
         **amount**: amount to send
         */

        URI uri = builder.build();
        urlstring = uri.toString()
                + "/pay_user?"
                + "candidate=" + "rizzo"
                + "&sender=" + senderPKey
                + "&receiver=" + receiverPKey
                + "&private_key=" + senderPrKey
                + "&amount=" + amount;

        //send request
        URL url = new URL(urlstring);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("GET");

        int status = con.getResponseCode();
        if (status > 299) {
            System.out.println("Failed to publish to blockchain!");
            con.disconnect();
        }
    }
}

//
//
//    public void runClient() {
//        try {
//            sendChatName();
////            System.out.println(serverSocket);
////            System.out.println(serverSocket.getInetAddress().getHostName() + ":" + serverSocket.getLocalPort());
//
//            while (true) {
//                Socket socket = serverSocket.accept();
//                new ClientThread(socket,ledger);
//            }
//           /* while (true) {
//                int opcode = Integer.parseInt(in.readLine());
//                switch (opcode) {
//                    case Opcode.CLIENT_CONNECTING:
//                        // this client is connecting
//                        boolean result = Boolean.valueOf(in.readLine());
//                        if (result) {
//                            System.out
//                                    .println(user.getName() + " is already present. Try different one.");
//                            runClient();
//                        }
//                        break;
//
//                    case Opcode.CLIENT_CONNECTED:
//                        // a new client is connected
//                        Integer totalClient = Integer.valueOf(in.readLine());
//                        System.out.println("Total Client:" + totalClient);
//
//                        for (int i = 0; i < totalClient; i++) {
//                            String client = in.readLine();
//                            System.out.println((i + 1) + ":" + client);
//                        }
//
//
//                        break;
//
//                }
//            }*/
//        } catch (IOException e) {
//            System.out.println("Client is closed...");
//        }
//    }