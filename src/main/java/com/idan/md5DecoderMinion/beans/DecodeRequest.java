package com.idan.md5DecoderMinion.beans;

public class DecodeRequest {
    private int startNumber;
    private int endNumber;
    private String hashToDecode;

    public int getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(int startNumber) {
        this.startNumber = startNumber;
    }

    public int getEndNumber() {
        return endNumber;
    }

    public void setEndNumber(int endNumber) {
        this.endNumber = endNumber;
    }

    public String getHashToDecode() {
        return hashToDecode;
    }

    public void setHashToDecode(String hashToDecode) {
        this.hashToDecode = hashToDecode;
    }

    @Override
    public String toString() {
        return "DecodeRequest{" +
                "startNumber=" + startNumber +
                ", endNumber=" + endNumber +
                ", hashToDecode='" + hashToDecode + '\'' +
                '}';
    }
}
