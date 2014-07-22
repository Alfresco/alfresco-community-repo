/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.workflow.DefaultWorkflowPropertyHandler;
import org.alfresco.repo.workflow.WorkflowAuthorityManager;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowObjectFactory;
import org.alfresco.repo.workflow.WorkflowPackageImpl;
import org.alfresco.repo.workflow.WorkflowPropertyHandlerRegistry;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiNodeConverter;
import org.alfresco.repo.workflow.activiti.ActivitiTypeConverter;
import org.alfresco.repo.workflow.activiti.ActivitiUtil;
import org.alfresco.repo.workflow.activiti.properties.ActivitiPropertyConverter;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.FileBinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.Processes;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker.QueryVariableHolder;
import org.alfresco.rest.workflow.api.model.Item;
import org.alfresco.rest.workflow.api.model.ProcessInfo;
import org.alfresco.rest.workflow.api.model.Variable;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;

public class ProcessesImpl extends WorkflowRestImpl implements Processes
{
    protected static String PROCESS_STATUS_ANY = "any";
    protected static String PROCESS_STATUS_ACTIVE = "active";
    protected static String PROCESS_STATUS_COMPLETED = "completed";
    protected static String PROCESS_STATUS_DELETED = "deleted";
    
    protected static final Set<String> PROCESS_STATUS_LIST = new HashSet<String>(Arrays.asList(
            PROCESS_STATUS_ANY, PROCESS_STATUS_ACTIVE, PROCESS_STATUS_COMPLETED, PROCESS_STATUS_DELETED
    ));
    
    protected static final Set<String> PROCESS_COLLECTION_EQUALS_QUERY_PROPERTIES = new HashSet<String>(Arrays.asList(
        "processDefinitionId", "businessKey", "processDefinitionKey", "startUserId", "status", "includeVariables"
    ));
    
    protected static final Set<String> PROCESS_COLLECTION_GREATERTHAN_QUERY_PROPERTIES = new HashSet<String>(Arrays.asList(
        "startedAt", "endedAt"
    ));
    
    protected static final Set<String> PROCESS_COLLECTION_LESSTHAN_QUERY_PROPERTIES = new HashSet<String>(Arrays.asList(
        "startedAt", "endedAt"
    ));
    
    protected static final Set<String> PROCESS_COLLECTION_SORT_PROPERTIES = new HashSet<String>(Arrays.asList(
        "processDefinitionId", "businessKey", "id", "startedAt", "endedAt", "durationInMillis"
    ));
    
    protected WorkflowPackageImpl workflowPackageComponent;
    protected ServiceRegistry serviceRegistry;
    protected AuthorityDAO authorityDAO;
    protected PersonService personService;
    protected MessageService messageService;
    protected String engineId;
    protected Repository repositoryHelper;
    protected RestVariableHelper restVariableHelper;
    
    protected ActivitiNodeConverter nodeConverter;
    protected ActivitiUtil activitiUtil;
    protected DefaultWorkflowPropertyHandler defaultPropertyHandler;
    protected WorkflowQNameConverter qNameConverter;
    protected QName defaultStartTaskType = WorkflowModel.TYPE_ACTIVTI_START_TASK;
    protected WorkflowObjectFactory workflowFactory;
    protected WorkflowPropertyHandlerRegistry handlerRegistry;
    protected WorkflowAuthorityManager authorityManager;
    protected ActivitiPropertyConverter propertyConverter;
    protected ActivitiTypeConverter typeConverter;
    
    public void setAuthorityDAO(AuthorityDAO authorityDAO)
    {
        this.authorityDAO = authorityDAO;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setWorkflowPackageComponent(WorkflowPackageImpl workflowPackageComponent)
    {
        this.workflowPackageComponent = workflowPackageComponent;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }

    public void setMessageService(MessageService messageService)
    {
        this.messageService = messageService;
    }
    
    public void setEngineId(String engineId)
    {
        this.engineId = engineId;
    }
    
    public void setRestVariableHelper(RestVariableHelper restVariableHelper)
    {
        this.restVariableHelper = restVariableHelper;
    }

    protected ActivitiNodeConverter getNodeConverter()
    {
        if (nodeConverter == null)
        {
            nodeConverter = new ActivitiNodeConverter(serviceRegistry);
        }
        return nodeConverter;
    }
    
    protected DefaultWorkflowPropertyHandler getDefaultPropertyHandler()
    {
        if (defaultPropertyHandler == null)
        {
            defaultPropertyHandler = new DefaultWorkflowPropertyHandler();
            defaultPropertyHandler.setMessageService(messageService);
            defaultPropertyHandler.setNodeConverter(getNodeConverter());
        }
        return defaultPropertyHandler;
    }
        
    protected ActivitiUtil getActivitiUtil()
    {
        if (activitiUtil == null)
        {
            activitiUtil = new ActivitiUtil(activitiProcessEngine, deployWorkflowsInTenant);
        }
        return activitiUtil;
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
            workflowFactory = new WorkflowObjectFactory(getQNameConverter(), tenantService, messageService, dictionaryService, engineId, defaultStartTaskType);
        }
        return workflowFactory;
    }
    
    protected WorkflowPropertyHandlerRegistry getHandlerRegistry()
    {
        if (handlerRegistry == null)
        {
            handlerRegistry = new WorkflowPropertyHandlerRegistry(getDefaultPropertyHandler(), getQNameConverter());
        }
        return handlerRegistry;
    }
    
    protected WorkflowAuthorityManager getAuthorityManager()
    {
        if (authorityManager == null)
        {
            authorityManager = new WorkflowAuthorityManager(authorityDAO);
        }
        return authorityManager;
    }
    
    protected ActivitiPropertyConverter getPropertyConverter()
    {
        if (propertyConverter == null)
        {
            propertyConverter = new ActivitiPropertyConverter(getActivitiUtil(), getWorkflowFactory(), getHandlerRegistry(), getAuthorityManager(), messageService, getNodeConverter());
        }
        return propertyConverter;
    }
        
    protected ActivitiTypeConverter getTypeConverter()
    {
        if (typeConverter == null)
        {
            typeConverter = new ActivitiTypeConverter(activitiProcessEngine, getWorkflowFactory(), getPropertyConverter(), deployWorkflowsInTenant);
        }
        return typeConverter;
    }

    @Override
    public CollectionWithPagingInfo<ProcessInfo> getProcesses(Parameters parameters)
    {
        Paging paging = parameters.getPaging();
        
        MapBasedQueryWalker propertyWalker = new MapBasedQueryWalker(PROCESS_COLLECTION_EQUALS_QUERY_PROPERTIES, null);
        
        propertyWalker.setSupportedGreaterThanParameters(PROCESS_COLLECTION_GREATERTHAN_QUERY_PROPERTIES);
        propertyWalker.setSupportedLessThanParameters(PROCESS_COLLECTION_LESSTHAN_QUERY_PROPERTIES);
        propertyWalker.enableVariablesSupport(namespaceService, dictionaryService);
        
        if(parameters.getQuery() != null)
        {
            QueryHelper.walk(parameters.getQuery(), propertyWalker);
        }
        
        String status = propertyWalker.getProperty("status", WhereClauseParser.EQUALS);
        String processDefinitionId = propertyWalker.getProperty("processDefinitionId", WhereClauseParser.EQUALS);
        String businessKey = propertyWalker.getProperty("businessKey", WhereClauseParser.EQUALS);
        String processDefinitionKey = propertyWalker.getProperty("processDefinitionKey", WhereClauseParser.EQUALS);
        String startUserId = propertyWalker.getProperty("startUserId", WhereClauseParser.EQUALS);
        Date startedAtGreaterThan = propertyWalker.getProperty("startedAt", WhereClauseParser.GREATERTHAN, Date.class);
        Date startedAtLessThan = propertyWalker.getProperty("startedAt", WhereClauseParser.LESSTHAN, Date.class);
        Date endedAtGreaterThan = propertyWalker.getProperty("endedAt", WhereClauseParser.GREATERTHAN, Date.class);
        Date endedAtLessThan = propertyWalker.getProperty("endedAt", WhereClauseParser.LESSTHAN, Date.class);
        Boolean includeVariables = propertyWalker.getProperty("includeVariables", WhereClauseParser.EQUALS, Boolean.class);
        
        if (status != null && PROCESS_STATUS_LIST.contains(status) == false)
        {
            throw new InvalidArgumentException("Invalid status parameter: " + status);
        }
        
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
        
        final HistoricProcessInstanceQuery query = activitiProcessEngine
                .getHistoryService()
                .createHistoricProcessInstanceQuery();

        if (processDefinitionId != null) query.processDefinitionId(processDefinitionId);
        if (businessKey != null) query.processInstanceBusinessKey(businessKey);
        if (processDefinitionKey != null)
        {
            if (tenantService.isEnabled() && deployWorkflowsInTenant) 
            {
                if (processDefinitionKey.startsWith("@" + TenantUtil.getCurrentDomain() + "@"))
                {
                    query.processDefinitionKey(processDefinitionKey);
                }
                else
                {
                    query.processDefinitionKey("@" + TenantUtil.getCurrentDomain() + "@" + processDefinitionKey);
                }
            }
            else
            {
                query.processDefinitionKey(processDefinitionKey);
            }
        }
        if (startUserId != null) query.startedBy(startUserId);
        if (startedAtGreaterThan != null) query.startedAfter(startedAtGreaterThan);
        if (startedAtLessThan != null) query.startedBefore(startedAtLessThan);
        if (endedAtGreaterThan != null) query.finishedAfter(endedAtGreaterThan);
        if (endedAtLessThan != null) query.finishedBefore(endedAtLessThan);
        
        if (status == null || PROCESS_STATUS_ACTIVE.equals(status))
        {
            query.unfinished();
        }
        else if (PROCESS_STATUS_COMPLETED.equals(status))
        {
            query.finished();
            query.notDeleted();
        }
        else if (PROCESS_STATUS_DELETED.equals(status))
        {
            query.deleted();
        }
        
        if (includeVariables != null && includeVariables) {
            query.includeProcessVariables();
        }
        
        List<QueryVariableHolder> variableProperties = propertyWalker.getVariableProperties();
        if (variableProperties != null)
        {
            for (QueryVariableHolder queryVariableHolder : variableProperties)
            {
                if (queryVariableHolder.getOperator() == WhereClauseParser.EQUALS)
                {    
                    query.variableValueEquals(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                }
                else if (queryVariableHolder.getOperator() == WhereClauseParser.GREATERTHAN)
                {
                    query.variableValueGreaterThan(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                }
                else if (queryVariableHolder.getOperator() == WhereClauseParser.GREATERTHANOREQUALS)
                {
                    query.variableValueGreaterThanOrEqual(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                }
                else if (queryVariableHolder.getOperator() == WhereClauseParser.LESSTHAN)
                {
                    query.variableValueLessThan(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                }
                else if (queryVariableHolder.getOperator() == WhereClauseParser.LESSTHANOREQUALS)
                {
                    query.variableValueLessThanOrEqual(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                }
                else if (queryVariableHolder.getOperator() == WhereClauseParser.MATCHES)
                {
                    if (queryVariableHolder.getPropertyValue() instanceof String == false)
                    {
                        throw new InvalidArgumentException("the matches operator can only be used with a String value for property " + queryVariableHolder.getPropertyName());
                    }
                    query.variableValueLike(queryVariableHolder.getPropertyName(), (String) queryVariableHolder.getPropertyValue());
                }
                else if (queryVariableHolder.getOperator() == WhereClauseParser.NEGATION)
                {
                    query.variableValueNotEquals(queryVariableHolder.getPropertyName(), queryVariableHolder.getPropertyValue());
                }
                else
                {
                    throw new InvalidArgumentException("variable " + queryVariableHolder.getPropertyName() + 
                            " can only be used with an =, >, >=, <=, <, not, matches comparison type");
                }
            }
        }
        
        if (authorityService.isAdminAuthority(AuthenticationUtil.getRunAsUser())) 
        {
            // Admin is allowed to read all processes in the current tenant
            if (tenantService.isEnabled()) 
            {
                query.variableValueEquals(ActivitiConstants.VAR_TENANT_DOMAIN, TenantUtil.getCurrentDomain());
            }
        }
        else
        {
            // If non-admin user, involvement in the process is required (either owner, assignee or externally involved).
            query.involvedUser(AuthenticationUtil.getRunAsUser());
        }
        
        if (sortColumn != null)
        {
            if (PROCESS_COLLECTION_SORT_PROPERTIES.contains(sortColumn.column))
            {
                if ("processDefinitionId".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByProcessDefinitionId();
                }
                else if ("id".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByProcessInstanceId();
                }
                else if ("businessKey".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByProcessInstanceBusinessKey();
                }
                else if ("startedAt".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByProcessInstanceStartTime();
                }
                else if ("endedAt".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByProcessInstanceEndTime();
                }
                else if ("durationInMillis".equalsIgnoreCase(sortColumn.column))
                {
                    query.orderByProcessInstanceDuration();
                }
            }
            else
            {
                throw new InvalidArgumentException("sort " + sortColumn.column + 
                        " is not supported, supported items are " + PROCESS_COLLECTION_SORT_PROPERTIES.toArray());
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
            query.orderByProcessInstanceStartTime().desc();
        }
        
        List<HistoricProcessInstance> processInstances = query.listPage(paging.getSkipCount(), paging.getMaxItems());
        int totalCount = (int) query.count();

        List<ProcessInfo> page = new ArrayList<ProcessInfo>(processInstances.size());
        Map<String, TypeDefinition> definitionTypeMap = new HashMap<String, TypeDefinition>();
        for (HistoricProcessInstance processInstance: processInstances) 
        {
            ProcessInfo processInfo = createProcessInfo(processInstance);
            if (includeVariables != null && includeVariables) 
            {
                if (definitionTypeMap.containsKey(processInfo.getProcessDefinitionId()) == false)
                {
                    StartFormData startFormData = activitiProcessEngine.getFormService().getStartFormData(processInfo.getProcessDefinitionId());
                    if (startFormData != null)
                    {
                        String formKey = startFormData.getFormKey();
                        definitionTypeMap.put(processInfo.getProcessDefinitionId(), getWorkflowFactory().getTaskFullTypeDefinition(formKey, true));
                    }
                }
                
                if (definitionTypeMap.containsKey(processInfo.getProcessDefinitionId()))
                {
                    // Convert raw variables to Variable objects
                    List<Variable> resultingVariables = restVariableHelper.getVariables(
                            processInstance.getProcessVariables(), definitionTypeMap.get(processInfo.getProcessDefinitionId()));
                    processInfo.setProcessVariables(resultingVariables);
                }
            }
            page.add(processInfo);
        }
        
        return CollectionWithPagingInfo.asPaged(paging, page, (page.size() + paging.getSkipCount()) != totalCount, totalCount);
    }

    @Override
    public ProcessInfo getProcess(String processId)
    {
        if (processId == null) 
        {
            throw new InvalidArgumentException("processId is required to get the process info");
        }
        
        HistoricProcessInstance processInstance = activitiProcessEngine
                .getHistoryService()
                .createHistoricProcessInstanceQuery()
                .processInstanceId(processId)
                .singleResult();
        
        if (processInstance == null) 
        {
            throw new EntityNotFoundException(processId);
        }

        return createProcessInfo(processInstance);
    }

    @Override
    public ProcessInfo create(ProcessInfo process)
    {
        if (process == null) 
        {
            throw new InvalidArgumentException("post body expected when starting a new process instance");
        }
        
        boolean definitionExistingChecked = false;
        RuntimeService runtimeService = activitiProcessEngine.getRuntimeService();
        String processDefinitionId = null;
        if (process.getProcessDefinitionId() != null) 
        {
            processDefinitionId = process.getProcessDefinitionId();
        } 
        else if (process.getProcessDefinitionKey() != null) 
        {
            ProcessDefinition definition = activitiProcessEngine
                    .getRepositoryService()
                    .createProcessDefinitionQuery()
                    .processDefinitionKey(getProcessDefinitionKey(process.getProcessDefinitionKey()))
                    .latestVersion()
                    .singleResult();
            
            if (definition == null)
            {
                throw new InvalidArgumentException("No workflow definition could be found with key '" + process.getProcessDefinitionKey() +"'.");
            }
            
            processDefinitionId = definition.getId();
            definitionExistingChecked = true;
        } 
        else 
        {
            throw new InvalidArgumentException("Either processDefinitionId or processDefinitionKey is required");
        }
        
        if (definitionExistingChecked == false)
        {
            // Check if the required definition actually exists
            ProcessDefinitionQuery query = activitiProcessEngine
                    .getRepositoryService()
                    .createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId);
            
            if (tenantService.isEnabled() && deployWorkflowsInTenant) 
            {
                query.processDefinitionKeyLike("@" + TenantUtil.getCurrentDomain() + "@%");
            }
            
            if(query.count() == 0)
            {
                throw new InvalidArgumentException("No workflow definition could be found with id '" + processDefinitionId +"'.");
            }
        }
        
        Map<QName, Serializable> startParams = new HashMap<QName, Serializable>();
        
        StartFormData startFormData = activitiProcessEngine.getFormService().getStartFormData(processDefinitionId);
        if (startFormData != null)
        {
            if (CollectionUtils.isEmpty(process.getVariables()) == false)
            {
                TypeDefinition startTaskType = getWorkflowFactory().getTaskFullTypeDefinition(startFormData.getFormKey(), true);
                
                // Lookup type definition for the startTask
                Map<QName, PropertyDefinition> taskProperties = startTaskType.getProperties();
                Map<String, QName> propNameMap = new HashMap<String, QName>();
                for (QName key : taskProperties.keySet())
                {
                    propNameMap.put(key.getPrefixString().replace(':', '_'), key);
                }
                
                Map<QName, AssociationDefinition> taskAssociations = startTaskType.getAssociations();
                for (QName key : taskAssociations.keySet())
                {
                    propNameMap.put(key.getPrefixString().replace(':', '_'), key);
                }
                
                for (String variableName : process.getVariables().keySet())
                {
                    if (propNameMap.containsKey(variableName))
                    {
                        Object variableValue = process.getVariables().get(variableName);
                        if (taskAssociations.containsKey(propNameMap.get(variableName)))
                        {
                            AssociationDefinition associationDef = taskAssociations.get(propNameMap.get(variableName));
                            variableValue = convertAssociationDefinitionValue(associationDef, variableName, variableValue);
                        }
                        else if (taskProperties.containsKey(propNameMap.get(variableName)))
                        {
                            PropertyDefinition propDef = taskProperties.get(propNameMap.get(variableName));
                            DataTypeDefinition propDataType = propDef.getDataType();
                            if ("java.util.Date".equalsIgnoreCase(propDataType.getJavaClassName()))
                            {
                                // fix for different ISO 8601 Date format classes in Alfresco (org.alfresco.util and Spring Surf)
                                variableValue = ISO8601DateFormat.parse((String) variableValue);
                            }
                        }
                        
                        if (variableValue instanceof Serializable)
                        {
                            startParams.put(propNameMap.get(variableName), (Serializable) variableValue);
                        }
                    } 
                }
            }
        }
        
        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        Authentication.setAuthenticatedUserId(currentUserName);
        
        NodeRef workflowPackageNodeRef = null;
        try 
        {
            workflowPackageNodeRef = workflowPackageComponent.createPackage(null);
            startParams.put(WorkflowModel.ASSOC_PACKAGE, workflowPackageNodeRef);
        } 
        catch (Exception e) 
        {
            throw new ApiException("couldn't create workflow package: " + e.getMessage(), e);
        }
        
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(process.getItems())) 
        {
            try 
            {
                for (String item: process.getItems()) 
                {
                    NodeRef itemNodeRef = getNodeRef(item);
                    QName workflowPackageItemId = QName.createQName("wpi", itemNodeRef.toString());
                    nodeService.addChild(workflowPackageNodeRef, itemNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, workflowPackageItemId);
                }
            } 
            catch (Exception e) 
            {
                throw new ApiException("Error while adding items to package: " + e.getMessage(), e);
            }
        }
        
        // Set start task properties. This should be done before instance is started, since it's id will be used
        Map<String, Object> variables = getPropertyConverter().getStartVariables(processDefinitionId, startParams);
        variables.put(WorkflowConstants.PROP_CANCELLED, Boolean.FALSE);
        
        // Add company home
        Object companyHome = getNodeConverter().convertNode(repositoryHelper.getCompanyHome());
        variables.put(WorkflowConstants.PROP_COMPANY_HOME, companyHome);
         
        // Add the initiator
        NodeRef initiator = getPersonNodeRef(currentUserName);
        if (initiator != null)
        {
            variables.put(WorkflowConstants.PROP_INITIATOR, nodeConverter.convertNode(initiator));
            // Also add the initiator home reference, if one exists
            NodeRef initiatorHome = (NodeRef) nodeService.getProperty(initiator, ContentModel.PROP_HOMEFOLDER);
            if (initiatorHome != null)
            {
                variables.put(WorkflowConstants.PROP_INITIATOR_HOME, nodeConverter.convertNode(initiatorHome));
            }
        }
        
        if (tenantService.isEnabled()) 
        {
            // Specify which tenant domain the workflow was started in.
            variables.put(ActivitiConstants.VAR_TENANT_DOMAIN, TenantUtil.getCurrentDomain());
        }
        
        // Start the process-instance
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId, process.getBusinessKey(), variables);
        
        if (processInstance.isEnded() == false)
        {
            runtimeService.setVariable(processInstance.getProcessInstanceId(), ActivitiConstants.PROP_START_TASK_END_DATE, new Date());
        }
        
        HistoricProcessInstance historicProcessInstance = activitiProcessEngine
            .getHistoryService()
            .createHistoricProcessInstanceQuery()
            .processInstanceId(processInstance.getId())
            .singleResult();
        
        return createProcessInfo(historicProcessInstance);
    }
    
    @Override
    public void deleteProcess(String id)
    {
        validateIfUserAllowedToWorkWithProcess(id);
        
        ProcessInstance processInstance = activitiProcessEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceId(id)
                .singleResult();
        
        if (processInstance == null)
        {
            throw new EntityNotFoundException(id);
        }
        
        activitiProcessEngine.getRuntimeService().deleteProcessInstance(id, "deleted through REST API call");
    }
    
    @Override
    public CollectionWithPagingInfo<Item> getItems(String processId, Paging paging)
    {
        if (processId == null) 
        {
            throw new InvalidArgumentException("processId is required to get the attached items of a process instance");
        }
        
        validateIfUserAllowedToWorkWithProcess(processId);
        return getItemsFromProcess(processId, paging);
    }
    
    @Override
    public Item getItem(String processId, String itemId)
    {
        if (processId == null) 
        {
            throw new InvalidArgumentException("processId is required to get the attached item of a process instance");
        }
        
        if (itemId == null) 
        {
            throw new InvalidArgumentException("itemId is required to get an attached item");
        }
        
        validateIfUserAllowedToWorkWithProcess(processId);
        return getItemFromProcess(itemId, processId);
    }

    @Override
    public Item createItem(String processId, Item item)
    {
        if (processId == null) 
        {
            throw new InvalidArgumentException("processId is required to add an attached item to a process instance");
        }
        
        if (item == null || item.getId() == null) 
        {
            throw new InvalidArgumentException("itemId is required to add an attached item");
        }
        
        validateIfUserAllowedToWorkWithProcess(processId);
        return createItemInProcess(item.getId(), processId);
    }

    @Override
    public void deleteItem(String processId, String itemId)
    {
        if (processId == null) 
        {
            throw new InvalidArgumentException("processId is required to delete an attached item to a process instance");
        }
        
        if (itemId == null) 
        {
            throw new InvalidArgumentException("itemId is required to delete an attached item");
        }
        
        validateIfUserAllowedToWorkWithProcess(processId);
        deleteItemFromProcess(itemId, processId);
    }
    
    @Override
    public CollectionWithPagingInfo<Variable> getVariables(String processId, Paging paging)
    {
        CollectionWithPagingInfo<Variable> result = null;
        
        // Check if user is allowed to get variables
        List<HistoricVariableInstance> variableInstances = validateIfUserAllowedToWorkWithProcess(processId);
        Map<String, Object> variables = new HashMap<String, Object>();
        for (HistoricVariableInstance variable : variableInstances)
        {
            variables.put(variable.getVariableName(), variable.getValue());
        }
        
        ProcessInstance processInstance = activitiProcessEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceId(processId)
                .singleResult();
        
        String processDefinitionId = null;
        
        if (processInstance != null)
        {
            processDefinitionId = processInstance.getProcessDefinitionId();
        }
        else
        {
            // Completed process instance
            HistoricProcessInstance historicInstance = activitiProcessEngine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(processId).singleResult();
            
            if (historicInstance == null)
            {
                throw new EntityNotFoundException(processId);
            }
            
            processDefinitionId = historicInstance.getProcessDefinitionId();
        }

        // Get start-task definition for explicit typing of variables submitted at the start
        String formKey = null;
        StartFormData startFormData = activitiProcessEngine.getFormService().getStartFormData(processDefinitionId);
        if (startFormData != null)
        {
            formKey = startFormData.getFormKey();
        }
        
        TypeDefinition startTaskTypeDefinition = getWorkflowFactory().getTaskFullTypeDefinition(formKey, true);
        
        // Convert raw variables to Variable objects
        List<Variable> resultingVariables = restVariableHelper.getVariables(variables, startTaskTypeDefinition);
        result = CollectionWithPagingInfo.asPaged(paging, resultingVariables);
        return result;
    }
    
    @Override
    public Variable updateVariable(String processId, Variable variable)
    {
        validateIfUserAllowedToWorkWithProcess(processId);
        
        ProcessInstance processInstance = activitiProcessEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceId(processId)
                .singleResult();
        
        if (processInstance == null)
        {
            throw new EntityNotFoundException(processId);
        }
        
        return updateVariableInProcess(processId, processInstance.getProcessDefinitionId(), variable);
    }
    
    @Override
    public List<Variable> updateVariables(String processId, List<Variable> variables) 
    {
        validateIfUserAllowedToWorkWithProcess(processId);
        
        ProcessInstance processInstance = activitiProcessEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceId(processId)
                .singleResult();
        
        if (processInstance == null)
        {
            throw new EntityNotFoundException(processId);
        }
        
        List<Variable> updatedVariables = new ArrayList<Variable>();
        if (variables != null)
        {
            for (Variable variable : variables)
            {
                updatedVariables.add(updateVariableInProcess(processId, processInstance.getProcessDefinitionId(), variable));
            }
        }
        return updatedVariables;
    }
    
    protected Variable updateVariableInProcess(String processId, String processDefinitionId, Variable variable)
    {
        if (variable.getName() == null)
        {
            throw new InvalidArgumentException("Variable name is required.");
        }
        
        // Get start-task definition for explicit typing of variables submitted at the start
        String formKey = null;
        StartFormData startFormData = activitiProcessEngine.getFormService().getStartFormData(processDefinitionId);
        if (startFormData != null)
        {
            formKey = startFormData.getFormKey();
        }
        
        DataTypeDefinition dataTypeDefinition = null;
        
        TypeDefinition startTaskTypeDefinition = getWorkflowFactory().getTaskFullTypeDefinition(formKey, true);
        TypeDefinitionContext context = new TypeDefinitionContext(startTaskTypeDefinition, getQNameConverter());
        if (context.getPropertyDefinition(variable.getName()) != null) 
        {
            dataTypeDefinition = context.getPropertyDefinition(variable.getName()).getDataType();
            if (variable.getType() != null && dataTypeDefinition.getName().toPrefixString(namespaceService).equals(variable.getType()) == false) {
                throw new InvalidArgumentException("type of variable " + variable.getName() + " should be " + 
                        dataTypeDefinition.getName().toPrefixString(namespaceService));
            }
        }
        else if (context.getAssociationDefinition(variable.getName()) != null) 
        {
            dataTypeDefinition = dictionaryService.getDataType(DataTypeDefinition.NODE_REF);
        }
        
        if (dataTypeDefinition == null && variable.getType() != null)
        {
            try
            {
                QName dataType = QName.createQName(variable.getType(), namespaceService);
                dataTypeDefinition = dictionaryService.getDataType(dataType);
            }
            catch(InvalidQNameException iqne)
            {
                throw new InvalidArgumentException("Unsupported type of variable: '" + variable.getType() +"'.");
            }
        } 
        else if (dataTypeDefinition == null)
        {
            // Fallback to raw value when no type has been passed and not present in model
            dataTypeDefinition = dictionaryService.getDataType(restVariableHelper.extractTypeFromValue(variable.getValue()));
        }
        
        if (dataTypeDefinition == null)
        {
            throw new InvalidArgumentException("Unsupported type of variable: '" + variable.getType() +"'.");
        }

        Object actualValue = null;
        if ("java.util.Date".equalsIgnoreCase(dataTypeDefinition.getJavaClassName()))
        {
            // fix for different ISO 8601 Date format classes in Alfresco (org.alfresco.util and Spring Surf)
            actualValue = ISO8601DateFormat.parse((String) variable.getValue());
        }
        else
        {
            if (context.getAssociationDefinition(variable.getName()) != null)
            {
                actualValue = convertAssociationDefinitionValue(context.getAssociationDefinition(variable.getName()), 
                        variable.getName(), variable.getValue());
            }
            else
            {
                actualValue = DefaultTypeConverter.INSTANCE.convert(dataTypeDefinition, variable.getValue());
            }
        }
        variable.setValue(actualValue);
        
        activitiProcessEngine.getRuntimeService().setVariable(processId, variable.getName(), actualValue);
        
        // Set actual used type before returning
        variable.setType(dataTypeDefinition.getName().toPrefixString(namespaceService));
        return variable;
    }
    
    @Override
    public void deleteVariable(String processId, String variableName)
    {
        validateIfUserAllowedToWorkWithProcess(processId);
        
        if(variableName == null)
        {
            throw new InvalidArgumentException("Variable name is required.");
        }

        try
        {
            if (activitiProcessEngine.getRuntimeService().hasVariable(processId, variableName) == false)
            {
                throw new EntityNotFoundException(variableName);
            }
            activitiProcessEngine.getRuntimeService().removeVariable(processId, variableName);
        }
        catch(ActivitiObjectNotFoundException aonfe)
        {
            throw new EntityNotFoundException(processId);
        }
    }
    
    @Override
    public BinaryResource getProcessImage(String processId)
    {
        validateIfUserAllowedToWorkWithProcess(processId);
        
        ProcessInstance processInstance = activitiProcessEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceId(processId)
                .singleResult();
        
        if (processInstance == null)
        {
            throw new EntityNotFoundException(processId);
        }
        
        try
        {
        	BpmnModel model = activitiProcessEngine.getRepositoryService().getBpmnModel(processInstance.getProcessDefinitionId());
            
            if(model != null && model.getLocationMap().size() > 0)
            {
                List<String> activeActivities = activitiProcessEngine.getRuntimeService().getActiveActivityIds(processId);
                InputStream generateDiagram = ProcessDiagramGenerator.generateDiagram(model, "png", activeActivities);
                
                File file = TempFileProvider.createTempFile(processId + UUID.randomUUID(), ".png");
                FileOutputStream fos = new FileOutputStream(file);
                IOUtils.copy(generateDiagram, fos);
                fos.close();
                
                return new FileBinaryResource(file);
            }
            else
            {
                throw new EntityNotFoundException(processId + "/image");
            }
        }
        catch (IOException error)
        {
            throw new ApiException("Error while getting process image.");
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
    
    protected String getProcessDefinitionKey(String paramProcessDefinitionKey)
    {
        String processDefinitionKey = null;
        if (tenantService.isEnabled() && deployWorkflowsInTenant) 
        {
            processDefinitionKey = "@" + TenantUtil.getCurrentDomain() + "@" + paramProcessDefinitionKey;
        }
        else
        {
            processDefinitionKey = paramProcessDefinitionKey;
        }
        return processDefinitionKey;
    }
    
    protected String getLocalProcessDefinitionKey(String key)
    {
        String processDefKey = null;
        if (tenantService.isEnabled() && deployWorkflowsInTenant)
        {
            processDefKey = key.substring(key.lastIndexOf("@") + 1);
        }
        else
        {
            processDefKey = key;
        }
        return processDefKey;
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
    
    protected ProcessInfo createProcessInfo(HistoricProcessInstance processInstance)
    {
        ProcessInfo processInfo = new ProcessInfo(processInstance);
        ProcessDefinition definitionEntity = activitiProcessEngine.getRepositoryService()
                .getProcessDefinition(processInstance.getProcessDefinitionId());
        processInfo.setProcessDefinitionKey(getLocalProcessDefinitionKey(definitionEntity.getKey()));
        return processInfo;
    }
}