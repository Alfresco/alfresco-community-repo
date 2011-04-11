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
package org.alfresco.repo.security.authority;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Checks that the duplicate child handling is done correctly.
 * 
 * @see org.alfresco.repo.model.filefolder.FileFolderServiceImpl
 * @author Derek Hulley
 * @since 2.1.0
 */
public class DuplicateAuthorityTest extends TestCase
{
    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private AuthenticationComponent authenticationComponent;

    private TransactionService transactionService;

    private RetryingTransactionHelper retryingTransactionHelper;

    private NodeService nodeService;

    private FileFolderService fileFolderService;

    private NodeRef rootNodeRef;

    private NodeRef workingRootNodeRef;

    private AuthorityService authorityService;

    private PersonService personService;

    @Override
    public void setUp() throws Exception
    {
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            throw new AlfrescoRuntimeException(
                    "A previous tests did not clean up transaction: " +
                    AlfrescoTransactionSupport.getTransactionId());
        }
        
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        transactionService = serviceRegistry.getTransactionService();
        retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
        retryingTransactionHelper.setMaxRetryWaitMs(10);
        nodeService = serviceRegistry.getNodeService();
        fileFolderService = serviceRegistry.getFileFolderService();
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        authorityService = (AuthorityService) ctx.getBean("authorityService");
        personService = (PersonService) ctx.getBean("personService");

        RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // authenticate
                authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

                // create a test store
                StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, getName() + System.currentTimeMillis());
                rootNodeRef = nodeService.getRootNode(storeRef);

                // create a folder to import into
                NodeRef nodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName(NamespaceService.ALFRESCO_URI, "working root"),
                        ContentModel.TYPE_FOLDER).getChildRef();
                // Done
                return nodeRef;
            }
        };
        workingRootNodeRef = retryingTransactionHelper.doInTransaction(callback, false, true);
    }

    public void tearDown() throws Exception
    {
        RetryingTransactionCallback<Void> callback1 = new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                for (int i = 0; i <= 10+1; i++)
                {
                    if (authorityService.authorityExists("GROUP_" + i))
                    {
                        authorityService.deleteAuthority("GROUP_" + i);
                    }
                }
                return null;
            }
        };
        retryingTransactionHelper.doInTransaction(callback1, false, true);

    }

    public void testDuplicateGroupDetection() throws Exception
    {
        // disable for now
        if(true)
        {
            return;
        }
        final int threadCount = 10;
        RetryingTransactionCallback<Void> callback1 = new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                for (int i = 0; i <= threadCount+1; i++)
                {
                    if (authorityService.authorityExists("GROUP_" + i))
                    {
                        authorityService.deleteAuthority("GROUP_" + i);
                    }
                }
                return null;
            }
        };
        retryingTransactionHelper.doInTransaction(callback1, false, true);

        // First create a file name F1
        RetryingTransactionCallback<String> callback = new CreateAuthorityCallback(0);
        String result = retryingTransactionHelper.doInTransaction(callback, false, true);
        // Check that the filename is F0
        assertEquals("GROUP_0", result);

        // Now create a whole lot of threads that attempt file creation

        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AuthThread[] workers = new AuthThread[threadCount];
        for (int i = 0; i < threadCount; i++)
        {
            workers[i] = new AuthThread(endLatch);
            workers[i].start();
        }
        // Wait at the end gate
        endLatch.await(300L, TimeUnit.SECONDS);

        // Analyse
        int failureCount = 0;
        int didNotCompleteCount = 0;
        for (int i = 0; i < threadCount; i++)
        {
            if (workers[i].error != null)
            {
                failureCount++;
            }
            else if (workers[i].success == null)
            {
                didNotCompleteCount++;
            }
        }
        System.out.println("" + failureCount + " of the " + threadCount + " threads failed and " + didNotCompleteCount + " did not finish.");
        assertEquals("Some failures", 0, failureCount);
        assertEquals("Some non-finishes", 0, didNotCompleteCount);

        retryingTransactionHelper.doInTransaction(callback1, false, true);
    }

    public void testDuplicatePersonDetection() throws Exception
    {
        // disable for now
        if(true)
        {
            return;
        }
        final int threadCount = 10;
        RetryingTransactionCallback<Void> callback1 = new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                for (int i = 0; i <= threadCount+1; i++)
                {
                    if (personService.personExists("Person_" + i))
                    {
                        personService.deletePerson("Person_" + i);
                    }
                }
                return null;
            }
        };
        //retryingTransactionHelper.doInTransaction(callback1, false, true);

        // First create a file name F1
        RetryingTransactionCallback<String> callback = new CreatePersonCallback(0);
        String result = retryingTransactionHelper.doInTransaction(callback, false, true);
        // Check that the filename is F0
        assertEquals("Person_0", result);

        // Now create a whole lot of threads that attempt file creation

        CountDownLatch endLatch = new CountDownLatch(threadCount);
        PersonThread[] workers = new PersonThread[threadCount];
        for (int i = 0; i < threadCount; i++)
        {
            workers[i] = new PersonThread(endLatch);
            workers[i].start();
        }
        // Wait at the end gate
        endLatch.await(300L, TimeUnit.SECONDS);

        // Analyse
        int failureCount = 0;
        int didNotCompleteCount = 0;
        for (int i = 0; i < threadCount; i++)
        {
            if (workers[i].error != null)
            {
                failureCount++;
            }
            else if (workers[i].success == null)
            {
                didNotCompleteCount++;
            }
        }
        System.out.println("" + failureCount + " of the " + threadCount + " threads failed and " + didNotCompleteCount + " did not finish.");
        assertEquals("Some failures", 0, failureCount);
        assertEquals("Some non-finishes", 0, didNotCompleteCount);

        retryingTransactionHelper.doInTransaction(callback1, false, true);
    }

    /**
     * Attempts to create a file "Fn" where n is the number supplied to the constructor.
     */
    private class CreateAuthorityCallback implements RetryingTransactionCallback<String>
    {
        private final int number;

        public CreateAuthorityCallback(int number)
        {
            this.number = number;
        }

        public String execute() throws Throwable
        {
            authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
            return authorityService.createAuthority(AuthorityType.GROUP, "" + number);
        }
    }

    private class CreatePersonCallback implements RetryingTransactionCallback<String>
    {
        private final int number;

        public CreatePersonCallback(int number)
        {
            this.number = number;
        }

        public String execute() throws Throwable
        {
            authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

            HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
            properties.put(ContentModel.PROP_USERNAME, "Person_" + number);
            properties.put(ContentModel.PROP_HOMEFOLDER, rootNodeRef);
            properties.put(ContentModel.PROP_FIRSTNAME, "Person_" + number);
            properties.put(ContentModel.PROP_LASTNAME, "Person_" + number);
            properties.put(ContentModel.PROP_EMAIL, "Person_" + number);
            properties.put(ContentModel.PROP_ORGID, "Person_" + number);

            personService.createPerson(properties);
            return "Person_" + number;
        }
    }

    private static ThreadGroup threadGroup = new ThreadGroup("DuplicateAuthorityTest");

    private static int threadNumber = -1;

    private class AuthThread extends Thread
    {
        private CountDownLatch endLatch;

        private Throwable error;

        private String success;

        public AuthThread(CountDownLatch endLatch)
        {
            super(threadGroup, "Worker " + ++threadNumber);
            this.endLatch = endLatch;
        }

        public void run()
        {
            String result = null;
            // Start the count with a guaranteed failure
            int number = 0;
            while (true)
            {
                RetryingTransactionCallback<String> callback = new CreateAuthorityCallback(number);
                try
                {
                    System.out.println("Thread " + getName() + " attempting file: " + number);
                    System.out.flush();

                    result = retryingTransactionHelper.doInTransaction(callback, false, true);
                    // It worked
                    success = result;
                    break;
                }
                catch (DataIntegrityViolationException e)
                {
                    // Try another number
                    number++;
                }
                catch (Throwable e)
                {
                    // Oops
                    error = e;
                    break;
                }
            }
            // Done
            if (error != null)
            {
                System.err.println("Thread " + getName() + " failed to create file " + number + ":");
                System.err.flush();
                error.printStackTrace();
            }
            else
            {
                System.out.println("\t\t\tThread " + getName() + " created auth: " + success);
                System.out.flush();
            }
            // Tick the latch
            endLatch.countDown();
        }
    }
    
    private class PersonThread extends Thread
    {
        private CountDownLatch endLatch;

        private Throwable error;

        private String success;

        public PersonThread(CountDownLatch endLatch)
        {
            super(threadGroup, "Worker " + ++threadNumber);
            this.endLatch = endLatch;
        }

        public void run()
        {
            String result = null;
            // Start the count with a guaranteed failure
            int number = 0;
            while (true)
            {
                RetryingTransactionCallback<String> callback = new CreatePersonCallback(number);
                try
                {
                    System.out.println("Thread " + getName() + " attempting file: " + number);
                    System.out.flush();

                    result = retryingTransactionHelper.doInTransaction(callback, false, true);
                    // It worked
                    success = result;
                    break;
                }
                catch (DataIntegrityViolationException e)
                {
                    // Try another number
                    number++;
                }
                catch (Throwable e)
                {
                    // Oops
                    error = e;
                    break;
                }
            }
            // Done
            if (error != null)
            {
                System.err.println("Thread " + getName() + " failed to create file " + number + ":");
                System.err.flush();
                error.printStackTrace();
            }
            else
            {
                System.out.println("\t\t\tThread " + getName() + " created auth: " + success);
                System.out.flush();
            }
            // Tick the latch
            endLatch.countDown();
        }
    }
}
