package com.tfg.ditraflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DitraFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(DitraFlowApplication.class, args);
    }
}