package com.alibaba.csp.sentinel.dashboard.rule.zookeeper.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *  
 *
 * @author tomgs
 * @version 2020/2/11 1.0 
 */
@ConditionalOnProperty(value = "sentinel.datasource.type", havingValue = "zookeeper")
@Configuration
@EnableZkPersistence
public class ZkConfig {

    @Value("${zk.remote-address:127.0.0.1:2181}")
    private String remoteAddress;

    @Value("${zk.root-path:/}")
    private String rootPath;

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public String getRootPath() {
        return rootPath;
    }

    @Bean(destroyMethod = "close")
    public CuratorFramework curatorFramework() {
        CuratorFramework zkClient = CuratorFrameworkFactory.newClient(remoteAddress, new ExponentialBackoffRetry(1000, 3));
        zkClient.start();
        return zkClient;
    }

}
