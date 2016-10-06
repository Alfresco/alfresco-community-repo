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

package org.alfresco.rest.api.model;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Concrete class carrying general information for an RM node
 * 
 * @author Ana Bozianu
 * @since 2.6
 */
public class RMNode extends Node
{
    protected Boolean isCategory;
    protected Boolean isRecordFolder;
    protected Boolean hasRetentionSchedule;

    public RMNode(NodeRef nodeRef, NodeRef parentNodeRef, Map<QName, Serializable> nodeProps, Map<String, UserInfo> mapUserInfo, ServiceRegistry sr)
    {
        super(nodeRef, parentNodeRef, nodeProps, mapUserInfo, sr);

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

    public Boolean getHasRetentionSchedule()
    {
        return hasRetentionSchedule;
    }

    public void setIsCategory(Boolean isCategory)
    {
        this.isCategory = isCategory;
    }

    public void setIsRecordFolder(Boolean isRecordFolder)
    {
        this.isRecordFolder = isRecordFolder;
    }

    public void setHasRetentionSchedule(Boolean hasRetentionSchedule)
    {
        this.hasRetentionSchedule = hasRetentionSchedule;
    }
}
