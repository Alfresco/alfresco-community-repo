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
package org.alfresco.repo.version.common.counter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @author Roy Wetherall
 */
public class VersionCounterServiceTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private StoreRef storeRef1;
    private StoreRef storeRef2;

    private TransactionService transactionService;
    private NodeService nodeService;
    private VersionCounterService counter;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        counter = (VersionCounterService) ctx.getBean("versionCounterService");
        
        // authenticate
        AuthenticationComponent auth = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        auth.setSystemUserAsCurrentUser();
        
        storeRef1 = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "test1_" + System.currentTimeMillis());
        storeRef2 = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "test2_" + System.currentTimeMillis());
    }
    
    public void testSetUp() throws Exception
    {
        assertNotNull(transactionService);
        assertNotNull(counter);
    }
    
    /**
     * Test nextVersionNumber
     */
    public void testNextVersionNumber() throws Exception
    {
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();
            
            int store1Version0 = counter.nextVersionNumber(storeRef1);
            assertEquals(1, store1Version0);
            
            int store1Version1 = counter.nextVersionNumber(storeRef1);
            assertEquals(2, store1Version1);
            
            int store2Version0 = counter.nextVersionNumber(storeRef2);
            assertEquals(1, store2Version0);
            
            int store1Version2 = counter.nextVersionNumber(storeRef1);
            assertEquals(3, store1Version2);
            
            int store2Version1 = counter.nextVersionNumber(storeRef2);
            assertEquals(2, store2Version1);
            
            int store1Current = counter.currentVersionNumber(storeRef1);
            assertEquals(3, store1Current);
            
            int store2Current = counter.currentVersionNumber(storeRef2);
            assertEquals(2, store2Current);
            
            // Need to clean-up since the version counter works in its own transaction
            counter.resetVersionNumber(storeRef1);
            counter.resetVersionNumber(storeRef2);
        }
        finally
        {
            try { txn.rollback(); } catch (Throwable e) {}
        }
    }

    public void testConcurrentVersionNumber() throws Throwable
    {
        counter.currentVersionNumber(storeRef1);
        VersionCounterThread[] threads = new VersionCounterThread[threadCount];
        for (int i = 0; i < threadCount; i++)
        {
            threads[i] = new VersionCounterThread("VersionCounterThread_" + i);
            // start the thread
            threads[i].start();
        }
        
        // wait for the threads to all be done (or 10 seconds has passed)
        endSignal.await(10, TimeUnit.SECONDS);
        
        // check for exceptions
        for (VersionCounterThread thread : threads)
        {
            if (thread.error != null)
            {
                throw thread.error;
            }
        }
    }

    private int threadCount = 5;
    private CountDownLatch startSignal = new CountDownLatch(threadCount);
    private CountDownLatch endSignal = new CountDownLatch(threadCount);
    
    private class VersionCounterThread extends Thread
    {
        private Throwable error = new RuntimeException("Execution didn't complete");
        public VersionCounterThread(String name)
        {
            super(name);
        }
        
        @Override
        public void run()
        {
            RetryingTransactionCallback<Object> versionWork = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    // wait for all other threads to enter into their transactions
                    startSignal.countDown();
                    
                    int startVersion = counter.currentVersionNumber(storeRef1);
                    // increment it
                    int incrementedVersion = counter.nextVersionNumber(storeRef1);
                    assertTrue("Version number was not incremented", incrementedVersion > startVersion);

                    return null;
                }
            };
            try
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(versionWork, false);
                error = null;
            }
            catch (Throwable e)
            {
                error = e;
                e.printStackTrace();
            }
            finally
            {
                endSignal.countDown();
            }
        }
    }
}
