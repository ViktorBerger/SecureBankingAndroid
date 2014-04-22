package hr.fer.zemris.berger.securebanking;

public class Transaction {

	private String senderAccountNumber;
	private String recipientAccountNumber;
	private String amount;
	
	public Transaction(){
	}

	public Transaction(String senderAccountNumber,
			String recipientAccountNumber, String amount) {
		super();
		this.senderAccountNumber = senderAccountNumber;
		this.recipientAccountNumber = recipientAccountNumber;
		this.amount = amount;
	}

	public String getSenderAccountNumber() {
		return senderAccountNumber;
	}

	public void setSenderAccountNumber(String senderAccountNumber) {
		this.senderAccountNumber = senderAccountNumber;
	}

	public String getRecipientAccountNumber() {
		return recipientAccountNumber;
	}

	public void setRecipientAccountNumber(String recipientAccountNumber) {
		this.recipientAccountNumber = recipientAccountNumber;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}
	
	

	
	
	
	
}
