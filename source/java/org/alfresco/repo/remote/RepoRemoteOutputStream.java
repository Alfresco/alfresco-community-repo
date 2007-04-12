/**
 * 
 */
package org.alfresco.repo.remote;

import java.io.IOException;
import java.io.OutputStream;

import org.alfresco.service.cmr.remote.RepoRemoteTransport;

/**
 * A wrapper implementation of OutputStream to work with a
 * RepoRemoteTransport instance.
 * @author britt
 */
public class RepoRemoteOutputStream extends OutputStream 
{
    private RepoRemoteTransport fRepoRemote;
    
    private String fHandle;
    
    private ClientTicketHolder fTicketHolder;
    
    /**
     * Create a new one.
     * @param handle The handle returned from an RepoRemoteTransport call.
     * @param remote The AVMRemote instance.
     */
    public RepoRemoteOutputStream(String handle, RepoRemoteTransport remote,
                                  ClientTicketHolder ticketHolder)
    {
        fRepoRemote = remote;
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
            fRepoRemote.closeOutputHandle(fTicketHolder.getTicket(), fHandle);
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
                fRepoRemote.writeOutput(fTicketHolder.getTicket(), fHandle, b, len);
            }
            else
            {
                byte [] buff = new byte[len];
                System.arraycopy(b, off, buff, 0, len);
                fRepoRemote.writeOutput(fTicketHolder.getTicket(), fHandle, buff, len);
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
