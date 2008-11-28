/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.forms;

import java.util.List;
import java.util.Map;

/**
 * A property field definition.
 *
 * @author Gavin Cornwell
 */
public class PropertyFieldDefiniton extends FieldDefinition
{
    protected String dataType;
    protected boolean mandatory;
    protected boolean repeats;
    protected List<FieldConstraint> constraints;
    
    /**
     * Constructs a FieldDefinition
     * 
     * @param name              The name of the property
     * @param label             The display label of the property
     * @param description       The description of the property
     * @param dataType          The data type of the property
     * @param defaultValue      Default value of the property
     * @param binding           Binding of the property
     * @param protectedField    Whether the property should be read only
     * @param mandatory         Whether the property is mandatory
     * @param repeats           Whether the property can contain multiple values
     * @param group             The group the property belongs to
     * @param constraints       List of constraints the property has
     */
    // TODO: Look at the Builder pattern to reduce the size of the constructor!!
    public PropertyFieldDefiniton(String name, String label, String description,
                String dataType, String defaultValue, String binding, 
                boolean protectedField, boolean mandatory, boolean repeats, 
                FieldGroup group, List<FieldConstraint> constraints)
    {
        this.name = name;
        this.label = label;
        this.description = description;
        this.defaultValue = defaultValue;
        this.binding = binding;
        this.protectedField = protectedField;
        this.group = group;
        
        this.dataType = dataType;
        this.mandatory = mandatory;
        this.repeats = repeats;
        this.constraints = constraints;
    }
    
    /**
     * Returns the dataType for the property, this is a value from the 
     * Alfresco data dictionary i.e. d:text, d:int etc.
     * 
     * @return The field's data type
     */
    public String getDataType()
    {
        return this.dataType;
    }

    /**
     * Determines if the property is mandatory
     * 
     * @return true if the field is mandatory
     */
    public boolean isMandatory()
    {
        return this.mandatory;
    }

    /**
     * Determines if the property can contain multiple values
     * 
     * @return true if the field can contain multiple values
     */
    public boolean isRepeating()
    {
        return this.repeats;
    }

    /**
     * Returns a list of constraints the property may have
     * 
     * @return List of FieldContstraint objects or null if there are
     *         no constraints for the field
     */
    public List<FieldConstraint> getConstraints()
    {
        return this.constraints;
    }

    /**
     * Represents a constraint on a property field
     */
    public class FieldConstraint
    {
        protected String name;
        protected Map<String, String> params;
        
        /**
         * Constructs a FieldConstraint
         * 
         * @param name      The name of the constraint
         * @param params    Map of parameters for the constraint
         */
        public FieldConstraint(String name, Map<String, String> params)
        {
            super();
            this.name = name;
            this.params = params;
        }

        /**
         * Returns the name of the constraint
         * 
         * @return The constraint name
         */
        public String getName()
        {
            return this.name;
        }

        /**
         * Returns the parameters for the constraint
         * 
         * @return Map of parameters for the constraint or null if
         *         there are no parameters
         */
        public Map<String, String> getParams()
        {
            return this.params;
        }
    }
}
