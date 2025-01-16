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

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.service.namespace.QName;

public class QNameMatcher
{
    private static final Logger LOGGER = LoggerFactory.getLogger(QNameMatcher.class);
    private static final String WILDCARD = "*";

    private final Set<QName> matchingTypes;
    private final Set<String> matchingNamespaceURIs;

    public QNameMatcher(Set<QName> qNamesToMatch)
    {
        matchingTypes = new HashSet<>();
        matchingNamespaceURIs = new HashSet<>();

        qNamesToMatch.forEach(qName -> {
            if (WILDCARD.equals(qName.getLocalName()))
            {
                matchingNamespaceURIs.add(qName.getNamespaceURI());
            }
            else
            {
                matchingTypes.add(qName);
            }
        });

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Matching namespace URIs:" + matchingNamespaceURIs);
            LOGGER.debug("Matching types:" + matchingTypes);
        }
    }

    public boolean isMatching(QName qName)
    {
        if (qName != null)
        {
            return matchingTypes.contains(qName) || matchingNamespaceURIs.contains(qName.getNamespaceURI());
        }
        return false;
    }
}
