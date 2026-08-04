/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.search.impl.elasticsearch;

/**
 * The search engine provider backing the Elasticsearch search subsystem.
 * <p>
 * Both OpenSearch and Elasticsearch expose a compatible REST API, so the provider is distinguished using the
 * {@code distribution} field returned by the engine's info endpoint ({@code GET /}). OpenSearch reports a distribution of
 * {@code "opensearch"}; Elasticsearch does not return a distribution at all.
 */
public enum SearchEngineType
{
    OPENSEARCH("OpenSearch"),
    ELASTICSEARCH("Elasticsearch"),
    UNKNOWN("Unknown");

    /**
     * The distribution value reported by an OpenSearch server in its info response.
     */
    public static final String OPENSEARCH_DISTRIBUTION = "opensearch";

    private final String displayName;

    SearchEngineType(String displayName)
    {
        this.displayName = displayName;
    }

    /**
     * @return a human readable name for the provider, suitable for logging and reporting.
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Resolves the search engine provider from the {@code distribution} value returned by the engine's info endpoint.
     *
     * @param distribution
     *            the distribution reported by the engine, or {@code null}/blank when none is reported
     * @return {@link #OPENSEARCH} when the distribution is {@code "opensearch"} (case-insensitive), {@link #ELASTICSEARCH}
     *         when no distribution is reported, otherwise {@link #UNKNOWN}
     */
    public static SearchEngineType fromDistribution(String distribution)
    {
        if (distribution == null || distribution.isBlank())
        {
            return ELASTICSEARCH;
        }
        if (OPENSEARCH_DISTRIBUTION.equalsIgnoreCase(distribution.trim()))
        {
            return OPENSEARCH;
        }
        return UNKNOWN;
    }
}
