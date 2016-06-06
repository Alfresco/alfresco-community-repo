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

package org.alfresco.service.cmr.activities;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Basic Activity information for use when posting Activities.
 *
 * @author Gethin James
 */
public class ActivityInfo
{
    private final NodeRef nodeRef;
    private final String parentPath;
    private final NodeRef parentNodeRef;
    private final String siteId;
    private final String fileName;
    private final boolean isFolder;
    private final FileInfo fileInfo;

    public ActivityInfo(NodeRef nodeRef, String parentPath, NodeRef parentNodeRef,
                        String siteId, String fileName, boolean isFolder)
    {
        super();
        this.nodeRef = nodeRef;
        this.parentPath = parentPath;
        this.parentNodeRef = parentNodeRef;
        this.siteId = siteId;
        this.fileName = fileName;
        this.isFolder = isFolder;
        this.fileInfo = null;
    }

    public ActivityInfo(String parentPath, NodeRef parentNodeRef, String siteId, FileInfo fileInfo)
    {
        super();
        this.nodeRef = fileInfo.getNodeRef();
        this.parentPath = parentPath;
        this.parentNodeRef = parentNodeRef;
        this.siteId = siteId;
        this.fileName = fileInfo.getName();
        this.isFolder = fileInfo.isFolder();
        this.fileInfo = fileInfo;
    }

    public FileInfo getFileInfo()
    {
        return fileInfo;
    }

    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public String getParentPath()
    {
        return parentPath;
    }

    public NodeRef getParentNodeRef()
    {
        return parentNodeRef;
    }

    public String getSiteId()
    {
        return siteId;
    }

    public String getFileName()
    {
        return fileName;
    }

    public boolean isFolder()
    {
        return isFolder;
    }
}
