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
package org.alfresco.repo.i18n;

import java.util.Locale;

import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * A {@link MessageLookup} that retrieves messages from a resource bundle in the classpath.
 */
public class StaticMessageLookup implements MessageLookup
{

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.i18n.MessageLookup#getMessage(java.lang.String)
     */
    @Override
    @NotAuditable
    public String getMessage(String messageKey)
    {
        return I18NUtil.getMessage(messageKey);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.i18n.MessageLookup#getMessage(java.lang.String, java.util.Locale)
     */
    @Override
    @NotAuditable
    public String getMessage(String messageKey, Locale locale)
    {
        return I18NUtil.getMessage(messageKey, locale);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.i18n.MessageLookup#getMessage(java.lang.String, java.lang.Object[])
     */
    @Override
    @NotAuditable
    public String getMessage(String messageKey, Object... params)
    {
        return I18NUtil.getMessage(messageKey, params);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.i18n.MessageLookup#getMessage(java.lang.String, java.util.Locale,
     * java.lang.Object[])
     */
    @Override
    @NotAuditable
    public String getMessage(String messageKey, Locale locale, Object... params)
    {
        return I18NUtil.getMessage(messageKey, locale, params);
    }
}
