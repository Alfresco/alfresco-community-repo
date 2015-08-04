/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
