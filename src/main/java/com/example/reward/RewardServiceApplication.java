package com.example.reward;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class RewardServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RewardServiceApplication.class, args);
    }
}
