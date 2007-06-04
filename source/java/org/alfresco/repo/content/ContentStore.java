/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.content;

import java.util.Date;
import java.util.Set;

import org.alfresco.service.cmr.repository.ContentAccessor;
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
 *   <li> <b>hour</b>: 0-based hour of the day </li>
 *   <li> <b>minute</b>: 0-based minute of the hour </li>
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
     * @param contentUrl        the path to the content
     * @return                  Returns true if the content exists, otherwise
     *                          false if the content doesn't exist or if the URL
     *                          is not applicable to this store.
     * @throws ContentIOException
     * 
     * @see ContentReader#exists()
     */
    public boolean exists(String contentUrl) throws ContentIOException;
    
    /**
     * Get the accessor with which to read from the content at the given URL.
     * The reader is <b>stateful</b> and can <b>only be used once</b>.
     * 
     * @param contentUrl the path to where the content is located
     * @return Returns a read-only content accessor for the given URL.  There may
     *      be no content at the given URL, but the reader must still be returned.
     * @throws ContentIOException
     *
     * @see #exists(String)
     * @see ContentReader#exists()
     * @see EmptyContentReader
     */
    public ContentReader getReader(String contentUrl) throws ContentIOException;
    
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
     * enable {@link RandomAccessContent random access}.  The store implementation
     * can enable this by copying the existing content into the new location
     * before supplying a writer onto the new content.
     * 
     * @param context                   the context of content.
     * @return  Returns a write-only content accessor
     * @throws ContentIOException if completely new content storage could not be created
     *
     * @see #getWriter(ContentReader, String)
     * @see ContentWriter#addListener(ContentStreamListener)
     * @see ContentWriter#getContentUrl()
     */
    public ContentWriter getWriter(ContentContext context) throws ContentIOException;
    
    /**
     * Shortcut method to {@link #getWriter(ContentContext)}.
     * 
     * @see #getWriter(ContentContext)
     * 
     * @deprecated
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
     * 
     * @param contentUrl the URL of the content to delete
     * @return Return true if the content was deleted (either by this or
     *      another operation), otherwise false.  If the content no longer
     *      exists, then <tt>true</tt> is returned.
     * @throws ContentIOException
     */
    public boolean delete(String contentUrl) throws ContentIOException;
}
