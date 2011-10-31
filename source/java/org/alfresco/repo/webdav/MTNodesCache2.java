/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.webdav;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.PropertyCheck;

/**
 * In-memory cache that stores nodeRefs per tenant. 
 * It is initialized using path to node and allows to retrieve nodeRef for current tenant.
 * 
 * @author Stas Sokolovsky
 * @author Mark Rogers
 */
public class MTNodesCache2
{
    private boolean enabled = false;
    
    private NodeService nodeService;

    private SearchService searchService;

    private NamespaceService namespaceService;

    private TenantService tenantService;

    private Map<String, NodeRef> nodesCache = new ConcurrentHashMap<String, NodeRef>();

    private NodeRef defaultNode = null;
    
    private String storeName;
    private String rootPath;
    
    public void init()
    {
        PropertyCheck.mandatory(this, "nodeService", getNodeService());
        PropertyCheck.mandatory(this, "searchService", getSearchService());
        PropertyCheck.mandatory(this, "namespaceService", getNamespaceService());
        PropertyCheck.mandatory(this, "tenantService", getTenantService());
            
        if(!enabled)
        {
            return;
        }
        
        PropertyCheck.mandatory(this, "storeName", storeName);
        PropertyCheck.mandatory(this, "rootPath", rootPath);
        
        AuthenticationUtil.setRunAsUserSystem();
        try
        {
            StoreRef storeRef = new StoreRef(storeName);
        
            if (nodeService.exists(storeRef) == false)
            {
                throw new RuntimeException("No store for path: " + storeName);
            }

            NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);

            List<NodeRef> nodeRefs = getSearchService().selectNodes(storeRootNodeRef, rootPath, null, getNamespaceService(), false);

            if (nodeRefs.size() > 1)
            {
                throw new RuntimeException("Multiple possible children for : \n" + "   path: " + rootPath + "\n" + "   results: " + nodeRefs);
            }
            else if (nodeRefs.size() == 0)
            {
                throw new RuntimeException("Node is not found for : \n" + "   root path: " + rootPath);
            }

            defaultNode = nodeRefs.get(0);
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    public NodeService getNodeService()
    {
        return nodeService;
    }

    /**
     * Returns nodeRef for current user tenant
     * 
     * @return nodeRef Node Reference
     */
    public NodeRef getNodeForCurrentTenant()
    {
        NodeRef result = null;

        if (!getTenantService().isEnabled())
        {
            result = defaultNode;
        }
        else
        {
            String domain = getTenantService().getCurrentUserDomain();
            if (nodesCache.containsKey(domain))
            {
                result = nodesCache.get(domain);
            }
            else
            {
                result = getTenantService().getRootNode(nodeService, getSearchService(), getNamespaceService(), rootPath, defaultNode);
                nodesCache.put(domain, result);
            }
        }
        return result;
    }
    
    /**
     * @return              Returns the name of the store
     * @throws ServletException if the store name was not set
     */
    public String getStoreName()
    {
        return storeName;
    }
    public void setStoreName(String storeName)
    {
        this.storeName = storeName;
    }
    
    /**
     * @return              Returns the WebDAV root path within the store
     * @throws ServletException if the root path was not set
     */
    public String getRootPath()
    {
        return rootPath;
    }
    public void setRootPath(String rootPath)
    {
        this.rootPath = rootPath;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public SearchService getSearchService()
    {
        return searchService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public NamespaceService getNamespaceService()
    {
        return namespaceService;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public TenantService getTenantService()
    {
        return tenantService;
    }
    public boolean getEnabled()
    {
        return enabled;
    }
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    


}
