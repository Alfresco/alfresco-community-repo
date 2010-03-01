/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.dictionary;

import java.util.Locale;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
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
     * @param locale
     * @param model
     * @param type
     * @param item
     * @param label
     * @return
     */
    public static String getLabel(Locale locale, ModelDefinition model, String type, QName item, String label)
    {
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
        return I18NUtil.getMessage(key, locale);
    }
    
    /**
     * Get label for data dictionary item
     * 
     * @param model
     * @param type
     * @param item
     * @param label
     * @return
     */
    public static String getLabel(ModelDefinition model, String type, QName item, String label)
    {
        return getLabel(I18NUtil.getLocale(), model, type, item, label);
    }
    
}
