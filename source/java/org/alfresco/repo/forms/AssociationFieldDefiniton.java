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
 * An association field definition.
 *
 * @author Gavin Cornwell
 */
public class AssociationFieldDefiniton extends FieldDefinition
{
    // 
    // TODO: Think about bi-directional association support,
    //       should we instead model as endpointType etc.
    //       and have a 'direction' flag, that would then allow
    //       form clients to display source objects not just
    //       target objects
    // 
    
    protected String targetType;
    protected String targetRole;
    protected boolean targetMandatory;
    protected boolean targetMany;
    
    // TODO: This may not even be needed for forms, what difference does it make!?
    protected boolean childAssociation;
    
    /**
     * Constructs an AssociationFieldDefinition
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
     * @param targetMandatory   Whether a target is mandatory
     * @param targetMany        Whether there can be multiple targets
     * @param targetType        The type of the target
     * @param childAssociation  Whether the association is a child association
     */
    // TODO: Look at the Builder pattern to reduce the size of the constructor!!
    public AssociationFieldDefiniton(String name, String label, String description,
                String defaultValue, String binding, boolean protectedField, 
                FieldGroup group, boolean targetMandatory, boolean targetMany, 
                String targetRole, String targetType, boolean childAssociation)
    {
        this.name = name;
        this.label = label;
        this.description = description;
        this.defaultValue = defaultValue;
        this.binding = binding;
        this.protectedField = protectedField;
        this.group = group;
        
        this.targetMandatory = targetMandatory;
        this.targetMany = targetMany;
        this.targetType = targetType;
        
        this.childAssociation = childAssociation;
    }

    /**
     * Returns the type of the target of the association
     * 
     * @return The type of the target
     */
    public String getTargetType()
    {
        return this.targetType;
    }

    /**
     * Determines whether the target is mandatory
     * 
     * @return true if a target has to be selected
     */
    public boolean isTargetMandatory()
    {
        return this.targetMandatory;
    }

    /**
     * Determines if multiple targets can be selected 
     * 
     * @return true if multiple targets can be selected
     */
    public boolean isTargetMany()
    {
        return this.targetMany;
    }

    /**
     * Determines if the association is a child association
     * 
     * @return true if the association is a child association
     */
    public boolean isChildAssociation()
    {
        return this.childAssociation;
    }
}
