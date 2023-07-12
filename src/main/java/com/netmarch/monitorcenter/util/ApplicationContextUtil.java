package com.netmarch.monitorcenter.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/** 
* @Description: ApplicationContextUtil spring上下文工具类
* @Author: fengxiang
* @Date: 2018/12/4 13:53
*/ 
@Component
public class ApplicationContextUtil implements ApplicationContextAware {
	private static ConfigurableApplicationContext context;
	private static AutowireCapableBeanFactory autowireCapableBeanFactory;
	private static DefaultListableBeanFactory listableBeanFactory;

	/**
	 * 获取自动注入工厂
	 * @return
	 */
	private static AutowireCapableBeanFactory singleAutowireCapableBeanFactory(){
		if (autowireCapableBeanFactory == null){
			autowireCapableBeanFactory =  context.getAutowireCapableBeanFactory();
		}
		return autowireCapableBeanFactory;
	}

	/**
	 * 获取注册工厂
	 * @return
	 */
	private static DefaultListableBeanFactory singleDefaultListableBeanFactory(){
		if (listableBeanFactory == null){
			listableBeanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
		}
		return listableBeanFactory;
	}

	/**
	 * 注入spring容器上下文
	 * @param context
	 * @throws BeansException
	 */
	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		ApplicationContextUtil.context = (ConfigurableApplicationContext) context;
	}
	
	public static ApplicationContext getContext(){
		return context;
	}
	
	public final static Object getBean(String beanName){
		return context.getBean(beanName);
	}
 
	public final static <T> T getBean(Class<T> requiredType) {
		return context.getBean(requiredType);
	}

	/**
	 * 删除容器里的bean
	 * @param beanName
	 */
	public static void removeBean(@NotNull String beanName){
		try {
			singleDefaultListableBeanFactory().removeBeanDefinition(beanName);
		} catch (NoSuchBeanDefinitionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 注册bean到spring容器
	 * @param beanName
	 * @param bean
	 */
	public static void registerBean(@NotNull String beanName, @NotNull Object bean){
		//将new出的对象放入Spring容器中
		singleDefaultListableBeanFactory().registerSingleton(beanName,bean);
		//bean
		singleAutowireCapableBeanFactory().autowireBean(bean);
	}
	public final static Object getBean(String beanName, Class<?> requiredType) {
		return context.getBean(beanName, requiredType);
	}
}
