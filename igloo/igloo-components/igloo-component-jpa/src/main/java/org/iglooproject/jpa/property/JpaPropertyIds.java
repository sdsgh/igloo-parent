package org.iglooproject.jpa.property;

import org.iglooproject.jpa.config.spring.provider.DatasourceProvider;
import org.iglooproject.spring.property.model.AbstractPropertyIds;
import org.iglooproject.spring.property.model.ImmutablePropertyId;

public final class JpaPropertyIds extends AbstractPropertyIds {

	/*
	 * Mutable Properties
	 */
	
	
	/*
	 * Immutable Properties
	 */
	public static final ImmutablePropertyId<DatasourceProvider> DB_DATASOURCE_PROVIDER = immutable("db.datasourceProvider");
	public static final ImmutablePropertyId<String> DB_JNDI_NAME = immutable("db.jndiName");
	
	
}
