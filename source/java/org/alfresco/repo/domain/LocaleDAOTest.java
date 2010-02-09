/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.extensions.surf.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * @see LocaleDAO
 * 
 * 
 * @author Derek Hulley
 * @since 2.2.1
 */
public class LocaleDAOTest extends TestCase
{
    private static Log logger = LogFactory.getLog(LocaleDAOTest.class);
    
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private RetryingTransactionHelper retryingTransactionHelper;
    private LocaleDAO dao;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        retryingTransactionHelper = serviceRegistry.getTransactionService().getRetryingTransactionHelper();
        dao = (LocaleDAO) ctx.getBean("localeDAO");
    }
    
    @Override
    public void tearDown() throws Exception
    {
        
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
                Pair<Long, Locale> localePair = dao.getOrCreateDefaultLocalePair();
                assertNotNull("Default locale should now exist", localePair);
                assertEquals(
                        "The default locale returned must match the current thread's default locale",
                        defaultLocale, localePair.getSecond());
                // Done
                return localePair;
            }
        };
        
        // Check that the default locale is handled properly
        retryingTransactionHelper.doInTransaction(callback);
        
        // Now change the default locale
        I18NUtil.setLocale(Locale.CANADA_FRENCH);
        // Repeat
        retryingTransactionHelper.doInTransaction(callback);
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
                    localePair = dao.getOrCreateLocalePair(locale);
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
                    retryingTransactionHelper.doInTransaction(callback);
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
