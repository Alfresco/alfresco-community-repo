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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

public class FilterAggregationsIT extends ElasticsearchBaseQueryIT
{
    @Before
    public void initDocuments()
    {
        indexDocument("term1 term2");
        indexDocument("term1 term2");
        indexDocument("term1 term3");
        indexDocument("term1 term4");
        indexDocument("term5 term2");
    }

    @Test
    public void buildFilterAggregations_correctAftsFacetQueries_shouldReturnFilterAggregations()
    {
        SearchParameters testParam = new SearchParameters();

        testParam.addFacetQuery("{!afts key='label1'}cm:creator:(I am a query)");
        testParam.addFacetQuery("{!afts key='label2'}query2");
        testParam.addFacetQuery("{!afts key='label3'}term1 term2");
        testParam.addFacetQuery("{!afts     key='label4' }cm:creator:(I am a query)");
        testParam.addFacetQuery("{!afts key='label5'}cm:content:query5");

        Map<String, Query> filtersAggregationBuilder = elasticsearchAggregationBuilder.filterAggregation(testParam, elasticsearchAFTSQueryBuilder);

        assertThat(filtersAggregationBuilder.keySet().stream().toList().get(0), is("label1"));
        assertThat(filtersAggregationBuilder.keySet().stream().toList().get(1), is("label2"));
        assertThat(filtersAggregationBuilder.keySet().stream().toList().get(2), is("label3"));
        assertThat(filtersAggregationBuilder.keySet().stream().toList().get(3), is("label4"));
        assertThat(filtersAggregationBuilder.keySet().stream().toList().get(4), is("label5"));

        assertThat(filtersAggregationBuilder.values().stream().toList().get(0)._kind().jsonValue(), is("bool"));
        assertThat(filtersAggregationBuilder.values().stream().toList().get(1)._kind().jsonValue(), is("query_string"));// single term query with no boolean operators
        assertThat(filtersAggregationBuilder.values().stream().toList().get(2)._kind().jsonValue(), is("bool"));
        assertThat(filtersAggregationBuilder.values().stream().toList().get(3)._kind().jsonValue(), is("bool"));
        assertThat(filtersAggregationBuilder.values().stream().toList().get(4)._kind().jsonValue(), is("query_string"));// single term query with no boolean operators
    }

    @Test
    public void buildFilterAggregations_malformedAftsFacetQueries_shouldReturnOnlyWellformed()
    {
        SearchParameters testParam = new SearchParameters();

        testParam.addFacetQuery("{!afts key='label1'}cm:creator:(I am a query)");
        // This is the wrong facet query
        testParam.addFacetQuery("{!afts key='label2'}[query2]:[]notParsable");
        testParam.addFacetQuery("{!afts key='label3'}term1 term2");

        Map<String, Query> filtersAggregationBuilder = elasticsearchAggregationBuilder.filterAggregation(testParam, elasticsearchAFTSQueryBuilder);

        assertThat(filtersAggregationBuilder.size(), is(2));
        assertThat(filtersAggregationBuilder.keySet().stream().toList().get(0), is("label1"));
        assertThat(filtersAggregationBuilder.keySet().stream().toList().get(1), is("label3"));

        assertThat(filtersAggregationBuilder.values().stream().toList().get(0)._kind().jsonValue(), is("bool"));
        assertThat(filtersAggregationBuilder.values().stream().toList().get(1)._kind().jsonValue(), is("bool"));// single term query with no boolean operators

    }

    @Test
    public void executeFacetQueries_labelledFacetQuery_shouldReturnCorrectCounts()
    {
        SearchParameters testParam = new SearchParameters();
        testParam.setQuery("term1");
        testParam.addFacetQuery("{!afts key='term2Label'}term2");
        testParam.addFacetQuery("{!afts key='term3Label'}term3");
        testParam.addFacetQuery("{!afts key='term2OrTerm3Label'}term2 term3");

        ResultSet resultSetRows = searchFor(testParam);

        assertThat(resultSetRows.getNumberFound(), is(4L));
        Map<String, Integer> facetQueriesResults = resultSetRows.getFacetQueries();
        assertThat(facetQueriesResults.get("term2Label"), is(2));
        assertThat(facetQueriesResults.get("term3Label"), is(1));
        assertThat(facetQueriesResults.get("term2OrTerm3Label"), is(3));
    }

    @Test
    public void shouldFindMyDocWithCorrectlyLabelledFacetQuery()
    {
        String fileName = "myDoc";
        indexDocument(fileName, "super interesting content", Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)));
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setQuery(fileName);
        String query = "@{http://www.alfresco.org/model/content/1.0}modified:[NOW/DAY-1DAY TO NOW/DAY+1DAY]";
        searchParameters.addFacetQuery(query);

        ResultSet resultSetRows = searchFor(searchParameters);

        assertThat(resultSetRows.getNumberFound(), is(1L));
        Map<String, Integer> facetQueriesResults = resultSetRows.getFacetQueries();
        assertThat(facetQueriesResults.get(query), is(1));
        resultSetRows.close();
    }
}
