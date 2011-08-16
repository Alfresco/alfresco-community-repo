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
package org.alfresco.repo.content;

import java.util.Date;

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
 * Content URLs must consist of a prefix or protocol followed by an
 * implementation-specific identifier.  For example, the content URL format
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
 * {@link UnsupportedContentUrlException} must be generated.  This will allow
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
     *                          will throw an {@link UnsupportedContentUrlException} when given
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
     * @deprecated      Since 3.3.3 use {@link #getSpaceUsed()}.
     * @see #getSpaceFree()
     * @see #getSpaceTotal()
     */
    public long getTotalSize();
    
    /**
     * Calculates the total size of <b>stored content</b>, excluding any other data in the underlying
     * storage.
     * <p/>
     * <b>NOTE:</b> Calculating this value can be time-consuming - use sparingly.
     * <p/>
     * <b>NOTE:</b> For efficiency, some implementations may provide a guess.  If not, this call could
     * take a long time.
     * 
     * @return
     *      Returns the total, possibly approximate size (in bytes) of the binary data stored or <tt>-1</tt>
     *      if no size data is available.
     * 
     * @since 3.3.3
     */
    public long getSpaceUsed();
    
    /**
     * Calcualates the remaing <i>free</i> space in the underlying store.
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
     * @throws UnsupportedContentUrlException
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
     * @throws UnsupportedContentUrlException
     *      if the content URL supplied is not supported by the store
     * @throws ContentIOException
     *      if an IO error occurs
     *
     * @see #exists(String)
     * @see ContentReader#exists()
     * @see EmptyContentReader
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
     * enable {@link RandomAccessContent random access}.  The store implementation
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
     * @see #getWriteSupport()
     * @see ContentWriter#addListener(ContentStreamListener)
     * @see ContentWriter#getContentUrl()
     */
    public ContentWriter getWriter(ContentContext context);
    
    /**
     * Shortcut method to {@link #getWriter(ContentContext)}.
     * 
     * @see #getWriter(ContentContext)
     * 
     * @deprecated
     */
    public ContentWriter getWriter(ContentReader existingContentReader, String newContentUrl);

    /**
     * Get all URLs for the store, regardless of creation time.
     * @return
     *      Returns a set of all unique content URLs in the store
     * @throws ContentIOException
     *      if an IO error occurs
     * @throws UnsupportedOperationException
     *      if the store is unable to provide the information
     * 
     * @see #getUrls(Date, Date)
     */
    public void getUrls(ContentUrlHandler handler) throws ContentIOException;

    /**
     * Get a set of all content URLs in the store.  This indicates all content available for reads.
     * 
     * @param createdAfter
     *      all URLs returned must have been created after this date.  May be null.
     * @param createdBefore
     *      all URLs returned must have been created before this date.  May be null.
     * @param handler
     *      the callback that will passed each URL
     * @throws ContentIOException
     *      if an error occurs
     * @throws UnsupportedOperationException
     *      if the store is unable to provide the information
     */
    public void getUrls(Date createdAfter, Date createdBefore, ContentUrlHandler handler) throws ContentIOException;
    
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
     * Iterface for to use during iteration over content URLs.
     * 
     * @author Derek Hulley
     * @since 2.0
     */
    public interface ContentUrlHandler
    {
        void handle(String contentUrl);
    }
}
