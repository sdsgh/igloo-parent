package org.iglooproject.jpa.batch.api;

/**
 * A listener interface for classes that want to be notified whenever a clear is performed on the EntityManager.
 * <p><strong>WARNING:</strong> Hibernate does not provide any way to add a "before clear" listener. Thus, listeners
 * <strong>will be notified only if the caller of {@link javax.persistence.EntityManager#clear()} also minds to call {@link #beforeClear()}
 * manually</strong>.
 * 
 * @deprecated use {@link IBatchExecutorListener}
 */
public interface IBeforeClearListener {
	
	void beforeClear();

}
