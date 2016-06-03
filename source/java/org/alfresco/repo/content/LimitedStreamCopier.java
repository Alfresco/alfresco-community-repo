package org.alfresco.repo.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is a simple utility to copy bytes from an {@link InputStream} to an {@link OutputStream}.
 * The copy can be performed with an optional byte limit and as soon as this limit is reached,
 * the copy will be stopped immediately and a {@link ContentLimitViolationException} will be thrown.
 * 
 * @since Thor
 */
public final class LimitedStreamCopier
{
    private static final Log logger = LogFactory.getLog(LimitedStreamCopier.class);
    private static final int BYTE_BUFFER_SIZE = 4096;
    
    /**
     * Copy of the the Spring FileCopyUtils, but does not silently absorb IOExceptions
     * when the streams are closed.  We require the stream write to happen successfully.
     * <p/>
     * Both streams are closed but any IOExceptions are thrown
     * 
     * @param in the stream from which to read content.
     * @param out the stream to which to write content.
     * @param sizeLimit the maximum number of bytes that will be copied between the streams before a
     *                  {@link ContentLimitViolationException} will be thrown.
     *                  A negative number or zero will be deemed to mean 'no limit'.
     */
    public final int copyStreams(InputStream in, OutputStream out, long sizeLimit) throws IOException
    {
        long bytes = copyStreamsLong(in, out, sizeLimit);
        if (bytes > Integer.MAX_VALUE)
        {
            throw new IllegalArgumentException(bytes + " cannot be cast to int.");
        }
        
        return (int) bytes;
    }
    
    public final long copyStreamsLong(InputStream in, OutputStream out, long sizeLimit) throws IOException
    {
        long byteCount = 0;
        IOException error = null;
        
        long totalBytesRead = 0;
        
        try
        {
            byte[] buffer = new byte[BYTE_BUFFER_SIZE];
            int bytesRead = -1;
            while ((bytesRead = in.read(buffer)) != -1)
            {
                // We are able to abort the copy immediately upon limit violation.
                totalBytesRead += bytesRead;
                if (sizeLimit > 0 && totalBytesRead > sizeLimit)
                {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Content size violation, limit = ")
                       .append(sizeLimit);
                    
                    throw new ContentLimitViolationException(msg.toString());
                }
                
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
        }
        finally
        {
            try
            {
                in.close();
            }
            catch (IOException e)
            {
                error = e;
                logger.error("Failed to close input stream: " + this, e);
            }
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                error = e;
                logger.error("Failed to close output stream: " + this, e);
            }
        }
        if (error != null)
        {
            throw error;
        }
        return byteCount;
    }
}
