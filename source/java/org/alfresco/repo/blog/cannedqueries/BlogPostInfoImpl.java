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
package org.alfresco.repo.blog.cannedqueries;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.blog.BlogPostInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * A simple data object responsible for holding information relevant to blog post NodeRefs.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class BlogPostInfoImpl implements BlogPostInfo
{
    private NodeRef nodeRef;
    private Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

    BlogPostInfoImpl(NodeRef nodeRef, Map<QName, Serializable> properties)
    {
        this.nodeRef = nodeRef;
        this.properties = properties;
    }
    
    /**
     * @see #getNodeRef()
     * @see NodeRef#equals(Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (this == obj)
        {
            return true;
        }
        else if (obj instanceof BlogPostInfoImpl == false)
        {
            return false;
        }
        BlogPostInfoImpl that = (BlogPostInfoImpl) obj;
        return (this.getNodeRef().equals(that.getNodeRef()));
    }

    /**
     * @see #getNodeRef()
     * @see NodeRef#hashCode()
     */
    @Override
    public int hashCode()
    {
        return getNodeRef().hashCode();
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append(BlogPostInfo.class.getSimpleName())
          .append("[name=").append(getName())
          .append(", nodeRef=").append(nodeRef);
        sb.append("]");
        return sb.toString();
    }
    
    @Override
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
    
    @Override
    public String getName()
    {
        return (String) properties.get(ContentModel.PROP_NAME);
    }
}
