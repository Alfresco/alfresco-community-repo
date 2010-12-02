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
package org.alfresco.repo.domain.locale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @see LocaleDAO
 * 
 * @author Derek Hulley, janv
 * @since 2.2.1, 3.3
 */
public class LocaleDAOTest extends TestCase
{
    private static Log logger = LogFactory.getLog(LocaleDAOTest.class);
    
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private LocaleDAO localeDAO;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        localeDAO = (LocaleDAO) ctx.getBean("localeDAO");
    }
    
    @Override
    public void tearDown() throws Exception
    {
        // NOOP
    }
    
    private Pair<Long, Locale> getLocale(final Locale locale, boolean expectSuccess)
    {
        RetryingTransactionCallback<Pair<Long, Locale>> callback = new RetryingTransactionCallback<Pair<Long, Locale>>()
        {
            public Pair<Long, Locale> execute() throws Throwable
            {
                Pair<Long, Locale> localePair = localeDAO.getOrCreateLocalePair(locale);
                return localePair;
            }
        };
        try
        {
            return txnHelper.doInTransaction(callback, false);
        }
        catch (Throwable e)
        {
            if (expectSuccess)
            {
                // oops
                throw new RuntimeException("Expected to get locale '" + locale + "'.", e);
            }
            else
            {
                return null;
            }
        }
    }
    
    
    public void testCreateLocale() throws Exception
    {
        // Create a locale
        Locale locale = new Locale(GUID.generate().substring(0, 20));
        Pair<Long, Locale> localePair = getLocale(locale, true);
        // Check that it can be retrieved
        Pair<Long, Locale> localePairCheck = getLocale(localePair.getSecond(), true);
        assertEquals("Locale ID changed", localePair.getFirst(), localePairCheck.getFirst());
        // Check the duplicate checking
        getLocale(locale, false);
    }
    
    public void testCreateLocaleEmpty() throws Exception
    {
        // Create a locale
        Locale locale = new Locale("");
        Pair<Long, Locale> localePair = getLocale(locale, true);
        // Check that it can be retrieved
        Pair<Long, Locale> localePairCheck = getLocale(localePair.getSecond(), true);
        assertEquals("Locale ID changed", localePair.getFirst(), localePairCheck.getFirst());
    }
    
    public void testDefaultLocale() throws Exception
    {
        RetryingTransactionCallback<Pair<Long, Locale>> callback = new RetryingTransactionCallback<Pair<Long, Locale>>()
        {
            public Pair<Long, Locale> execute() throws Throwable
            {
                // What is the thread's default locale?
                Locale defaultLocale = I18NUtil.getLocale();
                // Now make it
                Pair<Long, Locale> localePair = localeDAO.getOrCreateDefaultLocalePair();
                assertNotNull("Default locale should now exist", localePair);
                assertEquals(
                        "The default locale returned must match the current thread's default locale",
                        defaultLocale, localePair.getSecond());
                // Done
                return localePair;
            }
        };
        
        // Check that the default locale is handled properly
        txnHelper.doInTransaction(callback);
        
        // Now change the default locale
        I18NUtil.setLocale(Locale.CANADA_FRENCH);
        // Repeat
        txnHelper.doInTransaction(callback);
    }
    
    /**
     * Forces a bunch of threads to attempt Locale creation.
     */
    public void testConcurrentLocale() throws Throwable
    {
        final Locale locale = Locale.SIMPLIFIED_CHINESE;
        
        int threadCount = 50;
        final CountDownLatch readyLatch = new CountDownLatch(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(threadCount);
        final List<Throwable> errors = Collections.synchronizedList(new ArrayList<Throwable>(0));
        final RetryingTransactionCallback<Long> callback = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                String threadName = Thread.currentThread().getName();
                
                // Notify that we are ready
                logger.debug("Thread " + threadName + " is READY");
                readyLatch.countDown();
                // Wait for start signal
                startLatch.await();
                logger.debug("Thread " + threadName + " is GO");
                // Go
                Pair<Long, Locale> localePair = null;
                try
                {
                    // This could fail with concurrency, but that's what we're testing
                    logger.debug("Thread " + threadName + " is CREATING " + locale);
                    localePair = localeDAO.getOrCreateLocalePair(locale);
                }
                catch (Throwable e)
                {
                    logger.debug("Failed to create LocaleEntity.  Might retry.", e);
                    throw e;
                }
                // Notify the counter that this thread is done
                logger.debug("Thread " + threadName + " is DONE");
                doneLatch.countDown();
                // Done
                return localePair.getFirst();
            }
        };
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                try
                {
                    txnHelper.doInTransaction(callback);
                }
                catch (Throwable e)
                {
                    logger.error("Error escaped from retry", e);
                    errors.add(e);
                }
            }
        };
        // Fire a bunch of threads off
        for (int i = 0; i < threadCount; i++)
        {
            Thread thread = new Thread(runnable, getName() + "-" + i);
            thread.setDaemon(true);     // Just in case there are complications
            thread.start();
        }
        // Wait for threads to be ready
        readyLatch.await(5, TimeUnit.SECONDS);
        // Let the threads go
        startLatch.countDown();
        // Wait for them all to be done (within limit of 10 seconds per thread)
        if (doneLatch.await(threadCount * 10, TimeUnit.SECONDS))
        {
            logger.warn("Still waiting for threads to finish after " + threadCount + " seconds.");
        }
        // Check if there are errors
        if (errors.size() > 0)
        {
            throw errors.get(0);
        }
    }
}
