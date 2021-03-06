package com.idan.md5DecoderMinion.api;

import com.idan.md5DecoderMinion.controler.MinionController;
import com.idan.md5DecoderMinion.exceptions.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MinionApi {

    @Autowired
    private MinionController controller;

    @RequestMapping(value = "/decodeRequest", method = RequestMethod.POST)
    public void startDecoding(@RequestBody String hashToDecode) throws ApplicationException {
        controller.addRequest(hashToDecode);
    }

    @RequestMapping(value = "/multipleDecodeRequest", method = RequestMethod.POST)
    public void startDecoding(@RequestBody String[] hashesToDecode) throws ApplicationException {
        controller.addRequest(hashesToDecode);
    }

    @RequestMapping(value = "/removeHash", method = RequestMethod.POST)
    public void removeHashToDecode(@RequestBody String hashToRemove) {
        controller.removeHashToDecode(hashToRemove);
    }

    @RequestMapping(value = "/updateRange", method = RequestMethod.POST)
    public void updateRange(@RequestBody int[] range) throws ApplicationException {
        controller.updateDecodingRange(range);
    }

    @RequestMapping(method = RequestMethod.GET)
    public void isAlive() {
    }
}
