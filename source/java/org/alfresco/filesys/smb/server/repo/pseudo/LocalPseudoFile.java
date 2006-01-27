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

import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.filesys.server.filesys.NetworkFile;

/**
 * Local Pseudo File Class
 * 
 *  <p>Pseudo file class that uses a file on the local filesystem.
 *  
 * @author gkspencer
 */
public class LocalPseudoFile extends PseudoFile
{
    // Path to the file on the local filesystem
    
    private String m_path;
    
    /**
     * Class constructor
     * 
     * @param name String
     * @param path String
     */
    public LocalPseudoFile(String name, String path)
    {
        super(name);
        
        m_path = path;
    }
    
    /**
     * Return the path to the file on the local filesystem
     * 
     * @return String
     */
    public final String getFilePath()
    {
        return m_path;
    }
    
    /**
     * Return the file information for the pseudo file
     *
     * @return FileInfo
     */
    public FileInfo getFileInfo()
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
     * Return a network file for reading/writing the pseudo file
     * 
     * @param netPath String
     * @return NetworkFile
     */
    public NetworkFile getFile(String netPath)
    {
        // Create a pseudo file mapped to a file in the local filesystem
        
        return new PseudoNetworkFile( getFileName(), getFilePath(), netPath);
    }
}
