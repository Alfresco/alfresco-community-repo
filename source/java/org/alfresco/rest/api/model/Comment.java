/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.rest.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.rest.api.people.PeopleEntityResource;
import org.alfresco.rest.framework.resource.EmbeddedEntityResource;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.namespace.QName;

/**
 * A representation of a Comment in the system.
 *
 * @author Gethin James
 * @author steveglover
 * 
 */
public class Comment
{
    public static final QName PROP_COMMENT_CONTENT = QName.createQName("RestApi", "commentContent");

	private String id;
    private String title;
    private String content;
    private Date createdAt;
    private String createdBy;
    private Date modifiedAt;
    private String modifiedBy;
    private Boolean edited;

    // permissions
    private boolean canEdit;
    private boolean canDelete;
    
    public Comment()
    {
        super();
    }

    public Comment(String id, Map<QName, Serializable> nodeProps, boolean canEdit, boolean canDelete)
    {
    	if(id == null)
    	{
    		throw new IllegalArgumentException();
    	}

    	this.id = id;
        mapProperties(nodeProps);
        this.canEdit = canEdit;
        this.canDelete = canDelete;
    }

    @UniqueId
    public String getId()
    {
		return id;
	}

    public void setId(String id)
    {
    	this.id = id;
    }
    
    public boolean getCanEdit()
    {
		return canEdit;
	}

	public boolean getCanDelete()
	{
		return canDelete;
	}

	public String getTitle()
    {
        return this.title;
    }
    
    public String getContent()
    {
        return this.content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Date getCreatedAt()
    {
		return createdAt;
	}

    @EmbeddedEntityResource(propertyName = "createdBy", entityResource = PeopleEntityResource.class)
	public String getCreatedBy()
	{
		return createdBy;
	}

    @EmbeddedEntityResource(propertyName = "modifiedBy", entityResource = PeopleEntityResource.class)
    public String getModifiedBy()
    {
		return modifiedBy;
	}
    
	public Date getModifiedAt()
	{
		return modifiedAt;
	}

	public Boolean getEdited()
    {
		return edited;
	}

    protected void mapProperties(Map<QName, Serializable> nodeProps)
    {
		String propTitle = (String) nodeProps.get(ContentModel.PROP_TITLE);
		if (propTitle != null)
		{
			title = propTitle;
		}

		this.modifiedAt = (Date)nodeProps.get(ContentModel.PROP_MODIFIED);
		this.createdAt = (Date)nodeProps.get(ContentModel.PROP_CREATED);
		if(modifiedAt != null && createdAt != null)
		{
			long diff = modifiedAt.getTime() - createdAt.getTime();
			this.edited = Boolean.valueOf(diff >= 100); // logic is consistent with existing (Javascript) comments implementation
		}

		this.createdBy = (String)nodeProps.get(ContentModel.PROP_CREATOR);
		this.modifiedBy = (String)nodeProps.get(ContentModel.PROP_MODIFIER);

		this.content = (String)nodeProps.get(PROP_COMMENT_CONTENT);
		nodeProps.remove(PROP_COMMENT_CONTENT);
    }

	@Override
	public String toString()
	{
		return "Comment [id=" + id + ", title=" + title
				+ ", content=" + content + ", createdAt=" + createdAt
				+ ", createdBy=" + createdBy + ", modifiedAt=" + modifiedAt
				+ ", modifiedBy=" + modifiedBy + ", edited=" + edited + "]";
	}

}
