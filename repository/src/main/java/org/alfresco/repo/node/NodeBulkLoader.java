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
package org.alfresco.repo.node;

import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A component that pre-fetches cached data for the given nodes. Client code can use this component when a list of <code>NodeRef</code> instances will be processed in a data-intensive manner.
 * 
 * @author Andy Hind
 * @author Derek Hulley
 */
public interface NodeBulkLoader
{
    /**
     * Gets the current set of cached ancestors of the given list of nodes.
     * 
     * @param nodeIds
     *            a list of node IDs to visit
     * @return the current set of cached ancestors of the given list of nodes, including the nodes themselves.
     */
    public Set<Long> getCachedAncestors(List<Long> nodeIds);

    /**
     * Transaction-scope setting to make the Node loader to guarantee the validity of all caches: some cache data will be reloaded; some cache data will be considered safe.
     */
    public void setCheckNodeConsistency();

    /**
     * Pre-cache data relevant to the given nodes. There is no need to split the collection up before calling this method; it is up to the implementations to ensure that batching is done where necessary.
     * 
     * @param nodeRefs
     *            the nodes that will be cached.
     */
    public void cacheNodes(List<NodeRef> nodeRefs);

    /**
     * Pre-cache data relevant to the given nodes. There is no need to split the collection up before calling this method; it is up to the implementations to ensure that batching is done where necessary.
     * 
     * @param nodeIds
     *            the nodes that will be cached.
     */
    public void cacheNodesById(List<Long> nodeIds);

    /**
     * <b>FOR TESTING ONLY: </b>Clears out node cache data
     */
    public void clear();
}
