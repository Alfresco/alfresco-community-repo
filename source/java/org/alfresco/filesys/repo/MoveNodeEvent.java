/*
 * Copyright (C) 2006-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.filesys.repo;

import org.alfresco.service.cmr.model.FileFolderServiceType;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Move Node Event Class
 * 
 * @author gkspencer
 */
public class MoveNodeEvent extends NodeEvent {

	// Moved node path and destination node
	
	private String m_path;
	
	private NodeRef m_moveToNode;
	
	/**
	 * Class constructor
	 * 
	 * @param fType FileFolderServiceTtype
	 * @param nodeRef NodeRef
	 * @param fromPath String
	 * @param toNodeRef NodeRef
	 */
	public MoveNodeEvent( FileFolderServiceType fType, NodeRef nodeRef, String fromPath, NodeRef toNodeRef) {
		super( fType, nodeRef);
		
		m_path = fromPath;
		m_moveToNode = toNodeRef;
	}
	
	/**
	 * Return the relative path of the target node
	 * 
	 * @return String
	 */
	public final String getPath() {
		return m_path;
	}
	
	/**
	 * Return the target node for a move
	 * 
	 * @return NodeRef
	 */
	public final NodeRef getMoveToNodeRef() {
		return m_moveToNode;
	}

	/**
	 * Return the node event as a string
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("[Move:fType=");
		str.append(getFileType());
		str.append(",nodeRef=");
		str.append(getNodeRef());
		str.append(",path=");
		str.append(getPath());
		str.append(",toNodeRef=");
		str.append(getMoveToNodeRef());
		str.append("]");
		
		return str.toString();
	}
}
