package io.hpb.contract.common;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.hpb.contract.filter.ActionFilter;
import io.hpb.contract.interceptor.WebInterceptor;

/**
 * @author Administrator # The default value is /**
 *         spring.mvc.static-path-pattern=classpath:/META-INF/resources/,classpath:/resources/,classpath:/static/
 *         spring.resources.static-locations=Set the path to be pointed to here. Use English commas to separate notes.
 *         spring.mvc.static-path-pattern Only one can be defined, and there is currently no support for multiple comma splitting.
 *         spring.mvc.view.prefix=/static/ spring.mvc.view.suffix=.jsp
 */
@Configuration
public class HpbWebMvcConfigurerAdapter implements WebMvcConfigurer {
	@Bean
	public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
	    return new ByteArrayHttpMessageConverter();
	}
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
	    converters.add(new ByteArrayHttpMessageConverter());
	    converters.add(new MappingJackson2HttpMessageConverter());
	    WebMvcConfigurer.super.configureMessageConverters(converters);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		WebInterceptor webInterceptor = new WebInterceptor();
		registry.addInterceptor(webInterceptor).addPathPatterns("/**").
		excludePathPatterns("/error/**").
		excludePathPatterns("/static/**").
		excludePathPatterns("/error").
		excludePathPatterns("/static");
		WebMvcConfigurer.super.addInterceptors(registry);
	}
	@Bean
	public FilterRegistrationBean<ActionFilter> filterRegistrationBean() {
		FilterRegistrationBean<ActionFilter> registrationBean = new FilterRegistrationBean<ActionFilter>();
		registrationBean.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
		registrationBean.setFilter(new ActionFilter());
		List<String> urlPatterns = new ArrayList<String>();
		urlPatterns.add("/**");
		registrationBean.setUrlPatterns(urlPatterns);
		return registrationBean;
	}
	/**
	 * Configuring static access resources
	 * 
	 * @param registry
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/error/**").addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX + "/error/");
		registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
		registry.addResourceHandler("/static/**").addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX + "/static/")
				/*.setCacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES).cachePrivate())
				.resourceChain(false)
				.addTransformer(new CssLinkResourceTransformer())
				.addResolver(new GzipResourceResolver())
				.addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"))*/
				;
		WebMvcConfigurer.super.addResourceHandlers(registry);
	}
	/**
	 * Manual configuration of static resource paths
	 */
	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(false).setUseTrailingSlashMatch(false);
		WebMvcConfigurer.super.configurePathMatch(configurer);
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**");
		WebMvcConfigurer.super.addCorsMappings(registry);
	}
	@Bean
	public ErrorPageRegistrar errorPageRegistrar(){
	    return new MyErrorPageRegistrar();
	}

	public static class MyErrorPageRegistrar implements ErrorPageRegistrar {
	    @Override
	    public void registerErrorPages(ErrorPageRegistry registry) {
	        registry.addErrorPages(new ErrorPage(HttpStatus.BAD_REQUEST, "/400"));
	    }
	}
	/**
	 * 
	 * @param registry
	 */
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/toLogin").setViewName("login");
		registry.addViewController("/error").setViewName("/error/error.html");
		WebMvcConfigurer.super.addViewControllers(registry);
	}
	
}
