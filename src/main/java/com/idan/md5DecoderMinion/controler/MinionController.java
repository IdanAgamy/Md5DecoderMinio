package com.idan.md5DecoderMinion.controler;

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

    public MinionController() {
        this.hashesToDecode = new HashSet<>();
    }

    private static String returnMD5Hash(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str.getBytes());
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }

    public boolean isCorrectPassword(String passwordAttempt, String hashToDecode) throws NoSuchAlgorithmException {
        return hashToDecode.equals(returnMD5Hash(passwordAttempt));
    }

    public void addToDecode(String HashToDecode) {
        validateHash(HashToDecode);
        this.hashesToDecode.add(HashToDecode);
    }

    //todo- implement
    private void validateHash(String HashToDecode) {
        System.out.println("validating hash: " + HashToDecode);
    }

    @Override
    public void run() {
        while (true) {
            if (this.hashesToDecode.isEmpty()) {
                try {
                    System.out.println("no hash to decode");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            System.out.println("hello");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
