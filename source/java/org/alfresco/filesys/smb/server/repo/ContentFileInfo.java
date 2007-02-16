/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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

import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Content Disk Driver File Info Class
 * 
 * <p>Adds fields for the file/folder NodeRef, and linked NodeRef for a link node.
 * 
 * @author gkspencer
 */
public class ContentFileInfo extends FileInfo {

	// Version id
	
	private static final long serialVersionUID = 2518699645372408663L;

	// File/folder node
	
	private NodeRef m_nodeRef;
	
	// Linked node
	
	private NodeRef m_linkRef;
	
	/**
	 * Return the file/folder node
	 * 
	 * @return NodeRef
	 */
	public final NodeRef getNodeRef()
	{
		return m_nodeRef;
	}
	
	/**
	 * Check if this is a link node
	 * 
	 * @return boolean
	 */
	public final boolean isLinkNode()
	{
		return m_linkRef != null ? true : false;
	}
	
	/**
	 * Return the link node, or null if this is not a link
	 * 
	 * @return NodeRef
	 */
	public final NodeRef getLinkNodeRef()
	{
		return m_linkRef;
	}
	
	/**
	 * Set the node for this file/folder
	 * 
	 *  @param node NodeRef
	 */
	public final void setNodeRef(NodeRef node)
	{
		m_nodeRef = node;
	}
	
	/**
	 * Set the link node
	 * 
	 * @param link NodeRef
	 */
	public final void setLinkNodeRef(NodeRef link)
	{
		m_linkRef = link;
	}
}
