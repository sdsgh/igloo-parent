package org.iglooproject.jpa.config.spring;

import static org.iglooproject.jpa.property.JpaPropertyIds.DB_DATASOURCE_PROVIDER;
import static org.iglooproject.jpa.property.JpaPropertyIds.DB_JNDI_NAME;

import org.iglooproject.jpa.config.spring.provider.DatasourceProvider;
import org.iglooproject.spring.config.spring.AbstractApplicationPropertyRegistryConfig;
import org.iglooproject.spring.property.service.IPropertyRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaApplicationPropertyRegistryConfig extends AbstractApplicationPropertyRegistryConfig {

	@Override
	public void register(IPropertyRegistry registry) {
		registry.registerEnum(DB_DATASOURCE_PROVIDER, DatasourceProvider.class);
		registry.registerString(DB_JNDI_NAME);
	}

}
