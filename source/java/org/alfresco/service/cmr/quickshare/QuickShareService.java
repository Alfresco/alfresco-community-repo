/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.service.cmr.quickshare;

import java.util.Map;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * The QuickShare service.
 * 
 * Responsible for creating, updating and retrieving Quick share metadata,
 *
 * @author Alex Miller, janv
 * @since Cloud/4.2
 */
public interface QuickShareService
{
    /**
     * Share content identified by nodeRef.
     * 
     * @param nodeRef The NodeRef of the content to share
     * @return QuickDTO with details of the share
     */
    public QuickShareDTO shareContent(NodeRef nodeRef) throws QuickShareDisabledException, InvalidNodeRefException;

    /**
     * Get QuickShare related metadata for the given node.
     *  
     * @param nodeRef
     * @return
     */
    public Map<String, Object> getMetaData(NodeRef nodeRef) throws QuickShareDisabledException, InvalidNodeRefException;

    /**
     * Get QuickShare related metadata for the given shareId.
     *  
     * @param shareId
     * @return
     */
    public Map<String, Object> getMetaData(String shareId) throws QuickShareDisabledException, InvalidSharedIdException;

    /**
     * Get the tenant domain and node reference for the the given share id.
     * 
     * @param sharedId
     * @return
     */
    public Pair<String, NodeRef> getTenantNodeRefFromSharedId(String sharedId) throws QuickShareDisabledException, InvalidSharedIdException;

    /**
     * Unshare the content identified by sharedId
     *  
     * @param sharedId The shared id of the content to unshare.
     */
    public void unshareContent(String sharedId) throws QuickShareDisabledException, InvalidSharedIdException;

    /**
     * Determine if the current user has permission to read the shared content.
     */
    public boolean canRead(String sharedId);
}
