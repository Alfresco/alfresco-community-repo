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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentAccessor;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.util.FileCopyUtils;

/**
 * Implements all the convenience methods of the interface.  The only methods
 * that need to be implemented, i.e. provide low-level content access are:
 * <ul>
 *   <li>{@link #getDirectReadableChannel()} to read content from the repository</li>
 * </ul>
 * 
 * @author Derek Hulley
 */
public abstract class AbstractContentReader extends AbstractContentAccessor implements ContentReader
{
    private static final Log logger = LogFactory.getLog(AbstractContentReader.class);
    
    private List<ContentStreamListener> listeners;
    private ReadableByteChannel channel;
    
    /**
     * @param contentUrl the content URL - this should be relative to the root of the store
     *      and not absolute: to enable moving of the stores
     */
    protected AbstractContentReader(String contentUrl)
    {
        super(contentUrl);
        
        listeners = new ArrayList<ContentStreamListener>(2);
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
     * implementation of the {@link ContentReader#getReader()} method.
     * <p>
     * Only the instance need be constructed.  The required mimetype, encoding, etc
     * will be copied across by this class.
     *  
     * @return Returns a reader onto the location referenced by this instance.
     *      The instance must <b>always</b> be a new instance.
     * @throws ContentIOException
     */
    protected abstract ContentReader createReader() throws ContentIOException;
    
    /**
     * Performs checks and copies required reader attributes
     */
    public final ContentReader getReader() throws ContentIOException
    {
        ContentReader reader = createReader();
        if (reader == null)
        {
            throw new AlfrescoRuntimeException("ContentReader failed to create new reader: \n" +
                    "   reader: " + this);
        }
        else if (reader.getContentUrl() == null || !reader.getContentUrl().equals(getContentUrl()))
        {
            throw new AlfrescoRuntimeException("ContentReader has different URL: \n" +
                    "   reader: " + this + "\n" +
                    "   new reader: " + reader);
        }
        // copy across common attributes
        reader.setMimetype(this.getMimetype());
        reader.setEncoding(this.getEncoding());
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Reader spawned new reader: \n" +
                    "   reader: " + this + "\n" +
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

    public synchronized boolean isChannelOpen()
    {
        if (channel != null)
        {
            return channel.isOpen();
        }
        else
        {
            return false;
        }
    }

    /**
     * Provides low-level access to read content from the repository.
     * <p>
     * This is the only of the content <i>reading</i> methods that needs to be implemented
     * by derived classes.  All other content access methods make use of this in their
     * underlying implementations.
     * 
     * @return Returns a channel from which content can be read
     * @throws ContentIOException if the channel could not be opened or the underlying content
     *      has disappeared
     */
    protected abstract ReadableByteChannel getDirectReadableChannel() throws ContentIOException;

    /**
     * Create a channel that performs callbacks to the given listeners.
     *  
     * @param directChannel the result of {@link #getDirectReadableChannel()}
     * @param listeners the listeners to call
     * @return Returns a channel
     * @throws ContentIOException
     */
    private ReadableByteChannel getCallbackReadableChannel(
            ReadableByteChannel directChannel,
            List<ContentStreamListener> listeners)
            throws ContentIOException
    {
        ReadableByteChannel callbackChannel = null;
        if (directChannel instanceof FileChannel)
        {
            callbackChannel = getCallbackFileChannel((FileChannel) directChannel, listeners);
        }
        else
        {
            // introduce an advistor to handle the callbacks to the listeners
            ChannelCloseCallbackAdvise advise = new ChannelCloseCallbackAdvise(listeners);
            ProxyFactory proxyFactory = new ProxyFactory(directChannel);
            proxyFactory.addAdvice(advise);
            callbackChannel = (ReadableByteChannel) proxyFactory.getProxy();
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Created callback byte channel: \n" +
                    "   original: " + directChannel + "\n" +
                    "   new: " + callbackChannel);
        }
        return callbackChannel;
    }

    /**
     * @see #getDirectReadableChannel()
     * @see #getCallbackReadableChannel(ReadableByteChannel, List)
     */
    public synchronized final ReadableByteChannel getReadableChannel() throws ContentIOException
    {
        // this is a use-once object
        if (channel != null)
        {
            throw new RuntimeException("A channel has already been opened");
        }
        ReadableByteChannel directChannel = getDirectReadableChannel();
        channel = getCallbackReadableChannel(directChannel, listeners);

        // notify that the channel was opened
        super.channelOpened();
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Opened channel onto content: " + this);
        }
        return channel;
    }
    
    
    /**
     * @inheritDoc
     */
    public FileChannel getFileChannel() throws ContentIOException
    {
        /*
         * Where the underlying support is not present for this method, a temporary
         * file will be used as a substitute.  When the write is complete, the
         * results are copied directly to the underlying channel.
         */
        
        // get the underlying implementation's best readable channel
        channel = getReadableChannel();
        // now use this channel if it can provide the random access, otherwise spoof it
        FileChannel clientFileChannel = null;
        if (channel instanceof FileChannel)
        {
            // all the support is provided by the underlying implementation
            clientFileChannel = (FileChannel) channel;
            // debug
            if (logger.isDebugEnabled())
            {
                logger.debug("Content reader provided direct support for FileChannel: \n" +
                        "   reader: " + this);
            }
        }
        else
        {
            // No random access support is provided by the implementation.
            // Spoof it by providing a 2-stage read from a temp file
            File tempFile = TempFileProvider.createTempFile("random_read_spoof_", ".bin");
            FileContentWriter spoofWriter = new FileContentWriter(tempFile);
            // pull the content in from the underlying channel
            FileChannel spoofWriterChannel = spoofWriter.getFileChannel(false);
            try
            {
                long spoofFileSize = this.getSize();
                spoofWriterChannel.transferFrom(channel, 0, spoofFileSize);
            }
            catch (IOException e)
            {
                throw new ContentIOException("Failed to copy from permanent channel to spoofed temporary channel: \n" +
                        "   reader: " + this + "\n" +
                        "   temp: " + spoofWriter,
                        e);
            }
            finally
            {
                try { spoofWriterChannel.close(); } catch (IOException e) {}
            }
            // get a reader onto the spoofed content
            final ContentReader spoofReader = spoofWriter.getReader();
            // Attach a listener
            // - ensure that the close call gets propogated to the underlying channel
            ContentStreamListener spoofListener = new ContentStreamListener()
                    {
                        public void contentStreamClosed() throws ContentIOException
                        {
                            try
                            {
                                channel.close();
                            }
                            catch (IOException e)
                            {
                                throw new ContentIOException("Failed to close underlying channel", e);
                            }
                        }
                    };
            spoofReader.addListener(spoofListener);
            // we now have the spoofed up channel that the client can work with
            clientFileChannel = spoofReader.getFileChannel();
            // debug
            if (logger.isDebugEnabled())
            {
                logger.debug("Content writer provided indirect support for FileChannel: \n" +
                        "   writer: " + this + "\n" +
                        "   temp writer: " + spoofWriter);
            }
        }
        // the file is now available for random access
        return clientFileChannel;
    }

    /**
     * @see Channels#newInputStream(java.nio.channels.ReadableByteChannel)
     */
    public InputStream getContentInputStream() throws ContentIOException
    {
        try
        {
            ReadableByteChannel channel = getReadableChannel();
            InputStream is = new BufferedInputStream(Channels.newInputStream(channel));
            // done
            return is;
        }
        catch (Throwable e)
        {
            throw new ContentIOException("Failed to open stream onto channel: \n" +
                    "   accessor: " + this,
                    e);
        }
    }

    /**
     * Copies the {@link #getContentInputStream() input stream} to the given
     * <code>OutputStream</code>
     */
    public final void getContent(OutputStream os) throws ContentIOException
    {
        try
        {
            InputStream is = getContentInputStream();
            FileCopyUtils.copy(is, os);  // both streams are closed
            // done
        }
        catch (IOException e)
        {
            throw new ContentIOException("Failed to copy content to output stream: \n" +
                    "   accessor: " + this,
                    e);
        }
    }

    public final void getContent(File file) throws ContentIOException
    {
        try
        {
            InputStream is = getContentInputStream();
            FileOutputStream os = new FileOutputStream(file);
            FileCopyUtils.copy(is, os);  // both streams are closed
            // done
        }
        catch (IOException e)
        {
            throw new ContentIOException("Failed to copy content to file: \n" +
                    "   accessor: " + this + "\n" +
                    "   file: " + file,
                    e);
        }
    }
    
    public final String getContentString(int length) throws ContentIOException
    {
        if (length < 0 || length > Integer.MAX_VALUE)
        {
            throw new IllegalArgumentException("Character count must be positive and within range");
        }
        Reader reader = null;
        try
        {
            // just create buffer of the required size
            char[] buffer = new char[length];
            
            String encoding = getEncoding();
            // create a reader from the input stream
            if (encoding == null)
            {
                reader = new InputStreamReader(getContentInputStream());
            }
            else
            {
                reader = new InputStreamReader(getContentInputStream(), encoding);
            }
            // read it all, if possible
            int count = reader.read(buffer, 0, length);
            // there may have been fewer characters - create a new string
            String result = new String(buffer, 0, count);
            // done
            return result;
        }
        catch (IOException e)
        {
            throw new ContentIOException("Failed to copy content to string: \n" +
                    "   accessor: " + this + "\n" +
                    "   length: " + length,
                    e);
        }
        finally
        {
            if (reader != null)
            {
                try { reader.close(); } catch (Throwable e) { logger.error(e); }
            }
        }
    }

    /**
     * Makes use of the encoding, if available, to convert bytes to a string.
     * <p>
     * All the content is streamed into memory.  So, like the interface said,
     * be careful with this method.
     * 
     * @see ContentAccessor#getEncoding()
     */
    public final String getContentString() throws ContentIOException
    {
        try
        {
            // read from the stream into a byte[]
            InputStream is = getContentInputStream();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            FileCopyUtils.copy(is, os);  // both streams are closed
            byte[] bytes = os.toByteArray();
            // get the encoding for the string
            String encoding = getEncoding();
            // create the string from the byte[] using encoding if necessary
            String content = (encoding == null) ? new String(bytes) : new String(bytes, encoding);
            // done
            return content;
        }
        catch (IOException e)
        {
            throw new ContentIOException("Failed to copy content to string: \n" +
                    "   accessor: " + this,
                    e);
        }
    }
}
