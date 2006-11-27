/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
