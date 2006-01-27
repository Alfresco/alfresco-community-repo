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
import org.alfresco.filesys.server.filesys.NetworkFile;

/**
 * Pseudo File Class
 * 
 * <p>Creates a pseudo file entry for a folder that maps to a file outside of the usual file area but appears
 * in folder listings for the owner folder.
 * 
 * @author gkspencer
 */
public abstract class PseudoFile
{
    // Dummy creation date/time to use for pseudo files
    
    protected static long _creationDateTime = System.currentTimeMillis();
    
    // File name for pseudo file
    
    private String m_fileName;

    // File flags/attributes
    
    private int m_fileFlags = FileAttribute.ReadOnly;
    
    // File information, used for file information/folder searches
    
    protected FileInfo m_fileInfo;
    
    /**
     * Class constructor
     * 
     * @param name String
     */
    protected PseudoFile(String name)
    {
        m_fileName = name;
    }
    
    /**
     * Class constructor
     * 
     * @param name String
     * @param flags int
     */
    protected PseudoFile(String name, int flags)
    {
        m_fileName = name;
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
    public abstract FileInfo getFileInfo();
    
    /**
     * Return a network file for reading/writing the pseudo file
     * 
     * @param netPath String
     * @return NetworkFile
     */
    public abstract NetworkFile getFile(String netPath);
    
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
        str.append(getFileInfo());
        str.append("]");
        
        return str.toString();
    }
}
