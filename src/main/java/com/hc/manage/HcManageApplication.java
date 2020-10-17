package com.hc.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class HcManageApplication {

	public static void main(String[] args) {
		SpringApplication.run(HcManageApplication.class, args);
	}

}
