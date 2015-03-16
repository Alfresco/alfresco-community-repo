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
package org.alfresco.repo.model.filefolder;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.content.filestore.SpoofedTextContentReader;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.repo.web.scripts.model.filefolder.FileFolderLoaderPost;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Remote FileFolderLoader testing
 * 
 * @author Derek Hulley
 * @since 5.1
 */
public class RemoteFileFolderLoaderTest  extends BaseWebScriptTest
{
    public static final String URL = "/api/model/filefolder/load";

    private Repository repositoryHelper;
    private NodeService nodeService;
    private TransactionService transactionService;
    private FileFolderService fileFolderService;
    private String sharedHomePath;
    private NodeRef loadHomeNodeRef;
    private String loadHomePath;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.repositoryHelper = (Repository)getServer().getApplicationContext().getBean("repositoryHelper");
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("nodeService");
        this.transactionService = (TransactionService) getServer().getApplicationContext().getBean("TransactionService");
        this.fileFolderService = (FileFolderService) getServer().getApplicationContext().getBean("FileFolderService");

        // Get the path of the shared folder home
        final NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
        final NodeRef sharedHomeNodeRef = repositoryHelper.getSharedHome();
        RetryingTransactionCallback<NodeRef> createFolderWork = new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                List<FileInfo> sharedHomeFileInfos = fileFolderService.getNamePath(companyHomeNodeRef, sharedHomeNodeRef);
                sharedHomePath = "/" + sharedHomeFileInfos.get(0).getName();

                String folderName = UUID.randomUUID().toString();
                // Create a folder
                FileInfo folderInfo = fileFolderService.create(sharedHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
                loadHomePath = sharedHomePath + "/" + folderName;
                // Done
                return folderInfo.getNodeRef();
            }
        };
        AuthenticationUtil.pushAuthentication();            // Will be cleared later
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        loadHomeNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(createFolderWork);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        RetryingTransactionCallback<Void> deleteFolderWork = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                fileFolderService.delete(loadHomeNodeRef);
                // Done
                return null;
            }
        };
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(deleteFolderWork);
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * Load with no folder path
     */
    public void testLoad_noFolderPath() throws Exception
    {
        JSONObject body = new JSONObject();
        
        sendRequest(
                new PostRequest(URL,  body.toString(), "application/json"),
                Status.STATUS_BAD_REQUEST,
                "bmarley");
    }
    
    /**
     * Load with defaults
     */
    @SuppressWarnings("unchecked")
    public void testLoad_default_default() throws Exception
    {
        JSONObject body = new JSONObject();
        body.put(FileFolderLoaderPost.KEY_FOLDER_PATH, loadHomePath);
        
        Response response = sendRequest(
                new PostRequest(URL,  body.toString(), "application/json"),
                Status.STATUS_OK,
                "bmarley");
        assertEquals("{\"count\":100}", response.getContentAsString());
        
        // Check file(s)
        assertEquals(100, nodeService.countChildAssocs(loadHomeNodeRef, true));
    }
    
    /**
     * Load 15 files with default sizes
     */
    @SuppressWarnings("unchecked")
    public void testLoad_15_default() throws Exception
    {
        JSONObject body = new JSONObject();
        body.put(FileFolderLoaderPost.KEY_FOLDER_PATH, loadHomePath);
        body.put(FileFolderLoaderPost.KEY_FILE_COUNT, 15);
        body.put(FileFolderLoaderPost.KEY_FILES_PER_TXN, 10);
        
        Response response = null;
        try
        {
            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setFullyAuthenticatedUser("hhoudini");
            response = sendRequest(
                    new PostRequest(URL,  body.toString(), "application/json"),
                    Status.STATUS_OK,
                    "hhoudini");
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
        assertEquals("{\"count\":15}", response.getContentAsString());
        
        // Check file(s)
        assertEquals(15, nodeService.countChildAssocs(loadHomeNodeRef, true));
        // Size should be default
        List<FileInfo> fileInfos = fileFolderService.list(loadHomeNodeRef);
        for (FileInfo fileInfo : fileInfos)
        {
            NodeRef fileNodeRef = fileInfo.getNodeRef();
            ContentReader reader = fileFolderService.getReader(fileNodeRef);
            // Expect spoofing by default
            assertTrue(reader.getContentUrl().startsWith(FileContentStore.SPOOF_PROTOCOL));
            assertTrue(
                    "Default file size not correct: " + reader,
                    FileFolderLoaderPost.DEFAULT_MIN_FILE_SIZE < reader.getSize() &&
                        reader.getSize() < FileFolderLoaderPost.DEFAULT_MAX_FILE_SIZE);
            // Check creator
            assertEquals("hhoudini", nodeService.getProperty(fileNodeRef, ContentModel.PROP_CREATOR));
            // We also expect the default language description to be present
            String description = (String) nodeService.getProperty(fileNodeRef, ContentModel.PROP_DESCRIPTION);
            assertNotNull("No description", description);
            assertEquals("Description length incorrect: ", 128L, description.getBytes("UTF-8").length);
        }
    }
        
    /**
     * Load 15 files; 1K size; 1 document sample; force binary storage
     */
    @SuppressWarnings("unchecked")
    public void testLoad_15_16bytes() throws Exception
    {
        JSONObject body = new JSONObject();
        body.put(FileFolderLoaderPost.KEY_FOLDER_PATH, loadHomePath);
        body.put(FileFolderLoaderPost.KEY_MIN_FILE_SIZE, 16L);
        body.put(FileFolderLoaderPost.KEY_MAX_FILE_SIZE, 16L);
        body.put(FileFolderLoaderPost.KEY_MAX_UNIQUE_DOCUMENTS, 1L);
        body.put(FileFolderLoaderPost.KEY_FORCE_BINARY_STORAGE, Boolean.TRUE);
        
        Response response = null;
        try
        {
            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setFullyAuthenticatedUser("maggi");
            response = sendRequest(
                    new PostRequest(URL,  body.toString(), "application/json"),
                    Status.STATUS_OK,
                    "maggi");
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
        assertEquals("{\"count\":100}", response.getContentAsString());
        
        // Check file(s)
        assertEquals(100, nodeService.countChildAssocs(loadHomeNodeRef, true));
        
        // Consistent binary text
        String contentUrlCheck = SpoofedTextContentReader.createContentUrl(Locale.ENGLISH, 0L, 16L);
        ContentReader readerCheck = new SpoofedTextContentReader(contentUrlCheck);
        String textCheck = readerCheck.getContentString();
        
        // Size should be default
        List<FileInfo> fileInfos = fileFolderService.list(loadHomeNodeRef);
        for (FileInfo fileInfo : fileInfos)
        {
            NodeRef fileNodeRef = fileInfo.getNodeRef();
            ContentReader reader = fileFolderService.getReader(fileNodeRef);
            // Expect storage in store
            assertTrue(reader.getContentUrl().startsWith(FileContentStore.STORE_PROTOCOL));
            // Check text
            String text = reader.getContentString();
            assertEquals("Text not the same.", textCheck, text);
        }
    }
}
