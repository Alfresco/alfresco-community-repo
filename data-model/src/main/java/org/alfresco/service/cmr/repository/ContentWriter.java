/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.repository;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Represents a handle to write specific content. Content may only be accessed once per instance.
 * <p>
 * Implementations of this interface <b>might</b> be <code>Serializable</code> but client code could should check suitability before attempting to serialize it.
 * <p>
 * Implementations that are able to provide inter-VM streaming, such as accessing WebDAV, would be <code>Serializable</code>. An accessor that has to access a local file on the server could not provide inter-VM streaming unless it specifically makes remote calls and opens sockets, etc.
 * 
 * @see org.alfresco.service.cmr.repository.ContentReader
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public interface ContentWriter extends ContentAccessor
{
    /**
     * Convenience method to get a reader onto newly written content. This method will return null if the content has not yet been written by the writer or if the output stream is still open.
     * 
     * @return Returns a reader onto the underlying content that this writer will or has written to
     * @throws ContentIOException
     */
    public ContentReader getReader() throws ContentIOException;

    /**
     * Convenience method to find out if this writer has been closed. Once closed, the content can no longer be written to and it become possible to get readers onto the written content.
     * 
     * @return Return true if the content output stream has been used and closed otherwise false.
     */
    public boolean isClosed();

    /**
     * Provides low-level access to write to repository content.
     * <p>
     * The channel returned to the client should remain open (subject to timeouts) until closed by the client. All lock detection, read-only access and other concurrency issues are dealt with during this operation. It remains possible that implementations will throw exceptions when the channel is closed.
     * <p>
     * The stream will notify any listeners according to the listener interface.
     * 
     * @return Returns a channel with which to write content
     * @throws ContentIOException
     */
    public WritableByteChannel getWritableChannel() throws ContentIOException;

    /**
     * Provides read-write, random-access to the underlying content. In general, this method should be considered more expensive than the sequential-access method, {@link #getWritableChannel()}.
     * <p>
     * Underlying implementations use the <code>truncate</code> parameter to determine the most effective means of providing access to the content.
     * 
     * @param truncate
     *            true to start with zero length content
     * @return Returns a random-access channel onto the content
     * @throws ContentIOException
     *
     * @see #getWritableChannel()
     * @see java.io.RandomAccessFile#getChannel()
     */
    public FileChannel getFileChannel(boolean truncate) throws ContentIOException;

    /**
     * Get a stream to write to the underlying channel.
     * 
     * @return Returns an output stream onto the underlying channel
     * @throws ContentIOException
     * 
     * @see #getWritableChannel()
     */
    public OutputStream getContentOutputStream() throws ContentIOException;

    /**
     * Copies content from the reader.
     * <p>
     * All resources will be closed automatically.
     * 
     * @param reader
     *            the reader acting as the source of the content
     * @throws ContentIOException
     * 
     * @see #getWritableChannel()
     */
    public void putContent(ContentReader reader) throws ContentIOException;

    /**
     * Puts content to the repository
     * <p>
     * All resources will be closed automatically.
     * 
     * @param is
     *            the input stream from which the content will be read
     * @throws ContentIOException
     * 
     * @see #getWritableChannel()
     */
    public void putContent(InputStream is) throws ContentIOException;

    /**
     * Puts content to the repository direct from file
     * <p>
     * All resources will be closed automatically.
     * 
     * @param file
     *            the file to load the content from
     * @throws ContentIOException
     * 
     * @see #getWritableChannel()
     */
    public void putContent(File file) throws ContentIOException;

    /**
     * Puts content to the repository direct from <code>String</code>.
     * <p>
     * If the {@link ContentAccessor#getEncoding() encoding } is known then it will be used otherwise the default system <tt>String</tt> to <tt>byte[]</tt> conversion will be used.
     * <p>
     * All resources will be closed automatically.
     * 
     * @param content
     *            a string representation of the content
     * @throws ContentIOException
     * 
     * @see #getWritableChannel()
     * @see String#getBytes(java.lang.String)
     */
    public void putContent(String content) throws ContentIOException;

    /**
     * Attempts to guess the mimetype of the Content based on the contents and the filename.
     * <p>
     * If the content has already been written, then the mimetype guessing will occur immediately. If the content has yet to be written, then the guessing will occur once the content write has completed.
     * 
     * @param filename
     *            The filename of the content (if known)
     */
    public void guessMimetype(String filename);

    /**
     * Attempts to guess the encoding of the Content based on the contents.
     * <p>
     * If the content has already been written, then the encoding guessing will occur immediately. If the content has yet to be written, then the guessing will occur once the content write has completed.
     */
    public void guessEncoding();
}
