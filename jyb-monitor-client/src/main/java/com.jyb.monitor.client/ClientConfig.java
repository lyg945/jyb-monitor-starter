package com.jyb.monitor.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class ClientConfig implements ApplicationRunner {
    @Autowired
    Environment environment;

    @Value("${spring.application.name}")
    String name ;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String profile = environment.getActiveProfiles()[0];
        String zk = getZk(profile);

        CuratorFramework client = CuratorFrameworkFactory.builder().connectString(zk)
                .sessionTimeoutMs(1000)    // 连接超时时间
                .connectionTimeoutMs(1000) // 会话超时时间
                // 刚开始重试间隔为1秒，之后重试间隔逐渐增加，最多重试不超过三次
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();

        String path =String.format("/tomcat/%s/%s/%s",profile,name,getHost());

        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, getUrl().getBytes());
//            client.create().forPath("/treeCache", "123".getBytes());
//            client.setData().forPath("/treeCache", "789".getBytes());
//            client.setData().forPath("/treeCache/c1", "910".getBytes());
//            client.delete().forPath("/treeCache/c1");
//            client.delete().forPath("/treeCache");
//            Thread.sleep(5000);
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

    /**
     * 服务端口
     * @return
     */
    public String getPort(){
        return environment.getProperty("local.server.port");
    }

    /**
     * 服务器地址
     * @return
     */
    public String getHost() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    /**
     * 获取访问URL
     * @return
     */
    public String getUrl() throws UnknownHostException {
        return "http://"+getHost() +":"+getPort();
    }
}
