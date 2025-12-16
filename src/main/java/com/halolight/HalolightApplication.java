package com.halolight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class HalolightApplication {

    public static void main(String[] args) {
        SpringApplication.run(HalolightApplication.class, args);
    }
}
