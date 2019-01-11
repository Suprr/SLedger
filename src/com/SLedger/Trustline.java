package com.SLedger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Logger;

//Object used to represent a bilateral transfer between two accounts/users before updating FakeChain
public class Trustline {
    private final static Logger LOGGER = Logger.getLogger(Trustline.class.getName());

    private User sender;
    private User receiver;
    //balance
    private double balance;

    public Trustline(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public void updateBal(User usrobj, double amount) {
        if (amount != 0) {
            usrobj.update(amount);
        }
    }

}
