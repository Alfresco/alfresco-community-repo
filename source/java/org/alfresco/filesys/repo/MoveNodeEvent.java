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
 * Move Node Event Class
 * 
 * @author gkspencer
 */
public class MoveNodeEvent extends NodeEvent {

	// Moved node path and destination node
	
	private String fromPath;
	private String toPath;
	
	/**
	 * Class constructor
	 * 
	 * @param fType FileFolderServiceTtype
	 * @param nodeRef NodeRef
	 * @param fromPath String
	 * @param toNodeRef NodeRef
	 */
	public MoveNodeEvent( FileFolderServiceType fType, NodeRef nodeRef, String fromPath, String toPath) {
		super( fType, nodeRef);
		
		this.fromPath = fromPath;
		this.toPath = toPath;
	}
	
	/**
	 * Return the relative path of the target node
	 * 
	 * @return String
	 */
	public final String getFromPath() 
	{
		return fromPath;
	}
	
	public final String getToPath() 
	{
	    return fromPath;
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
		str.append(",fromPath=");
		str.append(getFromPath());
		str.append(",toPath=");
		str.append(getToPath());
		str.append("]");
		
		return str.toString();
	}
}
