package com.idan.md5DecoderMinion.api;

import com.idan.md5DecoderMinion.controler.MinionController;
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
    public void startDecoding(@RequestBody String hashToDecode) throws InterruptedException {


        controller.addRequest(hashToDecode);
    }

    @RequestMapping(value = "/removeHash", method = RequestMethod.POST)
    public void removeHashToDecode(@RequestBody String hashToRemove) throws InterruptedException {
        controller.removeHashToDecode(hashToRemove);
    }

    @RequestMapping(value = "/updateRange", method = RequestMethod.POST)
    public void updateRange(@RequestBody int[] range) throws InterruptedException {
        controller.updateDecodingRange(range);
    }

    @RequestMapping(method = RequestMethod.GET)
    public void isAlive() {
    }
}
