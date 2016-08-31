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
package org.alfresco.util;

import java.util.Collection;

/**
 * Utility class to perform various common parameter checks
 * 
 * @author gavinc
 */
public final class ParameterCheck
{
    /**
     * Checks that the parameter with the given name has content i.e. it is not
     * null
     * 
     * @param strParamName Name of parameter to check
     * @param object Value of the parameter to check
     */
    public static final void mandatory(final String strParamName, final Object object)
    {
        // check that the object is not null
        if (object == null)
        {
            throw new IllegalArgumentException(strParamName + " is a mandatory parameter");
        }
    }

    /**
     * Checks that the string parameter with the given name has content i.e. it
     * is not null and not zero length
     * 
     * @param strParamName Name of parameter to check
     * @param strParamValue Value of the parameter to check
     */
    public static final void mandatoryString(final String strParamName, final String strParamValue)
    {
        // check that the given string value has content
        if (strParamValue == null || strParamValue.length() == 0)
        {
            throw new IllegalArgumentException(strParamName + " is a mandatory parameter");
        }
    }

    /**
     * Checks that the collection parameter contains at least one item.
     * 
     * @param strParamName Name of parameter to check
     * @param coll collection to check
     */
    public static final void mandatoryCollection(final String strParamName, final Collection coll)
    {
        if (coll == null || coll.size() == 0)
        {
            throw new IllegalArgumentException(strParamName + " collection must contain at least one item");
        }
    }

}
