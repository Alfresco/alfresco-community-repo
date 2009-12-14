/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.domain.contentdata;

import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentData;
import org.springframework.extensions.surf.util.Pair;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * DAO services for <b>alf_content_data</b> table
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public interface ContentDataDAO
{
    /**
     * Create a new ContentData instance.
     * 
     * @param contentData   the ContentData details
     * @return              the ContentData pair (id, ContentData) (never null)
     */
    Pair<Long, ContentData> createContentData(ContentData contentData);

    /**
     * @param id            the unique ID of the entity
     * @return              the ContentData pair (id, ContentData) or <tt>null</tt> if it doesn't exist
     * @throws              AlfrescoRuntimeException if the ID provided is invalid
     */
    Pair<Long, ContentData> getContentData(Long id);
    
    /**
     * Delete an instance of content.
     * @param id            the unique ID of the entity
     * @throws              ConcurrencyFailureException if the ID does not exist
     */
    void deleteContentData(Long id);
    
    /**
     * Deletes all <b>alf_content_data</b> rows that are referenced by the given node
     * 
     * @param nodeId        the node ID
     * @param qnameIds      the content properties to target
     */
    void deleteContentDataForNode(Long nodeId, Set<Long> qnameIds);
    
    /**
     * Interface for callbacks during content URL enumeration
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    public static interface ContentUrlHandler
    {
        void handle(String contentUrl);
    }
    
    /**
     * Enumerate all available content URLs
     * 
     * @param contentUrlHandler
     */
    void getAllContentUrls(ContentUrlHandler contentUrlHandler);
}
