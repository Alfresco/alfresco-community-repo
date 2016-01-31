/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.module.org_alfresco_module_rm.util;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class that contains validation not present in {@link org.alfresco.util.ParameterCheck}.
 * 
 * @author tpage
 */
public class RMParameterCheck
{
    /**
     * Checks that the string parameter with the given name is not blank i.e. it is not null, zero length or entirely
     * composed of whitespace.
     * 
     * @param strParamName Name of parameter to check
     * @param strParamValue Value of the parameter to check
     */
    public static void checkNotBlank(final String strParamName, final String strParamValue)
                throws IllegalArgumentException
    {
        if (StringUtils.isBlank(strParamValue)) { throw new IllegalArgumentException(strParamName
                    + " is a mandatory parameter"); }
    }
}
