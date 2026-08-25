package com.example.javademo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JavademoApplication {
    public static void main(String[] args) {
        SpringApplication.run(JavademoApplication.class, args);
    }
}
