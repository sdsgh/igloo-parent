package fr.openwide.core.jpa.more.config.spring;

import org.springframework.context.annotation.Bean;

import fr.openwide.core.jpa.more.business.property.dao.IImmutablePropertyDao;
import fr.openwide.core.jpa.more.business.property.dao.ImmutablePropertyDaoImpl;
import fr.openwide.core.jpa.more.business.property.service.IConfigurablePropertyService;
import fr.openwide.core.jpa.more.business.property.service.PropertyServiceImpl;

public abstract class AbstractApplicationPropertyConfig {

	@Bean
	public IImmutablePropertyDao immutablePropertyDao() {
		return new ImmutablePropertyDaoImpl();
	}

	@Bean
	public IConfigurablePropertyService propertyService() {
		return new PropertyServiceImpl();
	}

}
