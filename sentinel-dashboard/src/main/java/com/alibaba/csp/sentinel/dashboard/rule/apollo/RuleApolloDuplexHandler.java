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

import com.alibaba.csp.sentinel.dashboard.config.ApolloConfig;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.fastjson.JSON;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * @author tangzy
 */
public class RuleApolloDuplexHandler extends AbstractRuleApolloDuplexHandler {

    public RuleApolloDuplexHandler(ApolloConfig apolloConfig,
        ApolloOpenApiClient apolloOpenApiClient) {
        super(apolloConfig, apolloOpenApiClient);
    }

    public List<FlowRuleEntity> getFlowRules(String appName) throws Exception {
        return super.getRules(ApolloConfigUtil.getFlowDataId(appName),
            source -> JSON.parseArray(source, FlowRuleEntity.class));
    }

    public List<DegradeRuleEntity> getDegradeRules(String appName) throws Exception {
        return super.getRules(ApolloConfigUtil.getDegradeDataId(appName),
            source -> JSON.parseArray(source, DegradeRuleEntity.class));
    }

    public List<AuthorityRuleEntity> getAuthorityRules(String appName) throws Exception {
        return super.getRules(ApolloConfigUtil.getAuthorityDataId(appName),
            source -> JSON.parseArray(source, AuthorityRuleEntity.class));
    }

    public List<ParamFlowRuleEntity> getParamFlowRules(String appName) throws Exception {
        return super.getRules(ApolloConfigUtil.getParamFlowDataId(appName),
            source -> JSON.parseArray(source, ParamFlowRuleEntity.class));
    }

    public List<SystemRuleEntity> getSystemRules(String appName) throws Exception {
        return super.getRules(ApolloConfigUtil.getSystemDataId(appName),
            source -> JSON.parseArray(source, SystemRuleEntity.class));
    }

}
