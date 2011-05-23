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

import org.activiti.engine.ProcessEngine;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.DefaultWorkflowPropertyHandler;
import org.alfresco.repo.workflow.WorkflowAuthorityManager;
import org.alfresco.repo.workflow.WorkflowObjectFactory;
import org.alfresco.repo.workflow.WorkflowPropertyHandlerRegistry;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.repo.workflow.activiti.properties.ActivitiPropertyConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public class ActivitiWorkflowManagerFactory implements FactoryBean<ActivitiWorkflowManager>
{
    // Set fields
    private TenantService tenantService;
    private MessageService messageService;
    private ServiceRegistry serviceRegistry;
    private SearchService unprotectedSearchService;
    private BPMEngineRegistry bpmEngineRegistry;
    private AuthorityDAO authorityDAO;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private PersonService personService;

    private ProcessEngine processEngine;
    
    private String engineId;
    private String companyHomePath;
    private String companyHomeStore;
    
    /**
    * {@inheritDoc}
    */
    @Override
    public ActivitiWorkflowManager getObject() throws Exception
    {
        if (messageService ==null)
        {
            throw new WorkflowException("MessageService not specified");
        }
        if (serviceRegistry ==null)
        {
            throw new WorkflowException("ServiceRegistry not specified");
        }
        if (tenantService ==null)
        {
            throw new WorkflowException("TenantService not specified");
        }
        ActivitiNodeConverter nodeConverter = new ActivitiNodeConverter(serviceRegistry);
        DefaultWorkflowPropertyHandler defaultPropertyHandler = new DefaultWorkflowPropertyHandler();
        defaultPropertyHandler.setMessageService(messageService);
        defaultPropertyHandler.setNodeConverter(nodeConverter);
        
        WorkflowQNameConverter qNameConverter = new WorkflowQNameConverter(namespaceService);
        WorkflowPropertyHandlerRegistry handlerRegistry = new WorkflowPropertyHandlerRegistry(defaultPropertyHandler, qNameConverter);
        
        WorkflowAuthorityManager authorityManager = new WorkflowAuthorityManager(authorityDAO);
        WorkflowObjectFactory factory = new WorkflowObjectFactory(qNameConverter, tenantService, messageService, dictionaryService, engineId);
        ActivitiUtil activitiUtil = new ActivitiUtil(processEngine);
        ActivitiPropertyConverter propertyConverter = new ActivitiPropertyConverter(activitiUtil, factory, handlerRegistry, authorityManager, messageService, nodeConverter);
        ActivitiTypeConverter typeConverter = new ActivitiTypeConverter(processEngine, factory, propertyConverter);
        
        ActivitiWorkflowEngine workflowEngine = new ActivitiWorkflowEngine();
        workflowEngine.setActivitiUtil(activitiUtil);
        workflowEngine.setAuthorityManager(authorityManager);
        workflowEngine.setBPMEngineRegistry(bpmEngineRegistry);
        workflowEngine.setCompanyHomePath(companyHomePath);
        workflowEngine.setCompanyHomeStore(companyHomeStore);
        workflowEngine.setEngineId(engineId);
        workflowEngine.setFactory(factory);
        workflowEngine.setMessageService(messageService);
        workflowEngine.setNamespaceService(namespaceService);
        workflowEngine.setNodeConverter(nodeConverter);
        workflowEngine.setNodeService(nodeService);
        workflowEngine.setPersonService(personService);
        workflowEngine.setPropertyConverter(propertyConverter);
        workflowEngine.setTenantService(tenantService);
        workflowEngine.setTypeConverter(typeConverter);
        workflowEngine.setUnprotectedSearchService(unprotectedSearchService);
        return new ActivitiWorkflowManager(workflowEngine, propertyConverter, handlerRegistry, nodeConverter, authorityManager);
    }

    /**
     * @param tenantService the tenantService to set
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * @param messageService the messageService to set
     */
    public void setMessageService(MessageService messageService)
    {
        this.messageService = messageService;
    }

    /**
     * @param serviceRegistry the serviceRegistry to set
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * @param unprotectedSearchService the unprotectedSearchService to set
     */
    public void setUnprotectedSearchService(SearchService unprotectedSearchService)
    {
        this.unprotectedSearchService = unprotectedSearchService;
    }

    /**
     * @param bpmEngineRegistry the bpmEngineRegistry to set
     */
    public void setBPMEngineRegistry(BPMEngineRegistry bpmEngineRegistry)
    {
        this.bpmEngineRegistry = bpmEngineRegistry;
    }

    /**
     * @param processEngine the processEngine to set
     */
    public void setProcessEngine(ProcessEngine processEngine)
    {
        this.processEngine = processEngine;
    }

    /**
     * @param engineId the engineId to set
     */
    public void setEngineId(String engineId)
    {
        this.engineId = engineId;
    }

    /**
     * @param companyHomePath the companyHomePath to set
     */
    public void setCompanyHomePath(String companyHomePath)
    {
        this.companyHomePath = companyHomePath;
    }

    /**
     * @param companyHomeStore the companyHomeStore to set
     */
    public void setCompanyHomeStore(String companyHomeStore)
    {
        this.companyHomeStore = companyHomeStore;
    }

    /**
     * @param authorityDAO
     *            the authorityDAO to set
     */
    public void setAuthorityDAO(AuthorityDAO authorityDAO)
    {
        this.authorityDAO = authorityDAO;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Class<? extends ActivitiWorkflowManager> getObjectType()
    {
        return ActivitiWorkflowManager.class;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public boolean isSingleton()
    {
        return true;
    }
    
    /**
     * @param namespaceService the namespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @param dictionaryService the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param personService the personService to set
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
}
