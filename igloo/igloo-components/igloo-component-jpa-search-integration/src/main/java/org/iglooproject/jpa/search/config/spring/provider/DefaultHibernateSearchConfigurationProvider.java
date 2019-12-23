package org.iglooproject.jpa.search.config.spring.provider;

import org.apache.lucene.analysis.Analyzer;
import org.springframework.beans.factory.annotation.Value;

public class DefaultHibernateSearchConfigurationProvider implements IHibernateSearchConfigurationProvider {

	@Value("${lucene.index.path}")
	private String hibernateSearchIndexBase;
	
	@Value("${lucene.index.inRam:false}")
	private boolean isHibernateSearchIndexInRam;
	
	@Value("${hibernate.search.analyzer:}") // Defaults to null
	private Class<? extends Analyzer> hibernateSearchDefaultAnalyzer;
	
	@Value("${hibernate.search.indexing_strategy:}") // Defaults to an empty string
	private String hibernateSearchIndexingStrategy;

	@Value("${hibernate.search.elasticsearch.enabled:false}")
	private boolean isHibernateSearchElasticSearchEnabled;
	
	@Value("${hibernate.search.default.elasticsearch.host}")
	private String elasticSearchHost;

	@Value("${hibernate.search.default.elasticsearch.index_schema_management_strategy}")
	private String elasticSearchIndexSchemaManagementStrategy;

	@Override
	public String getHibernateSearchIndexBase() {
		return hibernateSearchIndexBase;
	}
	
	@Override
	public boolean isHibernateSearchIndexInRam() {
		return isHibernateSearchIndexInRam;
	}

	@Override
	public Class<? extends Analyzer> getHibernateSearchDefaultAnalyzer() {
		return hibernateSearchDefaultAnalyzer;
	}
	
	@Override
	public String getHibernateSearchIndexingStrategy() {
		return hibernateSearchIndexingStrategy;
	}

	@Override
	public boolean isHibernateSearchElasticSearchEnabled() {
		return isHibernateSearchElasticSearchEnabled;
	}

	@Override
	public String getElasticSearchHost() {
		return elasticSearchHost;
	}

	@Override
	public String getElasticSearchIndexSchemaManagementStrategy() {
		return elasticSearchIndexSchemaManagementStrategy;
	}

}
