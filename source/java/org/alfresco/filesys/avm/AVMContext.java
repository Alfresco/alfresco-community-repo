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

package org.alfresco.filesys.avm;

import org.alfresco.filesys.server.filesys.DiskDeviceContext;
import org.alfresco.filesys.server.filesys.FileSystem;
import org.alfresco.filesys.server.filesys.SrvDiskInfo;

/**
 * AVM Filesystem Context Class
 * 
 * <p>Contains per filesystem context.
 *
 * @author GKSpencer
 */
public class AVMContext extends DiskDeviceContext {

	// Constants
	//
	// Version id that indicates the head version
	
	public static final int VERSION_HEAD	= -1;
	
	// Store, root path and version
    
    private String m_storePath;
    private int m_version = VERSION_HEAD; 

    /**
     * Class constructor
     * 
     * @param storePath String
     * @param version int
     */
    public AVMContext( String storePath, int version)
    {
    	super( storePath + "(" + version + ")");
    	
    	// Set the store root path, remove any trailing slash as relative paths will be appended to this value
    	
    	m_storePath = storePath;
    	if ( m_storePath.endsWith( "/"))
    		m_storePath = m_storePath.substring(0, m_storePath.length() - 1);
    	
    	// Set the store version to use
    	
    	m_version = version;
    	
        // Default the filesystem to look like an 80Gb sized disk with 90% free space
        
        setDiskInformation(new SrvDiskInfo(2560000, 64, 512, 2304000));
        
        // Set filesystem parameters
        
        setFilesystemAttributes(FileSystem.CasePreservedNames + FileSystem.UnicodeOnDisk +
        		FileSystem.CaseSensitiveSearch);
    }

    /**
     * Return the filesystem type, either FileSystem.TypeFAT or FileSystem.TypeNTFS.
     * 
     * @return String
     */
    public String getFilesystemType()
    {
        return FileSystem.TypeNTFS;
    }
    
    /**
     * Return the store path
     * 
     * @return String
     */
    public final String getStorePath()
    {
        return m_storePath;
    }
    
    /**
     * Return the version
     * 
     * @return int
     */
    public final int isVersion()
    {
    	return m_version;
    }
    
    /**
     * Close the filesystem context
     */
	public void CloseContext() {
		
		//	Call the base class
		
		super.CloseContext();
	}
}
