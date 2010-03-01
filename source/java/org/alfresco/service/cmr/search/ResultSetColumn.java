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
package org.alfresco.service.cmr.search;

import org.alfresco.service.namespace.QName;

/**
 * The metadata for a column in a result set.
 * All columns should have a data type, they may have a property type.
 * 
 * @author andyh
 *
 */
public interface ResultSetColumn
{
    /**
     * The column name
     * @return - the column name
     */
    public String getName();
    
    /**
     * The type of the column
     * @return - the data type for the column
     */
    public QName getDataType();
    
    /**
     * The property definition if there is one for the column 
     * @return - the property definition or null if it does not make sense for the column 
     */
    public QName getPropertyType();
}
