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
package org.alfresco.repo.web.scripts.search;

import java.io.IOException;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;


@Ignore("This test doesn't pass on HEAD-BUG-FIX and isn't included in a test suite.")
public class AdvancedSearchTest extends BaseWebScriptTest
{
    private static final String TEST_FILE_NAME1 = "qwe iop.txt";
    private static final String TEST_FILE_NAME2 = "qwe yu iop.txt";
    private static final String TEST_IN_SITES_FILE_NAME = "qwesiteiop";
    private static final String SEARCH_NAME = "qwe iop";
    private static final String SEARCH_IN_SITES = "qwesiteiop";
    private static final String TEST_FOLDER = "test_folder-" + System.currentTimeMillis();
    
    private TransactionService transactionService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private AuthenticationComponent authenticationComponent;
    private NodeRef testNodeRef1;
    private NodeRef testNodeRef2;
    private NodeRef folderNodeRef;
    private NodeRef rootNodeRef;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        this.transactionService = (TransactionService) getServer().getApplicationContext().getBean("TransactionService");
        this.nodeService = (NodeService) getServer().getApplicationContext().getBean("NodeService");
        this.fileFolderService = (FileFolderService)getServer().getApplicationContext().getBean("FileFolderService");
        this.authenticationComponent = (AuthenticationComponent) getServer().getApplicationContext().getBean("authenticationComponent");
        this.searchService = (SearchService) getServer().getApplicationContext().getBean("SearchService");
        this.namespaceService = (NamespaceService) getServer().getApplicationContext().getBean("namespaceService");

        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        this.rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        
        List<NodeRef> results = searchService.selectNodes(this.rootNodeRef, "/app:company_home", null, namespaceService, false);
        if (results.size() == 0)
        {
            throw new AlfrescoRuntimeException("Can't find /app:company_home");
        }
        
        NodeRef companyHomeNodeRef = results.get(0);
        
        folderNodeRef = fileFolderService.create(companyHomeNodeRef, TEST_FOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
        
        testNodeRef1 = fileFolderService.create(folderNodeRef, TEST_FILE_NAME1, ContentModel.TYPE_CONTENT).getNodeRef();
        testNodeRef2 = fileFolderService.create(folderNodeRef, TEST_FILE_NAME2, ContentModel.TYPE_CONTENT).getNodeRef();


        assertNotNull(testNodeRef1);
        assertNotNull(testNodeRef2);
    }
    
    private void deleteNodeIfExists(NodeRef nodeRef)
    {
        if (nodeService.exists(nodeRef))
        {
            nodeService.deleteNode(nodeRef);
        }
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        RetryingTransactionCallback<Void> deleteCallback = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                deleteNodeIfExists(testNodeRef1);
                deleteNodeIfExists(testNodeRef2);
                deleteNodeIfExists(folderNodeRef);
                return null;
            }
            
        };
        this.transactionService.getRetryingTransactionHelper().doInTransaction(deleteCallback);
        this.authenticationComponent.clearCurrentSecurityContext();
    }
    
    public void testSearchFile() throws IOException, JSONException
    {
        //Name search without quotes
        String url = "/slingshot/search?site=&term=&tag=&maxResults=251&sort=&query={\"prop_cm_name\":\"" + SEARCH_NAME + "\",\"datatype\":\"cm:content\"}&repo=true&rootNode=alfresco://company/home";
        Response res =  sendRequest(new GetRequest(url), Status.STATUS_OK);
        JSONObject result = new JSONObject(res.getContentAsString());
        assertEquals(2, result.getInt("totalRecords"));
        
        url = "/slingshot/search?site=&term=&tag=&maxResults=251&sort=&query={\"prop_cm_name\":\"\\\"" + SEARCH_NAME + "\\\"\",\"datatype\":\"cm:content\"}&repo=true&rootNode=alfresco://company/home";
        res =  sendRequest(new GetRequest(url), Status.STATUS_OK);
        result = new JSONObject(res.getContentAsString());
        assertEquals(1, result.getInt("totalRecords"));
    }

    public void testSearchInSites() throws IOException, JSONException
    {
    	List<NodeRef> results = searchService.selectNodes(this.rootNodeRef, "/app:company_home/st:sites", null, namespaceService, false);
        if (results.size() == 0)
        {
            throw new AlfrescoRuntimeException("Can't find /app:company_home/st:sites");
        }
        
        NodeRef sitesNodeRef = results.get(0);
        
        long time = System.currentTimeMillis();
        
        NodeRef sitefolderNodeRef = fileFolderService.create(sitesNodeRef, TEST_FOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
        NodeRef nodeRef = fileFolderService.create(sitefolderNodeRef, TEST_IN_SITES_FILE_NAME + time, ContentModel.TYPE_CONTENT).getNodeRef();
       // String url = "/slingshot/search?site=&term=" + SEARCH_NAME + "&tag=&maxResults=251&sort=&query=&repo=false&rootNode=alfresco%3A%2F%2Fcompany%2Fhome&pageSize=50&startIndex=0";
        String url = "/slingshot/search?site=&term=&tag=&maxResults=251&sort=&query={\"prop_cm_name\":\"" + SEARCH_IN_SITES + time + "\",\"datatype\":\"cm:content\"}&repo=true&rootNode=alfresco://company/home";
        Response res =  sendRequest(new GetRequest(url), Status.STATUS_OK);
        JSONObject result = new JSONObject(res.getContentAsString());
        assertEquals(1, result.getInt("totalRecords"));
        
        deleteNodeIfExists(nodeRef);
        deleteNodeIfExists(sitefolderNodeRef);
        
    }

}
