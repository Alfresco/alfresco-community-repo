/**
 * 
 */
package org.alfresco.repo.remote;

import java.io.IOException;
import java.io.InputStream;

import org.alfresco.service.cmr.remote.RepoRemoteTransport;

/**
 * A wrapper implementation of InputStream to work with
 * a RepoRemoteTransport instance.
 * @author britt
 */
public class RepoRemoteInputStream extends InputStream 
{
    /**
     * The RepoRemoteTransport reference.
     */
    private RepoRemoteTransport fRepoRemote;
    
    /**
     * The ticket holder.
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
    public RepoRemoteInputStream(String handle, RepoRemoteTransport remote,
                                 ClientTicketHolder ticketHolder)
    {
        fHandle = handle;
        fRepoRemote = remote;
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
            byte [] buff = fRepoRemote.readInput(fTicketHolder.getTicket(), fHandle, 1);
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
            byte [] buff = fRepoRemote.readInput(fTicketHolder.getTicket(), fHandle, len);
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
            fRepoRemote.closeInputHandle(fTicketHolder.getTicket(), fHandle);
        }
        catch (Exception e)
        {
            throw new IOException("Remote Error closing input stream.");
        }
    }
}
