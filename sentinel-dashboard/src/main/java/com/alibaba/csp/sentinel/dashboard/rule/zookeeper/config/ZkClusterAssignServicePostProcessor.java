package com.alibaba.csp.sentinel.dashboard.rule.zookeeper.config;

import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.ZkClusterAssignServiceImpl;
import com.alibaba.csp.sentinel.dashboard.service.ClusterAssignServiceImpl;
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
 *  
 *
 * @author tomgs
 * @version 2020/2/24 1.0 
 */
public class ZkClusterAssignServicePostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(@Nullable Object bean, @Nullable String beanName) throws BeansException {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        String targetBeanName = Introspector.decapitalize(ClusterAssignServiceImpl.class.getSimpleName());
        if (targetBeanName.equals(beanName)) {
            beanFactory.removeBeanDefinition(targetBeanName);
            BeanDefinition beanDefinition = BeanDefinitionBuilder
                    .genericBeanDefinition(ZkClusterAssignServiceImpl.class).getBeanDefinition();
            beanFactory.registerBeanDefinition(targetBeanName, beanDefinition);
            return beanFactory.createBean(ZkClusterAssignServiceImpl.class);
        }
        return bean;
    }

    @Override
    public void setApplicationContext(@Nullable ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }
}
