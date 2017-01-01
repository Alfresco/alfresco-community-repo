/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rm.rest.api.model;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Concrete class carrying general information for a fileplan component node
 *
 * @author Ana Bozianu
 * @since 2.6
 */
public class FileplanComponentNode extends Node
{
    protected Boolean isCategory;
    protected Boolean isRecordFolder;

    public FileplanComponentNode(NodeRef nodeRef, NodeRef parentNodeRef, Map<QName, Serializable> nodeProps, Map<String, UserInfo> mapUserInfo, ServiceRegistry sr)
    {
        super(nodeRef, parentNodeRef, nodeProps, mapUserInfo, sr);
        defineType();
    }

    public FileplanComponentNode(Node node)
    {
        this.nodeRef = node.getNodeRef();
        this.name = node.getName();
        this.createdAt = node.getCreatedAt();
        this.modifiedAt = node.getModifiedAt();
        this.createdByUser = node.getCreatedByUser();
        this.modifiedByUser = node.getModifiedByUser();
        this.archivedAt = node.getArchivedAt();
        this.archivedByUser = node.getArchivedByUser();
        this.parentNodeRef = node.getParentId();
        this.pathInfo = node.getPath();
        this.prefixTypeQName = node.getNodeType();
        this.relativePath = node.getRelativePath();
        this.secondaryChildren = node.getSecondaryChildren();
        this.targets = node.getTargets();
        this.aspectNames = node.getAspectNames();
        this.properties =node.getProperties();
        this.allowableOperations = node.getAllowableOperations();
        this.contentInfo = node.getContent();
        this.description = node.getDescription();
        defineType();
    }

    protected void defineType()
    {
        isCategory = false;
        isRecordFolder = false;
        isFile = false;
    }

    public Boolean getIsCategory()
    {
        return isCategory;
    }

    public Boolean getIsRecordFolder()
    {
        return isRecordFolder;
    }

    public void setIsCategory(Boolean isCategory)
    {
        this.isCategory = isCategory;
    }

    public void setIsRecordFolder(Boolean isRecordFolder)
    {
        this.isRecordFolder = isRecordFolder;
    }
}
