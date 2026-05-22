/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import static org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock.generateQName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Disposition lifecycle job execution unit test.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class DispositionLifecycleJobExecuterUnitTest extends BaseUnitTest
{
    /** disposition actions */
    private static final String CUTOFF = "cutoff";
    private static final String RETAIN = "retain";
    private static final String DESTROY = "destroy";
    private static final int BATCH_SIZE = 1;

    /** test query snippet */
    private static final String QUERY = "TYPE:\"rma:dispositionAction\"";
    private static final String FILTER_QUERY = "@rma\\:dispositionAction:(\"cutoff\" OR \"retain\")";
    /** mocked result set */
    @Mock
    ResultSet mockedResultSet;

    /** disposition lifecycle job executer */
    @InjectMocks
    DispositionLifecycleJobExecuter executer;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest#before()
     */
    @Override
    @Before
    public void before() throws Exception
    {
        super.before();

        Answer<Object> doInTransactionAnswer = invocation -> {
            RetryingTransactionCallback callback = (RetryingTransactionCallback) invocation.getArguments()[0];
            return callback.execute();
        };
        doAnswer(doInTransactionAnswer).when(mockedRetryingTransactionHelper).doInTransaction(any(RetryingTransactionCallback.class),
                anyBoolean(), anyBoolean());

        // setup data
        List<String> dispositionActions = buildList(CUTOFF, RETAIN);
        executer.setDispositionActions(dispositionActions);
        executer.setBatchSize(BATCH_SIZE);

        // setup interactions
        doReturn(mockedResultSet).when(mockedSearchService).query(any(SearchParameters.class));
        when(mockedResultSet.hasMore()).thenReturn(false);
    }

    /**
     * Helper method to verify that the query has been executed and closed
     * 
     * @param numberOfInvocation
     *            number of times the query has been executed and closed
     */
    private void verifyQueryTimes(int numberOfInvocation)
    {
        ArgumentCaptor<SearchParameters> paramsCaptor = ArgumentCaptor.forClass(SearchParameters.class);
        verify(mockedSearchService, times(numberOfInvocation)).query(paramsCaptor.capture());
        assertTrue(paramsCaptor.getValue().getQuery().contains(QUERY));
        assertTrue(paramsCaptor.getValue().getFilterQueries().toString().contains(FILTER_QUERY));
        verify(mockedResultSet, times(numberOfInvocation)).getNodeRefs();
        verify(mockedResultSet, times(numberOfInvocation)).close();
    }

    /**
     * When the are no results in query.
     */
    @Test
    public void noResultsInQuery()
    {
        // given
        doReturn(Collections.EMPTY_LIST).when(mockedResultSet).getNodeRefs();

        // when
        executer.executeImpl();

        // then

        // ensure the query is executed and closed
        verifyQueryTimes(1);

        // ensure nothing else happens becuase we have no results
        verifyNoMoreInteractions(mockedNodeService, mockedRecordFolderService, mockedRetryingTransactionHelper);
    }

    /**
     * When the disposition actions do not match those that can be processed automatically.
     */
    @Test
    public void dispositionActionDoesNotMatch()
    {
        // test data
        NodeRef node1 = generateNodeRef();
        NodeRef node2 = generateNodeRef();

        // given
        doReturn(DESTROY).when(mockedNodeService).getProperty(node1, RecordsManagementModel.PROP_DISPOSITION_ACTION);
        doReturn(DESTROY).when(mockedNodeService).getProperty(node2, RecordsManagementModel.PROP_DISPOSITION_ACTION);

        when(mockedResultSet.getNodeRefs())
                .thenReturn(buildList(node1))
                .thenReturn(buildList(node2));

        when(mockedResultSet.hasMore())
                .thenReturn(true)
                .thenReturn(false);

        // when
        executer.executeImpl();

        // then
        verify(mockedNodeService, times(2)).getPrimaryParent(any(NodeRef.class));
        // ensure the query is executed and closed
        verifyQueryTimes(2);

        // ensure node existence is checked for each result
        verify(mockedNodeService, times(2)).exists(any(NodeRef.class));

        // ensure each node is processed correctly
        verify(mockedNodeService, times(1)).getProperty(node1, RecordsManagementModel.PROP_DISPOSITION_ACTION);
        verify(mockedNodeService, times(1)).getProperty(node2, RecordsManagementModel.PROP_DISPOSITION_ACTION);

        // ensure no more interactions
        verifyNoMoreInteractions(mockedNodeService);
        verifyNoMoreInteractions(mockedRecordsManagementActionService);

    }

    /**
     * When a node does not exist
     */
    @Test
    public void nodeDoesNotExist()
    {
        // test data
        NodeRef node1 = generateNodeRef(null, false);
        List<NodeRef> nodeRefs = buildList(node1);

        // given
        doReturn(nodeRefs).when(mockedResultSet).getNodeRefs();

        // when
        executer.executeImpl();

        // then
        verify(mockedNodeService, times(1)).getPrimaryParent(any(NodeRef.class));
        // ensure the query is executed and closed
        verifyQueryTimes(1);

        // ensure the node exist check is made for the node
        verify(mockedNodeService, times(1)).exists(any(NodeRef.class));

        // ensure no more interactions
        verifyNoMoreInteractions(mockedNodeService);
        verifyNoMoreInteractions(mockedRecordsManagementActionService);
    }

    /**
     * When there are disposition actions eligible for processing
     */
    @Test
    public void dispositionActionProcessed()
    {
        // test data
        NodeRef node1 = generateNodeRef();
        NodeRef node2 = generateNodeRef();
        NodeRef parent = generateNodeRef();
        ChildAssociationRef parentAssoc = new ChildAssociationRef(ASSOC_NEXT_DISPOSITION_ACTION, parent, generateQName(), generateNodeRef());

        doReturn(CUTOFF).when(mockedNodeService).getProperty(node1, RecordsManagementModel.PROP_DISPOSITION_ACTION);
        doReturn(RETAIN).when(mockedNodeService).getProperty(node2, RecordsManagementModel.PROP_DISPOSITION_ACTION);
        doReturn(parentAssoc).when(mockedNodeService).getPrimaryParent(any(NodeRef.class));
        doReturn(false).when(mockedRecordFolderService).isRecordFolder(parentAssoc.getParentRef());
        doReturn(true).when(mockedRecordService).isRecord(parentAssoc.getParentRef());
        doReturn(false).when(mockedFreezeService).isFrozen(parentAssoc.getParentRef());

        when(mockedResultSet.getNodeRefs())
                .thenReturn(buildList(node1))
                .thenReturn(buildList(node2));

        when(mockedResultSet.hasMore())
                .thenReturn(true)
                .thenReturn(false);

        // when
        executer.executeImpl();

        // then

        // ensure the query is executed and closed
        verifyQueryTimes(2);

        // ensure work is executed for each node
        verify(mockedNodeService, times(2)).exists(any(NodeRef.class));

        // ensure each node is processed correctly
        // node1
        verify(mockedNodeService, times(1)).getProperty(node1, RecordsManagementModel.PROP_DISPOSITION_ACTION);
        verify(mockedNodeService, times(1)).getPrimaryParent(node1);
        verify(mockedRecordsManagementActionService, times(1)).executeRecordsManagementAction(eq(parent), eq(CUTOFF), anyMap());
        // node2
        verify(mockedNodeService, times(1)).getProperty(node2, RecordsManagementModel.PROP_DISPOSITION_ACTION);
        verify(mockedNodeService, times(1)).getPrimaryParent(node2);
        verify(mockedRecordsManagementActionService, times(1)).executeRecordsManagementAction(eq(parent), eq(RETAIN), anyMap());

        // ensure no more interactions
        verifyNoMoreInteractions(mockedNodeService, mockedRecordsManagementActionService);
    }

    /**
     * Verify that getCmisQuery() generates a valid CMIS query containing the required action filters and conditions. The query includes a dynamic UTC timestamp cutoff, so we verify key components rather than exact string match.
     */
    @Test
    public void testGetCmisQuery()
    {
        String actual = executer.getCmisQuery();

        // Verify the CMIS query contains all required components
        assertTrue("CMIS query should start with SELECT statement", actual.contains("SELECT * FROM rma:dispositionAction"));
        assertTrue("CMIS query should filter by disposition actions", actual.contains("rma:dispositionAction IN ('cutoff','retain')"));
        assertTrue("CMIS query should exclude completed actions", actual.contains("rma:dispositionActionCompletedAt IS NULL"));
        assertTrue("CMIS query should check for eligible events", actual.contains("rma:dispositionEventsEligible = true"));
        assertTrue("CMIS query should check asOf date with UTC timestamp", actual.contains("rma:dispositionAsOf <= TIMESTAMP"));
        assertTrue("CMIS query should use UTC timezone (Z suffix)", actual.contains("T23:59:59.999Z"));
    }

    /**
     * CMIS mode: when there are no results the job finishes after a single query.
     */
    @Test
    public void cmisNoResultsInQuery()
    {
        executer.setQueryMode("CMIS");
        doReturn(Collections.EMPTY_LIST).when(mockedResultSet).getNodeRefs();

        executer.executeImpl();

        verify(mockedSearchService, times(1)).query(any(SearchParameters.class));
        verify(mockedResultSet, times(1)).getNodeRefs();
        verify(mockedResultSet, times(1)).close();
        verifyNoMoreInteractions(mockedNodeService, mockedRecordsManagementActionService);
    }

    /**
     * CMIS mode: when a node no longer exists it is skipped, no action is executed, and the skip offset advances so the job does not loop on the same batch forever.
     */
    @Test
    public void cmisNodeDoesNotExist()
    {
        executer.setQueryMode("CMIS");
        NodeRef node1 = generateNodeRef(null, false); // exists = false
        ChildAssociationRef parentAssoc = new ChildAssociationRef(
                ASSOC_NEXT_DISPOSITION_ACTION, generateNodeRef(), generateQName(), generateNodeRef());
        doReturn(parentAssoc).when(mockedNodeService).getPrimaryParent(node1);
        doReturn(false).when(mockedFreezeService).isFrozenOrHasFrozenChildren(any(NodeRef.class));

        // first batch returns the non-existent node; second batch (after skip advance) is empty
        when(mockedResultSet.getNodeRefs())
                .thenReturn(Collections.singletonList(node1))
                .thenReturn(Collections.emptyList());

        executer.executeImpl();

        // two queries: one for the real batch, one after skip advance that finds nothing
        verify(mockedSearchService, times(2)).query(any(SearchParameters.class));
        verify(mockedNodeService, times(1)).getPrimaryParent(node1);
        verify(mockedNodeService, times(1)).exists(node1);
        verifyNoMoreInteractions(mockedRecordsManagementActionService);
    }

    /**
     * CMIS mode: when the disposition action on the node does not match any configured action the node is skipped and the skip offset advances.
     */
    @Test
    public void cmisDispositionActionDoesNotMatch()
    {
        executer.setQueryMode("CMIS");
        NodeRef node1 = generateNodeRef();
        ChildAssociationRef parentAssoc = new ChildAssociationRef(
                ASSOC_NEXT_DISPOSITION_ACTION, generateNodeRef(), generateQName(), generateNodeRef());
        doReturn(DESTROY).when(mockedNodeService).getProperty(node1, RecordsManagementModel.PROP_DISPOSITION_ACTION);
        doReturn(parentAssoc).when(mockedNodeService).getPrimaryParent(node1);
        doReturn(false).when(mockedFreezeService).isFrozenOrHasFrozenChildren(any(NodeRef.class));

        when(mockedResultSet.getNodeRefs())
                .thenReturn(Collections.singletonList(node1))
                .thenReturn(Collections.emptyList());

        executer.executeImpl();

        verify(mockedSearchService, times(2)).query(any(SearchParameters.class));
        verify(mockedNodeService, times(1)).getPrimaryParent(node1);
        verify(mockedNodeService, times(1)).exists(node1);
        verify(mockedNodeService, times(1)).getProperty(node1, RecordsManagementModel.PROP_DISPOSITION_ACTION);
        verifyNoMoreInteractions(mockedRecordsManagementActionService);
    }

    /**
     * CMIS mode: when disposition actions are eligible they are processed, the skip offset is reset to zero after a successful batch, and the job continues until no more results.
     */
    @Test
    public void cmisDispositionActionProcessed()
    {
        executer.setQueryMode("CMIS");
        NodeRef node1 = generateNodeRef();
        NodeRef node2 = generateNodeRef();
        NodeRef parent = generateNodeRef();
        ChildAssociationRef parentAssoc = new ChildAssociationRef(
                ASSOC_NEXT_DISPOSITION_ACTION, parent, generateQName(), generateNodeRef());

        doReturn(CUTOFF).when(mockedNodeService).getProperty(node1, RecordsManagementModel.PROP_DISPOSITION_ACTION);
        doReturn(RETAIN).when(mockedNodeService).getProperty(node2, RecordsManagementModel.PROP_DISPOSITION_ACTION);
        doReturn(parentAssoc).when(mockedNodeService).getPrimaryParent(any(NodeRef.class));
        doReturn(false).when(mockedFreezeService).isFrozenOrHasFrozenChildren(any(NodeRef.class));

        // batch 1 → node1 processed → skip resets to 0
        // batch 2 → node2 processed → skip resets to 0
        // batch 3 → empty → loop stops
        when(mockedResultSet.getNodeRefs())
                .thenReturn(Collections.singletonList(node1))
                .thenReturn(Collections.singletonList(node2))
                .thenReturn(Collections.emptyList());

        executer.executeImpl();

        verify(mockedSearchService, times(3)).query(any(SearchParameters.class));

        verify(mockedNodeService, times(1)).exists(node1);
        verify(mockedNodeService, times(1)).getPrimaryParent(node1);
        verify(mockedNodeService, times(1)).getProperty(node1, RecordsManagementModel.PROP_DISPOSITION_ACTION);
        verify(mockedRecordsManagementActionService, times(1))
                .executeRecordsManagementAction(eq(parent), eq(CUTOFF), anyMap());

        verify(mockedNodeService, times(1)).exists(node2);
        verify(mockedNodeService, times(1)).getPrimaryParent(node2);
        verify(mockedNodeService, times(1)).getProperty(node2, RecordsManagementModel.PROP_DISPOSITION_ACTION);
        verify(mockedRecordsManagementActionService, times(1))
                .executeRecordsManagementAction(eq(parent), eq(RETAIN), anyMap());
    }

    /**
     * CMIS mode: the configured query limit caps how many rows are read in one run.
     */
    @Test
    public void cmisQueryLimitStopsLoop()
    {
        executer.setQueryMode("CMIS");
        executer.setCmisQueryLimit(2);

        NodeRef node1 = generateNodeRef();
        ChildAssociationRef parentAssoc = new ChildAssociationRef(
                ASSOC_NEXT_DISPOSITION_ACTION, generateNodeRef(), generateQName(), generateNodeRef());
        doReturn(parentAssoc).when(mockedNodeService).getPrimaryParent(node1);
        doReturn(false).when(mockedFreezeService).isFrozenOrHasFrozenChildren(any(NodeRef.class));
        doReturn(false).when(mockedNodeService).exists(node1);

        when(mockedResultSet.getNodeRefs())
                .thenReturn(Collections.singletonList(node1));

        executer.executeImpl();

        verify(mockedSearchService, times(2)).query(any(SearchParameters.class));
        verify(mockedResultSet, times(2)).getNodeRefs();
        verify(mockedResultSet, times(2)).close();
    }

    /**
     * Given the maximum page of elements for search service is 2 and search service finds more than one page of elements When the job executer runs Then the executer retrieves both pages and iterates all elements
     */
    @Test
    public void testPagination()
    {
        final NodeRef node1 = generateNodeRef();
        final NodeRef node2 = generateNodeRef();
        final NodeRef node3 = generateNodeRef();
        final NodeRef node4 = generateNodeRef();

        // mock the search service to return the right page
        when(mockedSearchService.query(any(SearchParameters.class))).thenAnswer((Answer<ResultSet>) invocation -> {
            SearchParameters params = invocation.getArgument(0, SearchParameters.class);
            if (params.getSkipCount() == 0)
            {
                // mock first page
                ResultSet result1 = mock(ResultSet.class);
                when(result1.getNodeRefs()).thenReturn(Arrays.asList(node1, node2));
                when(result1.hasMore()).thenReturn(true);
                return result1;
            }
            else if (params.getSkipCount() == 2)
            {
                // mock second page
                ResultSet result2 = mock(ResultSet.class);
                when(result2.getNodeRefs()).thenReturn(Arrays.asList(node3, node4));
                when(result2.hasMore()).thenReturn(false);
                return result2;
            }
            throw new IndexOutOfBoundsException("Pagination did not stop after the second page!");
        });

        // call the service
        executer.executeImpl();

        // check the loop iterated through all the elements
        verify(mockedNodeService).exists(node1);
        verify(mockedNodeService).exists(node2);
        verify(mockedNodeService).exists(node3);
        verify(mockedNodeService).exists(node4);
        verify(mockedSearchService, times(2)).query(any(SearchParameters.class));
    }

    /**
     * Given a batch size < 1 Then the executer use default value instead
     */
    @Test
    public void testInvalidBatchSize()
    {
        executer.setBatchSize(0);
        executer.executeImpl();

        ArgumentCaptor<SearchParameters> paramsCaptor = ArgumentCaptor.forClass(SearchParameters.class);
        verify(mockedSearchService, times(1)).query(paramsCaptor.capture());
        assertEquals(DispositionLifecycleJobExecuter.DEFAULT_BATCH_SIZE, paramsCaptor.getValue().getMaxItems());
        verify(mockedResultSet, times(1)).close();

        executer.setBatchSize(BATCH_SIZE);
    }
}
