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
package org.alfresco.repo.nuxeo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Nuxeo document type.
 * Document types define the structure and behavior of documents in Nuxeo.
 * They can inherit from parent types and include multiple schemas and facets.
 *
 * @author Alfresco Data Model Migration Team
 */
public class NuxeoDocumentType
{
    private String name;
    private String parent;
    private List<String> schemas;
    private List<String> facets;
    private String description;

    /**
     * Default constructor.
     */
    public NuxeoDocumentType()
    {
        this.schemas = new ArrayList<>();
        this.facets = new ArrayList<>();
    }

    /**
     * Constructor with name.
     *
     * @param name document type name
     */
    public NuxeoDocumentType(String name)
    {
        this();
        this.name = name;
    }

    /**
     * Constructor with name and parent.
     *
     * @param name document type name
     * @param parent parent document type
     */
    public NuxeoDocumentType(String name, String parent)
    {
        this(name);
        this.parent = parent;
    }

    /**
     * Gets the document type name.
     *
     * @return document type name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the document type name.
     *
     * @param name document type name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the parent document type.
     *
     * @return parent document type name
     */
    public String getParent()
    {
        return parent;
    }

    /**
     * Sets the parent document type.
     *
     * @param parent parent document type name
     */
    public void setParent(String parent)
    {
        this.parent = parent;
    }

    /**
     * Gets the list of schemas included in this document type.
     *
     * @return list of schema names
     */
    public List<String> getSchemas()
    {
        return schemas;
    }

    /**
     * Sets the list of schemas.
     *
     * @param schemas list of schema names
     */
    public void setSchemas(List<String> schemas)
    {
        this.schemas = schemas != null ? schemas : new ArrayList<>();
    }

    /**
     * Adds a schema to the document type.
     *
     * @param schemaName schema name to add
     */
    public void addSchema(String schemaName)
    {
        if (schemaName != null && !schemaName.isEmpty() && !this.schemas.contains(schemaName))
        {
            this.schemas.add(schemaName);
        }
    }

    /**
     * Gets the list of facets included in this document type.
     *
     * @return list of facet names
     */
    public List<String> getFacets()
    {
        return facets;
    }

    /**
     * Sets the list of facets.
     *
     * @param facets list of facet names
     */
    public void setFacets(List<String> facets)
    {
        this.facets = facets != null ? facets : new ArrayList<>();
    }

    /**
     * Adds a facet to the document type.
     *
     * @param facetName facet name to add
     */
    public void addFacet(String facetName)
    {
        if (facetName != null && !facetName.isEmpty() && !this.facets.contains(facetName))
        {
            this.facets.add(facetName);
        }
    }

    /**
     * Gets the document type description.
     *
     * @return document type description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the document type description.
     *
     * @param description document type description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NuxeoDocumentType that = (NuxeoDocumentType) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }

    @Override
    public String toString()
    {
        return "NuxeoDocumentType{" +
               "name='" + name + '\'' +
               ", parent='" + parent + '\'' +
               ", schemas=" + schemas.size() +
               ", facets=" + facets.size() +
               '}';
    }
}
