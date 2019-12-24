package org.iglooproject.jpa.batch.api;

import java.util.Collection;

import org.iglooproject.jpa.exception.ServiceException;

/**
 * A listener interface for classes that want to be notified whenever a clear is performed on the EntityManager.
 * <p><strong>WARNING:</strong> Hibernate does not provide any way to add a "before clear" listener. Thus, listeners
 * <strong>will be notified only if the caller of {@link javax.persistence.EntityManager#clear()} also minds to call {@link #beforeClear()}
 * manually</strong>.
 */
@SuppressWarnings("deprecation")
public interface IBatchExecutorListener extends IBeforeClearListener {

	/**
	 * Called each time a flush is performed by batch executor
	 */
	void onBatchExecutorFlush();

	/**
	 * Called when the whole batch is ended.
	 * 
	 * @param collection of types of modified entities
	 */
	void onBatchExecutorEnd(Collection<Class<?>> modifiedEntityTypes) throws ServiceException;

	/**
	 * Called each time session is cleared by batch executor
	 */
	@Override
	void beforeClear();

}
