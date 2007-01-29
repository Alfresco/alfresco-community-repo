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

import org.alfresco.filesys.alfresco.AlfrescoContext;
import org.alfresco.filesys.alfresco.IOControlHandler;
import org.alfresco.filesys.server.filesys.*;
import org.alfresco.service.cmr.repository.*;

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
        
        // Create the I/O control handler
        
        setIOHandler( createIOHandler( null));
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
    	return new ContentIOControlHandler();
    }
}
