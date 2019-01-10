package com.SLedger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Used for settlement or when trustline balance exceeds 100
public class Transaction {
    //time when transaction made
    public String date;
    private User sender;
    private User receiver;
    private double amount;
    private Trustline trustline;


    public Transaction(User sender, User receiver, Trustline trustline, double amount) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        this.date = dtf.format(now);
        this.sender = sender;
        this.receiver = receiver;
        this.trustline = trustline;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public Trustline getTrustline() {
        return trustline;
    }

    public void setTrustline(Trustline trustline) {
        this.trustline = trustline;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
