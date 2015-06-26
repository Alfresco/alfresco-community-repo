/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.search.impl.querymodel.QueryEngineResults;
import org.alfresco.service.cmr.search.ResultSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Sets;

/**
 * Unit tests for {@link QueryEngineResultPostMethodInvocationProcessor}.
 *
 * @author Tom Page
 * @since 3.0
 */
public class QueryEngineResultsPostMethodInvocationProcessorUnitTest
{
    private static final Set<String> KEY_1 = Sets.newHashSet("KEY_1");
    private static final Set<String> KEY_2 = Sets.newHashSet("KEY_2");
    private static final Set<String> KEY_3 = Sets.newHashSet("KEY_3");
    private static final Set<String> KEY_4 = Sets.newHashSet("KEY_4");
    private static final ResultSet UNCLASSIFIED_RESULT_SET = mock(ResultSet.class);
    private static final ResultSet CLASSIFIED_RESULT_SET = mock(ResultSet.class);

    @InjectMocks
    private QueryEngineResultsPostMethodInvocationProcessor processor = new QueryEngineResultsPostMethodInvocationProcessor();
    @Mock
    private PostMethodInvocationProcessor mockPostMethodInvocationProcessor;
    @Mock
    private ResultSetPostMethodInvocationProcessor mockResultSetPMIP;

    @Before
    public void setUp()
    {
        initMocks(this);

        when(mockPostMethodInvocationProcessor.getProcessor(Mockito.any())).thenReturn(mockResultSetPMIP);

        when(mockResultSetPMIP.process(UNCLASSIFIED_RESULT_SET)).thenReturn(UNCLASSIFIED_RESULT_SET);
        when(mockResultSetPMIP.process(CLASSIFIED_RESULT_SET)).thenReturn(null);
    }

    /** Check that {@code process} filters out the classified result sets. */
    @Test
    public void testProcess()
    {
        Map<Set<String>, ResultSet> resultsMap = new HashMap<>();
        resultsMap.put(KEY_1, UNCLASSIFIED_RESULT_SET);
        resultsMap.put(KEY_2, CLASSIFIED_RESULT_SET);
        resultsMap.put(KEY_3, UNCLASSIFIED_RESULT_SET);
        resultsMap.put(KEY_4, CLASSIFIED_RESULT_SET);
        QueryEngineResults queryEngineResults = new QueryEngineResults(resultsMap);

        QueryEngineResults returnedQueryEngineResults = processor.process(queryEngineResults);

        Map<Set<String>, ResultSet> expectedResultSet = new HashMap<>();
        expectedResultSet.put(KEY_1, UNCLASSIFIED_RESULT_SET);
        expectedResultSet.put(KEY_3, UNCLASSIFIED_RESULT_SET);
        assertEquals("Unexpected results from query.", expectedResultSet, returnedQueryEngineResults.getResults());
    }
}
