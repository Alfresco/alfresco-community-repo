/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import java.util.Objects;

public class Association
{
    private String id;
    private String title;
    private String description;
    private Boolean isChild;
    private Boolean isProtected;
    private AssociationSource source = null;
    private AssociationSource target = null;

    public Association()
    {
    }

    public Association(String id, String title, String description, Boolean isChild, Boolean isProtected, AssociationSource source, AssociationSource target)
    {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isChild = isChild;
        this.isProtected = isProtected;
        this.source = source;
        this.target = target;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Boolean getIsChild()
    {
        return isChild;
    }

    public void setIsChild(Boolean isChild)
    {
        this.isChild = isChild;
    }

    public Boolean getIsProtected()
    {
        return isProtected;
    }

    public void setIsProtected(Boolean isProtected)
    {
        this.isProtected = isProtected;
    }

    public AssociationSource getSource()
    {
        return source;
    }

    public void setSource(AssociationSource source)
    {
        this.source = source;
    }

    public AssociationSource getTarget()
    {
        return target;
    }

    public void setTarget(AssociationSource target)
    {
        this.target = target;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Association other = (Association) obj;
        return Objects.equals(id, other.getId()) &&
                Objects.equals(title, other.getTitle()) &&
                Objects.equals(description, other.getDescription()) &&
                Objects.equals(isChild, other.getIsChild()) &&
                Objects.equals(isProtected, other.getIsProtected()) &&
                Objects.equals(source, other.getSource()) &&
                Objects.equals(target, other.getTarget());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(512);
        builder.append("Association [id=").append(this.id)
                .append(", title=").append(this.title)
                .append(", description=").append(this.description)
                .append(", isChild=").append(isChild)
                .append(", isProtected=").append(isProtected)
                .append(", source=").append(source)
                .append(", target=").append(target)
                .append(']');
        return builder.toString();
    }
}