/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import java.io.Closeable;
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

/**
 * Proxy for {@link ContentWriter} which captures {@link OutputStream} or {@link WritableByteChannel} to introduce a possibility of releasing captured resource
 * 
 * @author Dmitry Velichkevich
 * @see ContentWriter
 * @see AbstractStreamAwareProxy
 */
public class StreamAwareContentWriterProxy extends AbstractStreamAwareProxy implements ContentWriter
{
    private ContentWriter delegatee;

    private Closeable releaseableResource;

    public StreamAwareContentWriterProxy(ContentWriter delegator)
    {
        this.delegatee = delegator;
    }

    @Override
    public OutputStream getContentOutputStream() throws ContentIOException
    {
        OutputStream result = delegatee.getContentOutputStream();

        if (null == releaseableResource)
        {
            releaseableResource = result;
        }

        return result;
    }

    @Override
    public FileChannel getFileChannel(boolean truncate) throws ContentIOException
    {
        FileChannel result = delegatee.getFileChannel(truncate);

        if (null == releaseableResource)
        {
            releaseableResource = result;
        }

        return result;
    }

    @Override
    public ContentReader getReader() throws ContentIOException
    {
        return delegatee.getReader();
    }

    @Override
    public WritableByteChannel getWritableChannel() throws ContentIOException
    {
        WritableByteChannel result = delegatee.getWritableChannel();

        if (null == releaseableResource)
        {
            releaseableResource = result;
        }

        return result;
    }

    @Override
    public void guessEncoding()
    {
        delegatee.guessEncoding();
    }

    @Override
    public void guessMimetype(String filename)
    {
        delegatee.guessMimetype(filename);
    }

    @Override
    public boolean isClosed()
    {
        return delegatee.isClosed();
    }

    @Override
    public void putContent(ContentReader reader) throws ContentIOException
    {
        delegatee.putContent(reader);
    }

    @Override
    public void putContent(InputStream is) throws ContentIOException
    {
        delegatee.putContent(is);
    }

    @Override
    public void putContent(File file) throws ContentIOException
    {
        delegatee.putContent(file);
    }

    @Override
    public void putContent(String content) throws ContentIOException
    {
        delegatee.putContent(content);
    }

    @Override
    public void addListener(ContentStreamListener listener)
    {
        delegatee.addListener(listener);
    }

    @Override
    public ContentData getContentData()
    {
        return delegatee.getContentData();
    }

    @Override
    public String getContentUrl()
    {
        return delegatee.getContentUrl();
    }

    @Override
    public String getEncoding()
    {
        return delegatee.getEncoding();
    }

    @Override
    public Locale getLocale()
    {
        return delegatee.getLocale();
    }

    @Override
    public String getMimetype()
    {
        return delegatee.getMimetype();
    }

    @Override
    public long getSize()
    {
        return delegatee.getSize();
    }

    @Override
    public boolean isChannelOpen()
    {
        return delegatee.isChannelOpen();
    }

    @Override
    public void setEncoding(String encoding)
    {
        delegatee.setEncoding(encoding);
    }

    @Override
    public void setLocale(Locale locale)
    {
        delegatee.setLocale(locale);
    }

    @Override
    public void setMimetype(String mimetype)
    {
        delegatee.setMimetype(mimetype);
    }

    @Override
    public boolean canBeClosed()
    {
        return delegatee.isChannelOpen();
    }

    @Override
    public Closeable getStream()
    {
        return releaseableResource;
    }
}
