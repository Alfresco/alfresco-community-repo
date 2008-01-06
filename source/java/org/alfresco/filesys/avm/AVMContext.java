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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.filesys.avm;

import java.util.Map;

import org.alfresco.filesys.alfresco.AlfrescoContext;
import org.alfresco.filesys.alfresco.IOControlHandler;
import org.alfresco.filesys.state.FileState;
import org.alfresco.filesys.state.FileStateTable;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.FileSystem;
import org.alfresco.jlan.server.filesys.NotifyChange;
import org.alfresco.repo.avm.CreateStoreCallback;
import org.alfresco.repo.avm.CreateVersionCallback;
import org.alfresco.repo.avm.PurgeStoreCallback;
import org.alfresco.repo.avm.PurgeVersionCallback;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.sandbox.SandboxConstants;
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
	
	// Store types to show in the virtualization view
	
	public static final int ShowNormalStores		= 0x0001;
	public static final int ShowStagingStores		= 0x0002;
	public static final int ShowAuthorStores		= 0x0004;	
	public static final int ShowPreviewStores		= 0x0008;
	
    // Store, root path and version
    
    private String m_storePath;
    private int m_version = VERSION_HEAD; 

    // Flag to indicate if the virtualization view is enabled
    //
    //	The first set of folders then map to the stores and the second layer map to the versions with
    //  paths below.

    private boolean m_virtualView;
    
    // Associated AVM filesystem driver
    
    private AVMDiskDriver m_avmDriver;

    // Virtualization view filtering options
    
    private int m_showOptions;
    
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
     * @param showOptions int
     * @param avmDriver AVMDiskDriver
     */
    public AVMContext( String filesysName, int showOptions, AVMDiskDriver avmDriver)
    {
    	super( filesysName, "VirtualView");
    	
    	// Enable the virtualization view
    	
    	m_virtualView = true;
    	m_showOptions = showOptions;
    	
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
     * Check if normal stores should be shown in the virtualization view
     * 
     * @return boolean
     */
    public final boolean showNormalStores()
    {
    	return (m_showOptions & ShowNormalStores) != 0 ? true : false;
    }
    
    /**
     * Check if author stores should be shown in the virtualization view
     * 
     * @return boolean
     */
    public final boolean showAuthorStores()
    {
    	return (m_showOptions & ShowAuthorStores) != 0 ? true : false;
    }
    
    /**
     * Check if preview stores should be shown in the virtualization view
     * 
     * @return boolean
     */
    public final boolean showPreviewStores()
    {
    	return (m_showOptions & ShowPreviewStores) != 0 ? true : false;
    }

    /**
     * Check if staging stores should be shown in the virtualization view
     * 
     * @return boolean
     */
    public final boolean showStagingStores()
    {
    	return (m_showOptions & ShowStagingStores) != 0 ? true : false;
    }

    /**
     * Check if the specified store type should be visible
     * 
     * @param storeType int
     * @return boolean
     */
    public final boolean showStoreType(int storeType)
    {
    	boolean showStore = false;
    	
    	switch ( storeType)
    	{
    	case StoreType.Normal:
    		showStore = showNormalStores();
    		break;
    	case StoreType.WebAuthorMain:
    		showStore = showAuthorStores();
    		break;
    	case StoreType.WebStagingMain:
    		showStore = showStagingStores();
    		break;
    	case StoreType.WebAuthorPreview:
    	case StoreType.WebStagingPreview:
    		showStore = showPreviewStores();
    		break;
    	}
    	
    	return showStore;
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
			// Delete the root folder file state and recreate it
			
			fsTable.removeFileState( FileName.DOS_SEPERATOR_STR);
//			m_avmDriver.findPseudoState( new AVMPath( ""), this);
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
			
			// Update the file state modify time
			
			rootState.setLastUpdated( System.currentTimeMillis());
			
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

			pathStr.append( FileName.DOS_SEPERATOR);
			pathStr.append( verName);
			
			verState.addPseudoFile( new VersionPseudoFile( verName, pathStr.toString()));
			
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
