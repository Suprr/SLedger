package main.java.SLedger.Server;

import main.java.SLedger.Ledger.Ledger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private String port;
    private volatile static Ledger ledger;
    // TCP Components
    private ServerSocket serverSocket;
    // Main Constructor

    public Server(String port, Ledger ledger) {
        this.port = port;
        this.ledger = ledger;
        startServer();// start the server
    }

    private void startServer() {
        try {
            // in constructor we are passing port no, back log and bind address which will be localhost
            // port no - the specified port, or 0 to use any free port.
            // backlog - the maximum length of the queue. use default if it is equal or less than 0
            // bindAddr - the local InetAddress the server will bind to

            int portNo = Integer.parseInt(port);
            serverSocket = new ServerSocket(portNo, 0, InetAddress.getLocalHost());
//            System.out.println(serverSocket);
//            System.out.println(serverSocket.getInetAddress().getHostName() + ":" + serverSocket.getLocalPort());

            while (true) {
                Socket socket = serverSocket.accept();
//                System.out.println("Incoming connection: " + socket.getInetAddress().getHostAddress() + ":" +socket.getPort());
                new ClientThread(socket,ledger);
            }
        } catch (IOException e) {
            System.out.println("IO Exception:" + e);
        } catch (NumberFormatException e) {
            System.out.println("Number Format Exception:" + e);
        }
    }

    public static Ledger getLedger(){
        return ledger;
    }
}
