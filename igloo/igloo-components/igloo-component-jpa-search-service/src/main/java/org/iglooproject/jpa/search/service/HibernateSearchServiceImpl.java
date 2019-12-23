package org.iglooproject.jpa.search.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.lucene.analysis.Analyzer;
import org.iglooproject.jpa.business.generic.model.GenericEntity;
import org.iglooproject.jpa.business.generic.model.GenericEntityReference;
import org.iglooproject.jpa.exception.ServiceException;
import org.iglooproject.jpa.search.dao.IHibernateSearchDao;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class HibernateSearchServiceImpl implements IHibernateSearchService {
	
	private IHibernateSearchDao hibernateSearchDao;
	
	@PersistenceContext
	private EntityManager entityManager;

	public HibernateSearchServiceImpl(IHibernateSearchDao hibernateSearchDao) {
		this.hibernateSearchDao = hibernateSearchDao;
	}
	
	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Analyzer getAnalyzer(String analyzerName) {
		return hibernateSearchDao.getAnalyzer(analyzerName);
	}
	
	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Analyzer getAnalyzer(Class<?> entityType) {
		return hibernateSearchDao.getAnalyzer(entityType);
	}
	
	@Override
	public void reindexAll() throws ServiceException {
		hibernateSearchDao.reindexAll();
	}
	
	@Override
	public void reindexClasses(Collection<Class<?>> classes) throws ServiceException {
		if (classes != null && !classes.isEmpty()) {
			hibernateSearchDao.reindexClasses(classes.toArray(new Class<?>[ classes.size() ]));
		}
	}
	
	@Override
	public <K extends Serializable & Comparable<K>, E extends GenericEntity<K, ?>> void reindexEntity(E entity) {
		hibernateSearchDao.reindexEntity(entity);
	}
	
	@Override
	public <K extends Serializable & Comparable<K>, E extends GenericEntity<K, ?>> void reindexEntity(GenericEntityReference<K, E> reference) {
		hibernateSearchDao.reindexEntity(entityManager.find(reference.getType(), reference.getId()));
	}
	
	@Override
	public <K extends Serializable & Comparable<K>, E extends GenericEntity<K, ?>> void reindexEntity(Class<E> clazz, K id) {
		hibernateSearchDao.reindexEntity(entityManager.find(clazz, id));
	}
	
	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Set<Class<?>> getIndexedRootEntities() throws ServiceException {
		return hibernateSearchDao.getIndexedRootEntities(Object.class);
	}
	
	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Set<Class<?>> getIndexedRootEntities(Collection<Class<?>> classes) throws ServiceException {
		if (classes != null && !classes.isEmpty()) {
			return hibernateSearchDao.getIndexedRootEntities(classes.toArray(new Class<?>[ classes.size() ]));
		} else {
			return Sets.newHashSet();
		}
	}
	
	@Override
	public void flushToIndexes() {
		hibernateSearchDao.flushToIndexes();
	}
	
}
