package org.iglooproject.jpa.search.config.spring;

import java.util.Collection;

import org.iglooproject.jpa.batch.api.IBatchExecutorListener;
import org.iglooproject.jpa.exception.ServiceException;
import org.iglooproject.jpa.search.service.IHibernateSearchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration must be loaded only when {@link IBatchExecutorListener} is available.
 */
@Configuration
public class HibernateSearchBatchExecutorConfig {

	@Bean
	public HibernateSearchBatchExecutorListener hibernateSearchBatchExecutorListener(
			IHibernateSearchService hibernateSearchService) {
		return new HibernateSearchBatchExecutorListener(hibernateSearchService);
	}

	/**
	 * Wrapper that provides {@link IBatchExecutorListener} from {@link IHibernateSearchService}.
	 */
	public static class HibernateSearchBatchExecutorListener implements IBatchExecutorListener {
		
		private final IHibernateSearchService hibernateSearchService;
		
		public HibernateSearchBatchExecutorListener(IHibernateSearchService hibernateSearchService) {
			this.hibernateSearchService = hibernateSearchService;
		}
		
		@Override
		public void onBatchExecutorFlush() {
			hibernateSearchService.flushToIndexes();
		}

		@Override
		public void onBatchExecutorEnd(Collection<Class<?>> modifiedEntityTypes) throws ServiceException {
			hibernateSearchService.reindexClasses(modifiedEntityTypes);
		}

		@Override
		public void beforeClear() {
			// Nothing
		}
		
	}

}
