/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.filesys.alfresco;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Desktop Target Class
 * 
 * <p>Contains the details of a target file/folder/node for a desktop action.
 * 
 * @author gkspencer
 */
public class DesktopTarget {

    // Desktop target types
    
    public static final int TargetFile			= 0;
    public static final int TargetFolder		= 1;
    public static final int TargetCopiedFile	= 2;
    public static final int TargetCopiedFolder	= 3;
    public static final int TargetNodeRef		= 4;
    
	// Target type
	
	private int m_type;
	
	// Target path/id
	
	private String m_target;
	
	// Associated noderef
	
	private NodeRef m_noderef;
	
	/**
	 * class constructor
	 * 
	 * @param typ int
	 * @param path String
	 */
	public DesktopTarget(int typ, String path)
	{
		m_type = typ;
		m_target = path;
	}
	
	/**
	 * Return the target type
	 * 
	 * @return int
	 */
	public final int isType()
	{
		return m_type;
	}
	
	/**
	 * Return the target path/id
	 * 
	 * @return String
	 */
	public final String getTarget()
	{
		return m_target;
	}

	/**
	 * Check if the associated node is valid
	 * 
	 * @return boolean
	 */
	public final boolean hasNodeRef()
	{
		return m_noderef != null ? true : false;
	}
	
	/**
	 * Return the associated node
	 * 
	 * @return NodeRef
	 */
	public final NodeRef getNode()
	{
		return m_noderef;
	}
	
	/**
	 * Return the target type as a string
	 * 
	 * @return String
	 */
	public final String getTypeAsString()
	{
		String str = null;
		
		switch( isType())
		{
		case TargetFile:
			str = "File";
			break;
		case TargetFolder:
			str = "Folder";
			break;
		case TargetCopiedFile:
			str = "File Copy";
			break;
		case TargetCopiedFolder:
			str = "Folder Copy";
			break;
		case TargetNodeRef:
			str = "NodeRef";
			break;
		}
		
		return str;
	}
	
	/**
	 * Set the associated node
	 * 
	 * @param node NodeRef
	 */
	public final void setNode(NodeRef node)
	{
		m_noderef = node;
	}
	
	/**
	 * Return the desktop target as a string
	 * 
	 * @return String
	 */
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		
		str.append("[");
		str.append(getTypeAsString());
		str.append(":");
		str.append(getTarget());
		
		if ( hasNodeRef())
		{
			str.append(":");
			str.append(getNode());
		}
		str.append("]");
		
		return str.toString();
	}
}
