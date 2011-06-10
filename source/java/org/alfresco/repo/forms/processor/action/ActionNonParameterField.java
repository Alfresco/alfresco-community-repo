/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.forms.processor.action;

import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.service.namespace.QName;

/**
 * This class represents a {@link Field} for an action form, which is not a parameter.
 *
 * @since 4.0
 */
public class ActionNonParameterField implements Field
{
    // There is currently only one instance of this class - the executeAsynchronously action form field.
    // It may be possible to remove this class and use a raw PropertyFieldDefinition.
    private PropertyFieldDefinition fieldDef;
    private String name;

    public ActionNonParameterField(String name, QName type)
    {
        this.name = name;
        this.fieldDef = new PropertyFieldDefinition(this.name, type.getLocalName());
        
        this.fieldDef.setLabel(this.name);
        this.fieldDef.setDataKeyName(this.name);
    }
    
    @Override
    public FieldDefinition getFieldDefinition()
    {
        return this.fieldDef;
    }

    @Override
    public String getFieldName()
    {
        return this.name;
    }

    @Override
    public Object getValue()
    {
        return null;
    }
}
