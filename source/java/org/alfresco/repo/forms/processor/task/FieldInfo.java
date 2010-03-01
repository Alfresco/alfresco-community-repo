/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.forms.processor.task;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.*;

import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 */
public class FieldInfo
{
    private final QName fullName;

    private final FieldDefinition fieldDefinition;

    private final String fieldName;

    private final DictionaryService dictionaryService;

    private final FieldDefinitionFactory factory;

    private FieldType fieldType;

    public FieldInfo(String fieldName, FieldCreationData data, FieldDefinitionFactory factory,
                DictionaryService dictionaryService, NamespaceService namespaceService)
    {
        this.factory = factory;
        this.dictionaryService = dictionaryService;
        this.fieldName = fieldName;
        this.fullName = makeNameAndFieldType(fieldName, namespaceService);
        this.fieldDefinition = createDefinition(data);
    }

    private FieldDefinition createDefinition(final FieldCreationData data)
    {
        // Callback used to find the PropertyDefinition associated with this
        // field (if any).
        Getter<PropertyDefinition> propDefGetter = new Getter<PropertyDefinition>()
        {
            public PropertyDefinition get()
            {
                return data.getPropDefs().get(fullName);
            }
        };

        // Callback used to find the PropertyDefinition associated with this
        // field (if any).
        Getter<AssociationDefinition> assocDefGetter = new Getter<AssociationDefinition>()
        {
            public AssociationDefinition get()
            {
                return data.getAssocDefs().get(fullName);
            }
        };

        FieldDefinition fieldDef = createDefinition(data, propDefGetter, assocDefGetter);
        if (fieldDef == null)
        {
            // If field is a forced field try to get it from dictionary
            // service.
            if (data.getForcedFields().contains(fieldName))
            {
                propDefGetter = new Getter<PropertyDefinition>()
                {
                    public PropertyDefinition get()
                    {
                        return dictionaryService.getProperty(fullName);
                    }
                };

                new Getter<AssociationDefinition>()
                {
                    public AssociationDefinition get()
                    {
                        return dictionaryService.getAssociation(fullName);
                    }
                };
                fieldDef = createDefinition(data);
            }
        }
        // If no field definition found then set fieldType to Invalid.
        if (fieldDef == null)
        {
            this.fieldType = FieldType.INVALID;
        }
        return fieldDef;
    }

    /**
     * @return
     */
    public boolean isValid()
    {
        return fieldDefinition != null;
    }

    private QName makeNameAndFieldType(String fieldName, NamespaceService namespaceService)
    {
        String[] parts = fieldName.split(":");
        if (parts.length < 2 || parts.length > 3)
        {
            this.fieldType = FieldType.INVALID;
            return QName.createQName(null, fieldName);
        }
        int indexer = 0;
        if (parts.length == 3)
        {
            indexer = 1;
            this.fieldType = FieldType.getType(parts[0]);
        }
        else
            this.fieldType = FieldType.UNKNOWN;
        String prefix = parts[0 + indexer];
        String localName = parts[1 + indexer];
        return QName.createQName(prefix, localName, namespaceService);
    }

    /**
     * @param data
     */
    private FieldDefinition createDefinition(FieldCreationData data, Getter<PropertyDefinition> propDefGetter,
                Getter<AssociationDefinition> assocDefGetter)
    {
        FieldDefinition fieldDef = null;
        switch (fieldType)
        {
            case INVALID:// So fieldDef will stay null.
                break;
            case PROPERTY:
                fieldDef = generatePropertyDefinition(data, propDefGetter);
                break;
            case ASSOCIATION:
                fieldDef = generateAssociationDefinition(data, assocDefGetter);
                break;
            case UNKNOWN:
                fieldDef = generatePropertyDefinition(data, propDefGetter);
                if (fieldDef != null)
                    fieldType = FieldType.PROPERTY;
                else
                {
                    fieldDef = generateAssociationDefinition(data, assocDefGetter);
                    if (fieldDef != null) fieldType = FieldType.ASSOCIATION;
                }
        }
        return fieldDef;
    }

    private FieldDefinition generateAssociationDefinition(FieldCreationData data,
                Getter<AssociationDefinition> assocDefGetter)
    {
        AssociationDefinition assocDef = assocDefGetter.get();
        if (assocDef != null)
            return factory.makeAssociationFieldDefinition(assocDef, data.getGroup());
        else
            return null;
    }

    private FieldDefinition generatePropertyDefinition(FieldCreationData data, Getter<PropertyDefinition> propDefGetter)
    {
        PropertyDefinition propDef = propDefGetter.get();
        if (propDef != null) //
            return factory.makePropertyFieldDefinition(propDef, data.getGroup());
        else
            return null;
    }

    /**
     * @return the fieldDefinition
     */
    public FieldDefinition getFieldDefinition()
    {
        return this.fieldDefinition;
    }

    /**
     * @return the fullName
     */
    public QName getFullName()
    {
        return this.fullName;
    }

    /**
     * @return the fieldName
     */
    public String getFieldName()
    {
        return this.fieldName;
    }

    private enum FieldType
    {
        ASSOCIATION, INVALID, PROPERTY, UNKNOWN;

        public static FieldType getType(String type)
        {
            if (PROP.equals(type))
            {
                return PROPERTY;
            }
            else if (ASSOC.equals(type)) return ASSOCIATION;
            return UNKNOWN;
        }
    }
}
