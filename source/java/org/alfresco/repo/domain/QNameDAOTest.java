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
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.springframework.extensions.surf.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * @see QNameDAO
 * 
 * 
 * @author Derek Hulley
 * @since 2.2
 */
public class QNameDAOTest extends TestCase
{
    private static Log logger = LogFactory.getLog(QNameDAOTest.class);
    
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private RetryingTransactionHelper retryingTransactionHelper;
    private QNameDAO dao;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        retryingTransactionHelper = serviceRegistry.getTransactionService().getRetryingTransactionHelper();
        dao = (QNameDAO) ctx.getBean("qnameDAO");
    }
    
    @Override
    public void tearDown() throws Exception
    {
        
    }
    
    public void testNewNamespace() throws Exception
    {
        final String namespaceUri = GUID.generate();
        RetryingTransactionCallback<Pair<Long, String>> callback = new RetryingTransactionCallback<Pair<Long, String>>()
        {
            public Pair<Long, String> execute() throws Throwable
            {
                Pair<Long, String> namespacePair = dao.getNamespace(namespaceUri);
                assertNull("Namespace should not exist yet", namespacePair);
                // Now make it
                namespacePair = dao.newNamespace(namespaceUri);
                assertNotNull("Namespace should now exist", dao.getNamespace(namespaceUri));
                // Done
                return namespacePair;
            }
        };
        retryingTransactionHelper.doInTransaction(callback);
    }
    
    public void testRenameNamespace() throws Exception
    {
        final String namespaceUriBefore = GUID.generate();
        final QName qnameBefore = QName.createQName(namespaceUriBefore, "before");
        final String namespaceUriAfter = GUID.generate();
        final QName qnameAfter = QName.createQName(namespaceUriAfter, "before");
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                dao.getOrCreateNamespace(namespaceUriBefore);
                // Get a QName that has the URI
                Long qnameIdBefore = dao.getOrCreateQName(qnameBefore).getFirst();
                // Now modify the namespace
                dao.updateNamespace(namespaceUriBefore, namespaceUriAfter);
                // The old qname must be gone
                assertNull("QName must be gone as the URI was renamed", dao.getQName(qnameBefore));
                // The new QName must be present and with the same ID
                Pair<Long, QName> qnamePairAfter = dao.getQName(qnameAfter);
                assertNotNull("Expected QName with new URI to exist.", qnamePairAfter);
                assertEquals("QName changed ID unexpectedly.", qnameIdBefore, qnamePairAfter.getFirst());
                // Done
                return null;
            }
        };
        retryingTransactionHelper.doInTransaction(callback);
    }
    
    public void testNewQName() throws Exception
    {
        final String namespaceUri = GUID.generate();
        final String localName = GUID.generate();
        final QName qname = QName.createQName(namespaceUri, localName);
        RetryingTransactionCallback<Pair<Long, QName>> callback = new RetryingTransactionCallback<Pair<Long, QName>>()
        {
            public Pair<Long, QName> execute() throws Throwable
            {
                Pair<Long, QName> qnamePair = dao.getQName(qname);
                assertNull("QName should not exist yet", qnamePair);
                // Now make it
                qnamePair = dao.newQName(qname);
                assertNotNull("QName should now exist", dao.getQName(qname));
                // Done
                return qnamePair;
            }
        };
        retryingTransactionHelper.doInTransaction(callback);
    }
    
    public void testGetQNameManyTimes() throws Exception
    {
        final String namespaceUri = GUID.generate();
        final String localName = GUID.generate();
        final QName qname = QName.createQName(namespaceUri, localName);
        RetryingTransactionCallback<Pair<Long, QName>> callback = new RetryingTransactionCallback<Pair<Long, QName>>()
        {
            public Pair<Long, QName> execute() throws Throwable
            {
                Pair<Long, QName> qnamePair = dao.getQName(qname);
                assertNull("QName should not exist yet", qnamePair);
                // Now make it
                qnamePair = dao.newQName(qname);
                assertNotNull("QName should now exist", dao.getQName(qname));
                // Done
                return qnamePair;
            }
        };
        retryingTransactionHelper.doInTransaction(callback);
        callback = new RetryingTransactionCallback<Pair<Long, QName>>()
        {
            public Pair<Long, QName> execute() throws Throwable
            {
                for (int i = 0; i < 1000; i++)
                {
                    dao.getQName(qname);
                }
                return null;
            }
        };
        retryingTransactionHelper.doInTransaction(callback);
    }
    
    /**
     * Sinces the unique indexes are case-sensitive, we have to ensure that the DAO handles this accordingly.
     */
    public void testNamespaceCaseInsensitivity() throws Throwable
    {
        final String guidNs = GUID.generate();
        final String namespaceUriLower = "aaa-" + guidNs;
        final String namespaceUriUpper = "AAA-" + guidNs;
        final QName namespaceUriLowerQName = QName.createQName(namespaceUriLower, "blah");
        final QName namespaceUriUpperQName = QName.createQName(namespaceUriUpper, "blah");
        final String localName = GUID.generate();
        final String localNameLower = "aaa-" + localName;
        final String localNameUpper = "AAA-" + localName;
        final QName localNameLowerQName = QName.createQName("blah", localNameLower);
        final QName localNameUpperQName = QName.createQName("blah", localNameUpper);
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // Create QNames with lowercase values
                dao.getOrCreateQName(namespaceUriLowerQName);
                dao.getOrCreateQName(localNameLowerQName);
                // Done
                return null;
            }
        };
        retryingTransactionHelper.doInTransaction(callback);
        RetryingTransactionCallback<Object> callback2 = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // Check namespace case-insensitivity
                Pair<Long, QName> namespaceUriLowerQNamePair = dao.getQName(namespaceUriLowerQName);
                assertNotNull(namespaceUriLowerQNamePair);
                Pair<Long, QName> namespaceUriUpperQNamePair = dao.getOrCreateQName(namespaceUriUpperQName);
                assertNotNull(namespaceUriUpperQNamePair);
                assertEquals(
                        "Didn't resolve case-insensitively on namespace",
                        namespaceUriLowerQNamePair.getFirst(), namespaceUriUpperQNamePair.getFirst());
                // Check localname case-insensitivity
                Pair<Long, QName> localNameLowerQNamePair = dao.getQName(localNameLowerQName);
                assertNotNull(localNameLowerQNamePair);
                Pair<Long, QName> localNameUpperQNamePair = dao.getOrCreateQName(localNameUpperQName);
                assertNotNull(localNameUpperQNamePair);
                assertEquals(
                        "Didn't resolve case-insensitively on local-name",
                        localNameLowerQNamePair.getFirst(), localNameUpperQNamePair.getFirst());
                // Done
                return null;
            }
        };
        retryingTransactionHelper.doInTransaction(callback2);
    }
    
    /**
     * Forces a bunch of threads to attempt QName creation at exactly the same time
     * for their first attempt.  The subsequent retries should all succeed by
     * finding the QNameEntity.
     */
    public void testConcurrentQName() throws Throwable
    {
        final Random random = new Random();
        final String namespaceUri = GUID.generate();
        int threadCount = 50;
        final CountDownLatch readyLatch = new CountDownLatch(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(threadCount);
        final List<Throwable> errors = Collections.synchronizedList(new ArrayList<Throwable>(0));
        final RetryingTransactionCallback<Pair<Long, QName>> callback = new RetryingTransactionCallback<Pair<Long, QName>>()
        {
            public Pair<Long, QName> execute() throws Throwable
            {
                String threadName = Thread.currentThread().getName();
                // We use a common namespace and assign one of a limited set of random numbers
                // as the localname.  The transaction waits a bit before going for the commit
                // so as to ensure a good few others are trying the same thing.
                String localName = "" + random.nextInt(10);
                QName qname = QName.createQName(namespaceUri, localName);
                Pair<Long, QName> qnamePair = dao.getQName(qname);
                if (qnamePair == null)
                {
                    // Notify that we are ready
                    logger.debug("Thread " + threadName + " is READY");
                    readyLatch.countDown();
                    // Wait for start signal
                    startLatch.await();
                    logger.debug("Thread " + threadName + " is GO");
                    // Go
                    try
                    {
                        // This could fail with concurrency, but that's what we're testing
                        logger.debug("Thread " + threadName + " is CREATING " + qname);
                        qnamePair = dao.newQName(qname);
                    }
                    catch (Throwable e)
                    {
                        logger.debug("Failed to create QNameEntity.  Might retry.", e);
                        throw e;
                    }
                }
                else
                {
                    // In the case where the threads have to wait for database connections,
                    // it is quite possible that the entity was created as the ready latch
                    // is released after five seconds
                }
                assertNotNull("QName should now exist", qnamePair);
                // Notify the counter that this thread is done
                logger.debug("Thread " + threadName + " is DONE");
                doneLatch.countDown();
                // Done
                return qnamePair;
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
