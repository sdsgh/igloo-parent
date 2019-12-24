package org.igloo.spring.autoconfigure.jpa;

import org.iglooproject.jpa.batch.config.spring.JpaBatchExecutorConfig;
import org.iglooproject.jpa.batch.executor.BatchExecutorCreator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "igloo-ac.batch-executor.disabled", havingValue = "false", matchIfMissing = true)
@ConditionalOnClass({ BatchExecutorCreator.class })
@AutoConfigureAfter(IglooJpaAutoConfiguration.class)
@Import({ JpaBatchExecutorConfig.class })
public class IglooJpaBatchExecutorAutoConfiguration {

}
