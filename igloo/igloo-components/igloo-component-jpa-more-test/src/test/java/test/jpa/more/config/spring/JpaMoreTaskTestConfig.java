package test.jpa.more.config.spring;

import org.iglooproject.jpa.batch.config.spring.JpaBatchExecutorConfig;
import org.iglooproject.spring.config.spring.AbstractApplicationConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
	JpaMoreTestConfig.class,
	JpaBatchExecutorConfig.class
})
public class JpaMoreTaskTestConfig extends AbstractApplicationConfig {

}
