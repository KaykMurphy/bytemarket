package com.bytemarket.bytemarket_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class BytemarketApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BytemarketApiApplication.class, args);
	}

}
