package main.java.SLedger.Server;

import main.java.SLedger.Ledger.Ledger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.HashMap;

public class ClientThread implements Runnable {
    // TCP Components
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String name;

    // seperate thread
    private Thread thread;

    // boolean variable to check that client is running or not
    private volatile boolean isRunning = true;

    // opcode
    private int opcode;
    private Ledger ledger;
    private HashMap<String, ClientThread> clientInfo = new HashMap<String, ClientThread>();

    public ClientThread(Socket socket, Ledger ledger) {
        try {
            this.socket = socket;
            this.ledger = ledger;
            this.clientInfo = Server.getClientInfo();

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
            while (isRunning) {
                if (!in.ready())
                    continue;

                opcode = Integer.parseInt(in.readLine());// getting opcode first from client
                switch (opcode) {
                    case Opcode.CLIENT_CONNECTING: {
                        name = in.readLine();

                        boolean result = clientInfo.containsKey(name);
                        out.println(Opcode.CLIENT_CONNECTING);
                        out.println(result);
                        if (result)// wait for another chat name if already present
                            continue;

                        // put new entry in clientInfo hashmap
                        clientInfo.put(name, this);
                        ledger.receiveTrustline(name);
                        int i = 0;
                        for (String key : clientInfo.keySet()) {
                            if (key.equals(name)) {
                                System.out.println(name + " added at " + (i + 1) + " position");
                            }
                            i++;
                        }

                        break;
                    }
                    case Opcode.CLIENT_PAYMENT:
                        name = in.readLine();
                        double amount = Double.parseDouble(in.readLine());// getting opcode first from client


                }
            }

            // close all connections
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
//            System.out.println(e);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}