package com.example.soop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class SoopApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoopApplication.class, args);
    }

}
