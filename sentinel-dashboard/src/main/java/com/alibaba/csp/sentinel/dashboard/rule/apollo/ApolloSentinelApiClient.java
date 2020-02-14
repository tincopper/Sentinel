package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 扩展sentinelApiClient支持Apollo的方式更新规则数据
 *
 * @author tangzy
 */
public class ApolloSentinelApiClient extends SentinelApiClient {

  private static Logger logger = LoggerFactory.getLogger(ApolloSentinelApiClient.class);

  private RuleApolloDuplexHandler ruleApolloDuplexHandler;

  public ApolloSentinelApiClient(final RuleApolloDuplexHandler ruleApolloDuplexHandler) {
    super();
    this.ruleApolloDuplexHandler = ruleApolloDuplexHandler;
  }

  @Override
  public List<FlowRuleEntity> fetchFlowRuleOfMachine(String app, String ip, int port) {
    List<FlowRuleEntity> flowRuleEntities;
    try {
      flowRuleEntities = ruleApolloDuplexHandler.getFlowRules(ApolloConfigUtil.getFlowDataId(app), app, ip, port);
    } catch (Exception e) {
      logger.warn("fetch flow rule from apollo error: {}", e.getMessage(), e);
      flowRuleEntities = super.fetchFlowRuleOfMachine(app, ip, port);
    }
    return flowRuleEntities;
  }

  @Override
  public List<DegradeRuleEntity> fetchDegradeRuleOfMachine(String app, String ip, int port) {
    List<DegradeRuleEntity> degradeRuleEntities;
    try {
      degradeRuleEntities = ruleApolloDuplexHandler.getDegradeRules(ApolloConfigUtil.getFlowDataId(app), app, ip, port);
    } catch (Exception e) {
      logger.warn("fetch degrade rule from apollo error: {}", e.getMessage(), e);
      degradeRuleEntities = super.fetchDegradeRuleOfMachine(app, ip, port);
    }
    return degradeRuleEntities;
  }

  @Override
  public List<SystemRuleEntity> fetchSystemRuleOfMachine(String app, String ip, int port) {
    List<SystemRuleEntity> systemRuleEntities;
    try {
      systemRuleEntities = ruleApolloDuplexHandler.getSystemRules(ApolloConfigUtil.getFlowDataId(app), app, ip, port);
    } catch (Exception e) {
      logger.warn("fetch system rule from apollo error: {}", e.getMessage(), e);
      systemRuleEntities = super.fetchSystemRuleOfMachine(app, ip, port);
    }
    return systemRuleEntities;
  }

  @Override
  public List<AuthorityRuleEntity> fetchAuthorityRulesOfMachine(String app, String ip, int port) {
    List<AuthorityRuleEntity> authorityRuleEntities;
    try {
      authorityRuleEntities = ruleApolloDuplexHandler.getAuthorityRules(ApolloConfigUtil.getFlowDataId(app), app, ip, port);
    } catch (Exception e) {
      logger.warn("fetch authority rule from apollo error: {}", e.getMessage(), e);
      authorityRuleEntities = super.fetchAuthorityRulesOfMachine(app, ip, port);
    }
    return authorityRuleEntities;
  }

  @Override
  public CompletableFuture<List<ParamFlowRuleEntity>> fetchParamFlowRulesOfMachine(String app, String ip, int port) {
    CompletableFuture<List<ParamFlowRuleEntity>> listCompletableFuture = new CompletableFuture<>();
    try {
      List<ParamFlowRuleEntity> paramFlowRuleEntities = ruleApolloDuplexHandler.getParamFlowRules(ApolloConfigUtil.getFlowDataId(app), app, ip, port);
      listCompletableFuture.complete(paramFlowRuleEntities);
    } catch (Exception e) {
      logger.warn("fetch param flow rule from apollo error: {}", e.getMessage(), e);
      listCompletableFuture = super.fetchParamFlowRulesOfMachine(app, ip, port);
    }
    return listCompletableFuture;
  }

  @Override
  public CompletableFuture<Void> setFlowRuleOfMachineAsync(String app, String ip, int port,
      List<FlowRuleEntity> rules) {
    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    try {
      ruleApolloDuplexHandler.publishFlowRules(ApolloConfigUtil.getFlowDataId(app), rules);
      completableFuture.complete(null);
    } catch (Exception e) {
      logger.error("publish flow rule to apollo error: {}", e.getMessage(), e);
      completableFuture.completeExceptionally(e);
    }
    // 如果apollo出现问题可以走内存的方式去修改规则数据
    if (completableFuture.isCompletedExceptionally()) {
      completableFuture = super.setFlowRuleOfMachineAsync(app, ip, port, rules);
    }
    return completableFuture;
  }

  @Override
  public CompletableFuture<Void> setParamFlowRuleOfMachine(String app, String ip, int port,
      List<ParamFlowRuleEntity> rules) {
    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    try {
      ruleApolloDuplexHandler.publishParamFlowRules(ApolloConfigUtil.getParamFlowDataId(app), rules);
      completableFuture.complete(null);
    } catch (Exception e) {
      logger.error("publish param flow rule to apollo error: {}", e.getMessage(), e);
      completableFuture.completeExceptionally(e);
    }
    // 如果apollo出现问题可以走内存的方式去修改规则数据
    if (completableFuture.isCompletedExceptionally()) {
      completableFuture = super.setParamFlowRuleOfMachine(app, ip, port, rules);
    }
    return completableFuture;
  }

  @Override
  public boolean setDegradeRuleOfMachine(String app, String ip, int port,
      List<DegradeRuleEntity> rules) {
    try {
      ruleApolloDuplexHandler.publishDegradeRules(ApolloConfigUtil.getDegradeDataId(app), rules);
      return true;
    } catch (Exception e) {
      logger.warn("publish degrade rule from apollo error: {}", e.getMessage(), e);
      return super.setDegradeRuleOfMachine(app, ip, port, rules);
    }
  }

  @Override
  public boolean setSystemRuleOfMachine(String app, String ip, int port,
      List<SystemRuleEntity> rules) {
    try {
      ruleApolloDuplexHandler.publishSystemRules(ApolloConfigUtil.getSystemDataId(app), rules);
      return true;
    } catch (Exception e) {
      logger.warn("publish system rule from apollo error: {}", e.getMessage(), e);
      return super.setSystemRuleOfMachine(app, ip, port, rules);
    }
  }

  @Override
  public boolean setAuthorityRuleOfMachine(String app, String ip, int port,
      List<AuthorityRuleEntity> rules) {
    try {
      ruleApolloDuplexHandler.publishAuthorityRules(ApolloConfigUtil.getAuthorityDataId(app), rules);
      return true;
    } catch (Exception e) {
      logger.warn("publish authority rule from apollo error: {}", e.getMessage(), e);
      return super.setAuthorityRuleOfMachine(app, ip, port, rules);
    }

  }

}