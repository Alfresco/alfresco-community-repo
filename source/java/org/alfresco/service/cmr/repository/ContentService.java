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
package org.alfresco.service.cmr.repository;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.namespace.QName;

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
@PublicService
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
     * Transforms the content from the reader and writes the content
     * back out to the writer.
     * <p>
     * The mimetypes used for the transformation must be set both on
     * the {@link ContentAccessor#getMimetype() reader} and on the
     * {@link ContentAccessor#getMimetype() writer}.
     * 
     * @param reader the source content location and mimetype
     * @param writer the target content location and mimetype
     * @throws NoTransformerException if no transformer exists for the
     *      given source and target mimetypes of the reader and writer
     * @throws ContentIOException if the transformation fails
     */
    @Auditable(parameters = {"reader", "writer"})
    public void transform(ContentReader reader, ContentWriter writer)
            throws NoTransformerException, ContentIOException;
    

    /**
     * @see org.aflresco.service.cmr.repository.ContentService.transform(ContentReader, ContentReader)
     * @see org.aflresco.service.cmr.repository.ContentService.transform(ContentReader, ContentWriter, TransformationOptions)
     * 
     * A map of transform options can be provided.
     * 
     * @param reader the source content location and mimetype
     * @param writer the target content location and mimetype
     * @param options the options for the transformation
     * @throws NoTransformerException if no transformer exists for the
     *      given source and target mimetypes of the reader and writer
     * @throws ContentIOException if the transformation fails
     * 
     * @depricated 
     * As of release 3.0 the TransformOptions class should be used to pass transformation options 
     * to a transformation execution.
     */
    @Auditable(parameters = {"reader", "writer", "options"})
    @Deprecated
    public void transform(ContentReader reader, ContentWriter writer, Map<String, Object> options)
            throws NoTransformerException, ContentIOException;
    
    /**
     * @see org.aflresco.service.cmr.repository.ContentService.transform(ContentReader, ContentReader)
     * 
     * A transformation options can be provided.
     * 
     * @param reader the source content location and mimetype
     * @param writer the target content location and mimetype
     * @param options the options for the transformation
     * @throws NoTransformerException if no transformer exists for the
     *      given source and target mimetypes of the reader and writer
     * @throws ContentIOException if the transformation fails
     */
    @Auditable(parameters = {"reader", "writer", "options"})
    public void transform(ContentReader reader, ContentWriter writer, TransformationOptions options)
            throws NoTransformerException, ContentIOException;
    
    /**
     * Fetch the transformer that is capable of transforming the content in the
     * given source mimetype to the given target mimetype.
     * <p>
     * Since no transformation options are provided only the source and destination mimetypes are
     * considered when getting the correct transformer.
     * 
     * @param the source mimetype
     * @param the target mimetype
     * @return Returns a transformer that can be used, or null if one was not available
     * 
     * @see org.alfresco.service.cmr.respository.ContentService.getTransformer(String, String, TransformationOptions)
     * @see ContentAccessor#getMimetype()
     */
    @Auditable(parameters = {"sourceMimetype", "targetMimetype"})
    public ContentTransformer getTransformer(String sourceMimetype, String targetMimetype);
    
    /**
     * Fetch the transformer that is capable of transforming the content in the
     * given source mimetype to the given target mimetype with the provided transformation
     * options.
     * <p/>
     * The transformation options provide a finer grain way of discovering the correct transformer, 
     * since the values and type of the options provided are considered by the transformer when
     * deciding whether it can satisfy the transformation request.
     * 
     * @param  sourceMimetype       the source mimetype
     * @param  targetMimetype       the target mimetype
     * @param  options              the transformation options
     * @return ContentTransformer   a transformer that can be used, or null if one was not available
     * 
     * @see ContentAccessor#getMimetype()
     */
    @Auditable(parameters = {"sourceMimetype", "targetMimetype", "options"})
    public ContentTransformer getTransformer(String sourceMimetype, String targetMimetype, TransformationOptions options);
    
    /**
     * Fetch all the transformers that are capable of transforming the content in the
     * given source mimetype to the given target mimetype with the provided transformation
     * options.
     * <p/>
     * The transformation options provide a finer grain way of discovering the correct transformer, 
     * since the values and type of the options provided are considered by the transformer when
     * deciding whether it can satisfy the transformation request.
     * <p/>
     * The list will contain all currently active, applicable transformers sorted in repository preference order.
     * The contents of this list may change depending on such factors as the availability of particular transformers
     * as well as their current behaviour. For these reasons, this list should not be cached.
     * 
     * @param  sourceMimetype       the source mimetype
     * @param  targetMimetype       the target mimetype
     * @param  options              the transformation options
     * @return ContentTransformers  a List of the transformers that can be used, or the empty list if none were available
     * 
     * @since 3.5
     * @see ContentAccessor#getMimetype()
     */
    @Auditable(parameters = {"sourceMimetype", "targetMimetype", "options"})
    public List<ContentTransformer> getActiveTransformers(String sourceMimetype, String targetMimetype, TransformationOptions options);
    
    /**
     * Fetch the transformer that is capable of transforming image content.
     * 
     * @return Returns a transformer that can be used, or null if one was not available
     */
    @Auditable
    public ContentTransformer getImageTransformer();
    
    /**
     * Returns whether a transformer exists that can read the content from
     * the reader and write the content back out to the writer.
     * <p>
     * Since no transformation options are specified, only the source and target
     * mimetypes will be considered when making this decision.
     * <p>
     * The mimetypes used for the transformation must be set both on
     * the {@link ContentAccessor#getMimetype() reader} and on the
     * {@link ContentAccessor#getMimetype() writer}.
     * 
     * @param reader the source content location and mimetype
     * @param writer the target content location and mimetype
     * 
     * @return true if a transformer exists, false otherwise
     * 
     * @see org.alfresco.service.cmr.repository.ContentService.isTransformable(ContentReader, ContentWriter, TransformationOptions)
     */
    @Auditable(parameters = {"reader", "writer"})
    public boolean isTransformable(ContentReader reader, ContentWriter writer);
    
    /**
     * Returns whether a transformer exists that can read the content from
     * the reader and write the content back out to the writer with the 
     * provided tranformation options.
     * <p>
     * The mimetypes used for the transformation must be set both on
     * the {@link ContentAccessor#getMimetype() reader} and on the
     * {@link ContentAccessor#getMimetype() writer}.
     * 
     * @param  reader   the source content location and mimetype
     * @param  writer   the target content location and mimetype
     * @param  options  the transformation options
     * @return boolean  true if a transformer exists, false otherwise
     */
    @Auditable(parameters = {"reader", "writer", "options"})
    public boolean isTransformable(ContentReader reader, ContentWriter writer, TransformationOptions options);
}
