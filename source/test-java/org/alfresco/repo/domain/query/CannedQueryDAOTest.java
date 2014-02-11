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
package org.alfresco.repo.domain.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.repo.domain.mimetype.MimetypeDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO.ResultHandler;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.GUID;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;
import org.junit.runners.MethodSorters;

/**
 * @see CannedQueryDAO
 * 
 * @author Derek Hulley
 * @since 3.2
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(OwnJVMTestsCategory.class)
public class CannedQueryDAOTest
{
    private static final String QUERY_NS = "alfresco.query.test";
    private static final String QUERY_SELECT_MIMETYPE_COUNT = "select_CountMimetypes";
    private static final String QUERY_SELECT_MIMETYPES = "select_Mimetypes";
    public static final String IBATIS_TEST_CONTEXT = "classpath*:alfresco/ibatis/ibatis-test-context.xml";
    
    // Rule to initialise the default Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT = ApplicationContextInit.createStandardContextWithOverrides(IBATIS_TEST_CONTEXT);

    // Tie them together in a static Rule Chain
    @ClassRule public static RuleChain staticRuleChain = RuleChain.outerRule(APP_CONTEXT_INIT);
    
    private static TransactionService transactionService;
    private static RetryingTransactionHelper txnHelper;
    private static CannedQueryDAO cannedQueryDAO;
    private static CannedQueryDAO cannedQueryDAOForTesting;
    private static MimetypeDAO mimetypeDAO;
    
    private static String mimetypePrefix;
    
    @BeforeClass public static void setup() throws Exception
    {

        ServiceRegistry serviceRegistry = (ServiceRegistry) APP_CONTEXT_INIT.getApplicationContext().getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        cannedQueryDAO = (CannedQueryDAO) APP_CONTEXT_INIT.getApplicationContext().getBean("cannedQueryDAO");
        cannedQueryDAOForTesting = (CannedQueryDAO) APP_CONTEXT_INIT.getApplicationContext().getBean("cannedQueryDAOForTesting");
        mimetypeDAO = (MimetypeDAO) APP_CONTEXT_INIT.getApplicationContext().getBean("mimetypeDAO");

        RetryingTransactionCallback<String> createMimetypeCallback = new RetryingTransactionCallback<String>()
        {
            @Override
            public String execute() throws Throwable
            {
                String mimetypePrefix = GUID.generate();
                mimetypeDAO.getOrCreateMimetype(mimetypePrefix + "-aaa");
                mimetypeDAO.getOrCreateMimetype(mimetypePrefix + "-bbb");
                return mimetypePrefix;
            }
        };
        mimetypePrefix = txnHelper.doInTransaction(createMimetypeCallback);
    }

    /**
     * Helper parameter class for testing
     * @author Derek Hulley
     */
    public static class TestOneParams
    {
        private final String mimetypeMatch;
        private final boolean exact;
        private boolean forceFail;          // Trigger a SQL exception
        public TestOneParams(String mimetypeMatch, boolean exact)
        {
            this.mimetypeMatch = mimetypeMatch;
            this.exact = exact;
            this.forceFail = false;
        }
        @Override
        public String toString()
        {
            return "TestOneParams [mimetypeMatch=" + mimetypeMatch + ", exact=" + exact + "]";
        }
        public String getMimetypeMatch()
        {
            return mimetypeMatch;
        }
        public boolean isExact()
        {
            return exact;
        }
        public boolean isForceFail()
        {
            return forceFail;
        }
        public void setForceFail(boolean forceFail)
        {
            this.forceFail = forceFail;
        }
    }

    /**
     * Force a failure and ensure that the connection is not tarnished
     */
    @Test public void testExecute_FailureRecovery() throws Throwable
    {
        RetryingTransactionCallback<Void> failCallback = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                TestOneParams params = new TestOneParams(null, true);
                params.setForceFail(true);
                try
                {
                	cannedQueryDAOForTesting.executeCountQuery(QUERY_NS, QUERY_SELECT_MIMETYPE_COUNT, params);
                    fail("Expected bad SQL");
                }
                catch (Throwable e)
                {
                    // Expected
                }
                // Now attempt to write to the connection
                mimetypeDAO.getOrCreateMimetype(mimetypePrefix + "_postfail");
                return null;
            }
        };
        txnHelper.doInTransaction(failCallback, false);
    }

    @Test public void testExecute_CountAllMimetypes() throws Throwable
    {
        RetryingTransactionCallback<Long> selectCallback = new RetryingTransactionCallback<Long>()
        {
            @Override
            public Long execute() throws Throwable
            {
                TestOneParams params = new TestOneParams(null, true);
                return cannedQueryDAOForTesting.executeCountQuery(QUERY_NS, QUERY_SELECT_MIMETYPE_COUNT, params);
            }
        };
        Long count = txnHelper.doInTransaction(selectCallback, true);
        assertNotNull(count);
        assertTrue(count.longValue() > 0L);
    }

    /**
     * Ensures that no results returns 0 since SQL will return a <tt>null</tt> count.
     */
    @Test public void testExecute_CountNoResults() throws Throwable
    {
        RetryingTransactionCallback<Long> selectCallback = new RetryingTransactionCallback<Long>()
        {
            @Override
            public Long execute() throws Throwable
            {
                TestOneParams params = new TestOneParams(GUID.generate(), true);
                return cannedQueryDAOForTesting.executeCountQuery(QUERY_NS, QUERY_SELECT_MIMETYPE_COUNT, params);
            }
        };
        Long count = txnHelper.doInTransaction(selectCallback, true);
        assertNotNull(count);
        assertEquals("Incorrect result count.", 0L, count.longValue());
    }

    @Test public void testExecute_CountMimetypeExact() throws Throwable
    {
        RetryingTransactionCallback<Long> selectCallback = new RetryingTransactionCallback<Long>()
        {
            @Override
            public Long execute() throws Throwable
            {
                TestOneParams params = new TestOneParams(mimetypePrefix + "-aaa", true);
                return cannedQueryDAOForTesting.executeCountQuery(QUERY_NS, QUERY_SELECT_MIMETYPE_COUNT, params);
            }
        };
        Long count = txnHelper.doInTransaction(selectCallback, true);
        assertNotNull(count);
        assertEquals("Incorrect result count.", 1L, count.longValue());
    }

    @Test public void testExecute_CountMimetypeWildcard() throws Throwable
    {
        RetryingTransactionCallback<Long> selectCallback = new RetryingTransactionCallback<Long>()
        {
            @Override
            public Long execute() throws Throwable
            {
                // Need to make sure this does not match the one created in testExecute_FailureRecovery()
                TestOneParams params = new TestOneParams(mimetypePrefix + "-%", false);
                return cannedQueryDAOForTesting.executeCountQuery(QUERY_NS, QUERY_SELECT_MIMETYPE_COUNT, params);
            }
        };
        Long count = txnHelper.doInTransaction(selectCallback, true);
        assertNotNull(count);
        //Two values -aaa, -bbb
        assertEquals("Incorrect result count.", 2L, count.longValue());
    }
    
    @Test public void testExecute_BadBounds() throws Throwable
    {
        try
        {
        	cannedQueryDAOForTesting.executeQuery(QUERY_NS, QUERY_SELECT_MIMETYPES, null, -1, 10);
            fail("Illegal parameter not detected");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
        try
        {
        	cannedQueryDAOForTesting.executeQuery(QUERY_NS, QUERY_SELECT_MIMETYPES, null, 0, -1);
            fail("Illegal parameter not detected");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
// TODO MyBatis workaround - temporarily support unlimited for nested result maps (see also below)
//        try
//        {
//            cannedQueryDAO.executeQuery(QUERY_NS, QUERY_SELECT_MIMETYPES, null, 0, Integer.MAX_VALUE);
//            fail("Illegal parameter not detected");
//        }
//        catch (IllegalArgumentException e)
//        {
//            // Expected
//        }
    }
    
    @Test public void testExecute_ListMimetypes() throws Throwable
    {
        RetryingTransactionCallback<List<String>> selectCallback = new RetryingTransactionCallback<List<String>>()
        {
            @Override
            public List<String> execute() throws Throwable
            {
                TestOneParams params = new TestOneParams(null, false);
                return cannedQueryDAOForTesting.executeQuery(QUERY_NS, QUERY_SELECT_MIMETYPES, params, 0, 2);
            }
        };
        List<String> mimetypes = txnHelper.doInTransaction(selectCallback, true);
        assertNotNull(mimetypes);
        assertTrue("Too many results", mimetypes.size() <= 2);
    }
    
    @Test public void testExecute_ResultHandlerWithError() throws Throwable
    {
        final ResultHandler<String> resultHandler = new ResultHandler<String>()
        {
            @Override
            public boolean handleResult(String result)
            {
                throw new UnsupportedOperationException();
            }
        };
        
        RetryingTransactionCallback<List<String>> selectCallback = new RetryingTransactionCallback<List<String>>()
        {
            @Override
            public List<String> execute() throws Throwable
            {
                TestOneParams params = new TestOneParams(null, false);
                try
                {
                	cannedQueryDAOForTesting.executeQuery(QUERY_NS, QUERY_SELECT_MIMETYPES, params, 0, 2, resultHandler);
                    fail("Expected UnsupportedOperationException");
                }
                catch (Exception e)
                {
                    // Expected, but make sure that our exception is the cause
                    Throwable ee = ExceptionStackUtil.getCause(e, UnsupportedOperationException.class);
                    if (ee == null)
                    {
                        throw e;
                    }
                }
                // Now query again with success
                return cannedQueryDAOForTesting.executeQuery(QUERY_NS, QUERY_SELECT_MIMETYPES, params, 0, 2);
            }
        };
        List<String> mimetypes = txnHelper.doInTransaction(selectCallback, true);
        assertNotNull(mimetypes);
        assertTrue("Too many results", mimetypes.size() <= 2);
    }
    
    @Test public void testExecute_ResultHandlerWithEarlyTermination() throws Throwable
    {
        final List<String> results = new ArrayList<String>();
        final ResultHandler<String> resultHandler = new ResultHandler<String>()
        {
            @Override
            public boolean handleResult(String result)
            {
                // Only one result then stop
                results.add(result);
                return false;
            }
        };
        
        RetryingTransactionCallback<Void> selectCallback = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                TestOneParams params = new TestOneParams(null, false);
                cannedQueryDAOForTesting.executeQuery(QUERY_NS, QUERY_SELECT_MIMETYPES, params, 0, 2, resultHandler);
                return null;
            }
        };
        txnHelper.doInTransaction(selectCallback, true);
        assertEquals("ResultHandler did not terminate early", 1, results.size());
    }
}
