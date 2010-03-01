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
import java.io.OutputStream;

import org.alfresco.service.cmr.remote.AVMRemoteTransport;

public class AVMRemoteOutputStream extends OutputStream 
{
    private AVMRemoteTransport fAVMRemote;
    
    private String fHandle;
    
    private ClientTicketHolder fTicketHolder;
    
    /**
     * Create a new one.
     * @param handle The handle returned from an AVMRemote call.
     * @param remote The AVMRemote instance.
     */
    public AVMRemoteOutputStream(String handle, AVMRemoteTransport remote,
                                 ClientTicketHolder ticketHolder)
    {
        fAVMRemote = remote;
        fHandle = handle;
        fTicketHolder = ticketHolder;
    }
    
    /**
     * Write one character.
     * @param b The character.
     */
    @Override
    public void write(int b) 
        throws IOException 
    {
        byte [] buff = new byte[1];
        buff[0] = (byte)b;
        write(buff);
    }

    /**
     * Close the stream.
     */
    @Override
    public void close() 
        throws IOException 
    {
        try
        {
            fAVMRemote.closeOutputHandle(fTicketHolder.getTicket(), fHandle);
        }
        catch (Exception e)
        {
            throw new IOException("IO Error: " + e);
        }
    }

    /**
     * Write a portion of a block of bytes.
     * @param b The buffer containing the data.
     * @param off The offset into the buffer.
     * @param len The number of bytes to write.
     */
    @Override
    public void write(byte[] b, int off, int len) 
    throws IOException 
    {
        try
        {
            if (off == 0)
            {
                fAVMRemote.writeOutput(fTicketHolder.getTicket(), fHandle, b, len);
            }
            else
            {
                byte [] buff = new byte[len];
                System.arraycopy(b, off, buff, 0, len);
                fAVMRemote.writeOutput(fTicketHolder.getTicket(), fHandle, buff, len);
            }
        }
        catch (Exception e)
        {
            throw new IOException("IO Error: " + e);
        }
    }

    /**
     * Write a buffer of data.
     * @param b The buffer.
     */
    @Override
    public void write(byte[] b) 
        throws IOException 
    {
        write(b, 0, b.length);
    }
}
