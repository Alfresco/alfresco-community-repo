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

package org.alfresco.filesys.avm;

import java.util.Hashtable;

import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Web Project Store Pseudo File Class
 * 
 * <p>Represents an AVM store as a folder.
 *
 * @author gkspencer
 */
public class WebProjectStorePseudoFile extends StorePseudoFile {

	// Store/web project user access roles
	
	public static final int RoleNone			= 0;
	public static final int RolePublisher		= 1;
	public static final int RoleContentManager	= 2;
	
	// Node ref for this store
	
	private NodeRef m_noderef;
	
	// List of users that are content managers/publishers for this web project store
	
	private Hashtable<String, Integer> m_users;
	
	/**
	 * Class constructor
	 * 
	 * @param storeDesc AVMStoreDescriptor
	 * @param relPath String
	 * @param nodeRef NodeRef
	 */
	public WebProjectStorePseudoFile( AVMStoreDescriptor storeDesc, String relPath, NodeRef nodeRef)
	{
		super( storeDesc, relPath, StoreType.WebStagingMain);
		
		// Create static file information from the store details
		
		FileInfo fInfo = new FileInfo( storeDesc.getName(), 0L, FileAttribute.Directory + FileAttribute.ReadOnly);

		fInfo.setCreationDateTime( storeDesc.getCreateDate());
		fInfo.setModifyDateTime( storeDesc.getCreateDate());
		fInfo.setAccessDateTime( storeDesc.getCreateDate());
		fInfo.setChangeDateTime( storeDesc.getCreateDate());
		
		fInfo.setPath( relPath);
		fInfo.setFileId( relPath.hashCode());
		
		setFileInfo( fInfo);

		// Set the associated node ref for the web project
		
		m_noderef = nodeRef;
	}
	
	/**
	 * Class constructor
	 * 
	 * @param storeName String
	 * @param relPath String
	 * @param nodeRef NodeRef
	 */
	public WebProjectStorePseudoFile( String storeName, String relPath, NodeRef nodeRef)
	{
		super( storeName, relPath);
		
		// Create static file information from the store details
		
		FileInfo fInfo = new FileInfo( storeName, 0L, FileAttribute.Directory + FileAttribute.ReadOnly);

		long timeNow = System.currentTimeMillis();
		fInfo.setCreationDateTime( timeNow);
		fInfo.setModifyDateTime( timeNow);
		fInfo.setAccessDateTime( timeNow);
		fInfo.setChangeDateTime( timeNow);
		
		fInfo.setPath( relPath);
		fInfo.setFileId( relPath.hashCode());
		
		setFileInfo( fInfo);

		// Set the associated node ref for the web project
		
		m_noderef = nodeRef;
	}
	
	/**
	 * Check if the associated node ref is valid
	 * 
	 * @return boolean
	 */
	public final boolean hasNodeRef()
	{
		return m_noderef != null ? true : false;
	}
	
	/**
	 * Get the associated node ref for the store
	 * 
	 * @return NodeRef
	 */
	public final NodeRef getNodeRef()
	{
		return m_noderef;
	}
	
	/**
	 * Set the associated node ref for the store
	 * 
	 * @param node NodeRef
	 */
	public final void setNodeRef(NodeRef node)
	{
		m_noderef = node;
	}
	
	/**
	 * Return the role for the specified user within this web project
	 * 
	 * @param userName String
	 * @return int
	 */
	public final int getUserRole(String userName)
	{
		if ( m_users == null)
			return RoleNone;
		
		Integer role = m_users.get( userName);
		return role != null ? role.intValue() : RoleNone;
	}
	
	/**
	 * Add a user role for this web project
	 * 
	 * @param userName String
	 * @param role int
	 */
	public final void addUserRole(String userName, int role)
	{
		if ( m_users == null)
			m_users = new Hashtable<String, Integer>();
		
		m_users.put(userName, new Integer(role));
	}
	
	/**
	 * Remove a user role for this project
	 * 
	 * @param userName String
	 */
	public final void removeUserRole(String userName)
	{
		if ( m_users != null)
			m_users.remove(userName);
	}
}
