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

import java.util.List;

import org.alfresco.service.namespace.QName;

/**
 * Read-only definition of a Property.
 * 
 * @author David Caruana
 */
public interface PropertyDefinition
{
    /**
     * @return defining model 
     */
    public ModelDefinition getModel();
    
    /**
     * @return the qualified name of the property
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
     * @return the default value 
     */
    public String getDefaultValue();
    
    /**
     * @return the qualified name of the property type
     */
    public DataTypeDefinition getDataType();

    /**
     * @return Returns the owning class's defintion
     */    
    public ClassDefinition getContainerClass();
    
    /**
     * @return  true => multi-valued, false => single-valued  
     */
    public boolean isMultiValued();

    /**
     * @return  true => mandatory, false => optional
     */
    public boolean isMandatory();
    
    /**
     * @return Returns true if the system enforces the presence of
     *      {@link #isMandatory() mandatory} properties, or false if the system
     *      just marks objects that don't have all mandatory properties present.  
     */
    public boolean isMandatoryEnforced();
    
    /**
     * @return  true => system maintained, false => client may maintain 
     */
    public boolean isProtected();

    /**
     * @return  true => indexed, false => not indexed
     */
    public boolean isIndexed();
    
    /**
     * @return  true => stored in index
     */
    public boolean isStoredInIndex();

    /**
     * @return true => tokenised when it is indexed (the stored value will not be tokenised)
     */
    public boolean isTokenisedInIndex();
    
    /**
     * All non atomic properties will be indexed at the same time.
     *
     * @return true => The attribute must be indexed in the commit of the transaction. 
     * false => the indexing will be done in the background and may be out of date.
     */
    public boolean isIndexedAtomically();
    
    /**
     * Get all constraints that apply to the property value
     * 
     * @return Returns a list of property constraint definitions
     */
    public List<ConstraintDefinition> getConstraints();
}
