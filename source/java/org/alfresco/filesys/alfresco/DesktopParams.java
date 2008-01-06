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
package org.alfresco.filesys.alfresco;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Desktop Parameters Class
 * 
 * <p>Contains the parameters for a desktop action request from the client side application.
 * 
 * @author gkspencer
 */
public class DesktopParams {

	// File server session
	
	private SrvSession m_session;
	
	// Filesystem driver
	
	private AlfrescoDiskDriver m_driver;
	
	// Folder node that the actions are working in
	
	private NodeRef m_folderNode;
	
	// Network file for the folder node
	
	private NetworkFile m_folderFile;
	
	// List of file/folder/node targets for the action
	
	private List<DesktopTarget> m_targets;
	
	/**
	 * Default constructor
	 */
	public DesktopParams()
	{
	}
	
	/**
	 * Class constructor
	 * 
	 * @param sess SrvSession
	 * @param driver AlfrescoDiskDriver
	 * @param folderNode NodeRef
	 * @param folderFile NetworkFile
	 */
	public DesktopParams(SrvSession sess, AlfrescoDiskDriver driver, NodeRef folderNode, NetworkFile folderFile)
	{
		m_session    = sess;
		m_driver     = driver;
		m_folderNode = folderNode;
		m_folderFile = folderFile;
	}
	
	/**
	 * Return the count of target nodes for the action
	 * 
	 * @return int
	 */
	public final int numberOfTargetNodes()
	{
		return m_targets != null ? m_targets.size() : 0;
	}

	/**
	 * Return the file server session
	 * 
	 * @return SrvSession
	 */
	public final SrvSession getSession()
	{
		return m_session;
	}

	/**
	 * Return the authentication ticket for the user/session
	 * 
	 * @return String
	 */
	public final String getTicket()
	{
		ClientInfo cInfo = m_session.getClientInformation();
		if ( cInfo != null && cInfo instanceof AlfrescoClientInfo) {
		    AlfrescoClientInfo alfInfo = (AlfrescoClientInfo) cInfo;
		    return alfInfo.getAuthenticationTicket();
		}
		return null;
	}
	
	/**
	 * Return the working directory node
	 * 
	 *  @return NodeRef
	 */
	public final NodeRef getFolderNode()
	{
		return m_folderNode;
	}

	/**
	 * Return the folder network file
	 * 
	 * @return NetworkFile
	 */
	public final NetworkFile getFolder()
	{
		return m_folderFile;
	}

	/**
	 * Return the filesystem driver
	 * 
	 * @return AlfrescoDiskDriver
	 */
	public final AlfrescoDiskDriver getDriver()
	{
	    return m_driver;
	}
	
	/**
	 * Set the folder network file
	 * 
	 * @param netFile NetworkFile
	 */
	public final void setFolder(NetworkFile netFile)
	{
		m_folderFile = netFile;
	}
	
	/**
	 * Return the required target
	 * 
	 * @param idx int
	 * @return DesktopTarget
	 */
	public final DesktopTarget getTarget(int idx)
	{
		DesktopTarget deskTarget = null;
		
		if ( m_targets != null && idx >= 0 && idx < m_targets.size())
			deskTarget = m_targets.get(idx);
		
		return deskTarget;
	}
	
	/**
	 * Add a target node for the action
	 * 
	 * @param target DesktopTarget
	 */
	public final void addTarget(DesktopTarget target)
	{
		if ( m_targets == null)
			m_targets = new ArrayList<DesktopTarget>();
		m_targets.add(target);
	}
	
	/**
	 * Return the desktop parameters as a string
	 * 
	 * @return String
	 */
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		
		str.append("[");
		str.append("Targets=");
		str.append(numberOfTargetNodes());
		str.append("]");
		
		return str.toString();
	}
}
