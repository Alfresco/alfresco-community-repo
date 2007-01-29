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

import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * NodeRef Based Network File Class
 * 
 * @author gkspencer
 */
public abstract class NodeRefNetworkFile extends NetworkFile {

	// Associated node ref
	
	protected NodeRef m_nodeRef;
	
    /**
     * Create a network file object with the specified file/directory name.
     * 
     * @param name File name string.
     */
    public NodeRefNetworkFile(String name)
    {
        super( name);
    }

    /**
     * Create a network file object with the specified file/directory name.
     * 
     * @param name File name string.
     * @param node NodeRef
     */
    public NodeRefNetworkFile(String name, NodeRef node)
    {
        super( name);
        
        m_nodeRef = node;
    }

	/**
	 * Return the node ref
	 * 
	 * @return NodeRef
	 */
	public NodeRef getNodeRef()
	{
		return m_nodeRef;
	}
	
	/**
	 * set the node ref
	 * 
	 * @param nodeRef NodeRef
	 */
	public void setNodeRef( NodeRef nodeRef)
	{
		m_nodeRef = nodeRef;
	}
}
