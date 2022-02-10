/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rm.community.util;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Utility class for checking parameters
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class ParameterCheck
{
    private ParameterCheck()
    {
        // Intentionally blank
    }

    /**
     * Checks if a given {@link String} is blank or not, i.e. not <code>null<code>, "" or " ".
     *
     * @param paramName The name of the parameter to check
     * @param paramValue The value of the parameter to check
     * @throws IllegalArgumentException Throws an exception if the given value is blank
     */
    public static void mandatoryString(final String paramName, final String paramValue) throws IllegalArgumentException
    {
        if (isBlank(paramValue))
        {
            throw new IllegalArgumentException("'" + paramName  + "' is a mandatory parameter.");
        }
    }

    /**
     * Checks if a given {@link Object} is null or not
     *
     * @param paramName The name of the parameter to check
     * @param object The value of the parameter to check
     * @throws IllegalArgumentException Throws an exception if the given value is null
     */
    public static void mandatoryObject(final String paramName, final Object object) throws IllegalArgumentException
    {
        if (object == null)
        {
            throw new IllegalArgumentException("'" + paramName  + "' is a mandatory parameter.");
        }
    }
}
