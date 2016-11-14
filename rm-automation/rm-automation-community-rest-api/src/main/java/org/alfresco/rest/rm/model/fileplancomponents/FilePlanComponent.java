/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.rm.model.fileplancomponents;

import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.ALLOWABLE_OPERATIONS;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.IS_CLOSED;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.PROPERTIES;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO for file plan component
 *
 * @author Tuna Aksoy
 * @since 1.0
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
    private boolean isCategory;

    @JsonProperty (required = true)
    private boolean isRecordFolder;

    @JsonProperty (required = true)
    private boolean isFile;

    @JsonProperty
    private boolean hasRetentionSchedule;

    @JsonProperty(value = IS_CLOSED)
    private boolean isClosed;

    @JsonProperty
    private boolean isCompleted;

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
    public boolean isIsCategory()
    {
        return this.isCategory;
    }

    /**
     * @param isCategory the isCategory to set
     */
    public void setCategory(boolean isCategory)
    {
        this.isCategory = isCategory;
    }

    /**
     * @return the isRecordFolder
     */
    public boolean isIsRecordFolder()
    {
        return this.isRecordFolder;
    }

    /**
     * @param isRecordFolder the isRecordFolder to set
     */
    public void setRecordFolder(boolean isRecordFolder)
    {
        this.isRecordFolder = isRecordFolder;
    }

    /**
     * @return the isFile
     */
    public boolean isIsFile()
    {
        return this.isFile;
    }

    /**
     * @param isFile the isFile to set
     */
    public void setFile(boolean isFile)
    {
        this.isFile = isFile;
    }

    /**
     * @return the hasRetentionSchedule
     */
    public boolean isHasRetentionSchedule()
    {
        return this.hasRetentionSchedule;
    }

    /**
     * @param hasRetentionSchedule the hasRetentionSchedule to set
     */
    public void setHasRetentionSchedule(boolean hasRetentionSchedule)
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
    public boolean isClosed()
    {
        return this.isClosed;
    }

    /**
     * @param closed the isClosed to set
     */
    public void setClosed(boolean closed)
    {
        this.isClosed = closed;
    }

    /**
     * @return the isCompleted
     */
    public boolean isCompleted()
    {
        return this.isCompleted;
    }

    /**
     * @param completed the isCompleted to set
     */
    public void setCompleted(boolean completed)
    {
        this.isCompleted = completed;
    }
}
