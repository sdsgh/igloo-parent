package org.iglooproject.jpa.more.config.spring;

import org.iglooproject.jpa.config.spring.DefaultJpaConfig;
import org.iglooproject.jpa.config.spring.JpaApplicationPropertyRegistryConfig;
import org.springframework.context.annotation.Import;

@Import({ DefaultJpaConfig.class, JpaApplicationPropertyRegistryConfig.class })
public abstract class AbstractConfiguredJpaMoreJpaConfig extends AbstractJpaMoreJpaConfig {
	
}
