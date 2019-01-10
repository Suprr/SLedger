package com.SLedger;

import java.util.Scanner;

public class Main {

    private static Ledger ledger;

    public static void main(String[] args) throws Exception {
        ledger = new Ledger();

        ledger.assignCurrentUser(new String[]{"1","2","3","4","5"});
        System.out.println("Welcome to SLedger - A decentralized platform to facilitate micropayments across FakeChain!");
        System.out.println("Type 'help' to see all commands");
        Scanner scanner = new Scanner(System.in);
        try {
            while (scanner.hasNextLine()){
                String line = scanner.nextLine().toLowerCase();
                switch (line){
                    case "openline":
                    case "open trustline":
                    case "open_trustline":
                        System.out.println("Who is the recipient? [Bob]");
                        String recipient = scanner.next();
                        System.out.println("IP address of recipient? [192.168.1.1]");
                        String ip = scanner.next();
                        System.out.println("Paste RSA public key such as:\n[-----BEGIN PUBLIC KEY-----\n" +
                                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCqGKukO1De7zhZj6+H0qtjTkVxwTCpvKe4eCZ0\n" +
                                "FPqri0cb2JZfXJ/DgYSF6vUpwmJG8wVQZKjeGcjDOL5UlsuusFncCzWBQ7RKNUSesmQRMSGkVb1/\n" +
                                "3j+skZ6UtW+5u09lHNsj6tQ51s1SPrCBkedbNf0Tp0GbMJDyR4e9T04ZZwIDAQAB\n" +
                                "-----END PUBLIC KEY-----]");
                        String pubkey = scanner.next();
                        scanner.nextLine();

                        System.out.println(recipient + " " + ip + " " + pubkey);
//                        ledger.createTrustline();
                        break;
                    case "pay":
                        System.out.println("Who is the recipient and what amount? [Bob]<space>[10]");
                        String payto = scanner.next();
                        double amount = scanner.nextDouble();
                        scanner.nextLine();
                        System.out.println(payto + amount);

                        ledger.createTransaction(payto,amount);
                        break;
                    case "balance":
//                        System.out.print(line);
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

    public static void helpMenu(){
        System.out.println(
                "\nopen_trustline \n\t[name] \n\t[ip] \n\t[pubkey]  \n\tConnect to another user on FakeChain" +
                        "\n\npay [recipient] [integer amount]|[float amount] \n\tSend funds to a peer on an established Trustline until a balance of 100 is reached. Must \n\thave an established Trustline to send funds. Whether you are paying or receiving money if\n\tTrustline balance exceeds 100 it will publish to FakeChain; any remaining balance will rollover back onto the Trustline balance." +
                        "\n\nbalance \n\tView your total and current Trustline balances"+
                        "\n\nexit \n\tSettle open Trustlines over FakeChain and closes SLedger"
        );
    }

}
