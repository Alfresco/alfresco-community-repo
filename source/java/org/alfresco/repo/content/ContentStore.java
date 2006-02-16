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
package org.alfresco.repo.content;

import java.util.Date;
import java.util.Set;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;

/**
 * Provides low-level retrieval of content
 * {@link org.alfresco.service.cmr.repository.ContentReader readers} and
 * {@link org.alfresco.service.cmr.repository.ContentWriter writers}.
 * <p>
 * Implementations of this interface should be soley responsible for
 * providing persistence and retrieval of the content against a
 * <code>content URL</code>.
 * <p>
 * The URL format is <b>store://year/month/day/GUID.bin</b> <br>
 * <ul>
 *   <li> <b>store://</b>: prefix identifying an Alfresco content stores
 *                         regardless of the persistence mechanism. </li>
 *   <li> <b>year</b>: year </li>
 *   <li> <b>month</b>: 1-based month of the year </li>
 *   <li> <b>day</b>: 1-based day of the month </li>
 *   <li> <b>GUID</b>: A unique identifier </li>
 * </ul>
 * The old <b>file://</b> prefix must still be supported - and functionality
 * around this can be found in the {@link org.alfresco.repo.content.AbstractContentStore}
 * implementation.
 * 
 * @author Derek Hulley
 */
public interface ContentStore
{
    /** <b>store://</b> is the new prefix for all content URLs */
    public static final String STORE_PROTOCOL = "store://";
    
    /**
     * Check for the existence of content in the store.
     * <p>
     * The implementation of this may be more efficient than first getting a
     * reader to {@link ContentReader#exists() check for existence}, although
     * that check should also be performed.
     * 
     * @param contentUrl the path to the content
     * @return Returns true if the content exists.
     * @throws ContentIOException
     * 
     * @see ContentReader#exists()
     */
    public boolean exists(String contentUrl) throws ContentIOException;
    
    /**
     * Get the accessor with which to read from the content
     * at the given URL.  The reader is <b>stateful</b> and
     * can <b>only be used once</b>.
     * 
     * @param contentUrl the path to where the content is located
     * @return Returns a read-only content accessor for the given URL.  There may
     *      be no content at the given URL, but the reader must still be returned.
     *      The reader may implement the {@link RandomAccessContent random access interface}.
     * @throws ContentIOException
     *
     * @see #exists(String)
     * @see ContentReader#exists()
     */
    public ContentReader getReader(String contentUrl) throws ContentIOException;
    
    /**
     * Get an accessor with which to write content to a location
     * within the store.  The writer is <b>stateful</b> and can
     * <b>only be used once</b>.  The location may be specified but must, in that case,
     * be a valid and unused URL.
     * <p>
     * By supplying a reader to existing content, the store implementation may
     * enable {@link RandomAccessContent random access}.  The store implementation
     * can enable this by copying the existing content into the new location
     * before supplying a writer onto the new content.
     * 
     * @param existingContentReader a reader onto any existing content for which
     *      a writer is required - may be null
     * @param newContentUrl an unused, valid URL to use - may be null.
     * @return Returns a write-only content accessor, possibly implementing
     *      the {@link RandomAccessContent random access interface}
     * @throws ContentIOException if completely new content storage could not be
     *      created
     *
     * @see ContentWriter#addListener(ContentStreamListener)
     * @see ContentWriter#getContentUrl()
     */
    public ContentWriter getWriter(ContentReader existingContentReader, String newContentUrl) throws ContentIOException;

    /**
     * Get all URLs for the store, regardless of creation time.
     * 
     * @see #getUrls(Date, Date)
     */
    public Set<String> getUrls() throws ContentIOException;

    /**
     * Get a set of all content URLs in the store.  This indicates all content
     * available for reads.
     * 
     * @param createdAfter all URLs returned must have been created after this date.  May be null.
     * @param createdBefore all URLs returned must have been created before this date.  May be null.
     * @return Returns a complete set of the unique URLs of all available content
     *      in the store
     * @throws ContentIOException
     */
    public Set<String> getUrls(Date createdAfter, Date createdBefore) throws ContentIOException;
    
    /**
     * Deletes the content at the given URL.
     * <p>
     * A delete cannot be forced since it is much better to have the
     * file remain longer than desired rather than deleted prematurely.
     * The store implementation should safeguard files for certain
     * minimum period, in which case all files younger than a certain
     * age will not be deleted.
     * 
     * @param contentUrl the URL of the content to delete
     * @return Return true if the content was deleted (either by this or
     *      another operation), otherwise false
     * @throws ContentIOException
     */
    public boolean delete(String contentUrl) throws ContentIOException;
}
