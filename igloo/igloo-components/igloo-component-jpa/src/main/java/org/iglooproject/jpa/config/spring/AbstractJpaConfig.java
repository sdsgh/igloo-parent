package org.iglooproject.jpa.config.spring;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.internal.scanner.Scanner;
import org.hibernate.integrator.spi.Integrator;
import org.iglooproject.jpa.batch.CoreJpaBatchPackage;
import org.iglooproject.jpa.business.generic.CoreJpaBusinessGenericPackage;
import org.iglooproject.jpa.config.spring.provider.IDatabaseConnectionConfigurationProvider;
import org.iglooproject.jpa.config.spring.provider.IJpaPropertiesProvider;
import org.iglooproject.jpa.config.spring.provider.JpaPackageScanProvider;
import org.iglooproject.jpa.hibernate.integrator.spi.MetadataRegistryIntegrator;
import org.iglooproject.jpa.integration.api.IJpaPropertiesConfigurer;
import org.iglooproject.jpa.migration.IglooMigrationResolver;
import org.iglooproject.jpa.more.config.util.FlywayConfiguration;
import org.iglooproject.jpa.property.FlywayPropertyIds;
import org.iglooproject.jpa.util.CoreJpaUtilPackage;
import org.iglooproject.spring.property.service.IPropertyService;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.google.common.collect.Maps;

/**
 * L'implémentation de cette classe doit être annotée {@link EnableAspectJAutoProxy}
 */
@ComponentScan(
	basePackageClasses = {
		CoreJpaBatchPackage.class,
		CoreJpaBusinessGenericPackage.class,
		CoreJpaUtilPackage.class
	},
	excludeFilters = @Filter(Configuration.class)
)
@Import(FlywayPropertyRegistryConfig.class)
public abstract class AbstractJpaConfig {

	@Autowired
	protected IDatabaseConnectionConfigurationProvider databaseConfigurationProvider;
	
	@Bean
	public MetadataRegistryIntegrator metdataRegistryIntegrator() {
		return new MetadataRegistryIntegrator();
	}

	@Bean(initMethod = "migrate", value = { "flyway", "databaseInitialization" })
	@Profile("flyway")
	public Flyway flyway(DataSource dataSource, FlywayConfiguration flywayConfiguration,
		IPropertyService propertyService, ConfigurableApplicationContext applicationContext) {
		
		FluentConfiguration configuration = Flyway.configure()
			.dataSource(dataSource)
			.schemas(flywayConfiguration.getSchemas())
			.table(flywayConfiguration.getTable())
			.locations(StringUtils.split(flywayConfiguration.getLocations(), ","))
			.baselineOnMigrate(true)
			// difficult to handle this case for the moment; we ignore mismatching checksums
			// TODO allow developers to handle mismatches during their tests.
			.validateOnMigrate(false);
		
		// Placeholders
		Map<String, String> placeholders = Maps.newHashMap();
		for (String property : propertyService.get(FlywayPropertyIds.FLYWAY_PLACEHOLDERS_PROPERTIES)) {
			placeholders.put(property, propertyService.get(FlywayPropertyIds.property(property)));
		}
		configuration.placeholderReplacement(true);
		configuration.placeholders(placeholders);
		
		// Custom Spring-autowiring migration resolver
		Scanner scanner = new Scanner(
			Arrays.asList(configuration.getLocations()),
			configuration.getClassLoader(),
			configuration.getEncoding()
		);
		
		configuration.resolvers(new IglooMigrationResolver(scanner, configuration, applicationContext));
		
		return configuration.load();
	}

	@Bean
	@Profile("flyway")
	public FlywayConfiguration flywayConfiguration() {
		return new FlywayConfiguration();
	}

	/**
	 * Placeholder when flyway is not enabled
	 */
	@Bean(value = { "flyway", "databaseInitialization" })
	@Profile("!flyway")
	public Object notFlyway() {
		return new Object();
	}

	@Bean(name = "hibernateDefaultExtraProperties")
	public PropertiesFactoryBean hibernateDefaultExtraProperties(@Value("${hibernate.defaultExtraPropertiesUrl}") Resource defaultExtraPropertiesUrl) {
		PropertiesFactoryBean f = new PropertiesFactoryBean();
		f.setIgnoreResourceNotFound(false);
		f.setFileEncoding("UTF-8");
		f.setLocations(defaultExtraPropertiesUrl);
		return f;
	}

	@Bean(name = "hibernateExtraProperties")
	public PropertiesFactoryBean hibernateExtraProperties(@Value("${hibernate.extraPropertiesUrl}") Resource extraPropertiesUrl) {
		PropertiesFactoryBean f = new PropertiesFactoryBean();
		f.setIgnoreResourceNotFound(true);
		f.setFileEncoding("UTF-8");
		f.setLocations(extraPropertiesUrl);
		return f;
	}

	/**
	 * Déclaration explicite de close comme destroyMethod (Spring doit la prendre en compte auto-magiquement même
	 * si non configurée).
	 */
	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		return JpaConfigUtils.dataSource(databaseConfigurationProvider);
	}

	@Bean
	public IJpaPropertiesConfigurer jpaPropertiesConfigurer(IJpaPropertiesProvider jpaPropertiesProvider,
			Collection<Integrator> integrators) {
		return new JpaHibernatePropertiesConfigurer(jpaPropertiesProvider, integrators);
	}

	@Bean
	@DependsOn("databaseInitialization")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(
			@Qualifier("dataSource") DataSource dataSource,
			Collection<IJpaPropertiesConfigurer> configurers,
			Collection<JpaPackageScanProvider> jpaPackagesScanProviders,
			@Nullable PersistenceProvider persistenceProvider
			) {
		return JpaConfigUtils.entityManagerFactory(
				dataSource, jpaPackagesScanProviders,
				configurers, persistenceProvider);
	}

	@Bean
	public abstract JpaPackageScanProvider applicationJpaPackageScanProvider();

	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

	@Bean
	public Advisor transactionAdvisor(PlatformTransactionManager transactionManager) {
		return JpaConfigUtils.defaultTransactionAdvisor(transactionManager);
	}

	@Bean
	public JpaPackageScanProvider coreJpaPackageScanProvider() {
		return new JpaPackageScanProvider(CoreJpaBusinessGenericPackage.class.getPackage());
	}

}
