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
import org.alfresco.repo.dictionary.M2Type;
import org.alfresco.repo.nuxeo.config.MappingContext;
import org.alfresco.repo.nuxeo.model.NuxeoDocumentType;
import org.alfresco.repo.nuxeo.model.NuxeoField;
import org.alfresco.repo.nuxeo.model.NuxeoSchema;

import java.util.List;

/**
 * Maps Alfresco types to Nuxeo document types.
 * Handles type hierarchy, properties, and mandatory aspects.
 *
 * @author Alfresco Data Model Migration Team
 */
public class AlfrescoToNuxeoTypeMapper
{
    private final AlfrescoToNuxeoPropertyMapper propertyMapper;

    /**
     * Default constructor.
     */
    public AlfrescoToNuxeoTypeMapper()
    {
        this.propertyMapper = new AlfrescoToNuxeoPropertyMapper();
    }

    /**
     * Constructor with property mapper.
     *
     * @param propertyMapper property mapper
     */
    public AlfrescoToNuxeoTypeMapper(AlfrescoToNuxeoPropertyMapper propertyMapper)
    {
        this.propertyMapper = propertyMapper;
    }

    /**
     * Maps an Alfresco type to a Nuxeo document type.
     *
     * @param type Alfresco type
     * @param context mapping context
     * @return Nuxeo document type
     */
    public NuxeoDocumentType mapType(M2Type type, MappingContext context)
    {
        if (type == null)
        {
            return null;
        }

        NuxeoDocumentType docType = new NuxeoDocumentType();
        
        // Map type name
        String typeName = type.getName();
        docType.setName(typeName);
        
        // Map parent type
        String parentName = type.getParentName();
        if (parentName != null)
        {
            String mappedParent = mapParentType(parentName, context);
            docType.setParent(mappedParent);
        }
        else
        {
            // Default to Nuxeo's Document type if no parent
            docType.setParent("Document");
        }
        
        // Map description
        if (type.getDescription() != null)
        {
            docType.setDescription(type.getDescription());
        }
        
        // Create schema for type properties
        if (type.getProperties() != null && !type.getProperties().isEmpty())
        {
            String schemaName = extractSchemaName(typeName);
            NuxeoSchema schema = createSchemaFromType(type, context);
            context.addSchema(schema);
            docType.addSchema(schemaName);
        }
        
        // Map mandatory aspects to facets
        List<String> mandatoryAspects = type.getMandatoryAspects();
        if (mandatoryAspects != null)
        {
            for (String aspectName : mandatoryAspects)
            {
                docType.addFacet(aspectName);
                
                // Also add the aspect's schema
                String aspectSchemaName = extractSchemaName(aspectName);
                docType.addSchema(aspectSchemaName);
            }
        }
        
        return docType;
    }

    /**
     * Creates a Nuxeo schema from an Alfresco type's properties.
     *
     * @param type Alfresco type
     * @param context mapping context
     * @return Nuxeo schema
     */
    private NuxeoSchema createSchemaFromType(M2Type type, MappingContext context)
    {
        String schemaName = extractSchemaName(type.getName());
        String prefix = extractPrefix(type.getName());
        
        NuxeoSchema schema = new NuxeoSchema(schemaName, prefix);
        schema.setDescription("Schema for " + type.getName());
        
        // Map properties to fields
        List<M2Property> properties = type.getProperties();
        if (properties != null)
        {
            List<NuxeoField> fields = propertyMapper.mapProperties(properties, context);
            schema.setFields(fields);
        }
        
        return schema;
    }

    /**
     * Maps an Alfresco parent type name to a Nuxeo parent document type.
     *
     * @param parentName Alfresco parent type name
     * @param context mapping context
     * @return Nuxeo parent document type name
     */
    private String mapParentType(String parentName, MappingContext context)
    {
        if (parentName == null)
        {
            return "Document";
        }

        // Check for custom mapping
        String customMapping = context.getCustomMapping("type.parent." + parentName);
        if (customMapping != null)
        {
            return customMapping;
        }

        // Map common Alfresco types to Nuxeo types
        if (parentName.contains("cm:content"))
        {
            return "File";
        }
        else if (parentName.contains("cm:folder"))
        {
            return "Folder";
        }
        else if (parentName.contains("cm:cmobject"))
        {
            return "Document";
        }

        // Default: use the same name
        return parentName;
    }

    /**
     * Extracts a schema name from a qualified type name.
     *
     * @param qualifiedName qualified name (e.g., "custom:invoice")
     * @return schema name
     */
    private String extractSchemaName(String qualifiedName)
    {
        if (qualifiedName == null)
        {
            return "custom";
        }

        // Extract prefix and local name
        String prefix = extractPrefix(qualifiedName);
        String localName = extractLocalName(qualifiedName);
        
        // Schema name is typically prefix_localName
        return prefix + "_" + localName;
    }

    /**
     * Extracts the prefix from a qualified name.
     *
     * @param qualifiedName qualified name (e.g., "cm:content")
     * @return prefix
     */
    private String extractPrefix(String qualifiedName)
    {
        if (qualifiedName == null)
        {
            return "custom";
        }

        int colonIndex = qualifiedName.indexOf(':');
        if (colonIndex > 0)
        {
            return qualifiedName.substring(0, colonIndex);
        }

        return "custom";
    }

    /**
     * Extracts the local name from a qualified name.
     *
     * @param qualifiedName qualified name (e.g., "cm:content")
     * @return local name
     */
    private String extractLocalName(String qualifiedName)
    {
        if (qualifiedName == null)
        {
            return "document";
        }

        int colonIndex = qualifiedName.indexOf(':');
        if (colonIndex > 0 && colonIndex < qualifiedName.length() - 1)
        {
            return qualifiedName.substring(colonIndex + 1);
        }

        return qualifiedName;
    }
}
