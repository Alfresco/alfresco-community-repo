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
package org.alfresco.repo.model.filefolder;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.ArgumentHelper;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Tests around some of the data structures that lead to performance
 * degradation.  We use the {@link org.alfresco.service.cmr.model.FileFolderService FileFolderService}
 * as it provides the most convenient and most common test scenarios.
 * <p>
 * Note that this test is not designed to validate performance figures, but is
 * rather a handy tool for doing benchmarking.  It is therefore not named <i>*Test</i> as is the
 * pattern for getting tests run by the continuous build.
 * 
 * @author Derek Hulley
 */
public class FileFolderPerformanceTester extends TestCase
{
    private static Log logger = LogFactory.getLog(FileFolderPerformanceTester.class);
    
    protected static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    protected RetryingTransactionHelper retryingTransactionHelper;
    protected NodeService nodeService;
    
    private AuthenticationComponent authenticationComponent;
    private FileFolderService fileFolderService;
    private SearchService searchService;
    private NodeRef rootFolderRef;
    private File dataFile;
    
    
    protected NodeService getNodeService()
    {
        return (NodeService)ctx.getBean("NodeService");
    }
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        retryingTransactionHelper = (RetryingTransactionHelper) ctx.getBean("retryingTransactionHelper");
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        
        fileFolderService = serviceRegistry.getFileFolderService();
        searchService = serviceRegistry.getSearchService();
        nodeService = getNodeService();
        
        // authenticate
        authenticationComponent.setSystemUserAsCurrentUser();
        
        rootFolderRef = getOrCreateRootFolder();
        
        dataFile = AbstractContentTransformerTest.loadQuickTestFile("txt");
    }
    
    public void testSetUp() throws Exception
    {
        assertNotNull(dataFile);
    }
    
    protected NodeRef getOrCreateRootFolder()
    {
     // find the guest folder
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_XPATH, "/app:company_home");
        try
        {
            if (rs.length() == 0)
            {
                throw new AlfrescoRuntimeException("Didn't find Company Home");
            }
            NodeRef companyHomeNodeRef = rs.getNodeRef(0);
            return fileFolderService.create(
                    companyHomeNodeRef,
                    getName() + "_" + System.currentTimeMillis(),
                    ContentModel.TYPE_FOLDER).getNodeRef();
        }
        finally
        {
            rs.close();
        }
    }
    
    /**
     * Creates <code>folderCount</code> folders below the given parent and populates each folder with
     * <code>fileCount</code> files.  The folders will be created as siblings in one go, but the files
     * are added one to each folder until each folder has the presribed number of files within it.
     * This can therefore be used to test the performance when the L2 cache sizes are exceeded.
     * <p>
     * Each creation (file or folder) uses the <b>PROPAGATION REQUIRED</b> transaction declaration.
     * 
     * @param parentNodeRef the level zero parent
     * @param randomOrder true if each thread must put the children into the folders in a random order
     * @return Returns the average time (ms) to create the <b>files only</b>
     */
    private void buildStructure(
            final NodeRef parentNodeRef,
            final int threadCount,
            final boolean randomOrder,
            final int folderCount,
            final int batchCount,
            final int filesPerBatch,
            final double[] dumpPoints)
    {
        RetryingTransactionCallback<NodeRef[]> createFoldersCallback = new RetryingTransactionCallback<NodeRef[]>()
        {
            public NodeRef[] execute() throws Exception
            {
                NodeRef[] folders = new NodeRef[folderCount];
                for (int i = 0; i < folderCount; i++)
                {
                    FileInfo folderInfo = fileFolderService.create(
                            parentNodeRef,
                            GUID.generate(),
                            ContentModel.TYPE_FOLDER);
                    // keep the reference
                    folders[i] = folderInfo.getNodeRef();
                }
                return folders;
            }
        };
        final NodeRef[] folders = retryingTransactionHelper.doInTransaction(createFoldersCallback);
        // the worker that will load the files into the folders
        Runnable runnable = new Runnable()
        {
            private long start;
            public void run()
            {
                // authenticate
                authenticationComponent.setSystemUserAsCurrentUser();
                
                // progress around the folders until they have been populated
                start = System.currentTimeMillis();
                int nextDumpNumber = 0;
                for (int i = 0; i < batchCount; i++)
                {
                    // must we dump results
                    double completedCount = (double) i;
                    double nextDumpCount = (dumpPoints == null || dumpPoints.length == 0 || nextDumpNumber >= dumpPoints.length)
                                           ? -1.0
                                           : (double) batchCount * dumpPoints[nextDumpNumber];
                    if ((nextDumpCount - 0.5) < completedCount && completedCount < (nextDumpCount + 0.5))
                    {
                        dumpResults(i);
                        nextDumpNumber++;
                    }
                    // shuffle folders if required
                    List<NodeRef> foldersList = Arrays.asList(folders);
                    if (randomOrder)
                    {
                        // shuffle folder list
                        Collections.shuffle(foldersList);
                    }
                    for (int j = 0; j < folders.length; j++)
                    {
                        final NodeRef folderRef = folders[j];
                        RetryingTransactionCallback<Void> createFileCallback = new RetryingTransactionCallback<Void>()
                        {
                            public Void execute() throws Exception
                            {
                                for (int i = 0; i < filesPerBatch; i++)
                                {
                                    FileInfo fileInfo = fileFolderService.create(
                                            folderRef,
                                            GUID.generate(),
                                            ContentModel.TYPE_CONTENT);
                                    NodeRef nodeRef = fileInfo.getNodeRef();
                                    // write the content
                                    ContentWriter writer = fileFolderService.getWriter(nodeRef);
                                    writer.putContent(dataFile);
                                }
                                // done
                                return null;
                            }
                        };
                        retryingTransactionHelper.doInTransaction(createFileCallback);
                    }
                }
                dumpResults(batchCount);
            }
            private void dumpResults(int currentBatchCount)
            {
                long end = System.currentTimeMillis();
                long time = (end - start);
                double average = (double) time / (double) (folderCount * currentBatchCount * filesPerBatch);
                double percentComplete = (double) currentBatchCount / (double) batchCount * 100.0;
                logger.debug("\n" +
                        "[" + Thread.currentThread().getName() + "] \n" +
                        "   Created " + (currentBatchCount*filesPerBatch) + " files in each of " + folderCount +
                            " folders (" + (randomOrder ? "shuffled" : "in order") + "): \n" +
                        "   Progress: " + String.format("%9.2f", percentComplete) +  " percent complete \n" +
                        "   Average: " + String.format("%10.2f", average) + " ms per file \n" +
                        "   Average: " + String.format("%10.2f", 1000.0/average) + " files per second");
            }
        };

        // kick off the required number of threads
        logger.debug("\n" +
                "Starting " + threadCount +
                " threads loading " + (batchCount * filesPerBatch) +
                " files in each of " + folderCount +
                " folders (" +
                (randomOrder ? "shuffled" : "in order") +
                (filesPerBatch > 1 ? (" and " + filesPerBatch + " files per txn") : "") +
                ").");
        ThreadGroup threadGroup = new ThreadGroup(getName());
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++)
        {
            threads[i] = new Thread(threadGroup, runnable, String.format("FileLoader-%02d", i));
            threads[i].start();
        }
        // join each thread so that we wait for them all to finish
        for (int i = 0; i < threads.length; i++)
        {
            try
            {
                threads[i].join();
            }
            catch (InterruptedException e)
            {
                // not too serious - the worker threads are non-daemon
            }
        }
    }
    
    @SuppressWarnings("unused")
    private void readStructure(
            final NodeRef parentNodeRef,
            final int threadCount,
            final int repetitions,
            final double[] dumpPoints)
    {
        final List<ChildAssociationRef> children = nodeService.getChildAssocs(parentNodeRef);
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                // authenticate
                authenticationComponent.setSystemUserAsCurrentUser();
                
                for (int i = 0; i < repetitions; i++)
                {
                    // read the contents of each folder
                    for (ChildAssociationRef childAssociationRef : children)
                    {
                        final NodeRef folderRef = childAssociationRef.getChildRef();
                        RetryingTransactionCallback<Object> readCallback = new RetryingTransactionCallback<Object>()
                        {
                            public Object execute() throws Exception
                            {
                                // read the child associations of the folder
                                nodeService.getChildAssocs(folderRef);
                                // get the type
                                nodeService.getType(folderRef);
                                // done
                                return null;
                            };
                        };
                        retryingTransactionHelper.doInTransaction(readCallback, true);
                    }
                }
            }            
        };

        // kick off the required number of threads
        logger.debug("\n" +
                "Starting " + threadCount +
                " threads reading properties and children of " + children.size() +
                " folder " + repetitions +
                " times.");
        ThreadGroup threadGroup = new ThreadGroup(getName());
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++)
        {
            threads[i] = new Thread(threadGroup, runnable, String.format("FileReader-%02d", i));
            threads[i].start();
        }
        // join each thread so that we wait for them all to finish
        for (int i = 0; i < threads.length; i++)
        {
            try
            {
                threads[i].join();
            }
            catch (InterruptedException e)
            {
                // not too serious - the worker threads are non-daemon
            }
        }
    }
    
//    /** Load 5000 files into a single folder using 2 threads */
//    public void test_2_ordered_1_2500() throws Exception
//    {
//        buildStructure(rootFolderRef, 2, false, 1, 2500, new double[] {0.25, 0.50, 0.75});
//    }
    
//    public void test_4_ordered_10_100() throws Exception
//    {
//        buildStructure(rootFolderRef, 4, false, 10, 100, new double[] {0.25, 0.50, 0.75});
//    }
//    
//    public void test_4_shuffled_10_100() throws Exception
//    {
//        buildStructure(rootFolderRef, 4, true, 10, 100, new double[] {0.25, 0.50, 0.75});
//    }
//    public void test_1_ordered_100_100() throws Exception
//    {
//        buildStructure(
//                rootFolderRef,
//                1,
//                false,
//                100,
//                100,
//                new double[] {0.10, 0.20, 0.30, 0.40, 0.50, 0.60, 0.70, 0.80, 0.90});
//    }
//    public void test_1_shuffled_10_400() throws Exception
//    {
//        buildStructure(
//                rootFolderRef,
//                1,
//                true,
//                10,
//                400,
//                new double[] {0.05, 0.10, 0.20, 0.30, 0.40, 0.50, 0.60, 0.70, 0.80, 0.90});
//    }
    public void test_4_shuffled_10_100() throws Exception
    {
        buildStructure(
                rootFolderRef,
                4,
                true,
                10,
                100,
                20,
                new double[] {0.05, 0.10, 0.20, 0.30, 0.40, 0.50, 0.60, 0.70, 0.80, 0.90});
    }
//    public void test_1_ordered_1_50000() throws Exception
//    {
//        buildStructure(
//                rootFolderRef,
//                1,
//                false,
//                1,
//                50000,
//                new double[] {0.01, 0.02, 0.03, 0.04, 0.05, 0.10, 0.20, 0.30, 0.40, 0.50, 0.60, 0.70, 0.80, 0.90});
//    }
    
    
    /**
     * Create a bunch of files and folders in a folder and then run multi-threaded directory
     * listings against it.
     * 
     * @param args         <x> <y> where 'x' is the number of files in a folder and 'y' is the 
     *                     number of threads to list
     */
    public static void main(String ... args)
    {
        ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) ApplicationContextHelper.getApplicationContext();

        try
        {
            run(ctx, args);
        }
        catch (Throwable e)
        {
            System.out.println("Failed to run CifsHelper performance test");
            e.printStackTrace();
        }
        finally
        {
            ctx.close();
        }
    }
    
    private static void run(final ApplicationContext ctx, String ... args) throws Throwable
    {
        ArgumentHelper argHelper = new ArgumentHelper(getUsage(), args);
        final int fileCount = argHelper.getIntegerValue("files", true, 1, 10000);
        final String folderRefStr = argHelper.getStringValue("folder", false, true);
        final int threadCount = argHelper.getIntegerValue("threads", false, 1, 100);
        final NodeRef selectedFolderNodeRef = folderRefStr == null ? null : new NodeRef(folderRefStr);
        
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        final MutableAuthenticationService authenticationService = serviceRegistry.getAuthenticationService();
        final PermissionService permissionService = serviceRegistry.getPermissionService();
        final NodeService nodeService = serviceRegistry.getNodeService();
        final SearchService searchService = serviceRegistry.getSearchService();
        final TransactionService transactionService = serviceRegistry.getTransactionService();
        final FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
        
        RunAsWork<String> createUserRunAs = new RunAsWork<String>()
        {
            public String doWork() throws Exception
            {
                String user = GUID.generate();
                authenticationService.createAuthentication(user, user.toCharArray());
                return user;
            }
        };
        final String user = AuthenticationUtil.runAs(createUserRunAs, AuthenticationUtil.getSystemUserName());

        // Create the files
        final RetryingTransactionCallback<NodeRef> createCallback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                AuthenticationUtil.pushAuthentication();

                DictionaryDAO dictionaryDao = (DictionaryDAO) ctx.getBean("dictionaryDAO");
                M2Model model = M2Model.createModel("tempModel");
                model.createNamespace("test", "t");
                model.createNamespace("testx", "");
                for (int m = 0; m < 30; m++)
                {
                    model.createAspect("t:aspect_" + m);
                }
                dictionaryDao.putModel(model);
                
                NodeRef folderNodeRef = null;
                try
                {
                    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                    if (selectedFolderNodeRef == null)
                    {
                        // find the guest folder
                        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
                        ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_XPATH, "/app:company_home");
                        try
                        {
                            if (rs.length() == 0)
                            {
                                throw new AlfrescoRuntimeException("Didn't find Company Home");
                            }
                            NodeRef companyHomeNodeRef = rs.getNodeRef(0);
                            folderNodeRef = fileFolderService.create(
                                    companyHomeNodeRef,
                                    "TOP_FOLDER_" + System.currentTimeMillis(),
                                    ContentModel.TYPE_FOLDER).getNodeRef();
                        }
                        finally
                        {
                            rs.close();
                        }
                        // Grant permissions
                        permissionService.setPermission(folderNodeRef, user, PermissionService.ALL_PERMISSIONS, true);
                    }
                    else
                    {
                        folderNodeRef = selectedFolderNodeRef;
                        // Grant permissions
                        permissionService.setPermission(folderNodeRef, user, PermissionService.ALL_PERMISSIONS, true);
                        System.out.println("Reusing folder " + folderNodeRef);
                    }
                }
                finally
                {
                    AuthenticationUtil.popAuthentication();
                }
                if (selectedFolderNodeRef == null)
                {
                    List<String> largeCollection = new ArrayList<String>(1000);
                    for (int i = 0; i < 50; i++)
                    {
                        largeCollection.add(String.format("Large-collection-value-%05d", i));
                    }
                    
                    // Create the files
                    for (int i = 0; i < fileCount; i++)
                    {
                        FileInfo fileInfo = fileFolderService.create(
                                folderNodeRef,
                                String.format("FILE-%4d", i),
                                ContentModel.TYPE_CONTENT);
                        NodeRef nodeRef = fileInfo.getNodeRef();
                        nodeService.setProperty(
                                nodeRef,
                                QName.createQName("{test}mv"),
                                (Serializable) largeCollection);
                        for (int m = 0; m < 30; m++)
                        {
                            nodeService.addAspect(
                                    nodeRef,
                                    QName.createQName("{test}aspect_"+m), null);
                        }
                    }
                    System.out.println("Created " + fileCount + " files in folder " + folderNodeRef);
                    
                }
                // Done
                return folderNodeRef;
            }
        };
        
        RunAsWork<NodeRef> createRunAs = new RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                return transactionService.getRetryingTransactionHelper().doInTransaction(createCallback);
            }
        };
        final NodeRef folderNodeRef = AuthenticationUtil.runAs(createRunAs, user);
        
        // Now wait for some input before commencing the read run
        System.out.print("Hit any key to commence directory listing ...");
        System.in.read();
        final RunAsWork<List<FileInfo>> readRunAs = new RunAsWork<List<FileInfo>>()
        {
            public List<FileInfo> doWork() throws Exception
            {
                return fileFolderService.search(folderNodeRef, "*", false);
            }
        };
        
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++)
        {
            Thread readThread = new Thread("FolderList-" + i)
            {
                int iteration = 0;
                public void run()
                {
                    while(++iteration <= 2)
                    {
                        runImpl();
                    }
                }
                private void runImpl()
                {
                    String threadName = Thread.currentThread().getName();
                    long start = System.currentTimeMillis();
                    List<FileInfo> nodeRefs = AuthenticationUtil.runAs(readRunAs, user);
                    long time = System.currentTimeMillis() - start;
                    double average = (double) time / (double) (fileCount);
                    
                    // Make sure that we have the correct number of entries
                    if (folderRefStr != null && nodeRefs.size() != fileCount)
                    {
                        System.err.println(
                                "WARNING: Thread " + threadName + " got " + nodeRefs.size() +
                                " but expected " + fileCount);
                    }
                    System.out.print("\n" +
                            "Thread " + threadName + ": \n" +
                            "   Read " + String.format("%4d", fileCount) +  " files \n" +
                            "   Average: " + String.format("%10.2f", average) + " ms per file \n" +
                            "   Average: " + String.format("%10.2f", 1000.0/average) + " files per second");
                }
            };
            readThread.start();
            threads[i] = readThread;
        }
        
        for (int i = 0; i < threads.length; i++)
        {
            threads[i].join();
        }
    }
    
    private static String getUsage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("FileFolderPerformanceTester usage: ").append("\n");
        sb.append("   FileFolderPerformanceTester --files=<filecount> --threads=<threadcount> --folder=<folderref>").append("\n");
        sb.append("      filecount: number of files in the folder").append("\n");
        sb.append("      threadcount: number of threads to do the directory listing").append("\n");
        return sb.toString();
    }
}
