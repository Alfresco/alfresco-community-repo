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

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

/**
 * Handles single Folder JSON responses
 * Example:
      "createdAt": "2016-10-03T09:48:52.385+0000",
      "sizeInBytes": 19,
      "versionLabel": "1.0",
      "createdBy": "admin",
      "modifiedAt": "2016-10-03T09:48:52.385+0000",
      "name": "file-OUQAyqdsNi.txt",
      "guid": "00f6b250-a841-4ff6-b660-5b45cee30497",
      "modifiedBy": "admin",
      "mimeType": "text/plain",
      "id": "00f6b250-a841-4ff6-b660-5b45cee30497"
 */

public class RestFileModel extends TestModel implements IRestModel<RestFileModel>
{
    @JsonProperty(value = "entry")
    RestFileModel model;

    @Override
    public RestFileModel onModel()
    {
        return model;
    }

    @JsonProperty(required = true)
    protected String createdAt;

    @JsonProperty(required = true)
    protected String sizeInBytes;

    @JsonProperty(required = true)
    protected String versionLabel;

    @JsonProperty(required = true)
    protected String createdBy;

    @JsonProperty(required = true)
    protected String modifiedAt;

    @JsonProperty(required = true)
    protected String name;

    @JsonProperty(required = true)
    protected String guid;

    @JsonProperty(required = true)
    protected String modifiedBy;

    @JsonProperty(required = true)
    protected String mimeType;

    @JsonProperty(required = true)
    protected String id;

    @JsonProperty(required = false)
    RestPathModel path;

    private boolean isFile;
    private boolean isFolder;
    private RestByUserModel createdByUser;
    private RestByUserModel modifiedByUser;
    private RestContentModel content;
    private String parentId;

    public RestFileModel()
    {
    }

    public String getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

    public String getSizeInBytes()
    {
        return sizeInBytes;
    }

    public void setSizeInBytes(String sizeInBytes)
    {
        this.sizeInBytes = sizeInBytes;
    }

    public String getVersionLabel()
    {
        return versionLabel;
    }

    public void setVersionLabel(String versionLabel)
    {
        this.versionLabel = versionLabel;
    }

    public String getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(String createdBy)
    {
        this.createdBy = createdBy;
    }

    public String getModifiedBy()
    {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy)
    {
        this.modifiedBy = modifiedBy;
    }

    public String getModifiedAt()
    {
        return modifiedAt;
    }

    public void setModifiedAt(String modifiedAt)
    {
        this.modifiedAt = modifiedAt;
    }

    public String getGuid()
    {
        return guid;
    }

    public void setGuid(String guid)
    {
        this.guid = guid;
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

    public String getMimeType()
    {
        return mimeType;
    }

    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    public boolean getIsFile()
    {
        return isFile;
    }

    public void setIsFile(boolean isFile)
    {
        this.isFile = isFile;
    }

    public RestByUserModel getCreatedByUser()
    {
        return createdByUser;
    }

    public void setCreatedByUser(RestByUserModel createdByUser)
    {
        this.createdByUser = createdByUser;
    }

    public RestByUserModel getModifiedByUser()
    {
        return modifiedByUser;
    }

    public void setModifiedByUser(RestByUserModel modifiedByUser)
    {
        this.modifiedByUser = modifiedByUser;
    }

    public RestContentModel getContent()
    {
        return content;
    }

    public void setContent(RestContentModel content)
    {
        this.content = content;
    }

    public String getParentId()
    {
        return parentId;
    }

    public void setParentId(String parentId)
    {
        this.parentId = parentId;
    }

    public boolean getIsFolder()
    {
        return isFolder;
    }

    public void setIsFolder(boolean isFolder)
    {
        this.isFolder = isFolder;
    }

    public RestPathModel getPath()
    {
        return path;
    }

    public void setPath(RestPathModel path)
    {
        this.path = path;
    }
}
