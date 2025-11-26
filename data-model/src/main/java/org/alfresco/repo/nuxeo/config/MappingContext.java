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
package org.alfresco.repo.nuxeo.config;

import org.alfresco.repo.nuxeo.model.NuxeoDocumentType;
import org.alfresco.repo.nuxeo.model.NuxeoFacet;
import org.alfresco.repo.nuxeo.model.NuxeoSchema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context holder for Alfresco to Nuxeo mapping configuration and state.
 * Maintains the mappings between Alfresco and Nuxeo entities during transformation.
 *
 * @author Alfresco Data Model Migration Team
 */
public class MappingContext
{
    private final Map<String, NuxeoDocumentType> documentTypes;
    private final Map<String, NuxeoSchema> schemas;
    private final Map<String, NuxeoFacet> facets;
    private final Map<String, String> namespaceMap;
    private final List<String> warnings;
    private final Map<String, String> customMappings;

    /**
     * Default constructor.
     */
    public MappingContext()
    {
        this.documentTypes = new HashMap<>();
        this.schemas = new HashMap<>();
        this.facets = new HashMap<>();
        this.namespaceMap = new HashMap<>();
        this.warnings = new ArrayList<>();
        this.customMappings = new HashMap<>();
    }

    /**
     * Adds a document type to the context.
     *
     * @param documentType document type to add
     */
    public void addDocumentType(NuxeoDocumentType documentType)
    {
        if (documentType != null && documentType.getName() != null)
        {
            this.documentTypes.put(documentType.getName(), documentType);
        }
    }

    /**
     * Gets a document type by name.
     *
     * @param name document type name
     * @return the document type if found, null otherwise
     */
    public NuxeoDocumentType getDocumentType(String name)
    {
        return this.documentTypes.get(name);
    }

    /**
     * Gets all document types.
     *
     * @return map of document types
     */
    public Map<String, NuxeoDocumentType> getDocumentTypes()
    {
        return documentTypes;
    }

    /**
     * Adds a schema to the context.
     *
     * @param schema schema to add
     */
    public void addSchema(NuxeoSchema schema)
    {
        if (schema != null && schema.getName() != null)
        {
            this.schemas.put(schema.getName(), schema);
        }
    }

    /**
     * Gets a schema by name.
     *
     * @param name schema name
     * @return the schema if found, null otherwise
     */
    public NuxeoSchema getSchema(String name)
    {
        return this.schemas.get(name);
    }

    /**
     * Gets all schemas.
     *
     * @return map of schemas
     */
    public Map<String, NuxeoSchema> getSchemas()
    {
        return schemas;
    }

    /**
     * Adds a facet to the context.
     *
     * @param facet facet to add
     */
    public void addFacet(NuxeoFacet facet)
    {
        if (facet != null && facet.getName() != null)
        {
            this.facets.put(facet.getName(), facet);
        }
    }

    /**
     * Gets a facet by name.
     *
     * @param name facet name
     * @return the facet if found, null otherwise
     */
    public NuxeoFacet getFacet(String name)
    {
        return this.facets.get(name);
    }

    /**
     * Gets all facets.
     *
     * @return map of facets
     */
    public Map<String, NuxeoFacet> getFacets()
    {
        return facets;
    }

    /**
     * Adds a namespace mapping.
     *
     * @param uri namespace URI
     * @param prefix namespace prefix
     */
    public void addNamespaceMapping(String uri, String prefix)
    {
        if (uri != null && prefix != null)
        {
            this.namespaceMap.put(uri, prefix);
        }
    }

    /**
     * Gets the prefix for a namespace URI.
     *
     * @param uri namespace URI
     * @return prefix if found, null otherwise
     */
    public String getPrefix(String uri)
    {
        return this.namespaceMap.get(uri);
    }

    /**
     * Gets all namespace mappings.
     *
     * @return map of namespace URI to prefix
     */
    public Map<String, String> getNamespaceMap()
    {
        return namespaceMap;
    }

    /**
     * Adds a warning message.
     *
     * @param warning warning message
     */
    public void addWarning(String warning)
    {
        if (warning != null && !warning.isEmpty())
        {
            this.warnings.add(warning);
        }
    }

    /**
     * Gets all warnings.
     *
     * @return list of warnings
     */
    public List<String> getWarnings()
    {
        return warnings;
    }

    /**
     * Adds a custom mapping rule.
     *
     * @param key mapping key
     * @param value mapping value
     */
    public void addCustomMapping(String key, String value)
    {
        if (key != null && value != null)
        {
            this.customMappings.put(key, value);
        }
    }

    /**
     * Gets a custom mapping value.
     *
     * @param key mapping key
     * @return mapping value if found, null otherwise
     */
    public String getCustomMapping(String key)
    {
        return this.customMappings.get(key);
    }

    /**
     * Gets all custom mappings.
     *
     * @return map of custom mappings
     */
    public Map<String, String> getCustomMappings()
    {
        return customMappings;
    }

    /**
     * Clears all context data.
     */
    public void clear()
    {
        this.documentTypes.clear();
        this.schemas.clear();
        this.facets.clear();
        this.namespaceMap.clear();
        this.warnings.clear();
        this.customMappings.clear();
    }
}
