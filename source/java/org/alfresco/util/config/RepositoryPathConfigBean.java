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
package org.alfresco.util.config;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;

/**
 * Composite property bean to identify a repository path.
 * 
 * @author Derek Hulley
 * @since 3.2 
 */
public class RepositoryPathConfigBean
{
    /** The Alfresco store reference */
    private StoreRef store;

    /** The path within the store to the root node. */
    private String rootPath;

    /**
     * Gets the Alfresco store reference
     * 
     * @return the Alfresco store reference
     */
    public StoreRef getStoreRef()
    {
        return this.store;
    }

    /**
     * @return          Returns the string representation of the store reference
     */
    public String getStore()
    {
        return store == null ? null : store.toString();
    }

    /**
     * Sets the Alfresco store name.
     * 
     * @param store
     *            the Alfresco store name
     */
    public void setStore(String storeRef)
    {
        PropertyCheck.mandatory(this, "store", storeRef);
        this.store = new StoreRef(storeRef);
    }

    /**
     * Gets the path within the store
     */
    public String getRootPath()
    {
        return this.rootPath;
    }

    /**
     * Sets the path within the store.  This must start with <b>/</b> at least.
     * 
     * @param path
     *            the path within the store
     */
    public void setRootPath(String path)
    {
        PropertyCheck.mandatory(this, "path", path);
        ParameterCheck.mandatoryString("path", path);
        if (!path.startsWith("/"))
        {
            throw new IllegalArgumentException("Propety 'path' must start with '/' - it is a path relative to the store root.");
        }
        this.rootPath = path;
    }

    /**
     * Helper method to resolve the path represented by this configuration bean.
     * <p>
     * Authentication and transactions are the client's responsibility.
     * 
     * @return                      Returns the node reference (first one found) or <tt>null</tt>
     */
    public NodeRef resolveNodePath(NamespaceService namespaceService, NodeService nodeService, SearchService searchService)
    {
        NodeRef rootNodeRef = nodeService.getRootNode(store);
        List<NodeRef> nodeRefs = searchService.selectNodes(rootNodeRef, rootPath, null, namespaceService, true);
        if (nodeRefs.size() == 0)
        {
            return null;
        }
        else
        {
            return nodeRefs.get(0);
        }
    }
}
