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
package org.alfresco.repo.search.impl.elasticsearch.query.language.lucene;

import static org.alfresco.repo.search.impl.elasticsearch.query.language.lucene.QueryConverter.fromLuceneToElasticsearch;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queries.spans.SpanQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.TermQuery;
import org.opensearch.client.opensearch._types.query_dsl.QueryStringQuery;

import org.alfresco.repo.search.impl.elasticsearch.query.language.LanguageQueryBuilder;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespacePrefixResolver;

public class LuceneQueryBuilder implements LanguageQueryBuilder
{
    private final NamespacePrefixResolver namespaceResolver;
    private final DictionaryService dictionaryService;
    private final SiteService siteService;

    public LuceneQueryBuilder(NamespacePrefixResolver namespaceResolver, DictionaryService dictionaryService, SiteService siteService)
    {
        this.namespaceResolver = namespaceResolver;
        this.dictionaryService = dictionaryService;
        this.siteService = siteService;
    }

    @Override
    public org.opensearch.client.opensearch._types.query_dsl.Query getQuery(SearchParameters searchParameters) throws ParseException
    {
        final var parser = new LuceneQueryParser(namespaceResolver, dictionaryService, siteService, searchParameters);

        final Query luceneQuery = parser.parse(searchParameters.getQuery());

        if (luceneQuery == null)
        {
            return null;
        }

        if (!StringUtils.isEmpty(luceneQuery.toString()) &&
                (luceneQuery instanceof SpanQuery ||
                        luceneQuery instanceof BooleanQuery ||
                        luceneQuery instanceof TermQuery))
        {
            return fromLuceneToElasticsearch(luceneQuery);
        }
        if (luceneQuery instanceof RegexpQuery regexpQuery && PathQueryConverter.isPathQuery(regexpQuery))
        {
            return fromLuceneToElasticsearch(regexpQuery);
        }
        return new QueryStringQuery.Builder().timeZone(searchParameters.getTimezone()).query(luceneQuery.toString()).build().toQuery();
    }

}
