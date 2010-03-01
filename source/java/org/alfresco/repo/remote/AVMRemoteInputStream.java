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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

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
     * The client ticket holder.
     */
    private ClientTicketHolder fTicketHolder;
    
    /**
     * The handle to the input stream.
     */
    private String fHandle;
    
    /**
     * Construct one.
     * @param handle The handle returned by getInputStream();
     * @param remote The AVMRemote instance.
     */
    public AVMRemoteInputStream(String handle, AVMRemoteTransport remote, 
                                ClientTicketHolder ticketHolder)
    {
        fHandle = handle;
        fAVMRemote = remote;
        fTicketHolder = ticketHolder;
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
            byte [] buff = fAVMRemote.readInput(fTicketHolder.getTicket(), fHandle, 1);
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
            byte [] buff = fAVMRemote.readInput(fTicketHolder.getTicket(), fHandle, len);
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
            fAVMRemote.closeInputHandle(fTicketHolder.getTicket(), fHandle);
        }
        catch (Exception e)
        {
            throw new IOException("Remote Error closing input stream.");
        }
    }
}
