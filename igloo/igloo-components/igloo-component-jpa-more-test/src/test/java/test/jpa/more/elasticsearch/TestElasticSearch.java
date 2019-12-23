package test.jpa.more.elasticsearch;

import org.iglooproject.jpa.search.service.IHibernateSearchService;
import org.iglooproject.jpa.util.EntityManagerUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import test.jpa.more.business.AbstractJpaMoreTestCase;
import test.jpa.more.business.entity.model.TestEntity;

public class TestElasticSearch extends AbstractJpaMoreTestCase {
	
	@Autowired
	protected EntityManagerUtils entityManagerUtils;

	@Autowired
	private IHibernateSearchService hibernateSearchService;

	/**
	 * Check that elasticsearch analyzers can be mapped to Lucene ones.
	 */
	@Test
	public void testElasticSearchAnalyzer() {
		hibernateSearchService.getAnalyzer(TestEntity.class);
	}

}
