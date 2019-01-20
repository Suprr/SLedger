package main.java.SLedger.Ledger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Used for settlement or when trustline balance exceeds 100
public class Transaction {
    //time when transaction made
    public String date;
    private main.java.SLedger.Ledger.User sender;
    private main.java.SLedger.Ledger.User receiver;
    private int amount;
    private Trustline trustline;


    public Transaction(main.java.SLedger.Ledger.User sender, main.java.SLedger.Ledger.User receiver, main.java.SLedger.Ledger.Trustline trustline, int amount) {
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

    public main.java.SLedger.Ledger.User getSender() {
        return sender;
    }

    public main.java.SLedger.Ledger.User getReceiver() {
        return receiver;
    }

    public void setReceiver(main.java.SLedger.Ledger.User receiver) {
        this.receiver = receiver;
    }

    public Trustline getTrustline() {
        return trustline;
    }

    public int getAmount() {
        return amount;
    }
}
