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
package org.alfresco.repo.web.scripts.tagging;

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
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Unit test to test tagging Web Script API
 * 
 * @author Roy Wetherall
 */
public class TaggingServiceTest extends BaseWebScriptTest
{    
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private TaggingService taggingService;
    private FileFolderService fileFolderService;
    private Repository repositoryHelper;
    private NodeService nodeService;
    private TransactionService transactionService;
    
    private static final String TEST_USER = "TaggingServiceTestUser";
    
    private static final String TAG_1 = "tagOneREST";
    private static final String TAG_2 = "tagTwoREST";
    private static final String TAG_3 = "tagThreeREST";
    private static final String TAG_4 = "tagFourREST";
    private static final String TAG_5 = "tagFiveREST";
    
    private NodeRef nodeOne;
    private NodeRef nodeTwo;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        this.taggingService = (TaggingService)getServer().getApplicationContext().getBean("TaggingService");        
        this.fileFolderService = (FileFolderService)getServer().getApplicationContext().getBean("FileFolderService");
        this.repositoryHelper = (Repository)getServer().getApplicationContext().getBean("repositoryHelper");
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
        this.transactionService = (TransactionService) getServer().getApplicationContext().getBean("TransactionService");
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        // Add a load of tags ready to use
        this.taggingService.createTag(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), TAG_1);
        this.taggingService.createTag(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), TAG_2);
        this.taggingService.createTag(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), TAG_3);
        this.taggingService.createTag(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), TAG_4);
        this.taggingService.createTag(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), TAG_5);     
        
        // Create test node's
        NodeRef testRoot = this.repositoryHelper.getCompanyHome();
        String guid = GUID.generate();
        this.nodeOne = this.fileFolderService.create(testRoot, "test_doc1" + guid + ".txt", ContentModel.TYPE_CONTENT).getNodeRef();
        this.nodeTwo = this.fileFolderService.create(testRoot, "test_dco2" + guid + ".txt", ContentModel.TYPE_CONTENT).getNodeRef();
        
        // Add tags to test nodes
        this.taggingService.addTag(nodeOne, TAG_1);
        this.taggingService.addTag(nodeOne, TAG_2);
        this.taggingService.addTag(nodeTwo, TAG_2);
        
        // Create users
        createUser(TEST_USER);
        
        // Do tests as user one
        this.authenticationComponent.setCurrentUser(TEST_USER);
    }
    
    private void createUser(String userName)
    {
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            this.authenticationService.createAuthentication(userName, "PWD".toCharArray());
            
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            this.personService.createPerson(ppOne);
        }        
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        this.taggingService.deleteTag(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), TAG_1);
        this.taggingService.deleteTag(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), TAG_2);
        this.taggingService.deleteTag(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), TAG_3);
        this.taggingService.deleteTag(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), TAG_4);
        this.taggingService.deleteTag(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), TAG_5);
        
        this.nodeService.deleteNode(this.nodeOne);
        this.nodeService.deleteNode(this.nodeTwo);
    }
    
    public void testGetTags()
        throws Exception
    {
        Response response = sendRequest(new GetRequest("/api/tags/" + StoreRef.PROTOCOL_WORKSPACE + "/SpacesStore"), 200);
        JSONArray jsonArray = new JSONArray(response.getContentAsString());
        
        // make sure there are results
        assertNotNull(jsonArray);
        assertTrue(jsonArray.length() > 0);
        
        response = sendRequest(new GetRequest("/api/tags/" + StoreRef.PROTOCOL_WORKSPACE + "/SpacesStore?tf=one"), 200);
        jsonArray = new JSONArray(response.getContentAsString());
        
        // make sure there are results
        assertNotNull(jsonArray);
        assertTrue(jsonArray.length() > 0);
        
        response = sendRequest(new GetRequest("/api/tags/" + StoreRef.PROTOCOL_WORKSPACE + "/SpacesStore?tf=none"), 200);
        jsonArray = new JSONArray(response.getContentAsString());
        
        // make sure there are no results
        assertNotNull(jsonArray);
        assertEquals(0, jsonArray.length());
    }
    
    public void testGetNodes()
        throws Exception
    {
        Response response = sendRequest(new GetRequest("/api/tags/" + StoreRef.PROTOCOL_WORKSPACE + "/SpacesStore/" + TAG_1 + "/nodes"), 200);
        JSONArray jsonArray = new JSONArray(response.getContentAsString());
        
        assertNotNull(jsonArray);
        assertEquals(1, jsonArray.length());
        
        System.out.println(response.getContentAsString());
        
        response = sendRequest(new GetRequest("/api/tags/" + StoreRef.PROTOCOL_WORKSPACE + "/SpacesStore/" + TAG_2 + "/nodes"), 200);
        jsonArray = new JSONArray(response.getContentAsString());
        
        assertNotNull(jsonArray);
        assertEquals(2, jsonArray.length());
        
        response = sendRequest(new GetRequest("/api/tags/" + StoreRef.PROTOCOL_WORKSPACE + "/SpacesStore/jumk/nodes"), 200);
        jsonArray = new JSONArray(response.getContentAsString());
        
        assertNotNull(jsonArray);
        assertEquals(0, jsonArray.length());
        
    }
    
    public void testTagCountSearch() throws Exception
    {
        boolean removeTestFolder = false;
        boolean removeTagScope = false;
        FileInfo testFolderInfo = null;
        NodeRef testRoot = this.repositoryHelper.getCompanyHome();
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setRunAsUserSystem();
        
        try
        {
            String guid = GUID.generate();
            testFolderInfo = this.fileFolderService.create(testRoot, "testFldr" + guid, ContentModel.TYPE_FOLDER);
            removeTestFolder = true;
            String tagName = GUID.generate();
            boolean isTagScope = nodeService.hasAspect(testRoot, ContentModel.ASPECT_TAGSCOPE);
            
            //generate content with tag
            UserTransaction transaction = transactionService.getUserTransaction();
            transaction.begin();
            for (int i = 0; i < 1200; i++)
            {
                FileInfo file = this.fileFolderService.create(testFolderInfo.getNodeRef(), "testDoc" + i + ".txt", ContentModel.TYPE_CONTENT);
                this.taggingService.addTag(file.getNodeRef(), tagName);
            }
            transaction.commit();
            
            
            
            Response response = null;
            JSONObject jsonObj = null;
            JSONArray tagsArr = null;
            int number = -1;
            
            // tagScope API: if container isn't tagScope
            if (!isTagScope)
            {
                response = sendRequest(new GetRequest("api/tagscopes/node/" + testRoot.toString().replace(":/", "") + "/tags"), 200);
                jsonObj = new JSONObject(response.getContentAsString());
                tagsArr = jsonObj.getJSONArray("tags");
                number = getTagNumber(tagName, tagsArr);
                assertEquals("Checking a search via tagScope API: Tag number must be -1, because node isn't  a tag scope.", -1, number);
            }
            
            // tag count via search engine
            response = sendRequest(new GetRequest("collaboration/tagQuery?s=count&n=alfresco%3A%2F%2Fcompany%2Fhome"), 200);
            jsonObj = new JSONObject(response.getContentAsString());
            tagsArr = jsonObj.getJSONArray("tags");
            number = getTagNumber(tagName, tagsArr);
            assertTrue("Checking a search via search engine: Tag number must be more then 0", number > 0);
            
            if (!isTagScope)
            {
                this.taggingService.addTagScope(testRoot);
                removeTagScope = true;
            }
            
            // tag count via tagScope API
            response = sendRequest(new GetRequest("api/tagscopes/node/" + testRoot.toString().replace(":/", "") + "/tags"), 200);
            jsonObj = new JSONObject(response.getContentAsString());
            tagsArr = jsonObj.getJSONArray("tags");
            number = getTagNumber(tagName, tagsArr);
            assertEquals("Checking a search via tagScope API: Tag number must be 1200, because node is a tag scope.", 1200, number);
            
        }
        finally
        {
            if (removeTestFolder)
            {
                this.fileFolderService.delete(testFolderInfo.getNodeRef());
            }
            if (removeTagScope)
            {
                taggingService.removeTagScope(testRoot);
            }
            AuthenticationUtil.popAuthentication();
        }
    }
    
    private int getTagNumber(String tagName, JSONArray jsonTagsArray) throws JSONException
    {
        int result = -1;
        if (jsonTagsArray.length() == 0)
        {
            return result;
        }
        
        for (int i = 0; i < jsonTagsArray.length(); i++)
        {
            JSONObject tagObj = jsonTagsArray.getJSONObject(i);
            if (tagName.equalsIgnoreCase(tagObj.getString("name")))
            {
                result = tagObj.getInt("count");
                break;
            }
        }
        
        return result;
        
    }
    
}