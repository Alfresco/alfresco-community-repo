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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.alfresco.repo.content.AbstractContentWriter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides direct access to a local file.
 * <p>
 * This class does not provide remote access to the file.
 * 
 * @author Derek Hulley
 */
public class FileContentWriter extends AbstractContentWriter
{
    private static final Log logger = LogFactory.getLog(FileContentWriter.class);
    
    private File file;
    private boolean allowRandomAccess;
    
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
        allowRandomAccess = true;
    }
    
    /* package */ void setAllowRandomAccess(boolean allow)
    {
        this.allowRandomAccess = allow;
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
        FileContentReader reader = new FileContentReader(this.file, getContentUrl());
        reader.setAllowRandomAccess(this.allowRandomAccess);
        return reader;
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
            WritableByteChannel channel = null;
            if (allowRandomAccess)
            {
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");  // will create it
                channel = randomAccessFile.getChannel();
            }
            else
            {
                OutputStream os = new FileOutputStream(file);
                channel = Channels.newChannel(os);
            }
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Opened write channel to file: \n" +
                        "   file: " + file + "\n" +
                        "   random-access: " + allowRandomAccess);
            }
            return channel;
        }
        catch (Throwable e)
        {
            throw new ContentIOException("Failed to open file channel: " + this, e);
        }
    }

    /**
     * @return Returns true always
     */
    public boolean canWrite()
    {
        return true;    // this is a writer
    }
}
