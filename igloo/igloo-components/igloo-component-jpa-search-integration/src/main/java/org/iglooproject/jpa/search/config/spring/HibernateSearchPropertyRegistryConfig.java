package org.iglooproject.jpa.search.config.spring;

import static org.iglooproject.jpa.search.property.JpaHibernateSearchPropertyIds.HIBERNATE_SEARCH_ELASTICSEARCH_ENABLED;
import static org.iglooproject.jpa.search.property.JpaHibernateSearchPropertyIds.HIBERNATE_SEARCH_ELASTICSEARCH_HOST;
import static org.iglooproject.jpa.search.property.JpaHibernateSearchPropertyIds.HIBERNATE_SEARCH_REINDEX_BATCH_SIZE;
import static org.iglooproject.jpa.search.property.JpaHibernateSearchPropertyIds.HIBERNATE_SEARCH_REINDEX_LOAD_THREADS;
import static org.iglooproject.jpa.search.property.JpaHibernateSearchPropertyIds.LUCENE_BOOLEAN_QUERY_MAX_CLAUSE_COUNT;

import org.apache.lucene.search.BooleanQuery;
import org.iglooproject.functional.Functions2;
import org.iglooproject.spring.config.spring.AbstractApplicationPropertyRegistryConfig;
import org.iglooproject.spring.property.service.IPropertyRegistry;
import org.springframework.context.annotation.Configuration;

import com.google.common.primitives.Ints;

@Configuration
public class HibernateSearchPropertyRegistryConfig extends AbstractApplicationPropertyRegistryConfig {

	@Override
	public void register(IPropertyRegistry registry) {
		registry.<Integer>register(
				LUCENE_BOOLEAN_QUERY_MAX_CLAUSE_COUNT,
				Functions2.from(Ints.stringConverter()),
				BooleanQuery::getMaxClauseCount
		);
		
		registry.registerInteger(HIBERNATE_SEARCH_REINDEX_BATCH_SIZE, 25);
		registry.registerInteger(HIBERNATE_SEARCH_REINDEX_LOAD_THREADS, 8);
		registry.registerBoolean(HIBERNATE_SEARCH_ELASTICSEARCH_ENABLED, false);
		registry.registerString(HIBERNATE_SEARCH_ELASTICSEARCH_HOST, "http://" + "127.0.0.1" + ":9220");
	}

}
