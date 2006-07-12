/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.repository;

import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.Auditable;
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
public interface ContentService
{
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
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef", "propertyQName"})
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
     * 
     * @param nodeRef a reference to a node having a content property
     * @param propertyQName the name of the property, which must be of type <b>content</b>
     * @param update true if the property must be updated atomically when the content write
     *      stream is closed (attaches a listener to the stream); false if the client code
     *      will perform the updates itself.
     * @return Returns a writer for the content associated with the node property
     * @throws InvalidNodeRefException if the node doesn't exist
     * @throws InvalidTypeException if the node property is not of type <b>content</b>
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef", "propertyQName", "update"})
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
     * Fetch the transformer that is capable of transforming the content in the
     * given source mimetype to the given target mimetype.
     * 
     * @param the source mimetype
     * @param the target mimetype
     * @return Returns a transformer that can be used, or null if one was not available
     * 
     * @see ContentAccessor#getMimetype()
     */
    @Auditable(parameters = {"sourceMimetype", "targetMimetype"})
    public ContentTransformer getTransformer(String sourceMimetype, String targetMimetype);
    
    /**
     * Returns whether a transformer exists that can read the content from
     * the reader and write the content back out to the writer.
     * <p>
     * The mimetypes used for the transformation must be set both on
     * the {@link ContentAccessor#getMimetype() reader} and on the
     * {@link ContentAccessor#getMimetype() writer}.
     * 
     * @param reader the source content location and mimetype
     * @param writer the target content location and mimetype
     * 
     * @return true if a transformer exists, false otherwise
     */
    @Auditable(parameters = {"reader", "writer"})
    public boolean isTransformable(ContentReader reader, ContentWriter writer);
}
