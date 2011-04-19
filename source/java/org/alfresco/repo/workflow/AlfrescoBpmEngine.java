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

package org.alfresco.repo.workflow;

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QNameCache;

/**
 * @since 3.4.e
 * @author Nick Smith
 *
 */
public abstract class AlfrescoBpmEngine extends BPMEngine
{
    protected TenantService tenantService;
    protected MessageService messageService;
    protected NamespaceService namespaceService;
    protected DictionaryService dictionaryService;
    protected WorkflowObjectFactory factory;
    protected WorkflowAuthorityManager authorityManager;

    /**
    * {@inheritDoc}
    */
    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
        if (tenantService ==null)
        {
            throw new WorkflowException("TenantService not specified");
        }
        if (messageService ==null)
        {
            throw new WorkflowException("MessageService not specified");
        }
        if (namespaceService ==null)
        {
            throw new WorkflowException("NamespaceService not specified");
        }
        WorkflowQNameConverter qNameConverter = new WorkflowQNameConverter(namespaceService);
        this.factory = new WorkflowObjectFactory(qNameConverter, tenantService, messageService, dictionaryService, getEngineId());
    }
    
        /**
     * Sets the Tenant Service
     * 
     * @param tenantService
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /**
     * Sets the Message Service
     * 
     * @param messageService
     */
    public void setMessageService(MessageService messageService)
    {
        this.messageService = messageService;
    }

    /**
     * Sets the Namespace Service
     * 
     * @param namespaceService
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
     * @param factory the factory to set
     */
    public void setWorkflowObjectFactory(WorkflowObjectFactory factory)
    {
        this.factory = factory;
    }
    
    /**
     * @param authorityManager the authorityManager to set
     */
    public void setWorkflowAuthorityManager(WorkflowAuthorityManager authorityManager)
    {
        this.authorityManager = authorityManager;
    }
}
