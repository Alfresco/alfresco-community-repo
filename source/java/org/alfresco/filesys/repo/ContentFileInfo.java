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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */
package org.alfresco.filesys.repo;

import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Content Disk Driver File Info Class
 * 
 * <p>Adds fields for the file/folder NodeRef, and linked NodeRef for a link node.
 * 
 * @author gkspencer
 */
public class ContentFileInfo extends FileInfo {

	
	private static final long serialVersionUID = 2518699645372408663L;

	// File/folder node
	
	private NodeRef m_nodeRef;
	
	// Linked node
	
	private NodeRef m_linkRef;

	public ContentFileInfo(NodeRef nodeRef)
	{
	    this.m_nodeRef = nodeRef;
	}
	
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
	
	@Override
	public boolean equals(Object other)
	{
	   if (this == other)
	   {
	       return true;
	   }
	   if (other == null || !(other instanceof FileInfo))
	   {
	       return false;
	   }
	      
	   ContentFileInfo o = (ContentFileInfo)other;
	   
	   return m_nodeRef.equals(o.getNodeRef());
	}     
	  
	@Override
	public int hashCode()
	{
	      return m_nodeRef.hashCode();
    }
}
