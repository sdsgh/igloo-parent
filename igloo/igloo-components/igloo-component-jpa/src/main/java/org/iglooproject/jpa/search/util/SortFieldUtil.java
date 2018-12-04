package org.iglooproject.jpa.search.util;

import java.util.Collection;

import javax.persistence.EntityManager;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.engine.search.dsl.sort.FieldSortContext;
import org.hibernate.search.engine.search.dsl.sort.ScoreSortContext;
import org.hibernate.search.engine.search.dsl.sort.SearchSortContainerContext;
import org.hibernate.search.engine.search.dsl.sort.SortOrderContext;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.jpa.FullTextEntityManager;
import org.hibernate.search.mapper.orm.jpa.FullTextQuery;
import org.hibernate.search.mapper.orm.jpa.FullTextSearchTarget;

public final class SortFieldUtil {

	public static final Sort getSort(EntityManager entityManager, Class<?> entityClass, SortField... sortFields) {
		if (sortFields == null || sortFields.length == 0) {
			return null;
		}
		
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
		FullTextSearchTarget<?> queryBuilder = fullTextEntityManager.search(entityClass);
		SearchSortContainerContext context = queryBuilder.sort();

		for (SortField sortField : sortFields) {
			if (sortField == null) {
				throw new IllegalStateException("SortField must not be null.");
			}
			
			switch (sortField.getType()) {
			case SCORE:
				ScoreSortContext score = context.byScore();
				order(score, sortField);
				break;
			default:
				FieldSortContext fieldContext = context.byField(sortField.getField());
				onMissingValue(fieldContext, sortField);
				order(fieldContext, sortField);
				break;
			}
		}
		
		return create(fieldContext);
	}

	private static void onMissingValue(FieldSortContext fieldContext, SortField sortField) {
		if (!(fieldContext instanceof FieldSortContext)) {
			return;
		}
		
		if (sortField.getMissingValue() == null) {
			return;
		}
		
		if (SortField.Type.STRING.equals(sortField.getType()) || SortField.Type.STRING_VAL.equals(sortField.getType())) {
			if (SortField.STRING_FIRST.equals(sortField.getMissingValue())) {
				fieldContext.onMissingValue().sortFirst();
			} else if (SortField.STRING_LAST.equals(sortField.getMissingValue())) {
				fieldContext.onMissingValue().sortLast();
			}
		} else {
			fieldContext.onMissingValue().use(sortField.getMissingValue());
		}
		
	}

	@SuppressWarnings("unchecked")
	private static void order(SortOrderContext<?> fieldContext, SortField sortField) {
		!sortField.getReverse() ? fieldContext.asc() : fieldContext.desc();
	}

	private static Sort create(SearchSortContainerContext fieldContext) {
		if (!(fieldContext instanceof SortTermination)) {
			throw new IllegalStateException("FieldContext must be a SortTermination.");
		}
		
		return ((SortTermination) fieldContext).createSort();
	}

	public static void setSort(FullTextQuery ftq, EntityManager entityManager, Class<?> entityClass, SortField... sortFields) {
		ftq.setSort(getSort(entityManager, entityClass, sortFields));
	}

	public static void setSort(FullTextQuery ftq, EntityManager entityManager, Class<?> entityClass, Collection<SortField> sortFields) {
		ftq.setSort(getSort(entityManager, entityClass, sortFields.toArray(new SortField[sortFields.size()])));
	}

	public static void setSort(FullTextQuery ftq, EntityManager entityManager, Class<?> entityClass, Sort sort) {
		setSort(ftq, entityManager, entityClass, sort.getSort());
	}

	private SortFieldUtil() {} // NOSONAR
}
