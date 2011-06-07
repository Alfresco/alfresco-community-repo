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
package org.alfresco.repo.content.filestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.MessageFormat;

import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides direct access to a local file.
 * <p>
 * This class does not provide remote access to the file.
 * 
 * @author Derek Hulley
 */
public class FileContentReader extends AbstractContentReader
    implements org.alfresco.service.cmr.repository.FileContentReader
{
    /**
     * message key for missing content.  Parameters are
     * <ul>
     *    <li>{@link org.alfresco.service.cmr.repository.NodeRef NodeRef}</li>
     *    <li>{@link ContentReader ContentReader}</li>
     * </ul>
     */
    public static final String MSG_MISSING_CONTENT = "content.content_missing";
    
    private static final Log logger = LogFactory.getLog(FileContentReader.class);
    
    private File file;
    private boolean allowRandomAccess;
    
    /**
     * Checks the existing reader provided and replaces it with a reader onto some
     * fake content if required.  If the existing reader is invalid, an debug message
     * will be logged under this classname category.
     * <p>
     * It is a convenience method that clients can use to cheaply get a reader that
     * is valid, regardless of whether the initial reader is valid.
     * 
     * @param existingReader a potentially invalid reader or null
     * @param msgTemplate the template message that will used to format the final <i>fake</i> content
     * @param args arguments to put into the <i>fake</i> content
     * @return Returns a the existing reader or a new reader onto some generated text content
     */
    public static ContentReader getSafeContentReader(ContentReader existingReader, String msgTemplate, Object ... args)
    {
        ContentReader reader = existingReader;
        if (existingReader == null || !existingReader.exists())
        {
            // the content was never written to the node or the underlying content is missing
            String fakeContent = MessageFormat.format(msgTemplate, args);
            
            // log it
            if (logger.isDebugEnabled())
            {
                logger.debug(fakeContent);
            }
            
            // fake the content
            File tempFile = TempFileProvider.createTempFile("getSafeContentReader_", ".txt");
            ContentWriter writer = new FileContentWriter(tempFile);
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding("UTF-8");
            writer.putContent(fakeContent);
            // grab the reader from the temp writer
            reader = writer.getReader();
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Created safe content reader: \n" +
                    "   existing reader: " + existingReader + "\n" +
                    "   safe reader: " + reader);
        }
        return reader;
    }
    
    /**
     * Constructor that builds a URL based on the absolute path of the file.
     * 
     * @param file the file for reading.  This will most likely be directly
     *      related to the content URL.
     */
    public FileContentReader(File file)
    {
        this(
                file,
                FileContentStore.STORE_PROTOCOL + ContentStore.PROTOCOL_DELIMITER + file.getAbsolutePath());
    }
    
    /**
     * Constructor that explicitely sets the URL that the reader represents.
     * 
     * @param file the file for reading.  This will most likely be directly
     *      related to the content URL.
     * @param url the relative url that the reader represents
     */
    public FileContentReader(File file, String url)
    {
        super(url);
        
        this.file = file;
        allowRandomAccess = true;
    }
    
    /* package */ void setAllowRandomAccess(boolean allow)
    {
        this.allowRandomAccess = allow;
    }
    
    /**
     * @return Returns the file that this reader accesses
     */
    public File getFile()
    {
        return file;
    }

    /**
     * @return Whether the file exists or not
     */
    public boolean exists()
    {
        return file.exists();
    }

    /**
     * @see File#length()
     */
    public long getSize()
    {
        if (!exists())
        {
            return 0L;
        }
        else
        {
            return file.length();
        }
    }
    
    /**
     * @see File#lastModified()
     */
    public long getLastModified()
    {
        if (!exists())
        {
            return 0L;
        }
        else
        {
            return file.lastModified();
        }
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
    protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException
    {
        try
        {
            // the file must exist
            if (!file.exists())
            {
                throw new IOException("File does not exist: " + file);
            }
            // create the channel
            ReadableByteChannel channel = null;
            if (allowRandomAccess)
            {
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");  // won't create it
                channel = randomAccessFile.getChannel();
            }
            else
            {
                InputStream is = new FileInputStream(file);
                channel = Channels.newChannel(is);
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
     * @return Returns false as this is a reader
     */
    public boolean canWrite()
    {
        return false;   // we only allow reading
    }
}
