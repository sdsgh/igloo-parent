package org.iglooproject.jpa.hibernate.analyzers;

import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.miscellaneous.TrimFilterFactory;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.pattern.PatternReplaceFilterFactory;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurer;
import org.hibernate.search.backend.lucene.analysis.model.dsl.LuceneAnalysisDefinitionContainerContext;
import org.iglooproject.jpa.search.util.HibernateSearchAnalyzer;
import org.iglooproject.jpa.search.util.HibernateSearchNormalizer;
import org.iglooproject.lucene.analysis.french.CoreFrenchMinimalStemFilterFactory;

public class CoreLuceneAnalysisConfigurer implements LuceneAnalysisConfigurer {

	@Override
	public void configure(LuceneAnalysisDefinitionContainerContext context) {
		configureWithPrefix("", context);
	}

	protected void configureWithPrefix(String prefix, LuceneAnalysisDefinitionContainerContext context) {
		context.analyzer(prefix + HibernateSearchAnalyzer.KEYWORD).custom().tokenizer(KeywordTokenizerFactory.class);
		
		context.analyzer(prefix + HibernateSearchAnalyzer.KEYWORD_CLEAN).custom().tokenizer(KeywordTokenizerFactory.class)
			.tokenFilter(ASCIIFoldingFilterFactory.class)
			.tokenFilter(LowerCaseFilterFactory.class);
		
		context.analyzer(prefix + HibernateSearchAnalyzer.TEXT).custom().tokenizer(WhitespaceTokenizerFactory.class)
				.tokenFilter(ASCIIFoldingFilterFactory.class)
				.tokenFilter(WordDelimiterGraphFilterFactory.class)
						.param("generateWordParts", "1")
						.param("generateNumberParts", "1")
						.param("catenateWords", "0")
						.param("catenateNumbers", "0")
						.param("catenateAll", "0")
						.param("splitOnCaseChange", "0")
						.param("splitOnNumerics", "0")
						.param("preserveOriginal", "1")
				.tokenFilter(LowerCaseFilterFactory.class);
		
		context.analyzer(prefix + HibernateSearchAnalyzer.TEXT_STEMMING).custom().tokenizer(WhitespaceTokenizerFactory.class)
				.tokenFilter(ASCIIFoldingFilterFactory.class)
				.tokenFilter(WordDelimiterGraphFilterFactory.class)
						.param("generateWordParts", "1")
						.param("generateNumberParts", "1")
						.param("catenateWords", "0")
						.param("catenateNumbers", "0")
						.param("catenateAll", "0")
						.param("splitOnCaseChange", "0")
						.param("splitOnNumerics", "0")
						.param("preserveOriginal", "1")
				.tokenFilter(LowerCaseFilterFactory.class)
				.tokenFilter(CoreFrenchMinimalStemFilterFactory.class);
		
		context.analyzer(prefix + HibernateSearchAnalyzer.TEXT_SORT).custom().tokenizer(KeywordTokenizerFactory.class)
				.tokenFilter(ASCIIFoldingFilterFactory.class)
				.tokenFilter(LowerCaseFilterFactory.class)
				.tokenFilter(PatternReplaceFilterFactory.class)
						.param("pattern", "('-&\\.,\\(\\))")
						.param("replacement", " ")
						.param("replace", "all")
				.tokenFilter(PatternReplaceFilterFactory.class)
						.param("pattern", "([^0-9\\p{L} ])")
						.param("replacement", "")
						.param("replace", "all")
				.tokenFilter(TrimFilterFactory.class);
		
		context.normalizer(prefix + HibernateSearchNormalizer.KEYWORD);
		
		context.normalizer(prefix + HibernateSearchNormalizer.KEYWORD_CLEAN).custom()
				.tokenFilter(ASCIIFoldingFilterFactory.class)
				.tokenFilter(LowerCaseFilterFactory.class);
		
		context.normalizer(HibernateSearchNormalizer.TEXT).custom()
				.tokenFilter(ASCIIFoldingFilterFactory.class)
				.tokenFilter(LowerCaseFilterFactory.class)
				.tokenFilter(PatternReplaceFilterFactory.class)
						.param("pattern", "('-&\\.,\\(\\))")
						.param("replacement", " ")
						.param("replace", "all")
				.tokenFilter(PatternReplaceFilterFactory.class)
						.param("pattern", "([^0-9\\p{L} ])")
						.param("replacement", "")
						.param("replace", "all")
				.tokenFilter(TrimFilterFactory.class);
		
	}

}
