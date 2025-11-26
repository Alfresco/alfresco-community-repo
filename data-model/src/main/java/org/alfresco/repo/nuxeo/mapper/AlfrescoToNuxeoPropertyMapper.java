/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.repo.nuxeo.mapper;

import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.repo.nuxeo.config.MappingContext;
import org.alfresco.repo.nuxeo.converter.DataTypeConverter;
import org.alfresco.repo.nuxeo.model.NuxeoField;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps Alfresco properties to Nuxeo fields.
 * Handles data type conversion, constraints, and field metadata.
 *
 * @author Alfresco Data Model Migration Team
 */
public class AlfrescoToNuxeoPropertyMapper
{
    /**
     * Maps an Alfresco property to a Nuxeo field.
     *
     * @param property Alfresco property
     * @param context mapping context
     * @return Nuxeo field
     */
    public NuxeoField mapProperty(M2Property property, MappingContext context)
    {
        if (property == null)
        {
            return null;
        }

        NuxeoField field = new NuxeoField();
        
        // Map name - extract local name from qualified name
        String name = extractLocalName(property.getName());
        field.setName(name);
        
        // Map data type
        String alfrescoType = property.getPropertyType();
        String nuxeoType = DataTypeConverter.convertType(alfrescoType);
        field.setType(nuxeoType);
        
        // Map required flag
        field.setRequired(property.isMandatory());
        
        // Map multi-valued flag
        field.setMultiValued(property.isMultiValued());
        
        // Map default value
        if (property.getDefaultValue() != null)
        {
            field.setDefaultValue(property.getDefaultValue());
        }
        
        // Map description
        if (property.getDescription() != null)
        {
            field.setDescription(property.getDescription());
        }
        
        // Map constraints
        List<String> constraints = mapConstraints(property);
        field.setConstraints(constraints);
        
        // Add warning if data type conversion has potential loss
        if (DataTypeConverter.hasDataLoss(alfrescoType))
        {
            String warning = String.format("Property '%s' type '%s': %s",
                property.getName(), alfrescoType, 
                DataTypeConverter.getDataLossDescription(alfrescoType));
            context.addWarning(warning);
        }
        
        return field;
    }

    /**
     * Maps multiple Alfresco properties to Nuxeo fields.
     *
     * @param properties list of Alfresco properties
     * @param context mapping context
     * @return list of Nuxeo fields
     */
    public List<NuxeoField> mapProperties(List<M2Property> properties, MappingContext context)
    {
        List<NuxeoField> fields = new ArrayList<>();
        
        if (properties != null)
        {
            for (M2Property property : properties)
            {
                NuxeoField field = mapProperty(property, context);
                if (field != null)
                {
                    fields.add(field);
                }
            }
        }
        
        return fields;
    }

    /**
     * Extracts the local name from a qualified name.
     *
     * @param qualifiedName qualified name (e.g., "cm:name" or "{http://...}name")
     * @return local name
     */
    private String extractLocalName(String qualifiedName)
    {
        if (qualifiedName == null)
        {
            return null;
        }

        // Handle prefix:localName format
        int colonIndex = qualifiedName.indexOf(':');
        if (colonIndex > 0 && colonIndex < qualifiedName.length() - 1)
        {
            return qualifiedName.substring(colonIndex + 1);
        }

        // Handle {uri}localName format
        int closeBraceIndex = qualifiedName.indexOf('}');
        if (closeBraceIndex > 0 && closeBraceIndex < qualifiedName.length() - 1)
        {
            return qualifiedName.substring(closeBraceIndex + 1);
        }

        // No prefix, return as-is
        return qualifiedName;
    }

    /**
     * Maps Alfresco property constraints to Nuxeo field constraints.
     * 
     * Note: This is a simplified implementation. M2Property stores constraints
     * in a complex structure that would require additional parsing logic.
     * In a production implementation, this should:
     * - Extract LIST constraints and convert to allowed values
     * - Extract REGEX constraints and convert to pattern validators
     * - Extract MINMAX constraints and convert to range validators
     * - Extract LENGTH constraints and convert to string length validators
     *
     * @param property Alfresco property
     * @return list of constraint descriptions
     */
    private List<String> mapConstraints(M2Property property)
    {
        List<String> constraints = new ArrayList<>();
        
        // TODO: Implement full constraint extraction from M2Property.getConstraints()
        // This requires parsing M2Constraint objects which have complex structures
        // For prototype purposes, constraints should be handled during full model parsing
        
        return constraints;
    }
}
