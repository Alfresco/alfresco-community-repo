/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2025 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.event2.shared;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TypeDefExpander
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeDefExpander.class);
    private static final String MARKER_INCLUDE_SUBTYPES = "include_subtypes";

    private final DictionaryService dictionaryService;
    private final NamespaceService namespaceService;

    public TypeDefExpander(DictionaryService dictionaryService, NamespaceService namespaceService)
    {
        this.dictionaryService = dictionaryService;
        this.namespaceService = namespaceService;
    }

    public Set<QName> expand(Collection<String> types){
        Set<QName> result = new HashSet<>();
        types.forEach(type -> result.addAll(expand(type)));
        return result;
    }

    public Collection<QName> expand(String typeDef)
    {
        if ((typeDef == null) || typeDef.isEmpty() || typeDef.equals("none") || typeDef.contains("${"))
        {
            return Collections.emptyList();
        }

        if (typeDef.indexOf(' ') < 0)
        {
            return Collections.singleton(getQName(typeDef));
        }

        String[] typeDefParts = typeDef.split(" ");
        if (typeDefParts.length != 2)
        {
            LOGGER.warn("Ignoring invalid type pattern: " + typeDef);
            return Collections.emptyList();
        }

        if (typeDefParts[1].equals(MARKER_INCLUDE_SUBTYPES))
        {
            if (typeDefParts[0].indexOf('*') >= 0)
            {
                LOGGER.warn("Ignoring invalid type pattern: " + typeDef);
                return Collections.emptyList();
            }
            QName baseType;
            try
            {
                baseType = getQName(typeDefParts[0]);
            }
            catch (NamespaceException ne)
            {
                return Collections.emptyList();
            }
            return dictionaryService.getSubTypes(baseType, true);
        }

        LOGGER.warn("Ignoring invalid type pattern: " + typeDef);
        return Collections.emptyList();
    }

    private QName getQName(String type)
    {
        return QName.createQName(type, namespaceService);
    }
}

