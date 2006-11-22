/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
package org.alfresco.filesys.smb.server.repo;

import java.util.Enumeration;

import org.alfresco.filesys.server.filesys.*;
import org.alfresco.filesys.server.state.FileStateReaper;
import org.alfresco.filesys.server.state.FileStateTable;
import org.alfresco.filesys.smb.server.repo.pseudo.ContentPseudoFileImpl;
import org.alfresco.filesys.smb.server.repo.pseudo.PseudoFileInterface;
import org.alfresco.service.cmr.repository.*;

/**
 * Content Filesystem Context Class
 * 
 * <p>Contains per filesystem context.
 * 
 * @author GKSpencer
 */
public class ContentContext extends DiskDeviceContext
{
    // Store and root path
    
    private String m_storeName;
    private String m_rootPath;
    
    // Root node
    
    private NodeRef m_rootNodeRef;
    
    // File state table and associated file state reaper
    
    private FileStateTable m_stateTable;
    private FileStateReaper m_stateReaper;
    
    // URL pseudo file web path prefix (server/port/webapp) and link file name
    
    private String m_urlPathPrefix;
    private String m_urlFileName;
    
    // Pseudo file interface
    
    private PseudoFileInterface m_pseudoFileInterface;

    // Desktop actions
    
    private DesktopActionTable m_desktopActions;
    
    // I/O control handler
    
    private IOControlHandler m_ioHandler;
    
    /**
     * Class constructor
     *
     *@param filesysName String
     * @param storeName String
     * @param rootPath String
     * @param rootNodeRef NodeRef
     */
    public ContentContext(String filesysName, String storeName, String rootPath, NodeRef rootNodeRef)
    {
        super(filesysName, rootNodeRef.toString());
        
        m_storeName = storeName;
        m_rootPath  = rootPath;
        
        m_rootNodeRef = rootNodeRef;
        
        // Create the file state table
        
        m_stateTable = new FileStateTable();
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
     * Return the store name
     * 
     * @return String
     */
    public final String getStoreName()
    {
        return m_storeName;
    }
    
    /**
     * Return the root path
     * 
     * @return String
     */
    public final String getRootPath()
    {
        return m_rootPath;
    }
    
    /**
     * Return the root node
     * 
     * @return NodeRef
     */
    public final NodeRef getRootNode()
    {
        return m_rootNodeRef;
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
    
    /**
     * Determine if the pseudo file interface is enabled
     * 
     * @return boolean
     */
    public final boolean hasPseudoFileInterface()
    {
    	return m_pseudoFileInterface != null ? true : false;
    }
    
    /**
     * Return the pseudo file interface
     * 
     * @return PseudoFileInterface
     */
    public final PseudoFileInterface getPseudoFileInterface()
    {
    	return m_pseudoFileInterface;
    }

    /**
     * Enable the pseudo file interface for this filesystem
     */
    public final void enabledPseudoFileInterface()
    {
    	if ( m_pseudoFileInterface == null)
    		m_pseudoFileInterface = new ContentPseudoFileImpl();
    }
    
    /**
     * Determine if there are desktop actins configured
     * 
     * @return boolean
     */
    public final boolean hasDesktopActions()
    {
    	return m_desktopActions != null ? true : false;
    }
    
    /**
     * Return the desktop actions table
     * 
     * @return DesktopActionTable
     */
    public final DesktopActionTable getDesktopActions()
    {
    	return m_desktopActions;
    }
    
    /**
     * Return the count of desktop actions
     * 
     * @return int
     */
    public final int numberOfDesktopActions()
    {
    	return m_desktopActions != null ? m_desktopActions.numberOfActions() : 0;
    }

    /**
     * Add a desktop action
     * 
     * @param action DesktopAction
     * @return boolean
     */
    public final boolean addDesktopAction(DesktopAction action)
    {
    	// Check if the desktop actions table has been created
    	
    	if ( m_desktopActions == null)
    	{
    		m_desktopActions = new DesktopActionTable();
    		
    		// Enable pseudo files
    		
    		enabledPseudoFileInterface();
    	}
    	
    	// Add the action
    	
    	return m_desktopActions.addAction(action);
    }

    /**
     * Determine if custom I/O control handling is enabled for this filesystem
     * 
     * @return boolean
     */
    public final boolean hasIOHandler()
    {
    	return m_ioHandler != null ? true : false;
    }
    
    /**
     * Return the custom I/O control handler
     * 
     * @return IOControlHandler
     */
    public final IOControlHandler getIOHandler()
    {
    	return m_ioHandler;
    }
    
    /**
     * Determine if the URL pseudo file is enabled
     * 
     * @return boolean
     */
    public final boolean hasURLFile()
    {
        if ( m_urlPathPrefix != null && m_urlFileName != null)
            return true;
        return false;
    }
    
    /**
     * Return the URL pseudo file path prefix
     * 
     * @return String
     */
    public final String getURLPrefix()
    {
        return m_urlPathPrefix;
    }
    
    /**
     * Return the URL pseudo file name
     * 
     * @return String
     */
    public final String getURLFileName()
    {
        return m_urlFileName;
    }
    
    /**
     * Set the URL path prefix
     * 
     * @param urlPrefix String
     */
    public final void setURLPrefix(String urlPrefix)
    {
        m_urlPathPrefix = urlPrefix;
        
        if ( urlPrefix != null)
        	enabledPseudoFileInterface();
    }
    
    /**
     * Set the URL pseudo file name
     * 
     * @param urlFileName String
     */
    public final void setURLFileName(String urlFileName)
    {
        m_urlFileName = urlFileName;
        
        if ( urlFileName != null)
        	enabledPseudoFileInterface();
    }

    /**
     * Set the desktop actions
     * 
     * @param desktopActions DesktopActionTable
     * @param filesysDriver DiskInterface
     */
    public final void setDesktopActions(DesktopActionTable desktopActions, DiskInterface filesysDriver)
    {
    	// Enumerate the desktop actions and add to this filesystem
    	
    	Enumeration<String> names = desktopActions.enumerateActionNames();
    	
    	while ( names.hasMoreElements())
    	{
    		addDesktopAction( desktopActions.getAction(names.nextElement()));
    	}
    	
    	// If there are desktop actions then create the custom I/O control handler
    	
    	if ( numberOfDesktopActions() > 0)
    	{
    		// Access the filesystem driver
    		
    		ContentDiskDriver contentDriver = (ContentDiskDriver) filesysDriver; 
    		
    		// Create the custom I/O control handler
    	
    		m_ioHandler = new ContentIOControlHandler();
    		m_ioHandler.initialize(contentDriver, this);
    	}
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
    
}
