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
package org.alfresco.service.cmr.repository;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;

/**
 * Represents a handle to read specific content.  Content may only be accessed
 * once per instance.
 * <p>
 * Implementations of this interface <b>might</b> be <code>Serializable</code>
 * but client code could should check suitability before attempting to serialize
 * it.
 * <p>
 * Implementations that are able to provide inter-VM streaming, such as accessing
 * WebDAV, would be <code>Serializable</code>.  An accessor that has to access a
 * local file on the server could not provide inter-VM streaming unless it specifically
 * makes remote calls and opens sockets, etc.
 * 
 * @see org.alfresco.service.cmr.repository.ContentWriter
 * 
 * @author Derek Hulley
 */
public interface ContentReader extends ContentAccessor
{
    /**
     * Convenience method to get another reader onto the underlying content.
     * 
     * @return Returns a reader onto the underlying content
     * @throws ContentIOException
     */
    public ContentReader getReader() throws ContentIOException;
    
    /**
     * Check if the {@link ContentAccessor#getContentUrl() underlying content} is present.
     * 
     * @return Returns true if there is content at the URL refered to by this reader
     */
    public boolean exists();
    
    /**
     * Gets the time of the last modification of the underlying content.
     * 
     * @return Returns the last modification time using the standard <tt>long</tt>
     *      time, or <code>0L</code> if the content doesn't {@link #exists() exist}.
     *      
     * @see System#currentTimeMillis()
     */
    public long getLastModified();

    /**
     * Convenience method to find out if this reader has been closed.
     * Once closed, the content can no longer be read.  This method could
     * be used to wait for a particular read operation to complete, for example.
     * 
     * @return Return true if the content input stream has been used and closed
     *      otherwise false.
     */
    public boolean isClosed();
    
    /**
     * Provides low-level access to the underlying content.
     * <p>
     * Once the stream is provided to a client it should remain active
     * (subject to any timeouts) until closed by the client.
     * 
     * @return Returns a stream that can be read at will, but must be closed when completed
     * @throws ContentIOException
     */
    public ReadableByteChannel getReadableChannel() throws ContentIOException;
    
    /**
     * Get a stream to read from the underlying channel
     * 
     * @return Returns an input stream onto the underlying channel
     * @throws ContentIOException
     * 
     * @see #getReadableChannel()
     */
    public InputStream getContentInputStream() throws ContentIOException;

    /**
     * Gets content from the repository.
     * <p>
     * All resources will be closed automatically.
     * <p>
     * Care must be taken that the bytes read from the stream are properly
     * decoded according to the {@link ContentAccessor#getEncoding() encoding}
     * property.
     * 
     * @param os the stream to which to write the content
     * @throws ContentIOException
     * 
     * @see #getReadableChannel()
     */
    public void getContent(OutputStream os) throws ContentIOException;
    
    /**
     * Gets content from the repository direct to file
     * <p>
     * All resources will be closed automatically.
     * 
     * @param file the file to write the content to - it will be overwritten
     * @throws ContentIOException
     * 
     * @see #getContentInputStream()
     */
    public void getContent(File file) throws ContentIOException;

    /**
     * Gets content from the repository direct to <code>String</code>.
     * <p>
     * If the {@link ContentAccessor#getEncoding() encoding } is known then it will be used
     * otherwise the default system <tt>byte[]</tt> to <tt>String</tt> conversion
     * will be used.
     * <p>
     * All resources will be closed automatically.
     * <p>
     * <b>WARNING: </b> This should only be used when the size of the content
     *                  is known in advance.
     * 
     * @return Returns a String representation of the content
     * @throws ContentIOException
     * 
     * @see #getContentString(int)
     * @see #getContentInputStream()
     * @see String#String(byte[])
     */
    public String getContentString() throws ContentIOException;
    
    /**
     * Gets content from the repository direct to <code>String</code>, but limiting
     * the string size to a given number of characters.
     * <p>
     * If the {@link ContentAccessor#getEncoding() encoding } is known then it will be used
     * otherwise the default system <tt>byte[]</tt> to <tt>String</tt> conversion
     * will be used.
     * <p>
     * All resources will be closed automatically.
     *
     * @param length the maximum number of characters to retrieve
     * @return Returns a truncated String representation of the content
     * @throws ContentIOException
     * @throws java.lang.IllegalArgumentException if the length is < 0 or > {@link Integer#MAX_VALUE}
     * 
     * @see #getContentString()
     * @see #getContentInputStream()
     * @see String#String(byte[])
     */
    public String getContentString(int length) throws ContentIOException;
}
