/*
 * #%L
 * Alfresco Search Services E2E Test
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

package org.alfresco.rest.search;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;

/**
 * Faceted Intervals Search Test
 */
public class FacetIntervalSearchTest extends AbstractSearchServicesE2ETest
{
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        searchServicesDataPreparation();
        waitForContentIndexing(file4.getContent(), true);
    }

    @Test
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH}, executionType = ExecutionType.REGRESSION,
            description = "Check facet intervals mandatory fields")
    public void checkingFacetsMandatoryErrorMessages() throws Exception
    {
        SearchRequest query = createQuery("cars");

        RestRequestFacetIntervalsModel facetIntervalsModel = new RestRequestFacetIntervalsModel();
        FacetInterval facetInterval = new FacetInterval(null, null, null);
        facetIntervalsModel.setIntervals(Collections.singletonList(facetInterval));
        query.setFacetIntervals(facetIntervalsModel);

        query(query);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.MANDATORY_PARAM, "facetIntervals intervals field"));
        facetInterval.setField("created");
        query(query);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.MANDATORY_COLLECTION, "facetIntervals intervals sets"));

        RestRequestFacetSetModel restFacetSetModel = new RestRequestFacetSetModel();
        restFacetSetModel.setLabel("theRest");
        facetInterval.setSets(Collections.singletonList(restFacetSetModel));
        query(query);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.MANDATORY_PARAM, "facetIntervals intervals created sets start"));

        restFacetSetModel.setStart("A");
        query(query);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.MANDATORY_PARAM, "facetIntervals intervals created sets end"));

        restFacetSetModel.setEnd("B");
        RestRequestFacetSetModel duplicate = new RestRequestFacetSetModel();
        facetInterval.setSets(Arrays.asList(restFacetSetModel, duplicate));
        duplicate.setLabel("theRest");
        duplicate.setStart("A");
        duplicate.setEnd("C");

        query(query);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary("duplicate set interval label [theRest=2]");

        facetInterval.setSets(Collections.singletonList(restFacetSetModel));
        facetInterval.setLabel("thesame");
        FacetInterval duplicateLabel = new FacetInterval("creator", "thesame", Collections.singletonList(duplicate));
        facetIntervalsModel.setIntervals(Arrays.asList(facetInterval, duplicateLabel));
        query.setFacetIntervals(facetIntervalsModel);

        query(query);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary("duplicate interval label [thesame=2]");
    }

    @Test
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH}, executionType = ExecutionType.REGRESSION,
            description = "Check basic facet intervals search api")
    public void searchWithBasicInterval() throws Exception
    {
        SearchRequest query = createQuery("cars");

        RestRequestFacetIntervalsModel facetIntervalsModel = new RestRequestFacetIntervalsModel();
        RestRequestFacetSetModel restRequestFacetSetModel = new RestRequestFacetSetModel();
        restRequestFacetSetModel.setStart("a");
        restRequestFacetSetModel.setEnd("user");
        restRequestFacetSetModel.setLabel("aUser");

        RestRequestFacetSetModel restFacetSetModel = new RestRequestFacetSetModel();
        restFacetSetModel.setStart("user");
        restFacetSetModel.setEnd("z");
        restFacetSetModel.setStartInclusive(false);
        restFacetSetModel.setLabel("theRest");

        FacetInterval facetInterval = new FacetInterval("creator", null, Arrays.asList(restRequestFacetSetModel, restFacetSetModel));
        facetIntervalsModel.setIntervals(Collections.singletonList(facetInterval));
        query.setFacetIntervals(facetIntervalsModel);

        SearchResponse response = query(query);
        response.assertThat().entriesListIsNotEmpty();
        response.getContext().assertThat().field("facets").isNotEmpty();
        RestGenericFacetResponseModel facetResponseModel = response.getContext().getFacets().get(0);

        RestGenericBucketModel bucket = facetResponseModel.getBuckets().get(0);
        Assert.assertEquals(facetResponseModel.getBuckets().size(), 2);
        bucket.assertThat().field("label").is("aUser");
        bucket.assertThat().field("filterQuery").is("creator:[\"a\" TO \"user\"]");
        bucket.getMetrics().getFirst().assertThat().field("type").is("count");
        bucket.getMetrics().getFirst().assertThat().field("value").contains("{count=");

        bucket = facetResponseModel.getBuckets().get(1);

        bucket.assertThat().field("label").is("theRest");
        bucket.assertThat().field("filterQuery").is("creator:[\"user\" TO \"z\"]");
        bucket.getMetrics().getFirst().assertThat().field("type").is("count");
        bucket.getMetrics().getFirst().assertThat().field("value").is("{count=0}");
    }

    @Test
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH}, executionType = ExecutionType.REGRESSION,
            description = "Check date facet intervals search api")
    public void searchWithDates() throws Exception
    {
        SearchRequest query = createQuery("cars");

        RestRequestFacetIntervalsModel facetIntervalsModel = new RestRequestFacetIntervalsModel();
        RestRequestFacetSetModel restRequestFacetSetModel = new RestRequestFacetSetModel();
        restRequestFacetSetModel.setStart("*");
        restRequestFacetSetModel.setEnd("2016");
        restRequestFacetSetModel.setEndInclusive(false);
        restRequestFacetSetModel.setLabel("Before2016");

        RestRequestFacetSetModel restFacetSetModel = new RestRequestFacetSetModel();
        restFacetSetModel.setStart("2016");
        restFacetSetModel.setEnd("now");
        restFacetSetModel.setLabel("From2016");

        FacetInterval facetInterval = new FacetInterval("cm:modified", "modified", Arrays.asList(restRequestFacetSetModel, restFacetSetModel));
        facetIntervalsModel.setIntervals(Collections.singletonList(facetInterval));
        query.setFacetIntervals(facetIntervalsModel);

        SearchResponse response = query(query);
        response.assertThat().entriesListIsNotEmpty();
        response.getContext().assertThat().field("facets").isNotEmpty();
        RestGenericFacetResponseModel facetResponseModel = response.getContext().getFacets().getFirst();

        facetResponseModel.assertThat().field("label").is("modified");
        RestGenericBucketModel bucket = facetResponseModel.getBuckets().getFirst();
        Assert.assertEquals(facetResponseModel.getBuckets().size(), 2);

        bucket.assertThat().field("label").is("From2016");
        bucket.assertThat().field("filterQuery").is("cm:modified:[\"2016\" TO \"now\"]");
        bucket.getMetrics().getFirst().assertThat().field("type").is("count");
        bucket.getMetrics().getFirst().assertThat().field("value").contains("{count=");

        bucket = facetResponseModel.getBuckets().get(1);

        bucket.assertThat().field("label").is("Before2016");
        bucket.assertThat().field("filterQuery").is("cm:modified:[\"*\" TO \"2016\"]");
        bucket.getMetrics().getFirst().assertThat().field("type").is("count");
        bucket.getMetrics().getFirst().assertThat().field("value").is("{count=0}");
    }

}
