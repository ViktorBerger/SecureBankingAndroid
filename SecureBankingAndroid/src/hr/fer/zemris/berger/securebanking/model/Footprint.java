package hr.fer.zemris.berger.securebanking.model;


public class Footprint {

    private String deviceID;
    private String hash;
    private String signature;

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

}
