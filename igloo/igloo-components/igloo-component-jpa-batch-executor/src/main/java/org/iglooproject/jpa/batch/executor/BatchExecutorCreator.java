package org.iglooproject.jpa.batch.executor;

import java.util.function.Supplier;

public class BatchExecutorCreator {
	
	private final Supplier<SimpleHibernateBatchExecutor> simpleHibernateBatchExecutorFactory;
	
	private final Supplier<MultithreadedBatchExecutor> multithreadedBatchExecutorFactory;
	
	public BatchExecutorCreator(Supplier<SimpleHibernateBatchExecutor> simpleHibernateBatchExecutorFactory,
			Supplier<MultithreadedBatchExecutor> multithreadedBatchExecutorFactory) {
		this.simpleHibernateBatchExecutorFactory = simpleHibernateBatchExecutorFactory;
		this.multithreadedBatchExecutorFactory = multithreadedBatchExecutorFactory;
	}
	
	public SimpleHibernateBatchExecutor newSimpleHibernateBatchExecutor() {
		return simpleHibernateBatchExecutorFactory.get();
	}
	
	public MultithreadedBatchExecutor newMultithreadedBatchExecutor() {
		return multithreadedBatchExecutorFactory.get();
	}

}
