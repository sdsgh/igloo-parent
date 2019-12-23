package org.iglooproject.jpa.security.config.spring;

import org.iglooproject.jpa.config.spring.DefaultJpaConfig;
import org.iglooproject.jpa.config.spring.JpaApplicationPropertyRegistryConfig;
import org.springframework.context.annotation.Import;

@Import({ DefaultJpaConfig.class, JpaApplicationPropertyRegistryConfig.class })
public abstract class AbstractConfiguredJpaSecurityJpaConfig extends AbstractJpaSecurityJpaConfig {

}
