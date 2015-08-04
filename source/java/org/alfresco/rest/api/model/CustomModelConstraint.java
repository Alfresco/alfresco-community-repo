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
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class CustomModelConstraint extends AbstractCommonDetails
{
    private String type;
    private List<CustomModelNamedValue> parameters = Collections.emptyList();

    public CustomModelConstraint()
    {
    }

    public CustomModelConstraint(ConstraintDefinition constraintDefinition, MessageLookup messageLookup)
    {
        this.name = constraintDefinition.getName().getLocalName();
        this.prefixedName = constraintDefinition.getConstraint().getShortName();
        this.type = constraintDefinition.getConstraint().getType();
        this.title = constraintDefinition.getTitle(messageLookup);
        this.description = constraintDefinition.getDescription(messageLookup);
        this.parameters = convertToNamedValue(constraintDefinition.getConstraint().getParameters());
    }

    private List<CustomModelNamedValue> convertToNamedValue(Map<String, Object> params)
    {
        List<CustomModelNamedValue> list = new ArrayList<>(params.size());
        for (Entry<String, Object> en : params.entrySet())
        {
            list.add(new CustomModelNamedValue(en.getKey(), en.getValue()));
        }

        return list;
    }

    public String getType()
    {
        return this.type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public List<CustomModelNamedValue> getParameters()
    {
        return this.parameters;
    }

    public void setParameters(List<CustomModelNamedValue> parameters)
    {
        this.parameters = parameters;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(300);
        builder.append("CustomModelConstraint [name=").append(this.name)
                    .append(", prefixedName=").append(this.prefixedName)
                    .append(", type=").append(this.type)
                    .append(", title=").append(this.title)
                    .append(", description=").append(this.description)
                    .append(", parameters=").append(this.parameters)
                    .append(']');
        return builder.toString();
    }
}