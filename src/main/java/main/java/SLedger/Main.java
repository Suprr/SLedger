package main.java.SLedger;

import main.java.SLedger.Ledger.Ledger;
import main.java.SLedger.Server.Server;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class Main {

    private static Ledger ledger;
    private static Server server;

    public static void main(String[] args) throws Exception {
        final String port = "" + pickport();
        Runnable r = () -> {
            createServer(port, ledger);
        };
//        https://stackoverflow.com/questions/12551514/create-threads-in-java-to-run-in-background
        new Thread(r).start();
        ledger = new Ledger();

        //uncommment to test without input arguments as user "test"
//        ledger.assignCurrentUser(new String[]{"test",
//                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC3gonoq5MgzGUGZ07+XO2ln2yU" +
//                "8xaYu6CNdC8L14f4GJy8zXpTMtk/kqdLxQSnXKYI8nzrlon4rVQz1piuMwiZS1fI" +
//                "z80JpVSDoCThzZ+UQbBy/pj+jXSYC1I1jRz1hFYIiXGSCYwahEqk6rzUKR+L8v6Z" +
//                "SQ5y5Vj3eIGjP9D+AQIDAQAB"
//                ,"99",
//                "MIICXQIBAAKBgQC3gonoq5MgzGUGZ07+XO2ln2yU8xaYu6CNdC8L14f4GJy8zXpT" +
//                "Mtk/kqdLxQSnXKYI8nzrlon4rVQz1piuMwiZS1fIz80JpVSDoCThzZ+UQbBy/pj+" +
//                "jXSYC1I1jRz1hFYIiXGSCYwahEqk6rzUKR+L8v6ZSQ5y5Vj3eIGjP9D+AQIDAQAB" +
//                "AoGBALETRo38WalRcb5/G4t5Elw5/OWxt8FDc8ZrMSaFII/29+97eykjLN0aX1JO" +
//                "15HDZffGPWJ7TcFnR5QJ5CRb3FOlJCBWZu+9b+1WWuCTvHeyjt3yLh6Df/jscO1O" +
//                "T9xd1vVGXMdZ9S9ydXjlbEUPY/mtoljvPS6AqY+0QrXgZ5EBAkEA3gTE+ee5iHoA" +
//                "vnJt4zI9sJAd+27IUJz8fR8gL9+srXlotsKzeE7sfHQT8luuUGfw4/3Vhhf3MV/n" +
//                "udnxA12P0QJBANOY53KnlRVUVStANe2e80lKNDXLbhlzkoz9YLxH5FZkl7SGNgpe" +
//                "gJ44/sjPJl/Kiudxy16kN9MUGPaLxYqNxzECQHG888Qq6Ct4hQULzivEQ0I+sn1q" +
//                "hYh2xAq9dVnRNr8wIWrvV83ccN5ZARb5zNU4SnoiQc8OW/6ZaTcW5ZeZyOECQQCU" +
//                "j40ofap5UD1/4VQ7olbThSrE/jAt5GvnW1pYtu0FDxlIINa+Tv1kmUWhPXeG19DQ" +
//                "kJ+lsgyTwU+JgjbOgZ5xAkApH8jt7USmCr7kYIbJjEojhShOhacSwaPPRHs7nASH" +
//                "qiy1Etfx6abtTneSYzJnFO5tzEDSNJ7fUyuna4WStCFP"
//                ,"192.168.1.1"
//                }, port);
        System.out.println("Welcome to SLedger - A decentralized platform to facilitate micropayments across FakeChain!");
        ledger.assignCurrentUser(args, port);
        System.out.println("Type 'help' to see all commands");

        Scanner scanner = new Scanner(System.in);
        try {
            while (scanner.hasNextLine()){
                String line = scanner.nextLine().toLowerCase();
                switch (line){
                    case "openline":
                    case "opentrustline":
                    case "open trustline":
                    case "open_trustline":
                        System.out.println("Name the recipient: [Bob]");
                        String recipient = scanner.next();
                        System.out.println("IP address of recipient: [192.168.1.1]");
                        String ip = scanner.next();
                        System.out.println("Port: [1337]");
                        String peerPort = scanner.next();
                        System.out.println("Paste their RSA public key: [anything]");
                        String pubkey = scanner.next();
                        scanner.nextLine();

                        ledger.createTrustline(recipient,ip,peerPort);
                        break;
                    case "pay":
                        System.out.println("Who is the recipient and what amount? [Bob]<space>[10]");
                        String payto = scanner.next();
                        double amount = Double.parseDouble(scanner.next());
                        scanner.nextLine();
                        ledger.createTransaction(payto,amount);
                        break;
                    case "balance":
                        ledger.balance();
                        break;
                    case "help":
                        helpMenu();
                        break;
                    case "exit":
                        System.out.println("Settling all trustlines");
                        System.exit(0);
                }
                System.out.println("\nPlease enter a command. Type 'help' to see available options: ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int pickport() {
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

    public static void helpMenu(){
        System.out.println(
                "\nopen_trustline \n\t[name] \n\t[ip] \n\t[pubkey]  \n\tConnect to another user on FakeChain" +
                        "\n\npay [recipient] [integer amount]|[float amount] \n\tSend funds to a peer on an established Trustline until a balance of 100 is reached. Must \n\thave an established Trustline to send funds. Whether you are paying or receiving money if\n\tTrustline balance exceeds 100 it will publish to FakeChain; any remaining balance will rollover back onto the Trustline balance." +
                        "\n\nbalance \n\tView your total and current Trustline balances"+
                        "\n\nexit \n\tSettle open Trustlines over FakeChain and closes SLedger"
        );
    }

    private static void createServer(String port, Ledger ledger) {
        server = new Server(port, ledger);
    }

    public static Ledger getLedger() {
        return ledger;
    }

    public static Server getServer() {
        return server;
    }

}