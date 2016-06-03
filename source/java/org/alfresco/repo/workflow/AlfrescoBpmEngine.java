
package org.alfresco.repo.workflow;

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 3.4.e
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
        QName defaultStartTaskType = getDefaultStartTaskType();
        this.factory = new WorkflowObjectFactory(qNameConverter, tenantService, messageService, dictionaryService, getEngineId(), defaultStartTaskType);
    }
    
        /**
     * Sets the Tenant Service
     * 
     * @param tenantService TenantService
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /**
     * Sets the Message Service
     * 
     * @param messageService MessageService
     */
    public void setMessageService(MessageService messageService)
    {
        this.messageService = messageService;
    }

    /**
     * Sets the Namespace Service
     * 
     * @param namespaceService NamespaceService
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
    
    protected abstract QName getDefaultStartTaskType();
}
