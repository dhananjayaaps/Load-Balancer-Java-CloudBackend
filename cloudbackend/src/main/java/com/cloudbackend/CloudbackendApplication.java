package com.cloudbackend;

import com.cloudbackend.service.AdminInitializationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CloudbackendApplication {

    private final AdminInitializationService adminInitializationService;

    public CloudbackendApplication(AdminInitializationService adminInitializationService) {
        this.adminInitializationService = adminInitializationService;
    }

    public static void main(String[] args) {
        SpringApplication.run(CloudbackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            adminInitializationService.initializeAdmin();
        };
    }
}
