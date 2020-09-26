package com.jyb.monitor.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.jyb.monitor.server")
public class ServerStart {

    public static void main(String[] args) {
        SpringApplication.run(ServerStart.class, args);
    }

}
