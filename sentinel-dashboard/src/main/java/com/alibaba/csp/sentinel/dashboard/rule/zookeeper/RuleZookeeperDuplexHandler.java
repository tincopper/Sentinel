package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import com.alibaba.fastjson.JSON;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *  
 *
 * @author tomgs
 * @version 2020/2/11 1.0 
 */
@Component("ruleZookeeperDuplex")
public class RuleZookeeperDuplexHandler extends AbstractRuleZookeeperDuplexHandler {
    
    public RuleZookeeperDuplexHandler(CuratorFramework zkClient) {
        super(zkClient);
    }

    public List<FlowRuleEntity> getFlowRules(String appName) throws Exception {
        return super.getRules(ZkConfigUtil.getFlowDataId(appName),
                source -> JSON.parseArray(source, FlowRuleEntity.class));
    }

    public List<DegradeRuleEntity> getDegradeRules(String appName) throws Exception {
        return super.getRules(ZkConfigUtil.getDegradeDataId(appName),
                source -> JSON.parseArray(source, DegradeRuleEntity.class));
    }

    public List<AuthorityRuleEntity> getAuthorityRules(String appName) throws Exception {
        return super.getRules(ZkConfigUtil.getAuthorityDataId(appName),
                source -> JSON.parseArray(source, AuthorityRuleEntity.class));
    }

    public List<ParamFlowRuleEntity> getParamFlowRules(String appName) throws Exception {
        return super.getRules(ZkConfigUtil.getParamFlowDataId(appName),
                source -> JSON.parseArray(source, ParamFlowRuleEntity.class));
    }

    public List<SystemRuleEntity> getSystemRules(String appName) throws Exception {
        return super.getRules(ZkConfigUtil.getSystemDataId(appName),
                source -> JSON.parseArray(source, SystemRuleEntity.class));
    }

}
