/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.content;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentAccessor;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.util.FileCopyUtils;

/**
 * Implements all the convenience methods of the interface.  The only methods
 * that need to be implemented, i.e. provide low-level content access are:
 * <ul>
 *   <li>{@link #getDirectWritableChannel()} to write content to the repository</li>
 * </ul>
 * 
 * @author Derek Hulley
 */
public abstract class AbstractContentWriter extends AbstractContentAccessor implements ContentWriter
{
    private static final Log logger = LogFactory.getLog(AbstractContentWriter.class);
    
    private List<ContentStreamListener> listeners;
    private WritableByteChannel channel;
    private ContentReader existingContentReader;
    
    /**
     * @param contentUrl the content URL
     * @param existingContentReader a reader of a previous version of this content
     */
    protected AbstractContentWriter(String contentUrl, ContentReader existingContentReader)
    {
        super(contentUrl);
        this.existingContentReader = existingContentReader;
        
        listeners = new ArrayList<ContentStreamListener>(2);
    }

    /**
     * @return Returns a reader onto the previous version of this content
     */
    protected ContentReader getExistingContentReader()
    {
        return existingContentReader;
    }

    /**
     * Adds the listener after checking that the output stream isn't already in
     * use.
     */
    public synchronized void addListener(ContentStreamListener listener)
    {
        if (channel != null)
        {
            throw new RuntimeException("Channel is already in use");
        }
        listeners.add(listener);
    }

    /**
     * A factory method for subclasses to implement that will ensure the proper
     * implementation of the {@link ContentWriter#getReader()} method.
     * <p>
     * Only the instance need be constructed.  The required mimetype, encoding, etc
     * will be copied across by this class.
     * <p>
     *  
     * @return Returns a reader onto the location referenced by this instance.
     *      The instance must <b>always</b> be a new instance and never null.
     * @throws ContentIOException
     */
    protected abstract ContentReader createReader() throws ContentIOException;
    
    /**
     * Performs checks and copies required reader attributes
     */
    public final ContentReader getReader() throws ContentIOException
    {
        if (!isClosed())
        {
            return null;
        }
        ContentReader reader = createReader();
        if (reader == null)
        {
            throw new AlfrescoRuntimeException("ContentReader failed to create new reader: \n" +
                    "   writer: " + this);
        }
        else if (reader.getContentUrl() == null || !reader.getContentUrl().equals(getContentUrl()))
        {
            throw new AlfrescoRuntimeException("ContentReader has different URL: \n" +
                    "   writer: " + this + "\n" +
                    "   new reader: " + reader);
        }
        // copy across common attributes
        reader.setMimetype(this.getMimetype());
        reader.setEncoding(this.getEncoding());
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Writer spawned new reader: \n" +
                    "   writer: " + this + "\n" +
                    "   new reader: " + reader);
        }
        return reader;
    }

    /**
     * An automatically created listener sets the flag
     */
    public synchronized final boolean isClosed()
    {
        if (channel != null)
        {
            return !channel.isOpen();
        }
        else
        {
            return false;
        }
    }

    /**
     * Provides low-level access to write content to the repository.
     * <p>
     * This is the only of the content <i>writing</i> methods that needs to be implemented
     * by derived classes.  All other content access methods make use of this in their
     * underlying implementations.
     * 
     * @return Returns a channel with which to write content
     * @throws ContentIOException if the channel could not be opened
     */
    protected abstract WritableByteChannel getDirectWritableChannel() throws ContentIOException;
    
    /**
     * Optionally override to supply an alternate callback channel.
     *
     * @param directChannel the result of {@link #getDirectWritableChannel()}
     * @param listeners the listeners to call
     * @return Returns a callback channel
     * @throws ContentIOException
     */
    protected WritableByteChannel getCallbackWritableChannel(
            WritableByteChannel directChannel,
            List<ContentStreamListener> listeners)
            throws ContentIOException
    {
        // proxy to add an advise
        ByteChannelCallbackAdvise advise = new ByteChannelCallbackAdvise(listeners);
        ProxyFactory proxyFactory = new ProxyFactory(directChannel);
        proxyFactory.addAdvice(advise);
        WritableByteChannel callbackChannel = (WritableByteChannel) proxyFactory.getProxy();
        // done
        return callbackChannel;
    }

    /**
     * @see #getDirectWritableChannel()
     * @see #getCallbackWritableChannel()
     */
    public synchronized final WritableByteChannel getWritableChannel() throws ContentIOException
    {
        // this is a use-once object
        if (channel != null)
        {
            throw new RuntimeException("A channel has already been opened");
        }
        WritableByteChannel directChannel = getDirectWritableChannel();
        channel = getCallbackWritableChannel(directChannel, listeners);

        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Opened channel onto content: \n" +
                    "   content: " + this + "\n" +
                    "   channel: " + channel);
        }
        return channel;
    }
    
    /**
     * @see Channels#newOutputStream(java.nio.channels.WritableByteChannel)
     */
    public OutputStream getContentOutputStream() throws ContentIOException
    {
        try
        {
            WritableByteChannel channel = getWritableChannel();
            OutputStream is = new BufferedOutputStream(Channels.newOutputStream(channel));
            // done
            return is;
        }
        catch (Throwable e)
        {
            throw new ContentIOException("Failed to open stream onto channel: \n" +
                    "   writer: " + this,
                    e);
        }
    }

    /**
     * @see ContentReader#getContentInputStream()
     * @see #putContent(InputStream) 
     */
    public void putContent(ContentReader reader) throws ContentIOException
    {
        try
        {
            // get the stream to read from
            InputStream is = reader.getContentInputStream();
            // put the content
            putContent(is);
        }
        catch (Throwable e)
        {
            throw new ContentIOException("Failed to copy reader content to writer: \n" +
                    "   writer: " + this + "\n" +
                    "   source reader: " + reader,
                    e);
        }
    }

    public final void putContent(InputStream is) throws ContentIOException
    {
        try
        {
            OutputStream os = getContentOutputStream();
            FileCopyUtils.copy(is, os);     // both streams are closed
            // done
        }
        catch (IOException e)
        {
            throw new ContentIOException("Failed to copy content from input stream: \n" +
                    "   writer: " + this,
                    e);
        }
    }
    
    public final void putContent(File file) throws ContentIOException
    {
        try
        {
            OutputStream os = getContentOutputStream();
            FileInputStream is = new FileInputStream(file);
            FileCopyUtils.copy(is, os);     // both streams are closed
            // done
        }
        catch (IOException e)
        {
            throw new ContentIOException("Failed to copy content from file: \n" +
                    "   writer: " + this + "\n" +
                    "   file: " + file,
                    e);
        }
    }
    
    /**
     * Makes use of the encoding, if available, to convert the string to bytes.
     * 
     * @see ContentAccessor#getEncoding()
     */
    public final void putContent(String content) throws ContentIOException
    {
        try
        {
            // attempt to use the correct encoding
            String encoding = getEncoding();
            byte[] bytes = (encoding == null) ? content.getBytes() : content.getBytes(encoding);
            // get the stream
            OutputStream os = getContentOutputStream();
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            FileCopyUtils.copy(is, os);     // both streams are closed
            // done
        }
        catch (IOException e)
        {
            throw new ContentIOException("Failed to copy content from string: \n" +
                    "   writer: " + this +
                    "   content length: " + content.length(),
                    e);
        }
    }
}
