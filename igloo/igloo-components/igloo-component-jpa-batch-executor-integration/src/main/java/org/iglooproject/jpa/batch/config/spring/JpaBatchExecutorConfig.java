package org.iglooproject.jpa.batch.config.spring;

import java.util.Collection;

import org.iglooproject.jpa.batch.api.IBatchExecutorListener;
import org.iglooproject.jpa.batch.executor.BatchExecutorCreator;
import org.iglooproject.jpa.batch.executor.MultithreadedBatchExecutor;
import org.iglooproject.jpa.batch.executor.SimpleHibernateBatchExecutor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class JpaBatchExecutorConfig {

	@Bean
	public BatchExecutorCreator batchExecutorCreator(ObjectProvider<SimpleHibernateBatchExecutor> simpleHibernateBatchExecutor,
			ObjectProvider<MultithreadedBatchExecutor> multithreadedBatchExecutor) {
		return new BatchExecutorCreator(simpleHibernateBatchExecutor::getObject, multithreadedBatchExecutor::getObject);
	}

	/**
	 * Stateful bean; a new instance must be retrieved each time it is needed
	 */
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SimpleHibernateBatchExecutor simpleHibernateBatchExecutor(Collection<IBatchExecutorListener> batchExecutorListeners) {
		return new SimpleHibernateBatchExecutor(batchExecutorListeners);
	}

	/**
	 * Stateful bean; a new instance must be retrieved each time it is needed
	 */
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public MultithreadedBatchExecutor multithreadedBatchExecutor() {
		return new MultithreadedBatchExecutor();
	}

}
