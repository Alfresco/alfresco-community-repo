/*
 * Copyright (C) 2006-2010 Alfresco Software Limited.
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.filesys.repo;

import org.alfresco.service.cmr.model.FileFolderServiceType;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Node Event Base Class
 *
 * <p>Contains the details of a file/folder node event to be processed by a node monitor thread.
 * 
 * @author gkspencer
 */
public class NodeEvent {

	// Target node
	
	private NodeRef m_nodeRef;

	// File/folder node type
	
	private FileFolderServiceType m_fileType;
	
	/**
	 * Class constructor
	 * 
	 * @param fType FileFolderServiceTtype
	 * @param nodeRef NodeRef
	 */
	protected NodeEvent( FileFolderServiceType fType, NodeRef nodeRef) {
		m_fileType = fType;
		m_nodeRef = nodeRef;
	}
	
	/**
	 * Return the target node
	 * 
	 * @return NodeRef
	 */
	public final NodeRef getNodeRef() {
		return m_nodeRef;
	}

	/**
	 * Return the node file/folder type
	 * 
	 * @return FileFolderServiceType
	 */
	public final FileFolderServiceType getFileType() {
		return m_fileType;
	}
	
}
