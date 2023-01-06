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
 * 
 "entry": {
          "createdAt": "2016-10-13T11:21:34.621+0000",
          "size": 19,
          "createdBy": "admin",
          "modifiedAt": "2016-10-13T11:21:38.338+0000",
          "name": "file-yCQFYpLniWAzkcR.txt",
          "modifiedBy": "User-cchKFZoNIAfZXXn",
          "id": "ffb7178f-fc11-41c9-8c40-df6523ad917f",
          "mimeType": "text/plain"
        }
 *
 */
public class RestItemModel extends TestModel implements IRestModel<RestItemModel>
{
    @JsonProperty(value = "entry")
    RestItemModel model;
 
    @JsonProperty(required = true)
    private String createdAt;
    private int size;
    private String createdBy;
    private String modifiedAt;
    private String name;
    private String modifiedBy;
    private String id;
    private String mimeType;
    
    @Override
    public RestItemModel onModel()
    {
        return model;
    }

    public String getCreatedAt()
    {
        return createdAt;
    }
    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }
    public int getSize()
    {
        return size;
    }
    public void setSize(int size)
    {
        this.size = size;
    }
    public String getCreatedBy()
    {
        return createdBy;
    }
    public void setCreatedBy(String createdBy)
    {
        this.createdBy = createdBy;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getModifiedBy()
    {
        return modifiedBy;
    }
    public void setModifiedBy(String modifiedBy)
    {
        this.modifiedBy = modifiedBy;
    }
    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }
    public String getMimeType()
    {
        return mimeType;
    }
    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }
    public String getModifiedAt()
    {
        return modifiedAt;
    }
    public void setModifiedAt(String modifiedAt)
    {
        this.modifiedAt = modifiedAt;
    }
}    
