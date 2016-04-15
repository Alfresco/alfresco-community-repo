/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.forms;

/**
 * Abstract representation of a field defintion.
 * 
 * @author Gavin Cornwell
 */
public abstract class FieldDefinition
{
    protected String name;
    protected String label;
    protected String description;
    protected String binding;
    protected String defaultValue;
    protected String dataKeyName;
    protected FieldGroup group;
    protected boolean protectedField = false;

    /**
     * Default constructor
     */
    public FieldDefinition(String name)
    {
        this.name = name;
    }
    
    /**
     * Returns the name of the field
     * 
     * @return The field's name
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * Returns the display label for the field
     * 
     * @return The field's display label
     */
    public String getLabel()
    {
        return this.label;
    }
    
    /**
     * Sets the display label for the field
     * 
     * @param label The field's display label
     */
    public void setLabel(String label)
    {
        this.label = label;
    }
    
    /**
     * Returns the description of the field
     * 
     * @return The field's description
     */
    public String getDescription()
    {
        return this.description;
    }
    
    /**
     * Sets the description of the field
     * 
     * @param description The field's description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    /**
     * Returns the binding for the field, this is used by some
     * FormModelProcessor implementations to generate an 
     * alternative representation of the data
     * 
     * @return The field's binding
     */
    public String getBinding()
    {
        return this.binding;
    }
    
    /**
     * Sets the binding to use for the field, this is used by some
     * FormModelProcessor implementations to generate an 
     * alternative representation of the data
     * 
     * @param binding The field's binding
     */
    public void setBinding(String binding)
    {
        this.binding = binding;
    }

    /**
     * Returns any default value the field may have
     * 
     * @return The field's default value or null if there isn't one
     */
    public String getDefaultValue()
    {
        return this.defaultValue;
    }
    
    /**
     * Sets the default value for the field
     * 
     * @param defaultValue The field's default value
     */
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }
    
    /**
     * Returns the name of the key being used to hold the data for the field
     * 
     * @return Name of the key being used to hold the data for the field
     */
    public String getDataKeyName()
    {
        return this.dataKeyName;
    }

    /**
     * Sets the name of the key to be used to hold the data for the field
     * 
     * @param dataKeyName The name of the key to be used to hold the data for the field
     */
    public void setDataKeyName(String dataKeyName)
    {
        this.dataKeyName = dataKeyName;
    }

    /**
     * Returns the group the field may be a part of
     * 
     * @return The field's group or null if it does not belong to a group
     */
    public FieldGroup getGroup()
    {
        return this.group;
    }
    
    /**
     * Sets the group the field is part of
     * 
     * @param group The group the field belongs to
     */
    public void setGroup(FieldGroup group)
    {
        this.group = group;
    }
    
    /**
     * Determines whether the field is protected i.e. it should be rendered
     * as read-only in any client displaying the field
     * 
     * @return true if the field is protected
     */
    public boolean isProtectedField()
    {
        return this.protectedField;
    }

    /**
     * Sets whether the field is protected i.e. it should be rendered
     * as read-only in any client displaying the field
     * 
     * @param protectedField true if the field is protected
     */
    public void setProtectedField(boolean protectedField)
    {
        this.protectedField = protectedField;
    }
}
