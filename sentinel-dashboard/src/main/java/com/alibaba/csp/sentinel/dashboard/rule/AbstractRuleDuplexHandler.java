package com.alibaba.csp.sentinel.dashboard.rule;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.ClusterGroupEntity;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import org.apache.zookeeper.KeeperException;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 *  rule provider and publisher template
 *
 * @author tomgs
 * @version 2020/2/14 1.0 
 */
public abstract class AbstractRuleDuplexHandler implements DynamicRuleProvider<String>, DynamicRulePublisher<Object> {

    public <T> T getRules(String dataId, Converter<String, T> converter) throws Exception {
        String rules = getRules(dataId);
        if (StringUtil.isEmpty(rules)) {
            return null;
        }
        return converter.convert(rules);
    }

    public List<FlowRuleEntity> getFlowRules(String dataId, String app, String ip, int port) throws Exception {
        List<FlowRule> rules = getRules(dataId, source -> JSON.parseArray(source, FlowRule.class));
        if (rules != null) {
            return rules.stream().map(rule -> FlowRuleEntity.fromFlowRule(app, ip, port, rule))
                    .collect(Collectors.toList());
        }
        return null;
    }

    public List<DegradeRuleEntity> getDegradeRules(String dataId, String app, String ip, int port) throws Exception {
        List<DegradeRule> rules = getRules(dataId, source -> JSON.parseArray(source, DegradeRule.class));
        if (rules != null) {
            return rules.stream().map(e -> DegradeRuleEntity.fromDegradeRule(app, ip, port, e))
                    .collect(Collectors.toList());
        }
        return null;
    }

    public List<AuthorityRuleEntity> getAuthorityRules(String dataId, String app, String ip, int port) throws Exception {
        List<AuthorityRule> rules = getRules(dataId, source -> JSON.parseArray(source, AuthorityRule.class));
        if (rules != null) {
            return rules.stream().map(e -> AuthorityRuleEntity.fromAuthorityRule(app, ip, port, e))
                    .collect(Collectors.toList());
        }
        return null;
    }

    public List<ParamFlowRuleEntity> getParamFlowRules(String dataId, String app, String ip, int port) throws Exception {
        List<ParamFlowRule> rules = getRules(dataId, source -> JSON.parseArray(source, ParamFlowRule.class));
        if (rules != null) {
            return rules.stream().map(e -> ParamFlowRuleEntity.fromAuthorityRule(app, ip, port, e))
                    .collect(Collectors.toList());
        }
        return null;
    }

    public List<SystemRuleEntity> getSystemRules(String dataId, String app, String ip, int port) throws Exception {
        List<SystemRule> rules = getRules(dataId, source -> JSON.parseArray(source, SystemRule.class));
        if (rules != null) {
            return rules.stream().map(rule -> SystemRuleEntity.fromSystemRule(app, ip, port, rule))
                    .collect(Collectors.toList());
        }
        return null;
    }

    public void publishParamFlowRules(String dataId, List<ParamFlowRuleEntity> rules) throws Exception {
        List<ParamFlowRule> paramFlowRules = rules.stream().map(ParamFlowRuleEntity::getRule).collect(Collectors.toList());
        publish(dataId, paramFlowRules);
    }

    public void publishFlowRules(String dataId, List<FlowRuleEntity> rules) throws Exception {
        List<FlowRule> flowRules = rules.stream().map(FlowRuleEntity::toRule).collect(Collectors.toList());
        publish(dataId, flowRules);
    }

    public void publishDegradeRules(String dataId, List<DegradeRuleEntity> rules) throws Exception {
        List<DegradeRule> degradeRules = rules.stream().map(DegradeRuleEntity::toRule).collect(Collectors.toList());
        publish(dataId, degradeRules);
    }

    public void publishSystemRules(String dataId, List<SystemRuleEntity> rules) throws Exception {
        List<SystemRule> systemRules = rules.stream().map(SystemRuleEntity::toRule).collect(Collectors.toList());
        publish(dataId, systemRules);
    }

    public void publishAuthorityRules(String dataId, List<AuthorityRuleEntity> rules) throws Exception {
        List<AuthorityRule> authorityRules = rules.stream().map(AuthorityRuleEntity::toRule).collect(Collectors.toList());
        publish(dataId, authorityRules);
    }

    public void publishClusterClientConfig(String dataId, ClusterClientConfig config) throws Exception {
        publish(dataId, config);
    }

    public void publishClusterServerTransportConfig(String dataId, String ip, int port, ServerTransportConfig config) throws Exception {
        List<ClusterGroupEntity> groupList = new ArrayList<>();
        AtomicBoolean exist = new AtomicBoolean(false);
        try {
            groupList = getRules(dataId, source -> JSON.parseArray(source, ClusterGroupEntity.class));
            if (!CollectionUtils.isEmpty(groupList)) {
                groupList.forEach(group -> {
                    if (parseMachineId(ip, port).equals(group.getMachineId())) {
                        group.setPort(config.getPort());
                        // 因为只有true才会进入到这里来
                        group.setBelongToApp(true);
                        exist.set(true);
                    }
                });
            }
        } catch (Exception e) {
            if (!(e instanceof KeeperException.NoNodeException)) {
                throw e;
            }
        }
        if (CollectionUtils.isEmpty(groupList) || !exist.get()) {
            groupList.add(new ClusterGroupEntity().setIp(ip).setPort(config.getPort()).setMachineId(parseMachineId(ip, port)).setBelongToApp(true));
        }
        publish(dataId, groupList);
    }

    public void publishClusterMode(String clusterMapConfigDataId, String clusterClientConfigDataId, String ip, int port, int mode) throws Exception {
        List<ClusterGroupEntity> groupList = new ArrayList<>();
        String machineId = parseMachineId(ip, port);
        try {
            groupList = getRules(clusterMapConfigDataId, source -> JSON.parseArray(source, ClusterGroupEntity.class));
        } catch (Exception e) {
            if (!(e instanceof KeeperException.NoNodeException)) {
                throw e;
            }
        }
        switch (mode) {
            case ClusterStateManager.CLUSTER_CLIENT:
                // get server token machine id, ignore the NPE
                ClusterClientConfig clientConfig = getRules(clusterClientConfigDataId, source -> JSON.parseObject(source, ClusterClientConfig.class));
                String serverId = parseMachineId(clientConfig.getServerHost(), clientConfig.getServerPort());
                if (!CollectionUtils.isEmpty(groupList)) {
                    Optional<ClusterGroupEntity> clusterGroupEntityOptional = groupList.stream()
                            .filter(group -> serverId.equals(group.getMachineId()))
                            .findFirst();
                    if (clusterGroupEntityOptional.isPresent()) {
                        clusterGroupEntityOptional.get().getClientSet().add(machineId);
                    } else {
                        groupList.add(newClusterGroupEntity(machineId, clientConfig));
                    }
                }
                if (groupList == null) {
                    groupList = new ArrayList<>();
                }
                groupList.add(newClusterGroupEntity(machineId, clientConfig));
                break;
            case ClusterStateManager.CLUSTER_SERVER:
                if (!CollectionUtils.isEmpty(groupList)) {
                    if (groupList.stream().anyMatch(group -> machineId.equals(group.getMachineId()))) {
                        break;
                    }
                }
                if (groupList == null) {
                    groupList = new ArrayList<>();
                }
                groupList.add(new ClusterGroupEntity().setMachineId(machineId).setIp(ip));
                break;
            default:
                throw new IllegalArgumentException("argument 'mode' value is invalid.");
        }

        publish(clusterMapConfigDataId, groupList);
    }

    private ClusterGroupEntity newClusterGroupEntity(String machineId, ClusterClientConfig clientConfig) {
        String serverId = parseMachineId(clientConfig.getServerHost(), clientConfig.getServerPort());
        HashSet<String> clientSet = new HashSet<>();
        clientSet.add(machineId);
        return new ClusterGroupEntity().setMachineId(serverId).setIp(clientConfig.getServerHost()).setClientSet(clientSet);
    }

    private String parseMachineId(String ip, int port) {
        return ip + "@" + port;
    }

}
