package com.alibaba.csp.sentinel.dashboard.client;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.apollo.ApolloConfigUtil;
import com.alibaba.csp.sentinel.dashboard.rule.apollo.RuleApolloDuplexHandler;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      flowRuleEntities = ruleApolloDuplexHandler.getFlowRules(app);
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
      degradeRuleEntities = ruleApolloDuplexHandler.getDegradeRules(app);
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
      systemRuleEntities = ruleApolloDuplexHandler.getSystemRules(app);
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
      authorityRuleEntities = ruleApolloDuplexHandler.getAuthorityRules(app);
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
      List<ParamFlowRuleEntity> paramFlowRuleEntities = ruleApolloDuplexHandler.getParamFlowRules(app);
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
      ruleApolloDuplexHandler.publish(ApolloConfigUtil.getFlowDataId(app), rules);
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
      ruleApolloDuplexHandler.publish(ApolloConfigUtil.getParamFlowDataId(app), rules);
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
      ruleApolloDuplexHandler.publish(ApolloConfigUtil.getDegradeDataId(app), rules);
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
      ruleApolloDuplexHandler.publish(ApolloConfigUtil.getSystemDataId(app), rules);
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
      ruleApolloDuplexHandler.publish(ApolloConfigUtil.getAuthorityDataId(app), rules);
      return true;
    } catch (Exception e) {
      logger.warn("publish authority rule from apollo error: {}", e.getMessage(), e);
      return super.setAuthorityRuleOfMachine(app, ip, port, rules);
    }

  }

}