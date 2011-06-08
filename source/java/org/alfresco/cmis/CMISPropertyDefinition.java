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
package org.alfresco.cmis;

import java.util.Collection;

import org.alfresco.opencmis.dictionary.CMISPropertyAccessor;
import org.alfresco.opencmis.dictionary.CMISPropertyLuceneBuilder;


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
     * For variable length properties, get the maximum length allowed.
     * 
     * @return
     */
    public int getMaximumLength();

    /**
     * For Integer and Decimal properties, get the minimum value allowed
     * 
     * @return
     */
    public Double getMinValue();
    
    /**
     * For Integer and Decimal properties, get the maximum value allowed
     * 
     * @return
     */
    public Double getMaxValue();
    
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
