/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.content.caching;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Locale;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.MimetypeServiceAware;

/**
 * Wrapper for cache writer that is aware of backing store
 * writer and calls backing store for getSize and getContentData
 * in case if cache file was deleted
 * 
 * @see <a href=https://issues.alfresco.com/jira/browse/MNT-9663>MNT-9663</a>
 * 
 * @author Viachaslau Tsikhanovich
 *
 */
public class BackingStoreAwareCacheWriter implements ContentWriter, MimetypeServiceAware
{
    /** Cache writer **/
    private ContentWriter cacheWriter;

    /** Backing store writer **/
    private ContentWriter bsWriter;

    public BackingStoreAwareCacheWriter(ContentWriter cacheWriter, ContentWriter bsWriter)
    {
        this.cacheWriter = cacheWriter;
        this.bsWriter = bsWriter;
    }

    @Override
    public boolean isChannelOpen()
    {
        return this.cacheWriter.isChannelOpen();
    }

    @Override
    public void addListener(ContentStreamListener listener)
    {
        this.cacheWriter.addListener(listener);
    }

    @Override
    public long getSize()
    {
        if (!this.cacheWriter.getReader().exists())
        {
            return this.bsWriter.getSize();
        }
        return this.cacheWriter.getSize();
    }

    @Override
    public ContentData getContentData()
    {
        if (!this.cacheWriter.getReader().exists())
        {
            return this.bsWriter.getContentData();
        }
        return this.cacheWriter.getContentData();
    }

    @Override
    public String getContentUrl()
    {
        return this.cacheWriter.getContentUrl();
    }

    @Override
    public String getMimetype()
    {
        return this.cacheWriter.getMimetype();
    }

    @Override
    public void setMimetype(String mimetype)
    {
        this.cacheWriter.setMimetype(mimetype);
    }

    @Override
    public String getEncoding()
    {
        return this.cacheWriter.getEncoding();
    }

    @Override
    public void setEncoding(String encoding)
    {
        this.cacheWriter.setEncoding(encoding);
    }

    @Override
    public Locale getLocale()
    {
        return this.cacheWriter.getLocale();
    }

    @Override
    public void setLocale(Locale locale)
    {
        this.cacheWriter.setLocale(locale);
    }

    @Override
    public ContentReader getReader() throws ContentIOException
    {
        return this.cacheWriter.getReader();
    }

    @Override
    public boolean isClosed()
    {
        return this.cacheWriter.isClosed();
    }

    @Override
    public WritableByteChannel getWritableChannel() throws ContentIOException
    {
        return this.cacheWriter.getWritableChannel();
    }

    @Override
    public FileChannel getFileChannel(boolean truncate) throws ContentIOException
    {
        return this.cacheWriter.getFileChannel(truncate);
    }

    @Override
    public OutputStream getContentOutputStream() throws ContentIOException
    {
        return this.cacheWriter.getContentOutputStream();
    }

    @Override
    public void putContent(ContentReader reader) throws ContentIOException
    {
        this.cacheWriter.putContent(reader);
    }

    @Override
    public void putContent(InputStream is) throws ContentIOException
    {
        this.cacheWriter.putContent(is);
    }

    @Override
    public void putContent(File file) throws ContentIOException
    {
        this.cacheWriter.putContent(file);
    }

    @Override
    public void putContent(String content) throws ContentIOException
    {
        this.cacheWriter.putContent(content);
    }

    @Override
    public void guessMimetype(String filename)
    {
        this.cacheWriter.guessMimetype(filename);
    }

    @Override
    public void guessEncoding()
    {
        this.cacheWriter.guessEncoding();
    }

    @Override
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        ((MimetypeServiceAware) cacheWriter).setMimetypeService(mimetypeService);
    }
}
