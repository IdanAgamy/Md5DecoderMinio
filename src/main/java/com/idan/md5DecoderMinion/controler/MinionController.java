package com.idan.md5DecoderMinion.controler;

//import com.idan.md5DecoderMinion.beans.DecodeRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

@Controller
public class MinionController implements Runnable {

    private final Set<String> hashesToDecode;
    private int startOfSearchRange;
    private int endOfSearchRange;
    private final Object waitForHashObj;

    @Value("${server.port}")
    private int port;
    @Value("${minion.hostname}")
    private String hostname;
    @Value("${master.port}")
    private int masterPort;
    @Value("${master.hostname}")
    private String masterHostname;

    public MinionController() {
        this.hashesToDecode = new HashSet<>();
        this.waitForHashObj = new Object();
    }

    private static String returnMD5Hash(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str.getBytes());
        byte[] digest = md.digest();
        String hash = DatatypeConverter.printHexBinary(digest).toUpperCase();
        return hash;
    }

    private boolean isCorrectPassword(String passwordAttempt, String hashToDecode) throws NoSuchAlgorithmException {
//        System.out.println(hashToDecode);
        return hashToDecode.equals(returnMD5Hash(passwordAttempt));
    }

    public void addRequest(String hashTpDecode) throws InterruptedException {
        validateHash(hashTpDecode);
        synchronized (this.hashesToDecode) {
            this.hashesToDecode.add(hashTpDecode.toUpperCase());
        }
        System.out.println("request added");
        synchronized (this.waitForHashObj) {
            this.waitForHashObj.notify();
        }
    }

    public void updateDecodingRange(int[] range) throws InterruptedException {
        validateDecodingRange(range);
        System.out.println("changing range to " + range[0] + "-" + range[1]);
        this.startOfSearchRange = range[0];
        this.endOfSearchRange = range[1];
    }

    //todo-implement
    private void validateDecodingRange(int[] range) {
        System.out.println("validating range");
    }

    private void decodingHash() throws NoSuchAlgorithmException {
        for (int i = this.startOfSearchRange; i <= this.endOfSearchRange; i++) {
            if (i < this.startOfSearchRange) {
                break;
            }
            String[] hashes;
            synchronized (this.hashesToDecode) {
                if (this.hashesToDecode.isEmpty()) {
                    break;
                }
                hashes = this.hashesToDecode.toArray(new String[0]);
            }
            String passwordAttempt = String.format("05%08d", i);
            for (String hashToDecode : hashes) {
                if (isCorrectPassword(passwordAttempt, hashToDecode)) {
                    System.out.println("password for hash " + hashToDecode + " is " + passwordAttempt);
                    sendResultToMaster(hashToDecode, passwordAttempt);
                }
            }
        }
    }

    private void sendResultToMaster(String decodedHash, String decodedPassword) {
        String masterUri = this.masterHostname + ":" + this.masterPort;
        RestTemplate rt = new RestTemplate();
        String uri = "http://" + masterUri + "/getResult";
        String[] results = {decodedHash, decodedPassword};
        HttpEntity<String[]> request = new HttpEntity<>(results);
        ResponseEntity<String[]> returnReq = rt.postForEntity(uri, request, String[].class);
    }

    //todo- implement
    private void validateHash(String HashToDecode) {
        System.out.println("validating hash: " + HashToDecode);
    }

    @Override
    public void run() {
        registerToMaster();
        decoding();
    }

    private void registerToMaster() {
        String masterUri = this.masterHostname + ":" + this.masterPort;
        String localUri = this.hostname + ":" + this.port;
        System.out.println("Registering local minion server " + localUri + " to master server " + masterUri);
        RestTemplate rt = new RestTemplate();
        String uri = "http://" + masterUri + "/registerMinionServer";
        HttpEntity<String> request = new HttpEntity<>(localUri);
        ResponseEntity<String> returnReq = rt.postForEntity(uri, request, String.class);
    }

    private void decoding() {
        while (true) {
            if (this.hashesToDecode.isEmpty()) {
                System.out.println("no hash to decode");
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
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeHashToDecode(String hashToRemove) {
        if (!this.hashesToDecode.contains(hashToRemove)) {
            System.out.println("No hash " + hashToRemove + " in decoding list");
        }
        synchronized (this.hashesToDecode) {
            this.hashesToDecode.remove(hashToRemove);
            System.out.println(hashToRemove + " removed from decoding list");
        }
    }
}
