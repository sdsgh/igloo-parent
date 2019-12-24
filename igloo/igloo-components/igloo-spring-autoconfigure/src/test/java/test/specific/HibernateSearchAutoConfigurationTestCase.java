package test.specific;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Condition;
import org.igloo.spring.autoconfigure.EnableIglooAutoConfiguration;
import org.igloo.spring.autoconfigure.applicationconfig.IglooApplicationConfigAutoConfiguration;
import org.igloo.spring.autoconfigure.bootstrap.IglooBootstrap3AutoConfiguration;
import org.igloo.spring.autoconfigure.bootstrap.IglooBootstrap4AutoConfiguration;
import org.igloo.spring.autoconfigure.security.IglooJpaSecurityAutoConfiguration;
import org.igloo.spring.autoconfigure.wicket.IglooWicketAutoConfiguration;
import org.iglooproject.jpa.batch.api.IBatchExecutorListener;
import org.iglooproject.jpa.batch.executor.BatchExecutorCreator;
import org.iglooproject.jpa.search.config.spring.HibernateSearchBatchExecutorConfig;
import org.iglooproject.jpa.search.service.IHibernateSearchService;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/**
 * Base class used to check that {@link EnableIglooAutoConfiguration} triggers IglooJpaAutoConfiguration properly. 
 * 
 * This class uses ApplicationContextRunner to initialize contexts with suitable configurations,
 * which are declared at the bottom of the file.
 *  
 */
public class HibernateSearchAutoConfigurationTestCase {

	/**
	 * Check that autoconfiguration from {@link IHibernateSearchService} is triggered with EnableIglooAutoConfiguration
	 */
	@Test
	public void testIglooHibernateSearchAutoConfigure() {
		new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(TestConfig.class))
			.run(
				(context) -> {
					assertThat(context).hasSingleBean(IHibernateSearchService.class);
					// by default, batch executor module and hibernate-search related items are present
					assertThat(context).hasSingleBean(BatchExecutorCreator.class);
					assertThat(context).getBeans(IBatchExecutorListener.class).hasValueSatisfying(
							new Condition<>(
									HibernateSearchBatchExecutorConfig.HibernateSearchBatchExecutorListener.class::isInstance,
									"Hibernate Search implementation for IBatchExecutorListener must be present in beans"
							)
					);
				}
			);
	}

	/**
	 * Check that autoconfiguration from {@link IHibernateSearchService} is triggered with EnableIglooAutoConfiguration
	 * and that BatchExecutor module can be ignored.
	 */
	@Test
	public void testIglooHibernateSearchBatchLessAutoConfigure() {
		new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(TestConfig.class))
			.withPropertyValues("igloo-ac.batch-executor.disabled", "true")
			.run(
				(context) -> {
					assertThat(context).hasSingleBean(IHibernateSearchService.class);
					assertThat(context).doesNotHaveBean(BatchExecutorCreator.class);
					assertThat(context).doesNotHaveBean(HibernateSearchBatchExecutorConfig.HibernateSearchBatchExecutorListener.class);
				}
			);
	}
	
	@Configuration
	@EnableIglooAutoConfiguration(exclude = {
			IglooBootstrap3AutoConfiguration.class,
			IglooBootstrap4AutoConfiguration.class,
			IglooWicketAutoConfiguration.class,
			IglooJpaSecurityAutoConfiguration.class,
			IglooApplicationConfigAutoConfiguration.class
	})
	public static class TestConfig {}

}
