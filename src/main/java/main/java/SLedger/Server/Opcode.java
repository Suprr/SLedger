package main.java.SLedger.Server;

public interface Opcode {
    int CLIENT_CONNECTING = 1;
    int CLIENT_CONNECTED = 2;
    int CLIENT_ACCPETED = 3;
    int CLIENT_DENIED = 4;
    int CLIENT_PAYMENT = 5;
    int PAYMENT_RECEIVED = 6;
    int CLIENT_SETTLE = 7;
    int SETTLE_RECEIVED = 8;
}