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
 * Represents a Nuxeo facet.
 * Facets are markers that enable behaviors and can include schemas.
 * They are similar to Alfresco aspects but less dynamic.
 *
 * @author Alfresco Data Model Migration Team
 */
public class NuxeoFacet
{
    private String name;
    private List<String> schemas;
    private String description;

    /**
     * Default constructor.
     */
    public NuxeoFacet()
    {
        this.schemas = new ArrayList<>();
    }

    /**
     * Constructor with name.
     *
     * @param name facet name
     */
    public NuxeoFacet(String name)
    {
        this();
        this.name = name;
    }

    /**
     * Gets the facet name.
     *
     * @return facet name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the facet name.
     *
     * @param name facet name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the list of schemas included in this facet.
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
     * Adds a schema to the facet.
     *
     * @param schemaName schema name to add
     */
    public void addSchema(String schemaName)
    {
        if (schemaName != null && !schemaName.isEmpty())
        {
            this.schemas.add(schemaName);
        }
    }

    /**
     * Gets the facet description.
     *
     * @return facet description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the facet description.
     *
     * @param description facet description
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
        NuxeoFacet that = (NuxeoFacet) o;
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
        return "NuxeoFacet{" +
               "name='" + name + '\'' +
               ", schemas=" + schemas +
               '}';
    }
}
