package main.java.SLedger.Server;

import main.java.SLedger.Ledger.Ledger;
import main.java.SLedger.Ledger.Transaction;
import main.java.SLedger.Ledger.Trustline;
import main.java.SLedger.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.sql.Time;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientThread implements Runnable {
    // TCP Components
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String name;

    // separate thread
    private Thread thread;

    // boolean variable to check that client is running or not
    private volatile boolean isRunning = true;

    // opcode
    private int opcode;
    private volatile Ledger ledger;
    public volatile String choice;
//    private HashMap<String, ClientThread> clientInfo = new HashMap<>();

    public ClientThread(Socket socket, Ledger ledger) {
        try {
            this.socket = socket;
            this.ledger = ledger;
//            this.clientInfo = Server.getClientInfo();

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            thread = new Thread(this);
            thread.start();

        } catch (IOException e) {
//            System.out.println(e);
        }
    }

    public void run() {
        try {
            String readline;
            if ((readline = in.readLine()) != null) {
                opcode = Integer.parseInt(readline);// getting opcode first from client
                switch (opcode) {
                    case Opcode.CLIENT_CONNECTING: {
                        String name = in.readLine();
                        String finalName = name;

                        CountDownLatch latch = new CountDownLatch(1);

                        Runnable r = () -> {
                            System.out.println(Ledger.capitalize(finalName) + " wants to start a trustline, accept? \n[Y/n]");
                            Scanner scanner = new Scanner(System.in);
                            boolean flag = true;
                            while (flag) {
                                choice = scanner.nextLine();
                                scanner.nextLine();
                                if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("n")) {
                                    flag = false;
                                    break;
                                } else {
                                    System.out.println("Wrong command, try again");
                                }
                            }
                            latch.countDown();
                        };

                        Thread input = new Thread(r);
                        //accessible from both threads
                        input.start();

                        //wait for user to input Y/n before writing response
//                        input.sleep(10000);
                        while (true) {
                            if (latch.await(2, TimeUnit.SECONDS)) {
                                break;
                            }
                        }

                        if (choice.equalsIgnoreCase("y")) {
                            sendAccept(name);
                            ledger.receiveTrustline(name);
                        } else {
                            sendReject(name);
                        }
                        // close all connections
                        out.close();
                        in.close();
                        socket.close();
                        break;
                    }
                    case Opcode.CLIENT_PAYMENT:
                        name = in.readLine();
                        int amount = Integer.parseInt(in.readLine());// getting opcode first from client
//                        System.out.println("WE GOT PAYMENT");
                        ledger.receieveTransaction(name, amount);

                        sendPaymentACK();
                        System.out.println("Received payment of " + amount + " from " + name + "\n");
                        // close all connections
                        out.close();
                        in.close();
                        socket.close();
                        break;
                    case Opcode.CLIENT_SETTLE:
                        //user that caused settlement handles payout send ACK only thing left to do is clear balances and update total
                        name = in.readLine();
                        settleBalance(name);
                        System.out.println("Received payment which caused Trustline with " + name + " to be settled!\nAnother payment may be coming...\n");

                        // sending opcode first then sending current users name to the server
                        out.println(Opcode.SETTLE_RECEIVED);
                        out.flush();
                    default:
                        System.out.println("Invalid request. " + readline);
                }
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void settleBalance(String name) {
        Queue<Transaction> transactions = ledger.getTransactions();
        for (Transaction t : transactions) {
            String receiver = t.getReceiver().getName();
            if (receiver.equals(name)) {
                // update the total so it doesnt reflect the payments that were settled
                ledger.updateTotal(-1*t.getAmount());
                // update the senders balance
                t.getSender().setBalance(t.getSender().getBalance() + t.getAmount());
                t.getReceiver().setBalance(t.getReceiver().getBalance() - t.getAmount());

                ledger.removeTransaction(t);
            }
        }
    }

    private void sendPaymentACK() {
        out.println(Opcode.PAYMENT_RECEIVED);
        out.flush();
    }

    private void sendAccept(String name) {
        out.println(Opcode.CLIENT_ACCPETED);
        out.flush();
    }

    private void sendReject(String name) {
        out.println(Opcode.CLIENT_REJECT);
        out.flush();
    }
}