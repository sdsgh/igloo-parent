package org.iglooproject.jpa.hibernate.analyzers;

import org.hibernate.search.backend.lucene.analysis.model.dsl.LuceneAnalysisDefinitionContainerContext;

public class CoreLuceneClientAnalyzersDefinitionProvider extends CoreLuceneAnalysisConfigurer {

	public static final String ANALYZER_NAME_PREFIX = "ES_";

	@Override
	public void configure(LuceneAnalysisDefinitionContainerContext builder) {
		configureWithPrefix(ANALYZER_NAME_PREFIX, builder);
	}

}
