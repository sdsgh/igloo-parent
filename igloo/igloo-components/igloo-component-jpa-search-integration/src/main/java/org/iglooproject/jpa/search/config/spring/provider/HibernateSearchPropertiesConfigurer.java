package org.iglooproject.jpa.search.config.spring.provider;

import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.hibernate.search.elasticsearch.cfg.ElasticsearchEnvironment;
import org.hibernate.search.store.impl.FSDirectoryProvider;
import org.hibernate.search.store.impl.RAMDirectoryProvider;
import org.iglooproject.jpa.integration.api.IJpaPropertiesConfigurer;
import org.iglooproject.jpa.search.analyzers.CoreElasticSearchAnalyzersDefinitionProvider;
import org.iglooproject.jpa.search.analyzers.CoreLuceneAnalyzersDefinitionProvider;
import org.springframework.util.StringUtils;

public class HibernateSearchPropertiesConfigurer implements IJpaPropertiesConfigurer {

	private final IHibernateSearchConfigurationProvider configuration;

	public HibernateSearchPropertiesConfigurer(IHibernateSearchConfigurationProvider hibernateSearchConfigurationProvider) {
		this.configuration = hibernateSearchConfigurationProvider;
	}

	@Override
	public void configure(Properties properties) {
		String hibernateSearchIndexBase = configuration.getHibernateSearchIndexBase();
		
		if (configuration.isHibernateSearchElasticSearchEnabled()) {
			properties.setProperty(ElasticsearchEnvironment.ANALYSIS_DEFINITION_PROVIDER, CoreElasticSearchAnalyzersDefinitionProvider.class.getName());
			properties.setProperty("hibernate.search.default.indexmanager", "elasticsearch");
			properties.setProperty("hibernate.search.default.elasticsearch.host", configuration.getElasticSearchHost());
			properties.setProperty("hibernate.search.default.elasticsearch.index_schema_management_strategy", configuration.getElasticSearchIndexSchemaManagementStrategy());
		} else if (StringUtils.hasText(hibernateSearchIndexBase)) {
			properties.setProperty(org.hibernate.search.cfg.Environment.ANALYSIS_DEFINITION_PROVIDER, CoreLuceneAnalyzersDefinitionProvider.class.getName());
			if (configuration.isHibernateSearchIndexInRam()) {
				properties.setProperty("hibernate.search.default.directory_provider", RAMDirectoryProvider.class.getName());
			} else {
				properties.setProperty("hibernate.search.default.directory_provider", FSDirectoryProvider.class.getName());
				properties.setProperty("hibernate.search.default.locking_strategy", "native");
			}
			
			properties.setProperty("hibernate.search.default.indexBase", hibernateSearchIndexBase);
			properties.setProperty("hibernate.search.default.exclusive_index_use", Boolean.TRUE.toString());
			properties.setProperty(org.hibernate.search.cfg.Environment.LUCENE_MATCH_VERSION,
					org.hibernate.search.cfg.Environment.DEFAULT_LUCENE_MATCH_VERSION.toString());
		} else {
			properties.setProperty("hibernate.search.autoregister_listeners", Boolean.FALSE.toString());
		}
		
		Class<? extends Analyzer> hibernateSearchDefaultAnalyzer = configuration.getHibernateSearchDefaultAnalyzer();
		if (hibernateSearchDefaultAnalyzer != null) {
			properties.setProperty(org.hibernate.search.cfg.Environment.ANALYZER_CLASS, hibernateSearchDefaultAnalyzer.getName());
		}
		
		String hibernateSearchIndexingStrategy = configuration.getHibernateSearchIndexingStrategy();
		if (StringUtils.hasText(hibernateSearchIndexingStrategy)) {
			properties.setProperty(org.hibernate.search.cfg.Environment.INDEXING_STRATEGY, hibernateSearchIndexingStrategy);
		}
	}

}
