package hr.fer.zemris.berger.securebanking.model;

import java.util.Date;


public class Footprint {

    private String deviceID;
    private String hash;
    private String signature;
    private Date timestamp;

    public Footprint(String deviceID, String hash, String signature) {
        this.deviceID = deviceID;
        this.hash = hash;
        this.signature = signature;
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
