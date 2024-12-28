package com.cloudbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.cloudbackend.repository")
@EntityScan(basePackages = "com.cloudbackend.entity")
public class CloudbackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudbackendApplication.class, args);
    }

}
