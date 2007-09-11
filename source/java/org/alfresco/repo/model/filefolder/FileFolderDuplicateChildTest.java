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
package org.alfresco.repo.model.filefolder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Checks that the duplicate child handling is done correctly.
 * 
 * @see org.alfresco.repo.model.filefolder.FileFolderServiceImpl
 * 
 * @author Derek Hulley
 * @since 2.1.0
 */
public class FileFolderDuplicateChildTest extends TestCase
{
    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private AuthenticationComponent authenticationComponent;
    private TransactionService transactionService;
    private RetryingTransactionHelper retryingTransactionHelper;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private NodeRef rootNodeRef;
    private NodeRef workingRootNodeRef;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        transactionService = serviceRegistry.getTransactionService();
        retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
        nodeService = serviceRegistry.getNodeService();
        fileFolderService = serviceRegistry.getFileFolderService();
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");

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
                NodeRef nodeRef = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(NamespaceService.ALFRESCO_URI, "working root"),
                        ContentModel.TYPE_FOLDER).getChildRef();
                // Done
                return nodeRef;
            }
        };
        workingRootNodeRef = retryingTransactionHelper.doInTransaction(callback, false, true);
    }
    
    public void tearDown() throws Exception
    {
    }
    
    public void testDuplicateChildNameDetection() throws Exception
    {
        // First create a file name F1
        RetryingTransactionCallback<FileInfo> callback = new CreateFileCallback(0);
        FileInfo fileInfo = retryingTransactionHelper.doInTransaction(callback, false, true);
        // Check that the filename is F0
        assertEquals("Incorrect initial filename", "F0", fileInfo.getName());
        
        // Now create a whole lot of threads that attempt file creation
        int threadCount = 10;
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        WorkerThread[] workers = new WorkerThread[threadCount];
        for (int i = 0; i < threadCount; i++)
        {
            workers[i] = new WorkerThread(endLatch);
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
    }
    
    /**
     * Attempts to create a file "Fn" where n is the number supplied to the constructor.
     */
    private class CreateFileCallback implements RetryingTransactionCallback<FileInfo>
    {
        private final int number;
        public CreateFileCallback(int number)
        {
            this.number = number;
        }
        public FileInfo execute() throws Throwable
        {
            authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
            return fileFolderService.create(
                    workingRootNodeRef,
                    "F" + number,
                    ContentModel.TYPE_CONTENT);
        }
    }
    
    private static ThreadGroup threadGroup = new ThreadGroup("FileFolderDuplicateChildTest");
    private static int threadNumber = -1;
    private class WorkerThread extends Thread
    {
        private CountDownLatch endLatch;
        private Throwable error;
        private FileInfo success;

        public WorkerThread(CountDownLatch endLatch)
        {
            super(threadGroup, "Worker " + ++threadNumber);
            this.endLatch = endLatch;
        }
        
        public void run()
        {
            FileInfo fileInfo = null;
            // Start the count with a guaranteed failure
            int number = 0;
            while(true)
            {
                RetryingTransactionCallback<FileInfo> callback = new CreateFileCallback(number);
                try
                {
                    System.out.println("Thread " + getName() + " attempting file: " + number);
                    System.out.flush();

                    fileInfo = retryingTransactionHelper.doInTransaction(callback, false, true);
                    // It worked
                    success = fileInfo;
                    break;
                }
                catch (FileExistsException e)
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
                System.out.println("\t\t\tThread " + getName() + " created file: " + success.getName());
                System.out.flush();
            }
            // Tick the latch
            endLatch.countDown();
        }
    }
}
