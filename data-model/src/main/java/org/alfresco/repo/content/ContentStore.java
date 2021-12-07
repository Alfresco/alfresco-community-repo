/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.content;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.ContentAccessor;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DirectAccessUrl;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;


/**
 * Provides low-level retrieval of content
 * {@link org.alfresco.service.cmr.repository.ContentReader readers} and
 * {@link org.alfresco.service.cmr.repository.ContentWriter writers}.
 * <p>
 * Implementations of this interface should be soley responsible for
 * providing persistence and retrieval of the content against a
 * <code>content URL</code>.
 * <p>
 * Content URLs must consist of a prefix or protocol followed by an
 * implementation-specific identifier. See
 * {@link org.alfresco.repo.content.filestore.TimeBasedFileContentUrlProvider TimeBasedFileContentUrlProvider} and
 * {@link org.alfresco.repo.content.filestore.VolumeAwareContentUrlProvider VolumeAwareContentUrlProvider} implementations of
 * {@link org.alfresco.repo.content.filestore.FileContentUrlProvider FileContentUrlProvider}  
 * For example, default content URL format
 * for file stores is <b>store://year/month/day/hour/minute/GUID.bin</b> <br>
 * <ul>
 *   <li> <b>store://</b>: prefix identifying an Alfresco content stores
 *                         regardless of the persistence mechanism. </li>
 *   <li> <b>year</b>: year </li>
 *   <li> <b>month</b>: 1-based month of the year </li>
 *   <li> <b>day</b>: 1-based day of the month </li>
 *   <li> <b>hour</b>: 0-based hour of the day </li>
 *   <li> <b>minute</b>: 0-based minute of the hour </li>
 *   <li> <b>GUID</b>: A unique identifier </li>
 * </ul>
 * <p>
 * Where the store cannot handle a particular content URL request, the
 * {@code UnsupportedContentUrlException} must be generated.  This will allow
 * various implementations to provide fallback code to other stores where
 * possible.
 * <p>
 * Where a store cannot serve a particular request because the functionality
 * is just not available, the <code>UnsupportedOperationException</code> should
 * be thrown.  Once again, there may be fallback handling provided for these
 * situations.
 * 
 * @since 1.0
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public interface ContentStore
{
    /**
     * An empty content context used to retrieve completely new content.
     * 
     * @see ContentStore#getWriter(ContentContext)
     */
    public static final ContentContext NEW_CONTENT_CONTEXT = new ContentContext(null, null);
    /**
     * The delimiter that must be found in all URLS, i.e <b>://</b>
     */
    public static final String PROTOCOL_DELIMITER = "://";
    
    /**
     * Check if the content URL format is supported by the store.
     * 
     * @param contentUrl        the content URL to check
     * @return                  Returns <tt>true</tt> if none of the other methods on the store
     *                          will throw an {@code UnsupportedContentUrlException} when given
     *                          this URL.
     * 
     * @since 2.1
     */
    public boolean isContentUrlSupported(String contentUrl);
    
    /**
     * Check if the store supports write requests.
     * 
     * @return Return true is the store supports write operations
     * 
     * @since 2.1
     */
    public boolean isWriteSupported();
    
    /**
     * Calculates the remaning <i>free</i> space in the underlying store.
     * <p>
     * <b>NOTE:</b> For efficiency, some implementations may provide a guess.
     * <p>
     * Implementations should focus on calculating a size value quickly, rather than accurately.
     * 
     * @return
     *      Returns the total, possibly approximate, free space (in bytes) available to the store
     *      or <tt>-1</tt> if no size data is available.
     * 
     * @since 3.3.3
     */
    public long getSpaceFree();
    
    /**
     * Calculates the total storage space of the underlying store.
     * <p>
     * <b>NOTE:</b> For efficiency, some implementations may provide a guess.
     * <p>
     * Implementations should focus on calculating a size value quickly, rather than accurately.
     * 
     * @return
     *      Returns the total, possibly approximate, size (in bytes) of the underlying store
     *      or <tt>-1</tt> if no size data is available.
     * 
     * @since 3.3.3
     */
    public long getSpaceTotal();
    
    /**
     * Get the location where the store is rooted.  The format of the returned value will depend on the
     * specific implementation of the store.
     * 
     * @return          Returns the store's root location or <b>.</b> if no information is available
     */
    public String getRootLocation();
    
    /**
     * Check for the existence of content in the store.
     * <p>
     * The implementation of this may be more efficient than first getting a
     * reader to {@link ContentReader#exists() check for existence}, although
     * that check should also be performed.
     * 
     * @param contentUrl
     *      the path to the content
     * @return
     *      Returns true if the content exists, otherwise false if the content doesn't
     *      exist or <b>if the URL is not applicable to this store</b>.
     * @throws org.alfresco.repo.content.UnsupportedContentUrlException
     *      if the content URL supplied is not supported by the store
     * @throws ContentIOException
     *      if an IO error occurs
     * 
     * @see ContentReader#exists()
     */
    public boolean exists(String contentUrl);
    
    /**
     * Get the accessor with which to read from the content at the given URL.
     * The reader is <b>stateful</b> and can <b>only be used once</b>.
     * 
     * @param contentUrl    the path to where the content is located
     * @return              Returns a read-only content accessor for the given URL.  There may
     *                      be no content at the given URL, but the reader must still be returned.
     * @throws org.alfresco.repo.content.UnsupportedContentUrlException
     *      if the content URL supplied is not supported by the store
     * @throws ContentIOException
     *      if an IO error occurs
     *
     * @see #exists(String)
     * @see ContentReader#exists()
     * @see org.alfresco.repo.content.EmptyContentReader
     */
    public ContentReader getReader(String contentUrl);
    
    /**
     * Get an accessor with which to write content to a location
     * within the store.  The writer is <b>stateful</b> and can
     * <b>only be used once</b>.  The location may be specified but must, in that case,
     * be a valid and unused URL.
     * <p>
     * The store will ensure that the {@link ContentAccessor#getContentUrl() new content URL} will
     * be valid for all subsequent read attempts.
     * <p>
     * By supplying a reader to existing content, the store implementation may
     * enable random access.  The store implementation
     * can enable this by copying the existing content into the new location
     * before supplying a writer onto the new content.
     * 
     * @param context
     *      the context of content.
     * @return
     *      Returns a write-only content accessor
     * @throws UnsupportedOperationException
     *      if the store is unable to provide the information
     * @throws UnsupportedContentUrlException
     *      if the content URL supplied is not supported by the store
     * @throws ContentExistsException
     *      if the content URL is already in use
     * @throws ContentIOException
     *      if an IO error occurs
     *
     * @see ContentWriter#addListener(ContentStreamListener)
     * @see ContentWriter#getContentUrl()
     */
    public ContentWriter getWriter(ContentContext context);
    
    /**
     * Deletes the content at the given URL.
     * <p>
     * A delete cannot be forced since it is much better to have the
     * file remain longer than desired rather than deleted prematurely.
     * 
     * @param contentUrl
     *      the URL of the content to delete
     * @return
     *      Returns <tt>true</tt> if the content was deleted (either by this or another operation),
     *      otherwise false.  If the content no longer exists, then <tt>true</tt> is returned.
     * @throws UnsupportedOperationException
     *      if the store is unable to perform the action
     * @throws UnsupportedContentUrlException
     *      if the content URL supplied is not supported by the store
     * @throws ContentIOException if an error occurs
     *      if an IO error occurs
     */
    public boolean delete(String contentUrl);

    /**
     * Checks if the store supports the retrieving of direct access URLs.
     *
     * @return {@code true} if direct access URLs retrieving is supported, {@code false} otherwise
     */
    default boolean isContentDirectUrlEnabled()
    {
        return false;
    }

    /**
     * Checks if the store supports the retrieving of a direct access URL for the given node.
     *
     * @param contentUrl    the {@code URL} of the content for which to request a direct access {@code URL}
     * @return {@code true} if direct access URLs retrieving is supported for the node, {@code false} otherwise
     */
    default boolean isContentDirectUrlEnabled(String contentUrl)
    {
        return false;
    }

    /**
     * Gets a presigned URL to directly access the content. It is up to the actual store
     * implementation if it can fulfil this request with an expiry time or not.
     *
     * @param contentUrl A content store {@code URL}
     * @param attachment {@code true} if an attachment URL is requested, {@code false} for an embedded {@code URL}.
     * @param fileName File name of the content
     * @return A direct access {@code URL} object for the content
     * @throws UnsupportedOperationException if the store is unable to provide the information
     */
    @Deprecated
    default DirectAccessUrl requestContentDirectUrl(String contentUrl, boolean attachment, String fileName)
    {
        return requestContentDirectUrl(contentUrl, attachment, fileName, null, null);
    }

    /**
     * Gets a presigned URL to directly access the content. It is up to the actual store
     * implementation if it can fulfil this request with an expiry time or not.
     *
     * @param contentUrl A content store {@code URL}
     * @param attachment {@code true} if an attachment URL is requested, {@code false} for an embedded {@code URL}.
     * @param fileName File name of the content
     * @return A direct access {@code URL} object for the content
     * @throws UnsupportedOperationException if the store is unable to provide the information
     */
    default DirectAccessUrl requestContentDirectUrl(String contentUrl, boolean attachment, String fileName, String mimetype)
    {
        return requestContentDirectUrl(contentUrl, attachment, fileName, mimetype, null);
    }

    /**
     * Gets a presigned URL to directly access the content. It is up to the actual store
     * implementation if it can fulfil this request with an expiry time or not.
     *
     * @param contentUrl A content store {@code URL}
     * @param attachment {@code true} if an attachment URL is requested, {@code false} for an embedded {@code URL}.
     * @param fileName File name of the content
     * @param validFor The time at which the direct access {@code URL} will expire.
     * @return A direct access {@code URL} object for the content.
     * @throws UnsupportedOperationException if the store is unable to provide the information
     */
    @Deprecated
    default DirectAccessUrl requestContentDirectUrl(String contentUrl, boolean attachment, String fileName, Long validFor)
    {
        return requestContentDirectUrl(contentUrl, attachment, fileName, null, validFor);
    }

    /**
     * Gets a presigned URL to directly access the content. It is up to the actual store
     * implementation if it can fulfil this request with an expiry time or not.
     *
     * @param contentUrl A content store {@code URL}
     * @param attachment {@code true} if an attachment URL is requested, {@code false} for an embedded {@code URL}.
     * @param fileName File name of the content
     * @param mimetype Mimetype of the content
     * @param validFor The time at which the direct access {@code URL} will expire.
     * @return A direct access {@code URL} object for the content.
     * @throws UnsupportedOperationException if the store is unable to provide the information
     */
    default DirectAccessUrl requestContentDirectUrl(String contentUrl, boolean attachment, String fileName, String mimetype, Long validFor)
    {
        throw new UnsupportedOperationException(
                "Retrieving direct access URLs is not supported by this content store.");
    }

    /**
     * Gets a key-value (String-String) collection of storage headers/properties with their respective values.
     * A particular Cloud Connector will fill in that data with Cloud Storage Provider generic data.
     * Map may be also filled in with entries consisting of pre-defined Alfresco keys of {@code ObjectStorageProps} and their values.
     * If empty Map is returned - no connector is present or connector is not supporting retrieval of the properties
     * or cannot determine the properties.
     *
     * @param contentUrl the URL of the content for which the storage properties are to be retrieved.
     * @return Returns a key-value (String-String) collection of storage headers/properties with their respective values.
     */
    @Experimental
    default Map<String, String> getStorageProperties(String contentUrl)
    {
        return Collections.emptyMap();
    }

    /**
     * Submit a request to send content to archive (offline) state.
     * If no connector is present or connector is not supporting sending to archive, then {@link UnsupportedOperationException} will be returned.
     * Specific connector will decide which storage class/tier will be set for content.
     * This method is experimental and subject to changes.
     *
     * @param contentUrl the URL of the content which is to be archived.
     * @param archiveParams a map of String-Serializable parameters defining Storage Provider specific request parameters (can be empty).
     * @return true when request successful, false when unsuccessful.
     * @throws UnsupportedOperationException when store is unable to handle request.
     */
    @Experimental
    default boolean requestSendContentToArchive(String contentUrl, Map<String, Serializable> archiveParams)
    {
        throw new UnsupportedOperationException("Request to archive content is not supported by this content store.");
    }

    /**
     * Submit a request to restore content from archive (offline) state.
     * If no connector is present or connector is not supporting restoring fom archive, then {@link UnsupportedOperationException} will be returned.
     * One of input parameters of this method is a map (String-Serializable) of Storage Provider specific input needed to perform proper restore.
     * Keys of this map should be restricted to {@code ContentRestoreParams} enumeration.
     * For AWS S3 map can indicating expiry days, Glacier restore tier.
     * For Azure Blob map can indicate rehydrate priority.
     * This method is experimental and subject to changes.
     *
     * @param contentUrl    the URL of the content which is to be archived.
     * @param restoreParams a map of String-Serializable parameters defining Storage Provider specific request parameters (can be empty).
     * @return true when request successful, false when unsuccessful.
     * @throws UnsupportedOperationException when store is unable to handle request.
     */
    @Experimental
    default boolean requestRestoreContentFromArchive(String contentUrl, Map<String, Serializable> restoreParams)
    {
        throw new UnsupportedOperationException("Request to restore content from archive is not supported by this content store.");
    }
}
