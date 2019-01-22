package main.java.SLedger.Ledger;

import java.util.logging.Logger;

//Object used to represent a bilateral transfer between two accounts/users before updating FakeChain
public class Trustline {
    private final static Logger LOGGER = Logger.getLogger(Trustline.class.getName());

    private User sender;
    private User receiver;
    private int balance;

    public Trustline(main.java.SLedger.Ledger.User sender, main.java.SLedger.Ledger.User receiver) {
        this.sender = sender;
        this.receiver = receiver;
        balance = 0;
    }

    public User getSender() {
        return sender;
    }
    public User getReceiver() {
        return receiver;
    }

    public synchronized int getBalance() {
        return balance;
    }

    public synchronized void setBalance(int balance) {
        this.balance = balance;
    }
}
