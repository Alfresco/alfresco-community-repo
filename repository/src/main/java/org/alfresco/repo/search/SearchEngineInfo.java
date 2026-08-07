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
package org.alfresco.repo.search;

/**
 * Holder for the search engine details detected when the search subsystem starts (ACS-12415).
 * <p>
 * The values are populated by {@link org.alfresco.repo.search.impl.elasticsearch.SearchEngineInfoDetector}
 * which issues a single {@code GET /} against the configured engine using the elasticsearch
 * subsystem's existing REST client. This bean is defined in the parent (repository) application
 * context and exposed under the id {@code searchEngineInfo} so that the pagination API and other
 * components can inject it directly:
 * <ul>
 *   <li>{@code searchengine.name} &rarr; {@link #getSearchEngineName()} ("opensearch" or "elasticsearch")</li>
 *   <li>{@code searchengine.version} &rarr; {@link #getSearchEngineVersion()} (for example "2.11.1")</li>
 *   <li>{@code searchengine.lucene.version} &rarr; {@link #getSearchEngineLuceneVersion()} (for example "9.7.0")</li>
 * </ul>
 * The fields are {@code volatile} because they are written by the subsystem start-up thread and
 * read by request-processing threads.
 */
public class SearchEngineInfo
{
    /** Provider name reported for an Elasticsearch backend. */
    public static final String PROVIDER_ELASTICSEARCH = "elasticsearch";

    /** Provider name reported for an OpenSearch backend. */
    public static final String PROVIDER_OPENSEARCH = "opensearch";

    /** Value used when a detail has not been (or could not be) determined. */
    public static final String UNKNOWN = "unknown";

    private volatile String searchEngineName = UNKNOWN;
    private volatile String searchEngineVersion = UNKNOWN;
    private volatile String searchEngineLuceneVersion = UNKNOWN;

    public String getSearchEngineName()
    {
        return searchEngineName;
    }

    public void setSearchEngineName(String searchEngineName)
    {
        this.searchEngineName = searchEngineName;
    }

    public String getSearchEngineVersion()
    {
        return searchEngineVersion;
    }

    public void setSearchEngineVersion(String searchEngineVersion)
    {
        this.searchEngineVersion = searchEngineVersion;
    }

    public String getSearchEngineLuceneVersion()
    {
        return searchEngineLuceneVersion;
    }

    public void setSearchEngineLuceneVersion(String searchEngineLuceneVersion)
    {
        this.searchEngineLuceneVersion = searchEngineLuceneVersion;
    }

    @Override
    public String toString()
    {
        return "SearchEngineInfo{name=" + searchEngineName
                + ", version=" + searchEngineVersion
                + ", luceneVersion=" + searchEngineLuceneVersion + "}";
    }
}
