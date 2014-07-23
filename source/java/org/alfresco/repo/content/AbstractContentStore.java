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

import org.alfresco.repo.content.ContentLimitProvider.NoLimitProvider;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class providing support for different types of content stores.
 * <p>
 * Since content URLs have to be consistent across all stores for
 * reasons of replication and backup, the most important functionality
 * provided is the generation of new content URLs and the checking of
 * existing URLs.
 * <p>
 * Implementations must override either of the <b>getWriter</b> methods;
 * {@link #getWriter(ContentContext)} or {@link #getWriterInternal(ContentReader, String)}.
 * 
 * @see #getWriter(ContentReader, String)
 * @see #getWriterInternal(ContentReader, String)
 * 
 * @author Derek Hulley
 */
public abstract class AbstractContentStore implements ContentStore
{
    private static Log logger = LogFactory.getLog(AbstractContentStore.class);
    /** Helper */
    private static final int PROTOCOL_DELIMETER_LENGTH = PROTOCOL_DELIMITER.length();

    /**
     * Checks that the content conforms to the format <b>protocol://identifier</b>
     * as specified in the contract of the {@link ContentStore} interface.
     * 
     * @param contentUrl    the content URL to check
     * @return              Returns <tt>true</tt> if the content URL is valid
     * 
     * @since 2.1
     */
    public static final boolean isValidContentUrl(String contentUrl)
    {
        if (contentUrl == null)
        {
            return false;
        }
        int index = contentUrl.indexOf(ContentStore.PROTOCOL_DELIMITER);
        if (index <= 0)
        {
            return false;
        }
        if (contentUrl.length() <= (index + PROTOCOL_DELIMETER_LENGTH))
        {
            return false;
        }
        return true;
    }

    protected ContentLimitProvider contentLimitProvider = new NoLimitProvider();

    /**
     * An object that prevents abuse of the underlying store(s)
     */
    public void setContentLimitProvider(ContentLimitProvider contentLimitProvider)
    {
        this.contentLimitProvider = contentLimitProvider;
    }
    
    /**
     * Splits the content URL into its component parts as separated by
     * {@link ContentStore#PROTOCOL_DELIMITER protocol delimiter}.
     * 
     * @param contentUrl    the content URL to split
     * @return              Returns the protocol and identifier portions of the content URL,
     *                      both of which will not be <tt>null</tt>
     * @throws              UnsupportedContentUrlException if the content URL is invalid
     * 
     * @since 2.1
     */
    protected Pair<String, String> getContentUrlParts(String contentUrl)
    {
        if (contentUrl == null)
        {
            throw new IllegalArgumentException("The contentUrl may not be null");
        }
        int index = contentUrl.indexOf(ContentStore.PROTOCOL_DELIMITER);
        if (index <= 0)
        {
            throw new UnsupportedContentUrlException(this, contentUrl);
        }
        String protocol = contentUrl.substring(0, index);
        String identifier = contentUrl.substring(
                index + PROTOCOL_DELIMETER_LENGTH,
                contentUrl.length());
        if (identifier.length() == 0)
        {
            throw new UnsupportedContentUrlException(this, contentUrl);
        }
        return new Pair<String, String>(protocol, identifier);
    }

    /**
     * Override this method to supply a efficient and direct check of the URL supplied.
     * The default implementation checks whether {@link ContentStore#getReader(String)}
     * throws the {@link UnsupportedContentUrlException} exception.
     * 
     * @since 2.1
     */
    @Override
    public boolean isContentUrlSupported(String contentUrl)
    {
        try
        {
            getReader(contentUrl);
            return true;
        }
        catch (UnsupportedContentUrlException e)
        {
            // It is not supported
            return false;
        }
    }

    /**
     * Override if the derived class supports the operation.
     * 
     * @throws UnsupportedOperationException    always
     * 
     * @since 2.1
     */
    @Override
    public boolean delete(String contentUrl)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @see #getUrls(Date, Date, ContentUrlHandler)
     */
    @SuppressWarnings("deprecation")
    @Override
    public final void getUrls(ContentUrlHandler handler) throws ContentIOException
    {
        getUrls(null, null, handler);
    }

    /**
     * Override to provide an implementation.  If no implementation is supplied, then the store will not support
     * cleaning of orphaned content binaries.
     * 
     * @throws UnsupportedOperationException always
     */
    @SuppressWarnings("deprecation")
    @Override
    public void getUrls(Date createdAfter, Date createdBefore, ContentUrlHandler handler) throws ContentIOException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Implement to supply a store-specific writer for the given existing content
     * and optional target content URL.
     * 
     * @param existingContentReader     a reader onto any content to initialize the new writer with
     * @param newContentUrl             an optional target for the new content
     *                 
     * @throws UnsupportedContentUrlException
     *      if the content URL supplied is not supported by the store
     * @throws ContentExistsException
     *      if the content URL is already in use
     * @throws ContentIOException
     *      if an IO error occurs
     * 
     * @since 2.1
     */
    protected ContentWriter getWriterInternal(ContentReader existingContentReader, String newContentUrl)
    {
        throw new UnsupportedOperationException("Override getWriterInternal (preferred) or getWriter");
    }

    /**
     * An implementation that does some sanity checking before requesting a writer from the
     * store.  If this method is not overridden, then an implementation of
     * {@link #getWriterInternal(ContentReader, String)} must be supplied.
     * 
     * @see #getWriterInternal(ContentReader, String)
     * @since 2.1
     */
    @Override
    public ContentWriter getWriter(ContentContext context)
    {
        ContentReader existingContentReader = context.getExistingContentReader();
        String contentUrl = context.getContentUrl();
        // Check if the store handles writes
        if (!isWriteSupported())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Write requests are not supported for this store:\n" +
                        "   Store:   " + this + "\n" +
                        "   Context: " + context);
            }
            throw new UnsupportedOperationException("Write operations are not supported by this store: " + this);
        }
        // Check the content URL
        if (contentUrl != null)
        {
            if (!isContentUrlSupported(contentUrl))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "Specific writer content URL is unsupported: \n" +
                            "   Store:   " + this + "\n" +
                            "   Context: " + context);
                }
                throw new UnsupportedContentUrlException(this, contentUrl);
            }
            else if (exists(contentUrl))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "The content location is already used: \n" +
                            "   Store:   " + this + "\n" +
                            "   Context: " + context);
                }
                throw new ContentExistsException(this, contentUrl);
            }
        }
        // Get the writer
        ContentWriter writer = getWriterInternal(existingContentReader, contentUrl);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Fetched new writer: \n" +
                    "   Store:   " + this + "\n" +
                    "   Context: " + context + "\n" +
                    "   Writer:  " + writer);
        }
        return writer;
    }

    /**
     * Simple implementation that uses the
     * {@link ContentReader#exists() reader's exists} method as its implementation.
     * Override this method if a more efficient implementation is possible.
     */
    @Override
    public boolean exists(String contentUrl)
    {
        ContentReader reader = getReader(contentUrl);
        return reader.exists();
    }

    /**
     * @return      Returns <tt>-1</tt> always
     */
    @Override
    public long getSpaceFree()
    {
        return -1;
    }

    /**
     * @return      Returns <tt>-1</tt> always
     */
    @Override
    public long getSpaceTotal()
    {
        return -1;
    }

    /**
     * @return      Returns a '.' (period) always
     */
    @Override
    public String getRootLocation()
    {
        return ".";
    }
}
