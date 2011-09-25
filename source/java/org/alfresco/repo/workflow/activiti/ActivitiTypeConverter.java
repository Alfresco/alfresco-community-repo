/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.repo.workflow.activiti;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.alfresco.repo.workflow.WorkflowObjectFactory;
import org.alfresco.repo.workflow.activiti.properties.ActivitiPropertyConverter;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.collections.Function;

/**
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class ActivitiTypeConverter
{
    /**
     * Default transition provided for all Nodes when using Activiti engine.
     */
    private static final WorkflowTransition NEXT_TRANSITION = new WorkflowTransition(ActivitiConstants.DEFAULT_TRANSITION_NAME, 
                ActivitiConstants.DEFAULT_TRANSITION_NAME, "Default Transition", true);
    
    private final RepositoryService repoService;
    private final RuntimeService runtimeService;
    private final FormService formService;
    private final HistoryService historyService;
    private final ActivitiPropertyConverter propertyConverter;
    private final WorkflowObjectFactory factory;

    private final ActivitiUtil activitiUtil;
    
    public ActivitiTypeConverter(ProcessEngine processEngine, 
                WorkflowObjectFactory factory,
                ActivitiPropertyConverter propertyConverter)
    {
        this.repoService = processEngine.getRepositoryService();
        this.runtimeService = processEngine.getRuntimeService();
        this.formService = processEngine.getFormService();
        this.historyService = processEngine.getHistoryService();
        this.factory = factory;
        this.propertyConverter =propertyConverter;
        this.activitiUtil = new ActivitiUtil(processEngine);
    }
    
    public <F, T> List<T> filterByDomainAndConvert(List<F> values, Function<F, String> processKeyGetter)
    {
        List<F> filtered = factory.filterByDomain(values, processKeyGetter);
        return convert(filtered);
    }

    /**
     * Convert a {@link Deployment} into a {@link WorkflowDeployment}.
     * @param deployment
     * @return
     */
    public WorkflowDeployment convert(Deployment deployment)
    {
        if(deployment == null)
            return null;
        
        List<ProcessDefinition> processDefs = repoService.createProcessDefinitionQuery()
            .deploymentId(deployment.getId())
            .list();
        ProcessDefinition processDef = processDefs.get(0);
        WorkflowDefinition wfDef = convert(processDef);
        return factory.createDeployment(wfDef);
    }

    /**
     * Convert a {@link ProcessDefinition} into a {@link WorkflowDefinition}.
     * @param processDef
     * @return
     */
    public WorkflowDefinition convert(ProcessDefinition definition)
    {
        if(definition==null)
            return null;
        
        String defId = definition.getId();
        String defName = definition.getKey();
        int version = definition.getVersion();
        String defaultTitle = definition.getName();
        
        String startTaskName = null;
        StartFormData startFormData = formService.getStartFormData(definition.getId());
        if(startFormData != null) 
        {
            startTaskName = startFormData.getFormKey();
        }
        
        ReadOnlyProcessDefinition def = activitiUtil.getDeployedProcessDefinition(defId);
        PvmActivity startEvent = def.getInitial();
        WorkflowTaskDefinition startTask = getTaskDefinition(startEvent, startTaskName, definition.getKey());
        
        return factory.createDefinition(defId,
                    defName, version, defaultTitle,
                    null, startTask);
    }
    
    public WorkflowTaskDefinition getTaskDefinition(PvmActivity activity, String taskFormKey, String processDefinitionName)
    {
        String startId = activity.getId();
        String startTitle = (String) activity.getProperty(ActivitiConstants.NODE_NAME);
        String startDescription= (String) activity.getProperty(ActivitiConstants.NODE_DESCRIPTION);
        String startType = (String) activity.getProperty(ActivitiConstants.NODE_TYPE);
        if(taskFormKey == null)
        {
            taskFormKey = startId;
        }
        WorkflowNode node = factory.createNode(startId, processDefinitionName, startTitle, startDescription, startType, true, NEXT_TRANSITION);
        WorkflowTaskDefinition startTask = factory.createTaskDefinition(taskFormKey, node, taskFormKey, true);
        return startTask;
    }

    public WorkflowInstance convert(ProcessInstance instance)
    {
       return convertAndSetVariables(instance, (Map<String, Object>) null);
    }
    
    public WorkflowInstance convertAndSetVariables(ProcessInstance instance, Map<String, Object> collectedvariables)
    {
        if(instance == null)
            return null;
        
        HistoricProcessInstance historicInstance = historyService
        	.createHistoricProcessInstanceQuery()
        	.processInstanceId(instance.getId())
        	.singleResult();
        
       return convertToInstanceAndSetVariables(historicInstance, collectedvariables);
    }

    public WorkflowInstance convert(HistoricProcessInstance instance, Map<String, Object> collectedvariables)
    {
        if(instance == null)
            return null;
        
        HistoricProcessInstance historicInstance = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(instance.getId())
        .singleResult();
        
        return convertToInstanceAndSetVariables(historicInstance, collectedvariables);
    }

    public WorkflowPath convert(Execution execution)
    {
        String instanceId = execution.getProcessInstanceId();
        ProcessInstance instance = activitiUtil.getProcessInstance(instanceId);
        return convert(execution, instance);
    }

    public WorkflowPath convert(Execution execution, ProcessInstance instance)
    {
        if(execution == null)
            return null;
        
        boolean isActive = !execution.isEnded();
        
        // Convert workflow and collect variables
        Map<String, Object> workflowInstanceVariables = new HashMap<String, Object>();
        WorkflowInstance wfInstance = convertAndSetVariables(instance, workflowInstanceVariables);
        
        WorkflowNode node = null;
        // Get active node on execution
        List<String> nodeIds = runtimeService.getActiveActivityIds(execution.getId());

        if (nodeIds != null && nodeIds.size() >= 1)
        {
            ReadOnlyProcessDefinition procDef = activitiUtil.getDeployedProcessDefinition(instance.getProcessDefinitionId());
            PvmActivity activity = procDef.findActivity(nodeIds.get(0));
            node = convert(activity);
        }

        return factory.createPath(execution.getId(), wfInstance, node, isActive);
    }
    
    public WorkflowNode convert(PvmActivity activity, boolean forceIsTaskNode)
    {
    	 String procDefId = activity.getProcessDefinition().getId();
         String key = activitiUtil.getProcessDefinition(procDefId).getKey();
         String name = activity.getId();
         String defaultTitle = (String) activity.getProperty(ActivitiConstants.NODE_NAME);
         String defaultDescription = (String) activity.getProperty(ActivitiConstants.NODE_DESCRIPTION);
         String type = (String) activity.getProperty(ActivitiConstants.NODE_TYPE);
         boolean isTaskNode = forceIsTaskNode || ActivitiConstants.USER_TASK_NODE_TYPE.equals(type);
         
         if(defaultTitle == null)
         {
         	defaultTitle = name;
         }
         if(defaultDescription == null)
         {
         	defaultDescription = name;
         }

        return factory.createNode(name, key, defaultTitle, defaultDescription, type, isTaskNode, NEXT_TRANSITION);
    }
    
    public WorkflowNode convert(PvmActivity activity)
    {
       return convert(activity, false);
    }

    public List<WorkflowPath> convertExecution(List<Execution> executions)
    {
        ArrayList<WorkflowPath> results = new ArrayList<WorkflowPath>(executions.size());
        for (Execution execution : executions)
        {
            results.add(convert(execution));
        }
        return results;
    }
    
    @SuppressWarnings("unchecked")
    public <T> List<T> convert(List<?> inputs)
    {
        ArrayList<T> results = new ArrayList<T>(inputs.size());
        for (Object in : inputs)
        {
            T out = (T) convert(in);
            if(out != null)
            {
                results.add(out);
            }
        }
        return results;
    }
    
    /**
     * Converts an Activiti object to an Alresco Workflow object.
     * Determines the exact conversion method to use by checking the class of object.
     * @param obj The object to be converted.
     * @return the converted object.
     */
    private Object convert(Object obj)
    {
        if(obj == null)
            return null;
        
        if (obj instanceof Deployment)
        {
            return convert( (Deployment) obj);
        }
        if (obj instanceof ProcessDefinition)
        {
            return convert( (ProcessDefinition) obj);
        }
        if (obj instanceof ProcessInstance)
        {
            return convert( (ProcessInstance) obj);
        }
        if (obj instanceof Execution)
        {
            return convert( (Execution) obj);
        }
        if (obj instanceof ActivityImpl)
        {
            return convert( (ActivityImpl) obj);
        }
        if (obj instanceof Task)
        {
            return convert( (Task) obj);
        }
        if(obj instanceof HistoricTaskInstance) 
        {
            return convert((HistoricTaskInstance) obj);
        }
        if(obj instanceof HistoricProcessInstance) 
        {
            return convert((HistoricProcessInstance) obj);
        }
        throw new WorkflowException("Cannot convert object: " + obj + " of type: " + obj.getClass());
    }
    
    
    public WorkflowTask convert(Task task)
    {
        if(task == null)
            return null;
        String id = task.getId();
        String defaultTitle = task.getName();
        String defaultDescription = task.getDescription();
        
        WorkflowTaskState state = WorkflowTaskState.IN_PROGRESS;
        Execution execution = activitiUtil.getExecution(task.getExecutionId());
        WorkflowPath path  = convert(execution);
        
        // Since the task is active, it's safe to use the active node on
        // the execution path
        WorkflowNode node = path.getNode();
        
        
        TaskFormData taskFormData =formService.getTaskFormData(task.getId());
        String taskDefId = null;
        if(taskFormData != null) 
        {
            taskDefId = taskFormData.getFormKey();
        }
        WorkflowTaskDefinition taskDef = factory.createTaskDefinition(taskDefId, node, taskDefId, false);
        
        // All task-properties should be fetched, not only local
        Map<QName, Serializable> properties = propertyConverter.getTaskProperties(task);
        
        return factory.createTask(id,
                    taskDef, taskDef.getId(), defaultTitle, defaultDescription, state, path, properties);
    }
    
    public WorkflowTask getVirtualStartTask(String processInstanceId, Boolean inProgress)
    {
        ProcessInstance processInstance = activitiUtil.getProcessInstance(processInstanceId);
        if(processInstance != null)
        {
            if(null == inProgress)
            {
                inProgress = isStartTaskActive(processInstanceId);
            }
            return getVirtualStartTask(processInstance, inProgress);
        }
        HistoricProcessInstance historicProcessInstance = activitiUtil.getHistoricProcessInstance(processInstanceId);
        return getVirtualStartTask(historicProcessInstance);
    }

    public boolean isStartTaskActive(String processInstanceId)
    {
        Object endDate = runtimeService.getVariable(processInstanceId, ActivitiConstants.PROP_START_TASK_END_DATE);
        return endDate == null;
    }

    private WorkflowTask getVirtualStartTask(ProcessInstance processInstance, boolean inProgress)
    {
        String processInstanceId = processInstance.getId();
        String id = ActivitiConstants.START_TASK_PREFIX + processInstanceId;
        
        WorkflowTaskState state = null;
        if(inProgress)
        {
            state = WorkflowTaskState.IN_PROGRESS;
        }
        else
        {
            state = WorkflowTaskState.COMPLETED;
        }
        
        WorkflowPath path  = convert((Execution)processInstance);
        
        // Convert start-event to start-task Node
        ReadOnlyProcessDefinition procDef = activitiUtil.getDeployedProcessDefinition(processInstance.getProcessDefinitionId());
        WorkflowNode startNode = convert(procDef.getInitial(), true);
        
        StartFormData startFormData = formService.getStartFormData(processInstance.getProcessDefinitionId());
        String taskDefId = null;
        if(startFormData != null) 
        {
            taskDefId = startFormData.getFormKey();
        }
        WorkflowTaskDefinition taskDef = factory.createTaskDefinition(taskDefId, startNode, taskDefId, true);
        
        // Add properties based on HistoricProcessInstance
        HistoricProcessInstance historicProcessInstance = historyService
            .createHistoricProcessInstanceQuery()
            .processInstanceId(processInstance.getId())
            .singleResult();
        
        Map<QName, Serializable> properties = propertyConverter.getStartTaskProperties(historicProcessInstance, taskDefId, !inProgress);
        
        // TODO: Figure out what name/description should be used for the start-task, start event's name?
        String defaultTitle = null;
        String defaultDescription = null;
        
        return factory.createTask(id,
                    taskDef, taskDef.getId(), defaultTitle, defaultDescription, state, path, properties);
    }
    
    private WorkflowTask getVirtualStartTask(HistoricProcessInstance historicProcessInstance)
    {
        if(historicProcessInstance == null)
        {
            return null;
        }
        
        String processInstanceId = historicProcessInstance.getId();
        String id = ActivitiConstants.START_TASK_PREFIX + processInstanceId;
        
        // Since the process instance is complete the Start Task must be complete!
        WorkflowTaskState state = WorkflowTaskState.COMPLETED;

        // We use the process-instance ID as execution-id. It's ended anyway
        WorkflowPath path  = buildCompletedPath(processInstanceId, processInstanceId);
        if(path == null)
        {
            return null;
        }
        
        // Convert start-event to start-task Node
        ReadOnlyProcessDefinition procDef = activitiUtil.getDeployedProcessDefinition(historicProcessInstance.getProcessDefinitionId());
        WorkflowNode startNode = convert(procDef.getInitial(), true);
        
        String taskDefId = activitiUtil.getStartFormKey(historicProcessInstance.getProcessDefinitionId());
        WorkflowTaskDefinition taskDef = factory.createTaskDefinition(taskDefId, startNode, taskDefId, true);
        
        boolean completed = historicProcessInstance.getEndTime() != null;
        Map<QName, Serializable> properties = propertyConverter.getStartTaskProperties(historicProcessInstance, taskDefId, completed);
        
        // TODO: Figure out what name/description should be used for the start-task, start event's name?
        String defaultTitle = null;
        String defaultDescription = null;
        
        return factory.createTask(id,
                    taskDef, taskDef.getId(), defaultTitle, defaultDescription, state, path, properties);
    }
    
    public WorkflowTask convert(HistoricTaskInstance historicTaskInstance) 
    {
        if(historicTaskInstance == null) 
        {
            return null;
        }
        WorkflowPath path = null;
        // Check to see if the instance is still running
        Execution execution = activitiUtil.getExecution(historicTaskInstance.getExecutionId());
        
        if(execution != null)
        {
            // Process execution still running
            path  = convert(execution);
        }
        else
        {
            // Process execution is historic
            path  = buildCompletedPath(historicTaskInstance.getExecutionId(), historicTaskInstance.getProcessInstanceId());
        }
        
        if(path == null)
        {
            // When path is null, workflow is deleted or cancelled. Task should
            // not be used
            return null;
        }
        
        // Extract node from historic task
        WorkflowNode node = buildHistoricTaskWorkflowNode(historicTaskInstance);
        
        WorkflowTaskState state= WorkflowTaskState.COMPLETED;

        String taskId = historicTaskInstance.getId();
        // Get the local task variables from the history
        Map<String, Object> variables = propertyConverter.getHistoricTaskVariables(taskId);
        Map<QName, Serializable> historicTaskProperties = propertyConverter.getTaskProperties(historicTaskInstance, variables);
        
        // Get task definition from historic variable 
        String formKey = (String) variables.get(ActivitiConstants.PROP_TASK_FORM_KEY);
        WorkflowTaskDefinition taskDef = factory.createTaskDefinition(formKey, node, formKey, false);
        String title = historicTaskInstance.getName();
        String description = historicTaskInstance.getDescription();
        String taskName = taskDef.getId();

        return factory.createTask(taskId, taskDef, taskName, 
                    title, description, state, path, historicTaskProperties);
    }

    private WorkflowNode buildHistoricTaskWorkflowNode(HistoricTaskInstance historicTaskInstance) 
    {
    	ReadOnlyProcessDefinition procDef = activitiUtil.getDeployedProcessDefinition(historicTaskInstance.getProcessDefinitionId());
    	PvmActivity taskActivity = procDef.findActivity(historicTaskInstance.getTaskDefinitionKey());
		return convert(taskActivity);
	}

	public WorkflowPath buildCompletedPath(String executionId, String processInstanceId)
    {
        WorkflowInstance wfInstance = null;
        ProcessInstance processInstance = activitiUtil.getProcessInstance(processInstanceId);
        if(processInstance != null)
        {
            wfInstance = convert(processInstance);
        } 
        else
        {
            HistoricProcessInstance historicProcessInstance = activitiUtil.getHistoricProcessInstance(processInstanceId);
            if(historicProcessInstance!= null)
                wfInstance = convert(historicProcessInstance);
        }
        if(wfInstance == null)
        {
        	// When workflow is cancelled or deleted, WorkflowPath should not be returned
        	return null;
        }
        WorkflowNode node = null;
        return factory.createPath(executionId, wfInstance, node, false);
    }

    public WorkflowInstance convertToInstanceAndSetVariables(HistoricProcessInstance historicProcessInstance, Map<String, Object> collectedVariables)
    {
        String processInstanceId = historicProcessInstance.getId();
        String id = processInstanceId;
        ProcessDefinition procDef = activitiUtil.getProcessDefinition(historicProcessInstance.getProcessDefinitionId());
        WorkflowDefinition definition = convert(procDef);
        
        // Set process variables based on historic detail query
        Map<String, Object> variables = propertyConverter.getHistoricProcessVariables(processInstanceId);
        
        Date startDate = historicProcessInstance.getStartTime();
        Date endDate = historicProcessInstance.getEndTime();

        // Copy all variables to map, if not null
        if(collectedVariables != null)
        {
        	collectedVariables.putAll(variables);
        }
        boolean isActive = endDate == null;
        return factory.createInstance(id, definition, variables, isActive, startDate, endDate);
    }
    
    public WorkflowInstance convert(HistoricProcessInstance historicProcessInstance)
    {
    	return convertToInstanceAndSetVariables(historicProcessInstance, null);
    }

}
