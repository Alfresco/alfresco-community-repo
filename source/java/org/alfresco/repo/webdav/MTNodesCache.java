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

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;

/**
 * In-memory cache that stores nodeRefs per tenant. 
 * It is initialized using path to node and allows to retrieve nodeRef for current tenant.
 * 
 * @author Stas Sokolovsky
 */
public class MTNodesCache
{
    private NodeService nodeService;

    private SearchService searchService;

    private NamespaceService namespaceService;

    private TenantService tenantService;

    private Map<String, NodeRef> nodesCache = new ConcurrentHashMap<String, NodeRef>();

    private String path = null;

    private NodeRef defaultNode = null;

    /**
     * Constructor
     * 
     * @param storeRef Store reference
     * @param path Path to node
     * @param nodeService NodeService
     * @param searchService SearchService
     * @param namespaceService NamespaceService
     * @param tenantService TenantService
     */
    public MTNodesCache(StoreRef storeRef, String path, NodeService nodeService, SearchService searchService, NamespaceService namespaceService, TenantService tenantService)
    {
        this.nodeService = nodeService;
        this.searchService = searchService;
        this.namespaceService = namespaceService;
        this.tenantService = tenantService;

        if (nodeService.exists(storeRef) == false)
        {
            throw new RuntimeException("No store for path: " + storeRef);
        }

        NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);

        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, path, null, namespaceService, false);

        if (nodeRefs.size() > 1)
        {
            throw new RuntimeException("Multiple possible children for : \n" + "   path: " + path + "\n" + "   results: " + nodeRefs);
        }
        else if (nodeRefs.size() == 0)
        {
            throw new RuntimeException("Node is not found for : \n" + "   root path: " + path);
        }

        this.path = path;
        defaultNode = nodeRefs.get(0);

    }

    /**
     * Returns nodeRef for current user tenant
     * 
     * @return nodeRef Node Reference
     */
    public NodeRef getNodeForCurrentTenant()
    {
        NodeRef result = null;

        if (!tenantService.isEnabled())
        {
            result = defaultNode;
        }
        else
        {
            String domain = tenantService.getCurrentUserDomain();
            if (nodesCache.containsKey(domain))
            {
                result = nodesCache.get(domain);
            }
            else
            {
                result = tenantService.getRootNode(nodeService, searchService, namespaceService, path, defaultNode);
                nodesCache.put(domain, result);
            }
        }
        return result;
    }

}
