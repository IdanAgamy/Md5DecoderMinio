package com.idan.md5DecoderMinion;

import com.idan.md5DecoderMinion.threads.StartDecodingThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.util.Timer;

@SpringBootApplication
@EnableAsync
public class Md5DecoderMinionApplication {

	@Autowired
	private StartDecodingThread task;

	public static void main(String[] args) {
		SpringApplication.run(Md5DecoderMinionApplication.class, args);

	}

	@PostConstruct
	private void startThreads() {
		Timer timer = new Timer();
		timer.schedule(task, 1000);
	}

}
