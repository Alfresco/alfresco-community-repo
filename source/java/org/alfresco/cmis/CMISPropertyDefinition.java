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


/**
 * CMIS Property Definition
 * 
 * @author andyh
 */
public interface CMISPropertyDefinition
{
    /**
     * Get Property Id
     * 
     * @return
     */
    public CMISPropertyId getPropertyId();

    /**
     * Get Owning Type
     * 
     * @return
     */
    public CMISTypeDefinition getOwningType();

    /**
     * Get the query name
     * @return
     */
    public String getQueryName();
    
    /**
     * Get the display name
     * 
     * @return
     */
    public String getDisplayName();

    /**
     * Get the description
     * 
     * @return
     */
    public String getDescription();

    /**
     * Get the property type
     * 
     * @return
     */
    public CMISDataTypeEnum getDataType();

    /**
     * Get the cardinality
     * 
     * @return
     */
    public CMISCardinalityEnum getCardinality();

    /**
     * Get the choices available as values for this property TODO: not implemented yet
     * 
     * @return
     */
    public Collection<CMISChoice> getChoices();

    /**
     * Is this a choice where a user can enter other values (ie a list with common options)
     * 
     * @return
     */
    public boolean isOpenChoice();

    /**
     * Is this property required?
     * 
     * @return
     */
    public boolean isRequired();

    /**
     * get the default value as a String
     * 
     * @return
     */
    public String getDefaultValue();

    /**
     * Is this property updatable?
     * 
     * @return
     */
    public CMISUpdatabilityEnum getUpdatability();

    /**
     * Is this property queryable?
     * 
     * @return
     */
    public boolean isQueryable();

    /**
     * Is this property orderable in queries?
     * 
     * @return
     */
    public boolean isOrderable();
    
    /**
     * For variable length properties, get the maximum length allowed. Unsupported.
     * 
     * @return
     */
    public int getMaximumLength();

    /**
     * Gets the property accessor (for reading / writing values)
     * 
     * @return
     */
    public CMISPropertyAccessor getPropertyAccessor();
    
    /**
     * Gets the property Lucene builder
     * 
     * @return
     */
    public CMISPropertyLuceneBuilder getPropertyLuceneBuilder();

}
