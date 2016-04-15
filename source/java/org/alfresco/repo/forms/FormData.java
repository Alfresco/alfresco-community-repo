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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.extensions.webscripts.servlet.FormData.FormField;

/**
 * Represents the data going to or coming from a Form.
 *
 * @author Gavin Cornwell
 */
public class FormData implements Iterable<FormData.FieldData>
{
    protected Map<String, FieldData> data;
    
    /**
     * Default constructor
     */
    public FormData()
    {
        this.data = new LinkedHashMap<String, FieldData>(8);
    }
    
    /**
     * Determines whether field data for the given item exists.
     * 
     * @param fieldName Name of field to look for
     * @return true if the field exists, false otherwise
     */
    public boolean hasFieldData(String fieldName)
    {
        return this.data.containsKey(fieldName);
    }
    
    /**
     * Returns the data for the given field.
     * 
     * @param fieldName Name of field to look for 
     * @return FieldData object representing the data for
     * the field or null if it doesn't exist
     */
    public FieldData getFieldData(String fieldName)
    {
        return this.data.get(fieldName);
    }
    
    /**
     * Adds the given data to the form.
     * <p>
     * NOTE: Adding the same named data will append the value and 
     * thereafter return a List containing all added values. 
     * </p>
     * 
     * @param fieldName The name of the field
     * @param fieldValue The value of the data
     */
    public void addFieldData(String fieldName, Object fieldValue)
    {
        this.addFieldData(fieldName, fieldValue, false);
    }
    
    /**
     * Adds the given webscript FormField object to the form.
     * 
     * @param field A WebScript FormField object
     */
    public void addFieldData(FormField field)
    {
        FieldData fieldData = new FieldData(field);
        this.data.put(fieldData.getName(), fieldData);
    }
    
    /**
     * Adds the given data to the form. If data for the field is already
     * present the behaviour is controlled by the overwrite property.
     * <p>
     * If overwrite is true the provided value replaces the existing value
     * whereas false will force the creation of a List (if necessary) and the 
     * provided value will be added to the List.
     * </p>
     * 
     * @param fieldName The name of the field
     * @param fieldValue The value of the data
     * @param overwrite boolean
     */
    @SuppressWarnings("unchecked")
    public void addFieldData(String fieldName, Object fieldValue, boolean overwrite)
    {
        // check whether some data already exists
        if (this.data.containsKey(fieldName))
        {
            // if we are overwriting just replace with provided data
            if (overwrite)
            {
                this.data.put(fieldName, new FieldData(fieldName, fieldValue, false));
            }
            else
            {
                // pull out the existing value and create a List if necessary
                List currentValues = null;
                Object currentValue = this.data.get(fieldName).getValue();
                if (currentValue instanceof List)
                {
                    currentValues = (List)currentValue;
                }
                else
                {
                    // a non List value is present, create the new list
                    // and add the current value to it
                    currentValues = new ArrayList(4);
                    currentValues.add(currentValue);
                    this.data.put(fieldName, new FieldData(fieldName, currentValues, false));
                }
                
                // add the provided value to the list
                currentValues.add(fieldValue);
            }
        }
        else
        {
            this.data.put(fieldName, new FieldData(fieldName, fieldValue, false));
        }
    }
    
    /**
     * Removes the data associated with the given field
     * if it exists.
     * 
     * @param fieldName Name of the field to remove
     */
    public void removeFieldData(String fieldName)
    {
        this.data.remove(fieldName);
    }
    
    /**
     * Returns a list of the names of the fields held by this
     * object.
     * 
     * @return List of String objects
     */
    public Set<String> getFieldNames()
    {
        return this.data.keySet();
    }
    
    /**
     * Returns the number of fields data is being held for.
     * 
     * @return Number of fields
     */
    public int getNumberOfFields()
    {
        return this.data.size();
    }
    
    /**
     * Returns an Iterator over the FieldData objects
     * held by this object.
     * 
     * @return Iterator of FieldData
     */
    public Iterator<FormData.FieldData> iterator()
    {
        return this.data.values().iterator();
    }
    
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder(super.toString());
        buffer.append(" (");
        buffer.append("data=").append(this.data);
        buffer.append(")");
        return buffer.toString();
    }

    /**
     * Inner class to represent the value of a field on a form
     *
     * @author Gavin Cornwell
     */
    public class FieldData
    {
        protected String name;
        protected Object value;
        protected boolean isFile = false;
        protected InputStream is;
        
        /**
         * Default Constructor 
         * 
         * @param name The name of the form field
         * @param value The value of the form field
         * @param isFile Whether the field data represents an uploaded file
         */
        public FieldData(String name, Object value, boolean isFile)
        {
            this.name = name;
            this.value = value;
            this.isFile = isFile;
        }
        
        /**
         * Constructs a FieldData object from a WebScript FormField object
         * 
         * @param field The WebScript FormData object to create the field from
         */
        public FieldData(FormField field)
        {
            this.name = field.getName();
            this.value = field.getValue();
            this.isFile = field.getIsFile();
           
            if (isFile)
            {
                is = field.getInputStream();
            }
        }

        /**
         * Returns the name of the form field that data represents
         * 
         * @return The name
         */
        public String getName()
        {
            return this.name;
        }

        /**
         * Returns the value of the form field that data represents
         * 
         * @return The value
         */
        public Object getValue()
        {
            return this.value;
        }

        /**
         * Determines whether the data represents a file
         * 
         * @return true if the data is a file
         */
        public boolean isFile()
        {
            return this.isFile;
        }
        
        /**
         * Returns an InputStream onto the content of the file,
         * throws IllegalStateException if this is called for
         * non file field data
         * 
         * @return An InputStream onto the file
         */
        public InputStream getInputStream()
        {
            return this.is;
        }
        
        /*
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            StringBuilder buffer = new StringBuilder(super.toString());
            buffer.append(" (");
            buffer.append("name=").append(this.name);
            buffer.append(", value=").append(this.value);
            buffer.append(", isFile=").append(this.isFile);
            buffer.append(")");
            return buffer.toString();
        }
    }
}
