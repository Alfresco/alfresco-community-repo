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

import org.alfresco.filesys.alfresco.AlfrescoContext;
import org.alfresco.filesys.alfresco.IOControlHandler;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.FileSystem;

/**
 * AVM Filesystem Context Class
 * 
 * <p>Contains per filesystem context.
 *
 * @author GKSpencer
 */
public class AVMContext extends AlfrescoContext {

	// Constants
	//
	// Version id that indicates the head version
	
	public static final int VERSION_HEAD	= -1;
	
	// Store, root path and version
    
    private String m_storePath;
    private int m_version = VERSION_HEAD; 

    // Flag to indicate if the virtualization view is enabled
    //
    //	The first set of folders then map to the stores and the second layer map to the versions with
    //  paths below.

    private boolean m_virtualView;
    
    /**
     * Class constructor
     * 
     * <p>Construct a context for a normal view onto a single store/version within AVM.
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
    }

    /**
     * Class constructor
     * 
     * <p>Construct a context for a virtualization view onto all stores/versions within AVM.
     * 
     * @param filesysName String
     */
    public AVMContext( String filesysName)
    {
    	super( filesysName, "VirtualView");
    	
    	// Enable the virtualization view
    	
    	m_virtualView = true;
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
     * Check if the virtualization view is enabled
     * 
     * @return boolean
     */
    public final boolean isVirtualizationView()
    {
    	return m_virtualView;
    }
    
    /**
     * Close the filesystem context
     */
	public void CloseContext() {
		
		//	Call the base class
		
		super.CloseContext();
	}
	
    /**
     * Create the I/O control handler for this filesystem type
     * 
     * @param filesysDriver DiskInterface
     * @return IOControlHandler
     */
    protected IOControlHandler createIOHandler( DiskInterface filesysDriver)
    {
    	return null;
    }
}
