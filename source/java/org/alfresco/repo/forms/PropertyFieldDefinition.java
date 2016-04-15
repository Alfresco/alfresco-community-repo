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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A property field definition.
 *
 * @author Gavin Cornwell
 */
public class PropertyFieldDefinition extends FieldDefinition
{
    /** Logger */
    private static Log logger = LogFactory.getLog(PropertyFieldDefinition.class);
    
    protected String dataType;
    protected DataTypeParameters dataTypeParams;
    protected boolean mandatory = false;
    protected boolean repeats = false;
    protected IndexTokenisationMode indexTokenisationMode = IndexTokenisationMode.TRUE;
    protected List<FieldConstraint> constraints;
    
    /**
     * Default constructor
     * 
     * @param name              The name of the property
     * @param dataType          The data type of the property
     */
    public PropertyFieldDefinition(String name, String dataType)
    {
        super(name);
        
        this.dataType = dataType;
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
     * Returns the data type parameters for the field
     * 
     * @return DataTypeParameters object or null
     */
    public DataTypeParameters getDataTypeParameters()
    {
        return this.dataTypeParams;
    }

    /**
     * Sets the data type parameters for the field
     * 
     * @param dataTypeParams The DataTypeParameters for the field
     */
    public void setDataTypeParameters(DataTypeParameters dataTypeParams)
    {
        this.dataTypeParams = dataTypeParams;
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
     * Sets whether the property is mandatory
     * 
     * @param mandatory true if it is mandatory
     */
    public void setMandatory(boolean mandatory)
    {
        this.mandatory = mandatory;
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
     * Sets whether the property can contain multiple values
     * 
     * @param repeats true if the field can contain multiple values
     */
    public void setRepeating(boolean repeats)
    {
        this.repeats = repeats;
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
     * Sets the list of FieldConstraint objects for the property
     * 
     * @param constraints List of FieldConstraint objects
     */
    public void setConstraints(List<FieldConstraint> constraints)
    {
        this.constraints = constraints;
    }

    /**
     * Returns a IndexTokenisationMode the property
     * 
     * @return IndexTokenisationMode objects or null
     */
    public IndexTokenisationMode getIndexTokenisationMode()
    {
        return indexTokenisationMode;
    }

    /**
     * Sets the IndexTokenisationMode objects for the property
     * 
     * @param indexTokenisationMode objects
     */
    public void setIndexTokenisationMode(IndexTokenisationMode indexTokenisationMode)
    {
        this.indexTokenisationMode = indexTokenisationMode;
    }

    /*
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder buffer = new StringBuilder(super.toString());
        buffer.append(" (");
        buffer.append("name=").append(this.name);
        buffer.append(", dataType=").append(this.dataType);
        buffer.append(", dataTypeParams=").append(this.dataTypeParams);
        buffer.append(", label=").append(this.label);
        buffer.append(", description=").append(this.description);
        buffer.append(", binding=").append(this.binding);
        buffer.append(", defaultValue=").append(this.defaultValue);
        buffer.append(", dataKeyName=").append(this.dataKeyName);
        buffer.append(", group=").append(this.group);
        buffer.append(", protectedField=").append(this.protectedField);
        buffer.append(", mandatory=").append(this.mandatory);
        buffer.append(", repeats=").append(this.repeats);
        buffer.append(", constraints=").append(this.constraints);
        buffer.append(")");
        return buffer.toString();
    }
    
    /**
     * Represents a constraint on a property field
     */
    public static class FieldConstraint
    {
        protected String type;
        protected Map<String, Object> params;
        
        /**
         * Constructs a FieldConstraint
         * 
         * @param type      The type of the constraint
         * @param params    Map of parameters for the constraint
         */
        public FieldConstraint(String type, Map<String, Object> params)
        {
            super();
            this.type = type;
            this.params = params;
        }

        /**
         * Returns the type of the constraint
         * 
         * @return The constraint type
         */
        public String getType()
        {
            return this.type;
        }

        /**
         * Returns the parameters for the constraint
         * 
         * @return Map of parameters for the constraint or null if
         *         there are no parameters
         */
        public Map<String, Object> getParameters()
        {
            return this.params;
        }
        
        /**
         * Returns the paramters for the constraint as a JSONObject
         * 
         * @return JSONObject representation of the parameters
         */
        @SuppressWarnings("unchecked")
        public JSONObject getParametersAsJSON()
        {
            JSONObject result = null;
            
            if (this.params != null)
            {
                result = new JSONObject();
                
                for (String name : this.params.keySet())
                {
                    try
                    {
                        Object value = this.params.get(name);
                        if (value instanceof Collection)
                        {
                            // if the value is a Collection add to JSONObject as a JSONArray
                            result.put(name, new JSONArray((Collection)value));
                        }
                        else
                        {
                            result.put(name, value);
                        }
                    }
                    catch (JSONException je)
                    {
                        // just log a warning
                        if (logger.isWarnEnabled())
                            logger.warn("Failed to add constraint parameter '" + name +
                                        "' to JSON object.", je);
                    }
                }
            }
            
            return result;
        }
    }
}
