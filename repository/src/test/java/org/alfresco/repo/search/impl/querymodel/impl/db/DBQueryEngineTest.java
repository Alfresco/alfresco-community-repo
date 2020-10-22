/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.search.impl.querymodel.impl.db;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.repo.search.impl.querymodel.QueryOptions;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryEngine.NodePermissionAssessor;
import org.alfresco.repo.security.permissions.impl.acegi.FilteringResultSet;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class DBQueryEngineTest
{
    private static final String SQL_TEMPLATE_PATH = "alfresco.metadata.query.select_byDynamicQuery";
    
    private DBQueryEngine engine;
    private SqlSessionTemplate template;
    private NodePermissionAssessor assessor;
    private DBQuery dbQuery;
    private ResultContext<Node> resultContext;
    private QueryOptions options;
    private EntityLookupCache<Long, Node, NodeRef> nodesCache;

    @Before
    public void setup()
    {
        template = mock(SqlSessionTemplate.class);
        nodesCache = mock(EntityLookupCache.class);
        engine = new DBQueryEngine();
        engine.setSqlSessionTemplate(template);
        engine.setNodesCache(nodesCache);
        assessor = mock(NodePermissionAssessor.class);
        dbQuery = mock(DBQuery.class);
        resultContext = spy(new DefaultResultContext<>());
        options = createQueryOptions();
    }
    
    @Test
    public void shouldGetAFilteringResultSetFromAcceleratedNodeSelection()
    {
        withMaxItems(10);

        ResultSet result = engine.acceleratedNodeSelection(options, dbQuery, assessor);

        assertTrue(result instanceof FilteringResultSet);
    }

    @Test
    public void shouldResultSetHaveExpectedAmountOfRequiredNodesBasedOnMaxItems()
    {
        withMaxItems(5);
        prepareTemplate(dbQuery, createNodes(20));
        when(assessor.isIncluded(any(Node.class))).thenReturn(true);
                
        FilteringResultSet result = engine.acceleratedNodeSelection(options, dbQuery, assessor);
        
        assertEquals(6, result.length());
        assertNodePresent(0, result);
        assertNodePresent(1, result);
        assertNodePresent(2, result);
        assertNodePresent(3, result);
        assertNodePresent(4, result);
    }
    
    @Test
    public void shouldResultContextBeClosedWhenMaxItemsReached()
    {
        withMaxItems(5);
        prepareTemplate(dbQuery, createNodes(20));
        when(assessor.isIncluded(any(Node.class))).thenReturn(true);
                
        FilteringResultSet result = engine.acceleratedNodeSelection(options, dbQuery, assessor);

        verify(resultContext).stop();
        assertEquals(6, result.length());
    }

    @Test
    public void shouldResultSetHaveCorrectAmountOfRequiredNodesWhenSkipCountIsUsed()
    {
        withMaxItems(5);
        withSkipCount(10);
        prepareTemplate(dbQuery, createNodes(20));
        when(assessor.isIncluded(any(Node.class))).thenReturn(true);
                
        FilteringResultSet result = engine.acceleratedNodeSelection(options, dbQuery, assessor);
        
        assertEquals(6, result.length());
        assertNodePresent(10, result);
        assertNodePresent(11, result);
        assertNodePresent(12, result);
        assertNodePresent(13, result);
        assertNodePresent(14, result);
    }
    
    @Test
    public void shouldNotConsiderInaccessibleNodesInResultSet()
    {
        withMaxItems(5);
        List<Node> nodes = createNodes(20);
        when(assessor.isIncluded(any(Node.class))).thenReturn(true);
        when(assessor.isIncluded(nodes.get(0))).thenReturn(false);
        when(assessor.isIncluded(nodes.get(1))).thenReturn(false);
        when(assessor.isIncluded(nodes.get(2))).thenReturn(false);
        prepareTemplate(dbQuery, nodes);
        
        FilteringResultSet result = engine.acceleratedNodeSelection(options, dbQuery, assessor);
        
        assertEquals(6, result.length());
        assertNodePresent(3, result);
        assertNodePresent(4, result);
        assertNodePresent(5, result);
        assertNodePresent(6, result);
        assertNodePresent(7, result);
    }
    
    @Test
    public void shouldNotConsiderInaccessibleNodesInResultSetWhenSkippingNodes()
    {
        withMaxItems(2);
        withSkipCount(2);
        List<Node> nodes = createNodes(6);
        when(assessor.isIncluded(any(Node.class))).thenReturn(true);
        when(assessor.isIncluded(nodes.get(2))).thenReturn(false);
        when(assessor.isIncluded(nodes.get(3))).thenReturn(false);
        prepareTemplate(dbQuery, nodes);
        
        FilteringResultSet result = engine.acceleratedNodeSelection(options, dbQuery, assessor);
        
        assertEquals(2, result.length());
        assertNodePresent(4, result);
        assertNodePresent(5, result);
    }
    
    @Test
    public void shouldQuitCheckingNodePermissionsWhenImposedLimitsAreReached()
    {
        prepareTemplate(dbQuery, createNodes(20));
        when(assessor.shouldQuitChecks()).thenReturn(true);
        
        FilteringResultSet result = engine.acceleratedNodeSelection(options, dbQuery, assessor);
        
        assertEquals(0, result.length());
        verify(resultContext).stop();
    }
    
    @Test
    public void shouldNodePermissionAssessorLimitisBeOverridenWhenSetValuesAreProvidedInQueryOptions()
    {
        when(options.getMaxPermissionChecks()).thenReturn(2000);
        when(options.getMaxPermissionCheckTimeMillis()).thenReturn(20000L);
        
        NodePermissionAssessor assessor = engine.createAssessor(options);
        
        assertEquals(assessor.getMaxPermissionChecks(), 2000);
        assertEquals(assessor.getMaxPermissionCheckTimeMillis(), 20000L);
    }
        
    private void prepareTemplate(DBQuery dbQuery, List<Node> nodes)
    {
        doAnswer(invocation -> {
            ResultHandler<Node> handler = (ResultHandler<Node>)invocation.getArgument(2);
            for (Node node: nodes)
            {
                if (!resultContext.isStopped())
                {
                    when(resultContext.getResultObject()).thenReturn(node);
                    handler.handleResult(resultContext);
                }
            }
            return null;
            
        }).when(template).select(eq(SQL_TEMPLATE_PATH), eq(dbQuery), any());
    }
    
    private QueryOptions createQueryOptions()
    {
        QueryOptions options = mock(QueryOptions.class);
        SearchParameters searchParams = mock(SearchParameters.class);
        when(options.getAsSearchParmeters()).thenReturn(searchParams);
        return options;
    }
    
    private void assertNodePresent(long id, FilteringResultSet result)
    {
        DBResultSet rs = (DBResultSet)result.getUnFilteredResultSet();
        for(int i = 0; i < rs.length(); i++)
        {
            if(rs.getNode(i).getId().equals(id))
            {
                return;
            }
        }
        fail("Node with id " + id + " was not found in the result set");
    }

    private void withMaxItems(int maxItems)
    {
        when(options.getMaxItems()).thenReturn(maxItems);
    }
    
    private void withSkipCount(int skipCount)
    {
        when(options.getSkipCount()).thenReturn(skipCount);
    }
    
    private List<Node> createNodes(int amount)
    {
        List<Node> nodes = new ArrayList<>();
        
        for(int i = 0; i < amount; i++)
        {
            nodes.add(createNode(i));
        }
        
        return nodes;
    }
    
    private Node createNode(int id) 
    {
        Node node = spy(NodeEntity.class);
        when(node.getId()).thenReturn((long)id);
        return node;
    }
    
}
