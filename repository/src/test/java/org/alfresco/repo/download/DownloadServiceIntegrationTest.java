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
package org.alfresco.repo.download;

import net.sf.acegisecurity.Authentication;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.SystemNodeUtils;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.download.DownloadStatus.Status;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.TemporaryNodes;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

/**
 * Integration test for DownloadServiceImpl
 *
 * @author Alex Miller
 */
@Category(OwnJVMTestsCategory.class)
public class DownloadServiceIntegrationTest 
{
    public static final long MAX_TIME = 5000;

    private static final long PAUSE_TIME = 1000;
    
    // Rule to initialize the default Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();
    
    // Rules to create 2 test users.
    public static AlfrescoPerson TEST_USER = new AlfrescoPerson(APP_CONTEXT_INIT, "User");
    public static AlfrescoPerson TEST_USER2 = new AlfrescoPerson(APP_CONTEXT_INIT, "User 2");

    public static String TEST_USER_NAME = "some-user";
    
    // A rule to manage test nodes reused across all the test methods
    public static TemporaryNodes STATIC_TEST_NODES = new TemporaryNodes(APP_CONTEXT_INIT);
    
    // Tie them together in a static Rule Chain
    @ClassRule public static RuleChain ruleChain = RuleChain.outerRule(APP_CONTEXT_INIT)
                                                            .around(TEST_USER)
                                                            .around(STATIC_TEST_NODES);
    
    // A rule to manage test nodes use in each test method
    @Rule public TemporaryNodes testNodes = new TemporaryNodes(APP_CONTEXT_INIT);

    // Service under test
    public static DownloadService DOWNLOAD_SERVICE;
    private static DownloadStorage DOWNLOAD_STORAGE;
    
    // Various supporting services
    private static CheckOutCheckInService    CHECK_OUT_CHECK_IN_SERVICE;
    private static ContentService            CONTENT_SERVICE;
    private static NodeService               NODE_SERVICE;
    private static PermissionService         PERMISSION_SERVICE;
    private static RetryingTransactionHelper TRANSACTION_HELPER;
    private static IntegrityChecker          INTEGRITY_CHECKER;
    
    // Test Content 
    private NodeRef rootFolder;
    private NodeRef rootFile;
    private NodeRef secondaryNode;

    private NodeRef level1Folder1;

    private NodeRef level1Folder2;

    private Set<String> allEntries;

    private NodeRef fileToCheckout;
    
    @BeforeClass public static void init()
    {
        // Resolve required services
        CHECK_OUT_CHECK_IN_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("CheckOutCheckInService", CheckOutCheckInService.class);
        CONTENT_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("contentService", ContentService.class);
        DOWNLOAD_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("DownloadService", DownloadService.class);
        DOWNLOAD_STORAGE = APP_CONTEXT_INIT.getApplicationContext().getBean("downloadStorage", DownloadStorage.class);
        NODE_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("NodeService", NodeService.class);
        PERMISSION_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("PermissionService", PermissionService.class);
        TRANSACTION_HELPER = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        INTEGRITY_CHECKER = APP_CONTEXT_INIT.getApplicationContext().getBean("integrityChecker", IntegrityChecker.class);
        INTEGRITY_CHECKER.setEnabled(true);
        INTEGRITY_CHECKER.setFailOnViolation(true);
        INTEGRITY_CHECKER.setTraceOn(true);
    }
 
    /**
     * Create the test content
     */
    @Before public void createContent()
    {
        allEntries = new TreeSet<String>();
        
        AuthenticationUtil.setRunAsUserSystem();
        
        Repository repositoryHelper = (Repository) APP_CONTEXT_INIT.getApplicationContext().getBean("repositoryHelper");
        NodeRef COMPANY_HOME = repositoryHelper.getCompanyHome();
        
        // Create some static test content
       rootFolder = testNodes.createNode(COMPANY_HOME, "rootFolder", ContentModel.TYPE_FOLDER, AuthenticationUtil.getAdminUserName());
       allEntries.add("rootFolder/");

       rootFile = testNodes.createNodeWithTextContent(COMPANY_HOME, "rootFile.txt", ContentModel.TYPE_CONTENT, AuthenticationUtil.getAdminUserName(), "Root file content");
       allEntries.add("rootFile.txt");
       
       testNodes.createNodeWithTextContent(rootFolder, "level1File.txt", ContentModel.TYPE_CONTENT, AuthenticationUtil.getAdminUserName(), "Level 1 file content");
       allEntries.add("rootFolder/level1File.txt");
       
       level1Folder1 = testNodes.createNode(rootFolder, "level1Folder1", ContentModel.TYPE_FOLDER, AuthenticationUtil.getAdminUserName());
       allEntries.add("rootFolder/level1Folder1/");
       
       level1Folder2 = testNodes.createNode(rootFolder, "level1Folder2", ContentModel.TYPE_FOLDER, AuthenticationUtil.getAdminUserName());
       allEntries.add("rootFolder/level1Folder2/");
       
       testNodes.createNode(rootFolder, "level1EmptyFolder", ContentModel.TYPE_FOLDER, AuthenticationUtil.getAdminUserName());
       allEntries.add("rootFolder/level1EmptyFolder/");
       
       testNodes.createNodeWithTextContent(level1Folder1, "level2File.txt", ContentModel.TYPE_CONTENT, AuthenticationUtil.getAdminUserName(), "Level 2 file content");
       allEntries.add("rootFolder/level1Folder1/level2File.txt");

       testNodes.createNodeWithTextContent(level1Folder2, "level2File.txt", ContentModel.TYPE_CONTENT, AuthenticationUtil.getAdminUserName(), "Level 2 file content");
       allEntries.add("rootFolder/level1Folder2/level2File.txt");
       
       secondaryNode = testNodes.createNodeWithTextContent(COMPANY_HOME, "secondaryNodeFile.txt", ContentModel.TYPE_CONTENT, AuthenticationUtil.getAdminUserName(), "Secondary node");
       ChildAssociationRef assoc = NODE_SERVICE.addChild(rootFolder, secondaryNode, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
       Assert.assertFalse(assoc.isPrimary());
       allEntries.add("rootFolder/secondaryNodeFile.txt");
       
       fileToCheckout = testNodes.createNodeWithTextContent(level1Folder2, "fileToCheckout.txt", ContentModel.TYPE_CONTENT, AuthenticationUtil.getAdminUserName(), "Level 2 file content");
       // Add the lock and version aspects to the created node
       NODE_SERVICE.addAspect(fileToCheckout, ContentModel.ASPECT_VERSIONABLE, null);
       NODE_SERVICE.addAspect(fileToCheckout, ContentModel.ASPECT_LOCKABLE, null);
       
       allEntries.add("rootFolder/level1Folder2/fileToCheckout.txt");
       PERMISSION_SERVICE.setPermission(level1Folder2, TEST_USER.getUsername(), PermissionService.ALL_PERMISSIONS, true);
       PERMISSION_SERVICE.setPermission(fileToCheckout, TEST_USER.getUsername(), PermissionService.ALL_PERMISSIONS, true);
    }
    
    @Test public void createDownload() throws IOException, InterruptedException
    {
        // Initiate the download
        final NodeRef downloadNode = DOWNLOAD_SERVICE.createDownload(new NodeRef[] {rootFile, rootFolder},  true);
        Assert.assertNotNull(downloadNode);

        testNodes.addNodeRef(downloadNode);
        
    	// Validate that the download node has been persisted correctly.
    	TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Object>()
        {

            @Override
            public Object execute() throws Throwable
            {
                Map<QName, Serializable> properties = NODE_SERVICE.getProperties(downloadNode);
                Assert.assertEquals(Boolean.TRUE, properties.get(DownloadModel.PROP_RECURSIVE));
                
                List<AssociationRef> associations = NODE_SERVICE.getTargetAssocs(downloadNode, DownloadModel.ASSOC_REQUESTED_NODES);
                for (AssociationRef association : associations)
                {
                    Assert.assertTrue(association.getTargetRef().equals(rootFile) || association.getTargetRef().equals(rootFolder));
                }
                
                Assert.assertTrue(NODE_SERVICE.hasAspect(downloadNode, ContentModel.ASPECT_INDEX_CONTROL));
                Assert.assertEquals(Boolean.FALSE,properties.get(ContentModel.PROP_IS_INDEXED));
                Assert.assertEquals(Boolean.FALSE,properties.get(ContentModel.PROP_IS_CONTENT_INDEXED));
                
                return null;
            }
        });
        
        DownloadStatus status = getDownloadStatus(downloadNode);
        while (status.getStatus() == Status.PENDING) 
        {
            Thread.sleep(PAUSE_TIME);
            status = getDownloadStatus(downloadNode);
        }
        
        Assert.assertEquals(6l, status.getTotalFiles());
        
        long elapsedTime = waitForDownload(downloadNode);
        
        Assert.assertTrue("Maximum creation time exceeded!", elapsedTime < MAX_TIME);

        
        // Validate the content.
        final Set<String> entryNames = getEntries(downloadNode);
        
        validateEntries(entryNames, allEntries, true);
    }

    private void validateEntries(final Set<String> entryNames, final Set<String> expectedEntries, boolean onlyExpected)
    {
        Set<String> copy = new TreeSet<String>(entryNames);
        for (String expectedEntry : expectedEntries) 
        {
            Assert.assertTrue("Missing entry:- " + expectedEntry, copy.contains(expectedEntry));
            copy.remove(expectedEntry);
        }
        
        if (onlyExpected == true)
        {
            Assert.assertTrue("Unexpected entries", copy.isEmpty());
        }
    }

    private Set<String> getEntries(final NodeRef downloadNode)
    {
        return TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Set<String>>()
        {

            @Override
            public Set<String> execute() throws Throwable
            {
                Set<String> entryNames = new TreeSet<String>();
                ContentReader reader = CONTENT_SERVICE.getReader(downloadNode, ContentModel.PROP_CONTENT);
                ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(reader.getContentInputStream());
                try 
                {
                    ZipArchiveEntry zipEntry = zipInputStream.getNextZipEntry();
                    while (zipEntry != null)
                    {
                        String name = zipEntry.getName();
                        entryNames.add(name);
                        zipEntry = zipInputStream.getNextZipEntry();
                    }
                }
                finally
                {
                    zipInputStream.close();
                }
                return entryNames;
            }
        });
    }

    private long waitForDownload(final NodeRef downloadNode) throws InterruptedException
    {
        long startTime = System.currentTimeMillis();
        // Wait for the staus to become done.
        DownloadStatus status;
        long elapsedTime;
        do {
            status = getDownloadStatus(downloadNode);
            elapsedTime = System.currentTimeMillis() - startTime;
            if (status.isComplete() == false) 
            {
                Thread.sleep(PAUSE_TIME);
            }
        } while (status.isComplete() == false && elapsedTime < MAX_TIME);
        return elapsedTime;
    }
    
    
    
    private DownloadStatus getDownloadStatus(final NodeRef downloadNode) 
    {
        return TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<DownloadStatus>()
        {

            @Override
            public DownloadStatus execute() throws Throwable
            {
                return DOWNLOAD_SERVICE.getDownloadStatus(downloadNode);
            }
        });
    }
    
    @Test public void deleteBeforeDateAsSystem() throws InterruptedException
    {
        NodeRef beforeNodeRef;
        NodeRef afterNodeRef;
        Date beforeTime;
        
        beforeNodeRef = DOWNLOAD_SERVICE.createDownload(new NodeRef[] {level1Folder1}, true);
        testNodes.addNodeRef(beforeNodeRef);
        waitForDownload(beforeNodeRef);
        
        beforeTime = new Date();
        
        afterNodeRef = DOWNLOAD_SERVICE.createDownload(new NodeRef[] {level1Folder2}, true);
        testNodes.addNodeRef(afterNodeRef);
        waitForDownload(afterNodeRef);
        
        DOWNLOAD_SERVICE.deleteDownloads(beforeTime);
        
        Assert.assertFalse(NODE_SERVICE.exists(beforeNodeRef));
        Assert.assertTrue(NODE_SERVICE.exists(afterNodeRef));

    }

    @Test
    public void deleteBeforeDateAsNormalUser() throws InterruptedException
    {
        String randomUsername = createRandomUser();

        Authentication previousAuth = AuthenticationUtil.getFullAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(randomUsername);

        NodeRef beforeNodeRef;
        NodeRef afterNodeRef;
        Date beforeTime;

        try
        {
            beforeNodeRef = DOWNLOAD_SERVICE.createDownload(new NodeRef[] { level1Folder1 }, true);
            testNodes.addNodeRef(beforeNodeRef);
            waitForDownload(beforeNodeRef);

            beforeTime = new Date();

            afterNodeRef = DOWNLOAD_SERVICE.createDownload(new NodeRef[] { level1Folder2 }, true);
            testNodes.addNodeRef(afterNodeRef);
            waitForDownload(afterNodeRef);
        }
        finally
        {
            // assuming previous authentication is the system user
            AuthenticationUtil.setFullAuthentication(previousAuth);
        }
        DOWNLOAD_SERVICE.deleteDownloads(beforeTime, 1000, false);

        Assert.assertFalse(NODE_SERVICE.exists(beforeNodeRef));
        Assert.assertTrue(NODE_SERVICE.exists(afterNodeRef));
    }

    // see MNT-20212
    @Test
    public void deleteBeforeDateAsNormalUserFromAllSysDownloadFolders() throws InterruptedException
    {
        String randomUsername = createRandomUser();

        NamespaceService namespaceService = APP_CONTEXT_INIT.getApplicationContext().getBean("namespaceService", NamespaceService.class);
        Repository repositoryHelper = APP_CONTEXT_INIT.getApplicationContext().getBean("repositoryHelper", Repository.class);
        FileFolderService fileFolderService = APP_CONTEXT_INIT.getApplicationContext().getBean("fileFolderService", FileFolderService.class);

        // this value should be similar to whatever is set in the properties files for:
        // system.downloads_container.childname=sys:downloads
        final String nodeName = "sys:downloads";
        QName container = QName.createQName(nodeName, namespaceService);

        final NodeRef problematicDuplicateSystemDownloadNode = createProblematicDuplicateSystemNode(container, NODE_SERVICE, repositoryHelper);

        try
        {
            //add "downloads_container"
            assertNotEquals(problematicDuplicateSystemDownloadNode.getId(), nodeName);
            // downloads_container is taken from the downloadsSpace.xml uuid setting
            assertNotEquals(problematicDuplicateSystemDownloadNode.getId(), "downloads_container");

            Authentication previousAuth = AuthenticationUtil.getFullAuthentication();
            AuthenticationUtil.setFullyAuthenticatedUser(randomUsername);

            NodeRef beforeNodeRef;
            NodeRef afterNodeRef;
            Date beforeTime;

            try
            {
                beforeNodeRef = DOWNLOAD_SERVICE.createDownload(new NodeRef[] { level1Folder1 }, true);
                testNodes.addNodeRef(beforeNodeRef);
                waitForDownload(beforeNodeRef);

                beforeTime = new Date();

                afterNodeRef = DOWNLOAD_SERVICE.createDownload(new NodeRef[] { level1Folder2 }, true);
                testNodes.addNodeRef(afterNodeRef);
                waitForDownload(afterNodeRef);
            }
            finally
            {
                // assuming previous authentication is the system user
                AuthenticationUtil.setFullAuthentication(previousAuth);
            }

            moveDownloadedFileToProblematicFolder(fileFolderService, problematicDuplicateSystemDownloadNode, beforeNodeRef);

            deleteDownloadsAndCheckParameters(beforeNodeRef, afterNodeRef, beforeTime);
        }
        finally
        {
            cleanProblematicFolder(problematicDuplicateSystemDownloadNode);
        }
    }

    private void deleteDownloadsAndCheckParameters(NodeRef beforeNodeRef, NodeRef afterNodeRef, Date beforeTime)
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute()
            {
                DOWNLOAD_SERVICE.deleteDownloads(beforeTime, 1000, false);

                Assert.assertTrue(NODE_SERVICE.exists(beforeNodeRef));
                Assert.assertTrue(NODE_SERVICE.exists(afterNodeRef));

                DOWNLOAD_SERVICE.deleteDownloads(beforeTime, 1000, true);
                // because when we moved the file (with fileFolderService) the modified date is updated,
                // therefore none of the downloads files will be deleted
                Assert.assertTrue(NODE_SERVICE.exists(beforeNodeRef));
                Assert.assertTrue(NODE_SERVICE.exists(afterNodeRef));

                Date newBeforeTime = new Date();

                DOWNLOAD_SERVICE.deleteDownloads(newBeforeTime, 1000, true);
                // now both download files should be removed
                Assert.assertFalse(NODE_SERVICE.exists(beforeNodeRef));
                Assert.assertFalse(NODE_SERVICE.exists(afterNodeRef));

                return null;
            }
        }, false, true);
    }

    private void moveDownloadedFileToProblematicFolder(FileFolderService fileFolderService, NodeRef problematicDuplicateSystemDownloadNode,
        NodeRef beforeNodeRef)
    {
        FileInfo fileInfo = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<FileInfo>()
        {
            @Override
            public FileInfo execute()
            {
                try
                {
                    // now move the beforeNodeRef to the problematicDuplicateSystemDownloadNode
                    return fileFolderService.move(beforeNodeRef, problematicDuplicateSystemDownloadNode, null);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    fail(e.getMessage());
                }
                return null;
            }
        }, false, true);

        if (fileInfo == null)
        {
            fail("The node move should have succeeded");
        }
    }

    private String createRandomUser()
    {
        String randomUsername = TEST_USER_NAME + GUID.generate();
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute()
            {
                createUser(randomUsername);
                return null;
            }
        }, false, true);
        return randomUsername;
    }

    private NodeRef createProblematicDuplicateSystemNode(final QName childName, final NodeService nodeService, final Repository repositoryHelper)
    {
        return TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute()
            {
                return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<NodeRef>()
                {
                    @Override
                    public NodeRef doWork() throws Exception
                    {
                        NodeRef system = SystemNodeUtils.getSystemContainer(nodeService, repositoryHelper);

                        NodeRef container = nodeService.createNode(system, ContentModel.ASSOC_CHILDREN, childName, ContentModel.TYPE_CONTAINER)
                            .getChildRef();
                        nodeService.setProperty(container, ContentModel.PROP_NAME, childName.getLocalName());

                        return container;
                    }
                });
            }
        }, false, true);
    }

    private void cleanProblematicFolder(NodeRef problematicDuplicateSystemDownloadNode)
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute()
            {
                NODE_SERVICE.deleteNode(problematicDuplicateSystemDownloadNode);
                return null;
            }
        }, false, true);
    }

    @Test public void cancel() throws InterruptedException
    {
        // Initiate the download
        final NodeRef downloadNode = DOWNLOAD_SERVICE.createDownload(new NodeRef[] {rootFile, rootFolder},  true);
        Assert.assertNotNull(downloadNode);

        testNodes.addNodeRef(downloadNode);

        DOWNLOAD_SERVICE.cancelDownload(downloadNode);
        
        DownloadStatus status = getDownloadStatus(downloadNode);
        int retryCount = 0;
        while (status.getStatus() != Status.CANCELLED && retryCount < 5)
        {
            retryCount++;
            Thread.sleep(PAUSE_TIME);
            status = getDownloadStatus(downloadNode);
        }
        
        Assert.assertEquals(Status.CANCELLED, status.getStatus());
    }
    
    /**
     * This test verifies that a user is given the correct file, when it is checked. The user who checked out
     * the file should get the working copy, while any other user should get the default version.
     * @throws InterruptedException 
     */
    @Test public void workingCopies() throws InterruptedException
    {
        final Set<String> preCheckoutExpectedEntries = new TreeSet<String>();
        preCheckoutExpectedEntries.add("level1Folder2/");
        preCheckoutExpectedEntries.add("level1Folder2/level2File.txt");
        preCheckoutExpectedEntries.add("level1Folder2/fileToCheckout.txt");
        
        validateWorkingCopyFolder(preCheckoutExpectedEntries, level1Folder2, TEST_USER.getUsername());
        validateWorkingCopyFolder(preCheckoutExpectedEntries, level1Folder2, TEST_USER2.getUsername());

        Authentication previousAuth = AuthenticationUtil.getFullAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER.getUsername());
        NodeRef workingCopy;
        try
        {
            workingCopy = CHECK_OUT_CHECK_IN_SERVICE.checkout(fileToCheckout);
        }
        finally
        {
           AuthenticationUtil.setFullAuthentication(previousAuth);
        }
        
        try
        {
            validateWorkingCopyFolder(preCheckoutExpectedEntries, level1Folder2, TEST_USER2.getUsername());
            
            final Set<String> postCheckoutExpectedEntries = new TreeSet<String>();
            postCheckoutExpectedEntries.add("level1Folder2/");
            postCheckoutExpectedEntries.add("level1Folder2/level2File.txt");
            postCheckoutExpectedEntries.add("level1Folder2/fileToCheckout (Working Copy).txt");
            validateWorkingCopyFolder(postCheckoutExpectedEntries, level1Folder2, TEST_USER.getUsername());
        }
        finally
        {
            previousAuth = AuthenticationUtil.getFullAuthentication();
            AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER.getUsername());
            try
            {
                CHECK_OUT_CHECK_IN_SERVICE.checkin(workingCopy, null);
            }
            finally
            {
               AuthenticationUtil.setFullAuthentication(previousAuth);
            }
        }
        validateWorkingCopyFolder(preCheckoutExpectedEntries, level1Folder2, TEST_USER.getUsername());
        validateWorkingCopyFolder(preCheckoutExpectedEntries, level1Folder2, TEST_USER2.getUsername());
    }

    private void validateWorkingCopyFolder(final Set<String> expectedEntries, final NodeRef folder, final String userID) throws InterruptedException
    {
        Authentication previousAuthentication = AuthenticationUtil.getFullAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(userID);
        try
        {
            final NodeRef downloadNode = DOWNLOAD_SERVICE.createDownload(new NodeRef[] {folder},  true);
            testNodes.addNodeRef(downloadNode);
            
            waitForDownload(downloadNode);
            
            validateEntries(getEntries(downloadNode), expectedEntries, true);
        }
        finally
        {
            AuthenticationUtil.setFullAuthentication(previousAuthentication);
        }
    }

    // ALF-18453
    @Test
    public void deleteAssociationAfterDownload() throws Exception
    {
        final NodeRef nodeRef;

        nodeRef = DOWNLOAD_SERVICE.createDownload(new NodeRef[] { level1Folder1 }, true);
        testNodes.addNodeRef(nodeRef);
        waitForDownload(nodeRef);

        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                try
                {
                    // remove the target associations
                    final List<AssociationRef> assocsList = NODE_SERVICE.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
                    Assert.assertEquals(1, assocsList.size());

                    NODE_SERVICE.removeAssociation(assocsList.get(0).getSourceRef(), assocsList
                                .get(0).getTargetRef(), DownloadModel.ASSOC_REQUESTED_NODES);

                    INTEGRITY_CHECKER.checkIntegrity();
                }
                catch (Exception ex)
                {
                    fail("The association should have been removed successfully from the target node.");
                }
                return null;
            }
        });
    }

    // you need to clean the DB if you make changes to downloadsSpace.xml
    @Test
    public void checkNormalUsersCanNotAccessSysDownloadFolder() throws Exception
    {
        final NodeRef containerFolderForDownloads = DOWNLOAD_STORAGE.getOrCreateDowloadContainer();

        String randomUsername = createRandomUser();

        Authentication previousAuth = AuthenticationUtil.getFullAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(randomUsername);
        try
        {
            try
            {
                NODE_SERVICE.getProperties(containerFolderForDownloads);
                fail("The normal user should not be able to read the sys:Download node properties");
            }
            catch (AccessDeniedException e)
            {
                // we expect this to happen.
                // normal users should not be able to read the properties of this sys:download container folder
            }
            try
            {
                NODE_SERVICE.getChildAssocs(containerFolderForDownloads);
                fail("The normal user should not be able to list the sys:Download node children");
            }
            catch (AccessDeniedException e)
            {
                // we expect this to happen.
                // normal users should not be able to list the children of this sys:download container folder
            }
        }
        finally
        {
            // assuming previous authentication is the system user
            AuthenticationUtil.setFullAuthentication(previousAuth);
        }
    }

    private void createUser(String username)
    {
        PersonService personService = APP_CONTEXT_INIT.getApplicationContext().getBean("PersonService", PersonService.class);

        MutableAuthenticationService mutableAuthenticationService = APP_CONTEXT_INIT.getApplicationContext()
            .getBean("authenticationService", MutableAuthenticationService.class);
        if (mutableAuthenticationService.authenticationExists(username))
        {
            return;
        }

        mutableAuthenticationService.createAuthentication(username, "password".toCharArray());

        PropertyMap personProperties = new PropertyMap();
        personProperties.put(ContentModel.PROP_USERNAME, username);
        personProperties.put(ContentModel.PROP_AUTHORITY_DISPLAY_NAME, "title" + username);
        personProperties.put(ContentModel.PROP_FIRSTNAME, "firstName");
        personProperties.put(ContentModel.PROP_LASTNAME, "lastName");
        personProperties.put(ContentModel.PROP_EMAIL, username + "@example.com");
        personProperties.put(ContentModel.PROP_JOBTITLE, "jobTitle");
        personService.createPerson(personProperties);
    }
}
