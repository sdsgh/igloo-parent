package org.iglooproject.spring.util.lucene.search;

import java.util.Objects;

import org.apache.lucene.search.Query;

public final class RawLuceneQuery extends Query {
	
	private String query;
	
	public RawLuceneQuery(String query) {
		this.query = query;
	}
	
	public String getQuery() {
		return query;
	}
	
	@Override
	public String toString(String field) {
		return query;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RawLuceneQuery)) {
			return false;
		}
		return Objects.equals(this.query, ((RawLuceneQuery) obj).query);
	}

	@Override
	public int hashCode() {
		if (query == null) {
			return 0;
		} else {
			return query.hashCode();
		}
	}
	
}
