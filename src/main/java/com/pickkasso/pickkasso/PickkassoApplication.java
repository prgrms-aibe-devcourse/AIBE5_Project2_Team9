package com.pickkasso.pickkasso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PickkassoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PickkassoApplication.class, args);
	}

}
