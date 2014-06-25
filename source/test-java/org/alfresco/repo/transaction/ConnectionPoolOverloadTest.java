/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.transaction;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.transaction.UserTransaction;

import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.transaction.ConnectionPoolException;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.sun.star.auth.InvalidArgumentException;

/**
 * A test designed to catch ConnectionPoolException
 * 
 * @author alex.mukha
 * @since 4.1.9
 */

public class ConnectionPoolOverloadTest
{
    private static Log logger = LogFactory.getLog(ConnectionPoolOverloadTest.class);
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private static MutableInt failCount;

    private TransactionService transactionService;
    private Properties properties;

    private int dbPoolMax;
    private int dbPoolWaitMax;

    @Before
    public void setUp() throws Exception
    {
        failCount = new MutableInt(0);
        transactionService = ctx.getBean("transactionComponent", TransactionService.class);
        properties = ctx.getBean("global-properties", Properties.class);

        String dbPoolMaxProp = properties.getProperty("db.pool.max");
        if (PropertyCheck.isValidPropertyString(dbPoolMaxProp))
        {
            dbPoolMax = Integer.parseInt(dbPoolMaxProp);
        }
        else
        {
            throw new InvalidArgumentException("The db.pool.max property is not valid.");
        }
        
        String dbPoolWaitMaxProp = properties.getProperty("db.pool.wait.max");
        if (PropertyCheck.isValidPropertyString(dbPoolWaitMaxProp))
        {
            dbPoolWaitMax = Integer.parseInt(dbPoolWaitMaxProp);
        }
        else
        {
            throw new InvalidArgumentException("The db.pool.wait.max property is not valid.");
        }
        
        dbPoolWaitMax = dbPoolWaitMax == -1 ? 100 : dbPoolWaitMax;
    }
    
    @Test
    @Ignore("The test will fail if db.pool.wait.max=-1 as all the threads will successfully open the transactions.")
    public void testOverload() throws Exception
    {
        List<Thread> threads = new LinkedList<Thread>();
        int numThreads = dbPoolMax + 1;
        int i = 0;
        try
        {
            for (i = 0; i < numThreads; i++)
            {
                Thread thread = new TxnThread("Thread-" + i);
                thread.start();
                threads.add(thread);
            }
        }
        finally
        {
            try
            {
                for (Thread thread : threads)
                {
                    if (thread != null)
                    {
                        try
                        {
                            thread.join(dbPoolWaitMax);
                        }
                        catch (Exception e)
                        {
                            fail("The " + thread.getName() + " failed to join.");
                        }
                    }
                }
            }
            finally
            {
                for (Thread thread : threads)
                {
                    if (thread != null)
                    {
                        thread.interrupt();
                    }
                }
            }
            assertTrue("The number of failed threads should not be 0.", failCount.intValue() > 0);
            assertTrue("The number of open transactions should not be more that the db pool maximum."
                    + "(Maybe a configuration of DB connection limit is less then db.pool.max)"
                    + " db.pool.max is " + dbPoolMax
                    + ", number of threads is " + numThreads
                    + ", number of failed threads is" + failCount.intValue(),
                    dbPoolMax >= numThreads - failCount.intValue());
        }
    }

    private class TxnThread extends Thread
    {
        private ThreadLocal<UserTransaction> txnTL = new ThreadLocal<UserTransaction>();

        public TxnThread(String name)
        {
            super(name);
            this.setDaemon(true);
        }

        @Override
        public void run()
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Start " + this.getName());
            }
            UserTransaction txn = transactionService.getUserTransaction();
            txnTL.set(txn);
            try
            {
                txn.begin();
            }
            catch (ConnectionPoolException e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("The " + this.getName() + " failed with ConnectionPoolException.");
                }
                failCount.increment();
                interrupt();
            }
            catch (Exception e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("The " + this.getName() + " failed with not expected exception.");
                }
                e.printStackTrace();
                failCount.increment();
                interrupt();
                fail("Thread should fail with ConnectionPoolException.");
            }
        }

        @Override
        public void interrupt()
        {
            if (txnTL.get() != null)
            {
                try
                {
                    txnTL.get().rollback();
                }
                catch (Exception e)
                {
                }
            }
            super.interrupt();
        }
    }
}
