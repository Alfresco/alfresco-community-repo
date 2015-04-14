/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.webdav;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for the {@link MoveMethod} class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class MoveMethodTest
{
    private static ApplicationContext ctx;

    private MoveMethod moveMethod;
    private @Mock WebDAVHelper davHelper;
    private MockHttpServletRequest req;
    private MockHttpServletResponse resp;
    private NodeRef rootNode;
    private @Mock FileFolderService mockFileFolderService;
    private @Mock WebDAVLockService davLockService;
    private String destPath;
    private String sourcePath;
    private FileInfo sourceFileInfo;
    private NodeRef sourceParentNodeRef;
    private NodeRef destParentNodeRef;
    private NodeRef sourceNodeRef;

    private SearchService searchService;
    private FileFolderService fileFolderService;
    private NodeService nodeService;
    private TransactionService transactionService;
    private ContentService contentService;
    private WebDAVHelper webDAVHelper;

    private NodeRef companyHomeNodeRef;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext(new String[]
        {
            "classpath:alfresco/application-context.xml", "classpath:alfresco/web-scripts-application-context.xml",
            "classpath:alfresco/remote-api-context.xml"
        });
    }

    @After
    public void tearDown()
    {
        moveMethod = null;
        req = null;
        resp = null;

        AuthenticationUtil.clearCurrentSecurityContext();
    }

    @Before
    public void setUp() throws Exception
    {
        req = new MockHttpServletRequest();
        resp = new MockHttpServletResponse();
        rootNode = new NodeRef("workspace://SpacesStore/node1");
        moveMethod = new MoveMethod()
        {
            @Override
            protected LockInfo checkNode(FileInfo fileInfo, boolean ignoreShared, boolean lockMethod)
                        throws WebDAVServerException
            {
                return new LockInfoImpl();
            }

            @Override
            protected LockInfo checkNode(FileInfo fileInfo) throws WebDAVServerException
            {
                return new LockInfoImpl();
            }
        };
        moveMethod.setDetails(req, resp, davHelper, rootNode);
        
        sourceFileInfo = Mockito.mock(FileInfo.class);
        when(sourceFileInfo.isFolder()).thenReturn(true);
        
        destPath = "/path/to/dest.doc";
        moveMethod.m_strDestinationPath = destPath;
        
        sourcePath = "/path/to/source.doc";
        moveMethod.m_strPath = sourcePath;
        
        when(davHelper.getFileFolderService()).thenReturn(mockFileFolderService);
        
        List<String> sourcePathSplit = Arrays.asList("path", "to", "source.doc");
        when(davHelper.splitAllPaths(sourcePath)).thenReturn(sourcePathSplit);
        
        
        List<String> destPathSplit = Arrays.asList("path", "to", "dest.doc");
        when(davHelper.splitAllPaths(destPath)).thenReturn(destPathSplit);
        
        
        when(mockFileFolderService.resolveNamePath(rootNode, sourcePathSplit)).thenReturn(sourceFileInfo);
        
        FileInfo destFileInfo = Mockito.mock(FileInfo.class);
        when(mockFileFolderService.resolveNamePath(rootNode, destPathSplit)).thenReturn(destFileInfo);
        
        sourceParentNodeRef = new NodeRef("workspace://SpacesStore/parent");
        destParentNodeRef = new NodeRef("workspace://SpacesStore/parent");
        
        
        sourceNodeRef = new NodeRef("workspace://SpacesStore/sourcefile");
        
        when(davHelper.getLockService()).thenReturn(davLockService);

        searchService = ctx.getBean("SearchService", SearchService.class);
        fileFolderService = ctx.getBean("FileFolderService", FileFolderService.class);
        nodeService = ctx.getBean("NodeService", NodeService.class);
        transactionService = ctx.getBean("transactionService", TransactionService.class);
        contentService = ctx.getBean("contentService", ContentService.class);
        webDAVHelper = ctx.getBean("webDAVHelper", WebDAVHelper.class);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        companyHomeNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                // find "Company Home"
                StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
                ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home\"");
                NodeRef result = resultSet.getNodeRef(0);
                resultSet.close();

                return result;
            }
        });
    }

    
    @Test
    public void canRenameFolders() throws Exception
    {
        moveMethod.moveOrCopy(sourceNodeRef, sourceParentNodeRef, destParentNodeRef, "dest.doc");
        
        verify(mockFileFolderService).rename(sourceNodeRef, "dest.doc");
        verify(davLockService).unlock(sourceNodeRef);
        verify(mockFileFolderService, never()).create(destParentNodeRef, "dest.doc", ContentModel.TYPE_CONTENT);
    }
    
    
    @Test
    public void canRenameFoldersWhenNewNameMatchesShufflePattern() throws Exception
    {
        when(davHelper.isRenameShuffle(destPath)).thenReturn(true);
        when(davHelper.isRenameShuffle(sourcePath)).thenReturn(false);
        
        // Test: Perform the rename
        moveMethod.moveOrCopy(sourceNodeRef, sourceParentNodeRef, destParentNodeRef, "dest.doc");
        
        
        verify(mockFileFolderService).rename(sourceNodeRef, "dest.doc");
        verify(davLockService).unlock(sourceNodeRef);
        verify(mockFileFolderService, never()).create(destParentNodeRef, "dest.doc", ContentModel.TYPE_CONTENT);
    }

    @Test
    public void testMNT_13144() throws Exception
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                FileInfo dwgFI = null;
                String fileName = "source-" + GUID.generate();
                String sourceFileName = fileName + ".dwg";

                FileInfo atmpFI = null;
                String destFileName = "atmp1234";

                FileInfo tmpFI = null;
                String tmpFileName = fileName + ".tmp";

                String bakFileName = fileName + ".bak";

                String content = "Some dwg content.";
                String tmpContent = "Change dwg content.";

                try
                {
                    dwgFI = fileFolderService.create(companyHomeNodeRef, sourceFileName, ContentModel.TYPE_CONTENT);
                    ContentWriter writer = contentService.getWriter(dwgFI.getNodeRef(), ContentModel.PROP_CONTENT, true);
                    writer.setMimetype(MimetypeMap.MIMETYPE_APP_DWG);
                    writer.setEncoding("UTF-8");
                    writer.putContent(content);

                    tmpFI = fileFolderService.create(companyHomeNodeRef, tmpFileName, ContentModel.TYPE_CONTENT);
                    writer = contentService.getWriter(tmpFI.getNodeRef(), ContentModel.PROP_CONTENT, true);
                    writer.setMimetype(MimetypeMap.MIMETYPE_APP_DWG);
                    writer.setEncoding("UTF-8");
                    writer.putContent(tmpContent);

                    atmpFI = fileFolderService.create(companyHomeNodeRef, destFileName, ContentModel.TYPE_CONTENT);

                    sourcePath = "/" + sourceFileName;
                    moveMethod.m_strPath = sourcePath;

                    destPath = "/" + destFileName;
                    moveMethod.m_strDestinationPath = destPath;

                    List<String> sourcePathSplit = Arrays.asList(sourceFileName);
                    when(davHelper.splitAllPaths(sourcePath)).thenReturn(sourcePathSplit);

                    when(mockFileFolderService.resolveNamePath(rootNode, sourcePathSplit)).thenReturn(dwgFI);
                    when(davHelper.isRenameShuffle(destPath)).thenReturn(true);
                    when(davHelper.isRenameShuffle(sourcePath)).thenReturn(false);

                    when(mockFileFolderService.create(companyHomeNodeRef, destFileName, ContentModel.TYPE_CONTENT)).thenReturn(atmpFI);

                    ServiceRegistry mockServiceRegistry = Mockito.mock(ServiceRegistry.class);
                    when(davHelper.getServiceRegistry()).thenReturn(mockServiceRegistry);
                    when(davHelper.getServiceRegistry().getContentService()).thenReturn(contentService);

                    // move webdav method dwg copy to atmp1234
                    moveMethod.moveOrCopy(dwgFI.getNodeRef(), companyHomeNodeRef, companyHomeNodeRef, destFileName);

                    ContentReader reader = contentService.getReader(atmpFI.getNodeRef(), ContentModel.PROP_CONTENT);
                    assertNotNull(reader);
                    assertEquals(content, reader.getContentString());

                    // move webdav method atmp1234 rename to bak
                    sourcePath = "/" + destFileName;
                    moveMethod.m_strPath = sourcePath;

                    destPath = "/" + bakFileName;
                    moveMethod.m_strDestinationPath = destPath;

                    sourcePathSplit = Arrays.asList(destFileName);
                    when(davHelper.splitAllPaths(sourcePath)).thenReturn(sourcePathSplit);

                    when(mockFileFolderService.resolveNamePath(rootNode, sourcePathSplit)).thenReturn(atmpFI);
                    when(davHelper.isRenameShuffle(destPath)).thenReturn(true);
                    when(davHelper.isRenameShuffle(sourcePath)).thenReturn(true);

                    moveMethod.moveOrCopy(atmpFI.getNodeRef(), companyHomeNodeRef, companyHomeNodeRef, bakFileName);

                    verify(mockFileFolderService).rename(atmpFI.getNodeRef(), bakFileName);
                    verify(davLockService).unlock(atmpFI.getNodeRef());
                    verify(mockFileFolderService, never()).create(destParentNodeRef, bakFileName, ContentModel.TYPE_CONTENT);

                    // move webdav method tmp copy content to dwg
                    sourcePath = "/" + tmpFileName;
                    moveMethod.m_strPath = sourcePath;

                    destPath = "/" + sourceFileName;
                    moveMethod.m_strDestinationPath = destPath;

                    sourcePathSplit = Arrays.asList(tmpFileName);
                    when(davHelper.splitAllPaths(sourcePath)).thenReturn(sourcePathSplit);

                    when(mockFileFolderService.resolveNamePath(rootNode, sourcePathSplit)).thenReturn(tmpFI);
                    when(davHelper.isRenameShuffle(destPath)).thenReturn(false);
                    when(davHelper.isRenameShuffle(sourcePath)).thenReturn(true);

                    moveMethod.moveOrCopy(atmpFI.getNodeRef(), companyHomeNodeRef, companyHomeNodeRef, bakFileName);

                    reader = contentService.getReader(dwgFI.getNodeRef(), ContentModel.PROP_CONTENT);
                    assertNotNull(reader);
                    assertEquals(tmpContent, reader.getContentString());
                }
                finally
                {
                    if (dwgFI != null)
                    {
                        nodeService.deleteNode(dwgFI.getNodeRef());
                    }

                    if (atmpFI != null)
                    {
                        nodeService.deleteNode(atmpFI.getNodeRef());
                    }
                }

                return null;
            }
        });
    }
    
    @Test(expected=AccessDeniedException.class)
    public void testMNT_10380_ThrowAccessDeniedExceptionWhenUserLacksPermissions() throws Exception
    {
        when(mockFileFolderService.rename(sourceNodeRef, "dest.doc")).
            thenThrow(new AccessDeniedException("Access denied in test by mockFileFolderService"));
        
        moveMethod.moveOrCopy(sourceNodeRef, sourceParentNodeRef, destParentNodeRef, "dest.doc");
    }
    
    @Test
    public void testMNT_9662()
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                // create test folder with name that doesn't match getDAVHelper().isRenameShuffle()
                FileInfo testFileInfo = fileFolderService.create(companyHomeNodeRef, "folder-" + GUID.generate().substring(29), ContentModel.TYPE_FOLDER);

                req = new MockHttpServletRequest(WebDAV.METHOD_MOVE, "/alfresco/webdav/" + testFileInfo.getName());
                resp = new MockHttpServletResponse();
                req.setServerPort(8080);
                req.setContextPath("/alfresco");
                req.setServletPath("/webdav");

                moveMethod = new MoveMethod();
                moveMethod.setDetails(req, resp, webDAVHelper, companyHomeNodeRef);

                // generate new name that matches getDAVHelper().isRenameShuffle()
                String newName = GUID.generate().substring(28);
                req.addHeader(WebDAV.HEADER_DESTINATION, "http://localhost:8080/alfresco/webdav/" + newName);

                try
                {
                    moveMethod.execute();

                    assertTrue(nodeService.exists(testFileInfo.getNodeRef()));
                    assertEquals(newName, nodeService.getProperty(testFileInfo.getNodeRef(), ContentModel.PROP_NAME));
                }
                catch (WebDAVServerException e)
                {
                    fail("Fail to rename folder: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                }
                finally
                {
                    nodeService.deleteNode(testFileInfo.getNodeRef());
                }
                return null;
            }
        });
    }
    
    @Test
    public void testMNT_6480()
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                // create test file with name that doesn't match getDAVHelper().isRenameShuffle()
                String originalFileName = "content-" + GUID.generate() + ".txt";
                FileInfo testFileInfo = fileFolderService.create(companyHomeNodeRef, originalFileName, ContentModel.TYPE_CONTENT);
                
                // rename source file to file with upper case name
                String newFileName = originalFileName.toUpperCase();

                req = new MockHttpServletRequest(WebDAV.METHOD_MOVE, "/alfresco/webdav/" + testFileInfo.getName());
                req.setServerPort(8080);
                req.setContextPath("/alfresco");
                req.setServletPath("/webdav");
                req.addHeader(WebDAV.HEADER_DESTINATION, "http://localhost:8080/alfresco/webdav/" + newFileName);
                
                resp = new MockHttpServletResponse();

                moveMethod = new MoveMethod();
                moveMethod.setDetails(req, resp, webDAVHelper, companyHomeNodeRef);

                try
                {
                    moveMethod.execute();
                    
                    // MNT-6480 - File should be renamed but not deleted
                    assertTrue(nodeService.exists(testFileInfo.getNodeRef()));
                    assertEquals(newFileName, nodeService.getProperty(testFileInfo.getNodeRef(), ContentModel.PROP_NAME));
                }
                catch (WebDAVServerException e)
                {
                    fail("Fail to rename node: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                }
                finally
                {
                    nodeService.deleteNode(testFileInfo.getNodeRef());
                }
                return null;
            }
        });
    }
    
    @Test
    public void testMNT_9777()
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                // create test file with name that does match getDAVHelper().isRenameShuffle()
                String originalFileName = "tempfile-" + GUID.generate() + ".tmp";
                
                // destination within same folder
                String newFileName = "destfile-" + GUID.generate() + ".txt";
                
                FileInfo sourceFileInfo = fileFolderService.create(companyHomeNodeRef, originalFileName, ContentModel.TYPE_CONTENT);
                FileInfo newFileInfo = fileFolderService.create(companyHomeNodeRef, newFileName, ContentModel.TYPE_CONTENT);

                String newContent = GUID.generate();
                ContentWriter writer;
                writer = contentService.getWriter(sourceFileInfo.getNodeRef(), ContentModel.PROP_CONTENT, true);
                writer.putContent(newContent);
                
                req = new MockHttpServletRequest(WebDAV.METHOD_MOVE, "/alfresco/webdav/" + sourceFileInfo.getName());
                resp = new MockHttpServletResponse();
                req.setServerPort(8080);
                req.setContextPath("/alfresco");
                req.setServletPath("/webdav");

                moveMethod = new MoveMethod();
                moveMethod.setDetails(req, resp, webDAVHelper, companyHomeNodeRef);

                String destPath = "http://localhost:8080/alfresco/webdav/" + newFileName;
                req.addHeader(WebDAV.HEADER_DESTINATION, destPath);

                try
                {
                    moveMethod.execute();
                    
                    // MNT-9777 - Source node should be deleted
                    assertTrue(!nodeService.exists(sourceFileInfo.getNodeRef()));
                    
                    // Content should be updated
                    ContentReader reader = contentService.getReader(newFileInfo.getNodeRef(), ContentModel.PROP_CONTENT);
                    assertEquals(newContent, reader.getContentString());
                }
                catch (WebDAVServerException e)
                {
                    fail("Fail to move node: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                }
                finally
                {
                    nodeService.deleteNode(newFileInfo.getNodeRef());
                }
                return null;
            }
        });
    }
}
