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
package org.alfresco.rest.rm.community.model.fileplancomponents;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.ALLOWABLE_OPERATIONS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.IS_CLOSED;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PROPERTIES;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO for file plan component
 *
 * @author Tuna Aksoy
 * @author Rodica Sutu
 * @since 2.6
 */
public class FilePlanComponent
{
    @JsonProperty (required = true)
    private String id;

    @JsonProperty (required = true)
    private String parentId;

    @JsonProperty (required = true)
    private String name;

    @JsonProperty (required = true)
    private String nodeType;

    @JsonProperty (required = true)
    private Boolean isCategory;

    @JsonProperty (required = true)
    private Boolean isRecordFolder;

    @JsonProperty (required = true)
    private Boolean isFile;

    @JsonProperty
    private Boolean hasRetentionSchedule;

    @JsonProperty(value = IS_CLOSED)
    private Boolean isClosed;

    @JsonProperty
    private Boolean isCompleted;

    @JsonProperty (required = true)
    private List<String> aspectNames;

    @JsonProperty (required = true)
    private FilePlanComponentUserInfo createdByUser;

    @JsonProperty(value = PROPERTIES, required = true)
    private FilePlanComponentProperties properties;

    @JsonProperty (value = ALLOWABLE_OPERATIONS)
    private List<String> allowableOperations;
    
    private FilePlanComponentPath path;

    @JsonProperty (required = true)
    private String modifiedAt;

    @JsonProperty (required = true)
    private String createdAt;

    @JsonProperty (required = true)
    private FilePlanComponentUserInfo modifiedByUser;


    /**Helper constructor for creating the file plan component using
     *
     * @param name File Plan Component name
     * @param nodeType File Plan Component node type
     * @param properties File Plan Component properties
     */
    public FilePlanComponent(String name, String nodeType, FilePlanComponentProperties properties)
    {
        this.name = name;
        this.nodeType = nodeType;
        this.properties = properties;
    }

    /**
     * Helper constructor to create empty  file plan component
     */
    public FilePlanComponent() { }

    /**
     * Helper constructor for creating the file plan component using
     *
     * @param name       File Plan Component name
     */
    public FilePlanComponent(String name)
    {
        this.name = name;
    }

    /**
     * Helper constructor for creating the file plan component using
     *
     * @param name       File Plan Component name
     * @param properties File Plan Component properties
     */
    public FilePlanComponent(String name, FilePlanComponentProperties properties)
    {
        this.name = name;
        this.properties = properties;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the parentId
     */
    public String getParentId()
    {
        return this.parentId;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(String parentId)
    {
        this.parentId = parentId;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the nodeType
     */
    public String getNodeType()
    {
        return this.nodeType;
    }

    /**
     * @param nodeType the nodeType to set
     */
    public void setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
    }

    /**
     * @return the isCategory
     */
    public Boolean isCategory()
    {
        return this.isCategory;
    }

    /**
     * @param isCategory the isCategory to set
     */
    public void setCategory(Boolean isCategory)
    {
        this.isCategory = isCategory;
    }

    /**
     * @return the isRecordFolder
     */
    public Boolean isRecordFolder()
    {
        return this.isRecordFolder;
    }

    /**
     * @param isRecordFolder the isRecordFolder to set
     */
    public void setRecordFolder(Boolean isRecordFolder)
    {
        this.isRecordFolder = isRecordFolder;
    }

    /**
     * @return the isFile
     */
    public Boolean isFile()
    {
        return this.isFile;
    }

    /**
     * @param isFile the isFile to set
     */
    public void setFile(Boolean isFile)
    {
        this.isFile = isFile;
    }

    /**
     * @return the hasRetentionSchedule
     */
    public Boolean hasRetentionSchedule()
    {
        return this.hasRetentionSchedule;
    }

    /**
     * @param hasRetentionSchedule the hasRetentionSchedule to set
     */
    public void setHasRetentionSchedule(Boolean hasRetentionSchedule)
    {
        this.hasRetentionSchedule = hasRetentionSchedule;
    }

    /**
     * @return the properties
     */
    public FilePlanComponentProperties getProperties()
    {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(FilePlanComponentProperties properties)
    {
        this.properties = properties;
    }

    /**
     * @return the aspectNames
     */
    public List<String> getAspectNames()
    {
        return this.aspectNames;
    }

    /**
     * @param aspectNames the aspectNames to set
     */
    public void setAspectNames(List<String> aspectNames)
    {
        this.aspectNames = aspectNames;
    }

    /**
     * @return the createdByUser
     */
    public FilePlanComponentUserInfo getCreatedByUser()
    {
        return this.createdByUser;
    }

    /**
     * @param createdByUser the createdByUser to set
     */
    public void setCreatedByUser(FilePlanComponentUserInfo createdByUser)
    {
        this.createdByUser = createdByUser;
    }

    /**
     * @return the allowableOperations
     */
    public List<String> getAllowableOperations()
    {
        return this.allowableOperations;
    }

    /**
     * @return the path
     */
    public FilePlanComponentPath getPath()
    {
        return this.path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(FilePlanComponentPath path)
    {
        this.path = path;
    }

    /**
     * @param modifiedAt the modifiedAt to set
     */
    public void setModifiedAt(String modifiedAt)
    {
        this.modifiedAt = modifiedAt;
    }

    /**
     * @param createdAt the createdAt to set
     */
    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

    /**
     * @param modifiedByUser the modifiedByUser to set
     */
    public void setModifiedByUser(FilePlanComponentUserInfo modifiedByUser)
    {
        this.modifiedByUser = modifiedByUser;
    }

    /**
     * @return the modifiedAt
     */
    public String getModifiedAt()
    {
        return this.modifiedAt;
    }

    /**
     * @return the createdAt
     */
    public String getCreatedAt()
    {
        return this.createdAt;
    }

    /**
     * @return the modifiedByUser
     */
    public FilePlanComponentUserInfo getModifiedByUser()
    {
        return this.modifiedByUser;
    }

    /**
     * @return the isClosed
     */
    public Boolean isClosed()
    {
        return this.isClosed;
    }

    /**
     * @param closed the isClosed to set
     */
    public void setClosed(Boolean closed)
    {
        this.isClosed = closed;
    }

    /**
     * @return the isCompleted
     */
    public Boolean isCompleted()
    {
        return this.isCompleted;
    }

    /**
     * @param completed the isCompleted to set
     */
    public void setCompleted(Boolean completed)
    {
        this.isCompleted = completed;
    }
}
