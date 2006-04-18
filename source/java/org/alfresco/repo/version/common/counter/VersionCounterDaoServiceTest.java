/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.version.common.counter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @author Roy Wetherall
 */
public class VersionCounterDaoServiceTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private StoreRef storeRef1;
    private StoreRef storeRef2;

    private TransactionService transactionService;
    private NodeService nodeService;
    private VersionCounterDaoService counter;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        counter = (VersionCounterDaoService) ctx.getBean("versionCounterDaoService");
        
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
            TransactionWork<Object> versionWork = new TransactionWork<Object>()
            {
                public Object doWork() throws Exception
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
                TransactionUtil.executeInNonPropagatingUserTransaction(transactionService, versionWork, false);
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
