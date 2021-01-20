/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.bulkimport.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.bulkimport.BulkImportParameters;
import org.alfresco.repo.bulkimport.NodeImporter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.testing.category.LuceneTests;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since 4.0
 */
@Category(LuceneTests.class)
public class AbstractBulkImportTests
{
    protected static ApplicationContext ctx;

    protected FileFolderService fileFolderService;
    protected NodeService nodeService;
    protected TransactionService transactionService;
    protected ContentService contentService;
    protected UserTransaction txn = null;
    protected RuleService ruleService;
    protected ActionService actionService;
    protected VersionService versionService;
    protected MultiThreadedBulkFilesystemImporter bulkImporter;

    protected NodeRef rootNodeRef;
    protected FileInfo topLevelFolder;
    protected NodeRef top;

    protected static void startContext()
    {
        ctx = ApplicationContextHelper.getApplicationContext();
    }

    protected static void startContext(String[] configLocations)
    {
        ctx = ApplicationContextHelper.getApplicationContext(configLocations);
    }

    protected static void stopContext()
    {
        ApplicationContextHelper.closeApplicationContext();
    }

    @Before
    public void setup() throws SystemException, NotSupportedException
    {
        try
        {
            nodeService = (NodeService)ctx.getBean("nodeService");
            fileFolderService = (FileFolderService)ctx.getBean("fileFolderService");
            transactionService = (TransactionService)ctx.getBean("transactionService");
            bulkImporter = (MultiThreadedBulkFilesystemImporter)ctx.getBean("bulkFilesystemImporter");
            contentService = (ContentService)ctx.getBean("contentService");
            actionService = (ActionService)ctx.getBean("actionService");
            ruleService = (RuleService)ctx.getBean("ruleService");
            versionService = (VersionService)ctx.getBean("versionService");

            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

            String s = "BulkFilesystemImport" + System.currentTimeMillis();

            txn = transactionService.getUserTransaction();
            txn.begin();

            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

            StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, s);
            rootNodeRef = nodeService.getRootNode(storeRef);
            top = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}top"), ContentModel.TYPE_FOLDER).getChildRef();
            
            topLevelFolder = fileFolderService.create(top, s, ContentModel.TYPE_FOLDER);

            txn.commit();
        }
        catch(Throwable e)
        {
            fail(e.getMessage());
        }
    }

    @After
    public void teardown() throws Exception
    {
        AuthenticationUtil.popAuthentication();
        if(txn != null)
        {
            txn.commit();
        }
    }

    protected List<FileInfo> getFolders(NodeRef parent, String pattern)
    {
        PagingResults<FileInfo> page = fileFolderService.list(parent, false, true, pattern, null, null, new PagingRequest(CannedQueryPageDetails.DEFAULT_PAGE_SIZE));
        List<FileInfo> folders = page.getPage();
        return folders;
    }

    protected void testCanVersionDocsWithoutSpecialInputFileNameExtension(
            Function<String, NodeImporter> importerFun)
            throws IOException, SystemException, NotSupportedException, HeuristicRollbackException,
            HeuristicMixedException, RollbackException
    {
        txn = transactionService.getUserTransaction();
        txn.begin();

        NodeRef folderNode = topLevelFolder.getNodeRef();

        // Import with appropriate importer.
        // bulkimport-autoversion/{pass1,pass2,pass3}/example.txt contains different versions of the
        // same file. Run the import once for each subfolder, the file will then get loaded
        // creating a new version for each example.txt
        NodeImporter nodeImporter = importerFun.apply("pass1");
        BulkImportParameters bulkImportParameters = new BulkImportParameters();
        bulkImportParameters.setTarget(folderNode);
        bulkImportParameters.setExistingFileMode(BulkImportParameters.ExistingFileMode.ADD_VERSION);
        bulkImportParameters.setBatchSize(1);

        ExpectedFile[] expectedFiles = new ExpectedFile[]{
                new ExpectedFile("example.txt", MimetypeMap.MIMETYPE_TEXT_PLAIN,
                        "This is an example file. This content is version 1.")
        };

        ExpectedFolder[] expectedFolders = new ExpectedFolder[] { };

        // Import initial version
        bulkImporter.bulkImport(bulkImportParameters, nodeImporter);
        txn.commit();
        txn = transactionService.getUserTransaction();
        txn.begin();
        assertEquals(false, bulkImporter.getStatus().inProgress());
        checkFiles(folderNode, null, expectedFiles, expectedFolders);

        Map<String, FileInfo> files = toMap(getFiles(folderNode, null));
        NodeRef fileNodeRef = files.get("example.txt").getNodeRef();
        assertFalse("Imported file should not yet be versioned:", versionService.isVersioned(fileNodeRef));

        // Import revised document/version
        nodeImporter = importerFun.apply("pass2");
        bulkImporter.bulkImport(bulkImportParameters, nodeImporter);
        txn.commit();
        txn = transactionService.getUserTransaction();
        txn.begin();
        expectedFiles = new ExpectedFile[]{
                new ExpectedFile("example.txt", MimetypeMap.MIMETYPE_TEXT_PLAIN,
                        // Note that pass2 has two versions 2 and 3 in it.
                        "This is an example file. This content is version 3.")
        };
        checkFiles(folderNode, null, expectedFiles, expectedFolders);

        // Import revised document/version
        nodeImporter = importerFun.apply("pass3");
        bulkImporter.bulkImport(bulkImportParameters, nodeImporter);

        txn.commit();
        txn = transactionService.getUserTransaction();
        txn.begin();

        expectedFiles = new ExpectedFile[]{
                new ExpectedFile("example.txt", MimetypeMap.MIMETYPE_TEXT_PLAIN,
                        "This is an example file. This content is version 4."),
        };
        expectedFolders = new ExpectedFolder[] {
                new ExpectedFolder("banana")
        };
        checkFiles(folderNode, null, expectedFiles, expectedFolders);

        // Check the files in the subfolder of pass3
        NodeRef subFolder = fileFolderService.searchSimple(folderNode, "banana");
        expectedFiles = new ExpectedFile[]{
                new ExpectedFile("file.txt", MimetypeMap.MIMETYPE_TEXT_PLAIN,
                        "Version 2")
        };
        expectedFolders = new ExpectedFolder[] { };
        checkFiles(subFolder, null, expectedFiles, expectedFolders);

        assertTrue("Imported file should be versioned:", versionService.isVersioned(fileNodeRef));
        VersionHistory history = versionService.getVersionHistory(fileNodeRef);
        assertNotNull(history);

        assertEquals("Incorrect number of versions.", 4, history.getAllVersions().size());

        Version[] versions = history.getAllVersions().toArray(new Version[4]);

        // Check the content of each version
        ContentReader contentReader;

        contentReader = contentService.getReader(versions[0].getFrozenStateNodeRef(), ContentModel.PROP_CONTENT);
        assertNotNull(contentReader);
        assertEquals("4.0", versions[0].getVersionLabel());
        assertEquals("This is an example file. This content is version 4.", contentReader.getContentString());

        contentReader = contentService.getReader(versions[1].getFrozenStateNodeRef(), ContentModel.PROP_CONTENT);
        assertNotNull(contentReader);
        assertEquals("3.0", versions[1].getVersionLabel());
        assertEquals("This is an example file. This content is version 3.", contentReader.getContentString());

        contentReader = contentService.getReader(versions[2].getFrozenStateNodeRef(), ContentModel.PROP_CONTENT);
        assertNotNull(contentReader);
        assertEquals("2.0", versions[2].getVersionLabel());
        assertEquals("This is an example file. This content is version 2.", contentReader.getContentString());

        contentReader = contentService.getReader(versions[3].getFrozenStateNodeRef(), ContentModel.PROP_CONTENT);
        assertNotNull(contentReader);
        assertEquals("1.0", versions[3].getVersionLabel());
        assertEquals("This is an example file. This content is version 1.", contentReader.getContentString());
    }



    protected List<FileInfo> getFiles(NodeRef parent, String pattern)
    {
        PagingResults<FileInfo> page = fileFolderService.list(parent, true, false, pattern, null, null, new PagingRequest(CannedQueryPageDetails.DEFAULT_PAGE_SIZE));
        List<FileInfo> files = page.getPage();
        return files;
    }

    protected Map<String, FileInfo> toMap(List<FileInfo> list)
    {
        Map<String, FileInfo> map = new HashMap<String, FileInfo>(list.size());
        for(FileInfo fileInfo : list)
        {
            map.put(fileInfo.getName(), fileInfo);
        }
        return map;
    }

    protected void checkFolder(NodeRef folderNode, String childFolderName, String pattern, int numExpectedFolders, int numExpectedFiles, ExpectedFolder[] expectedFolders, ExpectedFile[] expectedFiles)
    {
        List<FileInfo> folders = getFolders(folderNode, childFolderName);
        assertEquals("", 1, folders.size());
        NodeRef folder1 = folders.get(0).getNodeRef();
        checkFiles(folder1, pattern, numExpectedFolders, numExpectedFiles, expectedFiles, expectedFolders);
    }

    protected void checkFiles(NodeRef parent, String pattern,
                              ExpectedFile[] expectedFiles, ExpectedFolder[] expectedFolders)
    {
        int expectedFilesLength = expectedFiles != null ? expectedFiles.length : 0;
        int expectedFoldersLength = expectedFolders != null ? expectedFolders.length : 0;
        checkFiles(parent, pattern, expectedFoldersLength, expectedFilesLength, expectedFiles, expectedFolders);
    }

    protected void checkFiles(NodeRef parent, String pattern, int expectedNumFolders, int expectedNumFiles,
            ExpectedFile[] expectedFiles, ExpectedFolder[] expectedFolders)
    {
        Map<String, FileInfo> folders = toMap(getFolders(parent, pattern));
        Map<String, FileInfo> files = toMap(getFiles(parent, pattern));
        assertEquals("Incorrect number of folders", expectedNumFolders, folders.size());
        assertEquals("Incorrect number of files", expectedNumFiles, files.size());

        if(expectedFiles != null)
        {
            for(ExpectedFile expectedFile : expectedFiles)
            {
                FileInfo fileInfo = files.get(expectedFile.getName());
                assertNotNull(
                        "Couldn't find expected file: "+expectedFile.getName()+
                        ", found: "+files.keySet(), fileInfo);
                assertNotNull("Content data unexpected null for "+expectedFile.getName(), fileInfo.getContentData());
                assertEquals(expectedFile.getMimeType(), fileInfo.getContentData().getMimetype());
                if(fileInfo.getContentData().getMimetype().equals(MimetypeMap.MIMETYPE_TEXT_PLAIN)
                        && expectedFile.getContentContains() != null)
                {
                    ContentReader reader = contentService.getReader(fileInfo.getNodeRef(), ContentModel.PROP_CONTENT);
                    String contentContains = expectedFile.getContentContains();
                    String actualContent = reader.getContentString();
                    assertTrue("Expected contents doesn't include text: " + contentContains +
                            ", full text:\n"+actualContent,
                            actualContent.contains(contentContains));
                }
            }
        }
        
        if(expectedFolders != null)
        {
            for(ExpectedFolder expectedFolder : expectedFolders)
            {
                FileInfo fileInfo = folders.get(expectedFolder.getName());
                assertNotNull("", fileInfo);
            }
        }
    }

    protected void checkContent(FileInfo file, String name, String mimeType)
    {
        assertEquals("", name, file.getName());
        assertEquals("", mimeType, file.getContentData().getMimetype());
    }
    
    
    protected static class ExpectedFolder
    {
        private String name;

        public ExpectedFolder(String name)
        {
            super();
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
    }

    protected static class ExpectedFile
    {
        private String name;
        private String mimeType;
        private String contentContains = null;
        
        public ExpectedFile(String name, String mimeType, String contentContains)
        {
            this(name, mimeType);
            this.contentContains = contentContains;
        }
        
        public ExpectedFile(String name, String mimeType)
        {
            super();
            this.name = name;
            this.mimeType = mimeType;
        }

        public String getName()
        {
            return name;
        }

        public String getMimeType()
        {
            return mimeType;
        }

        public String getContentContains()
        {
            return contentContains;
        }
    }
}
