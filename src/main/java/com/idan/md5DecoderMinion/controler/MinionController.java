package com.idan.md5DecoderMinion.controler;

import com.idan.md5DecoderMinion.beans.DecodeRequest;
import org.springframework.stereotype.Controller;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

@Controller
public class MinionController implements Runnable {

    private Set<String> hashesToDecode;
    private int startOfSearch;
    private int endOfSearch;
    private final Object waitForHashObj;
    private final Object waitForFinishCurrentJobObj;

    public MinionController() {
        this.hashesToDecode = new HashSet<>();
        this.waitForHashObj = new Object();
        this.waitForFinishCurrentJobObj = new Object();
    }

    private static String returnMD5Hash(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str.getBytes());
        byte[] digest = md.digest();
        String hash = DatatypeConverter.printHexBinary(digest).toUpperCase();
//        System.out.println(hash);
        return hash;
    }

    private boolean isCorrectPassword(String passwordAttempt, String hashToDecode) throws NoSuchAlgorithmException {
//        System.out.println(hashToDecode);
        return hashToDecode.equals(returnMD5Hash(passwordAttempt));
    }

    public void addRequest(DecodeRequest request) throws InterruptedException {
        validateHash(request.getHashToDecode());
        this.hashesToDecode.add(request.getHashToDecode().toUpperCase());
        System.out.println("request added");
        synchronized (this.waitForHashObj) {
            this.waitForHashObj.notify();
        }
        if (this.startOfSearch != request.getStartNumber() || this.endOfSearch != request.getEndNumber()) {
            synchronized (this.waitForFinishCurrentJobObj) {
                System.out.println("waiting for end of current decoding");
                this.waitForFinishCurrentJobObj.wait();
            }
            System.out.println("changing range");
            updateDecodingRange(request);
        }
    }

    private void updateDecodingRange(DecodeRequest request) {
        this.startOfSearch = request.getStartNumber();
        this.endOfSearch = request.getEndNumber();
    }

    private void decodingHash() throws NoSuchAlgorithmException {
        for (int i = this.startOfSearch; i <= this.endOfSearch; i++) {
            String passwordAttempt = String.format("05%08d", i);
            String[] hashes = this.hashesToDecode.toArray(new String[0]);
            for (String hashToDecode : hashes) {
                if (isCorrectPassword(passwordAttempt, hashToDecode)) {
                    System.out.println("password for hash " + hashToDecode + " is " + passwordAttempt);
                    this.hashesToDecode.remove(hashToDecode);
                }
            }
        }
    }

    //todo- implement
    private void validateHash(String HashToDecode) {
        System.out.println("validating hash: " + HashToDecode);
    }

    @Override
    public void run() {
        while (true) {
            if (this.hashesToDecode.isEmpty()) {
//                try {
                System.out.println("no hash to decode");
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                try {
                    synchronized (waitForHashObj) {
                        this.waitForHashObj.wait();
                        System.out.println("resuming");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                decodingHash();
                synchronized (this.waitForFinishCurrentJobObj) {
                    this.waitForFinishCurrentJobObj.notify();
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }
}
