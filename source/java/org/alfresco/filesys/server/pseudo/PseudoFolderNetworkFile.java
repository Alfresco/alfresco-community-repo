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

package org.alfresco.filesys.server.pseudo;

import java.io.IOException;

import org.alfresco.filesys.server.filesys.AccessDeniedException;
import org.alfresco.filesys.server.filesys.FileAttribute;
import org.alfresco.filesys.server.filesys.NetworkFile;

/**
 * Pseudo Folder Network File Class
 * 
 * <p>Represents an open pseudo folder.
 *
 * @author gkspencer
 */
public class PseudoFolderNetworkFile extends NetworkFile {

    /**
     * Class constructor.
     * 
     * @param name String
     */
    public PseudoFolderNetworkFile(String name)
    {
        super( name);
        
        setAttributes( FileAttribute.Directory);
    }

    /**
     * Class constructor.
     * 
     * @param name String
     * @param relPath String
     */
    public PseudoFolderNetworkFile(String name, String relPath)
    {
        super( name);
        
        setFullName( relPath);
        setAttributes( FileAttribute.Directory);
    }

    /**
     * Close the network file.
     */
    public void closeFile() throws java.io.IOException
    {
        // Nothing to do
    }

    /**
     * Return the current file position.
     * 
     * @return long
     */
    public long currentPosition()
    {
        return 0L;
    }

    /**
     * Flush the file.
     * 
     * @exception IOException
     */
    public void flushFile() throws IOException
    {
        // Nothing to do
    }

    /**
     * Determine if the end of file has been reached.
     * 
     * @return boolean
     */
    public boolean isEndOfFile() throws java.io.IOException
    {
        return true;
    }

    /**
     * Open the file.
     * 
     * @param createFlag boolean
     * @exception IOException
     */
    public void openFile(boolean createFlag) throws java.io.IOException
    {
    }

    /**
     * Read from the file.
     * 
     * @param buf byte[]
     * @param len int
     * @param pos int
     * @param fileOff long
     * @return Length of data read.
     * @exception IOException
     */
    public int readFile(byte[] buf, int len, int pos, long fileOff) throws java.io.IOException
    {
    	throw new AccessDeniedException( "Attempt to read/write folder file");
    }

    /**
     * Seek to the specified file position.
     * 
     * @param pos long
     * @param typ int
     * @return long
     * @exception IOException
     */
    public long seekFile(long pos, int typ) throws IOException
    {
    	throw new AccessDeniedException( "Attempt to read/write folder file");
    }

    /**
     * Truncate the file
     * 
     * @param siz long
     * @exception IOException
     */
    public void truncateFile(long siz) throws IOException
    {
    	throw new AccessDeniedException( "Attempt to read/write folder file");
    }

    /**
     * Write a block of data to the file.
     * 
     * @param buf byte[]
     * @param len int
     * @exception IOException
     */
    public void writeFile(byte[] buf, int len, int pos) throws java.io.IOException
    {
    	throw new AccessDeniedException( "Attempt to read/write folder file");
    }

    /**
     * Write a block of data to the file.
     * 
     * @param buf byte[]
     * @param len int
     * @param pos int
     * @param offset long
     * @exception IOException
     */
    public void writeFile(byte[] buf, int len, int pos, long offset) throws java.io.IOException
    {
    	throw new AccessDeniedException( "Attempt to read/write folder file");
    }
}
