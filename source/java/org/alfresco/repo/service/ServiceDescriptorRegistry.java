/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.service.ServiceDescriptor;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
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
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;


/**
 * Implementation of a Service Registry based on the definition of
 * Services contained within a Spring Bean Factory.
 * 
 * @author David Caruana
 */
public class ServiceDescriptorRegistry
    implements BeanFactoryAware, BeanFactoryPostProcessor, ServiceRegistry
{
    // Bean Factory within which the registry lives
    private BeanFactory beanFactory = null;

    // Service Descriptor map
    private Map<QName, BeanServiceDescriptor> descriptors = new HashMap<QName, BeanServiceDescriptor>();

    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
     */
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
    {
        Map beans = beanFactory.getBeansOfType(ServiceDescriptorMetaData.class);
        Iterator iter = beans.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry entry = (Map.Entry)iter.next();
            ServiceDescriptorMetaData metaData = (ServiceDescriptorMetaData)entry.getValue();
            QName serviceName = QName.createQName(metaData.getNamespace(), (String)entry.getKey());
            StoreRedirector redirector = (entry.getValue() instanceof StoreRedirector) ? (StoreRedirector)entry.getValue() : null;
            BeanServiceDescriptor serviceDescriptor = new BeanServiceDescriptor(serviceName, metaData, redirector);
            descriptors.put(serviceDescriptor.getQualifiedName(), serviceDescriptor);
        }
    }

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
        return Collections.unmodifiableSet(descriptors.keySet());
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#isServiceProvided(org.alfresco.repo.ref.QName)
     */
    public boolean isServiceProvided(QName service)
    {
        return descriptors.containsKey(service);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.service.ServiceRegistry#getServiceDescriptor(org.alfresco.repo.ref.QName)
     */
    public ServiceDescriptor getServiceDescriptor(QName service)
    {
        return descriptors.get(service);
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
}
