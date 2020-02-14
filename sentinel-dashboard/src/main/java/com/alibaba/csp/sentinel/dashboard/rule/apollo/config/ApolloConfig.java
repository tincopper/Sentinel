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
package com.alibaba.csp.sentinel.dashboard.rule.apollo.config;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hantianwei@gmail.com
 * @since 1.5.0
 */
@ConditionalOnProperty(value = "sentinel.datasource.type", havingValue = "apollo")
@Configuration
@EnableApolloPersistence
public class ApolloConfig {

    @Value("${apollo.app.id}")
    private String appId;

    @Value("${apollo.env}")
    private String env;

    @Value("${apollo.cluster.name:default}")
    private String clusterName;

    @Value("${apollo.namespace.name:application}")
    private String namespaceName;

    @Value("${apollo.portal.url}")
    private String portalUrl;

    @Value("${apollo.openapi.token}")
    private String token;

    public String getAppId() {
        return appId;
    }

    public String getEnv() {
        return env;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    @Bean
    public ApolloOpenApiClient apolloOpenApiClient() {
        return ApolloOpenApiClient.newBuilder()
            .withPortalUrl(portalUrl)
            .withToken(token)
            .build();
    }

}
