/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.rest.api.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.rest.api.model.AssocChild;
import org.alfresco.rest.api.model.AssocTarget;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.PathInfo;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.service.cmr.repository.NodeRef;

public class RMNode extends Node
{
    protected boolean hasRetentionSchedule;

    public boolean isHasRetentionSchedule()
    {
        return hasRetentionSchedule;
    }

    public void setHasRetentionSchedule(boolean hasRetentionSchedule)
    {
        this.hasRetentionSchedule = hasRetentionSchedule;
    }

    public RMNode(Node node)
    {
        this.nodeRef = node.getNodeRef();
        this.name = node.getName();
        this.createdAt = node.getCreatedAt();
        this.modifiedAt = node.getModifiedAt();
        this.createdByUser = node.getCreatedByUser();
        this.modifiedByUser = node.getModifiedByUser();
        this.archivedAt = node.getArchivedAt();
        this.archivedByUser = node.getArchivedByUser();
        this.isFolder = node.getIsFolder();
        this.isFile = node.getIsFile();
        this.isLink = node.getIsLink();
        this.parentNodeRef = node.getParentId();
        this.pathInfo = node.getPath();
        this.relativePath = node.getRelativePath();
        this.secondaryChildren = node.getSecondaryChildren();
        this.targets = node.getTargets();
        this.aspectNames = node.getAspectNames();
        this.properties = node.getProperties();
        this.allowableOperations = node.getAllowableOperations();

        this.hasRetentionSchedule = true;
    }
}
