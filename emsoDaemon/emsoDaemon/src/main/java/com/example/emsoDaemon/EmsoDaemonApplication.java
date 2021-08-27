package com.example.emsoDaemon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EmsoDaemonApplication {
	public static void main(String[] args) {
		SpringApplication.run(EmsoDaemonApplication.class, args);
	}

}


