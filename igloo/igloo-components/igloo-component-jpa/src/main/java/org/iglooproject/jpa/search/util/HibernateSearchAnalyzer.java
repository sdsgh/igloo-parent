package org.iglooproject.jpa.search.util;

public final class HibernateSearchAnalyzer {
	
	public static final String TEXT = "text";
	
	public static final String TEXT_STEMMING = "textStemming";
	
	/**
	 * // TODO: hibernate-search - trouver la nouvelle façon de procéder
	 * @deprecated Use {@link Normalizer} instead.
	 */
	@Deprecated
	public static final String TEXT_SORT = "textSort";
	
	public static final String KEYWORD = "keyword";
	
	public static final String KEYWORD_CLEAN = "keywordClean";
	
	private HibernateSearchAnalyzer() {
	}

}
