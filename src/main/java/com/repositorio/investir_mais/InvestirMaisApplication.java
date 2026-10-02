package com.repositorio.investir_mais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class InvestirMaisApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvestirMaisApplication.class, args);
	}

}