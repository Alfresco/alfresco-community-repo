/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.quickshare;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * This class tests QuickShare REST API
 * 
 * @author janv
 * @since Cloud/4.1
 */
public class QuickShareRestApiTest extends BaseWebScriptTest
{
    private static final String USER_ONE = "UserOne";
    private static final String USER_TWO = "UserTwo";
    
    private final static String SHARE_URL = "/api/internal/shared/share/{node_ref_3}";
    
    private final static String UNSHARE_URL = "/api/internal/shared/unshare/{shared_id}";
    private final static String SHARE_METADATA_URL = "/api/internal/shared/node/{shared_id}/metadata";
    private final static String SHARE_CONTENT_URL = "/api/internal/shared/node/{shared_id}/content";
    private final static String SHARE_CONTENT_THUMBNAIL_URL = "/api/internal/shared/node/{shared_id}/content/thumbnails/{thumbnailname}?c=force";
    
    // note: node_ref_3 => three segments, eg. store_protocol/store_id/node_uuid
    private final static String AUTH_METADATA_URL = "/api/node/{node_ref_3}/metadata";
    private final static String AUTH_CONTENT_URL = "/api/node/{node_ref_3}/content";
    private final static String AUTH_CONTENT_THUMBNAIL_URL = "/api/node/{node_ref_3}/content/thumbnails/{thumbnailname}?c=force";
    
    private static final String APPLICATION_JSON = "application/json";
    
    private NodeRef testNode;
    private final static String TEST_NAME = "test node";
    private final static String TEST_CONTENT = "test content";
    private final static String TEST_MIMETYPE_TEXT_PLAIN = MimetypeMap.MIMETYPE_TEXT_PLAIN;
    private final static String TEST_MIMETYPE_IMAGE_PNG = MimetypeMap.MIMETYPE_IMAGE_PNG;
    
    private MutableAuthenticationService authenticationService;
    private NodeService nodeService;
    private PersonService personService;
    private PermissionService permissionService;
    private ContentService contentService;
    private Repository repositoryHelper;
    private RetryingTransactionHelper transactionHelper;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        authenticationService = (MutableAuthenticationService) getServer().getApplicationContext().getBean("AuthenticationService");
        nodeService = (NodeService) getServer().getApplicationContext().getBean("NodeService");
        contentService = (ContentService) getServer().getApplicationContext().getBean("ContentService");
        personService = (PersonService) getServer().getApplicationContext().getBean("PersonService");
        permissionService = (PermissionService) getServer().getApplicationContext().getBean("PermissionService");
        repositoryHelper = (Repository) getServer().getApplicationContext().getBean("repositoryHelper");
        transactionHelper = (RetryingTransactionHelper)getServer().getApplicationContext().getBean("retryingTransactionHelper");
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        createUser(USER_ONE);
        createUser(USER_TWO);
        
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        testNode = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                props.put(ContentModel.PROP_NAME, TEST_NAME);
                ChildAssociationRef result = nodeService.createNode(repositoryHelper.getUserHome(personService.getPerson(USER_ONE)),
                                                        ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS,
                                                        ContentModel.TYPE_CONTENT, props);
                
                NodeRef nodeRef = result.getChildRef();
                ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(TEST_MIMETYPE_TEXT_PLAIN);
                writer.putContent(TEST_CONTENT);
                
                return nodeRef;
            }
        });
        
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testNode, PermissionService.READ));
        
    }
    
    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
        
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                if (testNode != null && nodeService.exists(testNode))
                {
                    nodeService.deleteNode(testNode);
                }
                return null;
            }
        });
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        deleteUser(USER_ONE);
        deleteUser(USER_TWO);
    }
    
    public void testSanityCheckUrls() throws Exception
    {
        final int expectedStatusOK = 200;
        final int expectedStatusNotFound = 404;
        final int expectedStatusServerError = 500; // currently mapped from AccessDenied (should it be 403, 404 or does it depend on use-case)
        
        String testNodeRef_3 = testNode.toString().replace("://", "/");
        
        // As user one ...
        
        // get metadata for node (authenticated)
        Response rsp = sendRequest(new GetRequest(AUTH_METADATA_URL.replace("{node_ref_3}", testNodeRef_3)), expectedStatusOK, USER_ONE);
        JSONObject jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        String name = jsonRsp.getString("name");
        assertEquals(TEST_NAME, name);
        String mimetype = jsonRsp.getString("mimetype");
        assertEquals(TEST_MIMETYPE_TEXT_PLAIN, mimetype);
        
        // get content for node (authenticated)
        rsp = sendRequest(new GetRequest(AUTH_CONTENT_URL.replace("{node_ref_3}", testNodeRef_3)), expectedStatusOK, USER_ONE);
        String content = rsp.getContentAsString();
        assertEquals(TEST_CONTENT, content);
        
        // get content thumbnail for node (authenticated)
        rsp = sendRequest(new GetRequest(AUTH_CONTENT_THUMBNAIL_URL.replace("{node_ref_3}", testNodeRef_3).replace("{thumbnailname}", "doclib")), expectedStatusOK, USER_ONE);
        String type = rsp.getContentType();
        assertEquals(TEST_MIMETYPE_IMAGE_PNG, type);
        
        // As user two ...
        
        rsp = sendRequest(new GetRequest(AUTH_METADATA_URL.replace("{node_ref_3}", testNodeRef_3)), expectedStatusServerError, USER_TWO);
        rsp = sendRequest(new GetRequest(AUTH_CONTENT_URL.replace("{node_ref_3}", testNodeRef_3)), expectedStatusServerError, USER_TWO);
        rsp = sendRequest(new GetRequest(AUTH_CONTENT_THUMBNAIL_URL.replace("{node_ref_3}", testNodeRef_3).replace("{thumbnailname}", "doclib")), expectedStatusServerError, USER_TWO);
        
        // As user one ...
        
        // share
        rsp = sendRequest(new PostRequest(SHARE_URL.replace("{node_ref_3}", testNodeRef_3), "", APPLICATION_JSON), expectedStatusOK, USER_ONE);
        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        String sharedId = jsonRsp.getString("sharedId");
        assertNotNull(sharedId);
        assertEquals(22, sharedId.length()); // note: we may have to adjust/remove this check if we change length of id (or it becomes variable length)
        
        // As user two ...
        
        // get metadata for share (note: can be unauthenticated)
        rsp = sendRequest(new GetRequest(SHARE_METADATA_URL.replace("{shared_id}", sharedId)), expectedStatusOK, USER_TWO);
        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        name = jsonRsp.getString("name");
        assertEquals(TEST_NAME, name);
        mimetype = jsonRsp.getString("mimetype");
        assertEquals(TEST_MIMETYPE_TEXT_PLAIN, mimetype);
        
        // get content for share (note: can be unauthenticated)
        rsp = sendRequest(new GetRequest(SHARE_CONTENT_URL.replace("{shared_id}", sharedId)), expectedStatusOK, USER_TWO);
        content = rsp.getContentAsString();
        assertEquals(TEST_CONTENT, content);
        
        // get content thumbnail for share (note: can be unauthenticated)
        rsp = sendRequest(new GetRequest(SHARE_CONTENT_THUMBNAIL_URL.replace("{shared_id}", sharedId).replace("{thumbnailname}", "doclib")), expectedStatusOK, USER_TWO);
        type = rsp.getContentType();
        assertEquals(TEST_MIMETYPE_IMAGE_PNG, type);
        
        // As user one ...
        
        // unshare
        rsp = sendRequest(new DeleteRequest(UNSHARE_URL.replace("{shared_id}", sharedId)), expectedStatusOK, USER_ONE);
        
        // As user two ...
        
        // -ve test (should not be able to get metadata or content via sharedId) - whether authenticated or not
        rsp = sendRequest(new GetRequest(SHARE_METADATA_URL.replace("{shared_id}", sharedId)), expectedStatusNotFound, USER_TWO);
        rsp = sendRequest(new GetRequest(SHARE_CONTENT_URL.replace("{shared_id}", sharedId)), expectedStatusNotFound, USER_TWO);
        rsp = sendRequest(new GetRequest(SHARE_CONTENT_THUMBNAIL_URL.replace("{shared_id}", sharedId).replace("{thumbnailname}", "doclib")), expectedStatusNotFound, USER_TWO);
    }
    
    private void createUser(String userName)
    {
        if (! authenticationService.authenticationExists(userName))
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());
        }
        
        if (! personService.personExists(userName))
        {
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            personService.createPerson(ppOne);
        }
    }
    
    private void deleteUser(String userName)
    {
        if (personService.personExists(userName))
        {
            personService.deletePerson(userName);
        }
    }
}
