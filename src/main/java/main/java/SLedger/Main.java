package main.java.SLedger;

import main.java.SLedger.Ledger.Ledger;
import main.java.SLedger.Server.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Main {

    private static volatile Ledger ledger;
    private static volatile Server server;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        ledger = new Ledger();

        final String port = "" + pickPort();
        Runnable r = () -> createServer(port, ledger);
        new Thread(r).start();

        System.out.println("\n=============================================================\n" +
                "\t\t\tWelcome to SLedger\nA decentralized platform to facilitate micropayments across FakeChain!\n" +
                "============================================================="
        );
        System.out.println("Type 'help' to see all commands\n");
        ledger.assignCurrentUser(args, port);

        try {
            boolean running = true;
            while (running) {
                String line = scanner.nextLine().toLowerCase().trim();
                switch (line) {
                    case "openline":
                    case "opentrustline":
                    case "open trustline":
                    case "open_trustline":
                        System.out.println("Name the recipient: [Bob]");
                        String recipient = scanner.next();
                        scanner.nextLine();

                        CountDownLatch latch = new CountDownLatch(1);
                        ledger.createTrustline(recipient, latch);
                        latch.await();
                        break;
                    case "pay":
                        System.out.println("Who is the recipient and what amount? [Bob]<space>[10]");
                        String payto = scanner.next();
                        while (!scanner.hasNextInt()) {
                            System.out.println("Please enter a valid number a number!");
                            scanner.next();
                        }
                        int amount = scanner.nextInt();
                        scanner.nextLine();

                        latch = new CountDownLatch(1);
                        ledger.createTransaction(payto, amount, latch);
                        latch.await();

                        break;
                    case "balance":
                        ledger.balance();
                        break;
                    case "help":
                        helpMenu();
                        break;
                    case "exit":
                        latch = new CountDownLatch(ledger.getTrustlines().size());
                        ledger.settleAllLines(latch);
                        latch.await(5, TimeUnit.SECONDS);
                        System.out.println("Thanks for using SLedger! Exiting... ");
                        running = false;
                        break;
                    default:
//                        System.out.println("\nPlease enter a command. Type 'help' to see available options: ");
                }
            }
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int pickPort() {
        int port = -1;
        try {
            ServerSocket socket = new ServerSocket(0);
            // here's your free port
            port = socket.getLocalPort();
            socket.close();
        } catch (IOException ioe) {
        }
        return port;
    }

    public static void helpMenu() {
        System.out.println(
                "\nopen_trustline \n\t\t[name] \n\t\t[ip] \n\t\t[pubkey]  \n\tConnect to another user on FakeChain" +
                        "\n\npay [recipient] [integer amount]|[float amount] \n\tSend funds to a peer on an established Trustline until a balance of 100 is reached. Must \n\thave an established Trustline to send funds. Whether you are paying or receiving money if\n\tTrustline balance exceeds 100 it will publish to FakeChain; any remaining balance will rollover\n\t back onto the Trustline balance." +
                        "\n\nbalance \n\tView your total and current Trustline balances" +
                        "\n\nexit \n\tSettle open Trustlines over FakeChain and closes SLedger"
        );
    }

    private static void createServer(String port, Ledger ledger) {
        server = new Server(port, ledger);
    }

}