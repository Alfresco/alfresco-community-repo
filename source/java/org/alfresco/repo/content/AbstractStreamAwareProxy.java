package org.alfresco.repo.content;

import java.io.Closeable;
import java.io.IOException;

/**
 * Base class for stream aware proxies
 * 
 * @author Dmitry Velichkevich
 */
public abstract class AbstractStreamAwareProxy
{
    /**
     * @return {@link Closeable} instance which represents channel or stream which uses channel
     */
    protected abstract Closeable getStream();

    /**
     * @return {@link Boolean} value which determines whether stream can (<code>true</code>) or cannot ((<code>false</code>)) be closed
     */
    protected abstract boolean canBeClosed();

    /**
     * Encapsulates the logic of releasing the captured stream or channel. It is expected that each resource object shares the same channel
     */
    public void release()
    {
        Closeable stream = getStream();

        if ((null == stream) || !canBeClosed())
        {
            return;
        }

        try
        {
            stream.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to close stream!", e);
        }
    }
}
