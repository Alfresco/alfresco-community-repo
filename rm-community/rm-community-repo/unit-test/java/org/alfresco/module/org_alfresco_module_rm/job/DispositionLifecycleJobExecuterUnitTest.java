/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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

import static org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock.generateQName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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

    /** test query snipit */
    private static final String QUERY = "\"" + CUTOFF + "\" OR \"" + RETAIN + "\"";

    /** mocked result set */
    @Mock ResultSet mockedResultSet;

    /** disposition lifecycle job executer */
    @InjectMocks DispositionLifecycleJobExecuter executer;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest#before()
     */
    @Override
    @Before
    public void before() throws Exception
    {
        super.before();

        // setup data
        List<String> dispositionActions = buildList(CUTOFF, RETAIN);
        executer.setDispositionActions(dispositionActions);

        // setup interactions
        doReturn(mockedResultSet).when(mockedSearchService).query(any(SearchParameters.class));
        when(mockedResultSet.hasMore()).thenReturn(false);
    }

    /**
     * Helper method to verify that the query has been executed and closed
     */
    private void verifyQuery()
    {
        ArgumentCaptor<SearchParameters> paramsCaptor = ArgumentCaptor.forClass(SearchParameters.class);
        verify(mockedSearchService, times(1)).query(paramsCaptor.capture());
        assertTrue(paramsCaptor.getValue().getQuery().contains(QUERY));
        verify(mockedResultSet, times(1)).getNodeRefs();
        verify(mockedResultSet, times(1)).close();
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
        verifyQuery();

        // ensure nothing else happens becuase we have no results
        verifyZeroInteractions(mockedNodeService, mockedRecordFolderService, mockedRetryingTransactionHelper);
    }

    /**
     * When the disposition actions do not match those that can be processed automatically.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void dispositionActionDoesNotMatch()
    {
        // test data
        NodeRef node1 = generateNodeRef();
        NodeRef node2 = generateNodeRef();
        List<NodeRef> nodeRefs = buildList(node1, node2);

        // given
        doReturn(nodeRefs).when(mockedResultSet).getNodeRefs();
        doReturn(DESTROY).when(mockedNodeService).getProperty(node1, RecordsManagementModel.PROP_DISPOSITION_ACTION);
        doReturn(DESTROY).when(mockedNodeService).getProperty(node2, RecordsManagementModel.PROP_DISPOSITION_ACTION);

        // when
        executer.executeImpl();

        // then

        // ensure the query is executed and closed
        verifyQuery();

        // ensure work is executed in transaction for each node processed
        verify(mockedNodeService, times(2)).exists(any(NodeRef.class));
        verify(mockedRetryingTransactionHelper, times(2)).<Object>doInTransaction(any(RetryingTransactionCallback.class));

        // ensure each node is process correctly
        verify(mockedNodeService, times(1)).getProperty(node1, RecordsManagementModel.PROP_DISPOSITION_ACTION);
        verify(mockedNodeService, times(1)).getProperty(node2, RecordsManagementModel.PROP_DISPOSITION_ACTION);

        // ensure no more interactions
        verifyNoMoreInteractions(mockedNodeService);
        verifyZeroInteractions(mockedRecordsManagementActionService);

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

        // ensure the query is executed and closed
        verifyQuery();

        // ensure the node exist check is made for the node
        verify(mockedNodeService, times(1)).exists(any(NodeRef.class));

        // ensure no more interactions
        verifyNoMoreInteractions(mockedNodeService);
        verifyZeroInteractions(mockedRecordsManagementActionService, mockedRetryingTransactionHelper);
    }

    /**
     * When there are disposition actions eligible for processing
     */
    @SuppressWarnings("unchecked")
    @Test
    public void dispositionActionProcessed()
    {
        // test data
        NodeRef node1 = generateNodeRef();
        NodeRef node2 = generateNodeRef();
        List<NodeRef> nodeRefs = buildList(node1, node2);
        NodeRef parent = generateNodeRef();
        ChildAssociationRef parentAssoc = new ChildAssociationRef(ASSOC_NEXT_DISPOSITION_ACTION, parent, generateQName(), generateNodeRef());

        // given
        doReturn(nodeRefs).when(mockedResultSet).getNodeRefs();
        doReturn(CUTOFF).when(mockedNodeService).getProperty(node1, RecordsManagementModel.PROP_DISPOSITION_ACTION);
        doReturn(RETAIN).when(mockedNodeService).getProperty(node2, RecordsManagementModel.PROP_DISPOSITION_ACTION);
        doReturn(parentAssoc).when(mockedNodeService).getPrimaryParent(any(NodeRef.class));

        // when
        executer.executeImpl();

        // then

        // ensure the query is executed and closed
        verifyQuery();

        // ensure work is executed in transaction for each node processed
        verify(mockedNodeService, times(2)).exists(any(NodeRef.class));
        verify(mockedRetryingTransactionHelper, times(2)).<Object>doInTransaction(any(RetryingTransactionCallback.class));

        // ensure each node is process correctly
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
     * Brittle unit test that simply checks the generated query is an exact string when the supplied disposition actions
     * are "CUTOFF" and "RETAIN" (see {@link #before}).
     */
    @Test
    public void testGetQuery()
    {
        String actual = executer.getQuery();

        String expected = "TYPE:\"rma:dispositionAction\" AND " +
                "(@rma\\:dispositionAction:(\"cutoff\" OR \"retain\")) " +
                "AND ISUNSET:\"rma:dispositionActionCompletedAt\"  " +
                "AND ( @rma\\:dispositionEventsEligible:true OR @rma\\:dispositionAsOf:[MIN TO NOW] ) ";

        assertEquals(expected, actual);
    }

    /**
     * Given the maximum page of elements for search service is 2
     *       and search service finds more than one page of elements
     * When the job executer runs
     * Then the executer retrieves both pages and iterates all elements
     */
    @Test
    public void testPagination()
    {
        final NodeRef node1 = generateNodeRef();
        final NodeRef node2 = generateNodeRef();
        final NodeRef node3 = generateNodeRef();
        final NodeRef node4 = generateNodeRef();

        // mock the search service to return the right page
        when(mockedSearchService.query(any(SearchParameters.class))).thenAnswer(
            new Answer<ResultSet>()
            {
                @Override
                public ResultSet answer(InvocationOnMock invocation)
                {
                    SearchParameters params = invocation.getArgumentAt(0, SearchParameters.class);
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
                }
            });

        // call the service
        executer.executeImpl();

        // check the loop iterated trough all the elements
        verify(mockedNodeService).exists(node1);
        verify(mockedNodeService).exists(node2);
        verify(mockedNodeService).exists(node3);
        verify(mockedNodeService).exists(node4);
        verify(mockedSearchService, times(2)).query(any(SearchParameters.class));
    }
}
