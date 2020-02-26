package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import com.alibaba.csp.sentinel.dashboard.discovery.AppInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ServerFlowConfig;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.config.ServerTransportConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 扩展sentinelApiClient支持zk的方式更新规则数据
 *
 * @author tangzy
 */
public class ZkSentinelApiClient extends SentinelApiClient {

    private static Logger logger = LoggerFactory.getLogger(ZkSentinelApiClient.class);

    private RuleZookeeperDuplexHandler ruleZookeeperDuplexHandler;
    private AppManagement appManagement;

    public ZkSentinelApiClient(final RuleZookeeperDuplexHandler ruleZookeeperDuplexHandler, final AppManagement appManagement) {
        super();
        this.ruleZookeeperDuplexHandler = ruleZookeeperDuplexHandler;
        this.appManagement = appManagement;
    }

    @Override
    public List<FlowRuleEntity> fetchFlowRuleOfMachine(String app, String ip, int port) {
        List<FlowRuleEntity> flowRuleEntities;
        try {
            flowRuleEntities = ruleZookeeperDuplexHandler.getFlowRules(ZkConfigUtil.getFlowDataId(app), app, ip, port);
        } catch (Exception e) {
            logger.warn("fetch flow rule from zk error: {}", e.getMessage(), e);
            flowRuleEntities = super.fetchFlowRuleOfMachine(app, ip, port);
        }
        return flowRuleEntities;
    }

    @Override
    public List<DegradeRuleEntity> fetchDegradeRuleOfMachine(String app, String ip, int port) {
        List<DegradeRuleEntity> degradeRuleEntities;
        try {
            degradeRuleEntities = ruleZookeeperDuplexHandler.getDegradeRules(ZkConfigUtil.getDegradeDataId(app), app, ip, port);
        } catch (Exception e) {
            logger.warn("fetch degrade rule from zk error: {}", e.getMessage(), e);
            degradeRuleEntities = super.fetchDegradeRuleOfMachine(app, ip, port);
        }
        return degradeRuleEntities;
    }

    @Override
    public List<SystemRuleEntity> fetchSystemRuleOfMachine(String app, String ip, int port) {
        List<SystemRuleEntity> systemRuleEntities;
        try {
            systemRuleEntities = ruleZookeeperDuplexHandler.getSystemRules(ZkConfigUtil.getSystemDataId(app), app, ip, port);
        } catch (Exception e) {
            logger.warn("fetch system rule from zk error: {}", e.getMessage(), e);
            systemRuleEntities = super.fetchSystemRuleOfMachine(app, ip, port);
        }
        return systemRuleEntities;
    }

    @Override
    public List<AuthorityRuleEntity> fetchAuthorityRulesOfMachine(String app, String ip, int port) {
        List<AuthorityRuleEntity> authorityRuleEntities;
        try {
            authorityRuleEntities = ruleZookeeperDuplexHandler.getAuthorityRules(ZkConfigUtil.getAuthorityDataId(app), app, ip, port);
        } catch (Exception e) {
            logger.warn("fetch authority rule from zk error: {}", e.getMessage(), e);
            authorityRuleEntities = super.fetchAuthorityRulesOfMachine(app, ip, port);
        }
        return authorityRuleEntities;
    }

    @Override
    public CompletableFuture<List<ParamFlowRuleEntity>> fetchParamFlowRulesOfMachine(String app, String ip, int port) {
        CompletableFuture<List<ParamFlowRuleEntity>> listCompletableFuture = new CompletableFuture<>();
        try {
            List<ParamFlowRuleEntity> paramFlowRuleEntities = ruleZookeeperDuplexHandler.getParamFlowRules(ZkConfigUtil.getParamFlowDataId(app), app, ip, port);
            listCompletableFuture.complete(paramFlowRuleEntities);
        } catch (Exception e) {
            logger.warn("fetch param flow rule from zk error: {}", e.getMessage(), e);
            listCompletableFuture = super.fetchParamFlowRulesOfMachine(app, ip, port);
        }
        return listCompletableFuture;
    }

    @Override
    public CompletableFuture<Void> setFlowRuleOfMachineAsync(String app, String ip, int port, List<FlowRuleEntity> rules) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        try {
            ruleZookeeperDuplexHandler.publishFlowRules(ZkConfigUtil.getFlowDataId(app), rules);
            completableFuture.complete(null);
        } catch (Exception e) {
            logger.error("publish flow rule to zk error: {}", e.getMessage(), e);
            completableFuture.completeExceptionally(e);
        }
        // 如果zk出现问题可以走内存的方式去修改规则数据
        if (completableFuture.isCompletedExceptionally()) {
            completableFuture = super.setFlowRuleOfMachineAsync(app, ip, port, rules);
        }
        return completableFuture;
    }

    @Override
    public CompletableFuture<Void> setParamFlowRuleOfMachine(String app, String ip, int port, List<ParamFlowRuleEntity> rules) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        try {
            ruleZookeeperDuplexHandler.publishParamFlowRules(ZkConfigUtil.getParamFlowDataId(app), rules);
            completableFuture.complete(null);
        } catch (Exception e) {
            logger.error("publish param flow rule to zk error: {}", e.getMessage(), e);
            completableFuture.completeExceptionally(e);
        }
        // 如果zk出现问题可以走内存的方式去修改规则数据
        if (completableFuture.isCompletedExceptionally()) {
            completableFuture = super.setParamFlowRuleOfMachine(app, ip, port, rules);
        }
        return completableFuture;
    }

    @Override
    public boolean setDegradeRuleOfMachine(String app, String ip, int port, List<DegradeRuleEntity> rules) {
        try {
            ruleZookeeperDuplexHandler.publishDegradeRules(ZkConfigUtil.getDegradeDataId(app), rules);
            return true;
        } catch (Exception e) {
            logger.warn("publish degrade rule from zk error: {}", e.getMessage(), e);
            return super.setDegradeRuleOfMachine(app, ip, port, rules);
        }
    }

    @Override
    public boolean setSystemRuleOfMachine(String app, String ip, int port, List<SystemRuleEntity> rules) {
        try {
            ruleZookeeperDuplexHandler.publishSystemRules(ZkConfigUtil.getSystemDataId(app), rules);
            return true;
        } catch (Exception e) {
            logger.warn("publish system rule from zk error: {}", e.getMessage(), e);
            return super.setSystemRuleOfMachine(app, ip, port, rules);
        }
    }

    @Override
    public boolean setAuthorityRuleOfMachine(String app, String ip, int port, List<AuthorityRuleEntity> rules) {
        try {
            ruleZookeeperDuplexHandler.publishAuthorityRules(ZkConfigUtil.getAuthorityDataId(app), rules);
            return true;
        } catch (Exception e) {
            logger.warn("publish authority rule from zk error: {}", e.getMessage(), e);
            return super.setAuthorityRuleOfMachine(app, ip, port, rules);
        }

    }

    //setFlowRuleOfMachine
    @Override
    public boolean setFlowRuleOfMachine(String app, String ip, int port, List<FlowRuleEntity> rules) {
        try {
            ruleZookeeperDuplexHandler.publishFlowRules(ZkConfigUtil.getFlowDataId(app), rules);
            return true;
        } catch (Exception e) {
            logger.error("publish flow rule to zk error: {}", e.getMessage(), e);
            return super.setFlowRuleOfMachine(app, ip, port, rules);
        }
    }

    //modifyClusterServerTransportConfig
    @Override
    public CompletableFuture<Void> modifyClusterServerTransportConfig(String app, String ip, int port, ServerTransportConfig config) {
        logger.info("modifyClusterServerTransportConfig##app:{}, ip:{}, port:{}, config:{}", app, ip, port, config);
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        try {
            Optional<MachineInfo> machine = appManagement.getDetailApp(app).getMachine(ip, port);
            ruleZookeeperDuplexHandler.publishClusterServerTransportConfig(ZkConfigUtil.getClusterMapConfig(app), ZkConfigUtil.getClusterClientConfig(app), ip, port, config, machine.isPresent());
            completableFuture.complete(null);
        } catch (Exception e) {
            logger.error("publish cluster server transport config to zk error: {}", e.getMessage(), e);
            completableFuture.completeExceptionally(e);
        }
        // 如果zk出现问题可以走内存的方式去修改规则数据
        if (completableFuture.isCompletedExceptionally()) {
            return super.modifyClusterServerTransportConfig(app, ip, port, config);
        }
        return completableFuture;
    }

    @Override
    public CompletableFuture<Void> modifyClusterServerFlowConfig(String app, String ip, int port, ServerFlowConfig config) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        try {
            ruleZookeeperDuplexHandler.publishClusterServerFlowConfig(ZkConfigUtil.getClusterMapConfig(app), ip, port, config);
            completableFuture.complete(null);
        } catch (Exception e) {
            logger.error("publish cluster client config to zk error: {}", e.getMessage(), e);
            completableFuture.completeExceptionally(e);
        }
        // 如果zk出现问题可以走内存的方式去修改规则数据
        if (completableFuture.isCompletedExceptionally()) {
            return super.modifyClusterServerFlowConfig(app, ip, port, config);
        }
        return completableFuture;
    }

    @Override
    public CompletableFuture<Void> modifyClusterServerNamespaceSet(String app, String ip, int port, Set<String> set) {
        logger.info("modifyClusterServerNamespaceSet##app:{}, ip:{}, port:{}, namespace:{}", app, ip, port, set);
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        try {
            ruleZookeeperDuplexHandler.publishClusterServerNamespaceSet(ZkConfigUtil.getClusterNameSpaceDataId(app), set);
            completableFuture.complete(null);
        } catch (Exception e) {
            logger.error("publish cluster client config to zk error: {}", e.getMessage(), e);
            completableFuture.completeExceptionally(e);
        }
        // 如果zk出现问题可以走内存的方式去修改规则数据
        if (completableFuture.isCompletedExceptionally()) {
            return super.modifyClusterServerNamespaceSet(app, ip, port, set);
        }
        return completableFuture;
    }

    @Override
    public CompletableFuture<Void> modifyClusterClientConfig(String app, String ip, int port, ClusterClientConfig config) {
        //String path = "/" + APP_NAME + "/config/common/prop/sentinel.cluster.client.config";
        logger.info("modifyClusterClientConfig##app:{}, ip:{}, port:{}, config:{}", app, ip, port, config);
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        try {
            ruleZookeeperDuplexHandler.publishClusterClientConfig(ZkConfigUtil.getClusterClientConfig(app), config);
            completableFuture.complete(null);
        } catch (Exception e) {
            logger.error("publish cluster client config to zk error: {}", e.getMessage(), e);
            completableFuture.completeExceptionally(e);
        }
        // 如果zk出现问题可以走内存的方式去修改规则数据
        if (completableFuture.isCompletedExceptionally()) {
            return super.modifyClusterClientConfig(app, ip, port, config);
        }
        return completableFuture;
    }

    @Override
    public CompletableFuture<Void> modifyClusterMode(String app, String ip, int port, int mode) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        try {
            ruleZookeeperDuplexHandler.publishClusterMode(ZkConfigUtil.getClusterMapConfig(app),
                    ZkConfigUtil.getClusterClientConfig(app), ip, port, mode);
            if (ClusterStateManager.CLUSTER_NOT_STARTED == mode) {
                super.modifyClusterMode(app, ip, port, mode);
            }
            completableFuture.complete(null);
        } catch (Exception e) {
            logger.error("publish cluster client config to zk error: {}", e.getMessage(), e);
            completableFuture.completeExceptionally(e);
        }
        // 如果zk出现问题可以走内存的方式去修改规则数据
        if (completableFuture.isCompletedExceptionally()) {
            return super.modifyClusterMode(app, ip, port, mode);
        }
        return completableFuture;
    }

}