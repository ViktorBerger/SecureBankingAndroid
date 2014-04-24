package hr.fer.zemris.berger.securebanking.model;

import java.util.Date;

/**
 * Contains footprint application data.
 * 
 * @author Viktor Berger
 * @version 1.0
 */
public class Footprint {

	private String deviceID;
	private String hash;
	private String signature;
	private Date timestamp;

	/**
	 * Constructor creates an instance of this class based on deviceID, hash
	 * code and signature of the application.
	 * 
	 * @param deviceID unique device identifier
	 * @param hash application hash code
	 * @param signature application signature
	 * @param timestamp footprint timestamp
	 */
	public Footprint(String deviceID, String hash, String signature, Date timestamp) {
		this.deviceID = deviceID;
		this.hash = hash;
		this.signature = signature;
		this.timestamp = timestamp;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

}
