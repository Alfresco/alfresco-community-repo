/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.node;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.locale.LocaleDAO;
import org.alfresco.service.namespace.QName;

/**
 * Class holding properties associated with the <b>sys:localized</b> aspect.
 * This aspect is common enough to warrant direct inclusion on the <b>Node</b> entity.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class LocalizedPropertiesEntity
{
    private static final Set<QName> LOCALIZED_PROP_QNAMES;
    static
    {
        LOCALIZED_PROP_QNAMES = new HashSet<QName>(8);
        LOCALIZED_PROP_QNAMES.add(ContentModel.PROP_LOCALE);
    }
    
    /**
     * @return          Returns <tt>true</tt> if the property belongs to the <b>sys:localized</b> aspect
     */
    public static boolean isLocalizedProperty(QName qname)
    {
        return LOCALIZED_PROP_QNAMES.contains(qname);
    }
    
    /**
     * Remove all {@link ContentModel#ASPECT_LOCALIZED localized} properties
     */
    public static void removeLocalizedProperties(Node node, Map<QName, Serializable> properties)
    {
        properties.keySet().removeAll(LOCALIZED_PROP_QNAMES);
    }
    
    /**
     * Remove all {@link ContentModel#ASPECT_LOCALIZED localized} properties
     */
    public static void removeLocalizedProperties(Set<QName> propertyQNames)
    {
        propertyQNames.removeAll(LOCALIZED_PROP_QNAMES);
    }
    
    /**
     * Adds all {@link ContentModel#ASPECT_LOCALIZED localized} properties.
     */
    public static void addLocalizedProperties(LocaleDAO localeDAO, Node node, Map<QName, Serializable> properties)
    {
        Long localeId = node.getLocaleId();
        Locale locale = localeDAO.getLocalePair(localeId).getSecond();
        properties.put(ContentModel.PROP_LOCALE, locale);
    }

    public static Serializable getLocalizedProperty(LocaleDAO localeDAO, Node node, QName qname)
    {
        if (qname.equals(ContentModel.PROP_LOCALE))
        {
            Long localeId = node.getLocaleId();
            return localeDAO.getLocalePair(localeId).getSecond();
        }
        throw new IllegalArgumentException("Not sys:localized property: " + qname);
    }
}
