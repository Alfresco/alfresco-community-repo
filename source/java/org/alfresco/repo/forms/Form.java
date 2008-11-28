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
 * Data representation of a form to be displayed in the UI.
 *
 * @author Gavin Cornwell
 */
public class Form
{
    protected String item;
    protected String submissionUrl;
    protected String type;
    protected List<FieldDefinition> fieldDefinitions;
    protected List<FieldGroup> fieldGroups;
    protected Map<String, String> formData;
    
    /**
     * Constructs a Form
     * 
     * @param item              The identifier
     * @param submissionUrl     Default submission URL
     * @param type              The type of the item
     * @param fieldDefinitions  List of fields that could be displayed
     * @param fieldGroups       List of field groups
     * @param formData          Map of the form data
     */
    public Form(String item, String submissionUrl, String type, 
                List<FieldDefinition> fieldDefinitions, List<FieldGroup> fieldGroups,
                Map<String, String> formData)
    {
        this.fieldDefinitions = fieldDefinitions;
        this.fieldGroups = fieldGroups;
        this.formData = formData;
        this.item = item;
        this.submissionUrl = submissionUrl;
        this.type = type;
    }

    /**
     * Returns an identifier for the item the form is for, in the case of a node
     * it will be a NodeRef, for a task, a task id etc.
     * 
     * @return The item
     */
    public String getItem()
    {
        return this.item;
    }
    
    /**
     * Returns the submission URL to use to post back to this service, acts as a
     * default URL for clients to use
     * 
     * @return The default submission URL
     */
    public String getSubmissionUrl()
    {
        return this.submissionUrl;
    }
    
    /**
     * Returns the type of the item the form is for, could be a content model type, a
     * workflow task type, an XML schema etc.
     * 
     * @return The type of the item
     */
    public String getType()
    {
        return this.type;
    }
       
    /**
     * Returns the list of fields appropriate for the item
     * 
     * @return List of FieldDefintion objects or null if there are no fields
     */
    public List<FieldDefinition> getFieldDefinitions()
    {
        return this.fieldDefinitions;
    }
    
    /**
     * Returns the list of field groups for the form 
     * 
     * @return List of FieldGroup objects or null if there are no groups
     */
    public List<FieldGroup> getFieldGroups()
    {
        return this.fieldGroups;
    }
    
    /**
     * Returns the data to display in the form
     * 
     * @return Map of String objects representing the form data or null if
     *         there is no data
     */
    public Map<String, String> getFormData()
    {
        return this.formData;
    }
}





