/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.dictionary;

import java.util.Locale;

import org.alfresco.i18n.I18NUtil;
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
