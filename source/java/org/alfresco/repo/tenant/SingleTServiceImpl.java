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
package org.alfresco.repo.tenant;

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
    
    public StoreRef getName(String username, StoreRef storeRef)
    {
        return storeRef;
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
    
    public StoreRef getBaseName(StoreRef storeRef)
    {
        return storeRef;
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
    
    public NodeRef getCompanyHomeNode(NodeService nodeService, String username, StoreRef storeRef) 
    {
        return null;
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
    
    public String getCurrentUserDomain()
    {
        return "";
    }
    
    public String getDomain(String name)
    {
        return "";
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
}