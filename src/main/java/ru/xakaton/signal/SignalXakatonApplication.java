package ru.xakaton.signal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import api.longpoll.bots.exceptions.VkApiException;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
public class SignalXakatonApplication {


    public static void main(String[] args) {
        SpringApplication.run(SignalXakatonApplication.class, args);
    }

    @PostConstruct
    protected void createBotServer() {
        CompletableFuture.runAsync(() -> {
            try {
                new VkBot().startPolling();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}