/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.forms.processor.node;

import org.alfresco.repo.forms.AssociationFieldDefinition;
import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

/**
 * DTO for a content model based Field.
 * 
 * @author Nick Smith
 * @since 3.4
 */
public class ContentModelField implements Field
{
    private final FieldDefinition fieldDefinition;
    private final ClassAttributeDefinition classDefinition;
    private final Object value;
    
    public ContentModelField(PropertyDefinition propertyDefinition, 
            PropertyFieldDefinition fieldDef, Object value)
    {
        this.classDefinition = propertyDefinition;
        this.fieldDefinition = fieldDef;
        this.value = value;
    }

    public ContentModelField(AssociationDefinition assocDefinition, 
            AssociationFieldDefinition fieldDef, Object value)
    {
        this.classDefinition = assocDefinition;
        this.fieldDefinition = fieldDef;
        this.value = value;
    }

    /**
     * This constructor should only be used to create FieldInfo for transient properties such as encoding, mimetype or size.
     * @param fieldDef The PropertyFieldDefinition for the transient property.
     */
    public ContentModelField(FieldDefinition fieldDef, Object value)
    {
        this.classDefinition = null;
        this.fieldDefinition = fieldDef;
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.task.Field#isTransient()
     */
    public boolean isTransient()
    {
        return classDefinition == null;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.task.Field#isTransient()
     */
    public boolean isProperty()
    {
        return fieldDefinition instanceof PropertyFieldDefinition;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.task.Field#getFieldDefinition()
     */
    public FieldDefinition getFieldDefinition()
    {
        return this.fieldDefinition;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.task.Field#getFullName()
     */
    public QName getFullName()
    {
        if (classDefinition == null)
        {
            return null;
        }
        
        return classDefinition.getName();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.task.Field#getFieldName()
     */
    public String getFieldName()
    {
        return fieldDefinition.getName();
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String type = isTransient()?"Transient ":"";
        type += isProperty() ? "Property" : "Association";
        return "Field: " + getFieldName() + " Type: " + type;
    }

    public Object getValue()
    {
        return value;
    }

}
