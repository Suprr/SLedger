package com.SLedger;

import java.util.Scanner;

public class Main {

    private static Ledger ledger;

    public static void main(String[] args) {
        ledger = new Ledger();
        Scanner scanner = new Scanner(System.in);
        try {
            while (scanner.hasNextLine()){
                String line = scanner.nextLine().toLowerCase();
                switch (line){
                    case "open_trustline":
                        System.out.println(line);
                        ledger.assignCurrentUser(new String[]{"1","2","3","4","5"});
                    case "pay":
                        System.out.println(line);
                        break;
                    case "balance":
                        System.out.print(line);
                        break;
                    case "exit":
                        System.out.println("Settling all trustlines");
                        System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (scanner != null)
                scanner.close();
                System.exit(0);
        }
    }
}
