package com.AttendPulse.attend_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AttendBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AttendBackendApplication.class, args);
	}

}
