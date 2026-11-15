package com.debbech.agenticbot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@Slf4j
@SpringBootApplication
@EnableScheduling
public class Main {

    public static void main(String[] args) {
        log.info("-- hello agent v1.0 --");
        SpringApplication.run(Main.class, args);
    }

}
