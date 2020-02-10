package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.config.ApolloConfig;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.client.exception.ApolloOpenApiException;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;

/**
 * 从apollo中获取规则和发布规则配置双向处理支持
 *
 * @author tangzy
 */
public abstract class AbstractRuleApolloDuplexHandler implements DynamicRuleProvider<String>, DynamicRulePublisher<Object> {

  private ApolloConfig apolloConfig;
  private ApolloOpenApiClient apolloOpenApiClient;

  public AbstractRuleApolloDuplexHandler(ApolloConfig apolloConfig,
      ApolloOpenApiClient apolloOpenApiClient) {
    this.apolloConfig = apolloConfig;
    this.apolloOpenApiClient = apolloOpenApiClient;
  }

  public <T> T getRules(String appName, Converter<String, T> converter) throws Exception {
    String rules = getRules(appName);
    if (StringUtil.isEmpty(rules)) {
      return null;
    }
    return converter.convert(rules);
  }

  @Override
  public String getRules(String appName) throws Exception {
    OpenNamespaceDTO openNamespaceDTO = apolloOpenApiClient.getNamespace(
        apolloConfig.getAppId(), apolloConfig.getEnv(), apolloConfig.getClusterName(), apolloConfig.getNamespaceName());
    return openNamespaceDTO
        .getItems()
        .stream()
        .filter(p -> p.getKey().equals(appName))
        .map(OpenItemDTO::getValue)
        .findFirst()
        .orElse("");
  }

  @Override
  public void publish(String app, Object rules) throws Exception {
    AssertUtil.notEmpty(app, "app name cannot be empty");
    if (rules == null) {
      return;
    }
    // Increase the configuration
    //String flowDataId = ApolloConfigUtil.getFlowDataId(app);
    OpenItemDTO openItemDTO = new OpenItemDTO();
    openItemDTO.setKey(app);
    openItemDTO.setValue(JSON.toJSONString(rules));
    openItemDTO.setComment("Program auto-join");
    openItemDTO.setDataChangeCreatedBy("apollo");

    //apolloOpenApiClient.createOrUpdateItem(appId, "DEV", "default", "application", openItemDTO);
    createOrUpdateItem(apolloConfig.getAppId(), apolloConfig.getEnv(),
        apolloConfig.getClusterName(), apolloConfig.getNamespaceName(), openItemDTO);

    // Release configuration
    NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
    namespaceReleaseDTO.setEmergencyPublish(true);
    namespaceReleaseDTO.setReleaseComment("Modify or add configurations");
    namespaceReleaseDTO.setReleasedBy("apollo");
    namespaceReleaseDTO.setReleaseTitle("Modify or add configurations");
    apolloOpenApiClient.publishNamespace(apolloConfig.getAppId(), apolloConfig.getEnv(),
        apolloConfig.getClusterName(), apolloConfig.getNamespaceName(), namespaceReleaseDTO);
  }

  private void createOrUpdateItem(String appId, String env, String clusterName, String namespace, OpenItemDTO openItemDTO) {
    // 兼容低版本
    ApolloOpenApiException apolloOpenApiException = null;
    try {
      apolloOpenApiClient.createOrUpdateItem(appId, env, clusterName, namespace, openItemDTO);
    } catch (Exception e) {
      if (!(e.getCause() instanceof ApolloOpenApiException)) {
        throw e;
      }
      apolloOpenApiException = (ApolloOpenApiException) e.getCause();
    }
    if (apolloOpenApiException == null) {
      return;
    }
    if (404 != apolloOpenApiException.getStatus()) {
      throw apolloOpenApiException;
    }
    apolloOpenApiClient.createItem(appId, env, clusterName, namespace, openItemDTO);
  }

}
