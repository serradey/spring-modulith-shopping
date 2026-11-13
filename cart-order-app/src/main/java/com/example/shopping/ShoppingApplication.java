package com.example.shopping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Boot entry point for the modular monolith hosting the {@code cart} and {@code order} modules.
 *
 * <p>{@link EnableAsync} is required because Spring Modulith's {@code @ApplicationModuleListener}
 * handlers (item popularity, order creation, Kafka externalization) run asynchronously after the
 * publishing transaction commits.
 */
@SpringBootApplication
@EnableAsync
public class ShoppingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingApplication.class, args);
    }
}
