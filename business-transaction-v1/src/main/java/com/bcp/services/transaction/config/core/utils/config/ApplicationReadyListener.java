package com.bcp.services.transaction.config.core.utils.config;

import com.bcp.services.transaction.config.core.utils.utils.PropertyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * ApplicationReadyListener.
 * This class is used to listen to the application ready event.
 */
@Component
@Slf4j
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {

  @Override
  public void onApplicationEvent(final ApplicationReadyEvent event) {
    log.trace("Configuring properties resolver...");
    final Environment environment = event.getApplicationContext().getEnvironment();
    PropertyUtils.setResolver(environment);
  }

}