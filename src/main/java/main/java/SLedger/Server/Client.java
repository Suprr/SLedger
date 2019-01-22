package main.java.SLedger.Server;

import main.java.SLedger.Ledger.Ledger;
import main.java.SLedger.Ledger.Transaction;
import main.java.SLedger.Ledger.Trustline;
import main.java.SLedger.Ledger.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.concurrent.CountDownLatch;

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
    private volatile int retries = 0;
    CountDownLatch latch;

    public Client(Trustline line, String command) {
        this.user = line.getSender();
        this.peer = line.getReceiver();
        this.trustline = line;
        this.ledger = Server.getLedger();
        if (initHostName()) {
            retries = 0;
            runClient(command);// have fun
        } else {
            System.out.println(peer.getName() + " could not be reached.");
        }
    }

    public Client(Trustline line, Transaction transaction, String command) {
        this.user = line.getSender();
        this.peer = line.getReceiver();
        this.trustline = line;
        this.transaction = transaction;
        this.ledger = Server.getLedger();
        if (initHostName()) {
            retries = 0;
            runClient(command);// have fun
        } else {
            System.out.println(peer.getName() + " could not be reached.");
        }
    }

    public Client(Trustline t, String s, CountDownLatch latch) {
        this.user = t.getSender();
        this.peer = t.getReceiver();
        this.trustline = t;
        this.ledger = Server.getLedger();
        this.latch = latch;
        if (initHostName()) {
            retries = 0;
            runClient(s);// have fun
        } else {
            System.out.println(peer.getName() + " could not be reached.");
        }
    }


    public boolean initHostName() {
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
//        System.out.println("Trying to connect with " + capitalize(peer.getName())+ " at \n"
//                + serverAddress + ":" + peer.getPort()+"\n");

//            // create socket over NON LAN
//            InetAddress inetAddress = InetAddress.getByName(serverAddress);
//            if (!inetAddress.isReachable(5000))// 5 sec
//            {
//                System.out
//                        .println("Error! Unable establish trustline, " + peer.getName()+ " is not online.");
//                return false;
//            }

        if (initPortNo()) return true;
        else return false;
    }

    public boolean initPortNo() {
        try {

            String portNo = peer.getPort();
            int port = Integer.parseInt(portNo);

            socket = new Socket(InetAddress.getByName(serverAddress), port);

            //timeout in 8 seconds if no connection
            socket.setSoTimeout(8000);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            return true;
        } catch (IOException e) {
            if (retries < 5) {
                ++retries;
                System.out.println("Could not connect to peer, Trustline not established.\nAttempt #" + retries);
                initPortNo();
            } else {
                System.out.println("Exceeded retry limit...\nSomething may have went wrong on their end.\n");
                return false;
            }
        }
        return false;
    }

    public void runClient(String command) {
        String[] arrOfStr = command.split("\\|");
        if (arrOfStr.length > 0) {
            int opcode = Integer.parseInt(arrOfStr[0]);

            try {
                switch (opcode) {
                    case Opcode.CLIENT_CONNECTING:
                        // connect to peers server to establish trustline
                        sendTrustline();

                        // check if peer accepted or declined Trustline
                        try {
                            int result = Integer.parseInt(in.readLine());
//                            System.out.println("Response from peer: " + result);
                            if (result == Opcode.CLIENT_ACCPETED) {
                                ledger.addToLines(trustline);
                                System.out.println("Trustline with " + capitalize(peer.getName()) + " started!\n");
                            } else if (result == Opcode.CLIENT_REJECT) {
                                System.out.println("Trustline with " + capitalize(peer.getName()) + " rejected.\n");
                            }
                        } catch (SocketTimeoutException ste) {
                            if (retries < 5) {
                                ++retries;
                                System.out.println("Peer did not respond, Trustline not established.\nAttempt #" + retries);
                                runClient(command);
                            } else {
                                System.out.println("Exceeded Trustline establishment retry limit...\nSomething may have went wrong on their end.\n");
                            }
                        }
                        break;
                    case Opcode.CLIENT_PAYMENT:
                        // pay someone on specified trustline
                        if (arrOfStr.length > 1) {
                            sendPayment(arrOfStr[1]);

                            // wait 5 seconds for a response then resend
                            socket.setSoTimeout(5000);

                            // wait for ACK from server
                            try {
                                int result = Integer.parseInt(in.readLine());
//                            System.out.println("Response from peer: " + result);
                                if (result == Opcode.PAYMENT_RECEIVED) {
                                    user.setBalance(user.getBalance() + transaction.getAmount());
                                    peer.setBalance(peer.getBalance() - transaction.getAmount());
                                    ledger.updateTotal(-1 * transaction.getAmount());
                                    ledger.addToTransactions(transaction);
                                    trustline.setBalance(trustline.getBalance() - transaction.getAmount());

                                    // inform current user their payment has sent and been received by peer
                                    System.out.println("Payment of " + arrOfStr[1] + " to " + capitalize(peer.getName()) + " sent!");
                                }
                            } catch (SocketTimeoutException ste) {
                                if (retries < 5) {
                                    ++retries;
                                    System.out.println("Peer did not respond for 60 seconds, payment not sent.\nAttempt #" + retries);
                                    runClient(command);
                                } else {
                                    System.out.println("Exceeded payment retry limit...\nSomething may have went wrong, payment not sent.\n");
                                }
                            }
                        } else {
                            System.out.println("No payment amount.");
                        }

                        break;
                    case Opcode.CLIENT_SETTLE:
                        try {
                            // pay the peer on FakeChain amount of threshold
                            settleBalance("100", transaction.getAmount());
                            // wait 5 seconds for a response then resend
                            socket.setSoTimeout(5000);

                            // wait for ACK from server
                            int result = Integer.parseInt(in.readLine());
//                            System.out.println("Response from peer: " + result);
                            if (result == Opcode.SETTLE_RECEIVED) {
                                // remove old transactions
                                ledger.settleLine(trustline);
                            }
                        } catch (SocketTimeoutException | URISyntaxException ste) {
                            if (retries < 5) {
                                ++retries;
                                System.out.println("Peer did not respond for 60 seconds, settlement not sent.\nAttempt #" + retries);
                                runClient(command);
                            } else {
                                System.out.println("Exceeded settlement retry limit...\nSomething may have went wrong, settlement failed.\n");
                            }

                        }
                        break;
                    case Opcode.CLIENT_TERMINATE:
                        sendTerminate();
                        Thread.sleep(5000);
                        latch.countDown();
                        break;
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null)
                        in.close();
                    if (out != null)
                        out.close();
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {
                }
            }
        } else {
            System.out.println("Invalid command");
        }
    }

    public void settleBalance(String amount, int transactionAmount) throws IOException, URISyntaxException {
        Trustline t = trustline;
        User receiver = t.getReceiver();
        User sender = t.getSender();
        String senderPKey = sender.getPubkey();
        String receiverPKey = receiver.getPubkey();
        String senderPrKey = sender.getPrivkey();

        ledger.payUserAPI(senderPKey, receiverPKey, senderPrKey, amount);

        // sending opcode first then sending current users name to the server
        out.println(Opcode.CLIENT_SETTLE);
        out.println(user.getName());
        out.println(transactionAmount);
        out.flush();
    }

    private void sendTerminate() {
        out.println(Opcode.CLIENT_TERMINATE);
        out.println(user.getName());
        out.flush();
    }

    public void sendTrustline() {
        String name = user.getName();

        // sending opcode first then sending current users name to the server
        out.println(Opcode.CLIENT_CONNECTING);
        out.println(name);
        out.flush();
    }

    public void sendPayment(String amount) {
        // sending opcode first then sending current users name to the server
        out.println(Opcode.CLIENT_PAYMENT);
        out.println(user.getName());
        out.println(amount);
        out.flush();
    }

}
