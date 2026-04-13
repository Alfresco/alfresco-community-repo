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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.encoding;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @since 2.1
 * @author Derek Hulley
 */
public abstract class AbstractCharactersetFinder implements CharactersetFinder
{
    private static Log logger = LogFactory.getLog(AbstractCharactersetFinder.class);
    private static boolean isDebugEnabled = logger.isDebugEnabled();
    
    private int bufferSize;
    
    public AbstractCharactersetFinder()
    {
        this.bufferSize  = 8192;
    }

    /**
     * Set the maximum number of bytes to read ahead when attempting to determine the characterset.
     * Most characterset detectors are efficient and can process 8K of buffered data very quickly.
     * Some, may need to be constrained a bit.
     * 
     * @param bufferSize        the number of bytes - default 8K.
     */
    public void setBufferSize(int bufferSize)
    {
        this.bufferSize = bufferSize;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The input stream is checked to ensure that it supports marks, after which
     * a buffer is extracted, leaving the stream in its original state.
     */
    public final Charset detectCharset(InputStream is)
    {
        // Only support marking streams
        if (!is.markSupported())
        {
            throw new IllegalArgumentException("The InputStream must support marks.  Wrap the stream in a BufferedInputStream.");
        }
        try
        {
            int bufferSize = getBufferSize();
            if (bufferSize < 0)
            {
                throw new RuntimeException("The required buffer size may not be negative: " + bufferSize);
            }
            // Mark the stream for just a few more than we actually will need
            is.mark(bufferSize);
            // Create a buffer to hold the data
            byte[] buffer = new byte[bufferSize];
            // Fill it
            int read = is.read(buffer);
            // Create an appropriately sized buffer
            if (read > -1 && read < buffer.length)
            {
                byte[] copyBuffer = new byte[read];
                System.arraycopy(buffer, 0, copyBuffer, 0, read);
                buffer = copyBuffer;
            }
            // Detect
            return detectCharset(buffer);
        }
        catch (IOException e)
        {
            // Attempt a reset
            throw new AlfrescoRuntimeException("IOException while attempting to detect charset encoding.", e);
        }
        finally
        {
            try { is.reset(); } catch (Throwable ee) {}
        }
    }
    
    public final Charset detectCharset(byte[] buffer)
    {
        try
        {
            Charset charset = detectCharsetImpl(buffer);
            // Done
            if (isDebugEnabled)
            {
                if (charset == null)
                {
                    // Read a few characters for debug purposes
                    logger.debug("\n" +
                            "Failed to identify stream character set: \n" +
                            "   Guessed 'chars': " + Arrays.toString(buffer));
                }
                else
                {
                    // Read a few characters for debug purposes
                    logger.debug("\n" +
                            "Identified character set from stream:\n" +
                            "   Charset:        " + charset + "\n" +
                            "   Detected chars: " + new String(buffer, charset.name()));
                }
            }
            return charset;
        }
        catch (Throwable e)
        {
            logger.error("IOException while attempting to detect charset encoding.", e);
            return null;
        }
    }

    /**
     * Some implementations may only require a few bytes to do detect the stream type,
     * whilst others may be more efficient with larger buffers.  In either case, the
     * number of bytes actually present in the buffer cannot be enforced.
     * <p>
     * Only override this method if there is a very compelling reason to adjust the buffer
     * size, and then consider handling the {@link #setBufferSize(int)} method by issuing a
     * warning.  This will prevent users from setting the buffer size when it has no effect.
     * 
     * @return              Returns the maximum desired size of the buffer passed
     *                      to the {@link CharactersetFinder#detectCharset(byte[])} method.
     * 
     * @see #setBufferSize(int)
     */
    protected int getBufferSize()
    {
        return bufferSize;
    }
    
    /**
     * Worker method for implementations to override.  All exceptions will be reported and
     * absorbed and <tt>null</tt> returned.
     * <p>
     * The interface contract is that the data buffer must not be altered in any way.
     * 
     * @param buffer            the buffer of data no bigger than the requested
     *                          {@linkplain #getBufferSize() best buffer size}.  This can,
     *                          very efficiently, be turned into an <tt>InputStream</tt> using a
     *                          <tt>ByteArrayInputStream<tt>. 
     * @return                  Returns the charset or <tt>null</tt> if an accurate conclusion
     *                          is not possible
     * @throws Exception        Any exception, checked or not
     */
    protected abstract Charset detectCharsetImpl(byte[] buffer) throws Exception;
}
