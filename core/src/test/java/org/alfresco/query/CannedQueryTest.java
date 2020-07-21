/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.util.Pair;
import org.alfresco.util.registry.NamedObjectRegistry;

/**
 * Tests the {@link CannedQuery name query} infrastructure.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class CannedQueryTest extends TestCase
{
    private static final String QUERY_TEST_ONE = "test.query.one";
    private static final String QUERY_TEST_TWO = "test.query.two";
    
    private static final List<String> RESULTS_ONE;
    private static final List<Long> RESULTS_TWO;
    private static final Set<Object> ANTI_RESULTS;
    
    static
    {
        RESULTS_ONE = new ArrayList<String>(10);
        for (int i = 0; i < 10; i++)
        {
            RESULTS_ONE.add("ONE_" + i);
        }
        RESULTS_TWO = new ArrayList<Long>(10);
        for (int i = 0; i < 10; i++)
        {
            RESULTS_TWO.add(new Long(i));
        }
        ANTI_RESULTS = new HashSet<Object>();
        ANTI_RESULTS.add("ONE_5");
        ANTI_RESULTS.add(new Long(5));
    }
    
    @SuppressWarnings("rawtypes")
    private NamedObjectRegistry<CannedQueryFactory> namedQueryFactoryRegistry;
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void setUp() throws Exception
    {
        // Create the registry
        namedQueryFactoryRegistry = new NamedObjectRegistry<CannedQueryFactory>();
        namedQueryFactoryRegistry.setStorageType(CannedQueryFactory.class);
        namedQueryFactoryRegistry.setNamePattern("test\\.query\\..*");
        // Registry the query factories
        // ONE
        TestCannedQueryFactory<String> namedQueryFactoryOne = new TestCannedQueryFactory(RESULTS_ONE);
        namedQueryFactoryOne.setBeanName(QUERY_TEST_ONE);
        namedQueryFactoryOne.setRegistry(namedQueryFactoryRegistry);
        namedQueryFactoryOne.afterPropertiesSet();
        // TWO
        TestCannedQueryFactory<Long> namedQueryFactoryTwo = new TestCannedQueryFactory(RESULTS_TWO);
        namedQueryFactoryTwo.setBeanName(QUERY_TEST_TWO);
        namedQueryFactoryTwo.setRegistry(namedQueryFactoryRegistry);
        namedQueryFactoryTwo.afterPropertiesSet();
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testRegistry() throws Exception
    {
        CannedQueryFactory<String> one = namedQueryFactoryRegistry.getNamedObject(QUERY_TEST_ONE);
        assertNotNull("No factory for " + QUERY_TEST_ONE, one);
        CannedQueryFactory<String> two = namedQueryFactoryRegistry.getNamedObject(QUERY_TEST_TWO);
        assertNotNull("No factory for " + QUERY_TEST_TWO, two);
        // Kick out registrations with incorrect naming convention
        try
        {
            TestCannedQueryFactory<Long> namedQueryFactoryBogus = new TestCannedQueryFactory(RESULTS_TWO);
            namedQueryFactoryBogus.setBeanName("test_query_blah");
            namedQueryFactoryBogus.setRegistry(namedQueryFactoryRegistry);
            namedQueryFactoryBogus.afterPropertiesSet();
            fail("Should have kicked out incorrectly-named registered queries");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }
    
    @SuppressWarnings("unchecked")
    public void testQueryAllResults() throws Exception
    {
        // An instance of the CannedQueryFactory could be injected or constructed as well
        CannedQueryFactory<String> qfOne = namedQueryFactoryRegistry.getNamedObject(QUERY_TEST_ONE);
        CannedQueryParameters params = new CannedQueryParameters(null);
        CannedQuery<String> qOne = qfOne.getCannedQuery(params);
        CannedQueryResults<String> qrOne = qOne.execute();
        // Attempt to reuse the query
        try
        {
            qOne.execute();
            fail("Second execution of same instance must not be allowed.");
        }
        catch (IllegalStateException e)
        {
            // Expected
        }
        // Get the number of results when not requested
        try
        {
            qrOne.getTotalResultCount();
            fail("Expected failure when requesting total count without explicit request.");
        }
        catch (IllegalStateException e)
        {
            // Expected
        }
        // Get the paged result count
        int pagedResultCount = qrOne.getPagedResultCount();
        assertEquals("Incorrect number of results", 9, pagedResultCount);
        assertEquals("No sorting was specified in the parameters", "ONE_0", qrOne.getPages().get(0).get(0));
        assertFalse("Should NOT have any more pages/items", qrOne.hasMoreItems());
    }
    
    @SuppressWarnings("unchecked")
    public void testQueryMaxResults() throws Exception
    {
        // An instance of the CannedQueryFactory could be injected or constructed as well
        CannedQueryFactory<String> qfOne = namedQueryFactoryRegistry.getNamedObject(QUERY_TEST_ONE);
        CannedQuery<String> qOne = qfOne.getCannedQuery(null, 0, 9, null);
        CannedQueryResults<String> qrOne = qOne.execute();
        
        // Get the paged result count
        int pagedResultCount = qrOne.getPagedResultCount();
        assertEquals("Incorrect number of results", 9, pagedResultCount);
        assertEquals("Incorrect number of pages", 1, qrOne.getPageCount());
        List<List<String>> pages = qrOne.getPages();
        assertEquals("Incorrect number of pages", 1, pages.size());
        assertEquals("No sorting was specified in the parameters", "ONE_0", qrOne.getPages().get(0).get(0));
        assertEquals("No sorting was specified in the parameters", "ONE_9", qrOne.getPages().get(0).get(8));
        assertFalse("Should have more pages/items", qrOne.hasMoreItems());
    }
    
    @SuppressWarnings("unchecked")
    public void testQueryPagedResults() throws Exception
    {
        CannedQueryFactory<String> qfOne = namedQueryFactoryRegistry.getNamedObject(QUERY_TEST_ONE);
        CannedQueryPageDetails qPageDetails = new CannedQueryPageDetails(0, 5, 1, 2);
        CannedQueryParameters params = new CannedQueryParameters(null, qPageDetails, null);
        CannedQuery<String> qOne = qfOne.getCannedQuery(params);
        CannedQueryResults<String> qrOne = qOne.execute();
        // Check pages
        assertEquals("Incorrect number of results", 9, qrOne.getPagedResultCount());
        assertEquals("No sorting was specified in the parameters", "ONE_0", qrOne.getPages().get(0).get(0));
        assertEquals("No sorting was specified in the parameters", "ONE_9", qrOne.getPages().get(1).get(3));
        List<List<String>> pages = qrOne.getPages();
        assertEquals("Incorrect number of pages", 2, pages.size());
        assertEquals("Incorrect results on page", 5, pages.get(0).size());
        assertEquals("Incorrect results on page", 4, pages.get(1).size());
        assertFalse("Should NOT have any more pages/items", qrOne.hasMoreItems());
        
        // Skip some results and use different page sizes
        qPageDetails = new CannedQueryPageDetails(2, 3, 1, 3);
        params = new CannedQueryParameters(null, qPageDetails, null);
        qOne = qfOne.getCannedQuery(params);
        qrOne = qOne.execute();
        // Check pages
        assertEquals("Incorrect number of results", 7, qrOne.getPagedResultCount());
        assertEquals("Incorrect number of pages", 3, qrOne.getPageCount());
        pages = qrOne.getPages();
        assertEquals("Incorrect number of pages", 3, pages.size());
        assertEquals("Incorrect results on page", 3, pages.get(0).size());
        assertEquals("Incorrect results on page", 3, pages.get(1).size());
        assertEquals("Incorrect results on page", 1, pages.get(2).size());
        assertFalse("Should NOT have any more pages/items", qrOne.hasMoreItems());
        
        // Skip some results and return less pages
        qPageDetails = new CannedQueryPageDetails(2, 3, 1, 2);
        params = new CannedQueryParameters(null, qPageDetails, null);
        qOne = qfOne.getCannedQuery(params);
        qrOne = qOne.execute();
        // Check pages
        assertEquals("Incorrect number of results", 6, qrOne.getPagedResultCount());
        assertEquals("Incorrect number of pages", 2, qrOne.getPageCount());
        pages = qrOne.getPages();
        assertEquals("Incorrect number of pages", 2, pages.size());
        assertEquals("Incorrect results on page", 3, pages.get(0).size());
        assertEquals("Incorrect results on page", 3, pages.get(1).size());
        assertTrue("Should have more pages/items", qrOne.hasMoreItems());
    }
    
    @SuppressWarnings("unchecked")
    public void testQuerySortedResults() throws Exception
    {
        CannedQueryFactory<String> qfOne = namedQueryFactoryRegistry.getNamedObject(QUERY_TEST_ONE);
        CannedQuerySortDetails qSortDetails = new CannedQuerySortDetails(
                new Pair<Object, SortOrder>("blah", SortOrder.DESCENDING));
        CannedQueryParameters params = new CannedQueryParameters(null, null, qSortDetails);
        CannedQuery<String> qOne = qfOne.getCannedQuery(params);
        CannedQueryResults<String> qrOne = qOne.execute();
        // Check pages
        assertEquals("Incorrect number of results", 9, qrOne.getPagedResultCount());
        assertEquals("Expected inverse sorting", "ONE_9", qrOne.getPages().get(0).get(0));
        assertEquals("Expected inverse sorting", "ONE_0", qrOne.getPages().get(0).get(8));
        assertFalse("Should NOT have any more pages/items", qrOne.hasMoreItems());
    }
    
    @SuppressWarnings("unchecked")
    public void testQueryPermissionCheckedResults() throws Exception
    {
        CannedQueryFactory<String> qfOne = namedQueryFactoryRegistry.getNamedObject(QUERY_TEST_ONE);
        CannedQueryParameters params = new CannedQueryParameters(null, null, null, 0, null);
        CannedQuery<String> qOne = qfOne.getCannedQuery(params);
        CannedQueryResults<String> qrOne = qOne.execute();
        // Check pages
        assertEquals("Incorrect number of results", 9, qrOne.getPagedResultCount());
        assertEquals("Incorrect result order", "ONE_0", qrOne.getPages().get(0).get(0));
        assertEquals("Incorrect result order", "ONE_1", qrOne.getPages().get(0).get(1));
        assertEquals("Incorrect result order", "ONE_2", qrOne.getPages().get(0).get(2));
        assertEquals("Incorrect result order", "ONE_3", qrOne.getPages().get(0).get(3));
        assertEquals("Incorrect result order", "ONE_4", qrOne.getPages().get(0).get(4)); // << missing 5!
        assertEquals("Incorrect result order", "ONE_6", qrOne.getPages().get(0).get(5));
        assertEquals("Incorrect result order", "ONE_7", qrOne.getPages().get(0).get(6));
        assertEquals("Incorrect result order", "ONE_8", qrOne.getPages().get(0).get(7));
        assertEquals("Incorrect result order", "ONE_9", qrOne.getPages().get(0).get(8));
        assertFalse("Should NOT have any more pages/items", qrOne.hasMoreItems());
    }
    
    @SuppressWarnings("unchecked")
    public void testQueryPermissionCheckedPagedTotalCount() throws Exception
    {
        CannedQueryFactory<String> qfOne = namedQueryFactoryRegistry.getNamedObject(QUERY_TEST_ONE);
        CannedQueryPageDetails qPageDetails = new CannedQueryPageDetails(5, 1, 1, 1);
        CannedQuerySortDetails qSortDetails = new CannedQuerySortDetails(
                new Pair<Object, SortOrder>("blah", SortOrder.DESCENDING));
        CannedQueryParameters params = new CannedQueryParameters(null, qPageDetails, qSortDetails, 1000, null);
        CannedQuery<String> qOne = qfOne.getCannedQuery(params);
        CannedQueryResults<String> qrOne = qOne.execute();
        // Check pages
        assertEquals("Incorrect number of total results",
                new Pair<Integer, Integer>(9,9), qrOne.getTotalResultCount()); // Pre-paging
        assertEquals("Incorrect number of paged results", 1, qrOne.getPagedResultCount());             // Skipped 5
        assertEquals("Incorrect result order", "ONE_3", qrOne.getPages().get(0).get(0));               // Order reversed
        assertTrue("Should have more pages/items", qrOne.hasMoreItems());
    }
    
    /**
     * Test factory to generate "queries" that just return a list of <tt>String</tt>s.
     *
     * @param <T>           the type of the results
     * 
     * @author Derek Hulley
     * @since 4.0
     */
    private static class TestCannedQueryFactory<T> extends AbstractCannedQueryFactory<T>
    {
        private final List<T> results;
        private TestCannedQueryFactory(List<T> results)
        {
            this.results = results;
        }
        
        @Override
        public CannedQuery<T> getCannedQuery(CannedQueryParameters parameters)
        {
            String queryExecutionId = super.getQueryExecutionId(parameters);
            return new TestCannedQuery<T>(parameters, queryExecutionId, results, ANTI_RESULTS);
        }
    }

    /**
     * Test query that just returns values passed in
     *
     * @param <T>           the type of the results
     * 
     * @author Derek Hulley
     * @since 4.0
     */
    private static class TestCannedQuery<T> extends AbstractCannedQuery<T>
    {
        private final List<T> results;
        private final Set<Object> antiResults;
        private TestCannedQuery(
                CannedQueryParameters params,
                String queryExecutionId, List<T> results, Set<Object> antiResults)
        {
            super(params);
            this.results = results;
            this.antiResults = antiResults;
        }
        
        @Override
        protected List<T> queryAndFilter(CannedQueryParameters parameters)
        {
            return results;
        }

        @Override
        protected boolean isApplyPostQuerySorting()
        {
            return true;
        }

        @Override
        protected List<T> applyPostQuerySorting(List<T> results, CannedQuerySortDetails sortDetails)
        {
            if (sortDetails.getSortPairs().size() == 0)
            {
                // Nothing to sort on
                return results;
            }
            List<T> ret = new ArrayList<T>(results);
            Collections.reverse(ret);
            return ret;
        }

        @Override
        protected boolean isApplyPostQueryPermissions()
        {
            return true;
        }

        @Override
        protected List<T> applyPostQueryPermissions(List<T> results, int requestedCount)
        {
            boolean cutoffAllowed = (getParameters().getTotalResultCountMax() == 0);
            
            final List<T> ret = new ArrayList<T>(results.size());
            for (T t : results)
            {
                if (!antiResults.contains(t))
                {
                    ret.add(t);
                }
                // Cut off if we have enough results
                if (cutoffAllowed && ret.size() == requestedCount)
                {
                    break;
                }
            }
            
            return ret;
        }
    }
}
