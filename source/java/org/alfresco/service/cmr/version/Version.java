/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.service.cmr.version;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Version interface.
 * 
 * Allows access to version property values and frozen state node references.
 * The version history tree can also be navigated.
 * 
 * @author Roy Wetherall
 */
public interface Version extends Serializable
{
    /**
     * Names of the system version properties
     */
    public static final String PROP_DESCRIPTION = "description";
    
    /**
     * Helper method to get the created date from the version property data.
     * 
     * @return  the date the version was created
     */
    public Date getCreatedDate();
    
    /**
     * Helper method to get the creator of the version.
     * 
     * @return  the creator of the version
     */
    public String getCreator();

    /**
     * Helper method to get the version label from the version property data.
     * 
     * @return  the version label
     */
    public String getVersionLabel();
    
    /**
     * Helper method to get the version type.
     * 
     * @return  the value of the version type as an enum value
     */
    public VersionType getVersionType();
    
    /**
     * Helper method to get the version description.
     * 
     * @return the version description
     */
    public String getDescription();

    /**
     * Get the map containing the version property values
     * 
     * @return  the map containing the version properties
     */
    public Map<String, Serializable> getVersionProperties();
    
    /**
     * Gets the value of a named version property.
     * 
     * @param name  the name of the property
     * @return      the value of the property
     * 
     */
    public Serializable getVersionProperty(String name);

    /**
     * Gets a reference to the node that this version was created from.
     * <p>
     * Note that this reference will be to the current state of the versioned
     * node which may now correspond to a later version.
     * 
     * @return  a node reference
     */
    public NodeRef getVersionedNodeRef();
    
    /**
     * Gets the reference to the node that contains the frozen state of the
     * version.
     * 
     * @return  a node reference
     */
    public NodeRef getFrozenStateNodeRef();
}
