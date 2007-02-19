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
