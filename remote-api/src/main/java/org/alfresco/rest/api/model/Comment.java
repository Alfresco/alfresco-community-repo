/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
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
    public static final QName PROP_COMMENT_CREATED_BY = QName.createQName("RestApi", "createdBy");
    public static final QName PROP_COMMENT_MODIFIED_BY = QName.createQName("RestApi", "modifiedBy");

    private String id;
    private String title;
    private String content;
    private Date createdAt;
    private Person createdBy;
    private Date modifiedAt;
    private Person modifiedBy;
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
        if (id == null)
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

    public Person getCreatedBy()
    {
        return createdBy;
    }

    public Person getModifiedBy()
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

        this.modifiedAt = (Date) nodeProps.get(ContentModel.PROP_MODIFIED);
        this.createdAt = (Date) nodeProps.get(ContentModel.PROP_CREATED);
        if (modifiedAt != null && createdAt != null)
        {
            long diff = modifiedAt.getTime() - createdAt.getTime();
            this.edited = Boolean.valueOf(diff >= 100); // logic is consistent with existing (Javascript) comments implementation
        }

        this.content = (String) nodeProps.get(PROP_COMMENT_CONTENT);
        nodeProps.remove(PROP_COMMENT_CONTENT);

        this.createdBy = (Person) nodeProps.get(PROP_COMMENT_CREATED_BY);
        nodeProps.remove(PROP_COMMENT_CREATED_BY);

        this.modifiedBy = (Person) nodeProps.get(PROP_COMMENT_MODIFIED_BY);
        nodeProps.remove(PROP_COMMENT_MODIFIED_BY);
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
