/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.tagging;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.json.JSONArray;

/**
 * Unit test to test tagging Web Script API
 * 
 * @author Roy Wetherall
 */
public class TaggingServiceTest extends BaseWebScriptTest
{    
    private AuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private TaggingService taggingService;
    private FileFolderService fileFolderService;
    private Repository repositoryHelper;
    private NodeService nodeService;
    
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
        
        this.authenticationService = (AuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        this.taggingService = (TaggingService)getServer().getApplicationContext().getBean("TaggingService");        
        this.fileFolderService = (FileFolderService)getServer().getApplicationContext().getBean("FileFolderService");
        this.repositoryHelper = (Repository)getServer().getApplicationContext().getBean("repositoryHelper");
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
        
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
        
        assertNotNull(jsonArray);
        //assertEquals(5, jsonArray.length());
        
        response = sendRequest(new GetRequest("/api/tags/" + StoreRef.PROTOCOL_WORKSPACE + "/SpacesStore?tf=one"), 200);
        jsonArray = new JSONArray(response.getContentAsString());
        
        assertNotNull(jsonArray);
        assertEquals(1, jsonArray.length());
        
        response = sendRequest(new GetRequest("/api/tags/" + StoreRef.PROTOCOL_WORKSPACE + "/SpacesStore?tf=none"), 200);
        jsonArray = new JSONArray(response.getContentAsString());
        
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
    
}