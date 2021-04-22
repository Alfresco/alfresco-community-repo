/*
 * Copyright 2005 - 2020 Alfresco Software Limited.
 *
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of the paid license agreement will prevail.
 * Otherwise, the software is provided under the following open source license terms:
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
package org.alfresco.slingshot.documentlibrary;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * JUnit test for folder-templates API
 * 
 * @author alex.mukha
 * @since 4.2.4
 */
public class FolderTemplateTest  extends BaseWebScriptTest
{
    private AuthenticationComponent authenticationComponent;
    private Repository repositoryHelper;
    private NodeService nodeService;
    private TransactionService transactionService;
    private SearchService searchService;
    private FileFolderService fileFolderService;
    private NodeRef companyHome;
    private NodeRef template;
    private NodeRef destination;
    private String templateName;
    private String destinationName;
    private UserTransaction txn;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.repositoryHelper = (Repository)getServer().getApplicationContext().getBean("repositoryHelper");
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
        this.transactionService = (TransactionService) getServer().getApplicationContext().getBean("TransactionService");
        this.searchService = (SearchService) getServer().getApplicationContext().getBean("SearchService");
        this.fileFolderService = (FileFolderService) getServer().getApplicationContext().getBean("FileFolderService");
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        txn = transactionService.getUserTransaction();
        txn.begin();
        
        companyHome = this.repositoryHelper.getCompanyHome();
        
        // Create template folder
        Map<QName, Serializable> propsTemplate = new HashMap<QName, Serializable>(1);
        templateName = "templateFolder" + GUID.generate();
        propsTemplate.put(ContentModel.PROP_NAME, templateName);
        template = nodeService.createNode(companyHome, ContentModel.ASSOC_CHILDREN, QName.createQName(templateName), ContentModel.TYPE_FOLDER, propsTemplate).getChildRef();   
        
        // Create destination
        Map<QName, Serializable> propsDestination = new HashMap<QName, Serializable>(1);
        destinationName = "destinationFolder" + GUID.generate();
        propsTemplate.put(ContentModel.PROP_NAME, destinationName);
        destination = nodeService.createNode(companyHome, ContentModel.ASSOC_CHILDREN, QName.createQName(destinationName), ContentModel.TYPE_FOLDER, propsDestination).getChildRef();  
        
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        txn.rollback();
    }
    
    /**
     * Test for MNT-11909
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void testFolderTemplatesPost() throws Exception
    {
        String url = "/slingshot/doclib/folder-templates";
        String newName = "FolderName" + GUID.generate();
        String newDescription = "FolderDescription" + GUID.generate();
        String newTitle = "FolderTitle" + GUID.generate();
        
        JSONObject body = new JSONObject();
        body.put("sourceNodeRef", template.toString());
        body.put("parentNodeRef", destination.toString());
        body.put("prop_cm_name", newName);
        body.put("prop_cm_description", newDescription);
        body.put("prop_cm_title", newTitle);
        
        Response response = sendRequest(new PostRequest(url,  body.toString(), "application/json"), Status.STATUS_OK);
        
        // Check the new folder
        String newFolderQuery = "/app:company_home/" + destinationName + "/" + newName;
        ResultSet result = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, newFolderQuery);
        if (result.length() == 0)
        {
            fail("The folder with name " + newName + " was not created in " + destinationName);
        }
        FileInfo newFolder = fileFolderService.getFileInfo(result.getRow(0).getNodeRef());
        assertNotNull("The folder is not found.", newFolder);
        assertTrue("The node should be a folder.", newFolder.isFolder());
        assertEquals("The folder's name should be " + newName + 
                ", but was " + newFolder.getName(),
                newName, newFolder.getName());
        assertEquals("The folder's description should be " + newDescription +
                ", but was " + newFolder.getProperties().get(ContentModel.PROP_DESCRIPTION),
                newDescription, newFolder.getProperties().get(ContentModel.PROP_DESCRIPTION));
        assertEquals("The folder's title should be " + newTitle +
                ", but was " + newFolder.getProperties().get(ContentModel.PROP_TITLE),
                newTitle, newFolder.getProperties().get(ContentModel.PROP_TITLE));
        
        // check the response
        JSONParser jsonParser = new JSONParser();
        Object contentJsonObject = jsonParser.parse(response
                .getContentAsString());
        JSONObject jsonData = (JSONObject) contentJsonObject;
        String persistedObject = (String) jsonData.get("persistedObject");
        assertEquals("The response's persistedObject should be "
                + newFolder.getNodeRef().toString() + " but it was "
                + persistedObject, newFolder.getNodeRef().toString(),
                persistedObject);
    }
}
