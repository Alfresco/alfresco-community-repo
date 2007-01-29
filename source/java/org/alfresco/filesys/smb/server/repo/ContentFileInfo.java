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
