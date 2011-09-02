/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.forms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.repo.forms.FormData.FieldData;

/**
 * Data representation of a form to be displayed in the UI.
 *
 * @author Gavin Cornwell
 */
public class Form
{
    protected Item item;
    protected String submissionUrl;
    protected List<FieldDefinition> fieldDefinitions;
    protected Collection<FieldGroup> fieldGroups;
    protected FormData data;
    
    /**
     * Constructs a Form
     * 
     * @param item The item the form is for
     */
    public Form(Item item)
    {
        this.item = item;
    }

    /**
     * Returns the item the form is for
     * 
     * @return The item
     */
    public Item getItem()
    {
        return this.item;
    }
    
    /**
     * Returns the submission URL to use for the form
     * 
     * @return URL to submit to
     */
    public String getSubmissionUrl()
    {
        return this.submissionUrl;
    }
    
    /**
     * Sets the submission URL the form should use
     * 
     * @param url URL to submit to
     */
    public void setSubmissionUrl(String url)
    {
        this.submissionUrl = url;
    }
       
    /**
     * Returns the list of field definitions for the form
     * 
     * @return List of FieldDefinition objects or null if there are no fields
     */
    public List<FieldDefinition> getFieldDefinitions()
    {
        return this.fieldDefinitions;
    }
    
    public List<String> getFieldDefinitionNames()
    {
        List<String> result = new ArrayList<String>(fieldDefinitions.size());
        for (FieldDefinition fieldDefn : fieldDefinitions)
        {
            result.add(fieldDefn.getName());
        }
        return result;
    }
    
    /**
     * Sets the list of FieldDefinition objects representing the fields the
     * form is able to display
     * 
     * @param fieldDefinitions List of FieldDefinition objects
     */
    public void setFieldDefinitions(List<FieldDefinition> fieldDefinitions)
    {
        this.fieldDefinitions = fieldDefinitions;
    }
    
    /**
     * Adds the given FieldDefinition to the form.
     * <p>
     * NOTE: Multiple fields with the same name can be added to the list,
     *       it is therefore the form processor and the client of the 
     *       FormService responsibility to differentiate the fields in
     *       some way i.e. by type, property vs. association.
     * 
     * @param definition The FieldDefinition to add
     */
    public void addFieldDefinition(FieldDefinition definition)
    {
        if (this.fieldDefinitions == null)
        {
            this.fieldDefinitions = new ArrayList<FieldDefinition>(8);
        }
        
        this.fieldDefinitions.add(definition);
    }
    
    /**
     * Returns the collection of field groups for the form 
     * 
     * @return Collection of FieldGroup objects or null if there are no groups
     */
    public Collection<FieldGroup> getFieldGroups()
    {
        return this.fieldGroups;
    }
    
    /**
     * Sets the collection of FieldGroup objects representing the groups of
     * fields the form should display and maintain
     * 
     * @param fieldGroups Collection of FieldGroup objects
     */
    public void setFieldGroups(Collection<FieldGroup> fieldGroups)
    {
        this.fieldGroups = fieldGroups;
    }
    
    /**
     * Returns the data to display in the form
     * 
     * @return FormData object holding the data of the form or null
     *         if there is no data i.e. for a create form
     */
    public FormData getFormData()
    {
        return this.data;
    }

    /**
     * Sets the data this form should display. This will overwrite
     * any existing form data being held
     * 
     * @param data FormData instance containing the data
     */
    public void setFormData(FormData data)
    {
        this.data = data;
    }

    /**
     * Returns <code>true</code> if the Form contains {@link FieldData} for the
     * specified <code>dataKey</code>.
     * 
     * @param dataKey The dataKey for the field.
     * @return
     */
    public boolean dataExists(String dataKey)
    {
        if(data == null)
            return false;
        return data.getFieldNames().contains(dataKey);
    }
    
    /**
     * Adds some data to be displayed by the form
     * 
     * @param fieldName Name of the field the data is for
     * @param fieldData The value
     */
    public void addData(String fieldName, Object fieldData)
    {
        if (this.data == null)
        {
            this.data = new FormData();
        }
        
        this.data.addFieldData(fieldName, fieldData);
    }
    
     /**
     * Adds a {@link Field} to the form by adding the {@link FieldDefinition}
     * and the value if any.
     * 
     * @param field
     */
    public void addField(Field field)
    {
        if (field == null)
        {
            return;
        }
        
        FieldDefinition fieldDefinition = field.getFieldDefinition();
        addFieldDefinition(fieldDefinition);
        Object value = field.getValue();
        
        if (value != null)
        {
            addData(fieldDefinition.getDataKeyName(), value);
        }
    }
    
    /**
     * Adds a {@link Collection} of {@link Field Fields} to the form by adding the {@link FieldDefinition FieldDefinitions}
     * and the values if any.
     */
    public void addFields(Collection<Field> fields)
    {
        for (Field field : fields) 
        {
            addField(field);
        }
    }
    
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder(super.toString());
        buffer.append(" (");
        buffer.append("item=").append(this.item);
        buffer.append(", submissionUrl=").append(this.submissionUrl);
        buffer.append(", fieldGroups=").append(this.fieldGroups);
        buffer.append("\nfieldDefinitions=").append(this.fieldDefinitions);
        buffer.append("\nformData=").append(this.data);
        buffer.append(")");
        return buffer.toString();
    }
}
