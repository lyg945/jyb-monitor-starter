package com.jyb.monitor.server.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ZkConfig {
    @Autowired
    Environment environment;

    /**
     * 获取 zk 客户端
     * @return
     */
    @Bean
    public CuratorFramework client(){
        String profile = environment.getActiveProfiles()[0];
        String zk = getZk(profile);

        CuratorFramework client = CuratorFrameworkFactory.builder().connectString(zk)
                .sessionTimeoutMs(1000)    // 连接超时时间
                .connectionTimeoutMs(1000) // 会话超时时间
                // 刚开始重试间隔为1秒，之后重试间隔逐渐增加，最多重试不超过三次
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();
        return client;
    }

    /**
     * 根据环境变量获取zk地址
     * @param activeProfile
     * @return
     */
    private String getZk(String activeProfile) {
        String zk = "172.16.5.137:2181,172.16.5.138:2181,172.16.5.139:2181";

        if(activeProfile.equals("vip")){
            zk = "172.16.5.164:2181,172.16.5.165:2181,172.16.5.166:2181";
        }
        if(activeProfile.equals("test")){
            zk = "172.16.5.161:2181,172.16.5.162:2181,172.16.5.163:2181";
        }
        if(activeProfile.equals("prd")){
            zk = "172.16.1.249:2181,172.16.1.250:2181,172.16.1.251:2181";
        }
        return zk;
    }

}
