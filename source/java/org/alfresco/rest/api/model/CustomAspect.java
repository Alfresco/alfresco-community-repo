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

import java.util.List;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class CustomAspect extends AbstractClassModel
{

    public CustomAspect()
    {
    }

    public CustomAspect(AspectDefinition aspectDefinition, MessageLookup messageLookup, List<CustomModelProperty> properties)
    {
        this.name = aspectDefinition.getName().getLocalName();
        this.prefixedName = aspectDefinition.getName().toPrefixString();
        this.title = aspectDefinition.getTitle(messageLookup);
        this.description = aspectDefinition.getDescription(messageLookup);
        this.parentName = getParentNameAsString(aspectDefinition.getParentName());
        this.properties = setList(properties);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(512);
        builder.append("CustomAspect [name=").append(this.name)
                    .append(", prefixedName=").append(this.prefixedName)
                    .append(", title=").append(this.title)
                    .append(", description=").append(this.description)
                    .append(", parentName=").append(parentName)
                    .append(", properties=").append(properties)
                    .append(']');
        return builder.toString();
    }
}
