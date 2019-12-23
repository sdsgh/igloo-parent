package org.iglooproject.jpa.search.analyzers;

import org.apache.lucene.analysis.Analyzer;

public interface LuceneEmbeddedAnalyzerRegistry {

	Analyzer getAnalyzer(String analyzerName);

}