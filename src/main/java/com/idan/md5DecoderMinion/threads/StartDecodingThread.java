package com.idan.md5DecoderMinion.threads;

import com.idan.md5DecoderMinion.controler.MinionController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.TimerTask;

@Component
public class StartDecodingThread extends TimerTask {

    @Autowired
    private MinionController controller;

    @Override
    public void run() {
        Thread t = new Thread(controller);
        t.start();
    }
}
