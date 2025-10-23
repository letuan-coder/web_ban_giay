package com.example.DATN;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EntityScan(basePackages = "com.example.DATN.models")
@EnableCaching
public class DatnApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatnApplication.class, args);
		System.out.println("Ung dung khoi dong thanh cong!");
	}

}
