package com.idan.md5DecoderMinion.beans;

public class DecodedHash {
    private String decodedPassword;
    private String decodedHash;

    public DecodedHash(String decodedPassword, String decodedHash) {
        this.decodedPassword = decodedPassword;
        this.decodedHash = decodedHash;
    }

    public String getDecodedPassword() {
        return decodedPassword;
    }

    public void setDecodedPassword(String decodedPassword) {
        this.decodedPassword = decodedPassword;
    }

    public String getDecodedHash() {
        return decodedHash;
    }

    public void setDecodedHash(String decodedHash) {
        this.decodedHash = decodedHash;
    }
}
