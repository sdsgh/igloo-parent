package org.iglooproject.jpa.config.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;

import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.cache.ehcache.ConfigSettings;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Environment;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.hibernate.loader.BatchFetchStyle;
import org.iglooproject.jpa.config.spring.provider.IJpaPropertiesProvider;
import org.iglooproject.jpa.hibernate.integrator.spi.MetadataRegistryIntegrator;
import org.iglooproject.jpa.hibernate.jpa.PerTableSequenceStrategyProvider;
import org.iglooproject.jpa.hibernate.model.naming.PostgreSQLPhysicalNamingStrategyImpl;
import org.iglooproject.jpa.integration.api.IJpaPropertiesConfigurer;
import org.springframework.util.StringUtils;

public class JpaHibernatePropertiesConfigurer implements IJpaPropertiesConfigurer {

	public static final int ORDER = 50;

	private final IJpaPropertiesProvider configuration;

	private final Collection<Integrator> integrators;

	public JpaHibernatePropertiesConfigurer(IJpaPropertiesProvider configuration,
			Collection<Integrator> integrators) {
		this.configuration = configuration;
		this.integrators = integrators;
	}

	@Override
	public void configure(Properties properties) {
		properties.setProperty(Environment.DEFAULT_SCHEMA, configuration.getDefaultSchema());
		properties.setProperty(Environment.DIALECT, configuration.getDialect().getName());
		properties.setProperty(Environment.HBM2DDL_AUTO, configuration.getHbm2Ddl());
		properties.setProperty(Environment.SHOW_SQL, Boolean.FALSE.toString());
		properties.setProperty(Environment.FORMAT_SQL, Boolean.FALSE.toString());
		properties.setProperty(Environment.GENERATE_STATISTICS, Boolean.FALSE.toString());
		properties.setProperty(Environment.USE_REFLECTION_OPTIMIZER, Boolean.TRUE.toString());
		properties.setProperty(Environment.CREATE_EMPTY_COMPOSITES_ENABLED,
				Boolean.valueOf(configuration.isCreateEmptyCompositesEnabled()).toString());
		
		properties.setProperty(AvailableSettings.JPAQL_STRICT_COMPLIANCE, Boolean.TRUE.toString());
		
		// this property must be reset by Igloo hibernate search own configurer
		properties.setProperty("hibernate.search.autoregister_listeners", Boolean.FALSE.toString());
		
		configurePerformance(properties);
		configureHbm2Ddl(properties);
		configureSecondLevelCache(properties);
		configureValidation(properties);
		configureNamingPolicies(properties);
		configureIntegrator(properties);
		configureExtraProperties(properties);
	}

	protected void configurePerformance(Properties properties) {
		Integer defaultBatchSize = configuration.getDefaultBatchSize();
		if (defaultBatchSize != null) {
			properties.setProperty(Environment.DEFAULT_BATCH_FETCH_SIZE, Integer.toString(defaultBatchSize));
			properties.setProperty(Environment.BATCH_FETCH_STYLE, BatchFetchStyle.PADDED.name());
		}
	}

	protected void configureHbm2Ddl(Properties properties) {
		String hibernateHbm2DdlImportFiles = configuration.getHbm2DdlImportFiles();
		if (StringUtils.hasText(hibernateHbm2DdlImportFiles)) {
			properties.setProperty(Environment.HBM2DDL_IMPORT_FILES, hibernateHbm2DdlImportFiles);
		}
	}

	protected void configureSecondLevelCache(Properties properties) {
		String ehCacheConfiguration = configuration.getEhCacheConfiguration();
		boolean singletonCache = configuration.isEhCacheSingleton();
		boolean queryCacheEnabled = configuration.isQueryCacheEnabled();
		if (StringUtils.hasText(ehCacheConfiguration)) {
			if (configuration.getEhCacheRegionFactory() != null) {
				properties.setProperty(Environment.CACHE_REGION_FACTORY, configuration.getEhCacheRegionFactory().getName());
			} else {
				// from 5.3.x, hibernate use alias names for hibernate-provided factory
				// classes still supported
				// https://github.com/hibernate/hibernate-orm/commit/f8964847dd40f64e1f478eba47c767be98742125
				if (singletonCache) {
					properties.setProperty(Environment.CACHE_REGION_FACTORY, "ehcache-singleton");
				} else {
					properties.setProperty(Environment.CACHE_REGION_FACTORY, "ehcache");
				}
			}
			properties.setProperty(AvailableSettings.JPA_SHARED_CACHE_MODE, SharedCacheMode.ENABLE_SELECTIVE.name());
			properties.setProperty(ConfigSettings.EHCACHE_CONFIGURATION_RESOURCE_NAME, ehCacheConfiguration);
			properties.setProperty(Environment.USE_SECOND_LEVEL_CACHE, Boolean.TRUE.toString());
			if (queryCacheEnabled) {
				properties.setProperty(Environment.USE_QUERY_CACHE, Boolean.TRUE.toString());
			} else {
				properties.setProperty(Environment.USE_QUERY_CACHE, Boolean.FALSE.toString());
			}
		} else {
			if (queryCacheEnabled) {
				throw new IllegalArgumentException("Could not enable query cache without EhCache configuration");
			}
			properties.setProperty(Environment.USE_SECOND_LEVEL_CACHE, Boolean.FALSE.toString());
			properties.setProperty(Environment.USE_QUERY_CACHE, Boolean.FALSE.toString());
		}
	}

	protected void configureValidation(Properties properties) {
		String validationMode = configuration.getValidationMode();
		if (StringUtils.hasText(validationMode)) {
			properties.setProperty(AvailableSettings.JPA_VALIDATION_MODE, validationMode);
		}
	}

	/**
	 * Configure naming policy related items (sequence name, table name, embedded attributes column name, ...).
	 */
	protected void configureNamingPolicies(Properties properties) {
		// custom generator strategy provider that handles one sequence by entity
		properties.setProperty(org.hibernate.jpa.AvailableSettings.IDENTIFIER_GENERATOR_STRATEGY_PROVIDER,
				PerTableSequenceStrategyProvider.class.getName());
		
		Class<? extends ImplicitNamingStrategy> implicitNamingStrategy = configuration.getImplicitNamingStrategy();
		if (implicitNamingStrategy != null) {
			properties.setProperty(Environment.IMPLICIT_NAMING_STRATEGY, implicitNamingStrategy.getName());
		} else {
			throw new IllegalStateException(Environment.IMPLICIT_NAMING_STRATEGY + " may not be null: sensible values are "
					+ ImplicitNamingStrategyJpaCompliantImpl.class.getName() + " for Igloo <= 0.7 or "
					+ ImplicitNamingStrategyComponentPathImpl.class.getName() + " for Igloo >= 0.8");
		}
		
		Class<? extends PhysicalNamingStrategy> physicalNamingStrategy = configuration.getPhysicalNamingStrategy();
		if (physicalNamingStrategy != null) {
			properties.setProperty(Environment.PHYSICAL_NAMING_STRATEGY, physicalNamingStrategy.getName());
		} else {
			throw new IllegalStateException(Environment.PHYSICAL_NAMING_STRATEGY + " may not be null: sensible values are "
					+ PhysicalNamingStrategyStandardImpl.class.getName() + " for no filtering of the name "
					+ PostgreSQLPhysicalNamingStrategyImpl.class.getName() + " to truncate the name to conform with PostgreSQL identifier max length");
		}
		
		Boolean isNewGeneratorMappingsEnabled = configuration.isNewGeneratorMappingsEnabled();
		if (isNewGeneratorMappingsEnabled != null) {
			properties.setProperty(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, isNewGeneratorMappingsEnabled.toString());
		}
	}

	/**
	 * Inject extra properties from provided {@link Properties} objects.
	 */
	protected void configureExtraProperties(Properties properties) {
		// Override properties
		properties.putAll(configuration.getDefaultExtraProperties());
		properties.putAll(configuration.getExtraProperties());
	}

	/**
	 * Configure IntegratorProvider; used to inject {@link MetadataRegistryIntegrator}.
	 */
	protected void configureIntegrator(Properties properties) {
		final List<Integrator> integratorsSnapshot = new ArrayList<>();
		if (integrators != null) {
			integratorsSnapshot.addAll(integrators);
		}
		IntegratorProvider integratorProvider = () -> integratorsSnapshot;
		properties.put(EntityManagerFactoryBuilderImpl.INTEGRATOR_PROVIDER, integratorProvider);
	}

}
