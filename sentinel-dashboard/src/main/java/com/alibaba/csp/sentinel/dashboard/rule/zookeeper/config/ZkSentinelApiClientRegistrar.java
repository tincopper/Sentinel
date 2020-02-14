package com.alibaba.csp.sentinel.dashboard.rule.zookeeper.config;

import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.RuleZookeeperDuplexHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

import java.beans.Introspector;

/**
 * @author tangzy
 * @since 1.0.0
 */
public class ZkSentinelApiClientRegistrar implements ImportBeanDefinitionRegistrar {

  @Override
  public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata,
      @NonNull BeanDefinitionRegistry registry) {
    String beanName = Introspector.decapitalize(ZkSentinelApiClientPostProcessor.class.getSimpleName());
    BeanDefinition beanDefinition = BeanDefinitionBuilder
        .genericBeanDefinition(ZkSentinelApiClientPostProcessor.class).getBeanDefinition();
    registry.registerBeanDefinition(beanName, beanDefinition);

    String handlerBeanName = Introspector.decapitalize(RuleZookeeperDuplexHandler.class.getSimpleName());
    BeanDefinition handlerBeanDefinition = BeanDefinitionBuilder
            .genericBeanDefinition(RuleZookeeperDuplexHandler.class).getBeanDefinition();
    registry.registerBeanDefinition(handlerBeanName, handlerBeanDefinition);
  }

}
