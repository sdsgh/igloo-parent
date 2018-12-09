package org.iglooproject.jpa.hibernate.analyzers;

import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurer;
import org.hibernate.search.backend.elasticsearch.analysis.model.dsl.ElasticsearchAnalysisDefinitionContainerContext;
import org.iglooproject.jpa.search.util.HibernateSearchAnalyzer;
import org.iglooproject.jpa.search.util.HibernateSearchNormalizer;

public class CoreElasticSearchAnalysisConfigurer implements ElasticsearchAnalysisConfigurer {

	private static final String KEYWORDTOKENIZER = "KeywordTokenizer";
	private static final String WHITESPACETOKENIZER = "WhitespaceTokenizer";
	
	private static final String ASCIIFOLDINGFILTER = "ASCIIFoldingFilter";
	private static final String LOWERCASEFILTER= "LowerCaseFilter";
	private static final String WORDDELIMITERFILTER = "WordDelimiterFilter";
	private static final String ELASTICSEARCHTOKENFILTER = "CoreFrenchMinimalStem";
	private static final String PATTERNREPLACEFILTERPUNCTUATION = "text_sort_replace_punctuations";
	private static final String PATTERNREPLACEFILTERNUMBER = "text_sort_replace_numbers";
	private static final String TRIMFILTER = "TrimFilter";

	@Override
	public void configure(ElasticsearchAnalysisDefinitionContainerContext context) {
		context.tokenizer(KEYWORDTOKENIZER).type("keyword");
		context.tokenizer(WHITESPACETOKENIZER).type("whitespace");
		
		context.tokenFilter(ASCIIFOLDINGFILTER).type("asciifolding");
		context.tokenFilter(LOWERCASEFILTER).type("lowercase");
		context.tokenFilter(WORDDELIMITERFILTER).type("word_delimiter")
				.param("generateWordParts", "1")
				.param("generateNumberParts", "1")
				.param("catenateWords", "0")
				.param("catenateNumbers", "0")
				.param("catenateAll", "0")
				.param("splitOnCaseChange", "0")
				.param("splitOnNumerics", "0")
				.param("preserveOriginal", "1");
		context.tokenFilter(ELASTICSEARCHTOKENFILTER).type("corefrenchminimalstem");
		context.tokenFilter(PATTERNREPLACEFILTERPUNCTUATION).type("pattern_replace")
				.param("pattern", "('-&\\.,\\(\\))")
				.param("replacement", " ")
				.param("replace", "all");
		context.tokenFilter(PATTERNREPLACEFILTERNUMBER).type("pattern_replace")
				.param("pattern", "([^0-9\\p{L} ])")
				.param("replacement", "")
				.param("replace", "all");
		context.tokenFilter(TRIMFILTER).type("trim");
		
		
		context.analyzer(HibernateSearchAnalyzer.KEYWORD).custom().withTokenizer(KEYWORDTOKENIZER);
		
		context.analyzer(HibernateSearchAnalyzer.KEYWORD_CLEAN).custom().withTokenizer(KEYWORDTOKENIZER)
				.withTokenFilters(ASCIIFOLDINGFILTER, LOWERCASEFILTER);
		
		context.analyzer(HibernateSearchAnalyzer.TEXT).custom().withTokenizer(WHITESPACETOKENIZER)
				.withTokenFilters(ASCIIFOLDINGFILTER, WORDDELIMITERFILTER, LOWERCASEFILTER);
		
		context.analyzer(HibernateSearchAnalyzer.TEXT_STEMMING).custom().withTokenizer(WHITESPACETOKENIZER)
				.withTokenFilters(ASCIIFOLDINGFILTER, WORDDELIMITERFILTER, LOWERCASEFILTER, ELASTICSEARCHTOKENFILTER);
		
		context.analyzer(HibernateSearchAnalyzer.TEXT_SORT).custom().withTokenizer(KEYWORDTOKENIZER)
				.withTokenFilters(ASCIIFOLDINGFILTER, LOWERCASEFILTER, PATTERNREPLACEFILTERPUNCTUATION, PATTERNREPLACEFILTERNUMBER, TRIMFILTER);
		
		context.normalizer(HibernateSearchNormalizer.KEYWORD);
		
		context.normalizer(HibernateSearchNormalizer.KEYWORD_CLEAN)
				.custom().withTokenFilters(ASCIIFOLDINGFILTER, LOWERCASEFILTER);
		
		context.normalizer(HibernateSearchNormalizer.TEXT)
				.custom().withTokenFilters(ASCIIFOLDINGFILTER, LOWERCASEFILTER, PATTERNREPLACEFILTERPUNCTUATION, PATTERNREPLACEFILTERNUMBER, TRIMFILTER);
	}

}
