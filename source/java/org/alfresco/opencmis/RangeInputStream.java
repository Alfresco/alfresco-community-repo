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
package org.alfresco.opencmis;

import java.io.IOException;
import java.io.InputStream;

public class RangeInputStream extends InputStream
{

    private InputStream inputStream;
    private long bytesRead;
    private long length;

    public RangeInputStream(InputStream inputStream, long offset, long length) throws IOException
    {
        super();

        this.inputStream = inputStream;
        this.length = length;
        this.bytesRead = 0;

        long l = this.inputStream.skip(offset);
        if (l < offset)
        {
            this.inputStream.skip(offset);
        }
    }

    @Override
    public int read() throws IOException
    {
        if (bytesRead < length)
        {
            bytesRead++;
            return inputStream.read();
        } else
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
        if (len > length - bytesRead)
        {
            len = (int) (length - bytesRead);
        }
        int readed = inputStream.read(b, off, len);
        bytesRead += readed;
        return readed;
    }

    @Override
    public int available() throws IOException
    {
        return (int) (length - bytesRead + 1);
    }

    @Override
    public void close() throws IOException
    {
        inputStream.close();
    }

    @Override
    public long skip(long n) throws IOException
    {
        if (bytesRead + n > length)
        {
            n = (length - n) > 0 ? (length - n) : length - bytesRead;
        }
        n = inputStream.skip(n);
        bytesRead += n;

        return n;
    }

}
