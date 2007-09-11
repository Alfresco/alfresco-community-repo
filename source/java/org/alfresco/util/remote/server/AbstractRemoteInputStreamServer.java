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
