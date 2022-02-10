/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.util;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;

/**
 * Provides operations to manipulate the records management root cache
 *
 * @author Tiago Salvado
 *
 * @see RecordsManagementModel
 */
public class RMContainerCacheManager implements RecordsManagementModel
{
    /** node service */
    private NodeService nodeService;

    /** root records management cache */
    private SimpleCache<Pair<StoreRef, String>, Set<NodeRef>> cache;

    /**
     * @param nodeService
     *            node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param cache
     */
    public void setCache(SimpleCache<Pair<StoreRef, String>, Set<NodeRef>> cache)
    {
        this.cache = cache;
    }

    /**
     * Verifies if there is cached nodes for supplied storeRef
     *
     * @param storeRef
     * @return true if there are cached nodes, false otherwise
     */
    public boolean isCached(StoreRef storeRef)
    {
        Pair<StoreRef, String> key = getKey(storeRef);
        Set<NodeRef> values = cache.get(key);
        boolean isCached = (values != null && !values.isEmpty());
        if (!isCached)
        {
            cache.remove(key);
        }
        return isCached;
    }

    /**
     * Obtains the cached nodes for supplied storeRef
     *
     * @param storeRef
     * @return a set containing the cached nodes
     */
    public Set<NodeRef> get(StoreRef storeRef)
    {
        return cache.get(getKey(storeRef));
    }

    /**
     * Caches the supplied node
     *
     * @param nodeRef
     */
    public void add(NodeRef nodeRef)
    {
        if (nodeRef != null && nodeService.hasAspect(nodeRef, ASPECT_RECORDS_MANAGEMENT_ROOT))
        {
            Set<NodeRef> entries;
            Pair<StoreRef, String> key = getKey(nodeRef.getStoreRef());

            if (cache.contains(key))
            {
                entries = this.cache.get(key);
            }
            else
            {
                entries = new HashSet<>();
            }

            if (!entries.contains(nodeRef))
            {
                entries.add(nodeRef);
            }

            if (entries.size() > 0)
            {
                cache.put(key, entries);
            }
        }
    }

    /**
     * Removes the supplied entry from the cache
     *
     * @param nodeRef
     */
    public void remove(NodeRef nodeRef)
    {
        if (nodeRef != null)
        {
            if (nodeService.hasAspect(nodeRef, ASPECT_RECORDS_MANAGEMENT_ROOT))
            {
                Pair<StoreRef, String> key = getKey(nodeRef.getStoreRef());
                if (cache.contains(key))
                {
                    cache.get(key).remove(nodeRef);
                    if (cache.get(key).size() == 0)
                    {
                        cache.remove(key);
                    }
                }
            }
        }
    }

    /**
     * Resets the cache entries
     */
    public void reset()
    {
        this.cache.clear();
    }

    /**
     * Builds the cache key using the supplied storeRef
     *
     * @param storeRef
     * @return a pair corresponding to the cache key
     */
    private Pair<StoreRef, String> getKey(StoreRef storeRef)
    {
        return new Pair<StoreRef, String>(storeRef, ASPECT_RECORDS_MANAGEMENT_ROOT.toString());
    }
}
