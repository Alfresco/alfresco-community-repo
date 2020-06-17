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
package org.alfresco.repo.event2;

import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Helper for {@link QName} objects.
 *
 * @author Sara Aspery
 */
public class QNameHelper
{
    private final NamespaceService namespaceService;

    public QNameHelper(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * Returns the QName in the format prefix:local, but in the exceptional case where there is no registered prefix
     * returns it in the form {uri}local.
     *
     * @param   k QName
     * @return  a String representing the QName in the format prefix:local or {uri}local.
     */
    public String getQNamePrefixString(QName k)
    {
        String key;
        try
        {
            key = k.toPrefixString(namespaceService);
        }
        catch (NamespaceException e)
        {
            key = k.toString();
        }
        return key;
    }
}
