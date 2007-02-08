/*
 * Copyright (C) 2006 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.alfresco.filesys.avm;

import java.util.Map;

import org.alfresco.filesys.alfresco.AlfrescoContext;
import org.alfresco.filesys.alfresco.IOControlHandler;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.FileName;
import org.alfresco.filesys.server.filesys.FileSystem;
import org.alfresco.filesys.server.filesys.NotifyChange;
import org.alfresco.filesys.server.state.FileState;
import org.alfresco.filesys.server.state.FileStateTable;
import org.alfresco.repo.avm.CreateStoreCallback;
import org.alfresco.repo.avm.CreateVersionCallback;
import org.alfresco.repo.avm.PurgeStoreCallback;
import org.alfresco.repo.avm.PurgeVersionCallback;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * AVM Filesystem Context Class
 * 
 * <p>Contains per filesystem context.
 *
 * @author GKSpencer
 */
public class AVMContext extends AlfrescoContext
	implements CreateStoreCallback, PurgeStoreCallback, CreateVersionCallback, PurgeVersionCallback {

    // Logging
    
    private static final Log logger = LogFactory.getLog(AVMContext.class);
    
	// Constants
	//
	// Version id that indicates the head version
	
	public static final int VERSION_HEAD	= -1;
	
    // Store properties
	
    public static QName PROP_WORKFLOWPREVIEW = QName.createQName(NamespaceService.DEFAULT_URI, ".sandbox.workflow.preview");
    public static QName PROP_AUTHORPREVIEW   = QName.createQName(NamespaceService.DEFAULT_URI, ".sandbox.author.preview");
	
    // Store, root path and version
    
    private String m_storePath;
    private int m_version = VERSION_HEAD; 

    // Flag to indicate if the virtualization view is enabled
    //
    //	The first set of folders then map to the stores and the second layer map to the versions with
    //  paths below.

    private boolean m_virtualView;
    
    // Show sandboxes in the virtualization view
    
    private boolean m_showSandboxes = false;
    
    // associated AVM filesystem driver
    
    private AVMDiskDriver m_avmDriver;
    
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
     * @param showSandboxes boolean
     * @param avmDriver AVMDiskDriver
     */
    public AVMContext( String filesysName, boolean showSandboxes, AVMDiskDriver avmDriver)
    {
    	super( filesysName, "VirtualView");
    	
    	// Enable the virtualization view
    	
    	m_virtualView = true;
    	m_showSandboxes = showSandboxes;
    	
    	// Save the associated filesystem driver
    	
    	m_avmDriver = avmDriver;
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
     * Check if sandboxes should be shown in the virtualization view
     * 
     * @return boolean
     */
    public final boolean showSandboxes()
    {
    	return m_showSandboxes;
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

	/**
	 * Create store call back handler
	 * 
	 * @param storeName String
	 * @param versionID int
	 */
	public void storeCreated(String storeName)
	{
		// Make sure the file state cache is enabled
		
		FileStateTable fsTable = getStateTable();
		if ( fsTable == null)
			return;
		
		// Find the file state for the root folder
		
		FileState rootState = fsTable.findFileState( FileName.DOS_SEPERATOR_STR, true, true);

		if ( rootState != null)
		{
    		// Get the properties for the new store
    		
			boolean sandbox = false;
    		Map<QName, PropertyValue> props = m_avmDriver.getAVMStoreProperties( storeName);
    		
    		if ( props.containsKey( PROP_WORKFLOWPREVIEW) || props.containsKey( PROP_AUTHORPREVIEW))
    			sandbox = true;
    		
    		// Add a pseudo file for the current store

    		if ( sandbox == false || showSandboxes() == true)
    		{
				// Add a pseudo folder for the new store
				
				rootState.addPseudoFile( new StorePseudoFile( storeName));
				
				// DEBUG
				
				if ( logger.isDebugEnabled())
					logger.debug( "Added pseudo folder for new store " + storeName);
				
				// Send a change notification for the new folder
				
				if ( hasChangeHandler())
				{
					// Build the filesystem relative path to the new store folder
					
					StringBuilder str = new StringBuilder();
					
					str.append( FileName.DOS_SEPERATOR);
					str.append( storeName);
					
					// Send the change notification
					
	                getChangeHandler().notifyDirectoryChanged(NotifyChange.ActionAdded, str.toString());
				}
    		}
		}
	}
	
	/**
	 * Purge store call back handler
	 * 
	 * @param storeName String
	 */
	public void storePurged(String storeName)
	{
		// Make sure the file state cache is enabled
		
		FileStateTable fsTable = getStateTable();
		if ( fsTable == null)
			return;
		
		// Find the file state for the root folder
		
		FileState rootState = fsTable.findFileState( FileName.DOS_SEPERATOR_STR);
		
		if ( rootState != null && rootState.hasPseudoFiles())
		{
			// Remove the pseudo folder for the store

			rootState.getPseudoFileList().removeFile( storeName, false);
			
			// Build the filesystem relative path to the deleted store folder
			
			StringBuilder pathStr = new StringBuilder();
			
			pathStr.append( FileName.DOS_SEPERATOR);
			pathStr.append( storeName);

			// Remove the file state for the deleted store
			
			String storePath = pathStr.toString();
			fsTable.removeFileState( storePath);
			
			// DEBUG
			
			if ( logger.isDebugEnabled())
				logger.debug( "Removed pseudo folder for purged store " + storeName);
			
			// Send a change notification for the deleted folder
			
			if ( hasChangeHandler())
			{
				// Send the change notification
				
	            getChangeHandler().notifyDirectoryChanged(NotifyChange.ActionRemoved, storePath);
			}
		}
	}

	/**
	 * Create version call back handler
	 * 
	 * @param storeName String
	 * @param versionID int
	 */
	public void versionCreated(String storeName, int versionID)
	{
		// Make sure the file state cache is enabled
		
		FileStateTable fsTable = getStateTable();
		if ( fsTable == null)
			return;

		// Build the path to the store version folder
		
		StringBuilder pathStr = new StringBuilder();

		pathStr.append( FileName.DOS_SEPERATOR);
		pathStr.append( storeName);
		pathStr.append( FileName.DOS_SEPERATOR);
		pathStr.append( AVMPath.VersionsFolder);
		
		// Find the file state for the store versions folder
		
		FileState verState = fsTable.findFileState( pathStr.toString());

		if ( verState != null)
		{
			// Create the version folder name
			
			StringBuilder verStr = new StringBuilder();
			
			verStr.append( AVMPath.VersionFolderPrefix);
			verStr.append( versionID);
			
			String verName = verStr.toString();
			
			// Add a pseudo folder for the new version
			
			verState.addPseudoFile( new VersionPseudoFile( verName));
			
			// DEBUG
			
			if ( logger.isDebugEnabled())
				logger.debug( "Added pseudo folder for new version " + storeName + ":/" + verName);
			
			// Send a change notification for the new folder
			
			if ( hasChangeHandler())
			{
				// Build the filesystem relative path to the new version folder
				
				pathStr.append( FileName.DOS_SEPERATOR);
				pathStr.append( verName);
				
				// Send the change notification
				
                getChangeHandler().notifyDirectoryChanged(NotifyChange.ActionAdded, pathStr.toString());
			}
		}
	}

	/**
	 * Purge version call back handler
	 * 
	 * @param storeName String
	 */
	public void versionPurged(String storeName, int versionID)
	{
		// Make sure the file state cache is enabled
		
		FileStateTable fsTable = getStateTable();
		if ( fsTable == null)
			return;

		// Build the path to the store version folder
		
		StringBuilder pathStr = new StringBuilder();

		pathStr.append( FileName.DOS_SEPERATOR);
		pathStr.append( storeName);
		pathStr.append( FileName.DOS_SEPERATOR);
		pathStr.append( AVMPath.VersionsFolder);
		
		// Find the file state for the store versions folder
		
		FileState verState = fsTable.findFileState( pathStr.toString());

		if ( verState != null && verState.hasPseudoFiles())
		{
			// Create the version folder name
			
			StringBuilder verStr = new StringBuilder();
			
			verStr.append( AVMPath.VersionFolderPrefix);
			verStr.append( versionID);
			
			String verName = verStr.toString();
			
			// Remove the pseudo folder for the purged version
			
			verState.getPseudoFileList().removeFile( verName, true);
			
			// DEBUG
			
			if ( logger.isDebugEnabled())
				logger.debug( "Removed pseudo folder for purged version " + storeName + ":/" + verName);
			
			// Send a change notification for the deleted folder
			
			if ( hasChangeHandler())
			{
				// Build the filesystem relative path to the deleted version folder
				
				pathStr.append( FileName.DOS_SEPERATOR);
				pathStr.append( verName);
				
				// Send the change notification
				
                getChangeHandler().notifyDirectoryChanged(NotifyChange.ActionRemoved, pathStr.toString());
			}
		}
	}
}
