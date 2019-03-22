package test.specific;

import static org.assertj.core.api.Assertions.assertThat;

import org.igloo.spring.autoconfigure.EnableIglooAutoConfiguration;
import org.igloo.spring.autoconfigure.IglooAutoConfigurationImportSelector;
import org.igloo.spring.autoconfigure.bootstrap.IglooBootstrap3AutoConfiguration;
import org.igloo.spring.autoconfigure.bootstrap.IglooBootstrap4AutoConfiguration;
import org.igloo.spring.autoconfigure.flyway.IglooFlywayAutoConfiguration;
import org.igloo.spring.autoconfigure.jpa.IglooJpaAutoConfiguration;
import org.igloo.spring.autoconfigure.search.IglooHibernateSearchAutoConfiguration;
import org.igloo.spring.autoconfigure.wicket.IglooWicketAutoConfiguration;
import org.iglooproject.spring.property.dao.IMutablePropertyDao;
import org.iglooproject.spring.property.service.IPropertyService;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Joiner;

/**
 * Base class used to check that {@link EnableIglooAutoConfiguration} triggers IglooPropertyAutoConfiguration properly. 
 * 
 * This class uses ApplicationContextRunner to initialize contexts with suitable configurations,
 * which are declared at the bottom of the file.
 *  
 */
public class PropertyAutoConfigurationTestCase {

	/**
	 * Check that autoconfiguration from {@link IPropertyService} is triggered with EnableIglooAutoConfiguration
	 */
	@Test
	public void testIglooPropertyAutoConfigure() {
		new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(TestConfig.class))
			.withPropertyValues(String.format("%s=%s",
					IglooAutoConfigurationImportSelector.PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE,
					IglooBootstrap3AutoConfiguration.class.getName()))
			.run(
				(context) -> { assertThat(context).hasSingleBean(IPropertyService.class); }
			);
	}

	/**
	 * Check that autoconfiguration from {@link IPropertyService} is triggered but {@link IMutablePropertyDao} isn't
	 * when excluding jpa, flyway and hibernate search auto configurations
	 */
	@Test
	public void testIglooPropertyNoJpaAutoConfigure() {
		new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(TestConfig.class))
			.withPropertyValues(String.format("%s=%s",
					IglooAutoConfigurationImportSelector.PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE,
					Joiner.on(",").join(IglooJpaAutoConfiguration.class.getName(),
							IglooFlywayAutoConfiguration.class.getName(),
							IglooHibernateSearchAutoConfiguration.class.getName(),
							IglooBootstrap3AutoConfiguration.class.getName(),
							IglooBootstrap4AutoConfiguration.class.getName(),
							IglooWicketAutoConfiguration.class.getName())))
			.run(
				(context) -> {
					assertThat(context).hasSingleBean(IPropertyService.class);
					assertThat(context).doesNotHaveBean(IMutablePropertyDao.class);
				}
			);
	}

	@Configuration
	@EnableIglooAutoConfiguration
	public static class TestConfig {}

}