package org.iglooproject.jpa.hibernate.analyzers;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.search.backend.lucene.analysis.impl.LuceneAnalysisComponentFactory;
import org.hibernate.search.backend.lucene.analysis.model.dsl.impl.InitialLuceneAnalysisDefinitionContainerContext;
import org.hibernate.search.backend.lucene.analysis.model.impl.LuceneAnalysisDefinitionRegistry;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.springframework.beans.factory.annotation.Autowired;

public class LuceneEmbeddedFromElasticsearchAnalyzerRegistry implements LuceneEmbeddedAnalyzerRegistry {

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	private LuceneAnalysisDefinitionRegistry registry;

	@Override
	public Analyzer getAnalyzer(String analyzerName) {
		return registry.getAnalyzerDefinition(analyzerName);
	}

	@PostConstruct
	private void init() {
		ServiceRegistryImplementor serviceRegistry = entityManagerFactory.unwrap(SessionFactoryImplementor.class).getServiceRegistry();
		ClassLoaderService classLoaderService = serviceRegistry.getService(ClassLoaderService.class);
		HibernateOrmClassLoaderServiceClassAndResourceResolver resolver = new HibernateOrmClassLoaderServiceClassAndResourceResolver(classLoaderService);
		LuceneAnalysisComponentFactory factory = new LuceneAnalysisComponentFactory(Version.LATEST, resolver, resolver);
		InitialLuceneAnalysisDefinitionContainerContext collector = new InitialLuceneAnalysisDefinitionContainerContext(factory);
		new CoreLuceneClientAnalyzersDefinitionProvider().configure(collector);
		registry = new LuceneAnalysisDefinitionRegistry(collector);
	}

}
