package hr.fer.zemris.berger.securebanking.model;

public class Transaction {

    private String senderAccount;
    private String recipientAccount;
    private String amount;

    public Transaction() {
    }

    public Transaction(String senderAccountNumber,
                       String recipientAccountNumber, String amount) {
        super();
        this.senderAccount = senderAccountNumber;
        this.recipientAccount = recipientAccountNumber;
        this.amount = amount;
    }

    public String getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
    }

    public String getRecipientAccount() {
        return recipientAccount;
    }

    public void setRecipientAccount(String recipientAccount) {
        this.recipientAccount = recipientAccount;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
