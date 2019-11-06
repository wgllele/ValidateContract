package io.hpb.contract.configure;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
@Configuration
@EnableAsync
public class TaskExecutorConfig implements AsyncConfigurer {

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor taskExecutor=new ThreadPoolTaskExecutor();
		//Thread pool maintains the min number of threads
		taskExecutor.setCorePoolSize(50);
		//Thread pool maintains the max number of threads
		taskExecutor.setMaxPoolSize(2000);
		//Cache queue
		taskExecutor.setQueueCapacity(800);
		taskExecutor.initialize();
		return taskExecutor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return null;
	}
}
