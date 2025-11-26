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
 * Represents a Nuxeo schema.
 * Schemas are named groups of related fields that can be attached to document types or facets.
 *
 * @author Alfresco Data Model Migration Team
 */
public class NuxeoSchema
{
    private String name;
    private String prefix;
    private List<NuxeoField> fields;
    private String description;

    /**
     * Default constructor.
     */
    public NuxeoSchema()
    {
        this.fields = new ArrayList<>();
    }

    /**
     * Constructor with name and prefix.
     *
     * @param name schema name
     * @param prefix schema prefix
     */
    public NuxeoSchema(String name, String prefix)
    {
        this();
        this.name = name;
        this.prefix = prefix;
    }

    /**
     * Gets the schema name.
     *
     * @return schema name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the schema name.
     *
     * @param name schema name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the schema prefix used for field naming.
     *
     * @return schema prefix
     */
    public String getPrefix()
    {
        return prefix;
    }

    /**
     * Sets the schema prefix.
     *
     * @param prefix schema prefix
     */
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    /**
     * Gets the list of fields in this schema.
     *
     * @return list of fields
     */
    public List<NuxeoField> getFields()
    {
        return fields;
    }

    /**
     * Sets the list of fields.
     *
     * @param fields list of fields
     */
    public void setFields(List<NuxeoField> fields)
    {
        this.fields = fields != null ? fields : new ArrayList<>();
    }

    /**
     * Adds a field to the schema.
     *
     * @param field field to add
     */
    public void addField(NuxeoField field)
    {
        if (field != null)
        {
            this.fields.add(field);
        }
    }

    /**
     * Gets the schema description.
     *
     * @return schema description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the schema description.
     *
     * @param description schema description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Gets a field by name.
     *
     * @param fieldName field name to search for
     * @return the field if found, null otherwise
     */
    public NuxeoField getField(String fieldName)
    {
        if (fieldName == null)
        {
            return null;
        }
        
        for (NuxeoField field : fields)
        {
            if (fieldName.equals(field.getName()))
            {
                return field;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NuxeoSchema that = (NuxeoSchema) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(prefix, that.prefix);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, prefix);
    }

    @Override
    public String toString()
    {
        return "NuxeoSchema{" +
               "name='" + name + '\'' +
               ", prefix='" + prefix + '\'' +
               ", fields=" + fields.size() +
               '}';
    }
}
