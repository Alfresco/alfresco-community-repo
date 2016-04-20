package org.alfresco.repo.jscript;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

public final class Imap extends BaseScopableProcessorExtension
{
    /** Service registry */
    private ServiceRegistry services;

    /** Default store reference */
    private StoreRef storeRef;
    
    /** Repository helper */
    private Repository repository;


    /**
     * Set the default store reference
     * 
     * @param   storeRef the default store reference
     */
    public void setStoreUrl(String storeRef)
    {
        // ensure this is not set again by a script instance!
        if (this.storeRef != null)
        {
            throw new IllegalStateException("Default store URL can only be set once.");
        }
        this.storeRef = new StoreRef(storeRef);
    }

    /**
     * Set the service registry
     * 
     * @param services  the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }
    
    /**
     * Set the repository helper
     * 
     * @param repository    the repository helper
     */
    public void setRepositoryHelper(Repository repository)
    {
        this.repository = repository;
    }

    /**
     * Searches NodeRef to the IMAP home for specified user
     * 
     * @param userName  the name of the user
     */
    public ScriptNode getImapHomeRef(String userName)
    {
        ScriptNode result = null;
        NodeRef nodeRef = services.getImapService().getUserImapHomeRef(userName);
        if (nodeRef != null)
        {
            result = new ScriptNode(nodeRef, this.services, getScope());
        }
        return result;
    }
    
}
