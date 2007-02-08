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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.remote;

import java.io.IOException;
import java.io.OutputStream;

import org.alfresco.service.cmr.remote.AVMRemoteTransport;

public class AVMRemoteOutputStream extends OutputStream 
{
    private AVMRemoteTransport fAVMRemote;
    
    private String fHandle;
    
    /**
     * Create a new one.
     * @param handle The handle returned from an AVMRemote call.
     * @param remote The AVMRemote instance.
     */
    public AVMRemoteOutputStream(String handle, AVMRemoteTransport remote)
    {
        fAVMRemote = remote;
        fHandle = handle;
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
            fAVMRemote.closeOutputHandle(ClientTicketHolder.GetTicket(), fHandle);
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
                fAVMRemote.writeOutput(ClientTicketHolder.GetTicket(), fHandle, b, len);
            }
            else
            {
                byte [] buff = new byte[len];
                System.arraycopy(b, off, buff, 0, len);
                fAVMRemote.writeOutput(ClientTicketHolder.GetTicket(), fHandle, buff, len);
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
