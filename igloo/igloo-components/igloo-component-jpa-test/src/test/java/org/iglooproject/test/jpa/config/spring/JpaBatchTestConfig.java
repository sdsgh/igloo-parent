package org.iglooproject.test.jpa.config.spring;

import org.iglooproject.config.bootstrap.spring.annotations.IglooPropertySourcePriority;
import org.iglooproject.jpa.batch.config.spring.JpaBatchExecutorConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * Added configuration to override some properties
 */
@Configuration
@PropertySource(
	name = IglooPropertySourcePriority.APPLICATION,
	value = "classpath:/igloo-jpa-batch.properties"
)
@Import(JpaBatchExecutorConfig.class)
public class JpaBatchTestConfig {

}
