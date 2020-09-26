package com.jyb.monitor.server.config;

import com.jyb.monitor.server.service.DelyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ServiceOnlineListener implements ApplicationRunner {
    @Autowired
    DelyService delyService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        delyService.serviceOnlineCheck();
    }
}
