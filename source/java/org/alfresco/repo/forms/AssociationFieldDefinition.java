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
 * An association field definition that can represent a source->target association
 * or a target->source association.
 *
 * @author Gavin Cornwell
 */
public class AssociationFieldDefinition extends FieldDefinition
{
    public enum Direction { SOURCE, TARGET }
    
    protected String endpointType;
    protected Direction endpointDirection; 
    protected boolean endpointMandatory = false;
    protected boolean endpointMany = false;
    
    /**
     * Default constructor
     * 
     * @param name The name of the association
     * @param endpointType The type of the item at the end of the association
     * @param endpointDirection The direction the association is going
     */
    public AssociationFieldDefinition(String name, String endpointType, Direction endpointDirection)
    {
        super(name);
        
        this.endpointType = endpointType;
        this.endpointDirection = endpointDirection;
    }

    /**
     * Returns the type of the target of the association
     * 
     * @return The type of the target
     */
    public String getEndpointType()
    {
        return this.endpointType;
    }
    
    /**
     * Returns the direction the association is going.
     * <p>
     * <code>Direction.TARGET</code> means the endpoint is the target
     * and the field is the source.
     * <p>
     * <code>Direction.SOURCE</code> means the endpoint is the source
     * and the field is the target.
     * 
     * @return Direction.TARGET or Direction.SOURCE
     */
    public Direction getEndpointDirection()
    {
        return this.endpointDirection;
    }

    /**
     * Determines whether the target is mandatory
     * 
     * @return true if a target has to be selected
     */
    public boolean isEndpointMandatory()
    {
        return this.endpointMandatory;
    }
    
    /**
     * Sets whether the target is mandatory
     * 
     * @param endpointMandatory true if a target has to be selected
     */
    public void setEndpointMandatory(boolean endpointMandatory)
    {
        this.endpointMandatory = endpointMandatory;
    }

    /**
     * Determines if multiple targets can be selected 
     * 
     * @return true if multiple targets can be selected
     */
    public boolean isEndpointMany()
    {
        return this.endpointMany;
    }
    
    /**
     * Sets whether multiple targets can be selected 
     * 
     * @param targetMany true if multiple targets can be selected
     */
    public void setEndpointMany(boolean endpointMany)
    {
        this.endpointMany = endpointMany;
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
        buffer.append(", endpointType=").append(this.endpointType);
        buffer.append(", endpointDirection=").append(this.endpointDirection);
        buffer.append(", endpointMandatory=").append(this.endpointMandatory);
        buffer.append(", endpointMany=").append(this.endpointMany);
        buffer.append(", label=").append(this.label);
        buffer.append(", description=").append(this.description);
        buffer.append(", binding=").append(this.binding);
        buffer.append(", defaultValue=").append(this.defaultValue);
        buffer.append(", dataKeyName=").append(this.dataKeyName);
        buffer.append(", group=").append(this.group);
        buffer.append(", protectedField=").append(this.protectedField);
        buffer.append(")");
        return buffer.toString();
    }
}
