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
package org.alfresco.service.cmr.dictionary;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 * Read-only definition of a Class.
 * 
 * @author David Caruana
 */
public interface ClassDefinition
{
    /**
     * @return  defining model
     */
    public ModelDefinition getModel();

    /**
     * @return the qualified name of the class
     */
    public QName getName();
    
    /**
     * @return the human-readable class title 
     */
    public String getTitle();
    
    /**
     * @return the human-readable class description 
     */
    public String getDescription();
    
    /**
     * @return  the super class (or null, if this is the root)
     */
    public QName getParentName();
    
    /**
     * @return true => aspect, false => type
     */
    public boolean isAspect();
    
    /**
     * @return Return true if the type should be archived on delete
     */
    public boolean isArchive();

    /**
     * @return the properties of the class, including inherited properties
     */
    public Map<QName, PropertyDefinition> getProperties();
    
    /**
     * @return a map containing the default property values, including inherited properties
     */
    public Map<QName, Serializable> getDefaultValues();
    
    /**
     * Fetch all associations for which this is a source type, including child associations.
     * 
     * @return the associations including inherited ones
     * @see ChildAssociationDefinition
     */
    public Map<QName, AssociationDefinition> getAssociations();
    
    /**
     * @return true => this class supports child associations
     */
    public boolean isContainer();
    
    /**
     * Fetch only child associations for which this is a source type.
     *
     * @return all child associations applicable to this type, including those
     *         inherited from super types
     */
    public Map<QName, ChildAssociationDefinition> getChildAssociations();

    /**
     * Fetch all associations for which this is a target type, including child associations.
     * 
     * @return the associations including inherited ones
     */
    // TODO: public Map<QName, AssociationDefinition> getTargetAssociations();
    
    /**
     * @return  the default aspects associated with this type
     */
    public List<AspectDefinition> getDefaultAspects();
    
}
