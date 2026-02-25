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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.spans.SpanFirstQuery;
import org.apache.lucene.queries.spans.SpanNearQuery;
import org.apache.lucene.queries.spans.SpanQuery;
import org.apache.lucene.queries.spans.SpanTermQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;
import org.opensearch.client.opensearch._types.query_dsl.SpanMultiTermQuery;

import org.alfresco.repo.search.impl.elasticsearch.model.FieldName;

public final class QueryConverter
{
    private static Set<String> analyzeWildcardFields = Set.of();

    private QueryConverter()
    {}

    public static void setAnalyzeWildcardFields(String fields)
    {
        analyzeWildcardFields = Arrays.stream(fields.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(FieldName::encoded)
                .collect(Collectors.toSet());
    }

    public static org.opensearch.client.opensearch._types.query_dsl.Query fromLuceneToElasticsearch(Query luceneQuery)
    {
        if (luceneQuery == null)
        {
            return null;
        }
        if (luceneQuery instanceof SpanQuery)
        {
            return fromLuceneSpanToElasticsearchSpan((SpanQuery) luceneQuery);
        }
        else if (luceneQuery instanceof BooleanQuery)
        {
            return fromLuceneBoolToElasticsearchBoolean((BooleanQuery) luceneQuery).toQuery();
        }
        else if (luceneQuery instanceof RegexpQuery regexpQuery)
        {
            return QueryBuilders.regexp().field(regexpQuery.getField()).value(regexpQuery.getRegexp().text()).build().toQuery();
        }
        else if (luceneQuery instanceof WildcardQuery wildcardQuery)
        {
            return QueryBuilders.wildcard().field(wildcardQuery.getField()).value(wildcardQuery.getTerm().text()).build().toQuery();
        }
        else if (luceneQuery instanceof TermQuery termQuery && shouldAnalyzeWildcard(termQuery.getTerm().field()))
        {
            return QueryBuilders.queryString().query(luceneQuery.toString()).analyzeWildcard(true).build().toQuery();
        }

        return QueryBuilders.queryString().query(luceneQuery.toString()).build().toQuery();
    }

    private static boolean shouldAnalyzeWildcard(String field)
    {
        return analyzeWildcardFields.contains(field);
    }

    public static BoolQuery fromLuceneBoolToElasticsearchBoolean(BooleanQuery booleanQuery)
    {

        BoolQuery.Builder boolQueryBuilder = QueryBuilders.bool();
        booleanQuery.clauses()
                .forEach(b -> {
                    Query query = b.getQuery();
                    BooleanClause.Occur occur = b.getOccur();

                    switch (occur)
                    {
                    case MUST -> boolQueryBuilder.must(fromLuceneToElasticsearch(query));
                    case FILTER -> boolQueryBuilder.filter(fromLuceneToElasticsearch(query));
                    case SHOULD -> boolQueryBuilder.should(fromLuceneToElasticsearch(query));
                    case MUST_NOT -> boolQueryBuilder.mustNot(fromLuceneToElasticsearch(query));
                    }
                });

        return boolQueryBuilder.build().toQuery().bool();
    }

    public static org.opensearch.client.opensearch._types.query_dsl.Query fromLuceneSpanToElasticsearchSpan(SpanQuery luceneQuery)
    {
        if (luceneQuery instanceof SpanTermQuery spanTermLucene)
        {
            Term luceneTerm = spanTermLucene.getTerm();
            if (luceneTerm.text().equals("*"))
            {
                org.opensearch.client.opensearch._types.query_dsl.WildcardQuery.Builder wildcardElasticsearch = new org.opensearch.client.opensearch._types.query_dsl.WildcardQuery.Builder().field(luceneTerm.field()).value(luceneTerm.text());
                return new SpanMultiTermQuery.Builder().match(wildcardElasticsearch.build().toQuery()).build().toQuery();
            }
            else
            {
                return new org.opensearch.client.opensearch._types.query_dsl.SpanTermQuery.Builder().field(luceneTerm.field()).value(luceneTerm.text()).build().toQuery();
            }
        }
        else if (luceneQuery instanceof SpanFirstQuery spanFirstLucene)
        {
            org.opensearch.client.opensearch._types.query_dsl.SpanQuery matchElasticsearch = fromLuceneSpanToElasticsearchSpan(spanFirstLucene.getMatch()).spanFirst()._toSpanQuery();
            return new org.opensearch.client.opensearch._types.query_dsl.SpanFirstQuery.Builder().end(spanFirstLucene.getEnd()).match(matchElasticsearch).build().toQuery();

        }
        else if (luceneQuery instanceof SpanNearQuery spanNearLucene)
        {
            List<org.opensearch.client.opensearch._types.query_dsl.SpanQuery> clausesElasticsearch = new ArrayList<>();
            for (SpanQuery clause : spanNearLucene.getClauses())
            {
                clausesElasticsearch.add(fromLuceneSpanToElasticsearchSpan(clause).spanNear()._toSpanQuery());
            }
            org.opensearch.client.opensearch._types.query_dsl.SpanNearQuery.Builder spanNearElasticsearch = new org.opensearch.client.opensearch._types.query_dsl.SpanNearQuery.Builder().clauses(clausesElasticsearch.get(0)).slop(spanNearLucene.getSlop());

            for (int i = 1; i < clausesElasticsearch.size(); i++)
            {
                spanNearElasticsearch.clauses(clausesElasticsearch.get(i));
            }
            return spanNearElasticsearch.build().toQuery();
        }
        else
        {
            throw new UnsupportedOperationException("Unknown span query operation " + luceneQuery);
        }
    }
}
