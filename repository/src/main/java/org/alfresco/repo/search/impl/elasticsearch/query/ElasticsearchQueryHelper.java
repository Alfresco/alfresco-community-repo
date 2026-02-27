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
package org.alfresco.repo.search.impl.elasticsearch.query;

import static java.util.Optional.of;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opensearch.client.opensearch._types.query_dsl.Query;

import org.alfresco.repo.search.impl.elasticsearch.query.language.afts.AFTSBooleanOperatorsAdaptor;
import org.alfresco.util.Pair;

public class ElasticsearchQueryHelper
{
    private static final Pattern FILTER_QUERY_CLEANUP_PATTERN = Pattern.compile("(\\{.*})(.*)");
    private static final Pattern KEY_FACET_QUERY_EXTRACT_PATTERN = Pattern.compile("(\\{.*key='(.*)'.*})(.*)");
    private static final Pattern NAMESPACE_FACET_QUERY_PATTERN = Pattern.compile("@\\{[^}]+}[^:]+:.+$");

    /**
     * 
     * @param query
     *            the query to check
     * @return true, if the query is a MatchNoneQueryBuilder because we can assume that it is an empty query. For instance an empty query filter is returned by {@link AFTSBooleanOperatorsAdaptor#getQuery()} when all queries were ignored
     */
    public static boolean isEmptyFilterQuery(Query query)
    {
        return query.isMatchNone();
    }

    public static String cleanUpFilterQueries(String filterQuery)
    {
        final Matcher matcher = FILTER_QUERY_CLEANUP_PATTERN.matcher(filterQuery);
        if (matcher.matches() && matcher.groupCount() == 2)
        {
            return matcher.group(2);
        }

        return filterQuery;
    }

    public static Optional<Pair<String, String>> extractFacetQueryAndLabel(String facetQuery)
    {
        final Matcher keyFacetMatcher = KEY_FACET_QUERY_EXTRACT_PATTERN.matcher(facetQuery);
        if (keyFacetMatcher.matches() && keyFacetMatcher.groupCount() == 3)
        {
            return of(new Pair<>(keyFacetMatcher.group(3), keyFacetMatcher.group(2)));
        }
        final Matcher namespaceFacetMatcher = NAMESPACE_FACET_QUERY_PATTERN.matcher(facetQuery);
        if (namespaceFacetMatcher.matches())
        {
            return of(new Pair<>(facetQuery, facetQuery));
        }
        return Optional.empty();
    }

}
