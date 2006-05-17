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
package org.alfresco.repo.model.filefolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.springframework.context.ApplicationContext;

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
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private TransactionService transactionService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private StoreRef storeRef;
    private NodeRef rootFolderRef;
    private File dataFile;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        fileFolderService = serviceRegistry.getFileFolderService();
        
        // create a folder root to work in
        storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, getName() + "_" + System.currentTimeMillis());
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
        rootFolderRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.ALFRESCO_URI, getName()),
                ContentModel.TYPE_FOLDER).getChildRef();
        dataFile = AbstractContentTransformerTest.loadQuickTestFile("txt");
    }
    
    public void testSetUp() throws Exception
    {
        assertNotNull(dataFile);
    }
    
    /**
     * Creates <code>folderCount</code> folders below the given parent and populates each folder with
     * <code>fileCount</code> files.  The folders will be created as siblings in one go, but the files
     * are added one to each folder until each folder has the presribed number of files within it.
     * This can therefore be used to test the performance when the L2 cache sizes are exceeded.
     * <p>
     * Each creation (file or folder) uses the <b>REQUIRES_NEW</b> transaction declaration.
     * 
     * @param parentNodeRef the level zero parent
     * @return Returns the average time (ms) to create the <b>files only</b>
     */
    private double buildStructure(final NodeRef parentNodeRef, final int folderCount, final int fileCount)
    {
        List<NodeRef> folders = new ArrayList<NodeRef>(folderCount);
        for (int i = 0; i < folderCount; i++)
        {
            TransactionWork<FileInfo> createFolderWork = new TransactionWork<FileInfo>()
            {
                public FileInfo doWork() throws Exception
                {
                    FileInfo folderInfo = fileFolderService.create(
                            parentNodeRef,
                            GUID.generate(),
                            ContentModel.TYPE_FOLDER);
                    // done
                    return folderInfo;
                }
            };
            FileInfo folderInfo = TransactionUtil.executeInUserTransaction(transactionService, createFolderWork);
            // keep the reference
            folders.add(folderInfo.getNodeRef());
        }
        // now progress around the folders until they have been populated
        long start = System.currentTimeMillis();
        for (int i = 0; i < fileCount; i++)
        {
            for (final NodeRef folderRef : folders)
            {
                TransactionWork<FileInfo> createFileWork = new TransactionWork<FileInfo>()
                {
                    public FileInfo doWork() throws Exception
                    {
                        FileInfo fileInfo = fileFolderService.create(
                                folderRef,
                                GUID.generate(),
                                ContentModel.TYPE_CONTENT);
                        NodeRef nodeRef = fileInfo.getNodeRef();
                        // write the content
                        ContentWriter writer = fileFolderService.getWriter(nodeRef);
                        writer.putContent(dataFile);
                        // done
                        return fileInfo;
                    }
                };
                TransactionUtil.executeInUserTransaction(transactionService, createFileWork);
            }
        }
        long end = System.currentTimeMillis();
        long time = (end - start);
        double average = (double) time / (double) (folderCount * fileCount);
        // done
        return average;
    }

    private void timeBuildStructure(NodeRef parentNodeRef, int folderCount, int fileCount)
    {
        System.out.println("Starting load of " + fileCount + " files in each of " + folderCount + " folders");
        double average = buildStructure(parentNodeRef, folderCount, fileCount);
        System.out.println(
                "[" + getName() + "] \n" +
                "   Created " + fileCount + " files in each of " + folderCount + " folders: \n" +
                "   Average: " + String.format("%10.2f", average) + "ms per file \n" +
                "   Average: " + String.format("%10.2f", 1000.0/average) + " files per second");
    }
    
    public void test1Folder10Children() throws Exception
    {
        timeBuildStructure(rootFolderRef, 1, 10);
    }
    
    public void test10Folders100ChildrenMultiTxn() throws Exception
    {
        timeBuildStructure(rootFolderRef, 10, 100);
    }
//    
//    public void test100Folders1Child() throws Exception
//    {
//        timeBuildStructure(rootFolderRef, 100, 1);
//    }
//    
//    public void test1000Folders10Children() throws Exception
//    {
//        timeBuildStructure(rootFolderRef, 1000, 10);
//    }
//    
//    public void test1000Folders100Children() throws Exception
//    {
//        timeBuildStructure(rootFolderRef, 5, 100);
//    }
//    
//    public void test1000Folders1000Children() throws Exception
//    {
//        timeBuildStructure(rootFolderRef, 1000, 1000);
//    }
}
