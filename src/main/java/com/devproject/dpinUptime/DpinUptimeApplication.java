package com.devproject.dpinUptime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableScheduling
@SpringBootApplication
public class DpinUptimeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DpinUptimeApplication.class, args);
	}

}
