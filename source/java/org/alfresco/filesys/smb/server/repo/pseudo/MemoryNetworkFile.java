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

package org.alfresco.filesys.smb.server.repo.pseudo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.alfresco.filesys.server.filesys.AccessDeniedException;
import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.smb.SeekType;

/**
 * In Memory Network File Class
 * 
 * <p>In memory network file implementation that uses a memory buffer for the file data.
 * 
 * @author gkspencer
 */
public class MemoryNetworkFile extends NetworkFile
{
    // Current file position
    
    private long m_filePos;

    // File data
    
    private byte[] m_data;
    
    // End of file flag

    private boolean m_eof;

    /**
     * Class constructor.
     * 
     * @param name String
     * @param localPath String
     * @param finfo FileInfo
     */
    public MemoryNetworkFile(String name, byte[] data, FileInfo finfo)
    {
        super( name);

        // Set the file data
        
        m_data = data;
        if ( m_data == null)
            m_data = new byte[0];
        
        // Set the file size

        setFileSize( m_data.length);
        m_eof = false;

        // Set the creation and modification date/times

        setModifyDate( finfo.getModifyDateTime());
        setCreationDate( finfo.getCreationDateTime());

        // Set the file id and relative path

        if ( finfo.getPath() != null)
        {
            setFileId( finfo.getPath().hashCode());
            setFullName( finfo.getPath());
        }
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
        return m_filePos;
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
        // Check if we reached end of file

        if ( m_filePos == m_data.length)
            return true;
        return false;
    }

    /**
     * Open the file.
     * 
     * @param createFlag boolean
     * @exception IOException
     */
    public void openFile(boolean createFlag) throws java.io.IOException
    {
        // Indicate that the file is open

        setClosed(false);
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
        // Check if the read is within the file data range

        long fileLen = (long) m_data.length;
        
        if ( fileOff >= fileLen)
            return 0;
        
        // Calculate the actual read length

        if (( fileOff + len) > fileLen)
            len = (int) ( fileLen - fileOff);

        // Copy the data to the user buffer
        
        System.arraycopy( m_data, (int) fileOff, buf, pos, len);

        // Update the current file position
        
        m_filePos = fileOff + len;
        
        // Return the actual length of data read

        return len;
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
        // Seek to the required file position

        switch (typ)
        {
        // From start of file

        case SeekType.StartOfFile:
            if (currentPosition() != pos)
                m_filePos = pos;
            break;

        // From current position

        case SeekType.CurrentPos:
            m_filePos += pos;
            break;

        // From end of file

        case SeekType.EndOfFile:
            m_filePos += pos;
            if ( m_filePos < 0)
                m_filePos = 0L;
            break;
        }

        // Return the new file position

        return currentPosition();
    }

    /**
     * Truncate the file
     * 
     * @param siz long
     * @exception IOException
     */
    public void truncateFile(long siz) throws IOException
    {
        // Do not allow the file to be written to
        
        throw new AccessDeniedException("Cannot truncate pseudo file");
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
        // Do not allow the file to be written to
        
        throw new AccessDeniedException("Cannot write to pseudo file");
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
        // Do not allow the file to be written to
        
        throw new AccessDeniedException("Cannot write to pseudo file");
    }
}
