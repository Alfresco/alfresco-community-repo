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
package org.alfresco.repo.search.impl.elasticsearch.resultset;

import static java.util.Collections.emptyList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.FieldHighlightParameters;
import org.alfresco.service.cmr.search.GeneralHighlightParameters;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.util.Pair;

@RunWith(MockitoJUnitRunner.class)
public class HighlightsHandlerTest
{
    private static final String NODE_ID = "nodeid";
    private static final NodeRef NODE_REF = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, NODE_ID);

    HighlightsHandler highlightsHandler;

    @Mock
    SearchParameters searchParameters;
    @Mock
    SearchResponse searchResponse;
    @Mock
    HitsMetadata searchHits;
    @Mock
    Hit searchHit;
    @Mock
    FieldHighlightParameters fieldHighlightParametersA;
    @Mock
    FieldHighlightParameters fieldHighlightParametersB;
    @Mock
    GeneralHighlightParameters generalHighlightParameters;

    @Before
    public void setUp()
    {
        given(searchResponse.hits()).willReturn(searchHits);
        given(searchHits.hits()).willReturn(Collections.singletonList(searchHit));
        given(searchHit.id()).willReturn(NODE_ID);
        given(generalHighlightParameters.getFields()).willReturn(List.of(fieldHighlightParametersA, fieldHighlightParametersB));
        given(searchParameters.getHighlight()).willReturn(generalHighlightParameters);

        highlightsHandler = new HighlightsHandler();
    }

    @Test
    public void handleHighlights_twoHighlightsForDocument()
    {
        given(fieldHighlightParametersA.getField()).willReturn("fieldA");
        given(fieldHighlightParametersB.getField()).willReturn("fieldB");
        given(searchHit.highlight()).willReturn(Map.of("fieldA", List.of("highlight A0", "highlight A1"), "fieldB", List.of("highlight B0")));

        Map<NodeRef, List<Pair<String, List<String>>>> actualHighlighting = highlightsHandler.handle(searchParameters, searchResponse);

        assertEquals("Unexpected set of node refs", Set.of(NODE_REF), actualHighlighting.keySet());
        Pair<String, List<String>> fieldAHighlights = new Pair<>("fieldA", List.of("highlight A0", "highlight A1"));
        Pair<String, List<String>> fieldBHighlights = new Pair<>("fieldB", List.of("highlight B0"));
        List<Pair<String, List<String>>> pairList = actualHighlighting.get(NODE_REF);
        assertThat(pairList, containsInAnyOrder(fieldAHighlights, fieldBHighlights));
    }

    @Test
    public void handleHighlights_nothingHighlighted()
    {
        given(fieldHighlightParametersA.getField()).willReturn("fieldA");
        given(fieldHighlightParametersB.getField()).willReturn("fieldB");

        Map<NodeRef, List<Pair<String, List<String>>>> actualHighlighting = highlightsHandler.handle(searchParameters, searchResponse);

        Map<NodeRef, List<Pair<String, List<String>>>> expected = Map.of(NODE_REF, emptyList());
        assertEquals(expected, actualHighlighting);
    }

    @Test
    public void handleHighlights_forContent()
    {
        given(fieldHighlightParametersA.getField()).willReturn("content");
        given(searchHit.highlight()).willReturn(Map.of("cm:content", List.of("some text")));

        Map<NodeRef, List<Pair<String, List<String>>>> actualHighlighting = highlightsHandler.handle(searchParameters, searchResponse);

        Pair<String, List<String>> highlightPair = new Pair<>("content", List.of("some text"));
        assertThat(actualHighlighting.get(NODE_REF), containsInAnyOrder(highlightPair));
    }
}
