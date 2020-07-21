/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.repo.tenant;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Empty Tenant Service implementation (for Single-Tenant / Single-Instance)
 */

public class SingleTServiceImpl implements TenantService
{
    public NodeRef getName(NodeRef nodeRef)
    {
        return nodeRef;
    }
        
    public NodeRef getName(NodeRef inNodeRef, NodeRef nodeRef)
    {
        return nodeRef;
    }
    
    public StoreRef getName(StoreRef storeRef)
    {
        return storeRef;
    }
    
    public ChildAssociationRef getName(ChildAssociationRef childAssocRef)
    {
    	return childAssocRef;
    }
    
    public AssociationRef getName(AssociationRef assocRef)
    {
        return assocRef;
    }

    public StoreRef getName(String username, StoreRef storeRef)
    {
        return storeRef;
    }
    
    public QName getName(QName name)
    {
        return name;
    }
    
    public QName getName(NodeRef inNodeRef, QName name)
    {
        return name;
    }
    
    public String getName(String name)
    {
        return name;
    }
    
    public QName getBaseName(QName name, boolean forceForNonTenant)
    {
        return name;
    }
     
    public NodeRef getBaseName(NodeRef nodeRef)
    {
        return nodeRef;
    }
    
    public NodeRef getBaseName(NodeRef nodeRef, boolean forceForNonTenant)
    {
        return nodeRef;
    }
    
    public StoreRef getBaseName(StoreRef storeRef)
    {
        return storeRef;
    }
    
    public ChildAssociationRef getBaseName(ChildAssociationRef childAssocRef)
    {
        return childAssocRef;
    }
    
    public ChildAssociationRef getBaseName(ChildAssociationRef childAssocRef, boolean forceForNonTenant)
    {
        return childAssocRef;
    }
    
    public AssociationRef getBaseName(AssociationRef assocRef)
    {
        return assocRef;
    }
    
    public String getBaseName(String name)
    {
        return name;
    }

    
    public String getBaseName(String name, boolean forceForNonTenant)
    {
        return name;
    }

    public String getBaseNameUser(String name)
    {
        return name;
    }
    
    public void checkDomainUser(String username)
    {
        // NOOP
    }
    
    public void checkDomain(String name)
    {
        // NOOP
    }
    
    public NodeRef getRootNode(NodeService nodeService, SearchService searchService, NamespaceService namespaceService, String rootPath, NodeRef rootNodeRef)
    {
        return rootNodeRef;
    }

    public boolean isTenantUser()
    {
        return false;
    }
    
    public boolean isTenantUser(String username)
    {
        return false;
    }
    
    public boolean isTenantName(String name)
    {
        return false;
    }
    
    public String getUserDomain(String username)
    {
        return DEFAULT_DOMAIN;
    }
    
    public String getCurrentUserDomain()
    {
        return DEFAULT_DOMAIN;
    }
    
    public String getDomain(String name)
    {
        return DEFAULT_DOMAIN;
    }
    
    public String getDomain(String name, boolean checkCurrentDomain)
    {
        return DEFAULT_DOMAIN;
    }
    
    public String getDomainUser(String baseUsername, String tenantDomain)
    {
        return baseUsername;
    }
    
    public Tenant getTenant(String tenantDomain)
    {
        return null;
    }

    public boolean isEnabled()
    {
        return false;
    }

    /**
     * In a single tenant system, always return the DEFAULT_DOMAIN.
     * 
     * @see TenantService
     */
	public String getPrimaryDomain(String user) {
		return DEFAULT_DOMAIN;
	}
}