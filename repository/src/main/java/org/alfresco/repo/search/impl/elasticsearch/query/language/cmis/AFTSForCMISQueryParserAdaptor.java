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

package org.alfresco.repo.search.impl.elasticsearch.query.language.cmis;

import static org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.QueryConverter.fromLuceneSpanToElasticsearchSpan;
import static org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.QueryConverter.fromLuceneToElasticsearch;

import org.apache.lucene.queries.spans.SpanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;

import org.alfresco.repo.search.adaptor.AnalysisMode;
import org.alfresco.repo.search.adaptor.LuceneFunction;
import org.alfresco.repo.search.adaptor.QueryConstants;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchExactTermSearchConfig;
import org.alfresco.repo.search.impl.elasticsearch.model.FieldName;
import org.alfresco.repo.search.impl.elasticsearch.query.language.EsTypeResolver;
import org.alfresco.repo.search.impl.elasticsearch.query.language.afts.AFTSQueryParserAdaptor;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespacePrefixResolver;

public class AFTSForCMISQueryParserAdaptor extends AFTSQueryParserAdaptor
{
    public AFTSForCMISQueryParserAdaptor(SearchParameters searchParameters,
            NamespacePrefixResolver namespacePrefixResolver, DictionaryService dictionaryService,
            SiteService siteService, ElasticsearchExactTermSearchConfig exactTermSearchConfig,
            EsTypeResolver esTypeResolver)
    {
        super(searchParameters, namespacePrefixResolver, dictionaryService, siteService, exactTermSearchConfig,
                esTypeResolver);
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getFieldQuery(String fieldName, String term)
    {
        // Replace the % wildcard with * for CMIS queries
        term = lucene.containsWildcard(term) ? replaceWildcardCharacters(term) : term;

        term = fieldQueryTransformer.transformTerm(fieldName, term);
        if (fieldName == null)
        {
            fieldName = QueryConstants.FIELD_TEXT;
        }
        Query luceneQuery = lucene.getFieldQuery(fieldName, term, true);

        if (luceneQuery instanceof SpanQuery)
        {
            return fromLuceneSpanToElasticsearchSpan((SpanQuery) luceneQuery);
        }

        if (luceneQuery instanceof WildcardQuery)
        {
            return fromLuceneToElasticsearch(luceneQuery);
        }

        return safeQueryStringQueryBuilder(luceneQuery);
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getFieldQuery(String fieldName, String term, AnalysisMode analysisMode, LuceneFunction luceneFunction)
    {
        // Replace the % wildcard with * for CMIS queries
        term = lucene.containsWildcard(term) ? replaceWildcardCharacters(term) : term;

        if (analysisMode == AnalysisMode.IDENTIFIER && (isText(fieldName) || isKeyword(fieldName)))
        {
            if (fieldName == null)
            {
                fieldName = QueryConstants.FIELD_TEXT;
            }

            Query luceneQuery = lucene.getFieldQuery(getFieldName(fieldName), term, true);

            if (luceneQuery instanceof WildcardQuery)
            {
                return fromLuceneToElasticsearch(luceneQuery);
            }

            return safeQueryStringQueryBuilder(luceneQuery);
        }
        else
        {
            return getFieldQuery(fieldName, term);
        }
    }

    private String replaceWildcardCharacters(String queryText)
    {
        // Only replace the % wildcard if its not escaped
        return queryText.replaceAll("(?<!\\\\)%", "*");
    }

    private String getFieldName(String fieldName)
    {
        if (fieldName.equals(QueryConstants.FIELD_TYPE))
        {
            return FieldName.exactTermSearch(fieldName);
        }

        // If field is tokenized-only, we don't support exact search
        if (lucene.isTokenizedOnlyField(fieldName))
        {
            throw new UnsupportedOperationException("Exact field search is not supported for tokenized-only fields.");
        }

        return FieldName.untokenized(fieldName);
    }
}
