package org.iglooproject.jpa.search.config.spring;

import static org.iglooproject.jpa.search.property.JpaHibernateSearchPropertyIds.HIBERNATE_SEARCH_ELASTICSEARCH_ENABLED;
import static org.iglooproject.jpa.search.property.JpaHibernateSearchPropertyIds.HIBERNATE_SEARCH_REINDEX_BATCH_SIZE;
import static org.iglooproject.jpa.search.property.JpaHibernateSearchPropertyIds.HIBERNATE_SEARCH_REINDEX_LOAD_THREADS;
import static org.iglooproject.jpa.search.property.JpaHibernateSearchPropertyIds.LUCENE_BOOLEAN_QUERY_MAX_CLAUSE_COUNT;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

import org.apache.lucene.search.BooleanQuery;
import org.iglooproject.jpa.search.analyzers.LuceneEmbeddedAnalyzerRegistry;
import org.iglooproject.jpa.search.config.spring.provider.DefaultHibernateSearchConfigurationProvider;
import org.iglooproject.jpa.search.config.spring.provider.HibernateSearchPropertiesConfigurer;
import org.iglooproject.jpa.search.config.spring.provider.IHibernateSearchConfigurationProvider;
import org.iglooproject.jpa.search.dao.HibernateSearchDaoImpl;
import org.iglooproject.jpa.search.dao.IHibernateSearchDao;
import org.iglooproject.jpa.search.service.HibernateSearchServiceImpl;
import org.iglooproject.jpa.search.service.IHibernateSearchService;
import org.iglooproject.spring.property.service.IPropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(HibernateSearchPropertyRegistryConfig.class)
public class HibernateSearchConfig {
	
	@Autowired
	private IPropertyService propertyService;
	
	@PostConstruct
	public void init() {
		BooleanQuery.setMaxClauseCount(propertyService.get(LUCENE_BOOLEAN_QUERY_MAX_CLAUSE_COUNT));
	}

	@Bean
	public IHibernateSearchDao hibernateSearchDao(IPropertyService propertyService,
			@Nullable LuceneEmbeddedAnalyzerRegistry luceneEmbeddedAnalyzerRegistry) {
		return new HibernateSearchDaoImpl(
				propertyService.get(HIBERNATE_SEARCH_ELASTICSEARCH_ENABLED),
				() -> propertyService.get(HIBERNATE_SEARCH_REINDEX_BATCH_SIZE),
				() -> propertyService.get(HIBERNATE_SEARCH_REINDEX_LOAD_THREADS),
				luceneEmbeddedAnalyzerRegistry);
	}

	@Bean
	public IHibernateSearchService hibernateService(IHibernateSearchDao hibernateSearchDao) {
		return new HibernateSearchServiceImpl(hibernateSearchDao);
	}

	@Bean
	public IHibernateSearchConfigurationProvider hibernateSearchConfigurationProvider() {
		return new DefaultHibernateSearchConfigurationProvider();
	}

	@Bean
	public HibernateSearchPropertiesConfigurer hibernateSearchPropertiesConfigurer(IHibernateSearchConfigurationProvider hibernateSearchConfigurationProvider) {
		return new HibernateSearchPropertiesConfigurer(hibernateSearchConfigurationProvider);
	}

}
