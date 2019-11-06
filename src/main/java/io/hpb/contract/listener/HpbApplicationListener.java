package io.hpb.contract.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
public class HpbApplicationListener implements ApplicationListener<ApplicationReadyEvent> {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void onApplicationEvent(ApplicationReadyEvent cre) {
		logger.info("调用onApplicationEvent");
	}
}