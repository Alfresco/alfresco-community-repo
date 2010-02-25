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
package org.alfresco.repo.cmis;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.alfresco.repo.cmis.ws.CmisException;
import org.alfresco.repo.cmis.ws.EnumServiceException;
import org.alfresco.repo.cmis.ws.utils.ExceptionUtil;

/**
 * Property filter supporting CMIS filter expression
 * 
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
public class PropertyFilter
{
    public static final String MATCH_ALL_FILTER = "*";
    public static final String PROPERTY_NAME_TOKENS_DELIMETER = ",";

    private static final int MINIMAL_ALLOWED_STRUCTURE_SIZE = 1;
    private static final Pattern PROPERTY_FILTER_REGEX = Pattern.compile("^(\\*)|(((cmis\\:)?[\\p{Alpha}\\p{Digit}_]+)((,){1}( )*((cmis\\:)?[\\p{Alpha}\\p{Digit}_]+))*)$");

    private Set<String> properties;

    public PropertyFilter()
    {
    }

    /**
     * @param filter filter value (case insensitive)
     * @throws FilterNotValidException if filter string isn't valid
     */
    public PropertyFilter(String filter) throws CmisException
    {
        if (filter == null || filter.length() < MINIMAL_ALLOWED_STRUCTURE_SIZE ? false : !PROPERTY_FILTER_REGEX.matcher(filter).matches())
        {
            throw ExceptionUtil.createCmisException(("\"" + filter + "\" filter value is invalid"), EnumServiceException.FILTER_NOT_VALID);
        }

        if (!filter.equals(MATCH_ALL_FILTER) && filter.length() >= MINIMAL_ALLOWED_STRUCTURE_SIZE)
        {
            splitFilterOnTokens(filter.split(PROPERTY_NAME_TOKENS_DELIMETER));
        }
    }

    private void splitFilterOnTokens(String[] tokens)
    {
        properties = new HashSet<String>();
        for (String token : tokens)
        {
            properties.add(token.trim().toLowerCase());
        }
    }

    /**
     * @param property property token name (e.g.: name (or Name), ObjectId (or: objectid, Objectid etc))
     * @return <b>true</b> returns if property is allowed by filter. In other case returns <b>false</b>
     */
    public boolean allow(String property)
    {
        return properties == null || properties.contains(property.toLowerCase());
    }
}