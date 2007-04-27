/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.service;

import java.util.Collection;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.CrossRepositoryCopyService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;


/**
 * Implementation of a Service Registry based on the definition of
 * Services contained within a Spring Bean Factory.
 * 
 * @author David Caruana
 */
public class ServiceDescriptorRegistry
    implements BeanFactoryAware, ServiceRegistry
{
    // Bean Factory within which the registry lives
    private BeanFactory beanFactory = null;

    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = beanFactory;
    }
        
    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getServices()
     */
    public Collection<QName> getServices()
    {
        // TODO: Implement
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#isServiceProvided(org.alfresco.repo.ref.QName)
     */
    public boolean isServiceProvided(QName service)
    {
        // TODO: Implement
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getService(org.alfresco.repo.ref.QName)
     */
    public Object getService(QName service)
    {
        return beanFactory.getBean(service.getLocalName()); 
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getDescriptorService()
     */
    public DescriptorService getDescriptorService()
    {
        return (DescriptorService)getService(DESCRIPTOR_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getNodeService()
     */
    public NodeService getNodeService()
    {
        return (NodeService)getService(NODE_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getNodeService()
     */
    public AuthenticationService getAuthenticationService()
    {
        return (AuthenticationService)getService(AUTHENTICATION_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getContentService()
     */
    public ContentService getContentService()
    {
        return (ContentService)getService(CONTENT_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getMimetypeService()
     */
    public MimetypeService getMimetypeService()
    {
        return (MimetypeService)getService(MIMETYPE_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getVersionService()
     */
    public VersionService getVersionService()
    {
        return (VersionService)getService(VERSION_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getLockService()
     */
    public LockService getLockService()
    {
        return (LockService)getService(LOCK_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getDictionaryService()
     */
    public DictionaryService getDictionaryService()
    {
        return (DictionaryService)getService(DICTIONARY_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getSearchService()
     */
    public SearchService getSearchService()
    {
        return (SearchService)getService(SEARCH_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getTransactionService()
     */
    public TransactionService getTransactionService()
    {
        return (TransactionService)getService(TRANSACTION_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getCopyService()
     */
    public CopyService getCopyService()
    {
        return (CopyService)getService(COPY_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getCheckOutCheckInService()
     */
    public CheckOutCheckInService getCheckOutCheckInService()
    {
        return (CheckOutCheckInService)getService(COCI_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getCategoryService()
     */
    public CategoryService getCategoryService()
    {
        return (CategoryService)getService(CATEGORY_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getNamespaceService()
     */
    public NamespaceService getNamespaceService()
    {
        return (NamespaceService)getService(NAMESPACE_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getImporterService()
     */
    public ImporterService getImporterService()
    {
        return (ImporterService)getService(IMPORTER_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getExporterService()
     */
    public ExporterService getExporterService()
    {
        return (ExporterService)getService(EXPORTER_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getRuleService()
     */
    public RuleService getRuleService()
    {
        return (RuleService)getService(RULE_SERVICE);
    }
    
    /*
     *  (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getActionService()
     */
    public ActionService getActionService()
    {
    	return (ActionService)getService(ACTION_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getPermissionService()
     */
    public PermissionService getPermissionService()
    {
        return (PermissionService)getService(PERMISSIONS_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getAuthorityService()
     */
    public AuthorityService getAuthorityService()
    {
        return (AuthorityService)getService(AUTHORITY_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getTemplateService()
     */
    public TemplateService getTemplateService()
    {
        return (TemplateService)getService(TEMPLATE_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getTemplateService()
     */
    public FileFolderService getFileFolderService()
    {
        return (FileFolderService)getService(FILE_FOLDER_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getScriptService()
     */
    public ScriptService getScriptService()
    {
        return (ScriptService)getService(SCRIPT_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getWorkflowService()
     */
    public WorkflowService getWorkflowService()
    {
        return (WorkflowService)getService(WORKFLOW_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getWorkflowService()
     */
    public AuditService getAuditService()
    {
        return (AuditService)getService(AUDIT_SERVICE);
    }

    /**
     * Get the AVMService.
     * @return The AVMService or null if there is none.
     */
    public AVMService getAVMService()
    {
        return (AVMService)getService(AVM_SERVICE);
    }
    
    /**
     * Get the AVM Sync Service.
     * @return The AVM Sync Service.
     */
    public AVMSyncService getAVMSyncService()
    {
        return (AVMSyncService)getService(AVM_SYNC_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getOwnableService()
     */
    public OwnableService getOwnableService()
    {
        return (OwnableService)getService(OWNABLE_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getPersonService()
     */
    public PersonService getPersonService()
    {
        return (PersonService)getService(PERSON_SERVICE);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getCrossRepositoryCopyService()
     */
    public CrossRepositoryCopyService getCrossRepositoryCopyService()
    {
        return (CrossRepositoryCopyService)getService(CROSS_REPO_COPY_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getAttributeService()
     */
    public AttributeService getAttributeService()
    {
        return (AttributeService)getService(ATTRIBUTE_SERVICE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.ServiceRegistry#getAVMLockingService()
     */
    public AVMLockingService getAVMLockingService()
    {
        return (AVMLockingService)getService(AVM_LOCKING_SERVICE);
    }
}
