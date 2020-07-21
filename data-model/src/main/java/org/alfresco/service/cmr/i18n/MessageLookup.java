/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.i18n;

import java.util.Locale;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.NotAuditable;

/**
 * An object providing basic message lookup facilities. May (or may not) be directly conntect to resource bundles.
 */
@AlfrescoPublicApi
public interface MessageLookup
{
    /**
     * Get message from registered resource bundle.
     * 
     * @param messageKey
     *            message key
     * @return localised message string, null if not found
     */
    @NotAuditable
    public String getMessage(String messageKey);

    /**
     * Get a localised message string
     * 
     * @param messageKey
     *            the message key
     * @param locale
     *            override the current locale
     * @return the localised message string, null if not found
     */
    @NotAuditable
    public String getMessage(final String messageKey, final Locale locale);

    /**
     * Get a localised message string, parameterized using standard MessageFormatter.
     * 
     * @param messageKey
     *            message key
     * @param params
     *            format parameters
     * @return the localised string, null if not found
     */
    @NotAuditable
    public String getMessage(String messageKey, Object... params);

    /**
     * Get a localised message string, parameterized using standard MessageFormatter.
     * 
     * @param messageKey
     *            the message key
     * @param locale
     *            override current locale
     * @param params
     *            the localised message string
     * @return the localised string, null if not found
     */
    @NotAuditable
    public String getMessage(String messageKey, Locale locale, Object... params);
}
