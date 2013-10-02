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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.workflow.WorkflowDeployer;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowObjectFactory;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.FileBinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.ProcessDefinitions;
import org.alfresco.rest.workflow.api.model.FormModelElement;
import org.alfresco.rest.workflow.api.model.ProcessDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.IOUtils;

public class ProcessDefinitionsImpl extends WorkflowRestImpl implements ProcessDefinitions
{
    private static final Set<String> PROCESS_DEFINITION_COLLECTION_EQUALS_QUERY_PROPERTIES = new HashSet<String>(Arrays.asList(new String[] {
        "category", "key", "name", "deploymentId", "version"
    }));
    
    private static final Set<String> PROCESS_DEFINITION_COLLECTION_MATCHES_QUERY_PROPERTIES = new HashSet<String>(Arrays.asList(new String[] {
        "category", "key", "name"
    }));
    
    private static final Set<String> PROCESS_DEFINITION_COLLECTION_SORT_PROPERTIES = new HashSet<String>(Arrays.asList(
        "deploymentId", "key", "category", "id", "version", "name"
    ));
    
    MessageService messageService;
    String engineId;
    
    WorkflowQNameConverter qNameConverter;
    QName defaultStartTaskType = WorkflowModel.TYPE_ACTIVTI_START_TASK;
    WorkflowObjectFactory workflowFactory;
    
    public void setMessageService(MessageService messageService)
    {
        this.messageService = messageService;
    }
    
    public void setEngineId(String engineId)
    {
        this.engineId = engineId;
    }

    @Override
    public CollectionWithPagingInfo<ProcessDefinition> getProcessDefinitions(Parameters parameters)
    {
        ProcessDefinitionQuery query = activitiProcessEngine
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionCategoryNotEquals(WorkflowDeployer.CATEGORY_ALFRESCO_INTERNAL);
        
        MapBasedQueryWalker propertyWalker = new MapBasedQueryWalker(PROCESS_DEFINITION_COLLECTION_EQUALS_QUERY_PROPERTIES, 
                    PROCESS_DEFINITION_COLLECTION_MATCHES_QUERY_PROPERTIES);
        
        boolean keyQueryIncluded = false;
        
        if(parameters.getQuery() != null)
        {
            QueryHelper.walk(parameters.getQuery(), propertyWalker);
            
            // Property equals
            String categoryProperty = propertyWalker.getProperty("category", WhereClauseParser.EQUALS);
            if (categoryProperty != null) 
            {
                query.processDefinitionCategory(categoryProperty);
            }
            
            String keyProperty = propertyWalker.getProperty("key", WhereClauseParser.EQUALS);
            if (keyProperty != null) 
            {
                query.processDefinitionKey(getProcessDefinitionKey(keyProperty));
                keyQueryIncluded = true;
            }
            
            String nameProperty = propertyWalker.getProperty("name", WhereClauseParser.EQUALS);
            if (nameProperty != null) 
            {
                query.processDefinitionName(nameProperty);
            }
            
            Integer versionProperty = propertyWalker.getProperty("version", WhereClauseParser.EQUALS, Integer.class);
            if (versionProperty != null) 
            {
                query.processDefinitionVersion(versionProperty);
            }
            
            String deploymentProperty = propertyWalker.getProperty("deploymentId", WhereClauseParser.EQUALS);
            if (deploymentProperty != null) 
            {
                query.deploymentId(deploymentProperty);
            }
            
            // Property matches
            String categoryMatchesProperty = propertyWalker.getProperty("category", WhereClauseParser.MATCHES);
            if (categoryMatchesProperty != null) 
            {
                query.processDefinitionCategoryLike(categoryMatchesProperty);
            }
            
            String keyMatchesProperty = propertyWalker.getProperty("key", WhereClauseParser.MATCHES);
            if (keyMatchesProperty != null) 
            {
                query.processDefinitionKeyLike(getProcessDefinitionKey(keyMatchesProperty));
                keyQueryIncluded = true;
            }
            
            String nameLikeProperty = propertyWalker.getProperty("name", WhereClauseParser.MATCHES);
            if (nameLikeProperty != null) 
            {
                query.processDefinitionNameLike(nameLikeProperty);
            }
        }
        
        // Filter based on tenant, if required
        if (keyQueryIncluded == false && tenantService.isEnabled() && deployWorkflowsInTenant) 
        {
            query.processDefinitionKeyLike("@" + TenantUtil.getCurrentDomain() + "@%");
        }
        
        String sortParam = parameters.getParameter("sort");
        if (sortParam != null)
        {
            if (PROCESS_DEFINITION_COLLECTION_SORT_PROPERTIES.contains(sortParam))
            {
                if ("id".equalsIgnoreCase(sortParam))
                {
                    query.orderByProcessDefinitionId();
                }
                else if ("deploymentId".equalsIgnoreCase(sortParam))
                {
                    query.orderByDeploymentId();
                }
                else if ("key".equalsIgnoreCase(sortParam))
                {
                    query.orderByProcessDefinitionKey();
                }
                else if ("category".equalsIgnoreCase(sortParam))
                {
                    query.orderByProcessDefinitionCategory();
                }
                else if ("version".equalsIgnoreCase(sortParam))
                {
                    query.orderByProcessDefinitionVersion();
                }
                else if ("name".equalsIgnoreCase(sortParam))
                {
                    query.orderByProcessDefinitionName();
                }
            }
            else
            {
                throw new InvalidArgumentException("sort " + sortParam + 
                        " is not supported, supported items are " + PROCESS_DEFINITION_COLLECTION_SORT_PROPERTIES.toArray());
            }
            
            String sortOrderParam = parameters.getParameter("sortOrder");
            if (sortOrderParam != null)
            {
                if ("asc".equalsIgnoreCase(sortOrderParam))
                {
                    query.asc();
                }
                else if ("desc".equalsIgnoreCase(sortOrderParam))
                {
                    query.desc();
                }
                else
                {
                    throw new InvalidArgumentException("sort order " + sortOrderParam + 
                            " is not supported, supported items are asc and desc");
                }
            }
        }
        else
        {
            query.orderByProcessDefinitionId().asc();
        }
        
        List<org.activiti.engine.repository.ProcessDefinition> processDefinitions = 
                query.listPage(parameters.getPaging().getSkipCount(), parameters.getPaging().getMaxItems());
        int totalCount = (int) query.count();

        List<ProcessDefinition> page = new ArrayList<ProcessDefinition>(processDefinitions.size());
        for (org.activiti.engine.repository.ProcessDefinition processDefinition: processDefinitions) 
        {
            page.add(createProcessDefinitionRest((ProcessDefinitionEntity) processDefinition));
        }
        
        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), page, page.size() != totalCount, totalCount);
    }

    @Override
    public ProcessDefinition getProcessDefinition(String definitionId)
    {
        ProcessDefinitionQuery query = activitiProcessEngine
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionId(definitionId);
        
        if (tenantService.isEnabled() && deployWorkflowsInTenant) 
        {
            query.processDefinitionKeyLike("@" + TenantUtil.getCurrentDomain() + "@%");
        }
        
        org.activiti.engine.repository.ProcessDefinition processDefinition = query.singleResult();
        
        if (processDefinition == null) 
        {
            throw new EntityNotFoundException(definitionId); 
        }

        ProcessDefinition deploymentRest = createProcessDefinitionRest((ProcessDefinitionEntity) processDefinition);
        return deploymentRest;
    }
    
    @Override
    public BinaryResource getProcessDefinitionImage(String definitionId)
    {
    	ProcessDefinitionQuery query = activitiProcessEngine
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionId(definitionId);
        
        if (tenantService.isEnabled() && deployWorkflowsInTenant) 
        {
            query.processDefinitionKeyLike("@" + TenantUtil.getCurrentDomain() + "@%");
        }
        
        org.activiti.engine.repository.ProcessDefinition processDefinition = query.singleResult();
        
        if (processDefinition == null) 
        {
            throw new EntityNotFoundException(definitionId); 
        }
        
        try
        {
        	InputStream processDiagram = activitiProcessEngine.getRepositoryService().getProcessDiagram(definitionId);
        	if (processDiagram != null) 
        	{
	            File file = TempFileProvider.createTempFile(definitionId + UUID.randomUUID(), ".png");
	            FileOutputStream fos = new FileOutputStream(file);
	            IOUtils.copy(processDiagram, fos);
	            fos.close();
	                
	            return new FileBinaryResource(file);
        	}
        	else
        	{
        		throw new ApiException("No image available for definitionId " + definitionId); 
        	}
        }
        catch (IOException error)
        {
            throw new ApiException("Error while getting process definition image.");
        }
    }

    @Override
    public CollectionWithPagingInfo<FormModelElement> getStartFormModel(String definitionId, Paging paging)
    {
        // first validate if user is allowed to access the process definition if workflows are deployed per tenant
        if (tenantService.isEnabled() && deployWorkflowsInTenant) 
        {
            ProcessDefinitionQuery query = activitiProcessEngine
                    .getRepositoryService()
                    .createProcessDefinitionQuery()
                    .processDefinitionId(definitionId);
        
            query.processDefinitionKeyLike("@" + TenantUtil.getCurrentDomain() + "@%");
            org.activiti.engine.repository.ProcessDefinition processDefinition = query.singleResult();
            
            if (processDefinition == null) 
            {
                throw new EntityNotFoundException(definitionId); 
            }
        }
        
        StartFormData startFormData = activitiProcessEngine.getFormService().getStartFormData(definitionId);
        if (startFormData == null)
        {
            throw new EntityNotFoundException(definitionId);
        }
        
        if (qNameConverter == null)
        {
            qNameConverter = new WorkflowQNameConverter(namespaceService);
        }
        if (workflowFactory == null) 
        {
            workflowFactory = new WorkflowObjectFactory(qNameConverter, tenantService, messageService, dictionaryService, engineId, defaultStartTaskType);
        }
        
        // Lookup type definition for the startTask
        TypeDefinition startTaskType = workflowFactory.getTaskFullTypeDefinition(startFormData.getFormKey(), true);
        return getFormModelElements(startTaskType, paging);
    }
    
    protected String getProcessDefinitionKey(String key)
    {
        String processDefKey = null;
        if (tenantService.isEnabled() && deployWorkflowsInTenant)
        {
            processDefKey = "@" + TenantUtil.getCurrentDomain() + "@" + key;
        }
        else
        {
            processDefKey = key;
        }
        return processDefKey;
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

    protected ProcessDefinition createProcessDefinitionRest(ProcessDefinitionEntity processDefinition)
    {
        ProcessDefinition processDefinitionRest = new ProcessDefinition(processDefinition);
        String localKey = getLocalProcessDefinitionKey(processDefinition.getKey());
        processDefinitionRest.setKey(localKey);
        
        String displayId = localKey + ".workflow";
        processDefinitionRest.setTitle(getLabel(displayId, "title"));
        processDefinitionRest.setDescription(getLabel(displayId, "description"));
       
        processDefinitionRest.setGraphicNotationDefined(processDefinition.isGraphicalNotationDefined());
        if (processDefinition.hasStartFormKey()) 
        {
            try 
            {
                StartFormData startFormData = activitiProcessEngine.getFormService().getStartFormData(processDefinition.getId());
                if (startFormData != null) 
                {
                    processDefinitionRest.setStartFormResourceKey(startFormData.getFormKey());
                }
            }
            catch (Exception e) 
            {
                throw new ApiException("Error while retrieving start form key");
            }
        }
        return processDefinitionRest;
    }
    
    protected String getLabel(String displayId, String labelKey)
    {
        String keyBase = displayId.replace(":", "_");
        String key = keyBase+ "." + labelKey;
        String label = messageService.getMessage(key);
        return label;
    }
}
