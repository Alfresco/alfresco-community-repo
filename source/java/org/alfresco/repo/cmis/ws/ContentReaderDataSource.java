/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import javax.activation.DataSource;

import org.alfresco.service.cmr.repository.ContentReader;

/**
 * DataSource facade for an Alfresco Content Reader
 * 
 * @author Dmitry Lazurkin
 */
public class ContentReaderDataSource implements DataSource
{
    private String mimetype;
    private InputStream inputStream;
    private String name;
    private long offset = 0;
    private long length = Long.MAX_VALUE / 2;
    private long sizeToRead = 0;

    public ContentReaderDataSource(ContentReader contentReader, String name, BigInteger offset, BigInteger length, long contentSize)
    {
        createContentReaderDataSource(contentReader.getContentInputStream(), contentReader.getMimetype(), name, offset, length, contentSize);
    }

    public ContentReaderDataSource(InputStream contentInputStream, String mimeType, String name, BigInteger offset, BigInteger length)
    {
        try
        {
            createContentReaderDataSource(contentInputStream, mimeType, name, offset, length, contentInputStream.available());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void createContentReaderDataSource(InputStream contentInputStream, String mimeType, String name, BigInteger offset, BigInteger length, long contentSize)
    {
        this.name = name;
        this.mimetype = mimeType;
        if (offset != null)
        {
            this.offset = offset.longValue();
        }
        if (length != null)
        {
            this.length = length.longValue();
        }
        if (this.offset + this.length < contentSize)
        {
            this.sizeToRead = this.length;
        }
        else
        {
            this.sizeToRead = contentSize - this.offset;
        }
        if (this.sizeToRead < 0)
        {
            throw new RuntimeException("Offset value exceeds content size");
        }
        try
        {
            inputStream = new RangedInputStream(contentInputStream);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public String getContentType()
    {
        return mimetype;
    }

    public InputStream getInputStream() throws IOException
    {
        return inputStream;
    }

    public String getName()
    {
        return name;
    }

    public OutputStream getOutputStream() throws IOException
    {
        return null;
    }

    public long getSizeToRead()
    {
        return sizeToRead;
    }

    private class RangedInputStream extends InputStream
    {

        private InputStream inputStream;
        private int bytesread;

        private RangedInputStream(InputStream inputStream) throws IOException
        {
            super();
            this.inputStream = inputStream;
            this.inputStream.skip(offset);
            this.bytesread = 0;
        }

        @Override
        public int read() throws IOException
        {
            if (bytesread < sizeToRead)
            {
                bytesread++;
                return inputStream.read();
            }
            else
            {
                return -1;
            }
        }

        @Override
        public int read(byte[] b) throws IOException
        {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            if (len > sizeToRead - bytesread)
            {
                len = (int) (sizeToRead - bytesread);
            }
            int readed = inputStream.read(b, off, len);
            bytesread += readed;
            return readed;
        }

        @Override
        public int available() throws IOException
        {
            return (int) (sizeToRead - bytesread + 1);
        }

        @Override
        public void close() throws IOException
        {
            inputStream.close();
        }

        @Override
        public long skip(long n) throws IOException
        {
            if (bytesread + n > sizeToRead)
            {
                n = (sizeToRead - n) > 0 ? (sizeToRead - n) : sizeToRead - bytesread;
            }
            n = inputStream.skip(n);
            bytesread += n;
            return n;
        }
    }
}
