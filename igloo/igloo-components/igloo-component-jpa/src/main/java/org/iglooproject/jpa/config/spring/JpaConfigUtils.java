package org.iglooproject.jpa.config.spring;

import static com.google.common.base.Strings.emptyToNull;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.naming.NamingException;
import javax.persistence.spi.PersistenceProvider;
import javax.sql.DataSource;

import org.hibernate.Interceptor;
import org.hibernate.cfg.AvailableSettings;
import org.iglooproject.jpa.business.generic.service.ITransactionalAspectAwareService;
import org.iglooproject.jpa.config.spring.provider.IDatabaseConnectionConfigurationProvider;
import org.iglooproject.jpa.config.spring.provider.IDatabaseConnectionJndiConfigurationProvider;
import org.iglooproject.jpa.config.spring.provider.IDatabaseConnectionPoolConfigurationProvider;
import org.iglooproject.jpa.config.spring.provider.JpaPackageScanProvider;
import org.iglooproject.jpa.exception.ServiceException;
import org.iglooproject.jpa.hibernate.interceptor.AbstractChainableInterceptor;
import org.iglooproject.jpa.hibernate.interceptor.ChainedInterceptor;
import org.iglooproject.jpa.integration.api.IJpaPropertiesConfigurer;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;

public final class JpaConfigUtils {

	private JpaConfigUtils() {}

	/**
	 * Construit un {@link LocalContainerEntityManagerFactoryBean} à partir d'un ensemble d'options usuelles.
	 */
	public static LocalContainerEntityManagerFactoryBean entityManagerFactory(
			DataSource dataSource,
			Collection<JpaPackageScanProvider> jpaPackagesScanProviders,
			List<IJpaPropertiesConfigurer> configurers,
			PersistenceProvider persistenceProvider,
			List<Interceptor> interceptors) {
		Properties properties = new Properties();
		for (IJpaPropertiesConfigurer propertiesConfigurer : configurers) {
			propertiesConfigurer.configure(properties);
		}
		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		
		entityManagerFactoryBean.setJpaProperties(properties);
		entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		entityManagerFactoryBean.setDataSource(dataSource);
		entityManagerFactoryBean.setPackagesToScan(getPackagesToScan(jpaPackagesScanProviders));
		
		if (persistenceProvider != null) {
			entityManagerFactoryBean.setPersistenceProvider(persistenceProvider);
		}
		
		if (interceptors != null && ! interceptors.isEmpty()) {
			Interceptor firstInterceptor = interceptors.get(0);
			// only AbstractChainableInterceptor can be combined in a ChainedInterceptor
			if (interceptors.size() > 1) {
				// if we have only one item, it does not need to be wrapped
				properties.put(AvailableSettings.INTERCEPTOR, firstInterceptor);
			} else {
				// if we have multiple items, all must be an AbstractChainableInterceptor
				ChainedInterceptor interceptor = new ChainedInterceptor();
				try {
					for (Interceptor i : interceptors) {
						interceptor.add((AbstractChainableInterceptor) i);
					}
				} catch (ClassCastException e) {
					throw new IllegalStateException(String.format(
							"Multiple interceptor only supports AbstractChainableInterceptor ; provided [%s]",
							interceptors.stream().map(Object::toString).collect(Collectors.joining(", "))));
				}
			}
		}
		
		return entityManagerFactoryBean;
	}

	public static Advisor defaultTransactionAdvisor(PlatformTransactionManager transactionManager) {
		return defaultTransactionAdvisor(transactionManager, Lists.<Class<? extends Exception>>newArrayList());
	}

	public static Advisor defaultTransactionAdvisor(PlatformTransactionManager transactionManager,
			List<Class<? extends Exception>> additionalRollbackRuleExceptions) {
		AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
		
		advisor.setExpression("this(" + ITransactionalAspectAwareService.class.getName() + ")");
		advisor.setAdvice(defaultTransactionInterceptor(transactionManager, additionalRollbackRuleExceptions));
		
		return advisor;
		
	}

	/**
	 * Construit un transactionInterceptor avec une configuration par défaut.
	 */
	public static TransactionInterceptor defaultTransactionInterceptor(PlatformTransactionManager transactionManager,
			List<Class<? extends Exception>> additionalRollbackRuleExceptions) {
		TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
		Properties transactionAttributes = new Properties();
		
		List<RollbackRuleAttribute> rollbackRules = Lists.newArrayList();
		rollbackRules.add(new RollbackRuleAttribute(ServiceException.class));
		// TODO voir si on ajoute SecurityServiceException.class en fonction de ce que ça donne sur le Wombat
		// ou voir si on ne la dégage pas carrément en fait...
		
		for (Class<? extends Exception> clazz : additionalRollbackRuleExceptions) {
			rollbackRules.add(new RollbackRuleAttribute(clazz));
		}
		
		DefaultTransactionAttribute readOnlyTransactionAttributes =
				new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRED);
		readOnlyTransactionAttributes.setReadOnly(true);
		
		RuleBasedTransactionAttribute writeTransactionAttributes =
				new RuleBasedTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRED, rollbackRules);
		
		String readOnlyTransactionAttributesDefinition = readOnlyTransactionAttributes.toString();
		String writeTransactionAttributesDefinition = writeTransactionAttributes.toString();
		// read-only
		transactionAttributes.setProperty("is*", readOnlyTransactionAttributesDefinition);
		transactionAttributes.setProperty("has*", readOnlyTransactionAttributesDefinition);
		transactionAttributes.setProperty("get*", readOnlyTransactionAttributesDefinition);
		transactionAttributes.setProperty("list*", readOnlyTransactionAttributesDefinition);
		transactionAttributes.setProperty("search*", readOnlyTransactionAttributesDefinition);
		transactionAttributes.setProperty("find*", readOnlyTransactionAttributesDefinition);
		transactionAttributes.setProperty("count*", readOnlyTransactionAttributesDefinition);
		// write et rollback-rule
		transactionAttributes.setProperty("*", writeTransactionAttributesDefinition);
		
		transactionInterceptor.setTransactionAttributes(transactionAttributes);
		transactionInterceptor.setTransactionManager(transactionManager);
		return transactionInterceptor;
	}

	public static DataSource dataSource(IDatabaseConnectionConfigurationProvider configurationProvider) {
		if (configurationProvider instanceof IDatabaseConnectionJndiConfigurationProvider) {
			String jndiName = ((IDatabaseConnectionJndiConfigurationProvider) configurationProvider).getJndiName();
			JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
			bean.setJndiName(jndiName);
			bean.setExpectedType(DataSource.class);
			try {
				bean.afterPropertiesSet();
			} catch (IllegalArgumentException | NamingException e) {
				throw new IllegalStateException(String.format("Error during jndi lookup for %s resource",
						jndiName), e);
			}
			return (DataSource) bean.getObject();
		} else if (configurationProvider instanceof IDatabaseConnectionPoolConfigurationProvider) {
			IDatabaseConnectionPoolConfigurationProvider provider =
					(IDatabaseConnectionPoolConfigurationProvider) configurationProvider;
			HikariDataSource dataSource = new HikariDataSource();
			
			dataSource.setDriverClassName(configurationProvider.getDriverClass().getName());
			dataSource.setJdbcUrl(provider.getUrl());
			dataSource.setUsername(provider.getUser());
			dataSource.setPassword(provider.getPassword());
			dataSource.addDataSourceProperty("user", provider.getUser());
			dataSource.addDataSourceProperty("password", provider.getPassword());
			dataSource.setMinimumIdle(provider.getMinIdle());
			dataSource.setMaximumPoolSize(provider.getMaxPoolSize());
			dataSource.setConnectionTestQuery(provider.getValidationQuery());
			dataSource.setConnectionInitSql(emptyToNull(provider.getInitSql()));
			
			return dataSource;
		} else {
			throw new IllegalStateException(String.format("JDBC pool : %s type not handled",
					configurationProvider.getClass().getName()));
		}
	}

	private static String[] getPackagesToScan(Collection<JpaPackageScanProvider> jpaPackageScanProviders) {
		return jpaPackageScanProviders.stream()
				.map(JpaPackageScanProvider::getPackages)
				.flatMap(Collection::stream) // stream<list<package>> -> stream<package>
				.map(Package::getName)
				.toArray(String[]::new);
	}

}
