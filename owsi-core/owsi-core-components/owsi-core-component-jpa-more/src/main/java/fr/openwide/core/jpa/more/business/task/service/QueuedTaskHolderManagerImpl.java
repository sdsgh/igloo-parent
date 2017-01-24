package fr.openwide.core.jpa.more.business.task.service;

import static fr.openwide.core.jpa.more.property.JpaMoreTaskPropertyIds.START_MODE;
import static fr.openwide.core.jpa.more.property.JpaMoreTaskPropertyIds.STOP_TIMEOUT;
import static fr.openwide.core.jpa.more.property.JpaMoreTaskPropertyIds.queueNumberOfThreads;
import static fr.openwide.core.jpa.more.property.JpaMoreTaskPropertyIds.queueStartDelay;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import fr.openwide.core.jpa.exception.SecurityServiceException;
import fr.openwide.core.jpa.exception.ServiceException;
import fr.openwide.core.jpa.more.business.task.model.AbstractTask;
import fr.openwide.core.jpa.more.business.task.model.IQueueId;
import fr.openwide.core.jpa.more.business.task.model.QueuedTaskHolder;
import fr.openwide.core.jpa.more.business.task.service.impl.TaskConsumer;
import fr.openwide.core.jpa.more.business.task.service.impl.TaskQueue;
import fr.openwide.core.jpa.more.business.task.util.TaskStatus;
import fr.openwide.core.jpa.more.config.spring.AbstractTaskManagementConfig;
import fr.openwide.core.jpa.more.util.transaction.model.ITransactionSynchronizationAfterCommitTask;
import fr.openwide.core.jpa.more.util.transaction.service.ITransactionSynchronizationTaskManagerService;
import fr.openwide.core.spring.config.util.TaskQueueStartMode;
import fr.openwide.core.spring.property.service.IPropertyService;
import fr.openwide.core.spring.util.SpringBeanUtils;
	
public class QueuedTaskHolderManagerImpl implements IQueuedTaskHolderManager, ApplicationListener<ContextRefreshedEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(QueuedTaskHolderManagerImpl.class);

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private IQueuedTaskHolderService queuedTaskHolderService;

	@Autowired
	@Qualifier(AbstractTaskManagementConfig.OBJECT_MAPPER_BEAN_NAME)
	private ObjectMapper queuedTaskHolderObjectMapper;

	@Autowired
	private IPropertyService propertyService;
	
	@Autowired
	private ITransactionSynchronizationTaskManagerService synchronizationManager;
	
	@Resource
	private Collection<? extends IQueueId> queueIds;
	
	private int stopTimeout;
	
	private final Multimap<TaskQueue, TaskConsumer> consumersByQueue
			= Multimaps.newListMultimap(new HashMap<TaskQueue, Collection<TaskConsumer>>(), new Supplier<List<TaskConsumer>>() {
				@Override
				public List<TaskConsumer> get() {
					return Lists.newArrayList();
				}
			});
	
	private final Map<String, TaskQueue> queuesById = Maps.newHashMap();
	
	private TaskQueue defaultQueue;

	private AtomicBoolean active = new AtomicBoolean(false);

	private AtomicBoolean availableForAction = new AtomicBoolean(true);

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		/* 
		 * onApplicationEvent is called for every context initialization, including potential child contexts.
		 * We avoid starting queues multiple times, by calling init() only on the root application context initialization.
		 */
		if (event != null && event.getSource() != null
				&& AbstractApplicationContext.class.isAssignableFrom(event.getSource().getClass())
				&& ((AbstractApplicationContext) event.getSource()).getParent() == null) {
			init();
		}
	}

	private void init() {
		stopTimeout = propertyService.get(STOP_TIMEOUT);

		initQueues();
		if (TaskQueueStartMode.auto.equals(propertyService.get(START_MODE))) {
			start();
		} else {
			LOGGER.warn("Task queue start configured in \"manual\" mode.");
		}
	}
	
	private final void initQueues() {
		Collection<String> queueIdsAsStrings = Lists.newArrayList();
		
		// Sanity checks
		for (IQueueId queueId : queueIds) {
			String queueIdAsString = queueId.getUniqueStringId();
			Assert.state(!IQueueId.RESERVED_DEFAULT_QUEUE_ID_STRING.equals(queueIdAsString),
					"Queue ID '" + IQueueId.RESERVED_DEFAULT_QUEUE_ID_STRING + "' is reserved for implementation purposes. Please choose another ID.");
			Assert.state(!queueIdsAsStrings.contains(queueIdAsString), "Queue ID '" + queueIdAsString + "' was defined more than once. Queue IDs must be unique.");
			queueIdsAsStrings.add(queueIdAsString);
		}

		defaultQueue = initQueue(IQueueId.RESERVED_DEFAULT_QUEUE_ID_STRING, 1);
		for (String queueIdAsString : queueIdsAsStrings) {
			int numberOfThreads = propertyService.get(queueNumberOfThreads(queueIdAsString));
			if (numberOfThreads < 1) {
				LOGGER.warn("Number of threads for queue '{}' is set to an invalid value ({}); defaulting to 1", queueIdAsString, numberOfThreads);
				numberOfThreads = 1;
			}
			initQueue(queueIdAsString, numberOfThreads);
		}
	}
	
	private TaskQueue initQueue(String queueId, int numberOfThreads) {
		TaskQueue queue = new TaskQueue(queueId);
		for (int i = 0 ; i < numberOfThreads ; ++i) {
			TaskConsumer consumer = new TaskConsumer(queue, i);
			SpringBeanUtils.autowireBean(applicationContext, consumer);
			consumersByQueue.put(queue, consumer);
		}
		queuesById.put(queueId, queue);
		return queue;
	}
	
	private String selectQueue(AbstractTask task) {
		SpringBeanUtils.autowireBean(applicationContext, task);
		IQueueId queueId = task.selectQueue();
		String queueIdString = queueId == null ? null : queueId.getUniqueStringId();
		return queueIdString;
	}
	
	private TaskQueue getQueue(String queueId) {
		if (queueId == null) {
			return defaultQueue;
		}
		
		TaskQueue queue = queuesById.get(queueId);
		if (queue == null) {
			LOGGER.warn("Queue ID '{}' is unknown ; falling back to default queue", queueId);
			queue = defaultQueue;
		}
		return queue;
	}

	@Override
	public boolean isAvailableForAction() {
		return availableForAction.get();
	}

	@Override
	public boolean isActive() {
		return active.get();
	}

	@Override
	public int getNumberOfWaitingTasks() {
		int total = 0;
		for (TaskQueue queue : queuesById.values()) {
			total += queue.size();
		}
		return total;
	}

	@Override
	public int getNumberOfRunningTasks() {
		int total = 0;
		for (TaskQueue queue : queuesById.values()) {
			Collection<TaskConsumer> consumers = consumersByQueue.get(queue);
			for (TaskConsumer consumer : consumers) {
				if (consumer.isWorking()) {
					total++;
				}
			}
		}
		return total;
	}
	
	@Override
	public Collection<IQueueId> getQueueIds() {
		return Collections.unmodifiableCollection(queueIds);
	}

	@Override
	public void start() {
		startConsumers();
	}

	protected synchronized void startConsumers() {
		if (!active.get()) {
			availableForAction.set(false);
			
			try {
				initQueuesFromDatabase();
				for (TaskConsumer consumer : consumersByQueue.values()) {
					Long configurationStartDelay = getStartDelay(consumer.getQueue().getId());
					consumer.start(configurationStartDelay);
				}
			} finally {
				active.set(true);
				availableForAction.set(true);
			}
		}
	}

	/**
	 *  The length of time the consumer threads will wait before their first access to their task queue.
	 */
	protected Long getStartDelay(String queueId) {
		return propertyService.get(queueStartDelay(queueId));
	}

	private void initQueuesFromDatabase() {
		for (TaskQueue queue : queuesById.values()) {
			try {
				List<Long> taskIds = queuedTaskHolderService.initializeTasksAndListConsumable(queue.getId());
				if (queue == defaultQueue) {
					taskIds.addAll(queuedTaskHolderService.initializeTasksAndListConsumable(null));
				}
				for (Long taskId : taskIds) {
					boolean status = queue.offer(taskId);
					if (!status) {
						LOGGER.error("Unable to offer the task " + taskId + " to the queue");
					}
				}
			} catch (RuntimeException | ServiceException | SecurityServiceException e) {
				LOGGER.error("Error while trying to init queue " + queue + " from database", e);
			}
		}
	}

	@Override
	public final QueuedTaskHolder submit(AbstractTask task) throws ServiceException {
		QueuedTaskHolder newQueuedTaskHolder = null;
		final String selectedQueueId;
		
		try {
			String serializedTask;
			selectedQueueId = selectQueue(task);
			serializedTask = queuedTaskHolderObjectMapper.writeValueAsString(task);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Serialized task: " + serializedTask);
			}
			
			newQueuedTaskHolder = new QueuedTaskHolder(
					task.getTaskName(), selectedQueueId /* May differ from selectedQueue.getId() */,
					task.getTaskType(), serializedTask
			);
			
			queuedTaskHolderService.create(newQueuedTaskHolder);
		} catch (IOException e) {
			throw new ServiceException("Error while trying to serialize task " + task, e);
		} catch (RuntimeException | ServiceException | SecurityServiceException e) {
			throw new ServiceException("Error while creating and saving task " + task, e);
		}
		
		final Long newQueuedTaskHolderId = newQueuedTaskHolder.getId();
		synchronizationManager.push(new ITransactionSynchronizationAfterCommitTask() {
			@Override
			public void run() throws Exception {
				doSubmit(selectedQueueId, newQueuedTaskHolderId);
			}
		});
		
		return newQueuedTaskHolder;
	}
	
	protected void doSubmit(String queueId, Long newQueuedTaskHolderId) throws ServiceException {
		if (active.get()) {
			TaskQueue selectedQueue = getQueue(queueId);
			boolean status = selectedQueue.offer(newQueuedTaskHolderId);
			if (!status) {
				LOGGER.error("Unable to offer the task " + newQueuedTaskHolderId + " to the queue");
			}
		}
	}
	
	@Override
	public void reload(Long queuedTaskHolderId) throws ServiceException, SecurityServiceException {
		QueuedTaskHolder queuedTaskHolder = queuedTaskHolderService.getById(queuedTaskHolderId);
		if (queuedTaskHolder != null) {
			queuedTaskHolder.setStatus(TaskStatus.TO_RUN);
			queuedTaskHolder.resetExecutionInformation();
			queuedTaskHolderService.update(queuedTaskHolder);
			
			TaskQueue queue = getQueue(queuedTaskHolder.getQueueId());
			
			if (active.get()) {
				boolean status = queue.offer(queuedTaskHolder.getId());
				if (!status) {
					LOGGER.error("Unable to offer the task " + queuedTaskHolder.getId() + " to the queue");
				}
			}
		}
	}
	
	@Override
	public void cancel(Long queuedTaskHolderId) throws ServiceException, SecurityServiceException {
		QueuedTaskHolder queuedTaskHolder = queuedTaskHolderService.getById(queuedTaskHolderId);
		if (queuedTaskHolder != null) {
			queuedTaskHolder.setStatus(TaskStatus.CANCELLED);
			queuedTaskHolderService.update(queuedTaskHolder);
		}
	}

	/**
	 * Warning: this destroy method is called twice, so all the related code has
	 * to be written with this constraint
	 * 
	 * @throws Exception
	 */
	@PreDestroy
	private void destroy() {
		stop();
	}

	@Override
	public synchronized void stop() {
		if (active.get()) {
			availableForAction.set(false);
			
			try {
				// TODO YRO gérer le timeout plus intelligemment
				for (TaskQueue queue : queuesById.values()) {
					for (TaskConsumer consumer : consumersByQueue.get(queue)) {
						try {
							consumer.stop(stopTimeout);
						} catch (RuntimeException e) {
							LOGGER.error("Error while trying to stop consumer " + consumer, e);
						}
					}
					interruptQueueProcesses(queue);
				}
			} finally {
				active.set(false);
				availableForAction.set(true);
			}
		}
	}

	private void interruptQueueProcesses(TaskQueue queue) {
		List<Long> queuedTaskHolderIds = new LinkedList<Long>();
		queue.drainTo(queuedTaskHolderIds);
		
		for (Long queuedTaskHolderId : queuedTaskHolderIds) {
			QueuedTaskHolder queuedTaskHolder = queuedTaskHolderService.getById(queuedTaskHolderId);
			if (queuedTaskHolder != null) {
				try {
					queuedTaskHolder.setStatus(TaskStatus.INTERRUPTED);
					queuedTaskHolder.setEndDate(new Date());
					queuedTaskHolder.resetExecutionInformation();
					queuedTaskHolderService.update(queuedTaskHolder);
				} catch (RuntimeException | ServiceException | SecurityServiceException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}