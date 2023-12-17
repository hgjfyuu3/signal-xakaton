package ru.xakaton.signal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SignalXakatonApplication {

    public static void main(String[] args) {
        SpringApplication.run(SignalXakatonApplication.class, args);
    }

}