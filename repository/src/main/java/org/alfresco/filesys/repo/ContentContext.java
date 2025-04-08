/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.filesys.repo;

import java.util.regex.Pattern;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.alfresco.AlfrescoContext;
import org.alfresco.filesys.alfresco.AlfrescoDiskDriver;
import org.alfresco.filesys.config.acl.AccessControlListBean;
import org.alfresco.jlan.server.config.CoreServerConfigSection;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.FileSystem;
import org.alfresco.jlan.server.filesys.cache.FileStateLockManager;
import org.alfresco.jlan.server.filesys.quota.QuotaManagerException;
import org.alfresco.jlan.server.thread.ThreadRequestPool;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Content Filesystem Context Class
 * 
 * <p>
 * Contains per filesystem context.
 * 
 * @author GKSpencer
 */
public class ContentContext extends AlfrescoContext
{
    // Store and root path

    private String m_storeName;
    private String m_rootPath;

    // Root node

    private NodeRef m_rootNodeRef;

    private String m_relativePath;

    private boolean m_offlineFiles;

    private boolean m_disableNodeMonitor;

    // Disable change notifications for CIFS

    private boolean m_disableChangeNotifications;

    private AccessControlListBean m_accessControlList;

    // File state based lock/oplock manager

    private FileStateLockManager m_lockManager;

    // Enable/disable oplocks

    private boolean m_oplocksDisabled;

    // Node monitor

    private NodeMonitor m_nodeMonitor;

    // Thread pool

    private ThreadRequestPool m_threadPool;

    // pattern is tested against full path after it has been lower cased.
    private Pattern renameShufflePattern = Pattern.compile("(.*[a-f0-9]{8}+$)|(.*\\.tmp$)|(.*\\.wbk$)|(.*\\.bak$)|(.*\\~$)");

    /**
     * Default constructor allowing initialization by container.
     */
    public ContentContext()
    {
        // // Create the I/O control handler

        // setIOHandler( createIOHandler( null));
    }

    /**
     * Class constructor
     * 
     * @param deviceName
     *            String
     * @param storeName
     *            String
     * @param rootPath
     *            String
     * @param rootNodeRef
     *            NodeRef
     */
    public ContentContext(String deviceName, String storeName, String rootPath, NodeRef rootNodeRef)
    {
        setDeviceName(deviceName);
        setStoreName(storeName);
        setRootPath(rootPath);
        setRootNodeRef(rootNodeRef);
    }

    public void setStoreName(String name)
    {
        m_storeName = name;
    }

    public void setRootPath(String path)
    {
        m_rootPath = path;
    }

    public void setRelativePath(String path)
    {
        // Make sure the path is in CIFS format
        m_relativePath = path.replace('/', FileName.DOS_SEPERATOR);
        ;
    }

    public void setOfflineFiles(boolean offlineFiles)
    {
        m_offlineFiles = offlineFiles;
    }

    public void setDisableNodeMonitor(boolean disableNodeMonitor)
    {
        m_disableNodeMonitor = disableNodeMonitor;
    }

    /**
     * Disable change notifications
     * 
     * @param disableChangeNotify
     *            boolean
     */
    public void setDisableChangeNotifications(boolean disableChangeNotify)
    {
        m_disableChangeNotifications = disableChangeNotify;
    }

    public void setAccessControlList(AccessControlListBean accessControlList)
    {
        m_accessControlList = accessControlList;
    }

    public void setRootNodeRef(NodeRef nodeRef)
    {
        m_rootNodeRef = nodeRef;
        setShareName(nodeRef.toString());
    }

    /**
     * Enable/disable oplock support
     * 
     * @param disableOplocks
     *            boolean
     */
    public void setDisableOplocks(boolean disableOplocks)
    {
        m_oplocksDisabled = disableOplocks;
    }

    /**
     * Get the regular expression pattern that will be applied to detected potential rename shuffles.
     * 
     * @return the regular expression pattern to match against
     */
    public Pattern getRenameShufflePattern()
    {
        return renameShufflePattern;
    }

    /**
     * Set the regular expression that will be applied to filenames during renames to detect whether clients are performing a renaming shuffle - common during file saving on various clients.
     * <p/>
     * <b>ALF-3856</b>
     * 
     * @param renameShufflePattern
     *            a regular expression filename match
     */
    public void setRenameShufflePattern(Pattern renameShufflePattern)
    {
        this.renameShufflePattern = renameShufflePattern;
    }

    @Override
    public void initialize(AlfrescoDiskDriver filesysDriver)
    {
        if (m_storeName == null || m_storeName.length() == 0)
        {
            throw new AlfrescoRuntimeException("Device missing storeName");
        }

        if (m_rootPath == null || m_rootPath.length() == 0)
        {
            throw new AlfrescoRuntimeException("Device missing rootPath");
        }

        // Enable file state caching

        getStateCache().setCaseSensitive(false);

        // Create the file state based lock manager

        m_lockManager = new FileStateLockManager(getStateCache());
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
     * Return the relative path
     * 
     * @return String
     */
    public String getRelativePath()
    {
        return m_relativePath;
    }

    /**
     * Determines whether locked files should be marked as offline.
     * 
     * @return <code>true</code> if locked files should be marked as offline
     */
    public boolean getOfflineFiles()
    {
        return m_offlineFiles;
    }

    /**
     * Determines whether a node monitor is required.
     * 
     * @return <code>true</code> if a node monitor is required
     */
    public boolean getDisableNodeMonitor()
    {
        return m_disableNodeMonitor;
    }

    /**
     * Determine if oplocks support should be disabled
     * 
     * @return boolean
     */
    public boolean getDisableOplocks()
    {
        return m_oplocksDisabled;
    }

    /**
     * Return the lock manager
     * 
     * @return FileStateLockManager
     */
    public FileStateLockManager getLockManager()
    {
        return m_lockManager;
    }

    /**
     * Determine if change notifications are disabled
     * 
     * @return boolean
     */
    public boolean getDisableChangeNotifications()
    {
        return m_disableChangeNotifications;
    }

    /**
     * Gets the access control list.
     * 
     * @return the access control list
     */
    public AccessControlListBean getAccessControlList()
    {
        return m_accessControlList;
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
     * Return the thread pool
     * 
     * @return ThreadRequestPool
     */
    public final ThreadRequestPool getThreadPool()
    {
        return m_threadPool;
    }

    /**
     * Close the filesystem context
     */
    public void CloseContext()
    {

        // Stop the node monitor, if enabled

        if (m_nodeMonitor != null)
            m_nodeMonitor.shutdownRequest();

        // Stop the quota manager, if enabled

        if (hasQuotaManager())
        {
            try
            {
                getQuotaManager().stopManager(null, this);
            }
            catch (QuotaManagerException ex)
            {}
        }

        // Call the base class

        super.CloseContext();
    }

    // /**
    // * Create the I/O control handler for this filesystem type
    // *
    // * @param filesysDriver DiskInterface
    // * @return IOControlHandler
    // */
    // protected IOControlHandler createIOHandler( DiskInterface filesysDriver)
    // {
    // return new ContentIOControlHandler();
    // }

    /**
     * Set the node monitor
     * 
     * @param nodeMonitor
     *            node monitor
     */
    protected void setNodeMonitor(NodeMonitor nodeMonitor)
    {
        m_nodeMonitor = nodeMonitor;
    }

    /**
     * Start the filesystem
     * 
     * @param share
     *            DiskSharedDevice
     * @exception DeviceContextException
     */
    public void startFilesystem(DiskSharedDevice share)
            throws DeviceContextException
    {

        // Call the base class

        super.startFilesystem(share);

        if (getStateCache() != null)
            getStateCache().setCaseSensitive(false);

        // Find the thread pool via the configuration

        CoreServerConfigSection coreConfig = (CoreServerConfigSection) share.getConfiguration().getConfigSection(CoreServerConfigSection.SectionName);
        if (coreConfig != null)
            m_threadPool = coreConfig.getThreadPool();

        // Start the lock manager, use the thread pool if available

        if (getLockManager() != null)
        {

            // Start the lock manager

            m_lockManager.startLockManager("OplockExpire_" + share.getName(), m_threadPool);
        }

        // Start the node monitor, if enabled

        if (m_nodeMonitor != null)
            m_nodeMonitor.startMonitor();
    }
}
