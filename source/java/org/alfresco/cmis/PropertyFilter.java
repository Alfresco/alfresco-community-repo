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

import java.util.Arrays;
import java.util.List;

/**
 * http://docs.oasis-open.org/cmis/CMIS/v1.0/os/cmis-spec-v1.0.htm
 * 2.1.2.1 Property
 * All properties MUST supply a String queryName attribute which is used for query and filter operations on object-types.
 * This is an opaque String with limitations. This string SHOULD NOT contain any characters that negatively interact with the BNF grammar.
 * 
 * The string MUST NOT contain:
 *         whitespace “ “,
 *         comma “,”
 *         double quotes ‘”’
 *         single quotes “’”
 *         backslash “\”
 *         the period “.” character or,
 *         the open “(“ or close “)” parenthesis characters.
 *         
 *         
 * 2.2.1.2.1 Properties
 * Description: All of the methods that allow for the retrieval of properties for CMIS Objects have a “Property Filter”
 * as an optional parameter, which allows the caller to specify a subset of properties for Objects that MUST be returned by the repository in the output of the method.
 * Optional Input Parameter:
 * String filter: Value indicating which properties for Objects MUST be returned. Values are:
 *  - Not set: The set of properties to be returned MUST be determined by the repository.
 *  - A comma-delimited list of property definition Query Names: The properties listed MUST be returned.
 *  - “*” : All properties MUST be returned for all objects.
 * Repositories SHOULD return only the properties specified in the property filter if they exist on the object’s type definition.
 * 
 * If a property filter specifies a property that is ‘not set’, it MUST be represented as a property element without a value element.

 * @author Dmitry Velichkevich
 * @author Arseny Kovalchuk
 */
public class PropertyFilter
{
    public static final String MATCH_ALL_FILTER = "*";
    public static final String PROPERTY_NAME_TOKENS_DELIMITER = ",";

    private static final char[] PROPERTY_INVALID_CHARS = { ' ', ',', '"', '\'', '\\', '.', ',', '(', ')' };
    private final List<String> properties;

    /**
     * @param filter filter value (case insensitive)
     * @throws CMISFilterNotValidException if filter string isn't valid
     */
    public PropertyFilter(String filter) throws CMISFilterNotValidException
    {
        properties = validateFilter(filter);
    }

    /**
     * @param filter to be validated
     * @return a list of tokenized and validated properties
     * @throws CMISFilterNotValidException if one of the filter tokens is not valid
     */
    private static List<String> validateFilter(String filter) throws CMISFilterNotValidException
    {
        if (filter != null)
        {
            if (!filter.equals(MATCH_ALL_FILTER))
            {
                String[] tokens = filter.split(PROPERTY_NAME_TOKENS_DELIMITER);
                for (int i = 0; i < tokens.length; i++)
            {
                    String token = tokens[i].trim();
                    if (token.isEmpty() || token.indexOf('*') != -1 || !isValidToken(token))
                throw new CMISFilterNotValidException("Property filter \"" + filter + "\" is invalid");
                    tokens[i] = token; // trimmed
                }
                return Arrays.asList(tokens);
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
            }

    /**
     * Validates particular token within property filter
     * 
     * @param token
     * @return true if token is valid
     */
    private static boolean isValidToken(String token)
    {
        if (token == null)
            return false;
        boolean result = true;
        for (char invalidChar : PROPERTY_INVALID_CHARS)
            {
            if (token.indexOf(invalidChar) != -1)
                {
                result = false;
                break;
            }
        }
        return result;
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