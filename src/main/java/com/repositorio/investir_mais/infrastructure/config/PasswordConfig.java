package com.repositorio.investir_mais.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
public class PasswordConfig {

    @Value("${security.argon2.salt-length}")
    private int saltLength;

    @Value("${security.argon2.hash-length}")
    private int hashLength;

    @Value("${security.argon2.parallelism}")
    private int parallelism;

    @Value("${security.argon2.memory}")
    private int memory;

    @Value("${security.argon2.iterations}")
    private int iterations;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(
                saltLength,
                hashLength,
                parallelism,
                memory,
                iterations
        );
    }
}
