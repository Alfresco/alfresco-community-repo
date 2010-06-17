/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.usage;

import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * This provides basic services such as caching, but defers to the underlying implementation
 * for CRUD operations.
 * 
 *     <b>alf_usage_delta</b>
 * 
 * @since 3.4
 * @author janv
 */
public interface UsageDAO
{
    /**
     * Create a usage delta entry.
     * 
     * @param deltaSize     the size change
     */
    public void insertDelta(NodeRef usageNodeRef, long deltaSize);
    
    /**
     * Get the total delta size for a node.
     * 
     * @param nodeRef       the node reference
     * @param removeDeltas  <tt>true</tt> to remove the deltas before returning the result.
     * @return              sum of delta sizes (in bytes) - can be +ve or -ve
     */
    public long getTotalDeltaSize(NodeRef nodeRef, boolean removeDeltas);
    
    public Set<NodeRef> getUsageDeltaNodes();
    
    /**
     * Delete usage deltas for given nodeRef
     * 
     * @param nodeRef
     * @return
     */
    public int deleteDeltas(NodeRef nodeRef);
    
    /**
     * Delete usage deltas for given node entity id
     * 
     * @param nodeId
     * @return
     */
    public int deleteDeltas(long nodeId);
    
    /**
     * New style content urls - Iterate and sum all content node sizes for user (owner/creator)
     * 
     * @param storeRef                          the store to search in
     * @param handler                           the callback to use while iterating over the content sizes (one row per user)
     * @return Returns the values for the given owner, creator and content size (summed)
     */
    public void getUserContentSizesForStore(
            StoreRef storeRef, 
            MapHandler resultsCallback);
    
    /**
     * Iterate over all person nodes to get users without a calculated usage
     * 
     * @param storeRef                          the store to search in
     * @param handler                           the callback to use while iterating over the people
     * @return Returns the values for username and person node uuid (excluding System)
     */
    public void getUsersWithoutUsage(
            StoreRef storeRef,
            MapHandler resultsCallback);
    
    /**
     * Iterate over all person nodes to get users with a calculated usage
     * 
     * @param storeRef                          the store to search in
     * @param handler                           the callback to use while iterating over the people
     * @return Returns the values for the username and person node uuid (excluding System)
     */
    public void getUsersWithUsage(
            StoreRef storeRef,
            MapHandler resultsCallback);
    
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
