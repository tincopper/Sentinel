package com.alibaba.csp.sentinel.dashboard.config;

import java.beans.Introspector;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

/**
 * @author tangzy
 * @since 1.0.0
 */
public class ApolloSentinelApiClientRegistrar implements ImportBeanDefinitionRegistrar {

  @Override
  public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata,
      @NonNull BeanDefinitionRegistry registry) {
    String beanName = Introspector.decapitalize(SentinelApiClientPostProcessor.class.getSimpleName());
    BeanDefinition beanDefinition = BeanDefinitionBuilder
        .genericBeanDefinition(SentinelApiClientPostProcessor.class).getBeanDefinition();
    registry.registerBeanDefinition(beanName, beanDefinition);
  }

}
