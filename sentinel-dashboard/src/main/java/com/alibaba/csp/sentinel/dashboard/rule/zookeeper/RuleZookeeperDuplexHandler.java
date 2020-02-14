package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import com.alibaba.csp.sentinel.dashboard.rule.AbstractRuleDuplexHandler;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;

/**
 *  
 *
 * @author tomgs
 * @version 2020/2/11 1.0 
 */
public class RuleZookeeperDuplexHandler extends AbstractRuleDuplexHandler {

    private CuratorFramework zkClient;

    public RuleZookeeperDuplexHandler(final CuratorFramework zkClient) {
        this.zkClient = zkClient;
    }

    @Override
    public String getRules(String zkPath) throws Exception {
        byte[] bytes = zkClient.getData().forPath(zkPath);
        if (null == bytes || bytes.length == 0) {
            return null;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public void publish(String zkPath, Object rules) throws Exception {
        AssertUtil.notEmpty(zkPath, "app name cannot be empty");

        Stat stat = zkClient.checkExists().forPath(zkPath);
        if (stat == null) {
            zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zkPath, null);
        }
        byte[] data = rules == null ? "[]".getBytes() : JSON.toJSONString(rules).getBytes(StandardCharsets.UTF_8);
        zkClient.setData().forPath(zkPath, data);
    }

}
