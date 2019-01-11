package main.java.SLedger.Ledger;

import java.util.logging.Logger;

//Object used to represent a bilateral transfer between two accounts/users before updating FakeChain
public class Trustline {
    private final static Logger LOGGER = Logger.getLogger(Trustline.class.getName());

    private User sender;
    private User receiver;

    public Trustline(main.java.SLedger.Ledger.User sender, main.java.SLedger.Ledger.User receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(main.java.SLedger.Ledger.User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(main.java.SLedger.Ledger.User receiver) {
        this.receiver = receiver;
    }

    public void updateBal(main.java.SLedger.Ledger.User usrobj, double amount) {
        if (amount != 0) {
            usrobj.update(amount);
        }
    }

}
