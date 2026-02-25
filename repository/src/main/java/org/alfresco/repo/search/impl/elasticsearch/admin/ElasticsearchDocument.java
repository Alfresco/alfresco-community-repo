/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.admin;

import java.util.Map;

import org.opensearch.client.opensearch.core.search.Hit;

/**
 * A representation of an Elasticsearch document
 */
public class ElasticsearchDocument
{
    private final String id;
    private final long contentIndexingLastUpdate;
    private final long metadataIndexingLastUpdate;
    private final String contentTrStatus;

    public ElasticsearchDocument(Hit searchHit)
    {
        Map<String, Object> sourceMap = (Map<String, Object>) searchHit.source();

        this.id = searchHit.id();
        this.contentIndexingLastUpdate = mapContentIndexingLastUpdate(sourceMap);
        this.metadataIndexingLastUpdate = mapMetadataIndexingLastUpdate(sourceMap);
        this.contentTrStatus = mapContentTrStatus(sourceMap);
    }

    public ElasticsearchDocument(String id, long contentIndexingLastUpdate, long metadataIndexingLastUpdate, String contentTrStatus)
    {
        this.id = id;
        this.contentIndexingLastUpdate = contentIndexingLastUpdate;
        this.metadataIndexingLastUpdate = metadataIndexingLastUpdate;
        this.contentTrStatus = contentTrStatus;
    }

    public String getId()
    {
        return id;
    }

    public long getContentIndexingLastUpdate()
    {
        return contentIndexingLastUpdate;
    }

    public long getMetadataIndexingLastUpdate()
    {
        return metadataIndexingLastUpdate;
    }

    public String getContentTrStatus()
    {
        return contentTrStatus;
    }

    private long mapMetadataIndexingLastUpdate(Map<String, Object> sourceMap)
    {
        return getValueFromSource(sourceMap.get("METADATA_INDEXING_LAST_UPDATE"));
    }

    private long mapContentIndexingLastUpdate(Map<String, Object> sourceMap)
    {
        return getValueFromSource(sourceMap.get("CONTENT_INDEXING_LAST_UPDATE"));
    }

    private String mapContentTrStatus(Map<String, Object> sourceMap)
    {
        return (String) sourceMap.getOrDefault("cm%3Acontent%2Etr_status", "");
    }

    private static long getValueFromSource(Object key)
    {
        if (key != null)
        {
            if (key instanceof Integer)
            {
                return ((Integer) key).longValue();
            }
            else
            {
                return (Long) key;
            }
        }
        return 0L;
    }

}
