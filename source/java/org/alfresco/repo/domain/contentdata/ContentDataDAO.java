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
package org.alfresco.repo.domain.contentdata;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.util.Pair;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;

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
     * Update a content data instance
     * 
     * @param id            the unique ID of the entity
     * @param contentData   the new data
     */
    void updateContentData(Long id, ContentData contentData);

    /**
     * Creates an immediately-orphaned content URL, if possible
     * 
     * @param contentUrl    the URL to create if it doesn't exist
     * @parma orphanTime    the recorded orphan time or <tt>null</tt> to apply the current time
     * @return              Returns the ID-URL pair
     * @throws DataIntegrityViolationException      if the URL already exists
     */
    Pair<Long, String> createContentUrlOrphaned(String contentUrl, Date orphanTime);
    
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
        void handle(Long id, String contentUrl, Long orphanTime);
    }
    
    /**
     * Enumerate all available content URLs that were orphaned on or before the given time
     * 
     * @param contentUrlHandler         the callback object to process the rows
     * @param maxOrphanTimeExclusive    the maximum orphan time (exclusive)
     * @param maxResults                the maximum number of results (1 or greater)
     * @return                          Returns a list of orphaned content URLs ordered by ID
     */
    void getContentUrlsOrphaned(
            ContentUrlHandler contentUrlHandler,
            Long maxOrphanTimeExclusive,
            int maxResults);
    
    /**
     * Delete a batch of content URL entities.
     */
    int deleteContentUrls(List<Long> ids);
}
