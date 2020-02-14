package com.alibaba.csp.sentinel.dashboard.rule.zookeeper.config;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.ZkSentinelApiClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.beans.Introspector;

/**
 * 用于将默认的SentinelApiClient进行替换为ZkApiClient的方式处理规则数据
 *
 * @author zy_tang
 */
public class ZkSentinelApiClientPostProcessor implements BeanPostProcessor, ApplicationContextAware {

  private ApplicationContext applicationContext;

  @Override
  public Object postProcessBeforeInitialization(@Nullable Object bean, @Nullable String beanName)
      throws BeansException {
    DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
    String targetBeanName = Introspector.decapitalize(SentinelApiClient.class.getSimpleName());
    if (targetBeanName.equals(beanName)) {
      beanFactory.removeBeanDefinition(targetBeanName);
      BeanDefinition beanDefinition = BeanDefinitionBuilder
          .genericBeanDefinition(ZkSentinelApiClient.class).getBeanDefinition();
      beanFactory.registerBeanDefinition(targetBeanName, beanDefinition);
      return beanFactory.createBean(ZkSentinelApiClient.class);
    }
    return bean;
  }

  @Override
  public void setApplicationContext(@Nullable ApplicationContext applicationContext)
      throws BeansException {
    this.applicationContext = applicationContext;
  }

}
