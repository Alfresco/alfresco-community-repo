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
package org.alfresco.com.util;

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
     * FIXME: Document me :)
     *
     * @param paramName FIXME: Document me :)
     * @param paramValue FIXME: Document me :)
     * @throws IllegalArgumentException FIXME: Document me :)
     */
    public static void mandatoryString(final String paramName, final String paramValue) throws IllegalArgumentException
    {
        if (isBlank(paramValue))
        {
            throw new IllegalArgumentException("'" + paramName  + "' is a mandatory parameter.");
        }
    }

    /**
     * FIXME: Document me :)
     *
     * @param paramName FIXME: Document me :)
     * @param object FIXME: Document me :)
     */
    public static void mandatoryObject(final String paramName, final Object object)
    {
        if (object == null)
        {
            throw new IllegalArgumentException("'" + paramName  + "' is a mandatory parameter.");
        }
    }
}
