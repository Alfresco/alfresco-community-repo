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
package org.alfresco.repo.search.impl.elasticsearch;

/**
 * This class contains definition of constants related to the Elasticsearch context.
 */
@SuppressWarnings("PMD.LongVariable")
public class ElasticsearchContextProperties
{
    /** Count of the documents indexed in the ES cluster. */
    public static final String ELASTICSEARCH_DOCUMENT_COUNT = "elasticsearch.document.count";

    /** Counters showing the documents content indexing status in the ES cluster. */
    public static final String ES_CONTENT_INDEX_SUCCESS_COUNT = "elasticsearch.content.indexing.success.count";
    public static final String ES_CONTENT_INDEX_FAILURES_COUNT = "elasticsearch.content.indexing.failures.count";
    public static final String ES_CONTENT_INDEX_NEW_PROGRESS_COUNT = "elasticsearch.content.indexing.new-documents.progress.count";
    public static final String ES_CONTENT_INDEX_UPDATE_PROGRESS_COUNT = "elasticsearch.content.indexing.outdated-documents.progress.count";

    /** String to return if a property could not be determined (e.g. due to ES unavailability). */
    public static final String UNAVAILABLE = "Unavailable";

    /** Count of the indexable documents. */
    public static final String NODES_COUNT = "repository.nodesCount";

    /**
     * Creates new instance of the class.
     */
    private ElasticsearchContextProperties()
    {}
}
