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
import org.alfresco.filesys.server.state.FileStateReaper;
import org.alfresco.filesys.server.state.FileStateTable;

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

    // File state table and associated file state reaper
    
    private FileStateTable m_stateTable;
    private FileStateReaper m_stateReaper;
    
    /**
     * Class constructor
     * 
     * @param filesysName String
     * @param storePath String
     * @param version int
     */
    public AVMContext( String filesysName, String storePath, int version)
    {
    	super( filesysName, storePath + "(" + version + ")");
    	
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
		
		//	Deregister the file state table from the reaper
		
		if ( m_stateTable != null)
			enableStateTable( false, m_stateReaper);
		
		//	Call the base class
		
		super.CloseContext();
	}
	
    /**
     * Determine if the file state table is enabled
     * 
     * @return boolean
     */
    public final boolean hasStateTable()
    {
        return m_stateTable != null ? true : false;
    }
    
    /**
     * Return the file state table
     * 
     * @return FileStateTable
     */
    public final FileStateTable getStateTable()
    {
        return m_stateTable;
    }
    
    /**
     * Enable/disable the file state table
     * 
     * @param ena boolean
     * @param stateReaper FileStateReaper
     */
    public final void enableStateTable(boolean ena, FileStateReaper stateReaper)
    {
        if ( ena == false)
        {
        	// Remove the state table from the reaper
        	
        	stateReaper.removeStateTable( getFilesystemName());
            m_stateTable = null;
        }
        else if ( m_stateTable == null)
        {
        	// Create the file state table

            m_stateTable = new FileStateTable();
            
            // Register with the file state reaper
            
            stateReaper.addStateTable( getFilesystemName(), m_stateTable);
        }
        
        // Save the reaper, for deregistering when the filesystem is closed
        
        m_stateReaper = stateReaper;
    }
}
