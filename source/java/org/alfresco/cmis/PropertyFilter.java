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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Property filter supporting CMIS filter expression
 * 
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
public class PropertyFilter
{
    public static final String MATCH_ALL_FILTER = "*";
    public static final String PROPERTY_NAME_TOKENS_DELIMITER = ",";

    private static final Pattern PROPERTY_FILTER_REGEX = Pattern.compile("^([^\\s,\"'\\\\\\.\\(\\)]+)(,[^\\s,\"'\\\\\\.\\(\\)]+)*$");
    private Set<String> properties;

    /**
     * @param filter filter value (case insensitive)
     * @throws FilterNotValidException if filter string isn't valid
     */
    public PropertyFilter(String filter) throws CMISFilterNotValidException
    {
        if (filter != null)
        {
            if (!PROPERTY_FILTER_REGEX.matcher(filter).matches())
            {
                throw new CMISFilterNotValidException("Property filter \"" + filter + "\" is invalid");
            }

            if (!filter.equals(MATCH_ALL_FILTER))
            {
                String[] tokens = filter.split(PROPERTY_NAME_TOKENS_DELIMITER);
                properties = new HashSet<String>(tokens.length * 2);
                for (String token : tokens)
                {
                    properties.add(token);
                }
            }
        }
    }

    /**
     * @param property property token name
     * @return <b>true</b> returns if property is allowed by filter. In other case returns <b>false</b>
     */
    public boolean allow(String property)
    {
        return properties == null || properties.contains(property);
    }
}