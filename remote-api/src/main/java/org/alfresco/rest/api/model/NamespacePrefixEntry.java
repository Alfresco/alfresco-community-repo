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

package org.alfresco.rest.api.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * v1 REST API entry for namespace-prefix mapping.
 *
 * Represents the namespace URI ↔ prefix mapping sourced from NamespaceService.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NamespacePrefixEntry
{
    @JsonProperty("prefixUriMap")
    private Map<String, String> prefixUriMap;

    public NamespacePrefixEntry()
    {}

    public NamespacePrefixEntry(Map<String, String> prefixUriMap)
    {
        this.prefixUriMap = prefixUriMap;
    }

    public Map<String, String> getPrefixUriMap()
    {
        return prefixUriMap;
    }

    public void setPrefixUriMap(Map<String, String> prefixUriMap)
    {
        this.prefixUriMap = prefixUriMap;
    }

    @Override
    public String toString()
    {
        return "NamespacePrefixEntry{" +
                "prefixUriMap size=" + (prefixUriMap != null ? prefixUriMap.size() : 0) +
                '}';
    }
}
