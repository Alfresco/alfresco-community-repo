/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.api.model.Assoc;
import org.alfresco.rest.api.model.PathInfo;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Abstract base class carrying general information for an RM node
 *
 * @author Ana Bozianu
 * @since 2.6
 */
public abstract class RMNode
{
    public static final String PARAM_ID = "id";
    public static final String PARAM_PARENT_ID = "parentId";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_NODE_TYPE = "nodeType";
    public static final String PARAM_CREATED_AT = "createdAt";
    public static final String PARAM_MODIFIED_AT = "modifiedAt";
    public static final String PARAM_CREATED_BY_USER = "createdByUser";
    public static final String PARAM_MODIFIED_BY_USER = "modifiedByUser";

    public static final String PARAM_ASPECT_NAMES = "aspectNames";
    public static final String PARAM_PROPERTIES = "properties";
    public static final String PARAM_PATH = "path";
    public static final String PARAM_ALLOWABLE_OPERATIONS = "allowableOperations";
    public static final String PARAM_AUTO_RENAME = "autoRename";
    
    public static final String PARAM_ISPRIMARY = "isPrimary";
    
    public static final String PARAM_INCLUDE_SUBTYPES = "INCLUDESUBTYPES";
    
    public static final String PARAM_HAS_RETENTION_SCHEDULE = "hasRetentionSchedule";
    public static final String PARAM_IS_CLOSED = "isClosed";
    public static final String PARAM_INCLUDE_ASSOCIATION = "association";
    
    public static final String FILE_PLAN_TYPE = "rma:filePlan";
    public static final String RECORD_CATEGORY_TYPE = "rma:recordCategory";
    public static final String RECORD_FOLDER_TYPE = "rma:recordFolder";
    public static final String RECORD_TYPE = "rma:record"; // generic record type
    public static final String UNFILED_RECORD_FOLDER_TYPE = "rma:unfiledRecordFolder";
    public static final String TRANSFER_TYPE = "rma:transfer";
    public static final String TRANSFER_CONTAINER_TYPE = "rma:transferContainer";
    public static final String UNFILED_CONTAINER_TYPE = "rma:unfiledRecordContainer";
    public static final String FOLDER_TYPE = "cm:folder";
    public static final String CONTENT_TYPE = "cm:content";
    public static final String NON_ELECTRONIC_RECORD_TYPE = "rma:nonElectronicDocument";

    // required properties
    protected NodeRef nodeRef;
    protected NodeRef parentNodeRef;
    protected String name;
    protected String nodeType;

    protected Date createdAt;
    protected Date modifiedAt;
    protected UserInfo createdByUser;
    protected UserInfo modifiedByUser;

    // optional properties
    protected List<String> aspectNames;
    protected Map<String, Object> properties;
    protected PathInfo path;
    protected List<String> allowableOperations;
    protected Assoc association;

    public RMNode()
    {

    }

    @JsonProperty ("id")
    @UniqueId
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }

    public NodeRef getParentId()
    {
        return parentNodeRef;
    }

    public void setParentId(NodeRef parentNodeRef)
    {
        this.parentNodeRef = parentNodeRef;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getNodeType()
    {
        return nodeType;
    }

    public void setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt)
    {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt()
    {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt)
    {
        this.modifiedAt = modifiedAt;
    }

    public UserInfo getCreatedByUser()
    {
        return createdByUser;
    }

    public void setCreatedByUser(UserInfo createdByUser)
    {
        this.createdByUser = createdByUser;
    }

    public UserInfo getModifiedByUser()
    {
        return modifiedByUser;
    }

    public void setModifiedByUser(UserInfo modifiedByUser)
    {
        this.modifiedByUser = modifiedByUser;
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

    public PathInfo getPath()
    {
        return path;
    }

    public void setPath(PathInfo path)
    {
        this.path = path;
    }

    public List<String> getAllowableOperations()
    {
        return allowableOperations;
    }

    public void setAllowableOperations(List<String> allowableOperations)
    {
        this.allowableOperations = allowableOperations;
    }

    public Assoc getAssociation()
    {
        return association;
    }

    public void setAssociation(Assoc association)
    {
        this.association = association;
    }

}
