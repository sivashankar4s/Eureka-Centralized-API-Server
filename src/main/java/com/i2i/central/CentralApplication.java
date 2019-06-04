package com.i2i.central;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableEurekaServer
public class CentralApplication {

	public static void main(String[] args) {
		SpringApplication.run(CentralApplication.class, args);
	}

}
