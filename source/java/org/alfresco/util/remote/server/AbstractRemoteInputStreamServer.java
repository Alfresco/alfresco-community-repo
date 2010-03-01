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
package org.alfresco.util.remote.server;

import java.io.IOException;
import java.io.InputStream;

/**
 * The data producing side of the remote connection that the <code>InputStream</code> spans.
 * 
 * @author <a href="mailto:Michael.Shavnev@effective-soft.com">Michael Shavnev</a>
 * @since Alfresco 2.2
 */
public abstract class AbstractRemoteInputStreamServer implements RemoteInputStreamServer
{
    protected InputStream inputStream;

    protected AbstractRemoteInputStreamServer(InputStream inputStream)
    {
        this.inputStream = inputStream;
    }

    public int read() throws IOException
    {
        return inputStream.read();
    }

    public int read(byte[] bytes) throws IOException
    {
        return inputStream.read(bytes);
    }

    public int read(byte[] bytes, int off, int len) throws IOException
    {
        return inputStream.read(bytes, off, len);
    }

    public long skip(long n) throws IOException
    {
        return inputStream.skip(n);
    }

    public int available() throws IOException
    {
        return inputStream.available();
    }

    public void mark(int readlimit)
    {
        inputStream.mark(readlimit);
    }

    public boolean markSupported()
    {
        return inputStream.markSupported();
    }

    public void reset() throws IOException
    {
        inputStream.reset();
    }

    public void close() throws IOException
    {
        inputStream.close();
    }
}
