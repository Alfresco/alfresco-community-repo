/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.rest.api.model;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Representation of a folder node.
 * 
 * @author steveglover
 * @author janv
 */
public class Folder extends Node
{
    public Folder()
    {
        super();
    }

    public Folder(NodeRef nodeRef, NodeRef parentNodeRef, Map<QName, Serializable> nodeProps, Map<String, UserInfo> mapUserInfo, ServiceRegistry sr)
    {
        super(nodeRef, parentNodeRef, nodeProps, mapUserInfo, sr);

        setIsFolder(true);
        setIsFile(false);
    }

    @Override
    public ContentInfo getContent()
    {
        return null;
    }

    @Override
    public void setContent(ContentInfo contentInfo)
    {
    }

    @Override
    public String toString()
    {
        return "Folder [nodeRef=" + nodeRef + ", name=" + name + ", title="
                    + title + ", description=" + description + ", createdAt="
                    + createdAt + ", modifiedAt=" + modifiedAt + ", createdBy="
                    + createdBy + ", modifiedBy=" + modifiedBy + "]";
    }
}
