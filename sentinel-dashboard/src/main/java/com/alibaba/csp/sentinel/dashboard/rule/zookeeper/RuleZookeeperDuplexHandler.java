package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.fastjson.JSON;
import org.apache.curator.framework.CuratorFramework;

import java.util.List;
import java.util.stream.Collectors;

/**
 *  
 *
 * @author tomgs
 * @version 2020/2/11 1.0 
 */
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

    public List<ParamFlowRuleEntity> getParamFlowRules(String appName, String ip, int port) throws Exception {
        List<ParamFlowRule> rules = super.getRules(ZkConfigUtil.getParamFlowDataId(appName),
                source -> JSON.parseArray(source, ParamFlowRule.class));
        return rules.stream().map(e -> ParamFlowRuleEntity.fromAuthorityRule(appName, ip, port, e))
                .collect(Collectors.toList());
    }

    public List<SystemRuleEntity> getSystemRules(String appName) throws Exception {
        return super.getRules(ZkConfigUtil.getSystemDataId(appName),
                source -> JSON.parseArray(source, SystemRuleEntity.class));
    }

}
