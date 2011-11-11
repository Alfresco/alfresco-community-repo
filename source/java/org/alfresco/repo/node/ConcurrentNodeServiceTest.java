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
package org.alfresco.repo.node;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * @author Nick Burch
 * @author Derek Hulley
 */
public class ConcurrentNodeServiceTest extends TestCase
{
    public static final String NAMESPACE = "http://www.alfresco.org/test/BaseNodeServiceTest";
    public static final String TEST_PREFIX = "test";
    public static final QName TYPE_QNAME_TEST_CONTENT = QName.createQName(NAMESPACE, "content");
    public static final QName ASPECT_QNAME_TEST_TITLED = QName.createQName(NAMESPACE, "titled");
    public static final QName PROP_QNAME_TEST_TITLE = QName.createQName(NAMESPACE, "title");
    public static final QName PROP_QNAME_TEST_MIMETYPE = QName.createQName(NAMESPACE, "mimetype");

    public static final int COUNT = 10;
    public static final int REPEATS = 20;

    private static Log logger = LogFactory.getLog(ConcurrentNodeServiceTest.class);

    static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private NodeService nodeService;
    private TransactionService transactionService;
    private NodeRef rootNodeRef;
    private AuthenticationComponent authenticationComponent;

    public ConcurrentNodeServiceTest()
    {
        super();
    }

    protected void setUp() throws Exception
    {
        DictionaryDAO dictionaryDao = (DictionaryDAO) ctx.getBean("dictionaryDAO");
        // load the system model
        ClassLoader cl = BaseNodeServiceTest.class.getClassLoader();
        InputStream modelStream = cl.getResourceAsStream("alfresco/model/systemModel.xml");
        assertNotNull(modelStream);
        M2Model model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);
        // load the test model
        modelStream = cl.getResourceAsStream("org/alfresco/repo/node/BaseNodeServiceTest_model.xml");
        assertNotNull(modelStream);
        model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);

        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        nodeService = serviceRegistry.getNodeService();
        transactionService = serviceRegistry.getTransactionService();
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");

        this.authenticationComponent.setSystemUserAsCurrentUser();

        // create a first store directly
        RetryingTransactionCallback<Object> createRootNodeCallback =  new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_"
                        + System.currentTimeMillis());
                rootNodeRef = nodeService.getRootNode(storeRef);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(createRootNodeCallback);
    }

    @Override
    protected void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        super.tearDown();
    }
    
    /**
     * Tests that when multiple threads try to edit different
     *  properties on a node, that transactions + retries always
     *  mean that every change always ends up on the node. 
     *  
     * @since 3.4 
     */
    public void testMultiThreaded_PropertyWrites() throws Exception
    {
        final List<Thread> threads = new ArrayList<Thread>();
        final int loops = 1000;

        // Have 5 threads, each trying to edit their own properties on the same node
        // Loop repeatedly
        final QName[] properties = new QName[] {
                QName.createQName("test1", "MadeUp1"),
                QName.createQName("test2", "MadeUp2"),
                QName.createQName("test3", "MadeUp3"),
                QName.createQName("test4", "MadeUp4"),
                QName.createQName("test5", "MadeUp5"),
        };
        final int[] propCounts = new int[properties.length];
        for (int propNum = 0; propNum < properties.length; propNum++)
        {
            final QName property = properties[propNum];
            final int propNumFinal = propNum;

            // Zap the property if it is there
            transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        nodeService.removeProperty(rootNodeRef, property);
                        return null;
                    }
                }
            );

            // Prep the thread
            Thread t = new Thread(new Runnable()
            {
                @Override
                public synchronized void run()
                {
                    // Let everything catch up
                    try
                    {
                        wait();
                    }
                    catch (InterruptedException e)
                    {
                    }
                    logger.info("About to start updating property " + property);

                    // Loop, incrementing each time
                    // If we miss an update, then at the end it'll be obvious
                    AuthenticationUtil.setRunAsUserSystem();
                    for (int i = 0; i < loops; i++)
                    {
                        RetryingTransactionCallback<Integer> callback = new RetryingTransactionCallback<Integer>()
                        {
                            @Override
                            public Integer execute() throws Throwable
                            {
                                // Grab the current value
                                int current = 0;
                                Object obj = (Object) nodeService.getProperty(rootNodeRef, property);
                                if (obj != null && obj instanceof Integer)
                                {
                                    current = ((Integer) obj).intValue();
                                }
                                // Increment by one. Really should be this!
                                current++;
                                nodeService.setProperty(rootNodeRef, property, Integer.valueOf(current));
                                // Check that the value is what we expected it to be
                                // We do this after the update so that we don't fall on retries
                                int expectedCurrent = propCounts[propNumFinal];
                                if (expectedCurrent != (current - 1))
                                {
                                    // We have a difference here already
                                    // It will never catch up, but we'll detect that later
                                    System.out.println("Found difference: " + Thread.currentThread().getName() + " " + current);
                                }
                                return current;
                            }
                        };
                        try
                        {
                            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
                            txnHelper.setMaxRetries(loops);
                            Integer newCount = txnHelper.doInTransaction(callback, false, true);
//                            System.out.println("Set value: " + Thread.currentThread().getName() + " " + newCount);
                            propCounts[propNumFinal] = newCount;
                        }
                        catch (Throwable e)
                        {
                            logger.error("Failed to set value: ", e);
                        }
                    }

                    // Report us as finished
                    logger.info("Finished updating property " + property);
                }
            }, "Thread-" + property);
            threads.add(t);
            t.start();
        }

        // Release the threads
        logger.info("Releasing the property update threads");
        for (Thread t : threads)
        {
            t.interrupt();
        }

        // Wait for the threads to finish
        for (Thread t : threads)
        {
            t.join();
        }
        
        Map<QName, Serializable> nodeProperties = nodeService.getProperties(rootNodeRef);
        List<String> errors = new ArrayList<String>();
        for (int i =0; i < properties.length; i++)
        {
            Integer value = (Integer) nodeProperties.get(properties[i]);
            if (value == null)
            {
                errors.add("\n   Prop " + properties[i] + " : " + value);
            }
            if (!value.equals(new Integer(loops)))
            {
                errors.add("\n   Prop " + properties[i] + " : " + value);
            }
        }
        if (errors.size() > 0)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Incorrect counts recieved for " + loops + " loops.");
            for (String error : errors)
            {
                sb.append(error);
            }
            fail(sb.toString());
        }
    }
    
    /**
     * Adds 'residual' aspects that are named according to the thread.  Multiple threads should all
     * get their changes in. 
     */
    public void testMultithreaded_AspectWrites() throws Exception
    {
        final Thread[] threads = new Thread[2];
        final int loops = 10;
        
        for (int i = 0; i < threads.length; i++)
        {
            final String name = "Thread-" + i + "-";
            Runnable runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    AuthenticationUtil.setRunAsUserSystem();
                    for (int loop = 0; loop < loops; loop++)
                    {
                        final String nameWithLoop = name + loop;
                        RetryingTransactionCallback<Void> runCallback = new RetryingTransactionCallback<Void>()
                        {
                            @Override
                            public Void execute() throws Throwable
                            {
                                // Add another aspect to the node
                                QName qname = QName.createQName(NAMESPACE, nameWithLoop);
                                nodeService.addAspect(rootNodeRef, qname, null);
                                return null;
                            }
                        };
                        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
                        txnHelper.setMaxRetries(40);
                        try
                        {
                            txnHelper.doInTransaction(runCallback);
                        }
                        catch (Throwable e)
                        {
                            logger.error(e);
                        }
                    }
                }
            };
            threads[i] = new Thread(runnable, name);
        }
        // Start all the threads
        for (int i = 0; i < threads.length; i++)
        {
            threads[i].start();
        }
        // Wait for them all to finish
        for (int i = 0; i < threads.length; i++)
        {
            threads[i].join();
        }
        // Check the aspects
        Set<QName> aspects = nodeService.getAspects(rootNodeRef);
        for (int i = 0; i < threads.length; i++)
        {
            for (int j = 0; j < loops; j++)
            {
                String nameWithLoop = "Thread-" + i + "-" + j;
                QName qname = QName.createQName(NAMESPACE, nameWithLoop);
                assertTrue("Missing aspect: "+ nameWithLoop, aspects.contains(qname));
            }
        }
    }
}
