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

import org.alfresco.filesys.server.filesys.FileAttribute;
import org.alfresco.filesys.server.filesys.FileInfo;

/**
 * Pseudo File Class
 * 
 * <p>Creates a pseudo file entry for a folder that maps to a file outside of the usual file area but appears
 * in folder listings for the owner folder.
 * 
 * @author gkspencer
 */
public class PseudoFile
{
    // Dummy creation date/time to use for pseudo files
    
    private static long _creationDateTime = System.currentTimeMillis();
    
    // File name for pseudo file
    
    private String m_fileName;

    // File flags/attributes
    
    private int m_fileFlags = FileAttribute.ReadOnly;
    
    // Path to the file data in the local filesystem
    
    private String m_filePath;
    
    // File information, used for file information/folder searches
    
    private FileInfo m_fileInfo;
    
    /**
     * Class constructor
     * 
     * @param name String
     * @param path String
     */
    public PseudoFile(String name, String path)
    {
        m_fileName = name;
        m_filePath = path;
    }
    
    /**
     * Class constructor
     * 
     * @param name String
     * @param path String
     * @param flags int
     */
    public PseudoFile(String name, String path, int flags)
    {
        m_fileName = name;
        m_filePath = path;
        
        m_fileFlags = flags;
    }
    
    /**
     * Return the pseudo file name as it will appear in folder listings
     * 
     * @return String
     */
    public final String getFileName()
    {
        return m_fileName;
    }
    
    /**
     * Return the path to the file data on the local filesystem
     * 
     * @return String
     */
    public final String getFilePath()
    {
        return m_filePath;
    }
    
    /**
     * Return the standard file attributes
     * 
     * @return int
     */
    public final int getAttributes()
    {
        return m_fileFlags;
    }
    
    /**
     * Return the file information for the pseudo file
     *
     * @return FileInfo
     */
    public final FileInfo getFileInfo()
    {
        // Check if the file information is valid
        
        if ( m_fileInfo == null) {
            
            // Get the file details
            
            File localFile = new File( getFilePath());
            if ( localFile.exists())
            {
                // Create the file information
                
                m_fileInfo = new FileInfo( getFileName(), localFile.length(), getAttributes());
                
                // Set the file creation/modification times
                
                m_fileInfo.setModifyDateTime( localFile.lastModified());
                m_fileInfo.setCreationDateTime( _creationDateTime);
                m_fileInfo.setChangeDateTime( _creationDateTime);

                // Set the allocation size, round up the actual length
                
                m_fileInfo.setAllocationSize(( localFile.length() + 512L) & 0xFFFFFFFFFFFFFE00L);
            }            
        }
        
        // Return the file information
        
        return m_fileInfo;
    }
    
    /**
     * Return the pseudo file as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("[");
        str.append(getFileName());
        str.append(",");
        str.append(getFilePath());
        str.append(":");
        
        if ( m_fileInfo != null)
            str.append( m_fileInfo.toString());
        else
            str.append("Null");
        str.append("]");
        
        return str.toString();
    }
}
