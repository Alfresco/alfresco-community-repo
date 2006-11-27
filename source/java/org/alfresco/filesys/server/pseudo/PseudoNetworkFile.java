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

package org.alfresco.filesys.server.pseudo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.smb.SeekType;

/**
 * Pseudo File Network File Class
 * 
 * <p>Represents an open pseudo file and provides access to the file data.
 * 
 * @author gkspencer
 */
public class PseudoNetworkFile extends NetworkFile
{
    // File details

    protected File m_file;

    // Random access file used to read/write the actual file

    protected RandomAccessFile m_io;

    // End of file flag

    protected boolean m_eof;

    /**
     * Class constructor.
     * 
     * @param name String
     * @param localPath String
     * @param netPath String
     */
    public PseudoNetworkFile(String name, String localPath, String netPath)
    {
        super( name);

        // Set the file using the existing file object

        m_file = new File( localPath);

        // Set the file size

        setFileSize(m_file.length());
        m_eof = false;

        // Set the modification date/time, if available. Fake the creation date/time as it's not
        // available from the File class

        long modDate = m_file.lastModified();
        setModifyDate(modDate);
        setCreationDate(modDate);

        // Set the file id

        setFileId(netPath.hashCode());
        
        // Set the full relative path
        
        setFullName( netPath);
    }

    /**
     * Close the network file.
     */
    public void closeFile() throws java.io.IOException
    {

        // Close the file, if used

        if (m_io != null)
        {

            // Close the file

            m_io.close();
            m_io = null;

            // Set the last modified date/time for the file

            if (this.getWriteCount() > 0)
                m_file.setLastModified(System.currentTimeMillis());

            // Indicate that the file is closed

            setClosed(true);
        }
    }

    /**
     * Return the current file position.
     * 
     * @return long
     */
    public long currentPosition()
    {

        // Check if the file is open

        try
        {
            if (m_io != null)
                return m_io.getFilePointer();
        }
        catch (Exception ex)
        {
        }
        return 0;
    }

    /**
     * Flush the file.
     * 
     * @exception IOException
     */
    public void flushFile() throws IOException
    {
        // Flush all buffered data

        if (m_io != null)
            m_io.getFD().sync();
    }

    /**
     * Determine if the end of file has been reached.
     * 
     * @return boolean
     */
    public boolean isEndOfFile() throws java.io.IOException
    {
        // Check if we reached end of file

        if (m_io != null && m_io.getFilePointer() == m_io.length())
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

        synchronized (m_file)
        {
            // Check if the file is open

            if (m_io == null)
            {

                // Open the file, always read-only for now

                m_io = new RandomAccessFile(m_file, "r");

                // Indicate that the file is open

                setClosed(false);
            }
        }
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

        // Open the file, if not already open

        if (m_io == null)
            openFile(false);

        // Seek to the required file position

        if (currentPosition() != fileOff)
            seekFile(fileOff, SeekType.StartOfFile);

        // Read from the file

        int rdlen = m_io.read(buf, pos, len);

        // Return the actual length of data read

        return rdlen;
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

        // Open the file, if not already open

        if (m_io == null)
            openFile(false);

        // Check if the current file position is the required file position

        switch (typ)
        {

        // From start of file

        case SeekType.StartOfFile:
            if (currentPosition() != pos)
                m_io.seek(pos);
            break;

        // From current position

        case SeekType.CurrentPos:
            m_io.seek(currentPosition() + pos);
            break;

        // From end of file

        case SeekType.EndOfFile: {
            long newPos = m_io.length() + pos;
            m_io.seek(newPos);
        }
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
        // Allow the truncate, just do not do anything
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
        // Allow the write, just do not do anything
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
        // Allow the write, just do not do anything
    }
}
