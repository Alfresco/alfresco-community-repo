/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.filesys.server.pseudo;

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
        
        if ( getInfo() == null) {
            
            // Get the file details
            
            File localFile = new File( getFilePath());
            if ( localFile.exists())
            {
                // Create the file information
                
                FileInfo fInfo = new FileInfo( getFileName(), localFile.length(), getAttributes());
                
                // Set the file creation/modification times
                
                fInfo.setModifyDateTime( localFile.lastModified());
                fInfo.setCreationDateTime( _creationDateTime);
                fInfo.setChangeDateTime( _creationDateTime);

                // Set the allocation size, round up the actual length
                
                fInfo.setAllocationSize(( localFile.length() + 512L) & 0xFFFFFFFFFFFFFE00L);
                
                setFileInfo( fInfo);
            }            
        }
        
        // Return the file information
        
        return getInfo();
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
