/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

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
