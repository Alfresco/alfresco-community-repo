/*
 * Copyright (C) 2006-2008 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.filesys.repo;

import org.alfresco.service.cmr.model.FileFolderServiceType;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Lock Node Event Class
 * 
 * @author gkspencer
 */
public class LockNodeEvent extends NodeEvent {

	// Before and after lock types
	
	private String m_lockBefore;
	private String m_lockAfter;
	
	/**
	 * Class constructor
	 * 
	 * @param fType FileFolderServiceTtype
	 * @param nodeRef NodeRef
	 * @param lockBefore String
	 * @param lockAfter String
	 */
	public LockNodeEvent( FileFolderServiceType fType, NodeRef nodeRef, String lockBefore, String lockAfter) {
		super( fType, nodeRef);

		m_lockAfter  = lockAfter;
		m_lockBefore = lockBefore;
	}
	
	/**
	 * Return the previous type
	 * 
	 *  @return String
	 */
	public final String getBeforeLockType() {
		return m_lockBefore;
	}
	
	/**
	 * Return the new lock type
	 * 
	 * @return String
	 */
	public final String getAfterLockType() {
		return m_lockAfter;
	}

	/**
	 * Return the node event as a string
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("[Lock:fType=");
		str.append(getFileType());
		str.append(",nodeRef=");
		str.append(getNodeRef());
		str.append(",lockBefore=");
		str.append(getBeforeLockType());
		str.append(",lockAfter=");
		str.append(getAfterLockType());
		str.append("]");
		
		return str.toString();
	}
}
