/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.node.getchildren;

import org.alfresco.service.namespace.QName;

/**
 * GetChildren - for string property filtering
 *
 * @author janv
 * @since 4.0
 */
public class FilterPropString implements FilterProp
{
    public static enum FilterTypeString implements FilterType
    {
        STARTSWITH_IGNORECASE,
        STARTSWITH,
        EQUALS_IGNORECASE,
        EQUALS
    }
    
    private QName propName;
    private String propVal;
    private FilterTypeString filterType;
    
    public FilterPropString(QName propName, String propVal, FilterTypeString filterType)
    {
        this.propName = propName;
        this.propVal = propVal;
        this.filterType = filterType;
        
    }
    
    public QName getPropName()
    {
        return propName;
    }
    
    public String getPropVal()
    {
        return propVal;
    }
    
    public FilterType getFilterType()
    {
        return filterType;
    }
}
