/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
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
