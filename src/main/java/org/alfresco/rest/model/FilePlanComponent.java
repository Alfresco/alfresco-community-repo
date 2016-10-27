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
package org.alfresco.rest.model;

import static org.alfresco.com.FilePlanComponentFields.PROPERTIES;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO for file plan component
 *
 * @author Tuna Aksoy
 * @since 1.0
 */
//FIXME: Once the fields have been added the JsonIgnoreProperties annotation should be removed
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilePlanComponent
{    
    private String id;

    private String parentId;

    private String name;

    private String nodeType;

    private boolean isCategory;

    private boolean isRecordFolder;

    private boolean isFile;

    private boolean hasRetentionSchedule;

    private List<String> aspectNames;
    
    @JsonProperty(PROPERTIES)
    private FilePlanComponentProperties properties;
    
    private FilePlanComponentCreatedByUser createdByUser;

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
    public FilePlanComponentCreatedByUser getCreatedByUser()
    {
        return this.createdByUser;
    }

    /**
     * @param createdByUser the createdByUser to set
     */
    public void setCreatedByUser(FilePlanComponentCreatedByUser createdByUser)
    {
        this.createdByUser = createdByUser;
    }
}
