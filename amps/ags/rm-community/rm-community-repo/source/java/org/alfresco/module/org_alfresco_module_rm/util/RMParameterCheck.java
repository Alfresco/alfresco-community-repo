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

package org.alfresco.module.org_alfresco_module_rm.util;

import org.apache.commons.lang3.StringUtils;

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
