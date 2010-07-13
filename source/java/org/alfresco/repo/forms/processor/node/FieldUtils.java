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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.FieldGroup;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Utility class to assist in creating {@link Field Fields} which represent
 * {@link PropertyDefinition PropertyDefinitions} and
 * {@link AssociationDefinition AssociationDefinitions}
 * 
 * @author Nick Smith
 * 
 */
public class FieldUtils
{

    public static Field makePropertyField(
                PropertyDefinition property,
                Object value,
                FieldGroup group,
                NamespaceService namespaceService)
    {
        PropertyFieldProcessor processor = new PropertyFieldProcessor(namespaceService, null);
        return processor.makeField(property, value, group);
    }
    
    public static List<Field> makePropertyFields(
                Collection<PropertyDefinition> propDefs,
                FieldGroup group,
                NamespaceService namespaceService)
    {
        return makePropertyFields(propDefs, null, group, namespaceService);
    }
    
    public static List<Field> makePropertyFields(
                Map<PropertyDefinition, Object> propDefAndValue,
                FieldGroup group,
                NamespaceService namespaceService)
    {
        return makePropertyFields(propDefAndValue.keySet(), propDefAndValue, group, namespaceService);
    }
    
    public static List<Field> makePropertyFields(
                Collection<PropertyDefinition> propDefs,
                Map<PropertyDefinition, Object> values,
                FieldGroup group,
                NamespaceService namespaceService)
    {
        PropertyFieldProcessor processor = new PropertyFieldProcessor(namespaceService, null);
        ArrayList<Field> fields = new ArrayList<Field>(propDefs.size());
        for (PropertyDefinition propDef : propDefs)
        {
            Object value = values==null?null:values.get(propDef);
            Field field = processor.makeField(propDef, value, group);
            fields.add(field);
        }
        return fields;
    }
    
    public static Field makeAssociationField(
                AssociationDefinition assocDef,
                Object value,
                FieldGroup group,
                NamespaceService namespaceService)
    {
        AssociationFieldProcessor processor = new AssociationFieldProcessor(namespaceService, null);
        return processor.makeField(assocDef, value, group);
    }
    

    public static List<Field> makeAssociationFields(
                Collection<AssociationDefinition> assocDefs,
                FieldGroup group,
                NamespaceService namespaceService)
    {
        return makeAssociationFields(assocDefs, null, group, namespaceService);
    }
    
    public static List<Field> makeAssociationFields(
                Map<AssociationDefinition, Object> assocDefAndValue,
                FieldGroup group,
                NamespaceService namespaceService)
    {
        return makeAssociationFields(assocDefAndValue.keySet(), assocDefAndValue, group, namespaceService);
    }
    
    public static List<Field> makeAssociationFields(
                Collection<AssociationDefinition> assocDefs,
                Map<AssociationDefinition, Object> values,
                FieldGroup group,
                NamespaceService namespaceService)
    {
        AssociationFieldProcessor processor = new AssociationFieldProcessor(namespaceService, null);
        ArrayList<Field> fields = new ArrayList<Field>(assocDefs.size());
        for (AssociationDefinition propDef : assocDefs)
        {
            Object value = values==null?null:values.get(propDef);
            Field field = processor.makeField(propDef, value, group);
            fields.add(field);
        }
        return fields;
    }
    
}
