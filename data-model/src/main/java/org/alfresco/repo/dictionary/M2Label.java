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
package org.alfresco.repo.dictionary;

import java.util.Locale;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.QName;
import org.springframework.util.StringUtils;


/**
 * Helper for obtaining display labels for data dictionary items
 * 
 * @author David Caruana
 */
public class M2Label
{

    /**
     * Get label for data dictionary item given specified locale
     * 
     * @param locale Locale
     * @param model ModelDefinition
     * @param messageLookup MessageLookup
     * @param type String
     * @param item QName
     * @param label String
     * @return String
     */
    public static String getLabel(Locale locale, ModelDefinition model, MessageLookup messageLookup, String type, QName item, String label)
    {
        if (messageLookup == null)
        {
            return null;
        }
        String key = model.getName().toPrefixString();
        if (type != null)
        {
            key += "." + type;
        }
        if (item != null)
        {
            key += "." + item.toPrefixString();
        }
        key += "." + label;
        key = StringUtils.replace(key, ":", "_");
        return messageLookup.getMessage(key, locale);
    }
    
    /**
     * Get label for data dictionary item
     * 
     * @param model ModelDefinition
     * @param messageLookup MessageLookup
     * @param type String
     * @param item QName
     * @param label String
     * @return String
     */
    public static String getLabel(ModelDefinition model, MessageLookup messageLookup, String type, QName item, String label)
    {
        return getLabel(I18NUtil.getLocale(), model, messageLookup, type, item, label);
    }
    
}
