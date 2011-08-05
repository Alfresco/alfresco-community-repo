/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.node.getchildren;

import java.util.List;

import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.repo.domain.node.NodePropertyEntity;

/**
 * Filterable/Sortable Node Entity
 *
 * Can be optionally filtered/sorted by (up to) three properties - note: sort properties are applied in order
 * 
 * @author jan
 * @since 4.0
 */
public class FilterSortNodeEntity
{
    private Long id; // node id
    
    private NodeEntity node;
    private NodePropertyEntity prop1;
    private NodePropertyEntity prop2;
    private NodePropertyEntity prop3;
    
    // Supplemental query-related parameters
    private Long parentNodeId;
    private Long prop1qnameId;
    private Long prop2qnameId;
    private Long prop3qnameId;
    private List<Long> childNodeTypeQNameIds;
    private String pattern;
    private Long namePropertyQNameId;
    private boolean auditableProps;
    private boolean nodeType;
    
    /**
     * Default constructor
     */
    public FilterSortNodeEntity()
    {
        auditableProps = false;
    }
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public String getPattern()
	{
		return pattern;
	}

	protected String escape(String s, char escapeChar)
	{
		StringBuilder sb = new StringBuilder();
		int idx = -1;
		int offset = 0;
		do
		{
			idx = s.indexOf(escapeChar, offset);
			if(idx != -1)
			{
				sb.append(s.substring(offset, idx));
				sb.append("\\");
				sb.append(escapeChar);
				offset = idx + 1;
			}
		}
		while(idx != -1);
		sb.append(s.substring(offset));
		return sb.toString();
	}
	
	public void setPattern(String pattern)
	{
		if(pattern != null)
		{
			// escape the '%' character with '\' (standard SQL escape character)
			pattern = escape(pattern, '%');
			// replace the wildcard character '*' with the one used in database queries i.e. '%'
			this.pattern = pattern.replace('*', '%');
		}
	}

	public Long getNamePropertyQNameId()
	{
		return namePropertyQNameId;
	}

	public void setNamePropertyQNameId(Long namePropertyQNameId)
	{
		this.namePropertyQNameId = namePropertyQNameId;
	}

	public NodePropertyEntity getProp1()
    {
        return prop1;
    }
    
    public void setProp1(NodePropertyEntity prop1)
    {
        this.prop1 = prop1;
    }
    
    public NodePropertyEntity getProp2()
    {
        return prop2;
    }
    
    public void setProp2(NodePropertyEntity prop2)
    {
        this.prop2 = prop2;
    }
    
    public NodePropertyEntity getProp3()
    {
        return prop3;
    }
    
    public void setProp3(NodePropertyEntity prop3)
    {
        this.prop3 = prop3;
    }
    
    public NodeEntity getNode()
    {
        return node;
    }
    
    public void setNode(NodeEntity childNode)
    {
        this.node = childNode;
    }
    
    // Supplemental query-related parameters
    
    public Long getParentNodeId()
    {
        return parentNodeId;
    }
    
    public void setParentNodeId(Long parentNodeId)
    {
        this.parentNodeId = parentNodeId;
    }
    
    public Long getProp1qnameId()
    {
        return prop1qnameId;
    }
    
    public void setProp1qnameId(Long prop1qnameId)
    {
        this.prop1qnameId = prop1qnameId;
    }
    
    public Long getProp2qnameId()
    {
        return prop2qnameId;
    }
    
    public void setProp2qnameId(Long prop2qnameId)
    {
        this.prop2qnameId = prop2qnameId;
    }
    
    public Long getProp3qnameId()
    {
        return prop3qnameId;
    }
    
    public void setProp3qnameId(Long prop3qnameId)
    {
        this.prop3qnameId = prop3qnameId;
    }
    
    public List<Long> getChildNodeTypeQNameIds()
    {
        return childNodeTypeQNameIds;
    }
    
    public void setChildNodeTypeQNameIds(List<Long> childNodeTypeQNameIds)
    {
        this.childNodeTypeQNameIds = childNodeTypeQNameIds;
    }
    
    public boolean isAuditableProps()
    {
        return auditableProps;
    }
    
    public void setAuditableProps(boolean auditableProps)
    {
        this.auditableProps = auditableProps;
    }
    
    public boolean isNodeType()
    {
        return nodeType;
    }
    
    public void setNodeType(boolean nodeType)
    {
        this.nodeType = nodeType;
    }
}