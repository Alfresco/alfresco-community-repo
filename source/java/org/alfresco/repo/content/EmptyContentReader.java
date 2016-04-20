package org.alfresco.repo.content;

import java.nio.channels.ReadableByteChannel;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * A blank reader for which <code>exists()</code> always returns false.
 * 
 * @author Derek Hulley
 */
public class EmptyContentReader extends AbstractContentReader
{
    /**
     * @param contentUrl    the content URL
     */
    public EmptyContentReader(String contentUrl)
    {
        super(contentUrl);
    }
    
    /**
     * @return  Returns an instance of the this class
     */
    @Override
    protected ContentReader createReader() throws ContentIOException
    {
        return new EmptyContentReader(this.getContentUrl());
    }

    @Override
    protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException
    {
        // ALF-17708: If we got the contentdata from the transactional cache, there's a chance that eager cleaning can
        // remove the content from under our feet
        throw new ConcurrencyFailureException(getContentUrl() + " no longer exists");
    }

    public boolean exists()
    {
        return false;
    }

    public long getLastModified()
    {
        return 0L;
    }

    public long getSize()
    {
        return 0L;
    }
}
