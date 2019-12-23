package org.iglooproject.jpa.search.config.spring.provider;

import org.apache.lucene.analysis.Analyzer;

public interface IHibernateSearchConfigurationProvider {

	String getHibernateSearchIndexBase();

	Class<? extends Analyzer> getHibernateSearchDefaultAnalyzer();

	String getHibernateSearchIndexingStrategy();
	
	boolean isHibernateSearchIndexInRam();

	boolean isHibernateSearchElasticSearchEnabled();

	String getElasticSearchHost();

	String getElasticSearchIndexSchemaManagementStrategy();

}