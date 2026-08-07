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
package org.alfresco.repo.search.impl.elasticsearch.query;

import java.util.Collections;
import java.util.Map;

import org.opensearch.client.opensearch.core.SearchRequest;

import org.alfresco.util.Pair;

public class SearchRequestWrapper
{

    private final SearchRequest searchRequest;
    private final Map<String, String> bucketsTranslator;
    private final Map<String, Pair<String, String>> complementaryBucketsTranslator;

    private SearchRequestWrapper(Builder builder)
    {
        this.searchRequest = builder.searchRequest;
        this.bucketsTranslator = builder.bucketsTranslator;
        this.complementaryBucketsTranslator = builder.complementaryBucketsTranslator;
    }

    public SearchRequest searchRequest()
    {
        return searchRequest;
    }

    public Map<String, String> bucketsTranslator()
    {
        return bucketsTranslator;
    }

    public Map<String, Pair<String, String>> complementaryBucketsTranslator()
    {
        return complementaryBucketsTranslator;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private SearchRequest searchRequest;
        private Map<String, String> bucketsTranslator = Collections.emptyMap();
        private Map<String, Pair<String, String>> complementaryBucketsTranslator = Collections.emptyMap();

        public Builder searchRequest(SearchRequest searchRequest)
        {
            this.searchRequest = searchRequest;
            return this;
        }

        public Builder bucketsTranslator(Map<String, String> bucketsTranslator)
        {
            this.bucketsTranslator = bucketsTranslator;
            return this;
        }

        public Builder complementaryBucketsTranslator(
                Map<String, Pair<String, String>> complementaryBucketsTranslator)
        {
            this.complementaryBucketsTranslator = complementaryBucketsTranslator;
            return this;
        }

        public SearchRequestWrapper build()
        {
            return new SearchRequestWrapper(this);
        }
    }
}
