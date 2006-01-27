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

import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.filesys.server.filesys.NetworkFile;

/**
 * In Memory Pseudo File Class
 * 
 * <p>Pseudo file class that uses an in memory buffer for the file data.
 *  
 * @author gkspencer
 */
public class MemoryPseudoFile extends PseudoFile
{
    // File data buffer
    
    private byte[] m_data;
    
    /**
     * Class constructor
     * 
     * @param name String
     * @param data byte[]
     */
    public MemoryPseudoFile(String name, byte[] data)
    {
        super( name);
        
        m_data = data;
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
            
            // Create the file information
            
            m_fileInfo = new FileInfo( getFileName(), m_data != null ? m_data.length : 0, getAttributes());
                
            // Set the file creation/modification times
            
            m_fileInfo.setCreationDateTime( _creationDateTime);
            m_fileInfo.setModifyDateTime( _creationDateTime);
            m_fileInfo.setChangeDateTime( _creationDateTime);

            // Set the allocation size, round up the actual length
            
            m_fileInfo.setAllocationSize(( m_fileInfo.getSize() + 512L) & 0xFFFFFFFFFFFFFE00L);
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
        // Create a pseudo file mapped to the in memory file data
        
        FileInfo finfo = getFileInfo();
        finfo.setPath( netPath);
        
        return new MemoryNetworkFile( getFileName(), m_data, finfo);
    }
}
