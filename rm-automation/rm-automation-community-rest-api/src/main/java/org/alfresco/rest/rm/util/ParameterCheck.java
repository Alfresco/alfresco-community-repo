/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.rm.util;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Utility class for checking parameters
 *
 * @author Tuna Aksoy
 * @since 1.0
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
