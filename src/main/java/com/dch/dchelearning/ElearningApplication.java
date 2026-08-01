package com.dch.dchelearning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ElearningApplication {

    static void main(String[] args) {
        SpringApplication.run(ElearningApplication.class, args);
    }

}
