package com.SLedger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Server {

    // Connection state info
    private static LinkedHashMap<String, ClientThread> clientInfo = new LinkedHashMap<String, ClientThread>();
    int port;
    // TCP Components
    private ServerSocket serverSocket;

    // Main Constructor
    public Server(int port) {
        this.port = port;
        startServer();// start the server
    }

    public void startServer() {

        try {
            // in constractor we are passing port no, back log and bind address whick will be local
            // host
            // port no - the specified port, or 0 to use any free port.
            // backlog - the maximum length of the queue. use default if it is equal or less than 0
            // bindAddr - the local InetAddress the server will bind to

            int portNo = Integer.valueOf(port);
            serverSocket = new ServerSocket(portNo, 0, InetAddress.getLocalHost());
            System.out.println(serverSocket);

            System.out.println(serverSocket.getInetAddress().getHostName() + ":"
                    + serverSocket.getLocalPort());

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientThread(socket);
            }
        } catch (IOException e) {
            System.out.println("IO Exception:" + e);
            System.exit(1);
        } catch (NumberFormatException e) {
            System.out.println("Number Format Exception:" + e);
            System.exit(1);
        }
    }

    public static HashMap<String, ClientThread> getClientInfo() {
        return clientInfo;
    }
}
