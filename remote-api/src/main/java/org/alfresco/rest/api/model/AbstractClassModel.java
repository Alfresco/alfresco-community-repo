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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.namespace.QName;

/**
 * @author Jamal Kaabi-Mofrad
 */
public abstract class AbstractClassModel extends AbstractCommonDetails
{
    /* package */String parentName;
    /* package */List<CustomModelProperty> properties = Collections.emptyList();

    public String getParentName()
    {
        return this.parentName;
    }

    public void setParentName(String parentName)
    {
        this.parentName = parentName;
    }

    public List<CustomModelProperty> getProperties()
    {
        return this.properties;
    }

    public void setProperties(List<CustomModelProperty> properties)
    {
        this.properties = properties;
    }

    /* package */<T> List<T> setList(List<T> sourceList)
    {
        if (sourceList == null)
        {
            return Collections.<T> emptyList();
        }
        return new ArrayList<>(sourceList);
    }

    /* package */String getParentNameAsString(QName parentQName)
    {
        if (parentQName != null)
        {
            return parentQName.toPrefixString();
        }
        return null;
    }
}
