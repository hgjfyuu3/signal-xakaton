package ru.xakaton.signal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.xakaton.signal.service.VkBot;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@EnableScheduling
public class SignalXakatonApplication {

    public static void main(String[] args) {
        SpringApplication.run(SignalXakatonApplication.class, args);
    }
}