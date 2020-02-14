package com.alibaba.csp.sentinel.dashboard.rule.zookeeper.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ZkSentinelApiClientRegistrar.class)
public @interface EnableZkPersistence {

}
