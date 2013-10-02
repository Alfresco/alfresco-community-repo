/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.workflow.api.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ActivitiTaskAlreadyClaimedException;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.TaskQuery;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowObjectFactory;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.Tasks;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker.QueryVariableHolder;
import org.alfresco.rest.workflow.api.model.FormModelElement;
import org.alfresco.rest.workflow.api.model.Item;
import org.alfresco.rest.workflow.api.model.Task;
import org.alfresco.rest.workflow.api.model.TaskCandidate;
import org.alfresco.rest.workflow.api.model.TaskStateTransition;
import org.alfresco.rest.workflow.api.model.TaskVariable;
import org.alfresco.rest.workflow.api.model.VariableScope;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;

public class TasksImpl extends WorkflowRestImpl implements Tasks
{
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_ANY = "any";
    private static final String STATUS_COMPLETED = "completed";
    /**
     * All properties that are read-only and cannot be updated on a single task-resource.
     */
    private static final List<String> TASK_READ_ONLY_PROPERTIES = Arrays.asList(
        "id", "processId", "processDefinitionId", "activityDefinitionId", "startedAt", "endedAt", "durationInMs", "formResourceKey"
    );
    
    private static final Set<String> TASK_COLLECTION_EQUALS_QUERY_PROPERTIES = new HashSet<String>(Arrays.asList(
        "status", "assignee", "owner", "candidateUser", "candidateGroup", "name", "description", "priority", "processId",
        "processBusinessKey", "activityDefinitionId", "processDefinitionId", "processDefinitionKey", "processDefinitionName", "startedAt", 
        "endedAt", "dueAt", "includeTaskVariables", "includeProcessVariables"
    ));
    
    private static final Set<String> TASK_COLLECTION_MATCHES_QUERY_PROPERTIES = new HashSet<String>(Arrays.asList(
        "assignee", "owner", "name", "description", "processBusinessKey", "activityDefinitionId", "processDefinitionKey", "processDefinitionName"
    ));
    
    private static final Set<String> TASK_COLLECTION_GREATERTHAN_QUERY_PROPERTIES = new HashSet<String>(Arrays.asList(
        "startedAt", "endedAt", "dueAt"
    ));
    
    private static final Set<String> TASK_COLLECTION_GREATERTHANOREQUAL_QUERY_PROPERTIES = new HashSet<String>(Arrays.asList(
        "priority"
    ));
    
    private static final Set<String> TASK_COLLECTION_LESSTHAN_QUERY_PROPERTIES = new HashSet<String>(Arrays.asList(
        "startedAt", "endedAt", "dueAt"
    ));
    
    private static final Set<String> TASK_COLLECTION_LESSTHANOREQUAL_QUERY_PROPERTIES = new HashSet<String>(Arrays.asList(
        "priority"
    ));
    
    private static final Set<String> TASK_COLLECTION_RUNNING_SORT_PROPERTIES = new HashSet<String>(Arrays.asList(
        "id", "name", "description", "priority", "processId", "assignee", "startedAt", "dueAt"
    ));
    
    private static final Set<String> TASK_COLLECTION_HISTORY_SORT_PROPERTIES = new HashSet<String>(Arrays.asList(
        "id", "name", "description", "priority", "processId", "processDefinitionId", "assignee", "owner", "startedAt", "endedAt", "durationInMs", "dueAt"
    ));
    
    private RestVariableHelper restVariableHelper;
    private WorkflowObjectFactory workflowFactory;
    private WorkflowQNameConverter qNameConverter;
    private MessageService messageService;
    private PersonService personService;
    
    public void setRestVariableHelper(RestVariableHelper restVariableHelper)
    {
        this.restVariableHelper = restVariableHelper;
    }
    
    public void setMessageService(MessageService messageService)
    {
        this.messageService = messageService;
    }
    
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    @Override
    public CollectionWithPagingInfo<Task> getTasks(Parameters parameters)
    {
        Paging paging = parameters.getPaging();
        MapBasedQueryWalker propertyWalker = new MapBasedQueryWalker(TASK_COLLECTION_EQUALS_QUERY_PROPERTIES, 
                TASK_COLLECTION_MATCHES_QUERY_PROPERTIES);
        
        propertyWalker.setSupportedGreaterThanParameters(TASK_COLLECTION_GREATERTHAN_QUERY_PROPERTIES);
        propertyWalker.setSupportedGreaterThanOrEqualParameters(TASK_COLLECTION_GREATERTHANOREQUAL_QUERY_PROPERTIES);
        propertyWalker.setSupportedLessThanParameters(TASK_COLLECTION_LESSTHAN_QUERY_PROPERTIES);
        propertyWalker.setSupportedLessThanOrEqualParameters(TASK_COLLECTION_LESSTHANOREQUAL_QUERY_PROPERTIES);
        propertyWalker.enableVariablesSupport(namespaceService, dictionaryService);
        
        if (parameters.getQuery() != null)
        {
            QueryHelper.walk(parameters.getQuery(), propertyWalker);
        }
        
        String status = propertyWalker.getProperty("status", WhereClauseParser.EQUALS);
        String assignee = propertyWalker.getProperty("assignee", WhereClauseParser.EQUALS);
        String assigneeLike = propertyWalker.getProperty("assignee", WhereClauseParser.MATCHES);
        String owner = propertyWalker.getProperty("owner", WhereClauseParser.EQUALS);
        String ownerLike = propertyWalker.getProperty("owner", WhereClauseParser.MATCHES);
        String candidateUser = propertyWalker.getProperty("candidateUser", WhereClauseParser.EQUALS);
        String candidateGroup = propertyWalker.getProperty("candidateGroup", WhereClauseParser.EQUALS);
        String name = propertyWalker.getProperty("name", WhereClauseParser.EQUALS);
        String nameLike = propertyWalker.getProperty("name", WhereClauseParser.MATCHES);
        String description = propertyWalker.getProperty("description", WhereClauseParser.EQUALS);
        String descriptionLike = propertyWalker.getProperty("description", WhereClauseParser.MATCHES);
        Integer priority = propertyWalker.getProperty("priority", WhereClauseParser.EQUALS, Integer.class);
        Integer priorityGreaterThanOrEquals = propertyWalker.getProperty("priority", WhereClauseParser.GREATERTHANOREQUALS, Integer.class);
        Integer priorityLessThanOrEquals = propertyWalker.getProperty("priority", WhereClauseParser.LESSTHANOREQUALS, Integer.class);
        String processInstanceId = propertyWalker.getProperty("processId", WhereClauseParser.EQUALS);
        String processInstanceBusinessKey = propertyWalker.getProperty("processBusinessKey", WhereClauseParser.EQUALS);
        String processInstanceBusinessKeyLike = propertyWalker.getProperty("processBusinessKey", WhereClauseParser.MATCHES);
        String activityDefinitionId = propertyWalker.getProperty("activityDefinitionId", WhereClauseParser.EQUALS);
        String activityDefinitionIdLike = propertyWalker.getProperty("activityDefinitionId", WhereClauseParser.MATCHES);
        String processDefinitionId = propertyWalker.getProperty("processDefinitionId", WhereClauseParser.EQUALS);
        String processDefinitionKey = propertyWalker.getProperty("processDefinitionKey", WhereClauseParser.EQUALS);
        String processDefinitionKeyLike = propertyWalker.getProperty("processDefinitionKey", WhereClauseParser.MATCHES);
        String processDefinitionName = propertyWalker.getProperty("processDefinitionName", WhereClauseParser.EQUALS);
        String processDefinitionNameLike = propertyWalker.getProperty("processDefinitionName", WhereClauseParser.MATCHES);
        Date startedAt = propertyWalker.getProperty("startedAt", WhereClauseParser.EQUALS, Date.class);
        Date startedAtGreaterThan = propertyWalker.getProperty("startedAt", WhereClauseParser.GREATERTHAN, Date.class);
        Date startedAtLessThan = propertyWalker.getProperty("startedAt", WhereClauseParser.LESSTHAN, Date.class);
        Date endedAt = propertyWalker.getProperty("endedAt", WhereClauseParser.EQUALS, Date.class);
        Date endedAtGreaterThan = propertyWalker.getProperty("endedAt", WhereClauseParser.GREATERTHAN, Date.class);
        Date endedAtLessThan = propertyWalker.getProperty("endedAt", WhereClauseParser.LESSTHAN, Date.class);
        Date dueAt = propertyWalker.getProperty("dueAt", WhereClauseParser.EQUALS, Date.class);
        Date dueAtGreaterThan = propertyWalker.getProperty("dueAt", WhereClauseParser.GREATERTHAN, Date.class);
        Date dueAtLessThan = propertyWalker.getProperty("dueAt", WhereClauseParser.LESSTHAN, Date.class);
        Boolean includeProcessVariables = propertyWalker.getProperty("includeProcessVariables", WhereClauseParser.EQUALS, Boolean.class);
        Boolean includeTaskVariables = propertyWalker.getProperty("includeTaskVariables", WhereClauseParser.EQUALS, Boolean.class);

        List<SortColumn> sortList = parameters.getSorting();
        SortColumn sortColumn = null;
        if (sortList != null && sortList.size() > 0)
        {
            if (sortList.size() != 1)
            {
                throw new InvalidArgumentException("Only one order by parameter is supported");
            }
            sortColumn = sortList.get(0);
        }
        
        List<Task> page = null;
        int totalCount = 0;
        if (status == null || STATUS_ACTIVE.equals(status))
        {
            TaskQuery query = activitiProcessEngine
                    .getTaskService()
                    .createTaskQuery();
            
            if (assignee != null) query.taskAssignee(assignee);
            if (assigneeLike != null) query.taskAssigneeLike(assigneeLike);
            if (owner != null) query.taskOwner(owner);
            if (ownerLike != null) query.taskOwner(ownerLike);
            if (candidateUser != null)
            {
            	Set<String> parents = authorityService.getContainingAuthorities(AuthorityType.GROUP, candidateUser, false);
            	if (parents != null)
            	{
            		List<String> authorities = new ArrayList<String>();
            		authorities.addAll(parents);
            		
            		// there's a limitation in at least Oracle for using an IN statement with more than 1000 items
            		if (parents.size() > 1000)
            		{
            			authorities = authorities.subList(0, 1000);
            		}
            		
            		if (authorities.size() > 0)
            		{
            		    query.taskCandidateGroupIn(authorities);
            		}
            		else
            		{
            		    query.taskCandidateUser(candidateUser);
            		}
            	}
            }
            if (candidateGroup != null) query.taskCandidateGroup(candidateGroup);
            if (name != null) query.taskName(name);
            if (nameLike != null) query.taskNameLike(nameLike);
            if (description != null) query.taskDescription(description);
            if (descriptionLike != null) query.taskDescriptionLike(descriptionLike);
            if (priority != null) query.taskPriority(priority);
            if (priorityGreaterThanOrEquals != null) query.taskMinPriority(priorityGreaterThanOrEquals);
            if (priorityLessThanOrEquals != null) query.taskMaxPriority(priorityLessThanOrEquals);
            if (processInstanceId != null) query.processInstanceId(processInstanceId);
            if (processInstanceBusinessKey != null) query.processInstanceBusinessKey(processInstanceBusinessKey);
            if (processInstanceBusinessKeyLike != null) query.processInstanceBusinessKeyLike(processInstanceBusinessKeyLike);
            if (activityDefinitionId != null) query.taskDefinitionKey(activityDefinitionId);
            if (activityDefinitionIdLike != null) query.taskDefinitionKey(activityDefinitionIdLike);
            if (processDefinitionId != null) query.processDefinitionId(processDefinitionId);
            if (processDefinitionKey != null) query.processDefinitionKey(processDefinitionKey);
            if (processDefinitionKeyLike != null) query.processDefinitionKeyLike(processDefinitionKeyLike);
            if (processDefinitionName != null) query.processDefinitionName(processDefinitionName);
            if (processDefinitionNameLike != null) query.processDefinitionNameLike(processDefinitionNameLike);
            if (dueAt != null) query.dueDate(dueAt);
            if (dueAtGreaterThan != null) query.dueAfter(dueAtGreaterThan);
            if (dueAtLessThan != null) query.dueBefore(dueAtLessThan);
            if (startedAt != null) query.taskCreatedOn(startedAt);
            if (startedAtGreaterThan != null) query.taskCreatedAfter(startedAtGreaterThan);
            if (startedAtLessThan != null) query.taskCreatedBefore(startedAtLessThan);
            
            if (includeProcessVariables != null && includeProcessVariables) {
                query.includeProcessVariables();
            }
            
            if (includeTaskVariables != null && includeTaskVariables) {
                query.includeTaskLocalVariables();
            }
            
            List<QueryVariableHolder> variableProperties = propertyWalker.getVariableProperties();
            if (variableProperties != null)
            {
                for (QueryVariableHolder queryVariableHolder : variableProperties)
                {
                    if (queryVariableHolder.getOperator() == WhereClauseParser.EQUALS)
                    {
                        if (queryVariableHolder.isGlobalScope())
                        {
                            query.processVariableValueEquals(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                        else
                        {
                            query.taskVariableValueEquals(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                    }
                    else if (queryVariableHolder.getOperator() == WhereClauseParser.GREATERTHAN)
                    {
                        if (queryVariableHolder.isGlobalScope())
                        {
                            query.processVariableValueGreaterThan(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                        else
                        {
                            query.taskVariableValueGreaterThan(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                    }
                    else if (queryVariableHolder.getOperator() == WhereClauseParser.GREATERTHANOREQUALS)
                    {
                        if (queryVariableHolder.isGlobalScope())
                        {
                            query.processVariableValueGreaterThanOrEqual(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                        else
                        {
                            query.taskVariableValueGreaterThanOrEqual(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                    }
                    else if (queryVariableHolder.getOperator() == WhereClauseParser.LESSTHAN)
                    {
                        if (queryVariableHolder.isGlobalScope())
                        {
                            query.processVariableValueLessThan(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                        else
                        {
                            query.taskVariableValueLessThan(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                    }
                    else if (queryVariableHolder.getOperator() == WhereClauseParser.LESSTHANOREQUALS)
                    {
                        if (queryVariableHolder.isGlobalScope())
                        {
                            query.processVariableValueLessThanOrEqual(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                        else
                        {
                            query.taskVariableValueLessThanOrEqual(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                    }
                    else if (queryVariableHolder.getOperator() == WhereClauseParser.MATCHES)
                    {
                        if (queryVariableHolder.getPropertyValue() instanceof String == false)
                        {
                            throw new InvalidArgumentException("the matches operator can only be used with a String value for property " + queryVariableHolder.getPropertyName());
                        }
                        
                        if (queryVariableHolder.isGlobalScope())
                        {
                            query.processVariableValueLike(queryVariableHolder.getPropertyName(), (String) queryVariableHolder.getPropertyValue());
                        }
                        else
                        {
                            query.taskVariableValueLike(queryVariableHolder.getPropertyName(), (String) queryVariableHolder.getPropertyValue());
                        }
                    }
                    else if (queryVariableHolder.getOperator() == WhereClauseParser.NEGATION)
                    {
                        if (queryVariableHolder.isGlobalScope())
                        {
                            query.processVariableValueNotEquals(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                        else
                        {
                            query.taskVariableValueNotEquals(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                    }
                    else
                    {
                        throw new InvalidArgumentException("variable " + queryVariableHolder.getPropertyName() + 
                                " can only be used with an =, not comparison type");
                    }
                }
            }
            
            // Add tenant-filtering
            if(tenantService.isEnabled()) {
                query.processVariableValueEquals(ActivitiConstants.VAR_TENANT_DOMAIN, TenantUtil.getCurrentDomain());
            }
            
            // Add involvment filtering if user is not admin
            if(processInstanceId == null && !authorityService.isAdminAuthority(AuthenticationUtil.getRunAsUser())) {
                query.taskInvolvedUser(AuthenticationUtil.getRunAsUser());
            }
            
            setSorting(query, sortColumn);
            
            List<org.activiti.engine.task.Task> tasks = query.listPage(paging.getSkipCount(), paging.getMaxItems());
            totalCount = (int) query.count();
            
            page = new ArrayList<Task>(tasks.size());
            Map<String, TypeDefinition> definitionTypeMap = new HashMap<String, TypeDefinition>();
            for (org.activiti.engine.task.Task taskInstance: tasks) 
            {
                Task task = new Task(taskInstance);
                task.setFormResourceKey(getFormResourceKey(taskInstance));
                if ((includeProcessVariables != null && includeProcessVariables) || (includeTaskVariables != null && includeTaskVariables))
                {
                    addVariables(task, includeProcessVariables, includeTaskVariables, taskInstance.getProcessVariables(), 
                            taskInstance.getTaskLocalVariables(), definitionTypeMap);
                }
                page.add(task);
            }
        }
        else if (STATUS_COMPLETED.equals(status) || STATUS_ANY.equals(status))
        {
            // Candidate user and group is only supported with STATUS_ACTIVE
            if (candidateUser != null)
            {
                throw new InvalidArgumentException("Filtering on candidateUser is only allowed in combination with status-parameter 'active'");
            }
            if (candidateGroup != null)
            {
                throw new InvalidArgumentException("Filtering on candidateGroup is only allowed in combination with status-parameter 'active'");
            }
            
            HistoricTaskInstanceQuery query = activitiProcessEngine
                    .getHistoryService()
                    .createHistoricTaskInstanceQuery();
            
            if (STATUS_COMPLETED.equals(status)) query.finished();
            if (assignee != null) query.taskAssignee(assignee);
            if (assigneeLike != null) query.taskAssigneeLike(assigneeLike);
            if (owner != null) query.taskOwner(owner);
            if (ownerLike != null) query.taskOwnerLike(ownerLike);
            if (name != null) query.taskName(name);
            if (nameLike != null) query.taskNameLike(nameLike);
            if (description != null) query.taskDescription(description);
            if (descriptionLike != null) query.taskDescriptionLike(descriptionLike);
            if (priority != null) query.taskPriority(priority);
            if (priorityGreaterThanOrEquals != null) query.taskMinPriority(priorityGreaterThanOrEquals);
            if (priorityLessThanOrEquals != null) query.taskMaxPriority(priorityLessThanOrEquals);
            if (processInstanceId != null) query.processInstanceId(processInstanceId);
            if (processInstanceBusinessKey != null) query.processInstanceBusinessKey(processInstanceBusinessKey);
            if (processInstanceBusinessKeyLike != null) query.processInstanceBusinessKeyLike(processInstanceBusinessKeyLike);
            if (activityDefinitionId != null) query.taskDefinitionKey(activityDefinitionId);
            if (activityDefinitionIdLike != null) query.taskDefinitionKey(activityDefinitionIdLike);
            if (processDefinitionId != null) query.processDefinitionId(processDefinitionId);
            if (processDefinitionKey != null) query.processDefinitionKey(processDefinitionKey);
            if (processDefinitionKeyLike != null) query.processDefinitionKeyLike(processDefinitionKeyLike);
            if (processDefinitionName != null) query.processDefinitionName(processDefinitionName);
            if (processDefinitionNameLike != null) query.processDefinitionNameLike(processDefinitionNameLike);
            if (dueAt != null) query.taskDueDate(dueAt);
            if (dueAtGreaterThan != null) query.taskDueAfter(dueAtGreaterThan);
            if (dueAtLessThan != null) query.taskDueBefore(dueAtLessThan);
            if (startedAt != null) query.taskCreatedOn(startedAt);
            if (startedAtGreaterThan != null) query.taskCreatedAfter(startedAtGreaterThan);
            if (startedAtLessThan != null) query.taskCreatedBefore(startedAtLessThan);
            if (endedAt != null) query.taskCompletedOn(endedAt);
            if (endedAtGreaterThan != null) query.taskCompletedAfter(endedAtGreaterThan);
            if (endedAtLessThan != null) query.taskCompletedBefore(endedAtLessThan);
            
            if (includeProcessVariables != null && includeProcessVariables) {
                query.includeProcessVariables();
            }
            
            if (includeTaskVariables != null && includeTaskVariables) {
                query.includeTaskLocalVariables();
            }
            
            List<QueryVariableHolder> variableProperties = propertyWalker.getVariableProperties();
            if (variableProperties != null)
            {
                for (QueryVariableHolder queryVariableHolder : variableProperties)
                {
                    if (queryVariableHolder.getOperator() == WhereClauseParser.EQUALS)
                    {
                        if (queryVariableHolder.isGlobalScope())
                        {
                            query.processVariableValueEquals(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                        else
                        {
                            query.taskVariableValueEquals(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                    }
                    else if (queryVariableHolder.getOperator() == WhereClauseParser.GREATERTHAN)
                    {
                        if (queryVariableHolder.isGlobalScope())
                        {
                            query.processVariableValueGreaterThan(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                        else
                        {
                            query.taskVariableValueGreaterThan(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                    }
                    else if (queryVariableHolder.getOperator() == WhereClauseParser.GREATERTHANOREQUALS)
                    {
                        if (queryVariableHolder.isGlobalScope())
                        {
                            query.processVariableValueGreaterThanOrEqual(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                        else
                        {
                            query.taskVariableValueGreaterThanOrEqual(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                    }
                    else if (queryVariableHolder.getOperator() == WhereClauseParser.LESSTHAN)
                    {
                        if (queryVariableHolder.isGlobalScope())
                        {
                            query.processVariableValueLessThan(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                        else
                        {
                            query.taskVariableValueLessThan(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                    }
                    else if (queryVariableHolder.getOperator() == WhereClauseParser.LESSTHANOREQUALS)
                    {
                        if (queryVariableHolder.isGlobalScope())
                        {
                            query.processVariableValueLessThanOrEqual(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                        else
                        {
                            query.taskVariableValueLessThanOrEqual(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                    }
                    else if (queryVariableHolder.getOperator() == WhereClauseParser.MATCHES)
                    {
                        if (queryVariableHolder.getPropertyValue() instanceof String == false)
                        {
                            throw new InvalidArgumentException("the matches operator can only be used with a String value for property " + queryVariableHolder.getPropertyName());
                        }
                        
                        if (queryVariableHolder.isGlobalScope())
                        {
                            query.processVariableValueLike(queryVariableHolder.getPropertyName(), (String) queryVariableHolder.getPropertyValue());
                        }
                        else
                        {
                            query.taskVariableValueLike(queryVariableHolder.getPropertyName(), (String) queryVariableHolder.getPropertyValue());
                        }
                    }
                    else if (queryVariableHolder.getOperator() == WhereClauseParser.NEGATION)
                    {
                        if (queryVariableHolder.isGlobalScope())
                        {
                            query.processVariableValueNotEquals(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                        else
                        {
                            query.taskVariableValueNotEquals(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                        }
                    }
                    else
                    {
                        throw new InvalidArgumentException("variable " + queryVariableHolder.getPropertyName() + 
                                " can only be used with an =, not comparison type");
                    }
                }
            }
            
            // Add tenant filtering
            if (tenantService.isEnabled()) 
            {
                query.processVariableValueEquals(ActivitiConstants.VAR_TENANT_DOMAIN, TenantUtil.getCurrentDomain());
            }
            
            // Add involvment filtering if user is not admin
            if(processInstanceId == null && !authorityService.isAdminAuthority(AuthenticationUtil.getRunAsUser())) 
            {
                query.taskInvolvedUser(AuthenticationUtil.getRunAsUser());
            }
            
            setSorting(query, sortColumn);
            
            List<HistoricTaskInstance> tasks = query.listPage(paging.getSkipCount(), paging.getMaxItems());
            totalCount = (int) query.count();

            page = new ArrayList<Task>(tasks.size());
            Map<String, TypeDefinition> definitionTypeMap = new HashMap<String, TypeDefinition>();
            for (HistoricTaskInstance taskInstance: tasks) 
            {
                Task task = new Task(taskInstance);
                if ((includeProcessVariables != null && includeProcessVariables) || (includeTaskVariables != null && includeTaskVariables))
                {
                    addVariables(task, includeProcessVariables, includeTaskVariables, taskInstance.getProcessVariables(), 
                            taskInstance.getTaskLocalVariables(), definitionTypeMap);
                }
                page.add(task);
            }
        } 
        else 
        {
            throw new InvalidArgumentException("Invalid status parameter: " + status);
        }
        
        return CollectionWithPagingInfo.asPaged(paging, page, page.size() != totalCount, totalCount);
    }
    
    protected void addVariables(Task task, Boolean includeProcessVariables, Boolean includeTaskVariables, 
            Map<String, Object> processVariables, Map<String, Object> taskVariables, Map<String, TypeDefinition> definitionTypeMap)
    {
        TypeDefinition startFormTypeDefinition = null;
        if (includeProcessVariables != null && includeProcessVariables) 
        {
            if (definitionTypeMap.containsKey(task.getProcessDefinitionId()) == false)
            {
                StartFormData startFormData = activitiProcessEngine.getFormService().getStartFormData(task.getProcessDefinitionId());
                if (startFormData != null)
                {
                    String formKey = startFormData.getFormKey();
                    definitionTypeMap.put(task.getProcessDefinitionId(), getWorkflowFactory().getTaskFullTypeDefinition(formKey, true));
                }
            }
            
            if (definitionTypeMap.containsKey(task.getProcessDefinitionId()))
            {
                startFormTypeDefinition = definitionTypeMap.get(task.getProcessDefinitionId());
            }
        }
        
        TypeDefinition taskTypeDefinition = null;
        if (includeTaskVariables != null && includeTaskVariables) 
        {
            taskTypeDefinition = getWorkflowFactory().getTaskFullTypeDefinition(task.getFormResourceKey(), false);
        }
        
        List<TaskVariable> variables = restVariableHelper.getTaskVariables(taskVariables, processVariables, 
                startFormTypeDefinition, taskTypeDefinition);
        task.setVariables(variables);
    }
    
    @Override
    public CollectionWithPagingInfo<Task> getTasks(String processId, Parameters parameters)
    {
        Paging paging = parameters.getPaging();
        String status = parameters.getParameter("status");
        
        List<SortColumn> sortList = parameters.getSorting();
        SortColumn sortColumn = null;
        if (sortList != null && sortList.size() > 0)
        {
            if (sortList.size() != 1)
            {
                throw new InvalidArgumentException("Only one order by parameter is supported");
            }
            sortColumn = sortList.get(0);
        }
        
        validateIfUserAllowedToWorkWithProcess(processId);

        List<Task> page = null;
        int totalCount = 0;
        if (status == null || STATUS_ACTIVE.equals(status))
        {
            TaskQuery query = activitiProcessEngine
                    .getTaskService()
                    .createTaskQuery();
            
            query.processInstanceId(processId);
            setSorting(query, sortColumn);
            
            List<org.activiti.engine.task.Task> tasks = query.listPage(paging.getSkipCount(), paging.getMaxItems());
            totalCount = (int) query.count();

            page = new ArrayList<Task>(tasks.size());
            for (org.activiti.engine.task.Task taskInstance: tasks) 
            {
                Task task = new Task(taskInstance);
                task.setFormResourceKey(getFormResourceKey(taskInstance));
                page.add(task);
            }
        }
        else if (STATUS_COMPLETED.equals(status) || STATUS_ANY.equals(status))
        {
            HistoricTaskInstanceQuery query = activitiProcessEngine
                    .getHistoryService()
                    .createHistoricTaskInstanceQuery();
            
            if (STATUS_COMPLETED.equals(status)) query.finished();
            
            query.processInstanceId(processId);
            
            // Add tenant filtering
            if(tenantService.isEnabled()) {
                query.processVariableValueEquals(ActivitiConstants.VAR_TENANT_DOMAIN, TenantUtil.getCurrentDomain());
            }
            
            setSorting(query, sortColumn);
            
            List<HistoricTaskInstance> tasks = query.listPage(paging.getSkipCount(), paging.getMaxItems());
            totalCount = (int) query.count();

            page = new ArrayList<Task>(tasks.size());
            for (HistoricTaskInstance taskInstance: tasks) 
            {
                Task task = new Task(taskInstance);
                page.add(task);
            }
        } 
        else 
        {
            throw new InvalidArgumentException("Invalid status parameter: " + status);
        }
        
        return CollectionWithPagingInfo.asPaged(paging, page, page.size() != totalCount, totalCount);
    }

    @Override
    public Task getTask(String taskId)
    {
        if(taskId == null) 
        {
            throw new InvalidArgumentException("Task id is required"); 
        }

        HistoricTaskInstance taskInstance = getValidHistoricTask(taskId);

        return new Task(taskInstance);
    }
    
    @Override
    public Task update(String taskId, Task task, Parameters parameters)
    {
        TaskStateTransition taskAction = null;
        
        List<String> selectedProperties = parameters.getSelectedProperties();
        if (selectedProperties.contains("state")) 
        {
            taskAction = TaskStateTransition.getTaskActionFromString(task.getState());
        }
        
        // Fetch the task unfiltered, we check authorization below
        TaskQuery query = activitiProcessEngine.getTaskService().createTaskQuery().taskId(taskId);
        org.activiti.engine.task.Task taskInstance = query.singleResult();
        
        if (taskInstance == null) 
        {
            // Check if task exists in history, to be able to return appropriate error when trying to update an
            // existing completed task vs. an unexisting task vs. unauthorized
            boolean taskHasExisted = activitiProcessEngine.getHistoryService().createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .count() > 0;
            
            if (taskHasExisted)
            {
                throw new UnsupportedResourceOperationException("Task with id: " + taskId + " cannot be updated, it's completed");
            }
            else
            {
                throw new EntityNotFoundException(taskId);
            }
        }
        else
        {
            String user = AuthenticationUtil.getRunAsUser();
            
            // Check if user is either assignee, owner or admin
            boolean authorized = authorityService.isAdminAuthority(user)
                || user.equals(taskInstance.getOwner())
                || user.equals(taskInstance.getAssignee());
            
            Set<String> candidateGroups = new HashSet<String>();
            
            if (!authorized) 
            {
                // Check if user is initiator of the process this task is involved with
                List<IdentityLink> linksForTask = activitiProcessEngine.getTaskService().getIdentityLinksForTask(taskId);
                
                // In case the action is claim, we gather all candidate groups for this tasks, since we already have
                // the identity-links, there is no reason why we should check candidate using a DB-query
                for (IdentityLink link : linksForTask) 
                {
                    if (user.equals(link.getUserId()) && IdentityLinkType.STARTER.equals(link.getType()))
                    {
                        authorized = true;
                        break;
                    }
                    if (taskAction == TaskStateTransition.CLAIMED && link.getGroupId() != null && link.getType().equals(IdentityLinkType.CANDIDATE)) 
                    {
                        candidateGroups.add(link.getGroupId());
                    }
                    if (taskAction == TaskStateTransition.CLAIMED && 
                            link.getUserId() != null && link.getType().equals(IdentityLinkType.CANDIDATE) &&
                            user.equals(link.getUserId())) 
                    {
                        // User is a direct candidate for the task, authorized to claim
                        authorized = true;
                        break;
                    }
                }
            }
            
            // When claiming, a limited update (set assignee through claim) is allowed
            if (!authorized && taskAction == TaskStateTransition.CLAIMED)
            {
                Set<String> userGroups = authorityService.getAuthoritiesForUser(user);
                for(String group : candidateGroups)
                {
                    if(userGroups.contains(group)) 
                    {
                        authorized = true;
                        break;
                    }
                }
            }
            
            if (!authorized) 
            {
                // None of the above conditions are met, not authorized to update task
                throw new PermissionDeniedException();
            }
        }
        
        // Update fields if no action is required
        if (taskAction == null)
        {
            // Only update task in Activiti API if actual properties are changed
            if (updateTaskProperties(selectedProperties, task, taskInstance))
            {
                activitiProcessEngine.getTaskService().saveTask(taskInstance);
            }
        }
        else
        {
            // Perform actions associated to state transition 
            if (taskAction != null) 
            {
                // look for variables submitted with task action
                Map<String, Object> globalVariables = new HashMap<String, Object>();
                Map<String, Object> localVariables = new HashMap<String, Object>();
                if (selectedProperties.contains("variables") && task.getVariables() != null && task.getVariables().size() > 0) 
                {
                    for (TaskVariable taskVariable : task.getVariables())
                    {
                        taskVariable = convertToTypedVariable(taskVariable, taskInstance);
                        if (taskVariable.getVariableScope() == VariableScope.GLOBAL)
                        {
                            globalVariables.put(taskVariable.getName(), taskVariable.getValue());
                        }
                        else
                        {
                            localVariables.put(taskVariable.getName(), taskVariable.getValue());
                        }
                    }
                }
                
                switch (taskAction) 
                {
                    case CLAIMED:
                        try
                        {
                            activitiProcessEngine.getTaskService().claim(taskId, AuthenticationUtil.getRunAsUser());
                        }
                        catch(ActivitiTaskAlreadyClaimedException atace)
                        {
                            throw new ConstraintViolatedException("The task is already claimed by another user.");
                        }
                        break;
                    case COMPLETED:
                        if (localVariables.size() > 0)
                        {
                            activitiProcessEngine.getTaskService().setVariablesLocal(taskId, localVariables);
                        }
                        if (globalVariables.size() > 0)
                        {
                            activitiProcessEngine.getTaskService().complete(taskId, globalVariables);
                        }
                        else
                        {    
                            activitiProcessEngine.getTaskService().complete(taskId);
                        }
                        
                        break;
                    case DELEGATED:
                        if(selectedProperties.contains("assignee") && task.getAssignee() != null)
                        {
                            if(taskInstance.getAssignee() == null || !taskInstance.getAssignee().equals(AuthenticationUtil.getRunAsUser()))
                            {
                                // Alter assignee before delegating to preserve trail of who actually delegated
                                activitiProcessEngine.getTaskService().setAssignee(taskId, AuthenticationUtil.getRunAsUser());
                            }
                            activitiProcessEngine.getTaskService().delegateTask(taskId, task.getAssignee());
                        }
                        else
                        {
                            throw new InvalidArgumentException("When delegating a task, assignee should be selected and provided in the request.");
                        }
                        break;
                    case RESOLVED:
                        if (localVariables.size() > 0)
                        {
                            activitiProcessEngine.getTaskService().setVariablesLocal(taskId, localVariables);
                        }
                        if (globalVariables.size() > 0)
                        {
                            activitiProcessEngine.getTaskService().resolveTask(taskId, globalVariables);
                        }
                        else
                        {    
                            activitiProcessEngine.getTaskService().resolveTask(taskId);
                        }
                        break;
                        
                    case UNCLAIMED:
                        activitiProcessEngine.getTaskService().setAssignee(taskId, null);
                        break;
                }
            }
        }
        
        Task responseTask = new Task(activitiProcessEngine.getHistoryService()
                    .createHistoricTaskInstanceQuery()
                    .taskId(taskId).singleResult());
        
        // if the task is not ended the task state might be pending or resolved
        if (responseTask.getEndedAt() == null)
        {
            try
            {
                org.activiti.engine.task.Task runningTask = activitiProcessEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();
                if (runningTask != null)
                {
                    if (runningTask.getDelegationState() == DelegationState.PENDING)
                    {
                        responseTask.setState(TaskStateTransition.DELEGATED.name().toLowerCase());
                    }
                    else if (runningTask.getDelegationState() == DelegationState.RESOLVED)
                    {
                        responseTask.setState(TaskStateTransition.RESOLVED.name().toLowerCase());
                    }
                }
            }
            catch (Exception e)
            {
                // ignore the exception
            }
        }
        
        return responseTask;
    }
    
    @Override
    public CollectionWithPagingInfo<FormModelElement> getTaskFormModel(String taskId, Paging paging)
    {
        // Check if task can be accessed by the current user
        HistoricTaskInstance task = getValidHistoricTask(taskId);
        String formKey = task.getFormKey();
        
        // Lookup type definition for the task
        TypeDefinition taskType = getWorkflowFactory().getTaskFullTypeDefinition(formKey, true);
        return getFormModelElements(taskType, paging);
    }
    
    @Override
    public CollectionWithPagingInfo<TaskVariable> getTaskVariables(String taskId, Paging paging, VariableScope scope) 
    {
        // Ensure the user is allowed to get variables for the task involved. 
        HistoricTaskInstance taskInstance = getValidHistoricTask(taskId);
        String formKey = taskInstance.getFormKey();

        // Based on the scope, right variables are queried
        Map<String, Object> taskvariables = new HashMap<String, Object>();
        Map<String, Object> processVariables = new HashMap<String, Object>();
        
        if (scope == VariableScope.ANY || scope == VariableScope.LOCAL)
        {
             List<HistoricVariableInstance> variables = activitiProcessEngine.getHistoryService()
                     .createHistoricVariableInstanceQuery()
                     .taskId(taskId)
                     .list();
             
             if (variables != null)
             {
                 for (HistoricVariableInstance variable : variables)
                 {
                     taskvariables.put(variable.getVariableName(), variable.getValue());
                 }
             }
        }
        
        if ((scope == VariableScope.ANY || scope == VariableScope.GLOBAL) && taskInstance.getProcessInstanceId() != null)
        {
            List<HistoricVariableInstance> variables = activitiProcessEngine.getHistoryService()
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(taskInstance.getProcessInstanceId())
                    .excludeTaskVariables()
                    .list();
            
            if (variables != null)
            {
                for (HistoricVariableInstance variable : variables)
                {
                    processVariables.put(variable.getVariableName(), variable.getValue());
                }
            }
        }
        
        // Convert raw variables to TaskVariables
        TypeDefinition taskTypeDefinition = getWorkflowFactory().getTaskFullTypeDefinition(formKey, false);
        TypeDefinition startFormTypeDefinition = null;
        StartFormData startFormData = activitiProcessEngine.getFormService().getStartFormData(taskInstance.getProcessDefinitionId());
        if (startFormData != null)
        {
            startFormTypeDefinition = getWorkflowFactory().getTaskFullTypeDefinition(startFormData.getFormKey(), true);
        }
        else
        {
            // fall back
            startFormTypeDefinition = taskTypeDefinition;
        }
        List<TaskVariable> page = restVariableHelper.getTaskVariables(taskvariables, processVariables, 
                startFormTypeDefinition, taskTypeDefinition);
        return CollectionWithPagingInfo.asPaged(paging, page, false, page.size());
    }
    
    @Override
    public TaskVariable updateTaskVariable(String taskId, TaskVariable taskVariable) 
    {
        org.activiti.engine.task.Task taskInstance = getValidTask(taskId);
        return updateVariableInTask(taskInstance, taskVariable);
    }
    
    public List<TaskVariable> updateTaskVariables(String taskId, List<TaskVariable> variables)
    {
        org.activiti.engine.task.Task taskInstance = getValidTask(taskId);
        List<TaskVariable> updatedVariables = new ArrayList<TaskVariable>();
        if (variables != null)
        {
            for (TaskVariable variable : variables)
            {
                updatedVariables.add(updateVariableInTask(taskInstance, variable));
            }
        }
        return updatedVariables;
    }
    
    protected TaskVariable updateVariableInTask(org.activiti.engine.task.Task taskInstance, TaskVariable taskVariable)
    {
        taskVariable = convertToTypedVariable(taskVariable, taskInstance);
        
        if (VariableScope.LOCAL.equals(taskVariable.getVariableScope()))
        {
            activitiProcessEngine.getTaskService().setVariableLocal(taskInstance.getId(), taskVariable.getName(), taskVariable.getValue());
        }
        else if(VariableScope.GLOBAL.equals(taskVariable.getVariableScope()))
        {
            if(taskInstance.getExecutionId() != null)
            {
                activitiProcessEngine.getRuntimeService().setVariable(taskInstance.getExecutionId(), taskVariable.getName(), taskVariable.getValue());
            }
            else
            {
                throw new InvalidArgumentException("Cannot set global variables on a task that is not part of a process.");
            }
        }
        
        return taskVariable;
    }
    
    protected TaskVariable convertToTypedVariable(TaskVariable taskVariable, org.activiti.engine.task.Task taskInstance)
    {
        if (taskVariable.getName() == null)
        {
            throw new InvalidArgumentException("Variable name is required.");
        }
        
        if (taskVariable.getVariableScope() == null || (taskVariable.getVariableScope() != VariableScope.GLOBAL && 
                taskVariable.getVariableScope() != VariableScope.LOCAL))
        {
            throw new InvalidArgumentException("Variable scope is required and can only be 'local' or 'global'.");
        }
        
        DataTypeDefinition dataTypeDefinition = null;
        TypeDefinitionContext context = null;
        if (taskVariable.getVariableScope() == VariableScope.GLOBAL)
        {
            // Get start-task definition for explicit typing of variables submitted at the start
            String formKey = null;
            StartFormData startFormData = activitiProcessEngine.getFormService().getStartFormData(taskInstance.getProcessDefinitionId());
            if (startFormData != null)
            {
                formKey = startFormData.getFormKey();
            }
            
            TypeDefinition startTaskTypeDefinition = getWorkflowFactory().getTaskFullTypeDefinition(formKey, true);
            context = new TypeDefinitionContext(startTaskTypeDefinition, getQNameConverter());
            if (context.getPropertyDefinition(taskVariable.getName()) != null) 
            {
                dataTypeDefinition = context.getPropertyDefinition(taskVariable.getName()).getDataType();
                if (taskVariable.getType() != null && dataTypeDefinition.getName().toPrefixString(namespaceService).equals(taskVariable.getType()) == false) {
                    throw new InvalidArgumentException("type of variable " + taskVariable.getName() + " should be " + 
                            dataTypeDefinition.getName().toPrefixString(namespaceService));
                }
            }
            else if (context.getAssociationDefinition(taskVariable.getName()) != null) 
            {
                dataTypeDefinition = dictionaryService.getDataType(DataTypeDefinition.NODE_REF);
            }
        } 
        else
        {
            // Revert to either the content-model type or the raw type provided by the request
            try 
            {
                String formKey = activitiProcessEngine.getFormService().getTaskFormKey(taskInstance.getProcessDefinitionId(), taskInstance.getTaskDefinitionKey());
                TypeDefinition typeDefinition = getWorkflowFactory().getTaskFullTypeDefinition(formKey, false);
                context = new TypeDefinitionContext(typeDefinition, getQNameConverter());
                if (context.getPropertyDefinition(taskVariable.getName()) != null) 
                {
                    dataTypeDefinition = context.getPropertyDefinition(taskVariable.getName()).getDataType();
                    if (taskVariable.getType() != null && dataTypeDefinition.getName().toPrefixString(namespaceService).equals(taskVariable.getType()) == false) {
                        throw new InvalidArgumentException("type of variable " + taskVariable.getName() + " should be " + 
                                dataTypeDefinition.getName().toPrefixString(namespaceService));
                    }
                }
                else if (context.getAssociationDefinition(taskVariable.getName()) != null) 
                {
                    dataTypeDefinition = dictionaryService.getDataType(DataTypeDefinition.NODE_REF);
                }
            }
            catch (InvalidQNameException ignore)
            {
                // In case the property is not part of the model, it's possible that the property-name is not a valid.
                // This can be ignored safeley as it falls back to the raw type
            }
        }
        
        if (dataTypeDefinition == null && taskVariable.getType() != null)
        {
            try
            {
                QName dataType = QName.createQName(taskVariable.getType(), namespaceService);
                dataTypeDefinition = dictionaryService.getDataType(dataType);
            }
            catch (InvalidQNameException iqne)
            {
                throw new InvalidArgumentException("Unsupported type of variable: '" + taskVariable.getType() +"'.");
            }
        }
        else if (dataTypeDefinition == null)
        {
            // Final fallback to raw value when no type has been passed and not present in model
            dataTypeDefinition = dictionaryService.getDataType(restVariableHelper.extractTypeFromValue(taskVariable.getValue()));
        }
        
        if (dataTypeDefinition == null)
        {
            throw new InvalidArgumentException("Unsupported type of variable: '" + taskVariable.getType() +"'.");
        }
        
        Object actualValue = null;
        if ("java.util.Date".equalsIgnoreCase(dataTypeDefinition.getJavaClassName()))
        {
            // fix for different ISO 8601 Date format classes in Alfresco (org.alfresco.util and Spring Surf)
            actualValue = ISO8601DateFormat.parse((String) taskVariable.getValue());
        }
        else
        {
            if (context != null && context.getAssociationDefinition(taskVariable.getName()) != null)
            {
                actualValue = convertAssociationDefinitionValue(context.getAssociationDefinition(taskVariable.getName()), 
                        taskVariable.getName(), taskVariable.getValue());
            }
            else
            {
                actualValue = DefaultTypeConverter.INSTANCE.convert(dataTypeDefinition, taskVariable.getValue());
            }
        }
        
        taskVariable.setValue(actualValue);
        
        // Set type so it's returned in case it was left empty
        taskVariable.setType(dataTypeDefinition.getName().toPrefixString(namespaceService));
        
        return taskVariable;
    }
    
    public void deleteTaskVariable(String taskId, String variableName)
    {
        if(variableName == null)
        {
            throw new InvalidArgumentException("Variable name is required.");
        }
        
        // Fetch task to check if user is authorized to perform the delete
        getValidTask(taskId);
        
        // Check if variable is present on the scope
        if (activitiProcessEngine.getTaskService().hasVariableLocal(taskId, variableName) == false)
        {
            throw new EntityNotFoundException(variableName);
        }
        activitiProcessEngine.getTaskService().removeVariableLocal(taskId, variableName);
    }
    
    @Override
    public CollectionWithPagingInfo<TaskCandidate> getTaskCandidates(String taskId, Paging paging) 
    {
        // Fetch task to check if user is authorized to perform the delete
        getValidTask(taskId);
        
        List<IdentityLink> links = activitiProcessEngine.getTaskService().getIdentityLinksForTask(taskId);
        List<TaskCandidate> page = new ArrayList<TaskCandidate>();
        if (links != null) 
        {
            for (IdentityLink identityLink : links)
            {
                if (IdentityLinkType.CANDIDATE.equals(identityLink.getType())) 
                {
                    page.add(new TaskCandidate(identityLink));
                }
            }
        }
        
        return CollectionWithPagingInfo.asPaged(paging, page, false, page.size());
    }
    
    @Override
    public Item createItem(String taskId, Item item)
    {
        org.activiti.engine.task.Task task = getValidTask(taskId);
      
        if (task.getProcessInstanceId() == null)
        {
            throw new UnsupportedResourceOperationException("Task is not part of process, no items available.");
        }
        return createItemInProcess(item.getId(), task.getProcessInstanceId());
    }
    
    @Override
    public void deleteItem(String taskId, String itemId)
    {
        org.activiti.engine.task.Task task = getValidTask(taskId);
        
        if (task.getProcessInstanceId() == null)
        {
            throw new UnsupportedResourceOperationException("Task is not part of process, no items available.");
        }
        deleteItemFromProcess(itemId, task.getProcessInstanceId());
    }
    
    @Override
    public Item getItem(String taskId, String itemId)
    {
        HistoricTaskInstance task = getValidHistoricTask(taskId);
        
        if (task.getProcessInstanceId() == null)
        {
            throw new UnsupportedResourceOperationException("Task is not part of process, no items available.");
        }
        return getItemFromProcess(itemId, task.getProcessInstanceId());
    }
    
    @Override
    public CollectionWithPagingInfo<Item> getItems(String taskId, Paging paging)
    {
        HistoricTaskInstance task = getValidHistoricTask(taskId);
        
        if (task.getProcessInstanceId() == null)
        {
            throw new UnsupportedResourceOperationException("Task is not part of process, no items available.");
        }
        return getItemsFromProcess(task.getProcessInstanceId(), paging);
    }

    protected String getFormResourceKey(final org.activiti.engine.task.Task task) 
    {
        if (task.getProcessDefinitionId() != null)
        {
            String formKey = activitiProcessEngine.getFormService().getTaskFormKey(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
            return formKey;
        } 
        else 
        {
            // Standalone task, no form key available
            return null;
        }
    }
    
    /**
     * @return true, if at least one task property has been changed based on the given parameters.
     */
    protected boolean updateTaskProperties(List<String> selectedProperties, Task task, 
                org.activiti.engine.task.Task taskInstance)
    {
        boolean taskNeedsUpdate = false;
        for(String selected : selectedProperties) 
        {
            if(!"state".equals(selected))
            {
                // "name", "description", "dueAt", "priority", "assignee", "owner"
                taskNeedsUpdate = true;
                if("name".equals(selected))
                {
                    taskInstance.setName(task.getName());
                }
                else if("description".equals(selected))
                {
                    taskInstance.setDescription(task.getDescription());
                }
                else if("dueAt".equals(selected))
                {
                    taskInstance.setDueDate(task.getDueAt());
                }
                else if("priority".equals(selected))
                {
                    taskInstance.setPriority(task.getPriority());   
                }
                else if("assignee".equals(selected))
                {
                    taskInstance.setAssignee(task.getAssignee());
                }
                else if("owner".equals(selected))
                {
                    taskInstance.setOwner(task.getOwner());
                }
                else
                {
                    if(TASK_READ_ONLY_PROPERTIES.contains(selected))
                    {
                        // Trying to update a read-only -but existing- property
                        throw new InvalidArgumentException("The property selected for update is read-only: " + selected);
                    }
                    else
                    {
                        // Trying to update unexisting property
                        throw new InvalidArgumentException("The property selected for update does not exist for this resource: " + selected);
                    }
                }
            }
        }
        return taskNeedsUpdate;
    }
    
    /**
     * Get a valid {@link HistoricTaskInstance} based on the given task id. Checks if current logged
     * in user is assignee/owner/involved with the task. In case true was passed for "validIfClaimable", 
     * the task is also valid if the current logged in user is a candidate for claiming the task.
     *  
     * @throws EntityNotFoundException when the task was not found
     * @throws PermissionDeniedException when the current logged in user isn't allowed to access task.
     */
    protected HistoricTaskInstance getValidHistoricTask(String taskId)
    {
        HistoricTaskInstanceQuery query = activitiProcessEngine.getHistoryService()
            .createHistoricTaskInstanceQuery()
            .taskId(taskId);
        
        if (authorityService.isAdminAuthority(AuthenticationUtil.getRunAsUser())) 
        {
            // Admin is allowed to read all tasks in the current tenant
            if (tenantService.isEnabled()) 
            {
                query.processVariableValueEquals(ActivitiConstants.VAR_TENANT_DOMAIN, TenantUtil.getCurrentDomain());
            }
        }
        else
        {
            // If non-admin user, involvement in the task is required (either owner, assignee or externally involved).
            query.taskInvolvedUser(AuthenticationUtil.getRunAsUser());
        }
        
        HistoricTaskInstance taskInstance = query.singleResult();
        
        if (taskInstance == null) 
        {
            // Either the task doesn't exist or the user is not involved directly. We can differentiate by
            // checking if the task exists without applying the additional filtering
            taskInstance =  activitiProcessEngine.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .singleResult();
            
            if (taskInstance == null) 
            {
                // Full error message will be "Task with id: 'id' was not found" 
                throw new EntityNotFoundException(taskId); 
            }
            else
            {
                boolean isTaskClaimable = false;
                if (taskInstance.getEndTime() == null) 
                {
                    // Task is not yet finished, so potentially claimable. If user is part of a "candidateGroup", the task is accessible to the
                    // user regardless of not being involved/owner/assignee
                    isTaskClaimable = activitiProcessEngine.getTaskService()
                            .createTaskQuery()
                            .taskCandidateGroupIn(new ArrayList<String>(authorityService.getAuthoritiesForUser(AuthenticationUtil.getRunAsUser())))
                            .taskId(taskId)
                            .count() == 1;
                }
                
                if (isTaskClaimable == false)
                {
                    throw new PermissionDeniedException();
                }
            }
        }
        return taskInstance;
    }
    
    /**
     * Get a valid {@link org.activiti.engine.task.Task} based on the given task id. Checks if current logged
     * in user is assignee/owner/involved with the task. In case true was passed for "validIfClaimable", 
     * the task is also valid if the current logged in user is a candidate for claiming the task.
     *  
     * @throws EntityNotFoundException when the task was not found
     * @throws PermissionDeniedException when the current logged in user isn't allowed to access task.
     */
    protected org.activiti.engine.task.Task getValidTask(String taskId)
    {
        if (taskId == null)
        {
            throw new InvalidArgumentException("Task id is required.");
        }
        
        TaskQuery query = activitiProcessEngine.getTaskService()
            .createTaskQuery()
            .taskId(taskId);
        
        if (authorityService.isAdminAuthority(AuthenticationUtil.getRunAsUser())) 
        {
            // Admin is allowed to read all tasks in the current tenant
            if (tenantService.isEnabled()) 
            {
                query.processVariableValueEquals(ActivitiConstants.VAR_TENANT_DOMAIN, TenantUtil.getCurrentDomain());
            }
        }
        else
        {
            // If non-admin user, involvement in the task is required (either owner, assignee or externally involved).
            query.taskInvolvedUser(AuthenticationUtil.getRunAsUser());
        }
        
        org.activiti.engine.task.Task taskInstance =  query.singleResult();
        
        if (taskInstance == null) 
        {
            // Either the task doesn't exist or the user is not involved directly. We can differentiate by
            // checking if the task exists without applying the additional filtering
            taskInstance =  activitiProcessEngine.getTaskService()
                .createTaskQuery()
                .taskId(taskId)
                .singleResult();
            
            if (taskInstance == null) 
            {
                // Full error message will be "Task with id: 'id' was not found" 
                throw new EntityNotFoundException(taskId); 
            }
            else
            {
                // Task is not yet finished, so potentially claimable. If user is part of a "candidateGroup", the task is accessible to the
                // user regardless of not being involved/owner/assignee
                boolean isTaskClaimable = activitiProcessEngine.getTaskService()
                        .createTaskQuery()
                        .taskCandidateGroupIn(new ArrayList<String>(authorityService.getAuthoritiesForUser(AuthenticationUtil.getRunAsUser())))
                        .taskId(taskId)
                        .count() == 1;
                
                if (isTaskClaimable == false)
                {
                    throw new PermissionDeniedException();
                }
            }
        }
        return taskInstance;
    }
    
    protected void setSorting(TaskQuery query, SortColumn sortColumn)
    {
        if (sortColumn != null)
        {
            if (TASK_COLLECTION_RUNNING_SORT_PROPERTIES.contains(sortColumn.column))
            {
                if ("id".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByTaskId();
                }
                else if ("name".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByTaskName();
                }
                else if ("description".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByTaskDescription();
                }
                else if ("priority".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByTaskPriority();
                }
                else if ("processId".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByProcessInstanceId();
                }
                else if ("assignee".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByTaskAssignee();
                }
                else if ("startedAt".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByTaskCreateTime();
                }
                else if ("dueAt".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByDueDate();
                }
            }
            else
            {
                throw new InvalidArgumentException("sort " + sortColumn.column + 
                        " is not supported, supported items are " + TASK_COLLECTION_RUNNING_SORT_PROPERTIES.toArray());
            }
            
            if (sortColumn.asc)
            {
                query.asc();
            }
            else
            {
                query.desc();
            }
        }
        else
        {
            query.orderByDueDate().asc();
        }
    }
    
    protected void setSorting(HistoricTaskInstanceQuery query, SortColumn sortColumn)
    {
        if (sortColumn != null)
        {
            if (TASK_COLLECTION_HISTORY_SORT_PROPERTIES.contains(sortColumn.column))
            {
                if ("id".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByTaskId();
                }
                else if ("name".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByTaskName();
                }
                else if ("description".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByTaskDescription();
                }
                else if ("priority".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByTaskPriority();
                }
                else if ("processId".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByProcessInstanceId();
                }
                else if ("processDefinitionId".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByProcessDefinitionId();
                }
                else if ("assignee".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByTaskAssignee();
                }
                else if ("owner".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByTaskOwner();
                }
                else if ("startedAt".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByHistoricTaskInstanceStartTime();
                }
                else if ("endedAt".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByHistoricTaskInstanceEndTime();
                }
                else if ("durationInMs".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByHistoricTaskInstanceDuration();
                }
                else if ("dueAt".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByTaskDueDate();
                }
            }
            else
            {
                throw new InvalidArgumentException("sort " + sortColumn.column + 
                        " is not supported, supported items are " + TASK_COLLECTION_HISTORY_SORT_PROPERTIES.toArray());
            }
            
            if (sortColumn.asc)
            {
                query.asc();
            }
            else
            {
                query.desc();
            }
        }
        else
        {
            query.orderByTaskDueDate().asc();
        }
    }
    
    protected Object convertAssociationDefinitionValue(AssociationDefinition associationDef, String variableName, Object variableValue) 
    {
        if (variableValue != null && ContentModel.TYPE_PERSON.equals(associationDef.getTargetClass().getName()))
        {
            if (associationDef.isTargetMany())
            {
                if (variableValue instanceof List<?>)
                {
                    List<NodeRef> personList = new ArrayList<NodeRef>();
                    List<?> values = (List<?>) variableValue;
                    for (Object value : values)
                    {
                        NodeRef personRef = getPersonNodeRef(value.toString());
                        if (personRef == null)
                        {
                            throw new InvalidArgumentException(value.toString() + " is not a valid person user id");
                        }
                        personList.add(personRef);
                    }
                    variableValue = personList;
                }
                else
                {
                    throw new InvalidArgumentException(variableName + " should have an array value");
                }
            }
            else
            {
                NodeRef personRef = getPersonNodeRef(variableValue.toString());
                if (personRef == null)
                {
                    throw new InvalidArgumentException(variableValue.toString() + " is not a valid person user id");
                }
                variableValue = personRef;
            }
        }
        else if (variableValue != null && ContentModel.TYPE_AUTHORITY_CONTAINER.equals(associationDef.getTargetClass().getName()))
        {
            if (associationDef.isTargetMany())
            {
                if (variableValue instanceof List<?>)
                {
                    List<NodeRef> authorityList = new ArrayList<NodeRef>();
                    List<?> values = (List<?>) variableValue;
                    for (Object value : values)
                    {
                        NodeRef authorityRef = authorityService.getAuthorityNodeRef(value.toString());
                        if (authorityRef == null)
                        {
                            throw new InvalidArgumentException(value.toString() + " is not a valid authority id");
                        }
                        authorityList.add(authorityRef);
                    }
                    variableValue = authorityList;
                }
                else
                {
                    throw new InvalidArgumentException(variableName + " should have an array value");
                }
            }
            else
            {
                NodeRef authorityRef = authorityService.getAuthorityNodeRef(variableValue.toString());
                if (authorityRef == null)
                {
                    throw new InvalidArgumentException(variableValue.toString() + " is not a valid authority id");
                }
                variableValue = authorityRef;
            }
        }
        return variableValue;
    }
    
    protected NodeRef getPersonNodeRef(String name)
    {
        NodeRef authority = null;
        if (name != null)
        {
            if (personService.personExists(name))
            {
                authority = personService.getPerson(name);
            }
        }
        return authority;
    }
    
    protected WorkflowQNameConverter getQNameConverter()
    {
        if (qNameConverter == null)
        {
            qNameConverter = new WorkflowQNameConverter(namespaceService);
        }
        return qNameConverter;
    }
    
    protected WorkflowObjectFactory getWorkflowFactory()
    {
        if (workflowFactory == null) 
        {
            workflowFactory = new WorkflowObjectFactory(getQNameConverter(), tenantService, messageService, dictionaryService, 
                        ActivitiConstants.ENGINE_ID, WorkflowModel.TYPE_ACTIVTI_START_TASK);
        }
        return workflowFactory;
    }
}
