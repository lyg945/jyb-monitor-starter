package com.jyb.monitor.server.config;


import com.jyb.monitor.server.service.DelyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@Slf4j
@Configuration
@EnableScheduling
public class CheckServiceRecoveryTask {
    @Autowired
    DelyService delyService;

    /**
     * 定时任务检查  每隔 20s 检查一次 已下线的服务是否已恢复
     */
    @Scheduled(cron = "0/20 * * * * ?")
    private void check() {
        log.info("----开始轮询检测-下线的服务是否重新上线--");
        delyService.checkServcesStatus();
    }

}
