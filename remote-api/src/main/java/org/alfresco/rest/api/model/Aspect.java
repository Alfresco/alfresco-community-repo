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

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.QName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Aspect extends AbstractCommonDetails
{
    List<NodeDefinitionProperty> properties = Collections.emptyList();
    String parentName;

    public Aspect()
    {
    }

    public Aspect(AspectDefinition aspectDefinition, MessageLookup messageLookup, List<NodeDefinitionProperty> properties)
    {
        this.name = aspectDefinition.getName().getLocalName();
        this.prefixedName = aspectDefinition.getName().toPrefixString();
        this.title = aspectDefinition.getTitle(messageLookup);
        this.description = aspectDefinition.getDescription(messageLookup);
        this.parentName = getParentNameAsString(aspectDefinition.getParentName());
        this.properties = setList(properties);
    }

    public List<NodeDefinitionProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(List<NodeDefinitionProperty> properties)
    {
        this.properties = properties;
    }

    public String getParentName()
    {
        return parentName;
    }


    public void setParentName(String parentName)
    {
        this.parentName = parentName;
    }

    <T> List<T> setList(List<T> sourceList)
    {
        if (sourceList == null)
        {
            return Collections.<T> emptyList();
        }
        return new ArrayList<>(sourceList);
    }

    String getParentNameAsString(QName parentQName)
    {
        if (parentQName != null)
        {
            return parentQName.toPrefixString();
        }
        return null;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(512);
        builder.append("Aspect [name=").append(this.name)
                .append(", prefixedName=").append(this.prefixedName)
                .append(", title=").append(this.title)
                .append(", description=").append(this.description)
                .append(", parentName=").append(parentName)
                .append(", properties=").append(properties)
                .append(']');
        return builder.toString();
    }
}

