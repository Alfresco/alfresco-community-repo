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

import org.alfresco.repo.dictionary.M2Aspect;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.repo.nuxeo.config.MappingContext;
import org.alfresco.repo.nuxeo.model.NuxeoFacet;
import org.alfresco.repo.nuxeo.model.NuxeoField;
import org.alfresco.repo.nuxeo.model.NuxeoSchema;

import java.util.List;

/**
 * Maps Alfresco aspects to Nuxeo facets and schemas.
 * Aspects are mapped to a combination of facets (for behavior) and schemas (for properties).
 *
 * @author Alfresco Data Model Migration Team
 */
public class AlfrescoToNuxeoAspectMapper
{
    private final AlfrescoToNuxeoPropertyMapper propertyMapper;

    /**
     * Default constructor.
     */
    public AlfrescoToNuxeoAspectMapper()
    {
        this.propertyMapper = new AlfrescoToNuxeoPropertyMapper();
    }

    /**
     * Constructor with property mapper.
     *
     * @param propertyMapper property mapper
     */
    public AlfrescoToNuxeoAspectMapper(AlfrescoToNuxeoPropertyMapper propertyMapper)
    {
        this.propertyMapper = propertyMapper;
    }

    /**
     * Maps an Alfresco aspect to a Nuxeo facet.
     *
     * @param aspect Alfresco aspect
     * @param context mapping context
     * @return Nuxeo facet
     */
    public NuxeoFacet mapAspectToFacet(M2Aspect aspect, MappingContext context)
    {
        if (aspect == null)
        {
            return null;
        }

        NuxeoFacet facet = new NuxeoFacet();
        
        // Map aspect name to facet name
        String aspectName = aspect.getName();
        facet.setName(aspectName);
        
        // Map description
        if (aspect.getDescription() != null)
        {
            facet.setDescription(aspect.getDescription());
        }
        
        // If aspect has properties, create and link a schema
        if (aspect.getProperties() != null && !aspect.getProperties().isEmpty())
        {
            String schemaName = extractSchemaName(aspectName);
            facet.addSchema(schemaName);
        }
        
        // Add warning about dynamic aspect application
        if (aspect.getProperties() != null && !aspect.getProperties().isEmpty())
        {
            context.addWarning(String.format(
                "Aspect '%s': Nuxeo facets are less dynamic than Alfresco aspects. " +
                "Consider pre-defining facets on document types.", aspectName));
        }
        
        return facet;
    }

    /**
     * Creates a Nuxeo schema from an Alfresco aspect's properties.
     *
     * @param aspect Alfresco aspect
     * @param context mapping context
     * @return Nuxeo schema
     */
    public NuxeoSchema mapAspectToSchema(M2Aspect aspect, MappingContext context)
    {
        if (aspect == null)
        {
            return null;
        }

        String schemaName = extractSchemaName(aspect.getName());
        String prefix = extractPrefix(aspect.getName());
        
        NuxeoSchema schema = new NuxeoSchema(schemaName, prefix);
        schema.setDescription("Schema for aspect " + aspect.getName());
        
        // Map properties to fields
        List<M2Property> properties = aspect.getProperties();
        if (properties != null)
        {
            List<NuxeoField> fields = propertyMapper.mapProperties(properties, context);
            schema.setFields(fields);
        }
        
        return schema;
    }

    /**
     * Maps an Alfresco aspect to both a facet and a schema.
     * This is the recommended approach for complete aspect mapping.
     *
     * @param aspect Alfresco aspect
     * @param context mapping context
     */
    public void mapAspect(M2Aspect aspect, MappingContext context)
    {
        if (aspect == null)
        {
            return;
        }

        // Create and add facet
        NuxeoFacet facet = mapAspectToFacet(aspect, context);
        if (facet != null)
        {
            context.addFacet(facet);
        }

        // Create and add schema if aspect has properties
        if (aspect.getProperties() != null && !aspect.getProperties().isEmpty())
        {
            NuxeoSchema schema = mapAspectToSchema(aspect, context);
            if (schema != null)
            {
                context.addSchema(schema);
            }
        }
    }

    /**
     * Extracts a schema name from a qualified aspect name.
     *
     * @param qualifiedName qualified name (e.g., "cm:versionable")
     * @return schema name
     */
    private String extractSchemaName(String qualifiedName)
    {
        if (qualifiedName == null)
        {
            return "custom";
        }

        String prefix = extractPrefix(qualifiedName);
        String localName = extractLocalName(qualifiedName);
        
        return prefix + "_" + localName;
    }

    /**
     * Extracts the prefix from a qualified name.
     *
     * @param qualifiedName qualified name (e.g., "cm:versionable")
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
     * @param qualifiedName qualified name (e.g., "cm:versionable")
     * @return local name
     */
    private String extractLocalName(String qualifiedName)
    {
        if (qualifiedName == null)
        {
            return "aspect";
        }

        int colonIndex = qualifiedName.indexOf(':');
        if (colonIndex > 0 && colonIndex < qualifiedName.length() - 1)
        {
            return qualifiedName.substring(colonIndex + 1);
        }

        return qualifiedName;
    }
}
