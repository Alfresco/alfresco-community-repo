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
 * Represents a field in a Nuxeo schema.
 * Fields are individual metadata properties that can have types, constraints, and validation rules.
 *
 * @author Alfresco Data Model Migration Team
 */
public class NuxeoField
{
    private String name;
    private String type;
    private boolean required;
    private boolean multiValued;
    private String defaultValue;
    private List<String> constraints;
    private String description;

    /**
     * Default constructor.
     */
    public NuxeoField()
    {
        this.constraints = new ArrayList<>();
    }

    /**
     * Constructor with name and type.
     *
     * @param name field name
     * @param type field type
     */
    public NuxeoField(String name, String type)
    {
        this();
        this.name = name;
        this.type = type;
    }

    /**
     * Gets the field name.
     *
     * @return field name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the field name.
     *
     * @param name field name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the field type (e.g., string, long, date, blob).
     *
     * @return field type
     */
    public String getType()
    {
        return type;
    }

    /**
     * Sets the field type.
     *
     * @param type field type
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Checks if the field is required.
     *
     * @return true if required, false otherwise
     */
    public boolean isRequired()
    {
        return required;
    }

    /**
     * Sets whether the field is required.
     *
     * @param required required flag
     */
    public void setRequired(boolean required)
    {
        this.required = required;
    }

    /**
     * Checks if the field is multi-valued (array type).
     *
     * @return true if multi-valued, false otherwise
     */
    public boolean isMultiValued()
    {
        return multiValued;
    }

    /**
     * Sets whether the field is multi-valued.
     *
     * @param multiValued multi-valued flag
     */
    public void setMultiValued(boolean multiValued)
    {
        this.multiValued = multiValued;
    }

    /**
     * Gets the default value for the field.
     *
     * @return default value
     */
    public String getDefaultValue()
    {
        return defaultValue;
    }

    /**
     * Sets the default value for the field.
     *
     * @param defaultValue default value
     */
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    /**
     * Gets the list of constraints applied to this field.
     *
     * @return list of constraints
     */
    public List<String> getConstraints()
    {
        return constraints;
    }

    /**
     * Sets the list of constraints.
     *
     * @param constraints list of constraints
     */
    public void setConstraints(List<String> constraints)
    {
        this.constraints = constraints != null ? constraints : new ArrayList<>();
    }

    /**
     * Adds a constraint to the field.
     *
     * @param constraint constraint to add
     */
    public void addConstraint(String constraint)
    {
        if (constraint != null && !constraint.isEmpty())
        {
            this.constraints.add(constraint);
        }
    }

    /**
     * Gets the field description.
     *
     * @return field description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the field description.
     *
     * @param description field description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Gets the full type including array notation if multi-valued.
     *
     * @return full type string
     */
    public String getFullType()
    {
        return multiValued ? type + "[]" : type;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NuxeoField that = (NuxeoField) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(type, that.type);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, type);
    }

    @Override
    public String toString()
    {
        return "NuxeoField{" +
               "name='" + name + '\'' +
               ", type='" + getFullType() + '\'' +
               ", required=" + required +
               '}';
    }
}
