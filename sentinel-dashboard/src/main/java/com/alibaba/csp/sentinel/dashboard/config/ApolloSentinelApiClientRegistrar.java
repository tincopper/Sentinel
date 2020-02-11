package com.alibaba.csp.sentinel.dashboard.config;

import java.beans.Introspector;

import com.alibaba.csp.sentinel.dashboard.rule.apollo.RuleApolloDuplexHandler;
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
    String processorBeanName = Introspector.decapitalize(ApolloSentinelApiClientPostProcessor.class.getSimpleName());
    BeanDefinition processorBeanDefinition = BeanDefinitionBuilder
        .genericBeanDefinition(ApolloSentinelApiClientPostProcessor.class).getBeanDefinition();
    registry.registerBeanDefinition(processorBeanName, processorBeanDefinition);

    String handlerBeanName = Introspector.decapitalize(RuleApolloDuplexHandler.class.getSimpleName());
    BeanDefinition handlerBeanDefinition = BeanDefinitionBuilder
            .genericBeanDefinition(RuleApolloDuplexHandler.class).getBeanDefinition();
    registry.registerBeanDefinition(handlerBeanName, handlerBeanDefinition);
  }

}
