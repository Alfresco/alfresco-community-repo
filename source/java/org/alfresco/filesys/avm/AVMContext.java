/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.filesys.avm;

import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.alfresco.AlfrescoContext;
import org.alfresco.filesys.alfresco.AlfrescoDiskDriver;
import org.alfresco.filesys.alfresco.IOControlHandler;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.FileSystem;
import org.alfresco.jlan.server.filesys.NotifyChange;
import org.alfresco.jlan.server.filesys.cache.FileState;
import org.alfresco.jlan.server.filesys.cache.FileStateCache;
import org.alfresco.jlan.util.StringList;
import org.alfresco.repo.avm.CreateStoreCallback;
import org.alfresco.repo.avm.CreateVersionCallback;
import org.alfresco.repo.avm.PurgeStoreCallback;
import org.alfresco.repo.avm.PurgeVersionCallback;
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
    
    public static final int VERSION_HEAD    = -1;
    
    // Store types to show in the virtualization view
    
    public static final int ShowNormalStores        = 0x0001;
    public static final int ShowSiteStores          = 0x0002;
    public static final int ShowStagingStores       = 0x0004;
    public static final int ShowAuthorStores        = 0x0008;   
    public static final int ShowPreviewStores       = 0x0010;
    
    // Store, root path and version
    
    private String m_storePath;
    private int m_version = VERSION_HEAD; 

    // Flag to indicate if the virtualization view is enabled
    //
    //  The first set of folders then map to the stores and the second layer map to the versions with
    //  paths below.

    private boolean m_virtualView;
    
    // Virtualization view filtering options
    
    private int m_showOptions = ShowStagingStores + ShowAuthorStores;
    
    // List of newly created store names that need adding into the virtualization view
    
    private StringList m_newStores;
    private Object m_newStoresLock;
    
    // Allow admin user to write to web project staging stores
    
    private boolean m_allowAdminStagingWrites;
    
    // Auto create the store if it doesn't exist?
    
    private boolean m_createStore;
    
    /**
     * Default constructor allowing initialization by container.
     */
    public AVMContext()
    {
    }

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
        setDeviceName(filesysName);
        
        // Set the store root path
        setStorePath(storePath);
        
        // Set the store version to use     
        setVersion(version);
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
        setDeviceName(filesysName);
        
        // Enable the virtualization view
        setVirtualView(true);
        setShowOptions(showOptions);
    }

    public void setStorePath(String path)
    {
        this.m_storePath = path;
    }

    public void setVersion(int version)
    {
        this.m_version = version;
    }

    public void setShowOptions(int showOptions)
    {
        this.m_showOptions = showOptions;
    }
    
    public void setStores(String showAttr)
    {
        if ( showAttr != null)
        {
            // Split the show options string
            
            StringTokenizer tokens = new StringTokenizer( showAttr, ",");
            StringList optList = new StringList();
            
            while ( tokens.hasMoreTokens())
                optList.addString( tokens.nextToken().trim().toLowerCase());
            
            // Build the show options mask
            
            this.m_showOptions = 0;
            
            if ( optList.containsString("normal"))
                this.m_showOptions += ShowNormalStores;
            
            if ( optList.containsString("site"))
                this.m_showOptions += ShowSiteStores;
            
            if ( optList.containsString("author"))
                this.m_showOptions += ShowAuthorStores;
            
            if ( optList.containsString("preview"))
                this.m_showOptions += ShowPreviewStores;
            
            if ( optList.containsString("staging"))
                this.m_showOptions += ShowStagingStores;
        }   
    }

    public void setVirtualView(boolean isVirtualView)
    {
        this.m_virtualView = isVirtualView;
    }
    
    public boolean getCreateStore()
    {
        return m_createStore;
    }

    public void setCreateStore(boolean createStore)
    {
        m_createStore = createStore;
    }

    
    @Override
    public void initialize(AlfrescoDiskDriver filesysDriver)
    {
        if (m_virtualView)
        {
            // A context for a view onto all stores/versions within AVM.
            m_newStoresLock = new Object();
            m_newStores = new StringList();

            setShareName("VirtualView");            
        }
        else
        {
            if (m_storePath == null
                    || m_storePath.length() == 0)
                throw new AlfrescoRuntimeException("Device missing init value: storePath");
            
            // A context for a normal view onto a single store/version within AVM.
            if (m_storePath.endsWith("/"))
                m_storePath = m_storePath.substring(0, m_storePath.length() - 1);

            // Range check the version id
            if (m_version < 0 && m_version != AVMContext.VERSION_HEAD)
                throw new AlfrescoRuntimeException("Invalid store version id specified, " + m_version);
            
            setShareName(m_storePath + "(" + m_version + ")");                        
        }
        super.initialize(filesysDriver);
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
     * Check if the admin user is allowed to write to web project staging stores
     * 
     * @return boolean
     */
    public final boolean allowAdminStagingWrites() {
        return m_allowAdminStagingWrites;
    }

    /**
     * Set the admin web project staging store writeable status
     * 
     * @param writeable boolean
     */
    public final void setAllowAdminStagingWrites(boolean writeable) {
        m_allowAdminStagingWrites = writeable;
    }
    
    /**
     * Check if there are any new stores queued for adding to the virtualization view
     * 
     * @return boolean
     */
    protected final boolean hasNewStoresQueued() {
        if ( m_newStores == null || m_newStores.numberOfStrings() == 0)
            return false;
        return true;
    }
    
    /**
     * Return the new stores queue, and reset the current queue
     * 
     * @return StringList
     */
    protected StringList getNewStoresQueue() {
        
        StringList storesQueue = null;
        
        synchronized ( m_newStoresLock) {
            storesQueue = m_newStores;
            m_newStores = new StringList();
        }
        
        return storesQueue;
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
     * Check if site data stores should be shown in the virtualization view
     * 
     * @return boolean
     */
    public final boolean showSiteStores()
    {
        return (m_showOptions & ShowSiteStores) != 0 ? true : false;
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
        
        switch (storeType)
        {
            case StoreType.Normal:
                showStore = showNormalStores();
                break;
            case StoreType.SiteStore:
                showStore = showSiteStores();
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
        
        //  Call the base class
        
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
        // Not interested if the virtualization view is not enabled
        
        if ( isVirtualizationView() == false)
            return;
        
        // Make sure the file state cache is enabled
        
        FileStateCache fsTable = getStateCache();
        if ( fsTable == null)
            return;
        
        // Find the file state for the root folder
        
        FileState rootState = fsTable.findFileState( FileName.DOS_SEPERATOR_STR, true);

        if ( rootState != null)
        {
            // DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug("Queueing new store " + storeName + " for addition to virtualization view");
            
            // Add the new store name to the list to be picked up by the next file server access
            // to the filesystem
            
            synchronized ( m_newStoresLock) {
                
                // Add the new store name
                
                m_newStores.addString( storeName);
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
        // Not interested if the virtualization view is not enabled
        
        if ( isVirtualizationView() == false)
            return;
        
        // Make sure the file state cache is enabled
        
        FileStateCache fsTable = getStateCache();
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
            
            rootState.updateModifyDateTime();
            
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
        // Not interested if the virtualization view is not enabled
        
        if ( isVirtualizationView() == false)
            return;
        
        // Make sure the file state cache is enabled
        
        FileStateCache fsTable = getStateCache();
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
        // Not interested if the virtualization view is not enabled
        
        if ( isVirtualizationView() == false)
            return;
        
        // Make sure the file state cache is enabled
        
        FileStateCache fsTable = getStateCache();
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
