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

package org.alfresco.repo.workflow.activiti.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricDetailQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.workflow.WorkflowAuthorityManager;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowNodeConverter;
import org.alfresco.repo.workflow.WorkflowObjectFactory;
import org.alfresco.repo.workflow.WorkflowPropertyHandlerRegistry;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.ActivitiTaskTypeManager;
import org.alfresco.repo.workflow.activiti.ActivitiUtil;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @since 3.4.e
 * @author Nick Smith
 *
 */
public class ActivitiPropertyConverter
{
    private static final String ERR_CONVERT_VALUE = "activiti.engine.convert.value.error";
    private static final String ERR_SET_TASK_PROPS_INVALID_VALUE = "activiti.engine.set.task.properties.invalid.value";
    private static final String ERR_MANDATORY_TASK_PROPERTIES_MISSING = "activiti.engine.mandatory.properties.missing";
    
    private final ActivitiTaskTypeManager typeManager;
    private final MessageService messageService;
    private final WorkflowObjectFactory factory;
    private final WorkflowAuthorityManager authorityManager;
    private final WorkflowPropertyHandlerRegistry handlerRegistry;
    private final WorkflowNodeConverter nodeConverter;
    
    private final ActivitiUtil activitiUtil;
    
    public ActivitiPropertyConverter(ActivitiUtil activitiUtil,
            WorkflowObjectFactory factory,
            WorkflowPropertyHandlerRegistry handlerRegistry,
            WorkflowAuthorityManager authorityManager,
            MessageService messageService,
            WorkflowNodeConverter nodeConverter)
    {
        this.activitiUtil = activitiUtil;
        this.factory = factory;
        this.handlerRegistry = handlerRegistry;
        this.authorityManager = authorityManager;
        this.messageService = messageService;
        this.nodeConverter = nodeConverter;
        this.typeManager = new ActivitiTaskTypeManager(factory, activitiUtil.getFormService());
    }

    public Map<QName, Serializable> getTaskProperties(Task task, boolean localOnly)
    {
        // retrieve type definition for task
        TypeDefinition taskDef = typeManager.getFullTaskDefinition(task);
        Map<QName, PropertyDefinition> taskProperties = taskDef.getProperties();
        Map<QName, AssociationDefinition> taskAssociations = taskDef.getAssociations();
        
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        TaskService taskService = activitiUtil.getTaskService();
        // Get the local task variables
        Map<String, Object> localVariables = taskService.getVariablesLocal(task.getId());
        Map<String, Object> variables = null;
        
        if(!localOnly)
        {
            variables = new HashMap<String, Object>();
            variables.putAll(localVariables);
            
            // Execution-variables should also be added, if no value is present locally
            Map<String, Object> executionVariables = activitiUtil.getExecutionVariables(task.getExecutionId());
            
            for(Entry<String, Object> entry : executionVariables.entrySet())
            {
                if(!localVariables.containsKey(entry.getKey()))
                {
                    variables.put(entry.getKey(), entry.getValue());
                }
            }
        }
        else
        {
            // Only local variables should be used.
            variables = localVariables;
        }
        
        // Map the arbitrary properties
        mapArbitraryProperties(variables, properties, localVariables, taskProperties, taskAssociations);
        
        // Map activiti task instance fields to properties
        properties.put(WorkflowModel.PROP_TASK_ID, task.getId());
        properties.put(WorkflowModel.PROP_DESCRIPTION, task.getDescription());
        // Since the task is never started explicitally, we use the create time
        properties.put(WorkflowModel.PROP_START_DATE, task.getCreateTime());
        
        // Due date is present on the process instance
        properties.put(WorkflowModel.PROP_DUE_DATE, task.getDueDate());
        
        // Since this is a runtime-task, it's not completed yet
        properties.put(WorkflowModel.PROP_COMPLETION_DATE, null);
        properties.put(WorkflowModel.PROP_PRIORITY, task.getPriority());
        properties.put(ContentModel.PROP_CREATED, task.getCreateTime());
        properties.put(ContentModel.PROP_OWNER, task.getAssignee());
        
        // Be sure to fetch the outcome
        String outcomeVarName = factory.mapQNameToName(WorkflowModel.PROP_OUTCOME);
        if(variables.get(outcomeVarName) != null)
        {
             properties.put(WorkflowModel.PROP_OUTCOME, (Serializable) variables.get(outcomeVarName));
        }
        
        List<IdentityLink> links = taskService.getIdentityLinksForTask(task.getId());
        mapPooledActors(links, properties);
        
        return filterTaskProperties(properties);
    }
    
    public Map<QName, Serializable> getPathProperties(String executionId)
    {
        Map<String, Object> variables = activitiUtil.getExecutionVariables(executionId);
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(variables.size());
        for (Entry<String, Object> entry: variables.entrySet())
        {
            QName qNameKey = factory.mapNameToQName(entry.getKey());
            Serializable value = convertPropertyValue(entry.getValue());
            properties.put(qNameKey, value);
        }
        return properties;
    }
    
    public List<NodeRef> getPooledActorsReference(Collection<IdentityLink> links) 
    {
         List<NodeRef> pooledActorRefs = new ArrayList<NodeRef>();
         if(links != null)
         {
             for(IdentityLink link : links)
             {
                 if(IdentityLinkType.CANDIDATE.equals(link.getType()))
                 {
                     String id = link.getGroupId();
                     if(id == null)
                     {
                         id = link.getUserId();
                     }
                     NodeRef pooledNodeRef = authorityManager.mapNameToAuthority(id);
                     if (pooledNodeRef != null)
                     {
                         pooledActorRefs.add(pooledNodeRef);
                     }
                 }
             }
         }
         return pooledActorRefs;
    }
    
    private void mapPooledActors(Collection<IdentityLink> links, Map<QName, Serializable> properties)
    {
        List<NodeRef> pooledActorRefs = getPooledActorsReference(links);
        if (pooledActorRefs != null)
        {
            properties.put(WorkflowModel.ASSOC_POOLED_ACTORS, (Serializable) pooledActorRefs);
        }
    }
    
    public Map<QName, Serializable> getTaskProperties(DelegateTask task, TypeDefinition typeDefinition, boolean localOnly)
    {
        Map<QName, PropertyDefinition> taskProperties = typeDefinition.getProperties();
        Map<QName, AssociationDefinition> taskAssociations = typeDefinition.getAssociations();
        
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        
        // Get the local task variables
        Map<String, Object> localVariables = task.getVariablesLocal();
        Map<String, Object> variables = null;
        
        if(localOnly==false)
        {
            variables = new HashMap<String, Object>();
            variables.putAll(localVariables);
            
            // Execution-variables should also be added, if no value is present locally
            Map<String, Object> executionVariables = task.getExecution().getVariables();
            
            for(Entry<String, Object> entry : executionVariables.entrySet())
            {
                String key = entry.getKey();
                if(localVariables.containsKey(key)==false)
                {
                    variables.put(key, entry.getValue());
                }
            }
        }
        else
        {
            // Only local variables should be used.
            variables = localVariables;
        }
        
        // Map the arbitrary properties
        mapArbitraryProperties(variables, properties, localVariables, taskProperties, taskAssociations);
        
        // Map activiti task instance fields to properties
        properties.put(WorkflowModel.PROP_TASK_ID, task.getId());
        properties.put(WorkflowModel.PROP_DESCRIPTION, task.getDescription());
        // Since the task is never started explicitally, we use the create time
        properties.put(WorkflowModel.PROP_START_DATE, task.getCreateTime());
        
        // Due date
        properties.put(WorkflowModel.PROP_DUE_DATE, task.getDueDate());
        
        // Since this is a runtime-task, it's not completed yet
        properties.put(WorkflowModel.PROP_COMPLETION_DATE, null);
        properties.put(WorkflowModel.PROP_PRIORITY, task.getPriority());
        properties.put(ContentModel.PROP_CREATED, task.getCreateTime());
        properties.put(ContentModel.PROP_OWNER, task.getAssignee());
        
        // TODO: Expose in DelegateTask
        Set<IdentityLink> links = ((TaskEntity)task).getCandidates();
        mapPooledActors(links, properties);
        
        return filterTaskProperties(properties);
    }
    
    @SuppressWarnings("unchecked")
    public Map<QName, Serializable> getTaskProperties(HistoricTaskInstance historicTask, Map<String,Object> variables)
    {
        // Retrieve type definition for task, based on taskFormKey variable
        String formKey = (String) variables.get(ActivitiConstants.PROP_TASK_FORM_KEY);
        TypeDefinition taskDef = typeManager.getFullTaskDefinition(formKey);
        
        Map<QName, PropertyDefinition> taskProperties = taskDef.getProperties();
        Map<QName, AssociationDefinition> taskAssociations = taskDef.getAssociations();
        
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        
        // Map the arbitrary properties
        mapArbitraryProperties(variables, properties, variables, taskProperties, taskAssociations);
        
        // Map activiti task instance fields to properties
        properties.put(WorkflowModel.PROP_TASK_ID, historicTask.getId());
        properties.put(WorkflowModel.PROP_DESCRIPTION, historicTask.getDescription());
        
        // Since the task is never started explicitly, we use the create time
        properties.put(WorkflowModel.PROP_START_DATE, historicTask.getStartTime());
        
        properties.put(WorkflowModel.PROP_DUE_DATE, historicTask.getDueDate());
        properties.put(WorkflowModel.PROP_COMPLETION_DATE, historicTask.getEndTime());
        
        properties.put(WorkflowModel.PROP_PRIORITY, historicTask.getPriority());
        
        properties.put(ContentModel.PROP_CREATED, historicTask.getStartTime());
        properties.put(ContentModel.PROP_OWNER, historicTask.getAssignee());
        
        // Be sure to fetch the outcome
        String outcomeVarName = factory.mapQNameToName(WorkflowModel.PROP_OUTCOME);
        if(variables.get(outcomeVarName) != null)
        {
             properties.put(WorkflowModel.PROP_OUTCOME, (Serializable) variables.get(outcomeVarName));
        }
        
        // History of pooled actors is stored in task variable
        List<NodeRef> pooledActors = new ArrayList<NodeRef>();
        List<String> pooledActorRefIds = (List<String>) variables.get(ActivitiConstants.PROP_POOLED_ACTORS_HISTORY);
        if(pooledActorRefIds != null)
        {
            for(String nodeId : pooledActorRefIds) 
            {
                pooledActors.add(new NodeRef(nodeId));
            }
        }
        // Add pooled actors. When no actors are found, set empty list
            properties.put(WorkflowModel.ASSOC_POOLED_ACTORS, (Serializable) pooledActors);
        
        return filterTaskProperties(properties);
    }
    
//    /**
//     * Sets all default workflow properties that should be set based on the given taskproperties.
//     * When the currentValues already contains a value for a certain key, this value is retained
//     * and the value in taskProperties is ignored.
//     * 
//     * @param startTask
//     *            start task instance
//     */
//    public void setDefaultWorkflowProperties(Map<String, Object> currentValues, Map<QName, Serializable> taskProperties)
//    {
//        if(taskProperties != null)
//        {
//            String workflowDescriptionName = factory.mapQNameToName(WorkflowModel.PROP_WORKFLOW_DESCRIPTION);
//            if (!currentValues.containsKey(workflowDescriptionName))
//            {
//                currentValues.put(workflowDescriptionName, taskProperties
//                            .get(WorkflowModel.PROP_WORKFLOW_DESCRIPTION));
//            }
//            String workflowDueDateName = factory.mapQNameToName(WorkflowModel.PROP_WORKFLOW_DUE_DATE);
//            if (!currentValues.containsKey(workflowDueDateName))
//            {
//                currentValues.put(workflowDueDateName, taskProperties.get(WorkflowModel.PROP_WORKFLOW_DUE_DATE));
//            }
//            String workflowPriorityName = factory.mapQNameToName(WorkflowModel.PROP_WORKFLOW_PRIORITY);
//            if (!currentValues.containsKey(workflowPriorityName))
//            {
//                currentValues.put(workflowPriorityName, taskProperties.get(WorkflowModel.PROP_WORKFLOW_PRIORITY));
//            }
//            String workflowContextName = factory.mapQNameToName(WorkflowModel.PROP_CONTEXT);
//            if (!currentValues.containsKey(workflowContextName))
//            {
//                Serializable contextRef = taskProperties.get(WorkflowModel.PROP_CONTEXT);
//                currentValues.put(workflowContextName, convertNodeRefs(false, contextRef));
//            }
//            String pckgName = factory.mapQNameToName(WorkflowModel.ASSOC_PACKAGE);
//            if (!currentValues.containsKey(pckgName))
//            {
//                Serializable pckgNode = taskProperties.get(WorkflowModel.ASSOC_PACKAGE);
//                currentValues.put(pckgName, convertNodeRefs(false, pckgNode));
//            }
//        }
//    }
    
    /**
     * Sets Default Properties of Task
     * 
     * @param instance
     *            task instance
     */
    public void setDefaultTaskProperties(DelegateTask task)
    {
        TypeDefinition typeDefinition = typeManager.getFullTaskDefinition(task);
        // Only local task properties should be set to default value
        Map<QName, Serializable> existingValues = getTaskProperties(task, typeDefinition, true);
        Map<QName, Serializable> defaultValues = new HashMap<QName, Serializable>();

        Map<QName, PropertyDefinition> propertyDefs = typeDefinition.getProperties();

        // for each property, determine if it has a default value
        for (Map.Entry<QName, PropertyDefinition> entry : propertyDefs.entrySet())
        {
            QName key = entry.getKey();
            String defaultValue = entry.getValue().getDefaultValue();
            if (defaultValue != null && existingValues.get(key) == null)
            {
                defaultValues.put(key, defaultValue);
            }
        }

        // Special case for task description default value
        String description = (String) existingValues.get(WorkflowModel.PROP_DESCRIPTION);
        if (description == null || description.length() == 0)
        {
            // Use the shared description set in the workflowinstance
            String descriptionKey = factory.mapQNameToName(WorkflowModel.PROP_WORKFLOW_DESCRIPTION);
            description = (String) task.getExecution().getVariable(descriptionKey);
            if (description != null && description.length() > 0)
            {
                defaultValues.put(WorkflowModel.PROP_DESCRIPTION, description);
            }
            else
            {
                String processDefinitionKey = ((TaskEntity)task).getExecution().getProcessDefinition().getId();
                
                // Revert to title in metaData
                String title = factory.getTaskTitle(typeDefinition, factory.buildGlobalId(processDefinitionKey), task.getName(), task.getName());
                defaultValues.put(WorkflowModel.PROP_DESCRIPTION, title);
            }
        }

        // Assign the default values to the task
        if (defaultValues.size() > 0)
        {
            setTaskProperties(task, defaultValues);
        }
    }
    
    public Map<QName, Serializable> getStartTaskProperties(HistoricProcessInstance historicProcessInstance, String taskDefId, boolean completed) 
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        
        TypeDefinition taskDef = typeManager.getStartTaskDefinition(taskDefId);
        Map<QName, PropertyDefinition> taskProperties = taskDef.getProperties();
        Map<QName, AssociationDefinition> taskAssociations = taskDef.getAssociations();
        
        Map<String, Object> variables = getStartVariables(historicProcessInstance);
        
        // Map all the properties
        mapArbitraryProperties(variables, properties, variables, taskProperties, taskAssociations);
        
        // Map activiti task instance fields to properties
        properties.put(WorkflowModel.PROP_TASK_ID, ActivitiConstants.START_TASK_PREFIX + historicProcessInstance.getId());
        
        properties.put(WorkflowModel.PROP_START_DATE, historicProcessInstance.getStartTime());
        
        // Use workflow due-date at the time of starting the process
        String wfDueDateKey = factory.mapQNameToName(WorkflowModel.PROP_WORKFLOW_DUE_DATE);
        String dueDateKey = factory.mapQNameToName(WorkflowModel.PROP_DUE_DATE);
        Serializable dueDate = (Serializable) variables.get(wfDueDateKey);
        if(dueDate == null)
        {
            dueDate = (Serializable) variables.get(dueDateKey);
        }
        properties.put(WorkflowModel.PROP_DUE_DATE, dueDate);
        
          // TODO: is it okay to use start-date?
        properties.put(WorkflowModel.PROP_COMPLETION_DATE, historicProcessInstance.getStartTime());
        
        // Use workflow priority at the time of starting the process
        String priorityKey = factory.mapQNameToName(WorkflowModel.PROP_PRIORITY);
        Serializable priority = (Serializable) variables.get(priorityKey);
        if(priority == null)
        {
            String wfPriorityKey = factory.mapQNameToName(WorkflowModel.PROP_WORKFLOW_PRIORITY);
            priority = (Serializable) variables.get(wfPriorityKey);
        }
        properties.put(WorkflowModel.PROP_PRIORITY, priority);
        
        properties.put(ContentModel.PROP_CREATED, historicProcessInstance.getStartTime());
        
        // Use initiator username as owner
        ActivitiScriptNode ownerNode = (ActivitiScriptNode) variables.get(WorkflowConstants.PROP_INITIATOR);
        if(ownerNode != null)
        {
            properties.put(ContentModel.PROP_OWNER, (Serializable) ownerNode.getProperties().get("userName"));            
        }
        
        if(completed)
        {
            // Override default 'Not Yet Started' when start-task is completed
            properties.put(WorkflowModel.PROP_STATUS, WorkflowConstants.TASK_STATUS_COMPLETED);     
            
            // Outcome is default transition
            properties.put(WorkflowModel.PROP_OUTCOME, ActivitiConstants.DEFAULT_TRANSITION_NAME);
        }
        
        return filterTaskProperties(properties);
    }

    /**
     * @param historicProcessInstance
     * @return
     */
    public Map<String, Object> getStartVariables(HistoricProcessInstance historicProcessInstance)
    {
        // Get historic variable values for start-event
        HistoricActivityInstance startEvent = activitiUtil.getHistoryService()
            .createHistoricActivityInstanceQuery()
            .processInstanceId(historicProcessInstance.getId())
            .activityId(historicProcessInstance.getStartActivityId())
            .singleResult();
        
        Map<String, Object> variables = getHistoricActivityVariables(startEvent.getId());
        return variables;
    }

    /**
     * Get all variable updates for process instance, latest updates on top
     * 
     * @param processId
     * @return
     */
    public Map<String, Object> getHistoricProcessVariables(String processId)
    {
        HistoricDetailQuery query = activitiUtil.getHistoryService()
            .createHistoricDetailQuery()
            .processInstanceId(processId);
        return getHistoricVariables(query);
    }
    
    /**
     * Get all variable updates for task instance, latest updates on top
     * 
     * @param taskId
     * @return
     */
    public Map<String, Object> getHistoricTaskVariables(String taskId)
    {
        HistoricDetailQuery query = activitiUtil.getHistoryService()
        .createHistoricDetailQuery()
        .taskId(taskId);
        return getHistoricVariables(query);
    }
    
    /**
     * Get all variable updates for activity, latest updates on top
     * 
     * @param taskId
     * @return
     */
    public Map<String, Object> getHistoricActivityVariables(String activityId)
    {
        HistoricDetailQuery query = activitiUtil.getHistoryService()
        .createHistoricDetailQuery()
        .activityInstanceId(activityId);
        return getHistoricVariables(query);
    }

    private Map<String, Object> getHistoricVariables(HistoricDetailQuery query)
    {
        List<HistoricDetail> historicDetails = query.variableUpdates()
            .orderByVariableName().asc()
            .orderByTime().desc()
            .orderByVariableRevision().desc()
            .list(); 
        return convertHistoricDetails(historicDetails);
    }
    
    private void mapArbitraryProperties(Map<String, Object> variables, Map<QName, Serializable> properties, 
                Map<String, Object> localVariables, Map<QName, PropertyDefinition> taskProperties, Map<QName, AssociationDefinition> taskAssociations) 
    {
        // Map arbitrary task variables
        for (Entry<String, Object> entry : variables.entrySet())
        {
            String key = entry.getKey();
            QName qname = factory.mapNameToQName(key);

            // Add variable, only if part of task definition or locally defined
            // on task
            if (taskProperties.containsKey(qname) || taskAssociations.containsKey(qname) || localVariables.containsKey(key))
            {
                Serializable value = convertPropertyValue(entry.getValue());
                properties.put(qname, value);
            }
        }
    }

    /**
     * Convert an Activiti variable value to an Alfresco value.
     * 
     * @param value
     *            activti value
     * @return alfresco value
     */
    public Serializable convertPropertyValue(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if(nodeConverter.isSupported(value))
        {
            return nodeConverter.convert(value);
        }
        if (value instanceof Serializable)
        {
            return (Serializable) value;
        }
        String msg = messageService.getMessage(ERR_CONVERT_VALUE, value);
        throw new WorkflowException(msg);
    }

    /**
     * Converts a {@link Serializable} value to the type of the specified property. 
     * @param value
     * @param definition
     * @return
     */
    public Serializable convertValueToPropertyType(Task task, Serializable value, QName propertyName)
    {
        TypeDefinition taskDef = typeManager.getFullTaskDefinition(task);
        PropertyDefinition propDef = taskDef.getProperties().get(propertyName);
        if(propDef != null)
        {
            return (Serializable) DefaultTypeConverter.INSTANCE.convert(propDef.getDataType(), value);
        }
        return value;
    }
    
    @SuppressWarnings("unchecked")
    private Map<QName, Serializable> getNewTaskProperties(Task task, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> add,
                Map<QName, List<NodeRef>> remove)
    {
     // create properties to set on task instance
        Map<QName, Serializable> newProperties = properties;

        if (add != null || remove != null)
        {
            if (newProperties == null )
            {
                newProperties = new HashMap<QName, Serializable>(10);
            }

            Map<QName, Serializable> existingProperties = getTaskProperties(task, false);

            if (add != null)
            {
                // add new associations
                for (Entry<QName, List<NodeRef>> toAdd : add.entrySet())
                {
                    // retrieve existing list of noderefs for
                    // association
                    List<NodeRef> existingAdd = (List<NodeRef>) newProperties.get(toAdd.getKey());
                    if (existingAdd == null)
                    {
                        existingAdd = (List<NodeRef>) existingProperties.get(toAdd.getKey());
                    }

                    // make the additions
                    if (existingAdd == null)
                    {
                        newProperties.put(toAdd.getKey(), (Serializable) toAdd.getValue());
                    }
                    else
                    {
                        for (NodeRef nodeRef : toAdd.getValue())
                        {
                            if (!(existingAdd.contains(nodeRef)))
                            {
                                existingAdd.add(nodeRef);
                            }
                        }
                    }
                }
            }

            if (remove != null)
            {
                // add new associations
                for (Entry<QName, List<NodeRef>> toRemove : remove.entrySet())
                {
                    // retrieve existing list of noderefs for
                    // association
                    List<NodeRef> existingRemove = (List<NodeRef>) newProperties.get(toRemove.getKey());
                    if (existingRemove == null)
                    {
                        existingRemove = (List<NodeRef>) existingProperties.get(toRemove.getKey());
                    }

                    // make the subtractions
                    if (existingRemove != null)
                    {
                        for (NodeRef nodeRef : toRemove.getValue())
                        {
                            existingRemove.remove(nodeRef);
                        }
                    }
                }
            }
        }
        return newProperties;
    }
    
    public void setTaskProperties(DelegateTask task, Map<QName, Serializable> properties)
    {
        if(properties==null || properties.isEmpty())
            return;
        TypeDefinition type = typeManager.getFullTaskDefinition(task);
        Map<String, Object> variablesToSet = handlerRegistry.handleVariablesToSet(properties, type, task, DelegateTask.class);
        if(variablesToSet.size() > 0)
        {
            task.setVariablesLocal(variablesToSet);
        }

    }

    /**
     * Sets the properties on the task, using Activiti API.
     */
    public void setTaskProperties(Task task, Map<QName, Serializable> properties)
    {
        if(properties==null || properties.isEmpty())
            return;
        
        TypeDefinition type = typeManager.getFullTaskDefinition(task);
        Map<String, Object> variablesToSet = handlerRegistry.handleVariablesToSet(properties, type, task, Task.class);

        TaskService taskService = activitiUtil.getTaskService();
        // Will be set when an assignee is present in passed properties. 
        taskService.saveTask(task);            

        // Set the collected variables on the task
        taskService.setVariablesLocal(task.getId(), variablesToSet);

        setTaskOwner(task, properties);
    }

    /**
     * @param task
     * @param properties
     */
    private void setTaskOwner(Task task, Map<QName, Serializable> properties)
    {
        QName ownerKey = ContentModel.PROP_OWNER;
        if (properties.containsKey(ownerKey))
        {
            Serializable owner = properties.get(ownerKey);
            if (owner != null && !(owner instanceof String))
            {
                throw getInvalidPropertyValueException(ownerKey, owner);
            }
            String assignee = (String) owner;
            String currentAssignee = task.getAssignee();
            // Only set the assignee if the value has changes to prevent
            // triggering assignementhandlers when not needed
            if (ObjectUtils.equals(currentAssignee, assignee)==false)
            {
                activitiUtil.getTaskService().setAssignee(task.getId(), assignee);
            }
        }
    }
    
    private WorkflowException getInvalidPropertyValueException(QName key, Serializable value)
    {
        String msg = messageService.getMessage(ERR_SET_TASK_PROPS_INVALID_VALUE, value, key);
        return new WorkflowException(msg);
    }

    /**
     * Filter out all internal task-properties.
     * 
     * @param properties
     * @return filtered properties.
     */
    private Map<QName, Serializable> filterTaskProperties(
            Map<QName, Serializable> properties) {
        if(properties != null)
        {
            properties.remove(QName.createQName(null, ActivitiConstants.PROP_POOLED_ACTORS_HISTORY));
            properties.remove(QName.createQName(null, ActivitiConstants.PROP_TASK_FORM_KEY));
        }
        return properties;
    }
    
    /**
     * Convert a list of {@link HistoricDetail} to a map with key-value pairs.
     * @param details the histroicDetails. Should be a list of {@link HistoricVariableUpdate}s.
     */
    public Map<String, Object> convertHistoricDetails(List<HistoricDetail> details)
    {
        // TODO: Sorting should be performed in query, not available right now.
        Collections.sort(details, new Comparator<HistoricDetail>()
        {
            @Override
            public int compare(HistoricDetail o1, HistoricDetail o2)
            {
                Long id1 = Long.valueOf(o1.getId());
                Long id2 = Long.valueOf(o2.getId());
                return - id1.compareTo(id2);
            }
        });
        Map<String, Object> variables = new HashMap<String, Object>();
        for(HistoricDetail detail : details)
        {
            HistoricVariableUpdate varUpdate = (HistoricVariableUpdate) detail;
            // First value for a single key is used
            if(!variables.containsKey(varUpdate.getVariableName()))
            {
                variables.put(varUpdate.getVariableName(), varUpdate.getValue());                
            }
        }
        return variables;
    }
    

    public Map<String, Object> getStartVariables(String processDefId, Map<QName, Serializable> properties)
    {
        ProcessDefinition procDef = activitiUtil.getProcessDefinition(processDefId);
        String startTaskTypeName = activitiUtil.getStartTaskTypeName(processDefId);
        TypeDefinition startTaskType  = factory.getTaskFullTypeDefinition(startTaskTypeName, true);
        
        // Lookup type definition for the startTask
        Map<QName, PropertyDefinition> taskProperties = startTaskType.getProperties();
        
        // Get all default values from the definitions
        Map<QName, Serializable> defaultProperties = new HashMap<QName, Serializable>();
        for (Map.Entry<QName, PropertyDefinition> entry : taskProperties.entrySet())
        {
            String defaultValue = entry.getValue().getDefaultValue();
            if (defaultValue != null)
            {
                defaultProperties.put(entry.getKey(), defaultValue);
            }
        }
        
        // Put all passed properties in map with defaults
        if(properties != null)
        {
            defaultProperties.putAll(properties);
        }
        
        // Special case for task description default value
        // Use the shared description set in the workflowinstance
        String description = (String) defaultProperties.get(WorkflowModel.PROP_DESCRIPTION);
        if(description == null)
        {
            String wfDescription = (String) defaultProperties.get(WorkflowModel.PROP_WORKFLOW_DESCRIPTION);
            String procDefKey = procDef.getKey();
            ReadOnlyProcessDefinition deployedDef = activitiUtil.getDeployedProcessDefinition(processDefId);
            String startEventName = deployedDef.getInitial().getId();
            String wfDefKey = factory.buildGlobalId(procDefKey);
            factory.getTaskDescription(startTaskType, wfDefKey, wfDescription, startEventName);
            defaultProperties.put(WorkflowModel.PROP_DESCRIPTION, description);
        }
        return handlerRegistry.handleVariablesToSet(defaultProperties, startTaskType, null, Void.class);
    }
    
    public void checkMandatoryProperties(DelegateTask task)
    {
         // Check all mandatory properties are set. This is checked here instead of in
         // the completeTask() to allow taskListeners to set variable values before checking
         List<QName> missingProps = getMissingMandatoryTaskProperties(task);
         if (missingProps != null && missingProps.size() > 0)
         {
             String missingPropString = StringUtils.join(missingProps.iterator(), ", ");
             throw new WorkflowException(messageService.getMessage(ERR_MANDATORY_TASK_PROPERTIES_MISSING, missingPropString));
         }
    }
    
    /**
     * Get missing mandatory properties on Task
     * 
     * @param task
     *            task instance
     * @return array of missing property names (or null, if none)
     */
    private List<QName> getMissingMandatoryTaskProperties(DelegateTask task)
    {

        TypeDefinition typeDefinition = typeManager.getFullTaskDefinition(task);
        // retrieve properties of task
        Map<QName, Serializable> existingValues = getTaskProperties(task, typeDefinition, false);

        // retrieve definition of task
        Map<QName, PropertyDefinition> propertyDefs = typeDefinition.getProperties();
        Map<QName, AssociationDefinition> assocDefs = typeDefinition.getAssociations();

        List<QName> missingProps = findMissingProperties(existingValues, propertyDefs);
        List<QName> missingAssocs= findMissingProperties(existingValues, assocDefs);
        missingProps.addAll(missingAssocs);
        return missingProps;
    }

    private List<QName> findMissingProperties(Map<QName, Serializable> existingValues,
                Map<QName, ? extends ClassAttributeDefinition> definitions)
    {
        // for each property, determine if it is mandatory
        List<QName> missingProps = new ArrayList<QName>();
        for (Map.Entry<QName, ? extends ClassAttributeDefinition> entry : definitions.entrySet())
        {
            QName name = entry.getKey();
            // Skip System and CM properties. Why?
            if ((name.getNamespaceURI().equals(NamespaceService.CONTENT_MODEL_1_0_URI) 
                        || (name.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI))))
            {
                continue;
            }
            if (isMandatory(entry.getValue()))
            {
                Object value = existingValues.get(entry.getKey());
                if (value == null || isEmptyString(value))
                {
                    missingProps.add(entry.getKey());
                }
            }
        }
        return missingProps;
    }

    private boolean isMandatory(ClassAttributeDefinition definition)
    {
        if(definition instanceof PropertyDefinition)
        {
            PropertyDefinition propDef = (PropertyDefinition) definition;
            return propDef.isMandatory();
        }
        AssociationDefinition assocDSef = (AssociationDefinition) definition;
        return assocDSef.isTargetMandatory();
    }

    /**
     * @param value
     * @return
     */
    private boolean isEmptyString(Object value)
    {
        if(value instanceof String)
        {
            String str = (String)value;
            return str.isEmpty();
        }
        return false;
    }

    public Task updateTask(Task task, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> add,
            Map<QName, List<NodeRef>> remove)
    {
        Map<QName, Serializable> newProperties = getNewTaskProperties(task, properties, add, remove);
        if (newProperties != null)
        {
            setTaskProperties(task, newProperties);
            return activitiUtil.getTaskInstance(task.getId());
        }
        return task;
    }

}