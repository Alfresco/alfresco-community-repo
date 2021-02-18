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

    public Boolean getChild()
    {
        return isChild;
    }

    public void setChild(Boolean child)
    {
        isChild = child;
    }

    public Boolean getProtected()
    {
        return isProtected;
    }

    public void setProtected(Boolean isProtected)
    {
        isProtected = isProtected;
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
}