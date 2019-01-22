package main.java.SLedger.Server;

import main.java.SLedger.Ledger.Ledger;
import main.java.SLedger.Ledger.Transaction;
import main.java.SLedger.Ledger.Trustline;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static main.java.SLedger.Ledger.Ledger.capitalize;

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

    private int opcode;
    private volatile Ledger ledger;
    public volatile String choice;

    public ClientThread(Socket socket, Ledger ledger) {
        try {
            this.socket = socket;
            this.ledger = ledger;

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            thread = new Thread(this);
            thread.start();

        } catch (IOException e) {
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
                            System.out.println(capitalize(finalName) + " wants to start a Trustline, accept? \n[Y/n]");
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
                            if (latch.await(5, TimeUnit.SECONDS)) {
                                break;
                            }
                        }

//                        latch.await();
                        if (choice.equalsIgnoreCase("y")) {
                            // does the other person already have a Trustline with you?
                            boolean retval = ledger.receiveTrustline(name);
                            // if not, send accept
                            if (retval) sendAccept();
                                // if you do, reject them - may be accessing from multiple instances!
                            else sendReject();
                        } else {
                            sendReject();
                        }
                        break;
                    }
                    case Opcode.CLIENT_PAYMENT:
                        name = in.readLine();
                        int amount = Integer.parseInt(in.readLine());// getting opcode first from client
                        ledger.receieveTransaction(name, amount);

                        sendPaymentACK();
                        System.out.println("Received payment of " + amount + " from " + capitalize(name) + ".");
                        break;
                    case Opcode.CLIENT_SETTLE:
                        //user that caused settlement handles payout send ACK only thing left to do is clear balances and update total
                        name = in.readLine();

                        settleBalance(name);

                        // sending opcode first then sending current users name to the server
                        out.println(Opcode.SETTLE_RECEIVED);
                        out.flush();
                        break;
                    case Opcode.CLIENT_TERMINATE:
                        // client ended session
                        name = in.readLine();

                        terminateTrustline(name);
                        Thread.sleep(3000);
                        System.out.println("Trustine with " + capitalize(name) + " terminated.\n");
                        break;
                    default:
                        System.out.println("You received an invalid request.");
                }
                // close all connections
                out.close();
                in.close();
                socket.close();
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void terminateTrustline(String name) {
        Trustline line = null;
        for (Transaction t : ledger.getTransactions()) {
            String receiver = t.getReceiver().getName();
            if (receiver.equalsIgnoreCase(name)) {
                ledger.removeTransaction(t);
                line = t.getTrustline();
            }
        }
        if (line != null)
            ledger.removeTrustline(line);
    }

    private void settleBalance(String name) {
        Queue<Transaction> transactions = ledger.getTransactions();
        int payment = 0;
        for (Transaction t : transactions) {
            String receiver = t.getReceiver().getName();
            if (receiver.equals(name)) {
                // update the total so it doesnt reflect the payments that were settled
                ledger.updateTotal(-1 * t.getAmount());
                payment = 100 + t.getSender().getBalance();
                // update the senders balance
                t.getSender().setBalance(t.getSender().getBalance() + t.getAmount());
                t.getReceiver().setBalance(t.getReceiver().getBalance() - t.getAmount());
                t.getTrustline().setBalance(0);

                ledger.removeTransaction(t);
            }
        }
        if (payment != 0)System.out.println("Received payment of " + payment + " which caused Trustline with " + capitalize(name) + " to be settled!\nAnother payment may be coming...\n");
        else System.out.println("Trustline with " + capitalize(name) + " has been settled!\nAnother payment may be coming...\n");
    }

    private void sendPaymentACK() {
        out.println(Opcode.PAYMENT_RECEIVED);
        out.flush();
    }

    private void sendAccept() {
        out.println(Opcode.CLIENT_ACCPETED);
        out.flush();
    }

    private void sendReject() {
        out.println(Opcode.CLIENT_REJECT);
        out.flush();
    }
}