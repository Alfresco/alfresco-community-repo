/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.rest.api.tests.client.data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Representation of a node (for client tests for File Folder API)
 *
 * @author janv
 */
public class Node
{
    protected String id;
    protected String name;

    protected Date createdAt;
    protected Date modifiedAt;
    protected UserInfo createdByUser;
    protected UserInfo modifiedByUser;

    protected Boolean isFolder;
    protected Boolean isLink;

    protected String parentId;
    protected PathInfo path;
    protected String nodeType;

    protected List<String> aspectNames;

    protected Map<String, Object> properties;

    public Node()
    {
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public Date getModifiedAt()
    {
        return modifiedAt;
    }

    public UserInfo getCreatedByUser()
    {
        return createdByUser;
    }

    public UserInfo getModifiedByUser()
    {
        return modifiedByUser;
    }

    public Boolean getIsFolder()
    {
        return isFolder;
    }

    public void setIsFolder(Boolean folder)
    {
        isFolder = folder;
    }

    public Boolean getIsLink()
    {
        return isLink;
    }

    public void setIsLink(Boolean link)
    {
        isLink = link;
    }

    public String getParentId()
    {
        return parentId;
    }

    public void setParentId(String parentId)
    {
        this.parentId = parentId;
    }

    public PathInfo getPath()
    {
        return path;
    }

    public void setPath(PathInfo path)
    {
        this.path = path;
    }

    public String getNodeType()
    {
        return nodeType;
    }

    public void setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
    }

    public List<String> getAspectNames()
    {
        return aspectNames;
    }

    public void setAspectNames(List<String> aspectNames)
    {
        this.aspectNames = aspectNames;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, Object> properties)
    {
        this.properties = properties;
    }
}