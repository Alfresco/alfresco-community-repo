/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.DictionaryModelTypeTest;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.Request;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Unit Tests for the Java-backed Node WebScripts
 * 
 * @since 4.1
 */
public class NodeWebScripTest extends BaseWebScriptTest
{
    private static Log logger = LogFactory.getLog(NodeWebScripTest.class);
    
    private static String CREATE_LINK_API = "/api/node/doclink/";
    private static String DESTINATION_NODE_REF_PARAM = "destinationNodeRef";
    private static String MULTIPLE_FILES_PARAM = "multipleFiles";

    private String TEST_SITE_NAME = "TestNodeSite";
    private SiteInfo TEST_SITE;
    
    private MutableAuthenticationService authenticationService;
    private RetryingTransactionHelper retryingTransactionHelper;
    private PersonService personService;
    private SiteService siteService;
    private NodeService nodeService;
    private NodeArchiveService nodeArchiveService;
    private CheckOutCheckInService checkOutCheckInService;
    
    private static final String USER_ONE = "UserOneSecondToo";
    private static final String USER_TWO = "UserTwoSecondToo";
    private static final String USER_THREE = "UserThreeStill";
    private static final String PASSWORD = "passwordTEST";
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        AbstractRefreshableApplicationContext ctx = (AbstractRefreshableApplicationContext)getServer().getApplicationContext();
        this.retryingTransactionHelper = (RetryingTransactionHelper)ctx.getBean("retryingTransactionHelper");
        this.authenticationService = (MutableAuthenticationService)ctx.getBean("AuthenticationService");
        this.personService = (PersonService)ctx.getBean("PersonService");
        this.siteService = (SiteService)ctx.getBean("SiteService");
        this.nodeService = (NodeService)ctx.getBean("NodeService");
        this.nodeArchiveService = (NodeArchiveService)ctx.getBean("nodeArchiveService");
        this.checkOutCheckInService = (CheckOutCheckInService)ctx.getBean("checkOutCheckInService");
        
        // Do the setup as admin
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        // Create a site
        TEST_SITE = createSite(TEST_SITE_NAME);
        
        // Create two users, one who's a site member
        createUser(USER_ONE, true);
        createUser(USER_TWO, false);
        
        // Do our tests by default as the first user who is a contributor
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        // Admin user required to delete users and sites
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        
        SiteInfo siteInfo = siteService.getSite(TEST_SITE.getShortName());
        if (siteInfo != null)
        {
            // Zap the site, and their contents
            siteService.deleteSite(TEST_SITE.getShortName());
            nodeArchiveService.purgeArchivedNode(nodeArchiveService.getArchivedNode(siteInfo.getNodeRef()));
        }

        
        // Delete users
        for (String user : new String[] {USER_ONE, USER_TWO, USER_THREE})
        {
            // Delete the user, as admin
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            if(personService.personExists(user))
            {
               personService.deletePerson(user);
            }
            if(this.authenticationService.authenticationExists(user))
            {
               this.authenticationService.deleteAuthentication(user);
            }
        }
    }
    
    private SiteInfo createSite(final String shortName)
    {
        return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<SiteInfo>()
           {
              @Override
              public SiteInfo execute() throws Throwable
              {
                  SiteInfo siteInfo = siteService.getSite(shortName);
                  if (siteInfo != null)
                  {
                      // Tidy up after failed earlier run
                      siteService.deleteSite(shortName);
                      nodeArchiveService.purgeArchivedNode(nodeArchiveService.getArchivedNode(siteInfo.getNodeRef()));
                  }
                  
                  // Do the create
                  SiteInfo site = siteService.createSite("Testing", shortName, shortName, null, SiteVisibility.PUBLIC);
                  
                  // Ensure we have a doclib
                  siteService.createContainer(shortName, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
                  
                  // All done
                  return site;
              }
           }, false, true
        ); 
    }
    
    private void createUser(final String userName, boolean contributor)
    {
        // Make sure a new user is created every time
        // This ensures a predictable password etc
        if(this.personService.personExists(userName))
        {
           this.personService.deletePerson(userName);
        }
        if(this.authenticationService.authenticationExists(userName))
        {
           this.authenticationService.deleteAuthentication(userName);
        }
        
        
        // Create a fresh user
        authenticationService.createAuthentication(userName, PASSWORD.toCharArray());

        // create person properties
        PropertyMap personProps = new PropertyMap();
        personProps.put(ContentModel.PROP_USERNAME, userName);
        personProps.put(ContentModel.PROP_FIRSTNAME, "First");
        personProps.put(ContentModel.PROP_LASTNAME, "Last");
        personProps.put(ContentModel.PROP_EMAIL, "FirstName123.LastName123@email.com");
        personProps.put(ContentModel.PROP_JOBTITLE, "JobTitle123");
        personProps.put(ContentModel.PROP_JOBTITLE, "Organisation123");

        // create person node for user
        personService.createPerson(personProps);

        // Set site permissions as needed
        if (contributor)
        {
            this.siteService.setMembership(TEST_SITE_NAME, userName, SiteModel.SITE_CONTRIBUTOR);
        }
        else
        {
            this.siteService.setMembership(TEST_SITE_NAME, userName, SiteModel.SITE_CONSUMER);
        }
    }
    
    private JSONObject asJSON(Response response) throws Exception
    {
        String json = response.getContentAsString();
        JSONParser p = new JSONParser();
        Object o = p.parse(json);
        
        if (o instanceof JSONObject)
        {
            return (JSONObject)o; 
        }
        throw new IllegalArgumentException("Expected JSONObject, got " + o + " from " + json);
    }
    
    
    @SuppressWarnings("unchecked")
    public void testFolderCreation() throws Exception
    {
        // Create a folder within the DocLib
        NodeRef siteDocLib = siteService.getContainer(TEST_SITE.getShortName(), SiteService.DOCUMENT_LIBRARY);
        
        String testFolderName = "testing";
        Map<QName,Serializable> testFolderProps = new HashMap<QName, Serializable>();
        testFolderProps.put(ContentModel.PROP_NAME, testFolderName);
        NodeRef testFolder = nodeService.createNode(siteDocLib, ContentModel.ASSOC_CONTAINS, 
                QName.createQName("testing"), ContentModel.TYPE_FOLDER, testFolderProps).getChildRef();
        
        String testNodeName = "aNEWfolder";
        String testNodeTitle = "aTITLEforAfolder";
        String testNodeDescription = "DESCRIPTIONofAfolder";
        JSONObject jsonReq = null;
        JSONObject json = null;
        NodeRef folder = null;
        
        
        // By NodeID
        Request req = new Request("POST", "/api/node/folder/"+testFolder.getStoreRef().getProtocol()+"/"+
                                   testFolder.getStoreRef().getIdentifier()+"/"+testFolder.getId());
        jsonReq = new JSONObject();
        jsonReq.put("name", testNodeName);
        req.setBody(jsonReq.toString().getBytes());
        req.setType(MimetypeMap.MIMETYPE_JSON);
        
        json = asJSON( sendRequest(req, Status.STATUS_OK) );
        assertNotNull(json.get("nodeRef"));

        folder = new NodeRef((String)json.get("nodeRef"));
        assertEquals(true, nodeService.exists(folder));
        assertEquals(testNodeName, nodeService.getProperty(folder, ContentModel.PROP_NAME));
        assertEquals(testNodeName, nodeService.getProperty(folder, ContentModel.PROP_TITLE));
        assertEquals(null, nodeService.getProperty(folder, ContentModel.PROP_DESCRIPTION));
        
        assertEquals(testFolder, nodeService.getPrimaryParent(folder).getParentRef());
        assertEquals(ContentModel.TYPE_FOLDER, nodeService.getType(folder));
        
        nodeService.deleteNode(folder);

        
        // In a Site Container
        req = new Request("POST", "/api/site/folder/"+TEST_SITE_NAME+"/"+SiteService.DOCUMENT_LIBRARY);
        jsonReq = new JSONObject();
        jsonReq.put("name", testNodeName);
        jsonReq.put("description", testNodeDescription);
        req.setBody(jsonReq.toString().getBytes());

        json = asJSON( sendRequest(req, Status.STATUS_OK) );
        assertNotNull(json.get("nodeRef"));

        folder = new NodeRef((String)json.get("nodeRef"));
        assertEquals(true, nodeService.exists(folder));
        assertEquals(testNodeName, nodeService.getProperty(folder, ContentModel.PROP_NAME));
        assertEquals(testNodeName, nodeService.getProperty(folder, ContentModel.PROP_TITLE));
        assertEquals(testNodeDescription, nodeService.getProperty(folder, ContentModel.PROP_DESCRIPTION));
        
        assertEquals(siteDocLib, nodeService.getPrimaryParent(folder).getParentRef());
        assertEquals(ContentModel.TYPE_FOLDER, nodeService.getType(folder));
        
        nodeService.deleteNode(folder);

        
        // A Child of a Site Container
        req = new Request("POST", "/api/site/folder/"+TEST_SITE_NAME+"/"+SiteService.DOCUMENT_LIBRARY+"/"+testFolderName);
        jsonReq = new JSONObject();
        jsonReq.put("name", testNodeName);
        jsonReq.put("title", testNodeTitle);
        jsonReq.put("description", testNodeDescription);
        req.setBody(jsonReq.toString().getBytes());

        json = asJSON( sendRequest(req, Status.STATUS_OK) );
        assertNotNull(json.get("nodeRef"));

        folder = new NodeRef((String)json.get("nodeRef"));
        assertEquals(true, nodeService.exists(folder));
        assertEquals(testNodeName, nodeService.getProperty(folder, ContentModel.PROP_NAME));
        assertEquals(testNodeTitle, nodeService.getProperty(folder, ContentModel.PROP_TITLE));
        assertEquals(testNodeDescription, nodeService.getProperty(folder, ContentModel.PROP_DESCRIPTION));
        
        assertEquals(testFolder, nodeService.getPrimaryParent(folder).getParentRef());
        assertEquals(ContentModel.TYPE_FOLDER, nodeService.getType(folder));

        nodeService.deleteNode(folder);

        
        // Type needs to be a subtype of folder
        
        // explicit cm:folder
        req = new Request("POST", "/api/site/folder/"+TEST_SITE_NAME+"/"+SiteService.DOCUMENT_LIBRARY+"/"+testFolderName);
        jsonReq = new JSONObject();
        jsonReq.put("name", testNodeName);
        jsonReq.put("type", "cm:folder");
        req.setBody(jsonReq.toString().getBytes());

        json = asJSON( sendRequest(req, Status.STATUS_OK) );
        assertNotNull(json.get("nodeRef"));

        folder = new NodeRef((String)json.get("nodeRef"));
        assertEquals(true, nodeService.exists(folder));
        assertEquals(testNodeName, nodeService.getProperty(folder, ContentModel.PROP_NAME));
        assertEquals(ContentModel.TYPE_FOLDER, nodeService.getType(folder));

        nodeService.deleteNode(folder);

        
        // cm:systemfolder extends from cm:folder
        req = new Request("POST", "/api/site/folder/"+TEST_SITE_NAME+"/"+SiteService.DOCUMENT_LIBRARY+"/"+testFolderName);
        jsonReq = new JSONObject();
        jsonReq.put("name", testNodeName);
        jsonReq.put("type", "cm:systemfolder");
        req.setBody(jsonReq.toString().getBytes());

        json = asJSON( sendRequest(req, Status.STATUS_OK) );
        assertNotNull(json.get("nodeRef"));

        folder = new NodeRef((String)json.get("nodeRef"));
        assertEquals(true, nodeService.exists(folder));
        assertEquals(testNodeName, nodeService.getProperty(folder, ContentModel.PROP_NAME));
        assertEquals(ContentModel.TYPE_SYSTEM_FOLDER, nodeService.getType(folder));

        nodeService.deleteNode(folder);

        
        // cm:content isn't allowed
        req = new Request("POST", "/api/site/folder/"+TEST_SITE_NAME+"/"+SiteService.DOCUMENT_LIBRARY+"/"+testFolderName);
        jsonReq = new JSONObject();
        jsonReq.put("name", testNodeName);
        jsonReq.put("type", "cm:content");
        req.setBody(jsonReq.toString().getBytes());

        sendRequest(req, Status.STATUS_BAD_REQUEST);
        
        
        // Check permissions - need to be Contributor
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        req = new Request("POST", "/api/node/folder/"+testFolder.getStoreRef().getProtocol()+"/"+
                                  testFolder.getStoreRef().getIdentifier()+"/"+testFolder.getId());
        jsonReq = new JSONObject();
        jsonReq.put("name", testNodeName);
        req.setBody(jsonReq.toString().getBytes());
        req.setType(MimetypeMap.MIMETYPE_JSON);

        json = asJSON( sendRequest(req, Status.STATUS_OK) );
        assertNotNull(json.get("nodeRef"));

        folder = new NodeRef((String)json.get("nodeRef"));
        assertEquals(true, nodeService.exists(folder));
        nodeService.deleteNode(folder);

        
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        sendRequest(req, Status.STATUS_FORBIDDEN);
    }

    @SuppressWarnings("unchecked")
    public void testLinkCreation() throws Exception
    {
        // Create a folder within the DocLib
        NodeRef siteDocLib = siteService.getContainer(TEST_SITE.getShortName(), SiteService.DOCUMENT_LIBRARY);

        String testFolder1Name = "testingLinkCreationFolder1";
        Map<QName, Serializable> testFolderProps = new HashMap<QName, Serializable>();
        testFolderProps.put(ContentModel.PROP_NAME, testFolder1Name);
        NodeRef testFolder1 = nodeService.createNode(siteDocLib, ContentModel.ASSOC_CONTAINS,
                QName.createQName("testingLinkCreationFolder1"), ContentModel.TYPE_FOLDER, testFolderProps).getChildRef();

        JSONObject jsonReq = null;
        JSONObject json = null;
        JSONArray jsonArray = new JSONArray();
        JSONArray jsonLinkNodes = null;
        JSONObject jsonLinkNode = null;

        //Create files in the testFolder1
        NodeRef testFile1 = createNode(testFolder1, "testingLinkCreationFile1", ContentModel.TYPE_CONTENT,
                AuthenticationUtil.getAdminUserName());
        NodeRef testFile2 = createNode(testFolder1, "testingLinkCreationFile2", ContentModel.TYPE_CONTENT,
                AuthenticationUtil.getAdminUserName());
        NodeRef testFile3 = createNode(testFolder1, "testingLinkCreationFile3", ContentModel.TYPE_CONTENT,
                AuthenticationUtil.getAdminUserName());

        //Create testFolder2 in the testFolder1
        String testFolder2Name = "testingLinkCreationFolder2";
        testFolderProps = new HashMap<QName, Serializable>();
        testFolderProps.put(ContentModel.PROP_NAME, testFolder2Name);
        NodeRef testFolder2 = nodeService.createNode(siteDocLib, ContentModel.ASSOC_CONTAINS,
                QName.createQName("testingLinkCreationFolder2"), ContentModel.TYPE_FOLDER, testFolderProps).getChildRef();

        // Create link to file1 in same folder - testFolder1
        Request req = new Request("POST", CREATE_LINK_API + testFile1.getStoreRef().getProtocol() + "/"
                + testFile1.getStoreRef().getIdentifier() + "/" + testFile1.getId());
        jsonReq = new JSONObject();
        jsonReq.put(DESTINATION_NODE_REF_PARAM, testFolder1.toString());

        jsonArray.add(testFile1.toString());
        jsonReq.put(MULTIPLE_FILES_PARAM, jsonArray);
        req.setBody(jsonReq.toString().getBytes());
        req.setType(MimetypeMap.MIMETYPE_JSON);

        json = asJSON(sendRequest(req, Status.STATUS_OK));
        jsonLinkNodes = (JSONArray) json.get("linkNodes");
        assertNotNull(jsonLinkNodes);
        assertEquals(1, jsonLinkNodes.size());
        assertEquals("true", json.get("overallSuccess"));
        assertEquals("1", json.get("successCount"));
        assertEquals("0", json.get("failureCount"));

        jsonLinkNode = (JSONObject) jsonLinkNodes.get(0);
        String nodeRef = (String) jsonLinkNode.get("nodeRef");
        NodeRef file1Link = new NodeRef(nodeRef);

        //Check that app:linked aspect is added on sourceNode
        assertEquals(true, nodeService.hasAspect(testFile1, ApplicationModel.ASPECT_LINKED));
        assertEquals(true, nodeService.exists(file1Link));
        nodeService.deleteNode(file1Link);
        assertEquals(false, nodeService.hasAspect(testFile1, ApplicationModel.ASPECT_LINKED));

        //Create link to testFolder2 in same folder (testFolder1)
        req = new Request("POST", CREATE_LINK_API + testFolder2.getStoreRef().getProtocol() + "/"
                + testFolder2.getStoreRef().getIdentifier() + "/" + testFolder2.getId());
        jsonReq = new JSONObject();
        jsonReq.put(DESTINATION_NODE_REF_PARAM, testFolder1.toString());
        jsonArray = new JSONArray();
        jsonArray.add(testFolder2.toString());
        jsonReq.put(MULTIPLE_FILES_PARAM, jsonArray);
        req.setBody(jsonReq.toString().getBytes());
        req.setType(MimetypeMap.MIMETYPE_JSON);

        json = asJSON(sendRequest(req, Status.STATUS_OK));
        jsonLinkNodes = (JSONArray) json.get("linkNodes");
        assertNotNull(jsonLinkNodes);
        assertEquals(1, jsonLinkNodes.size());
        assertEquals("true", json.get("overallSuccess"));
        assertEquals("1", json.get("successCount"));
        assertEquals("0", json.get("failureCount"));

        jsonLinkNode = (JSONObject) jsonLinkNodes.get(0);
        nodeRef = (String) jsonLinkNode.get("nodeRef");
        NodeRef folder2Link = new NodeRef(nodeRef);
        assertEquals(true, nodeService.hasAspect(testFolder2, ApplicationModel.ASPECT_LINKED));
        assertEquals(true, nodeService.exists(folder2Link));

        // create another link of testFolder2 in siteDocLib
        req = new Request("POST", CREATE_LINK_API + testFolder2.getStoreRef().getProtocol() + "/"
                + testFolder2.getStoreRef().getIdentifier() + "/" + testFolder2.getId());
        jsonReq = new JSONObject();
        jsonReq.put(DESTINATION_NODE_REF_PARAM, siteDocLib.toString());
        jsonArray = new JSONArray();
        jsonArray.add(testFolder2.toString());
        jsonReq.put(MULTIPLE_FILES_PARAM, jsonArray);
        req.setBody(jsonReq.toString().getBytes());
        req.setType(MimetypeMap.MIMETYPE_JSON);

        json = asJSON(sendRequest(req, Status.STATUS_OK));
        jsonLinkNodes = (JSONArray) json.get("linkNodes");
        assertNotNull(jsonLinkNodes);
        assertEquals(1, jsonLinkNodes.size());
        assertEquals("true", json.get("overallSuccess"));
        assertEquals("1", json.get("successCount"));
        assertEquals("0", json.get("failureCount"));

        jsonLinkNode = (JSONObject) jsonLinkNodes.get(0);
        nodeRef = (String) jsonLinkNode.get("nodeRef");
        NodeRef folder2Link2 = new NodeRef(nodeRef);

        // delete folder2Link and check that aspect exists since we have another
        // link for testFolder2
        nodeService.deleteNode(folder2Link);
        assertEquals(true, nodeService.hasAspect(testFolder2, ApplicationModel.ASPECT_LINKED));
        nodeService.deleteNode(folder2Link2);
        assertEquals(false, nodeService.hasAspect(testFolder2, ApplicationModel.ASPECT_LINKED));

        // Create link to testFile1, testFile2 and testFile3 in same testFolder1
        req = new Request("POST", CREATE_LINK_API + testFolder1.getStoreRef().getProtocol() + "/"
                + testFolder1.getStoreRef().getIdentifier() + "/" + testFolder1.getId());
        jsonReq = new JSONObject();
        jsonReq.put(DESTINATION_NODE_REF_PARAM, testFolder1.toString());
        jsonArray = new JSONArray();
        jsonArray.add(testFile1.toString());
        jsonArray.add(testFile2.toString());
        jsonArray.add(testFile3.toString());
        jsonReq.put(MULTIPLE_FILES_PARAM, jsonArray);
        req.setBody(jsonReq.toString().getBytes());
        req.setType(MimetypeMap.MIMETYPE_JSON);

        json = asJSON(sendRequest(req, Status.STATUS_OK));
        jsonLinkNodes = (JSONArray) json.get("linkNodes");
        assertNotNull(jsonLinkNodes);
        assertEquals(3, jsonLinkNodes.size());
        assertEquals("true", json.get("overallSuccess"));
        assertEquals("3", json.get("successCount"));
        assertEquals("0", json.get("failureCount"));

        NodeRef fileLink = null;
        List<NodeRef> fileLinks = new ArrayList<NodeRef>();
        for (int i = 0; i < jsonLinkNodes.size(); i++)
        {
            jsonLinkNode = (JSONObject) jsonLinkNodes.get(i);
            nodeRef = (String) jsonLinkNode.get("nodeRef");
            fileLink = new NodeRef(nodeRef);
            fileLinks.add(fileLink);
            assertEquals(true, nodeService.exists(fileLink));
        }

        //try to create another link in the same location - an exception should be thrown
        req = new Request("POST", CREATE_LINK_API + testFolder1.getStoreRef().getProtocol() + "/"
                + testFolder1.getStoreRef().getIdentifier() + "/" + testFolder1.getId());
        jsonReq = new JSONObject();
        jsonReq.put(DESTINATION_NODE_REF_PARAM, testFolder1.toString());
        jsonArray = new JSONArray();
        jsonArray.add(testFile1.toString());
        jsonReq.put(MULTIPLE_FILES_PARAM, jsonArray);
        req.setBody(jsonReq.toString().getBytes());
        req.setType(MimetypeMap.MIMETYPE_JSON);

        json = asJSON(sendRequest(req, Status.STATUS_BAD_REQUEST));

        // delete all 3 files and check that the links are deleted too
        nodeService.deleteNode(testFile1);
        nodeService.deleteNode(testFile2);
        nodeService.deleteNode(testFile3);
        for (NodeRef linkNodeRef : fileLinks)
        {
            assertEquals(false, nodeService.exists(linkNodeRef));
        }

        //try create a link to a site - shouldn't be possible
        SiteInfo site2 = createSite("Site2TestingNodeCreateLink");
        NodeRef siteNodeRef = site2.getNodeRef();

        req = new Request("POST", CREATE_LINK_API + testFolder1.getStoreRef().getProtocol() + "/"
                + testFolder1.getStoreRef().getIdentifier() + "/" + testFolder1.getId());
        jsonReq = new JSONObject();
        jsonReq.put(DESTINATION_NODE_REF_PARAM, testFolder1.toString());
        jsonArray = new JSONArray();
        jsonArray.add(siteNodeRef.toString());
        jsonReq.put(MULTIPLE_FILES_PARAM, jsonArray);
        req.setBody(jsonReq.toString().getBytes());
        req.setType(MimetypeMap.MIMETYPE_JSON);

        json = asJSON(sendRequest(req, Status.STATUS_BAD_REQUEST));

        siteService.deleteSite(site2.getShortName());
        nodeArchiveService.purgeArchivedNode(nodeArchiveService.getArchivedNode(siteNodeRef));

        //Links can be created in Shared Files, My Files and Repository
        NodeRef testFile4 = createNode(testFolder1, "testingLinkCreationFile4", ContentModel.TYPE_CONTENT,
                AuthenticationUtil.getAdminUserName());
        req = new Request("POST", CREATE_LINK_API + testFile4.getStoreRef().getProtocol() + "/"
                + testFile4.getStoreRef().getIdentifier() + "/" + testFile4.getId());
        jsonReq = new JSONObject();
        jsonReq.put(DESTINATION_NODE_REF_PARAM, "alfresco://company/shared");

        jsonArray = new JSONArray();
        jsonArray.add(testFile4.toString());
        jsonReq.put(MULTIPLE_FILES_PARAM, jsonArray);
        req.setBody(jsonReq.toString().getBytes());
        req.setType(MimetypeMap.MIMETYPE_JSON);

        json = asJSON(sendRequest(req, Status.STATUS_OK));

        req = new Request("POST", CREATE_LINK_API + testFile4.getStoreRef().getProtocol() + "/"
                + testFile4.getStoreRef().getIdentifier() + "/" + testFile4.getId());
        jsonReq = new JSONObject();
        jsonReq.put(DESTINATION_NODE_REF_PARAM, "alfresco://user/home");

        jsonArray = new JSONArray();
        jsonArray.add(testFile4.toString());
        jsonReq.put(MULTIPLE_FILES_PARAM, jsonArray);
        req.setBody(jsonReq.toString().getBytes());
        req.setType(MimetypeMap.MIMETYPE_JSON);

        json = asJSON(sendRequest(req, Status.STATUS_OK));

        //create link in Repository as Admin
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        req = new Request("POST", CREATE_LINK_API + testFile4.getStoreRef().getProtocol() + "/"
                + testFile4.getStoreRef().getIdentifier() + "/" + testFile4.getId());
        jsonReq = new JSONObject();
        jsonReq.put(DESTINATION_NODE_REF_PARAM, "alfresco://company/home");

        jsonArray = new JSONArray();
        jsonArray.add(testFile4.toString());
        jsonReq.put(MULTIPLE_FILES_PARAM, jsonArray);
        req.setBody(jsonReq.toString().getBytes());
        req.setType(MimetypeMap.MIMETYPE_JSON);

        json = asJSON(sendRequest(req, Status.STATUS_OK));

        //all 3 links are created with success, delete all links
        req = new Request("DELETE", CREATE_LINK_API + testFile4.getStoreRef().getProtocol() + "/"
                + testFile4.getStoreRef().getIdentifier() + "/" + testFile4.getId() + "/delete");
        req.setType(MimetypeMap.MIMETYPE_JSON);
        json = asJSON(sendRequest(req, Status.STATUS_OK));

        //all links are removed with success marker aspect should be removed too
        assertEquals(false, nodeService.hasAspect(testFile4, ApplicationModel.ASPECT_LINKED));

        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
    }

    private NodeRef createNode(NodeRef parentNode, String nodeCmName, QName nodeType, String ownerUserName)
    {
        QName childName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, nodeCmName);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, nodeCmName);
        ChildAssociationRef childAssoc = nodeService.createNode(parentNode,
                    ContentModel.ASSOC_CONTAINS,
                    childName,
                    nodeType,
                    props);
        return childAssoc.getChildRef();
    }
}