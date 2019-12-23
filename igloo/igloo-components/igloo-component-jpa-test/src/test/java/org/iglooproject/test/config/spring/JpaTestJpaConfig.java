package org.iglooproject.test.config.spring;

import org.iglooproject.jpa.config.spring.AbstractConfiguredJpaConfig;
import org.iglooproject.jpa.config.spring.provider.JpaPackageScanProvider;
import org.iglooproject.jpa.search.config.spring.HibernateSearchConfig;
import org.iglooproject.test.business.JpaTestBusinessPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAspectJAutoProxy
@Import(HibernateSearchConfig.class)
public class JpaTestJpaConfig extends AbstractConfiguredJpaConfig {

	@Override
	public JpaPackageScanProvider applicationJpaPackageScanProvider() {
		return new JpaPackageScanProvider(JpaTestBusinessPackage.class.getPackage());
	}

}
