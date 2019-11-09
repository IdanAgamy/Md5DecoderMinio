package com.idan.md5DecoderMinion.api;

import com.idan.md5DecoderMinion.beans.DecodeRequest;
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

    @RequestMapping(method = RequestMethod.POST)
    public void startDecoding(@RequestBody DecodeRequest request) throws InterruptedException {
        Thread t = new Thread(controller);
        t.start();

        controller.addRequest(request);
    }
}
