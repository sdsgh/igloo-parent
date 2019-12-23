package org.iglooproject.jpa.config.spring;

import org.springframework.context.annotation.Import;

/**
 * Configuration JPA qui se construit automatiquement à partir des clés de configuration par défaut
 * (voir {@link DefaultJpaConfig})
 */
@Import({ DefaultJpaConfig.class, JpaApplicationPropertyRegistryConfig.class })
public abstract class AbstractConfiguredJpaConfig extends AbstractJpaConfig {

}
