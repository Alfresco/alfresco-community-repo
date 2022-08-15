/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

/**
 * Created by Andrei Forascu on 30/08/2017.
 */
public class RestNodeAssociationModel extends TestModel implements IRestModel<RestNodeAssociationModel>
{
    @JsonProperty(value = "entry")
    RestNodeAssociationModel model;

    @Override
    public RestNodeAssociationModel onModel()
    {
        return model;
    }

    @JsonProperty(required = true)
    private String id;

    /**
     * The name must not contain spaces or the following special characters: * " < > \ / ? : and |.
     * The character . must not be used at the end of the name.
     */
    @JsonProperty(required = true)
    private String name;

    @JsonProperty(required = true)
    private String nodeType;

    @JsonProperty(required = true)
    private boolean isFolder;

    @JsonProperty(required = true)
    private boolean isFile;

    private boolean isLocked;

    @JsonProperty(required = true)
    private String modifiedAt;

    @JsonProperty(required = true)
    private RestByUserModel modifiedByUser;

    @JsonProperty(required = true)
    private String createdAt;
    
    @JsonProperty
    private String archivedAt;
    
    @JsonProperty
    private RestByUserModel archivedByUser;

    @JsonProperty(required = true)
    private RestByUserModel createdByUser;

    private String parentId;

    private boolean isLink;

    private RestContentModel content;

    private List<String> aspectNames;

    private Object properties;

    private List<String> allowableOperations;

    private Object path;

    private Object permissions;
    
    private RestNodeAssociationTypeModel association;

    public RestNodeAssociationTypeModel getAssociation()
    {
        return association;
    }

    public void setAssociation(RestNodeAssociationTypeModel association)
    {
        this.association = association;
    }

    public RestNodeAssociationModel getModel()
    {
        return model;
    }

    public void setModel(RestNodeAssociationModel model)
    {
        this.model = model;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
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

    public boolean isFolder()
    {
        return isFolder;
    }

    public void setFolder(boolean isFolder)
    {
        this.isFolder = isFolder;
    }

    public boolean isFile()
    {
        return isFile;
    }

    public void setFile(boolean isFile)
    {
        this.isFile = isFile;
    }

    public boolean isLocked()
    {
        return isLocked;
    }

    public void setLocked(boolean isLocked)
    {
        this.isLocked = isLocked;
    }

    public String getModifiedAt()
    {
        return modifiedAt;
    }

    public void setModifiedAt(String modifiedAt)
    {
        this.modifiedAt = modifiedAt;
    }

    public RestByUserModel getModifiedByUser()
    {
        return modifiedByUser;
    }

    public void setModifiedByUser(RestByUserModel modifiedByUser)
    {
        this.modifiedByUser = modifiedByUser;
    }

    public String getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

    public String getArchivedAt()
    {
        return archivedAt;
    }

    public void setArchivedAt(String archivedAt)
    {
        this.archivedAt = archivedAt;
    }

    public RestByUserModel getArchivedByUser()
    {
        return archivedByUser;
    }

    public void setArchivedByUser(RestByUserModel archivedByUser)
    {
        this.archivedByUser = archivedByUser;
    }

    public RestByUserModel getCreatedByUser()
    {
        return createdByUser;
    }

    public void setCreatedByUser(RestByUserModel createdByUser)
    {
        this.createdByUser = createdByUser;
    }

    public String getParentId()
    {
        return parentId;
    }

    public void setParentId(String parentId)
    {
        this.parentId = parentId;
    }

    public boolean isLink()
    {
        return isLink;
    }

    public void setLink(boolean isLink)
    {
        this.isLink = isLink;
    }

    public RestContentModel getContent()
    {
        return content;
    }

    public void setContent(RestContentModel content)
    {
        this.content = content;
    }

    public List<String> getAspectNames()
    {
        return aspectNames;
    }

    public void setAspectNames(List<String> aspectNames)
    {
        this.aspectNames = aspectNames;
    }

    public Object getProperties()
    {
        return properties;
    }

    public void setProperties(Object properties)
    {
        this.properties = properties;
    }

    public List<String> getAllowableOperations()
    {
        return allowableOperations;
    }

    public void setAllowableOperations(List<String> allowableOperations)
    {
        this.allowableOperations = allowableOperations;
    }

    public Object getPath()
    {
        return path;
    }

    public void setPath(Object path)
    {
        this.path = path;
    }

    public Object getPermissions()
    {
        return permissions;
    }

    public void setPermissions(Object permissions)
    {
        this.permissions = permissions;
    }

}
