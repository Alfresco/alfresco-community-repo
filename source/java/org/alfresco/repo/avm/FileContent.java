/*
 * Copyright (C) 2006 Alfresco, Inc.
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
package org.alfresco.repo.avm;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Interface for file content.  FileContent can be shared between files.
 * @author britt
 */
public interface FileContent
{
    /**
     * Get the number of files that refer to this content.
     * @return The reference count.
     */
    public int getRefCount();

    /**
     * Set the reference count.
     * @param count The count to set.
     */
    public void setRefCount(int count);

    /**
     * Get an input stream from the content.
     * @return An InputStream.
     */
    public InputStream getInputStream();

    /**
     * Get an output stream to the content.
     * @return an OutputStream.
     */
    public OutputStream getOutputStream();
    
    /**
     * Get a random access file to this content.
     * @param access The mode to open the file in.
     * @return A RandomAccessFile.
     */
    public RandomAccessFile getRandomAccess(String access);
    
    /**
     * Delete the contents of this from the backing store.
     */
    public void delete();
    
    /**
     * Get the length of the file.
     * @return The length of the file.
     */
    public long getLength();
    
    /**
     * Get the object id.
     * @return object id.
     */
    public long getId();
}