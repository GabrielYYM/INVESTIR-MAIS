package com.repositorio.investir_mais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class InvestirMaisApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvestirMaisApplication.class, args);
	}

}
