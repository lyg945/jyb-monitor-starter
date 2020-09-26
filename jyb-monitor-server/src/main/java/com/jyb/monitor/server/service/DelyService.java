package com.jyb.monitor.server.service;


public interface DelyService {

    /**
     * 定时任务检测-服务状态
     */
    void checkServcesStatus();

    /**
     * 项目启动时-监听zk中服务状态
     */
    void serviceOnlineCheck();

    /**
     * 服务在线状态变更
     **/
    void changeStatus(String status, String path,String content);

    /**
     * 添加 下线的 服务
     */
    void addDelyService(String path,String content);

}
