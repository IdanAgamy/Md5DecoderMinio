package com.idan.md5DecoderMinion.controler;

//import com.idan.md5DecoderMinion.beans.DecodeRequest;

import com.idan.md5DecoderMinion.enums.ErrorType;
import com.idan.md5DecoderMinion.exceptions.ApplicationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Controller
public class MinionController implements Runnable {

    private final Set<String> hashesToDecode;
    private int startOfSearchRange;
    private int endOfSearchRange;
    private final Object waitForHashObj;
    private static final Logger logger = LogManager.getLogger(MinionController.class);

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

    private static String returnMD5Hash(String str) throws ApplicationException {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] digest = md.digest();
            String hash = DatatypeConverter.printHexBinary(digest).toUpperCase();
            return hash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new ApplicationException("Could not encode hash for password: " + str, e, ErrorType.SYSTEM_ERROR);
        }
    }

    private boolean isCorrectPassword(String passwordAttempt, String hashToDecode) throws ApplicationException {
        return hashToDecode.equals(returnMD5Hash(passwordAttempt));
    }

    public void addRequest(String hashTpDecode) throws ApplicationException {
        validateHash(hashTpDecode);
        synchronized (this.hashesToDecode) {
            this.hashesToDecode.add(hashTpDecode.toUpperCase());
        }
        System.out.println("request added");
        synchronized (this.waitForHashObj) {
            this.waitForHashObj.notify();
        }
    }

    public void addRequest(String[] hashesTpDecode) throws ApplicationException {
        for (String hash : hashesTpDecode) {
            addRequest(hash);
        }
    }

    public void updateDecodingRange(int[] range) throws ApplicationException {
        validateDecodingRange(range);
        logger.info(String.format("changing range to 05%08d-05%08d", range[0], range[1]));
        this.startOfSearchRange = range[0];
        this.endOfSearchRange = range[1];
    }

    private void decodingHash() throws ApplicationException {
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
                    logger.info("password for hash " + hashToDecode + " is " + passwordAttempt);
                    sendResultToMaster(hashToDecode, passwordAttempt);
                }
            }
        }
    }

    private void sendResultToMaster(String decodedHash, String decodedPassword) throws ApplicationException {
        String masterUri = this.masterHostname + ":" + this.masterPort;
        RestTemplate rt = new RestTemplate();
        String uri = "http://" + masterUri + "/getResult";
        String[] results = {decodedHash, decodedPassword};
        HttpEntity<String[]> request = new HttpEntity<>(results);
        try {
            ResponseEntity<String[]> returnReq = rt.postForEntity(uri, request, String[].class);
        } catch (RestClientException e) {
            e.printStackTrace();
            throw new ApplicationException("Could not send results to master server", e, ErrorType.HTTP_REQUEST_ERROR);
        }
    }

    @Override
    public void run() {
        try {
            registerToMaster();
            decoding();
        } catch (ApplicationException e) {
            e.printStackTrace();
            logger.error("Register failed, existing");
            System.exit(e.getErrorType().getNumber());
        }
    }

    private void registerToMaster() throws ApplicationException {
        String masterUri = this.masterHostname + ":" + this.masterPort;
        String localUri = this.hostname + ":" + this.port;
        logger.info("Registering local minion server " + localUri + " to master server " + masterUri);
        RestTemplate rt = new RestTemplate();
        String uri = "http://" + masterUri + "/registerMinionServer";
        HttpEntity<String> request = new HttpEntity<>(localUri);
        try {
            ResponseEntity<String> returnReq = rt.postForEntity(uri, request, String.class);
        } catch (RestClientException e) {
            e.printStackTrace();
            throw new ApplicationException("Could not register to master server", e, ErrorType.HTTP_REQUEST_ERROR);
        }
    }

    private void decoding() throws ApplicationException {
        while (true) {
            if (this.hashesToDecode.isEmpty()) {
                logger.debug("no hash to decode");
                try {
                    synchronized (waitForHashObj) {
                        this.waitForHashObj.wait();
                        logger.debug("resuming");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            decodingHash();
        }
    }

    public void removeHashToDecode(String hashToRemove) {
        if (!this.hashesToDecode.contains(hashToRemove)) {
            logger.debug("No hash " + hashToRemove + " in decoding list");
        }
        synchronized (this.hashesToDecode) {
            this.hashesToDecode.remove(hashToRemove);
            logger.info(hashToRemove + " removed from decoding list");
        }
    }

    private void validateDecodingRange(int[] range) throws ApplicationException {
        for (int limit : range) {
            if (limit > 99999999 || limit < 0) {
                throw new ApplicationException("range out of bound (" + limit + ")", ErrorType.INCORRECT_VALIDATION);
            }
        }
        if (range[0] > range[1]) {
            throw new ApplicationException("range out of bound parameters are reversed", ErrorType.INCORRECT_VALIDATION);
        }
    }

    private void validateHash(String hash) throws ApplicationException {
        if (hash.length() != 32) {
            throw new ApplicationException("The hash is not 32 char long or the password is not 10 char long " + hash + ").", ErrorType.INCORRECT_VALIDATION);
        }
        if (!Pattern.compile("^[0-9A-Fa-f]+$").matcher(hash).matches()) {
            throw new ApplicationException("the hash is not in hex " + hash + ").", ErrorType.INCORRECT_VALIDATION);
        }
    }
}
