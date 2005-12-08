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
package org.alfresco.repo.content.filestore;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.AbstractContentWriter;
import org.alfresco.repo.content.RandomAccessContent;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides direct access to a local file.
 * <p>
 * This class does not provide remote access to the file.
 * 
 * @author Derek Hulley
 */
public class FileContentWriter extends AbstractContentWriter implements RandomAccessContent
{
    private static final Log logger = LogFactory.getLog(FileContentWriter.class);
    
    private File file;
    
    /**
     * Constructor that builds a URL based on the absolute path of the file.
     * 
     * @param file the file for writing.  This will most likely be directly
     *      related to the content URL.
     */
    public FileContentWriter(File file)
    {
        this(
                file,
                FileContentStore.STORE_PROTOCOL + file.getAbsolutePath(),
                null);
    }
    
    /**
     * Constructor that builds a URL based on the absolute path of the file.
     * 
     * @param file the file for writing.  This will most likely be directly
     *      related to the content URL.
     * @param existingContentReader a reader of a previous version of this content
     */
    public FileContentWriter(File file, ContentReader existingContentReader)
    {
        this(
                file,
                FileContentStore.STORE_PROTOCOL + file.getAbsolutePath(),
                existingContentReader);
    }
    
    /**
     * Constructor that explicitely sets the URL that the reader represents.
     * 
     * @param file the file for writing.  This will most likely be directly
     *      related to the content URL.
     * @param url the relative url that the reader represents
     * @param existingContentReader a reader of a previous version of this content
     */
    public FileContentWriter(File file, String url, ContentReader existingContentReader)
    {
        super(url, existingContentReader);
        
        this.file = file;
    }

    /**
     * @return Returns the file that this writer accesses
     */
    public File getFile()
    {
        return file;
    }

    /**
     * @return Returns the size of the underlying file or 
     */
    public long getSize()
    {
        if (file == null)
            return 0L;
        else if (!file.exists())
            return 0L;
        else
            return file.length();
    }

    /**
     * The URL of the write is known from the start and this method contract states
     * that no consideration needs to be taken w.r.t. the stream state.
     */
    @Override
    protected ContentReader createReader() throws ContentIOException
    {
        return new FileContentReader(this.file, getContentUrl());
    }
    
    @Override
    protected WritableByteChannel getDirectWritableChannel() throws ContentIOException
    {
        try
        {
            // we may not write to an existing file - EVER!!
            if (file.exists() && file.length() > 0)
            {
                throw new IOException("File exists - overwriting not allowed");
            }
            // create the channel
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");  // will create it
            FileChannel channel = randomAccessFile.getChannel();
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Opened channel to file: " + file);
            }
            return channel;
        }
        catch (Throwable e)
        {
            throw new ContentIOException("Failed to open file channel: " + this, e);
        }
    }

    /**
     * @param directChannel a file channel
     */
    @Override
    protected WritableByteChannel getCallbackWritableChannel(
            WritableByteChannel directChannel,
            List<ContentStreamListener> listeners) throws ContentIOException
    {
        if (!(directChannel instanceof FileChannel))
        {
            throw new AlfrescoRuntimeException("Expected write channel to be a file channel");
        }
        FileChannel fileChannel = (FileChannel) directChannel;
        // wrap it
        FileChannel callbackChannel = new CallbackFileChannel(fileChannel, listeners);
        // done
        return callbackChannel;
    }

    /**
     * @return Returns true always
     */
    public boolean canWrite()
    {
        return true;    // this is a writer
    }

    public FileChannel getChannel() throws ContentIOException
    {
        /*
         * By calling this method, clients indicate that they wish to make random
         * changes to the file.  It is possible that the client might only want
         * to update a tiny proportion of the file - or even none of it.  Either
         * way, the file must be as whole and complete as before it was accessed.
         */
        
        // go through the super classes to ensure that all concurrency conditions
        // and listeners are satisfied
        FileChannel channel = (FileChannel) super.getWritableChannel();
        // random access means that the the new content's starting point must be
        // that of the existing content
        ContentReader existingContentReader = getExistingContentReader();
        if (existingContentReader != null)
        {
            ReadableByteChannel existingContentChannel = existingContentReader.getReadableChannel();
            long existingContentLength = existingContentReader.getSize();
            // copy the existing content
            try
            {
                channel.transferFrom(existingContentChannel, 0, existingContentLength);
                // copy complete
                if (logger.isDebugEnabled())
                {
                    logger.debug("Copied content for random access: \n" +
                            "   writer: " + this + "\n" +
                            "   existing: " + existingContentReader);
                }
            }
            catch (IOException e)
            {
                throw new ContentIOException("Failed to copy from existing content to enable random access: \n" +
                        "   writer: " + this + "\n" +
                        "   existing: " + existingContentReader,
                        e);
            }
            finally
            {
                try { existingContentChannel.close(); } catch (IOException e) {}
            }
        }
        // the file is now available for random access
        return channel;
    }
}
