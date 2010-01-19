/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.repo;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.alfresco.AlfrescoContext;
import org.alfresco.filesys.alfresco.AlfrescoDiskDriver;
import org.alfresco.filesys.alfresco.IOControlHandler;
import org.alfresco.filesys.config.acl.AccessControlListBean;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.FileSystem;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Content Filesystem Context Class
 * 
 * <p>Contains per filesystem context.
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
    
    private AccessControlListBean m_accessControlList;
        
    // Enable/disable oplocks
    
    private boolean m_oplocksDisabled;
    
    // Node monitor
    
    private NodeMonitor m_nodeMonitor;

    /**
     * Default constructor allowing initialization by container.
     */
    public ContentContext()
    {
        // Create the I/O control handler
        
        setIOHandler( createIOHandler( null));
    }
    
    /**
     * Class constructor
     * 
     *@param filesysName
     *            String
     * @param storeName
     *            String
     * @param rootPath
     *            String
     * @param rootNodeRef
     *            NodeRef
     */
    public ContentContext(String filesysName, String storeName, String rootPath, NodeRef rootNodeRef)
    {
        this();
        
        setDeviceName(filesysName);

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
        m_relativePath = path.replace( '/', FileName.DOS_SEPERATOR);;
    }

    public void setOfflineFiles(boolean offlineFiles)
    {
        m_offlineFiles = offlineFiles;
    }        

    public void setDisableNodeMonitor(boolean disableNodeMonitor)
    {
        m_disableNodeMonitor = disableNodeMonitor;
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
     * @param disableOplocks boolean
     */
    public void setDisableOplocks( boolean disableOplocks) {
    	m_oplocksDisabled = disableOplocks;
    }
    
    @Override
    public void initialize(AlfrescoDiskDriver filesysDriver)
    {
        super.initialize(filesysDriver);

        if (m_storeName == null || m_storeName.length() == 0)
        {
            throw new AlfrescoRuntimeException("Device missing storeName");
        }
        
        if (m_rootPath == null || m_rootPath.length() == 0)
        {
            throw new AlfrescoRuntimeException("Device missing rootPath");
        }        
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
    public boolean getDisableOplocks() {
    	return m_oplocksDisabled;
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
     * Close the filesystem context
     */
    public void CloseContext() {

        // Stop the node monitor, if enabled
        
        if ( m_nodeMonitor != null)
            m_nodeMonitor.shutdownRequest();
        
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
        return new ContentIOControlHandler();
    }
    
    /**
     * Set the node monitor
     * 
     * @param filesysDriver ContentDiskDriver
     */
    protected void setNodeMonitor( NodeMonitor nodeMonitor) {
        m_nodeMonitor = nodeMonitor;
    }

    /**
     * Start the filesystem
     * 
     * @param share DiskSharedDevice
     * @exception DeviceContextException
     */
    public void startFilesystem(DiskSharedDevice share)
        throws DeviceContextException {

        // Call the base class
        
        super.startFilesystem(share);
        
        // Start the node monitor, if enabled
        
        if ( m_nodeMonitor != null)
            m_nodeMonitor.startMonitor();
    }
}
