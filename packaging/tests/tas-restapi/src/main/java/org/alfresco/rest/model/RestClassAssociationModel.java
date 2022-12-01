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

import org.alfresco.utility.model.TestModel;

public class RestClassAssociationModel extends TestModel
{
    public String id;
    public String title;
    public String description;
    public Boolean isChild;
    public Boolean isProtected;
    public RestClassAssociationDefinitionModel source = null;
    public RestClassAssociationDefinitionModel target = null;

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

    public RestClassAssociationDefinitionModel getSource()
    {
        return source;
    }

    public void setSource(RestClassAssociationDefinitionModel source)
    {
        this.source = source;
    }

    public RestClassAssociationDefinitionModel getTarget()
    {
        return target;
    }

    public void setTarget(RestClassAssociationDefinitionModel target)
    {
        this.target = target;
    }
}


