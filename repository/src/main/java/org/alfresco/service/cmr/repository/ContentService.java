/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.service.cmr.repository;


import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.model.ContentModel;
import org.alfresco.service.Auditable;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Provides methods for accessing and transforming content.
 * <p>
 * Implementations of this service are primarily responsible for ensuring
 * that the correct store is used to access content, and that reads and
 * writes for the same node reference are routed to the same store instance.
 * <p>
 * The mechanism for selecting an appropriate store is not prescribed by
 * the interface, but typically the decision will be made on the grounds
 * of content type.
 * <p>
 * Whereas the content stores have no knowledge of nodes other than their
 * references, the <code>ContentService</code> <b>is</b> responsible for
 * ensuring that all the relevant node-content relationships are maintained.
 * 
 * @see org.alfresco.repo.content.ContentStore
 * @see org.alfresco.service.cmr.repository.ContentReader
 * @see org.alfresco.service.cmr.repository.ContentWriter
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public interface ContentService
{
    /**
     * Gets the total space of the underlying content store (not exclusively Alfresco-controlled binaries).
     * 
     * @return
     *      Returns the total, possibly approximate, size (in bytes) of of the store
     *      or <tt>-1</tt> if no size data is available.
     * 
     * @since 3.3.3
     */
    public long getStoreTotalSpace();
    
    /**
     * Gets the remaing <i>available</i> space in the underlying content store.
     * 
     * @return
     *      Returns the total, possibly approximate, remaining space (in bytes) available to store content
     *      or <tt>-1</tt> if no size data is available.
     * 
     * @since 3.3.3
     */
    public long getStoreFreeSpace();
    
    /**
     * Fetch content from the low-level stores using a content URL.  None of the
     * metadata associated with the content will be populated.  This method should
     * be used only to stream the binary data out when no other metadata is
     * required.
     * <p>
     * <tt>null</tt> is never returned, but the reader should always be checked for
     * {@link ContentReader#exists() existence}.
     * 
     * @param contentUrl        a content store URL
     * @return                  Returns a reader for the URL that needs to be checked.
     */
    @Auditable(parameters = {"contentUrl"})
    public ContentReader getRawReader(String contentUrl);
    
    /**
     * Gets a reader for the content associated with the given node property.
     * <p>
     * If a content URL is present for the given node then a reader <b>must</b>
     * be returned.  The {@link ContentReader#exists() exists} method should then
     * be used to detect 'missing' content.
     * 
     * @param nodeRef a reference to a node having a content property
     * @param propertyQName the name of the property, which must be of type <b>content</b>
     * @return Returns a reader for the content associated with the node property,
     *      or null if no content has been written for the property
     * @throws InvalidNodeRefException if the node doesn't exist
     * @throws InvalidTypeException if the node is not of type <b>content</b>
     * 
     * @see org.alfresco.repo.content.filestore.FileContentReader#getSafeContentReader(ContentReader, String, Object[])
     */
    @Auditable(parameters = {"nodeRef", "propertyQName"})
    public ContentReader getReader(NodeRef nodeRef, QName propertyQName)
            throws InvalidNodeRefException, InvalidTypeException;

    /**
     * Get a content writer for the given node property, choosing to optionally have
     * the node property updated automatically when the content stream closes.
     * <p>
     * If the update flag is off, then the state of the node property will remain unchanged
     * regardless of the state of the written binary data.  If the flag is on, then the node
     * property will be updated on the same thread as the code that closed the write
     * channel.
     * <p>
     * If no node is supplied, then the writer will provide a stream into the backing content
     * store, but will not be associated with any new or previous content.
     * <p/>
     * <b>NOTE: </b>The content URL provided will be registered for automatic cleanup in the event
     * that the transaction, in which this method was called, rolls back.  If the transaction
     * is successful, the writer may still be open and available for use but the underlying binary
     * will not be cleaned up subsequently.  The recommended pattern is to group calls to retrieve
     * the writer in the same transaction as the calls to subsequently update and close the
     * write stream - including setting of the related content properties.
     * 
     * @param nodeRef a reference to a node having a content property, or <tt>null</tt>
     *      to just get a valid writer into a backing content store.
     * @param propertyQName the name of the property, which must be of type <b>content</b>
     * @param update true if the property must be updated atomically when the content write
     *      stream is closed (attaches a listener to the stream); false if the client code
     *      will perform the updates itself.
     * @return Returns a writer for the content associated with the node property
     * @throws InvalidNodeRefException if the node doesn't exist
     * @throws InvalidTypeException if the node property is not of type <b>content</b>
     */
    @Auditable(parameters = {"nodeRef", "propertyQName", "update"})
    public ContentWriter getWriter(NodeRef nodeRef, QName propertyQName, boolean update)
                throws InvalidNodeRefException, InvalidTypeException;

    /**
     * Gets a writer to a temporary location.  The longevity of the stored
     * temporary content is determined by the system.
     * 
     * @return Returns a writer onto a temporary location
     */
    @Auditable
    public ContentWriter getTempWriter();

    /**
     * Checks if the system and at least one store supports the retrieving of direct access URLs.
     *
     * @return {@code true} if direct access URLs retrieving is supported, {@code false} otherwise
     */
    boolean isContentDirectUrlEnabled();

    /**
     * Checks if the system and store supports the retrieving of a direct access {@code URL} for the given node.
     *
     * @return {@code true} if direct access URLs retrieving is supported for the node, {@code false} otherwise
     */
    @Deprecated
    default boolean isContentDirectUrlEnabled(NodeRef nodeRef)
    {
        return isContentDirectUrlEnabled(nodeRef, ContentModel.PROP_CONTENT);
    }

    /**
     * Checks if the system and store supports the retrieving of a direct access {@code URL} for the given node.
     *
     * @param nodeRef a reference to a node having a content property
     * @param propertyQName the name of the property, which must be of type <b>content</b>
     * @return {@code true} if direct access URLs retrieving is supported for the node, {@code false} otherwise
     */
    boolean isContentDirectUrlEnabled(NodeRef nodeRef, QName propertyQName);

    /**
     * Gets a presigned URL to directly access the content. It is up to the actual store
     * implementation if it can fulfil this request with an expiry time or not.
     *
     * @param nodeRef Node ref for which to obtain the direct access {@code URL}.
     * @param attachment {@code true} if an attachment URL is requested, {@code false} for an embedded {@code URL}.
     * @return A direct access {@code URL} object for the content.
     * @throws UnsupportedOperationException if the store is unable to provide the information.
     */
    @Deprecated
    default DirectAccessUrl requestContentDirectUrl(NodeRef nodeRef, boolean attachment)
    {
        return requestContentDirectUrl(nodeRef, attachment, null);
    }

    /**
     * Gets a presigned URL to directly access the content. It is up to the actual store
     * implementation if it can fulfil this request with an expiry time or not.
     *
     * @param nodeRef Node ref for which to obtain the direct access {@code URL}.
     * @param propertyQName the name of the property, which must be of type <b>content</b>
     * @param attachment {@code true} if an attachment URL is requested, {@code false} for an embedded {@code URL}.
     * @return A direct access {@code URL} object for the content.
     * @throws UnsupportedOperationException if the store is unable to provide the information.
     */
    default DirectAccessUrl requestContentDirectUrl(NodeRef nodeRef, QName propertyQName, boolean attachment)
    {
        return requestContentDirectUrl(nodeRef, propertyQName, attachment, null);
    }

    /**
     * Gets a presigned URL to directly access the content. It is up to the actual store
     * implementation if it can fulfil this request with an expiry time or not.
     *
     * @param nodeRef Node ref for which to obtain the direct access {@code URL}.
     * @param attachment {@code true} if an attachment URL is requested, {@code false} for an embedded {@code URL}.
     * @param validFor The time at which the direct access {@code URL} will expire.
     * @return A direct access {@code URL} object for the content.
     * @throws UnsupportedOperationException if the store is unable to provide the information.
     */
    @Auditable(parameters = {"nodeRef", "validFor"})
    @Deprecated
    default public DirectAccessUrl requestContentDirectUrl(NodeRef nodeRef, boolean attachment, Long validFor)
    {
        return requestContentDirectUrl(nodeRef, ContentModel.PROP_CONTENT, attachment, validFor);
    }

    /**
     * Gets a presigned URL to directly access the content. It is up to the actual store
     * implementation if it can fulfil this request with an expiry time or not.
     *
     * @param nodeRef Node ref for which to obtain the direct access {@code URL}.
     * @param propertyQName the name of the property, which must be of type <b>content</b>
     * @param attachment {@code true} if an attachment URL is requested, {@code false} for an embedded {@code URL}.
     * @param validFor The time at which the direct access {@code URL} will expire.
     * @return A direct access {@code URL} object for the content.
     * @throws UnsupportedOperationException if the store is unable to provide the information.
     */
    @Auditable(parameters = {"nodeRef", "propertyQName", "validFor"})
    DirectAccessUrl requestContentDirectUrl(NodeRef nodeRef, QName propertyQName, boolean attachment, Long validFor);

    /**
     * Gets a key-value (String-String) collection of storage headers/properties with their respective values for a specific node reference.
     * A particular Cloud Connector will fill in that data with Cloud Storage Provider generic data.
     * Map may be also filled in with entries consisting of pre-defined Alfresco keys of {@code ObjectStorageProps} and their values.
     * If empty Map is returned - no connector is present or connector is not supporting retrieval of the properties
     * or cannot determine the properties.
     *
     * @param nodeRef a reference to a node having a content property
     * @param propertyQName the name of the property, which must be of type <b>content</b>
     * @return Returns a key-value (String-String) collection of storage headers/properties with their respective values for a given {@link NodeRef}.
     */
    @Auditable(parameters = {"nodeRef", "propertyQName"})
    @Experimental
    default Map<String, String> getStorageProperties(NodeRef nodeRef, QName propertyQName)
    {
        return Collections.emptyMap();
    }

    /**
     * Submit a request to send content to archive (offline) state.
     * If no connector is present or connector is not supporting sending to archive, then {@link UnsupportedOperationException} will be returned.
     * Specific connector will decide which storage class/tier will be set for content.
     * This method is experimental and subject to changes.
     *
     * @param nodeRef a reference to a node having a content property
     * @param propertyQName the name of the property, which must be of type <b>content</b>
     * @param archiveParams a map of String-Serializable parameters defining Storage Provider specific request parameters (can be empty).
     * @return true when request successful, false when unsuccessful.
     * @throws UnsupportedOperationException when method not implemented
     */
    @Auditable(parameters = {"nodeRef", "propertyQName", "archiveParams"})
    @Experimental
    default boolean requestSendContentToArchive(NodeRef nodeRef, QName propertyQName,
                                                Map<String, Serializable> archiveParams)
    {
        throw new UnsupportedOperationException("Request to archive content is not supported by content service.");
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
     * @param nodeRef a reference to a node having a content property
     * @param propertyQName the name of the property, which must be of type <b>content</b>
     * @param restoreParams a map of String-Serializable parameters defining Storage Provider specific request parameters (can be empty).
     * @return true when request successful, false when unsuccessful.
     * @throws UnsupportedOperationException when method not implemented
     */
    @Auditable(parameters = {"nodeRef", "propertyQName", "restoreParams"})
    @Experimental
    default boolean requestRestoreContentFromArchive(NodeRef nodeRef, QName propertyQName, Map<String, Serializable> restoreParams)
    {
        throw new UnsupportedOperationException("Request to restore content from archive is not supported by content service.");
    }
}
