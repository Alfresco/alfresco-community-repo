/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.cmis;

import java.util.Collection;
import java.util.Map;


/**
 * The base type definition for CMIS
 * 
 * @author andyh
 */
public interface CMISTypeDefinition
{
    /**
     * @return  true => type definition is for public consumption
     */
    public boolean isPublic();
    
    /**
     * Get the unique identifier for the type
     * 
     * @return - the type id
     */
    public CMISTypeId getTypeId();

    /**
     * Get the table name used for queries against the type. This is also a unique identifier for the type. The string
     * conforms to SQL table naming conventions. TODO: Should we impose a maximum length and if so how do we avoid
     * collisions from truncations?
     * 
     * @return the sql table name
     */
    public String getQueryName();

    /**
     * Get the display name for the type.
     * 
     * @return - the display name
     */
    public String getDisplayName();

    /**
     * Get the type for the parent
     * 
     * @return - the parent type id
     */
    public CMISTypeDefinition getParentType();

    /**
     * Get the sub-types
     * 
     * @param descendants
     * @return
     */
    public Collection<CMISTypeDefinition> getSubTypes(boolean descendants);
    
    /**
     * Get the base type
     * 
     * @return
     */
    public CMISTypeDefinition getBaseType();
    
    /**
     * Get the description for the type
     * 
     * @return - the description
     */
    public String getDescription();

    /**
     * Can objects of this type be created?
     * 
     * @return
     */
    public boolean isCreatable();

    /**
     * Are objects of this type fileable?
     * 
     * @return
     */
    public boolean isFileable();

    /**
     * Is this type queryable? If not, the type may not appear in the FROM clause of a query. This property of the type
     * is not inherited in the type hierarchy. It is set on each type.
     * 
     * @return true if queryable
     */
    public boolean isQueryable();
    
    /**
     * Is the type full text indexed for querying via CONTAINS()
     * @return
     */
    public boolean isFullTextIndexed();

    /**
     * Are objects of this type controllable via Policies.
     * 
     * @return
     */
    public boolean isControllablePolicy();

    /**
     * Are objects of this type controllable via ACLs.
     * 
     * @return
     */
    public boolean isControllableACL();

    /**
     * Are objects of this type included in super type queries
     * 
     * @return
     */
    public boolean isIncludedInSuperTypeQuery();

    /**
     * Is this type versionable? If true this implies all instances of the type are versionable.
     * 
     * @return true if versionable
     */
    public boolean isVersionable();

    /**
     * Is a content stream allowed for this type? It may be disallowed, optional or mandatory.
     * 
     * @return
     */
    public CMISContentStreamAllowedEnum getContentStreamAllowed();

    /**
     * For an association, get the collection of valid source types. For non-associations the collection will be empty.
     * 
     * @return
     */
    public Collection<CMISTypeDefinition> getAllowedSourceTypes();

    /**
     * For an association, get the collection of valid target types. For non-associations the collection will be empty.
     * 
     * @return
     */
    public Collection<CMISTypeDefinition> getAllowedTargetTypes();

    /**
     * Gets the property definitions for this type (owned and inherited)
     * 
     * @return  property definitions
     */
    public Map<String, CMISPropertyDefinition> getPropertyDefinitions();

    /**
     * Gets the property definitions owned by this type
     * 
     * @return
     */
    public Map<String, CMISPropertyDefinition> getOwnedPropertyDefinitions();
    
    /**
     * Gets the Action evaluators for this type
     * 
     * @return
     */
    public Map<CMISAllowedActionEnum, CMISActionEvaluator> getActionEvaluators();
}
