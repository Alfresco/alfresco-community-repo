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
package org.alfresco.repo.domain.usage;

import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * This provides basic services such as caching, but defers to the underlying implementation for CRUD operations.
 * 
 * <b>alf_usage_delta</b>
 * 
 * @since 3.4
 * @author janv
 */
public interface UsageDAO
{
    /**
     * Create a usage delta entry.
     * 
     * @param deltaSize
     *            the size change
     */
    public void insertDelta(NodeRef usageNodeRef, long deltaSize);

    /**
     * Get the total delta size for a node.
     * 
     * @param nodeRef
     *            the node reference
     * @param removeDeltas
     *            <tt>true</tt> to remove the deltas before returning the result.
     * @return sum of delta sizes (in bytes) - can be +ve or -ve
     */
    public long getTotalDeltaSize(NodeRef nodeRef, boolean removeDeltas);

    public Set<NodeRef> getUsageDeltaNodes();

    /**
     * Delete usage deltas for given nodeRef
     * 
     * @param nodeRef
     *            NodeRef
     * @return int
     */
    public int deleteDeltas(NodeRef nodeRef);

    /**
     * Delete usage deltas for given node entity id
     * 
     * @param nodeId
     *            long
     * @return int
     */
    public int deleteDeltas(long nodeId);

    /**
     * New style content urls - Iterate and sum all content node sizes for user (owner/creator)
     * 
     * @param storeRef
     *            the store to search in
     * @param resultsCallback
     *            the callback to use while iterating over the content sizes (one row per user)
     */
    public void getUserContentSizesForStore(
            StoreRef storeRef,
            MapHandler resultsCallback);

    /**
     * Iterate over all person nodes to get users without a calculated usage
     * 
     * @param storeRef
     *            the store to search in
     * @param resultsCallback
     *            the callback to use while iterating over the people
     */
    public void getUsersWithoutUsage(
            StoreRef storeRef,
            MapHandler resultsCallback);

    /**
     * Iterate over all person nodes to get users with a calculated usage
     * 
     * @param storeRef
     *            the store to search in
     * @param resultsCallback
     *            the callback to use while iterating over the people
     */
    public void getUsersWithUsage(
            StoreRef storeRef,
            MapHandler resultsCallback);

    /**
     * Get user with a calculated usage
     * 
     * @param storeRef
     *            the store to search in
     * @param userName
     *            the username
     * @return Returns ussage
     */
    public Long getContentSizeForStoreForUser(StoreRef storeRef, String userName);

    /**
     * A callback handler for iterating over the String results
     */
    public interface StringHandler
    {
        void handle(String string);
    }

    /**
     * A callback handler for iterating over the Map results
     */
    public interface MapHandler
    {
        void handle(Map<String, Object> result);
    }
}
