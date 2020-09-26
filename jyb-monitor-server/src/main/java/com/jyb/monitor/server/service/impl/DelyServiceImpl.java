package com.jyb.monitor.server.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.jyb.monitor.server.service.DelyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DelyServiceImpl implements DelyService {

    @Autowired
    CuratorFramework client;

    //失败的服务列表
    ConcurrentHashMap map = new ConcurrentHashMap();

    @Override
    public void addDelyService(String path,String content) {
        map.put(path,content);
    }

    @Override
    public void checkServcesStatus(){
        map.forEach((k, v) -> {
            log.info("path:【{}】- url:【{}】：",k,v);
            HttpResponse response = HttpRequest.get((String) v).timeout(20000).execute();
                    int status = response.getStatus();
                    String content = response.body();

                    if((null != content && content.indexOf("Whitelabel") > -1) || 200 == status){
                        try {
                            //上线后更新zk
                            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath((String) k, ((String) v).getBytes());
                            //从下线列表移除
                            map.remove(k);
                        } catch (Exception e) {
                            log.error("path:【{}】- url:【{}】 ",k,v,e);
                        }
                    }
                }
        );
    }

    @Override
    public void changeStatus(String status, String path, String content) {
        String message = "服务上线下通知，请相关同事注意。\n" +
                "> 当前环境:<font color='comment'>  %s</font>\n" +
                "> 服务名称:<font color='comment'>  %s</font>\n" +
                "> 服务器ip:<font color='comment'> %s</font>\n" +
                "> 服务地址:<font color='comment'>  [%s](%s)</font>\n" ;
        if(status.equals("上线")){
            message = message + "> 当前状态:<font color='info'> %s</font>";
        }
        if(status.equals("下线")){
            message = message + "> 当前状态:<font color='warning'>  %s</font>";
        }
        String [] str = path.split("/");
        if(str.length > 4){
            //上线 下线发送消息
            send(String.format(message,str[2],str[3],str[4],content,content,status));

            //下线后添加到下线队列
            if("下线".equals(status)){
                addDelyService(path,content);
            }
        }
    }

    @Override
    public void serviceOnlineCheck() {
        log.info("________________服务启动中，开始进行检查_________________");
        //定义watch
        TreeCache treeCache = new TreeCache(client, "/tomcat");
        try {
            treeCache.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        treeCache.getListenable().addListener((curatorFramework, treeCacheEvent) -> {
            if(treeCacheEvent.getData() == null){
                return;
            }
            if(treeCacheEvent.getData().getPath() == null){
                return;
            }
            if(treeCacheEvent.getData().getData() == null){
                return;
            }
            String path = treeCacheEvent.getData().getPath();
            String content = new String(treeCacheEvent.getData().getData());
            switch (treeCacheEvent.getType()) {
                case NODE_ADDED:
                    changeStatus("上线",path,content);
                    break;
                case NODE_UPDATED:
                    changeStatus("变更了",path,content);
                    break;
                case NODE_REMOVED:
                    changeStatus("下线",path,content);
                    break;
                default:
                    break;
            }
        });
//        finally {
//            CloseableUtils.closeQuietly(client);//建议放在finally块中
//        }
    }


    /**
     * 触发企业微信消息-告警
     * @param content
     */
    private void send(String content) {
        log.info(content);
        content = "{\"msgtype\":\"markdown\"," +"\"markdown\":{\"content\":\"" + content + "\"}}";
        HttpRequest.post("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=7339d20e-5e30-42e8-bd34-9d6978b534dc11")
                .body(content)
                .timeout(20000)//超时，毫秒
                .execute().body();
    }
}
