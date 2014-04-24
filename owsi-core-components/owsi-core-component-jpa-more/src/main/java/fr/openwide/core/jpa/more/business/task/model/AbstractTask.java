package fr.openwide.core.jpa.more.business.task.model;

import java.io.Serializable;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Throwables;

import fr.openwide.core.commons.util.CloneUtils;
import fr.openwide.core.jpa.exception.SecurityServiceException;
import fr.openwide.core.jpa.exception.ServiceException;
import fr.openwide.core.jpa.more.business.task.service.IQueuedTaskHolderManager;
import fr.openwide.core.jpa.more.business.task.service.IQueuedTaskHolderService;
import fr.openwide.core.jpa.more.business.task.util.TaskStatus;

public abstract class AbstractTask implements Runnable, Serializable {
	private static final long serialVersionUID = 7734300264023051135L;

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTask.class);

	private TransactionTemplate transactionTemplate;

	@Autowired
	protected IQueuedTaskHolderService queuedTaskHolderService;

	@Autowired
	protected IQueuedTaskHolderManager queuedTaskHolderManager;

	@JsonIgnore
	@org.codehaus.jackson.annotate.JsonIgnore
	protected Long queuedTaskHolderId;

	@JsonIgnore
	@org.codehaus.jackson.annotate.JsonIgnore
	protected String report;

	protected Date triggeringDate;

	protected String taskName;

	protected String taskType;

	protected AbstractTask() { }

	public AbstractTask(String taskName, ITaskTypeProvider taskTypeProvider, Date triggeringDate) {
		this(taskName, taskTypeProvider.getTaskType(), triggeringDate);
	}

	public AbstractTask(String taskName, String taskType, Date triggeringDate) {
		setTaskName(taskName);
		setTaskType(taskType);
		setTriggeringDate(triggeringDate);
	}
	
	/**
	 * @return The ID of the queue this task must be added in, or <code>null</code> for the default queue. 
	 */
	public IQueueId selectQueue() {
		return null;
	}

	/**
	 * Permet à la tâche d'indiquer un transactionTemplate alternatif. Utile en particulier :
	 *  - pour s'assurer d'être readOnly
	 *  - pour mettre en place une tâche non transactionnelle
	 */
	protected TransactionTemplate getTaskTransactionTemplate() {
		return transactionTemplate;
	}

	/**
	 * Permet à la tâche d'indiquer un transactionTemplate alternatif. Utile en particulier :
	 *  - pour s'assurer d'être readOnly
	 *  - pour mettre en place une tâche non transactionnelle
	 */
	protected TransactionTemplate getPropagationRequiresNewReadOnlyFalseTransactionTemplate() {
		return transactionTemplate;
	}

	@Autowired
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		DefaultTransactionAttribute transactionAttributes = new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionAttributes.setReadOnly(false);
		transactionTemplate = new TransactionTemplate(transactionManager, transactionAttributes);
	}

	@Override
	public void run() {
		final Exception beforeTaskResult = getPropagationRequiresNewReadOnlyFalseTransactionTemplate().execute(new TransactionCallback<Exception>() {
			@Override
			public Exception doInTransaction(TransactionStatus status) {
				try {
					QueuedTaskHolder queuedTaskHolder = queuedTaskHolderService.getById(queuedTaskHolderId);

					if (queuedTaskHolder == null) {
						throw new IllegalArgumentException("No task found with id " + getQueuedTaskHolderId());
					}

					queuedTaskHolder.setStartDate(new Date());
					queuedTaskHolder.setStatus(TaskStatus.RUNNING);
					queuedTaskHolderService.update(queuedTaskHolder);

					return null;
				} catch (Exception e) {
					status.setRollbackOnly();
					return e;
				}
			}
		});

		if (beforeTaskResult != null) {
			getPropagationRequiresNewReadOnlyFalseTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					try {
						QueuedTaskHolder queuedTaskHolder = queuedTaskHolderService.getById(queuedTaskHolderId);

						LOGGER.error("An error has occured while executing task " + queuedTaskHolder, beforeTaskResult);

						queuedTaskHolder.setStatus(onFailStatus());
						queuedTaskHolder.setEndDate(new Date());
						queuedTaskHolder.setReport(report);
						queuedTaskHolder.setResult(Throwables.getStackTraceAsString(beforeTaskResult));
						queuedTaskHolderService.update(queuedTaskHolder);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		}

		final Exception taskResult = getTaskTransactionTemplate().execute(new TransactionCallback<Exception>() {
			@Override
			public Exception doInTransaction(TransactionStatus status) {
				try {
					QueuedTaskHolder queuedTaskHolder = queuedTaskHolderService.getById(queuedTaskHolderId);
					
					// Auparavant, on mettait à jour le statut de succès ici, après avoir traité la tâche.
					// La tâche pouvait intervenir sur les informations du queuedTaskHolder.
					// Le problème de cette approche est qu'il n'est pas possible pour la tâche de gérer
					// finement sa transaction et son entityManager car ils vont être partagés.
					// 
					// Maintenant : on sauve le queuedTaskHolder pour être compatible avec le code qui aurait
					// fait des modifications (ce code ne doit pas jouer avec le entityManager ou les transactions)
					// Pour le code qui veut jouer avec ces éléments, il peut traiter de manière spécifique le
					// updateQueuedTaskHolder
					// L'information de succès est sauvée dans un deuxième temps.
					doTask(queuedTaskHolder);
					updateQueuedTaskHolder(queuedTaskHolder);
					
					return null;
				} catch (Exception e) {
					status.setRollbackOnly();
					return e;
				}
			}
		});

		if (taskResult == null) {
			// Cas du succès
			getPropagationRequiresNewReadOnlyFalseTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					try {
						QueuedTaskHolder queuedTaskHolder = queuedTaskHolderService.getById(queuedTaskHolderId);
						
						queuedTaskHolder = queuedTaskHolderService.getById(queuedTaskHolderId);
						queuedTaskHolder.setEndDate(new Date());
						queuedTaskHolder.setStatus(TaskStatus.COMPLETED);
						queuedTaskHolder.setReport(report);
						queuedTaskHolderService.update(queuedTaskHolder);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		} else {
			// Cas de l'erreur
			getPropagationRequiresNewReadOnlyFalseTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					try {
						QueuedTaskHolder queuedTaskHolder = queuedTaskHolderService.getById(queuedTaskHolderId);
						
						LOGGER.error("An error has occured while executing task " + queuedTaskHolder, taskResult);
						
						queuedTaskHolder.setStatus(onFailStatus());
						queuedTaskHolder.setEndDate(new Date());
						queuedTaskHolder.setReport(report);
						queuedTaskHolder.setResult(Throwables.getStackTraceAsString(taskResult));
						queuedTaskHolderService.update(queuedTaskHolder);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
	}

	/**
	 * En cas d'utilisation compliquée du transactionManager, permet de laisser la task gérer de manière judicieuse
	 * la mise à jour.
	 * 
	 * NOTA : il n'est pas nécessaire de mettre à jour les informations standard d'échec / succès.
	 * 
	 * @throws SecurityServiceException 
	 * @throws ServiceException 
	 */
	protected void updateQueuedTaskHolder(QueuedTaskHolder queuedTaskHolder) throws ServiceException, SecurityServiceException {
		// Dans le comportement par défaut, on met à jour les modifications potentielles sur le queuedTaskHolder
		queuedTaskHolderService.update(queuedTaskHolder);
	}

	protected abstract void doTask(QueuedTaskHolder queuedTaskHolder) throws Exception;

	public Long getQueuedTaskHolderId() {
		return queuedTaskHolderId;
	}

	public void setQueuedTaskHolderId(Long queuedTaskHolderId) {
		this.queuedTaskHolderId = queuedTaskHolderId;
	}

	public Date getTriggeringDate() {
		return CloneUtils.clone(triggeringDate);
	}

	public void setTriggeringDate(Date triggeringDate) {
		this.triggeringDate = CloneUtils.clone(triggeringDate);
	}

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	@JsonIgnore
	@org.codehaus.jackson.annotate.JsonIgnore
	public TaskStatus onFailStatus() {
		return TaskStatus.FAILED;
	}
}