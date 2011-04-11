/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import junit.framework.TestCase;

import org.alfresco.repo.domain.mimetype.MimetypeDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.springframework.context.ApplicationContext;

/**
 * @see CannedQueryDAO
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class CannedQueryDAOTest extends TestCase
{
    private static final String QUERY_NS = "alfresco.query.test";
    private static final String QUERY_SELECT_MIMETYPE_COUNT = "select_CountMimetypes";
    
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private CannedQueryDAO cannedQueryDAO;
    private MimetypeDAO mimetypeDAO;
    
    private String mimetypePrefix;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        cannedQueryDAO = (CannedQueryDAO) ctx.getBean("cannedQueryDAO");
        mimetypeDAO = (MimetypeDAO) ctx.getBean("mimetypeDAO");

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
    public void testExecute_FailureRecovery() throws Throwable
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
                    cannedQueryDAO.executeCountQuery(QUERY_NS, QUERY_SELECT_MIMETYPE_COUNT, params);
                    fail("Expected bad SQL");
                }
                catch (Throwable e)
                {
                    // Expected
                }
                // Now attempt to write to the connection
                mimetypeDAO.getOrCreateMimetype(mimetypePrefix + "-postfail");
                return null;
            }
        };
        txnHelper.doInTransaction(failCallback, false);
    }

    public void testExecute_CountAllMimetypes() throws Throwable
    {
        RetryingTransactionCallback<Long> selectCallback = new RetryingTransactionCallback<Long>()
        {
            @Override
            public Long execute() throws Throwable
            {
                TestOneParams params = new TestOneParams(null, true);
                return cannedQueryDAO.executeCountQuery(QUERY_NS, QUERY_SELECT_MIMETYPE_COUNT, params);
            }
        };
        Long count = txnHelper.doInTransaction(selectCallback, true);
        assertNotNull(count);
        assertTrue(count.longValue() > 0L);
    }

    /**
     * Ensures that no results returns 0 since SQL will return a <tt>null</tt> count.
     */
    public void testExecute_CountNoResults() throws Throwable
    {
        RetryingTransactionCallback<Long> selectCallback = new RetryingTransactionCallback<Long>()
        {
            @Override
            public Long execute() throws Throwable
            {
                TestOneParams params = new TestOneParams(GUID.generate(), true);
                return cannedQueryDAO.executeCountQuery(QUERY_NS, QUERY_SELECT_MIMETYPE_COUNT, params);
            }
        };
        Long count = txnHelper.doInTransaction(selectCallback, true);
        assertNotNull(count);
        assertEquals("Incorrect result count.", 0L, count.longValue());
    }

    public void testExecute_CountMimetypeExact() throws Throwable
    {
        RetryingTransactionCallback<Long> selectCallback = new RetryingTransactionCallback<Long>()
        {
            @Override
            public Long execute() throws Throwable
            {
                TestOneParams params = new TestOneParams(mimetypePrefix + "-aaa", true);
                return cannedQueryDAO.executeCountQuery(QUERY_NS, QUERY_SELECT_MIMETYPE_COUNT, params);
            }
        };
        Long count = txnHelper.doInTransaction(selectCallback, true);
        assertNotNull(count);
        assertEquals("Incorrect result count.", 1L, count.longValue());
    }

    public void testExecute_CountMimetypeWildcard() throws Throwable
    {
        RetryingTransactionCallback<Long> selectCallback = new RetryingTransactionCallback<Long>()
        {
            @Override
            public Long execute() throws Throwable
            {
                TestOneParams params = new TestOneParams(mimetypePrefix + "%", false);
                return cannedQueryDAO.executeCountQuery(QUERY_NS, QUERY_SELECT_MIMETYPE_COUNT, params);
            }
        };
        Long count = txnHelper.doInTransaction(selectCallback, true);
        assertNotNull(count);
        assertEquals("Incorrect result count.", 2L, count.longValue());
    }
}
