/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.service.cmr.workflow;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiTypeConverter;
import org.alfresco.service.namespace.QName;


/**
 * A {@link WorkflowTask} instance that will only fecth the {@link WorkflowPath} if needed and will only
 * fetch the properties when properties are needed which cannot be returned by the wrapped {@link Task} 
 * or {@link HistoricTaskInstance} object.
 * 
 * The instance can only be used inside of the alfresco-context/transaction where the list of instances
 * is returned.
 * 
 * @author Frederik Heremans
 */
public class LazyActivitiWorkflowTask extends WorkflowTask
{
	private transient ActivitiTypeConverter activitiTypeConverter;
	private transient Task task;
	private transient HistoricTaskInstance historicTask;
	
	private LazyPropertiesMap lazyPropertiesMap;
	
	@SuppressWarnings("deprecation")
	public LazyActivitiWorkflowTask(Task task, ActivitiTypeConverter typeConverter, TenantService tenantService, String workflowDefinitionName) 
	{
		super(BPMEngineRegistry.createGlobalId(ActivitiConstants.ENGINE_ID, task.getId()), null, null, null, null, null, null, null);
		this.task = task;
		this.activitiTypeConverter = typeConverter;
		this.lazyPropertiesMap = new LazyPropertiesMap();
		
		// Fetch task-definition and a partially-initialized WorkflowTask (not including properties and path)
		WorkflowTaskDefinition taskDefinition = activitiTypeConverter.getTaskDefinition(task);
		
		WorkflowTask partiallyInitialized = typeConverter.getWorkflowObjectFactory().createTask(task.getId(), taskDefinition, taskDefinition.getId(), task.getName(),
				task.getDescription(), WorkflowTaskState.IN_PROGRESS, null, workflowDefinitionName , lazyPropertiesMap);
		
		this.definition = taskDefinition;
		this.name = taskDefinition.getId();
		this.title = partiallyInitialized.getTitle();
		this.description = partiallyInitialized.getDescription();
		this.state = partiallyInitialized.getState();
	}
	
	@SuppressWarnings("deprecation")
	public LazyActivitiWorkflowTask(HistoricTaskInstance historicTask, ActivitiTypeConverter typeConverter, TenantService tenantService) 
	{
		super(BPMEngineRegistry.createGlobalId(ActivitiConstants.ENGINE_ID, historicTask.getId()), null, null, null, null, null, null, null);
		this.historicTask = historicTask;
		this.activitiTypeConverter = typeConverter;
		this.lazyPropertiesMap = new LazyPropertiesMap();
		
		// Fetch task-definition and a partially-initialized WorkflowTask (not including properties and path)
		WorkflowTaskDefinition taskDefinition = activitiTypeConverter.getTaskDefinition(historicTask.getTaskDefinitionKey(), historicTask.getProcessDefinitionId());
		
		String workflowDefinitionName = activitiTypeConverter.getWorkflowDefinitionName(historicTask.getProcessDefinitionId());
		workflowDefinitionName = tenantService.getBaseName(workflowDefinitionName);
		
		WorkflowTask partiallyInitialized = typeConverter.getWorkflowObjectFactory().createTask(historicTask.getId(), taskDefinition, taskDefinition.getId(), historicTask.getName(),
				historicTask.getDescription(), WorkflowTaskState.COMPLETED, null, workflowDefinitionName , lazyPropertiesMap);
		
		this.definition = taskDefinition;
		this.name = taskDefinition.getId();
		this.title = partiallyInitialized.getTitle();
		this.description = partiallyInitialized.getDescription();
		this.state = partiallyInitialized.getState();
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public WorkflowPath getPath() 
	{
		if(path == null)
		{
			if(task != null)
			{
				this.path = activitiTypeConverter.getWorkflowPath(task.getExecutionId(), true);
			}
			else
			{
				this.path = activitiTypeConverter.getWorkflowPath(historicTask);
			}
		}
		return super.getPath();
	}
	
	@Override
	public Map<QName, Serializable> getProperties() {
		return lazyPropertiesMap;
	}
	
	@SuppressWarnings("deprecation")
	public Map<QName, Serializable> ensureProperties() 
	{
		if(this.properties == null)
		{
			if(task != null)
			{
				this.properties = activitiTypeConverter.getTaskProperties(task);
			}
			else
			{
				this.properties =  activitiTypeConverter.getTaskProperties(historicTask);
			}
		}
		return this.properties;
	}
	
	
	
	/**
	 * Property-map that returns properties that are known from the {@link Task} instance
	 * without having to fetch the actual task-properties. Once a property is required that cannot
	 * be deducted from the {@link Task} instance, all properties are fetched.
	 *  
	 * @author Frederik Heremans
	 */
	@SuppressWarnings("deprecation")
	private class LazyPropertiesMap implements Map<QName, Serializable>
	{
		@Override
		public void clear() {
			ensureProperties().clear();
		}

		@Override
		public boolean containsKey(Object key) {
			return ensureProperties().containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return ensureProperties().containsValue(value);
		}

		@Override
		public Set<java.util.Map.Entry<QName, Serializable>> entrySet() {
			return ensureProperties().entrySet();
		}

		@Override
		public Serializable get(Object key) {
			if(WorkflowModel.PROP_DUE_DATE.equals(key)) {
				return getDueDate();
			}
			else if(WorkflowModel.PROP_PRIORITY.equals(key)) 
			{
				return getPriority();
			}
			else if(WorkflowModel.PROP_DESCRIPTION.equals(key)) 
			{
				// Description-property is based on the task.getDescription(). Revert to the default task(type) description, if missing.
				if(task != null) 
				{
					return (task.getDescription() != null && !task.getDescription().isEmpty()) ? task.getDescription() : getDescription();
				}
				else
				{
					return (historicTask.getDescription() != null && !historicTask.getDescription().isEmpty()) ? historicTask.getDescription() : getDescription();
				}
			} 
			else if(ContentModel.PROP_CREATED.equals(key) || WorkflowModel.PROP_START_DATE.equals(key)) 
			{
				return getCreated();
			} 
			else if(ContentModel.PROP_OWNER.equals(key)) 
			{
				return getOwner();
			}
			else if(ContentModel.PROP_NAME.equals(key)) 
			{
				return getName();
			} 
			else if(WorkflowModel.PROP_TASK_ID.equals(key)) 
			{
				return getId();
			} 
			else
			{
				return ensureProperties().get(key);
			}
		}

		@Override
		public boolean isEmpty() {
			return ensureProperties().isEmpty();
		}

		@Override
		public Set<QName> keySet() {
			return ensureProperties().keySet();
		}

		@Override
		public Serializable put(QName key, Serializable value) {
			return ensureProperties().put(key, value);
		}

		@Override
		public void putAll(Map<? extends QName, ? extends Serializable> m) {
			ensureProperties().putAll(m);
		}

		@Override
		public Serializable remove(Object key) {
			return ensureProperties().remove(key);
		}

		@Override
		public int size() {
			return ensureProperties().size();
		}

		@Override
		public Collection<Serializable> values() {
			return ensureProperties().values();
		}
		
		private String getId()
		{
			if(task != null)
			{
				return task.getId();
			}
			else
			{
				return historicTask.getExecutionId();
			}
		}
		
		private Date getDueDate()
		{
			if(task != null)
			{
				return task.getDueDate();
			}
			else
			{
				return historicTask.getDueDate();
			}
		}
		
		private int getPriority()
		{
			if(task != null)
			{
				return task.getPriority();
			}
			else
			{
				return historicTask.getPriority();
			}
		}
		
		private Date getCreated()
		{
			if(task != null)
			{
				return task.getCreateTime();
			}
			else
			{
				return historicTask.getStartTime();
			}
		}
		
		private String getOwner()
		{
			if(task != null)
			{
				return task.getAssignee();
			}
			else
			{
				return historicTask.getAssignee();
			}
		}
	}
}
