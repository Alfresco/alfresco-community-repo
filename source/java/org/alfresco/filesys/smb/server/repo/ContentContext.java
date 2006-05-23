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
package org.alfresco.filesys.smb.server.repo;

import org.alfresco.filesys.server.filesys.*;
import org.alfresco.filesys.smb.server.repo.pseudo.PseudoFile;
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
    
    // File state table
    
    private FileStateTable m_stateTable;
    
    // Drag and drop pseudo file
    
    private PseudoFile m_dragAndDropApp;
    
    // URL pseudo file web path prefix (server/port/webapp) and link file name
    
    private String m_urlPathPrefix;
    private String m_urlFileName;
    
    /**
     * Class constructor
     *
     * @param storeName String
     * @param rootPath String
     * @param rootNodeRef NodeRef
     */
    public ContentContext(String storeName, String rootPath, NodeRef rootNodeRef)
    {
        super(rootNodeRef.toString());
        
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
     */
    public final void enableStateTable(boolean ena)
    {
        if ( ena == false)
            m_stateTable = null;
        else if ( m_stateTable == null)
            m_stateTable = new FileStateTable();
    }
    
    /**
     * Determine if the drag and drop pseudo file has been configured
     * 
     * @return boolean
     */
    public final boolean hasDragAndDropApp()
    {
        return m_dragAndDropApp != null ? true : false;
    }
    
    /**
     * Return the drag and drop pseudo file
     * 
     * @return PseudoFile
     */
    public final PseudoFile getDragAndDropApp()
    {
        return m_dragAndDropApp;
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
     * Set the drag and drop application details
     * 
     * @param dragDropApp PseudoFile
     */
    public final void setDragAndDropApp(PseudoFile dragDropApp)
    {
        m_dragAndDropApp = dragDropApp;
    }
    
    /**
     * Set the URL path prefix
     * 
     * @param urlPrefix String
     */
    public final void setURLPrefix(String urlPrefix)
    {
        m_urlPathPrefix = urlPrefix;
    }
    
    /**
     * Set the URL pseudo file name
     * 
     * @param urlFileName String
     */
    public final void setURLFileName(String urlFileName)
    {
        m_urlFileName = urlFileName;
    }

    /**
     * Close the filesystem context
     */
	public void CloseContext() {
		
		// Check if file states are enabled
		
		if ( hasStateTable())
		{
			//	Shutdown the file state checker thread
			
			getStateTable().shutdownRequest();
		}
		
		//	Call the base class
		
		super.CloseContext();
	}
    
}
