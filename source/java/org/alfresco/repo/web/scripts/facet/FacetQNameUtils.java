/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.web.scripts.facet;

import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * This class provides some simple utility methods for dealing with {@link QName QNames}
 * within the faceted search feature.
 * These are not intended for general use, or else they'd be in the {@link QName} class.
 * @since 5.0
 */
public abstract class FacetQNameUtils
{
    /**
     * This method converts the supplied qname string into a {@link QName} object.
     * It accepts both short and long form qname strings.
     * 
     * @param s a qname string, such as "cm:name" or "{http://www.alfresco.org/model/content/1.0}name"
     * @param resolver this is needed to convert any qname prefixes into their long form.
     * @return the QName instance.
     * @throws NullPointerException if the provided string is {@code null}.
     * @throws IllegalArgumentException if the provided string could not be recognised as a valid QName.
     */
    public static QName createQName(String s, NamespacePrefixResolver resolver)
    {
        final QName result;
        
        if (s.length() < 2) { throw new IllegalArgumentException("Cannot convert string '" + s + "'"); }
        
        if (s.charAt(0) == QName.NAMESPACE_BEGIN &&
            s.substring(1).contains(Character.toString(QName.NAMESPACE_END)))
        {
            // Assume it's a long-form qname.
            result = QName.createQName(s);
        }
        else if ( !s.contains(Character.toString(QName.NAMESPACE_BEGIN)) &&
                 s.contains(Character.toString(QName.NAMESPACE_PREFIX)))
        {
            // Assume it's a short-form qname.
            result = QName.createQName(s, resolver);
        }
        else
        {
            // We're not sure what sort of qname this is supposed to be.
            throw new IllegalArgumentException("Cannot convert string '" + s + "'");
        }
        
        return result;
    }
}