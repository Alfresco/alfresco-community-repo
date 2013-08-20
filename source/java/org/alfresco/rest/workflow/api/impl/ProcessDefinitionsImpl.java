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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.ProcessDefinitions;
import org.alfresco.rest.workflow.api.model.FormModelElement;
import org.alfresco.rest.workflow.api.model.ProcessDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

public class ProcessDefinitionsImpl extends WorkflowRestImpl implements ProcessDefinitions
{
    private static final Set<String> PROCESS_DEFINITION_COLLECTION_EQUALS_QUERY_PROPERTIES = new HashSet<String>(Arrays.asList(new String[] {
        "category", "key", "name", "deploymentId", "version"
    }));
    
    private static final Set<String> PROCESS_DEFINITION_COLLECTION_MATCHES_QUERY_PROPERTIES = new HashSet<String>(Arrays.asList(new String[] {
        "category", "key", "name"
    }));
    
    
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
                .processDefinitionCategoryNotEquals(WorkflowDeployer.CATEGORY_ALFRESCO_INTERNAL)
                .orderByProcessDefinitionName().asc()
                .orderByProcessDefinitionVersion().asc();
        
        // Filter based on tenant, if required
        if (tenantService.isEnabled() && deployWorkflowsInTenant) 
        {
            query.processDefinitionKeyLike("@" + TenantUtil.getCurrentDomain() + "@%");
        }
        
        MapBasedQueryWalker propertyWalker = new MapBasedQueryWalker(PROCESS_DEFINITION_COLLECTION_EQUALS_QUERY_PROPERTIES, 
                    PROCESS_DEFINITION_COLLECTION_MATCHES_QUERY_PROPERTIES);
        
        if(parameters.getQuery() != null)
        {
            QueryHelper.walk(parameters.getQuery(), propertyWalker);
            
            // Property equals
            if(propertyWalker.getProperty("category", WhereClauseParser.EQUALS) != null) {
                query.processDefinitionCategory(propertyWalker.getProperty("category", WhereClauseParser.EQUALS));
            }
            if(propertyWalker.getProperty("key", WhereClauseParser.EQUALS) != null) {
                query.processDefinitionKey(propertyWalker.getProperty("key", WhereClauseParser.EQUALS));
            }
            if(propertyWalker.getProperty("name", WhereClauseParser.EQUALS) != null) {
                query.processDefinitionName(propertyWalker.getProperty("name", WhereClauseParser.EQUALS));
            }
            if(propertyWalker.getProperty("version", WhereClauseParser.EQUALS) != null) {
                query.processDefinitionVersion(propertyWalker.getProperty("version", WhereClauseParser.EQUALS, Integer.class));
            }
            if(propertyWalker.getProperty("deploymentId", WhereClauseParser.EQUALS) != null) {
                query.deploymentId(propertyWalker.getProperty("deploymentId", WhereClauseParser.EQUALS));
            }
            
            // Property matches
            if(propertyWalker.getProperty("category", WhereClauseParser.MATCHES) != null) {
                query.processDefinitionCategoryLike(propertyWalker.getProperty("category", WhereClauseParser.MATCHES));
            }
            if(propertyWalker.getProperty("key", WhereClauseParser.MATCHES) != null) {
                query.processDefinitionKeyLike(propertyWalker.getProperty("key", WhereClauseParser.MATCHES));
            }
            if(propertyWalker.getProperty("name", WhereClauseParser.MATCHES) != null) {
                query.processDefinitionNameLike(propertyWalker.getProperty("name", WhereClauseParser.MATCHES));
            }
        }
        
        List<org.activiti.engine.repository.ProcessDefinition> processDefinitions = 
                query.listPage(parameters.getPaging().getSkipCount(), parameters.getPaging().getMaxItems());

        List<ProcessDefinition> page = new ArrayList<ProcessDefinition>(processDefinitions.size());
        for (org.activiti.engine.repository.ProcessDefinition processDefinition: processDefinitions) 
        {
            page.add(createProcessDefinitionRest((ProcessDefinitionEntity) processDefinition));
        }
        
        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), page, false, page.size());
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
    public CollectionWithPagingInfo<FormModelElement> getStartFormModel(String definitionId, Paging paging)
    {
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

    protected ProcessDefinition createProcessDefinitionRest(ProcessDefinitionEntity processDefinition)
    {
        ProcessDefinition processDefinitionRest = new ProcessDefinition(processDefinition);
        processDefinitionRest.setGraphicNotationDefined(processDefinition.isGraphicalNotationDefined());
        if (processDefinition.hasStartFormKey()) 
        {
            try {
                StartFormData startFormData = activitiProcessEngine.getFormService().getStartFormData(processDefinition.getId());
                if (startFormData != null) 
                {
                    processDefinitionRest.setStartFormResourceKey(startFormData.getFormKey());
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return processDefinitionRest;
    }
}
