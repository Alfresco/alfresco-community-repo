/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

package org.alfresco.rest.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.rest.api.NamespacePrefixes;
import org.alfresco.rest.api.model.NamespacePrefixEntry;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.namespace.NamespaceService;

/**
 * v1 REST API implementation for namespace-prefix mapping. Returns complete namespace URI ↔ prefix mapping from NamespaceService.
 */
public class NamespacePrefixesImpl implements NamespacePrefixes
{
    private static final Logger LOGGER = LoggerFactory.getLogger(NamespacePrefixesImpl.class);

    private NamespaceService namespaceService;

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    @Override
    public CollectionWithPagingInfo<NamespacePrefixEntry> getNamespacePrefixes(Parameters parameters)
    {
        try
        {
            Map<String, String> prefixUriMap = new HashMap<>();

            for (String prefix : namespaceService.getPrefixes())
            {
                String uri = namespaceService.getNamespaceURI(prefix);

                if (uri == null)
                {
                    LOGGER.debug("Skipping prefix '{}' with null URI", prefix);
                    continue;
                }

                String existing = prefixUriMap.put(uri, prefix);
                if (existing != null && !existing.equals(prefix))
                {
                    LOGGER.debug("Duplicate namespace URI '{}': " +
                            "existing prefix '{}' replaced by '{}'",
                            uri, existing, prefix);
                }
            }

            LOGGER.debug("Namespace-prefix mapping built: {} unique URIs", prefixUriMap.size());

            List<NamespacePrefixEntry> entries = new ArrayList<>();
            entries.add(new NamespacePrefixEntry(prefixUriMap));
            return CollectionWithPagingInfo.asPaged(parameters.getPaging(), entries);
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("Failed to build namespace-prefix mapping", e);
        }
    }
}
