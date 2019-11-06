package io.hpb.contract.common;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

public class SpringBootContext {
	private static ApplicationContext aplicationContext;

	public static void setAplicationContext(ApplicationContext aplicationContext) {
		SpringBootContext.aplicationContext = aplicationContext;
	}

	public static ApplicationContext getAplicationContext() {
		return aplicationContext;
	}

	public static Object getBean(String name) throws BeansException {
		return aplicationContext.getBean(name);
	}

	public static <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		return aplicationContext.getBean(name, requiredType);
	}

	public static <T> T getBean(Class<T> requiredType) throws BeansException {
		return aplicationContext.getBean(requiredType);
	}

	public static Object getBean(String name, Object... args) throws BeansException {
		return aplicationContext.getBean(name, args);
	}

	public static <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
		return aplicationContext.getBean(requiredType, args);
	};

	public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		return aplicationContext.getType(name);
	}

	public static String[] getAliases(String name) {
		return aplicationContext.getAliases(name);
	}
}
