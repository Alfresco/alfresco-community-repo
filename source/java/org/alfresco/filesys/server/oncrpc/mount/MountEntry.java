/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.filesys.server.oncrpc.mount;

/**
 * Mount Entry Class
 * 
 * <p>Contains the details of an active NFS mount.
 * 
 * @author GKSpencer
 */
public class MountEntry {

	//	Remote host name/address
		
	private String m_host;
		
	//	Mount path
		
	private String m_path;
				
	/**
	 * Class constructor
	 * 
	 * @param host String
	 * @param path String
	 */
	public MountEntry(String host, String path) {
		m_host = host;
		m_path = path;
	}
	
	/**
	 * Return the host name/address
	 * 
	 * @return String
	 */
	public final String getHost() {
		return m_host;
	}
	
	/**
	 * Return the mount path
	 *
	 * @return String
	 */
	public final String getPath() {
		return m_path;
	}
	
	/**
	 * Return the mount entry as a string
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuffer str = new StringBuffer();
		
		str.append("[");
		str.append(getHost());
		str.append(":");
		str.append(getPath());
		str.append("]");
		
		return str.toString();
	}
}
