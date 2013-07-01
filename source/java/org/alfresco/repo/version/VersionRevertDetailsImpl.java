/*
 * Copyright (C) 2013-2013 Alfresco Software Limited.
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
package org.alfresco.repo.version;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Implementation of VersionRevertDetails
 * @author mrogers
 * @since 4.2
 */
/*package*/class VersionRevertDetailsImpl implements VersionRevertDetails
{
	private NodeRef nodeRef;
	private QName nodeType;
	
	
	Map<String, Serializable> revertedProperties;

	public void setNodeRef(NodeRef nodeRef) 
	{
		this.nodeRef = nodeRef;
	}
	
	@Override
	public NodeRef getNodeRef() 
	{
		return nodeRef;
	}

	public void setNodeType(QName nodeType) {
		this.nodeType = nodeType;
	}

	@Override
	public QName getNodeType() {
		return nodeType;
	}

}
