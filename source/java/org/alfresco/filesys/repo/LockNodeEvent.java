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
 * Lock Node Event Class
 * 
 * @author gkspencer
 */
public class LockNodeEvent extends NodeEvent {

	// Before and after lock types
	
	private String m_lockBefore;
	private String m_lockAfter;
    private String relPath;
    private String name;

	
	/**
	 * Class constructor
	 * 
	 * @param fType FileFolderServiceTtype
	 * @param nodeRef NodeRef
	 * @param lockBefore String
	 * @param lockAfter String
	 */
	public LockNodeEvent( FileFolderServiceType fType, NodeRef nodeRef, String relPath, String name, String lockBefore, String lockAfter) {
		super( fType, nodeRef);

		m_lockAfter  = lockAfter;
		m_lockBefore = lockBefore;
	    this.setRelPath(relPath);
	    this.setName(name);
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
	
    public void setRelPath(String relPath)
    {
        this.relPath = relPath;
    }

    public String getRelPath()
    {
        return relPath;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
