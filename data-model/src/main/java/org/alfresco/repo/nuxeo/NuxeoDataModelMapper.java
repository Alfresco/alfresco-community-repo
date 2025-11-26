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
package org.alfresco.repo.nuxeo;

import org.alfresco.repo.dictionary.M2Aspect;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Namespace;
import org.alfresco.repo.dictionary.M2Type;
import org.alfresco.repo.nuxeo.config.MappingContext;
import org.alfresco.repo.nuxeo.mapper.AlfrescoToNuxeoAspectMapper;
import org.alfresco.repo.nuxeo.mapper.AlfrescoToNuxeoAssociationMapper;
import org.alfresco.repo.nuxeo.mapper.AlfrescoToNuxeoTypeMapper;
import org.alfresco.repo.nuxeo.model.NuxeoDocumentType;
import org.alfresco.repo.nuxeo.model.NuxeoFacet;
import org.alfresco.repo.nuxeo.model.NuxeoSchema;

import java.io.InputStream;
import java.util.List;

/**
 * Main orchestrator for mapping Alfresco data models to Nuxeo data models.
 * Coordinates the transformation of types, aspects, properties, and associations.
 *
 * @author Alfresco Data Model Migration Team
 */
public class NuxeoDataModelMapper
{
    private final AlfrescoToNuxeoTypeMapper typeMapper;
    private final AlfrescoToNuxeoAspectMapper aspectMapper;
    private final AlfrescoToNuxeoAssociationMapper associationMapper;

    /**
     * Default constructor.
     */
    public NuxeoDataModelMapper()
    {
        this.typeMapper = new AlfrescoToNuxeoTypeMapper();
        this.aspectMapper = new AlfrescoToNuxeoAspectMapper();
        this.associationMapper = new AlfrescoToNuxeoAssociationMapper();
    }

    /**
     * Constructor with custom mappers.
     *
     * @param typeMapper type mapper
     * @param aspectMapper aspect mapper
     * @param associationMapper association mapper
     */
    public NuxeoDataModelMapper(AlfrescoToNuxeoTypeMapper typeMapper,
                                 AlfrescoToNuxeoAspectMapper aspectMapper,
                                 AlfrescoToNuxeoAssociationMapper associationMapper)
    {
        this.typeMapper = typeMapper;
        this.aspectMapper = aspectMapper;
        this.associationMapper = associationMapper;
    }

    /**
     * Maps an Alfresco M2Model to Nuxeo document types, schemas, and facets.
     *
     * @param alfrescoModel Alfresco model
     * @return mapping context containing all mapped entities
     */
    public MappingContext mapModel(M2Model alfrescoModel)
    {
        if (alfrescoModel == null)
        {
            throw new IllegalArgumentException("Alfresco model cannot be null");
        }

        MappingContext context = new MappingContext();

        // Map namespaces
        mapNamespaces(alfrescoModel, context);

        // Map aspects first (they may be referenced by types)
        mapAspects(alfrescoModel, context);

        // Map types
        mapTypes(alfrescoModel, context);

        return context;
    }

    /**
     * Maps an Alfresco model from XML input stream.
     *
     * @param xmlInputStream XML input stream
     * @return mapping context containing all mapped entities
     */
    public MappingContext mapModel(InputStream xmlInputStream)
    {
        if (xmlInputStream == null)
        {
            throw new IllegalArgumentException("XML input stream cannot be null");
        }

        M2Model alfrescoModel = M2Model.createModel(xmlInputStream);
        return mapModel(alfrescoModel);
    }

    /**
     * Maps namespaces to schema prefixes.
     *
     * @param alfrescoModel Alfresco model
     * @param context mapping context
     */
    private void mapNamespaces(M2Model alfrescoModel, MappingContext context)
    {
        List<M2Namespace> namespaces = alfrescoModel.getNamespaces();
        if (namespaces != null)
        {
            for (M2Namespace namespace : namespaces)
            {
                context.addNamespaceMapping(namespace.getUri(), namespace.getPrefix());
            }
        }
    }

    /**
     * Maps Alfresco types to Nuxeo document types.
     *
     * @param alfrescoModel Alfresco model
     * @param context mapping context
     */
    private void mapTypes(M2Model alfrescoModel, MappingContext context)
    {
        List<M2Type> types = alfrescoModel.getTypes();
        if (types != null)
        {
            for (M2Type type : types)
            {
                NuxeoDocumentType docType = typeMapper.mapType(type, context);
                if (docType != null)
                {
                    context.addDocumentType(docType);
                }
            }
        }
    }

    /**
     * Maps Alfresco aspects to Nuxeo facets and schemas.
     *
     * @param alfrescoModel Alfresco model
     * @param context mapping context
     */
    private void mapAspects(M2Model alfrescoModel, MappingContext context)
    {
        List<M2Aspect> aspects = alfrescoModel.getAspects();
        if (aspects != null)
        {
            for (M2Aspect aspect : aspects)
            {
                aspectMapper.mapAspect(aspect, context);
            }
        }
    }

    /**
     * Generates a JSON representation of the mapped Nuxeo model.
     *
     * @param context mapping context
     * @return JSON string
     */
    public String toJSON(MappingContext context)
    {
        if (context == null)
        {
            return "{}";
        }

        StringBuilder json = new StringBuilder();
        json.append("{\n");

        // Document types
        json.append("  \"documentTypes\": [\n");
        boolean first = true;
        for (NuxeoDocumentType docType : context.getDocumentTypes().values())
        {
            if (!first) json.append(",\n");
            first = false;
            json.append("    {\n");
            json.append("      \"name\": \"").append(docType.getName()).append("\",\n");
            json.append("      \"parent\": \"").append(docType.getParent()).append("\",\n");
            json.append("      \"schemas\": ").append(toJSONArray(docType.getSchemas())).append(",\n");
            json.append("      \"facets\": ").append(toJSONArray(docType.getFacets())).append("\n");
            json.append("    }");
        }
        json.append("\n  ],\n");

        // Schemas
        json.append("  \"schemas\": [\n");
        first = true;
        for (NuxeoSchema schema : context.getSchemas().values())
        {
            if (!first) json.append(",\n");
            first = false;
            json.append("    {\n");
            json.append("      \"name\": \"").append(schema.getName()).append("\",\n");
            json.append("      \"prefix\": \"").append(schema.getPrefix()).append("\",\n");
            json.append("      \"fields\": ").append(schema.getFields().size()).append("\n");
            json.append("    }");
        }
        json.append("\n  ],\n");

        // Facets
        json.append("  \"facets\": [\n");
        first = true;
        for (NuxeoFacet facet : context.getFacets().values())
        {
            if (!first) json.append(",\n");
            first = false;
            json.append("    {\n");
            json.append("      \"name\": \"").append(facet.getName()).append("\",\n");
            json.append("      \"schemas\": ").append(toJSONArray(facet.getSchemas())).append("\n");
            json.append("    }");
        }
        json.append("\n  ],\n");

        // Warnings
        json.append("  \"warnings\": ").append(toJSONArray(context.getWarnings())).append("\n");

        json.append("}");
        return json.toString();
    }

    /**
     * Converts a list of strings to a JSON array.
     *
     * @param list list of strings
     * @return JSON array string
     */
    private String toJSONArray(List<String> list)
    {
        if (list == null || list.isEmpty())
        {
            return "[]";
        }

        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (String item : list)
        {
            if (!first) json.append(", ");
            first = false;
            json.append("\"").append(item).append("\"");
        }
        json.append("]");
        return json.toString();
    }

    /**
     * Generates a summary report of the mapping.
     *
     * @param context mapping context
     * @return summary report
     */
    public String generateSummaryReport(MappingContext context)
    {
        if (context == null)
        {
            return "No mapping context available.";
        }

        StringBuilder report = new StringBuilder();
        report.append("=== Alfresco to Nuxeo Data Model Mapping Summary ===\n\n");

        // Statistics
        report.append("Statistics:\n");
        report.append("  Document Types: ").append(context.getDocumentTypes().size()).append("\n");
        report.append("  Schemas: ").append(context.getSchemas().size()).append("\n");
        report.append("  Facets: ").append(context.getFacets().size()).append("\n");
        report.append("  Namespaces: ").append(context.getNamespaceMap().size()).append("\n");
        report.append("  Warnings: ").append(context.getWarnings().size()).append("\n\n");

        // Warnings
        if (!context.getWarnings().isEmpty())
        {
            report.append("Warnings:\n");
            for (String warning : context.getWarnings())
            {
                report.append("  - ").append(warning).append("\n");
            }
            report.append("\n");
        }

        // Document Types
        report.append("Document Types:\n");
        for (NuxeoDocumentType docType : context.getDocumentTypes().values())
        {
            report.append("  - ").append(docType.getName());
            if (docType.getParent() != null)
            {
                report.append(" (extends ").append(docType.getParent()).append(")");
            }
            report.append("\n");
        }

        return report.toString();
    }
}
