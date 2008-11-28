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
    protected FieldGroup group;
    protected boolean protectedField;

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
     * Returns the description of the field
     * 
     * @return The field's description
     */
    public String getDescription()
    {
        return this.description;
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
     * Returns any default value the field may have
     * 
     * @return The field's default value or null if there isn't one
     */
    public String getDefaultValue()
    {
        return this.defaultValue;
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
     * Determines whether the field is protected i.e. it should be rendered
     * as read-only in any client displaying the field
     * 
     * @return true if the field is protected
     */
    public boolean isProtectedField()
    {
        return this.protectedField;
    }
}
