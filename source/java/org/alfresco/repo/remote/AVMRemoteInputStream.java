/*
 * Copyright (C) 2006 Alfresco, Inc.
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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.remote;

import java.io.IOException;
import java.io.InputStream;

import org.alfresco.service.cmr.remote.AVMRemoteTransport;

/**
 * Wrapper around AVMRemote stream reading.
 * @author britt
 */
public class AVMRemoteInputStream extends InputStream
{
    /**
     * The AVMRemote reference.
     */
    private AVMRemoteTransport fAVMRemote;
    
    /**
     * The handle to the input stream.
     */
    private String fHandle;
    
    /**
     * Construct one.
     * @param handle The handle returned by getInputStream();
     * @param remote The AVMRemote instance.
     */
    public AVMRemoteInputStream(String handle, AVMRemoteTransport remote)
    {
        fHandle = handle;
        fAVMRemote = remote;
    }
    
    /**
     * Read in a single byte.
     * @return The byte as 0-255 or -1 for eof.
     */
    @Override
    public int read() throws IOException
    {
        try
        {
            byte [] buff = fAVMRemote.readInput(ClientTicketHolder.GetTicket(), fHandle, 1);
            if (buff.length == 0)
            {
                return -1;
            }
            return ((int)buff[0]) & 0xff;
        }
        catch (Exception e)
        {
            throw new IOException("Remote I/O Error.");
        }
    }

    /**
     * Read a buffer of bytes.
     * @param b The buffer into which to put the bytes.
     * @param off The offset into the buffer.
     * @param len The number of bytes to read.
     * @return The actual number of bytes read or -1 on eof.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        try
        {
            byte [] buff = fAVMRemote.readInput(ClientTicketHolder.GetTicket(), fHandle, len);
            if (buff.length == 0)
            {
                return -1;
            }
            System.arraycopy(buff, 0, b, off, buff.length);
            return buff.length;
        }
        catch (Exception e)
        {
            throw new IOException("Remote I/O Error.");
        }
    }

    /**
     * Close the underlying AVMRemote handle.
     */
    @Override
    public void close() throws IOException
    {
        try
        {
            fAVMRemote.closeInputHandle(ClientTicketHolder.GetTicket(), fHandle);
        }
        catch (Exception e)
        {
            throw new IOException("Remote Error closing input stream.");
        }
    }
}
