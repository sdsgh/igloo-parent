package org.iglooproject.jpa.more.config.spring;

import java.util.Collection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

import org.iglooproject.jpa.more.business.CoreJpaMoreBusinessPackage;
import org.iglooproject.jpa.more.business.task.dao.IQueuedTaskHolderDao;
import org.iglooproject.jpa.more.business.task.dao.QueuedTaskHolderDaoImpl;
import org.iglooproject.jpa.more.business.task.model.IQueueId;
import org.iglooproject.jpa.more.business.task.service.IQueuedTaskHolderManager;
import org.iglooproject.jpa.more.business.task.service.IQueuedTaskHolderService;
import org.iglooproject.jpa.more.business.task.service.QueuedTaskHolderManagerImpl;
import org.iglooproject.jpa.more.business.task.service.QueuedTaskHolderServiceImpl;

@ComponentScan(basePackageClasses = { CoreJpaMoreBusinessPackage.class })
public abstract class AbstractTaskManagementConfig {
	
	public static final String OBJECT_MAPPER_BEAN_NAME = "queuedTaskHolderObjectMapper";

	@Bean(name = OBJECT_MAPPER_BEAN_NAME)
	public ObjectMapper queuedTaskHolderObjectMapper() {
		return new ObjectMapper().enableDefaultTyping(DefaultTyping.NON_FINAL);
	}

	@Bean
	public IQueuedTaskHolderDao queuedTaskHolderDao() {
		return new QueuedTaskHolderDaoImpl();
	}

	@Bean
	public IQueuedTaskHolderService queuedTaskHolderService(IQueuedTaskHolderDao queuedTaskHolderDao) {
		return new QueuedTaskHolderServiceImpl(queuedTaskHolderDao);
	}
	
	@Bean
	public IQueuedTaskHolderManager queuedTaskHolderManager() {
		return new QueuedTaskHolderManagerImpl();
	}
	
	/**
	 * Must return all the {@link IQueueId queue IDs} that are valid in this application.
	 */
	@Bean
	public abstract Collection<? extends IQueueId> queueIds();
}
