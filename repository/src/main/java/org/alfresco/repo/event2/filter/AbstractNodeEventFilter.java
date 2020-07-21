/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.event2.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * Abstract {@link EventFilter} implementation, containing common event filtering
 * functionality for the {@link QName} type.
 *
 * @author Jamal Kaabi-Mofrad
 */
public abstract class AbstractNodeEventFilter implements EventFilter<QName>
{
    private static final Logger LOGGER = Logger.getLogger(AbstractNodeEventFilter.class);

    private static final String MARKER_INCLUDE_SUBTYPES = "include_subtypes";
    private static final String WILDCARD = "*";

    protected DictionaryService dictionaryService;
    protected NamespaceService namespaceService;

    private Set<QName> excludedTypes;
    private Set<String> excludedNamespaceURI;

    public AbstractNodeEventFilter()
    {
        this.excludedTypes = new HashSet<>();
        this.excludedNamespaceURI = new HashSet<>();
    }

    public final void init()
    {
        preprocessExcludedTypes(getExcludedTypes());
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    @Override
    public boolean isExcluded(QName qName)
    {
        if (qName != null)
        {
            return excludedTypes.contains(qName) || excludedNamespaceURI.contains(qName.getNamespaceURI());
        }
        return false;
    }

    protected abstract Set<QName> getExcludedTypes();

    protected List<String> parseFilterList(String unparsedFilterList)
    {
        List<String> list = new LinkedList<>();

        StringTokenizer st = new StringTokenizer(unparsedFilterList, ",");
        while (st.hasMoreTokens())
        {
            String entry = st.nextToken().trim();
            if (!entry.isEmpty())
            {
                if (!entry.equals("none") && !entry.contains("${"))
                {
                    list.add(entry);
                }
            }
        }
        return list;
    }

    /**
     * Processes the user-defined list of types into valid QNames. It
     * validates them against the dictionary and also supports wildcards
     */
    private void preprocessExcludedTypes(Set<QName> excluded)
    {
        excluded.forEach(qName -> {
            if (WILDCARD.equals(qName.getLocalName()))
            {
                //excludedPrefixes.add(getPrefix(qName));
                excludedNamespaceURI.add(qName.getNamespaceURI());
            }
            else
            {
                excludedTypes.add(qName);
            }
        });

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Excluded namespace URIs:" + excludedNamespaceURI);
            LOGGER.debug("Excluded types:" + excludedTypes);
        }
    }

    private QName getQName(String type)
    {
        return QName.createQName(type, namespaceService);
    }

    protected Collection<QName> expandTypeDef(String typeDef)
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
            LOGGER.warn("Ignoring invalid blacklist type pattern: " + typeDef);
            return Collections.emptyList();
        }

        if (typeDefParts[1].equals(MARKER_INCLUDE_SUBTYPES))
        {
            if (typeDefParts[0].indexOf('*') >= 0)
            {
                LOGGER.warn("Ignoring invalid blacklist type pattern: " + typeDef);
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

        LOGGER.warn("Ignoring invalid blacklist type pattern: " + typeDef);
        return Collections.emptyList();
    }
}
