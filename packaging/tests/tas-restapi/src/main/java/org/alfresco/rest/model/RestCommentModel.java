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

public class RestCommentModel extends TestModel implements IRestModel<RestCommentModel>
{
    @JsonProperty(value = "entry")
    RestCommentModel model;

    @Override
    public RestCommentModel onModel()
    {         
        return model;
    }

    private String createdAt;
    private RestPersonModel createdBy;
    private String edited;
    private String modifiedAt;
    private boolean canEdit;
    private RestPersonModel modifiedBy;
    private boolean canDelete;
    private String id;
    private String content;

    public String getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

    public String getEdited()
    {
        return edited;
    }

    public void setEdited(String edited)
    {
        this.edited = edited;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public RestPersonModel getCreatedBy()
    {
        return createdBy;
    }

    public void RestPersonModel(RestPersonModel createdBy)
    {
        this.createdBy = createdBy;
    }

    public String getModifiedAt()
    {
        return modifiedAt;
    }

    public void setModifiedAt(String modifiedAt)
    {
        this.modifiedAt = modifiedAt;
    }

    public boolean isCanEdit()
    {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit)
    {
        this.canEdit = canEdit;
    }

    public RestPersonModel getModifiedBy()
    {
        return modifiedBy;
    }

    public void setModifiedBy(RestPersonModel modifiedBy)
    {
        this.modifiedBy = modifiedBy;
    }

    public boolean isCanDelete()
    {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete)
    {
        this.canDelete = canDelete;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
