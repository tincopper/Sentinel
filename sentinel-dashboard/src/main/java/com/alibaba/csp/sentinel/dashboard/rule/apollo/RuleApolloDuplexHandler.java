/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.rule.AbstractRuleDuplexHandler;
import com.alibaba.csp.sentinel.dashboard.rule.apollo.config.ApolloConfig;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.client.exception.ApolloOpenApiException;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;

/**
 * @author tangzy
 */
public class RuleApolloDuplexHandler extends AbstractRuleDuplexHandler {

    private ApolloConfig apolloConfig;
    private ApolloOpenApiClient apolloOpenApiClient;

    public RuleApolloDuplexHandler(final ApolloConfig apolloConfig,
        final ApolloOpenApiClient apolloOpenApiClient) {
        this.apolloConfig = apolloConfig;
        this.apolloOpenApiClient = apolloOpenApiClient;
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
