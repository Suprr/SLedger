package main.java.SLedger.Server;

import main.java.SLedger.Ledger.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class Client {
    private String serverAddress;

    // TCP Components
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    User peer;
    User user;

    public Client(User user, User peer) {
        this.user = user;
        this.peer = peer;
        initHostName();
        runClient();// have fun
    }

    public void initHostName() {
        try {
            //replace host name with your computer name or IP address
//            String host = InetAddress.getLocalHost().getHostAddress();
            serverAddress = peer.getIp();
            if (serverAddress == null)
                System.exit(1);

            serverAddress = serverAddress.trim();
            if (serverAddress.length() == 0)// empty field
            {
                System.out.println("Server IP Address or Name can't be blank.");
                initHostName();
                return;
            }
            System.out.println("Trying to connect with server...\nServer IP Address:"
                    + serverAddress);

            // create socket
            InetAddress inetAddress = InetAddress.getByName(serverAddress);
            if (!inetAddress.isReachable(60000))// 60 sec
            {
                System.out
                        .println("Error! Unable to connect with server.\nServer IP Address may be wrong.");
                System.exit(1);
            }

            initPortNo();
        } catch (SocketException e) {
            System.out.println("Socket Exception:\n" + e);
            initHostName();
            return;
        } catch (IOException e) {
            initHostName();
            return;
        }
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
            System.out.println("Trying to connect with server...\nServer Port No:" + portNo);

            socket = new Socket(serverAddress, 0);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

        } catch (IOException e) {
            System.out.println("IO Exception:\n" + e);
            initPortNo();
            return;
        }
    }

    public void sendChatName() throws IOException {
        String name = user.getCandidate();
        
        // sending opcode first then sending chatName to the server
        out.println(Opcode.CLIENT_CONNECTING);
        out.println(user.getCandidate());
    }

    public void runClient() {
        try {
            sendChatName();
            while (true) {
                int opcode = Integer.parseInt(in.readLine());
                switch (opcode) {
                    case Opcode.CLIENT_CONNECTING:
                        // this client is connecting
                        boolean result = Boolean.valueOf(in.readLine());
                        if (result) {
                            System.out
                                    .println(user.getCandidate() + " is already present. Try different one.");
                            runClient();
                        }

                        break;

                    case Opcode.CLIENT_CONNECTED:
                        // a new client is connected
                        Integer totalClient = Integer.valueOf(in.readLine());
                        System.out.println("Total Client:" + totalClient);

                        for (int i = 0; i < totalClient; i++) {
                            String client = in.readLine();
                            System.out.println((i + 1) + ":" + client);
                        }

                        break;

                }
            }
        } catch (IOException e) {
            System.out.println("Client is closed...");
        }
    }

    // *********************************** Main Method ********************

//    public static void main(String args[]) {
//        new Client();
//    }

}