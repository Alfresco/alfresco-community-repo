/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.groups;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.TestWebScriptServer.DeleteRequest;
import org.alfresco.web.scripts.TestWebScriptServer.GetRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PostRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PutRequest;
import org.alfresco.web.scripts.TestWebScriptServer.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Unit test of Groups REST APIs.   /api/groups 
 *  
 * @author Mark Rogers
 */
public class GroupsTest extends BaseWebScriptTest
{    
    private AuthenticationService authenticationService;
    private AuthorityService authorityService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    
    private String ADMIN_GROUP = "ALFRESCO_ADMINISTRATORS";
    private String TEST_ROOTGROUP = "GROUPS_TESTROOT";
    private String TEST_GROUPA = "TestA";
    private String TEST_GROUPB = "TESTB";
    private String TEST_GROUPC = "TesTC";
    private String TEST_GROUPD = "TESTD";
    private String TEST_ROOTGROUP_DISPLAY_NAME = "GROUPS_TESTROOTDisplayName";
    
    private static final String USER_ONE = "GroupTestOne";
    private static final String USER_TWO = "GroupTestTwo";
    private static final String USER_THREE = "GroupTestThree";
    
    private static final String URL_GROUPS = "/api/groups";
    private static final String URL_ROOTGROUPS = "/api/rootgroups";
    
    /**
     * Test Tree for all group tests
     *
     * TEST_ROOTGROUP
     *	GROUPA
     *	GROUPB
     *		GROUPD
     *		USER_TWO
     *		USER_THREE
     *	GROUPC
     *		USER_TWO
     */		
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (AuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        this.authorityService = (AuthorityService)getServer().getApplicationContext().getBean("AuthorityService");
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        // Create users
        createUser(USER_ONE);
        createUser(USER_TWO);
        createUser(USER_THREE);
        
        // create a test group tree
        String rootGroup = authorityService.createAuthority(AuthorityType.GROUP, null, TEST_ROOTGROUP , TEST_ROOTGROUP_DISPLAY_NAME);
        authorityService.createAuthority(AuthorityType.GROUP, rootGroup, TEST_GROUPA);
        String groupB = authorityService.createAuthority(AuthorityType.GROUP, rootGroup, TEST_GROUPB);
        authorityService.createAuthority(AuthorityType.GROUP, groupB, TEST_GROUPD);
        authorityService.addAuthority(groupB, USER_TWO);
        authorityService.addAuthority(groupB, USER_THREE);
        
        String groupC = authorityService.createAuthority(AuthorityType.GROUP, rootGroup, TEST_GROUPC);
        authorityService.addAuthority(groupC, USER_TWO);
        
        // Do tests as user one
        this.authenticationComponent.setCurrentUser(USER_ONE);
        
        Thread.sleep(10);
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
                
    }
    
    /**
     * Detailed test of get root groups
     */
    public void testGetRootGroup() throws Exception
    {
    	/**
    	 * Get all root groups should be at least the ALFRESCO_ADMINISTRATORS groups
    	 */
    	{
    		Response response = sendRequest(new GetRequest(URL_ROOTGROUPS), Status.STATUS_OK);
    		JSONObject top = new JSONObject(response.getContentAsString());
    		System.out.println(response.getContentAsString());
    		JSONArray data = top.getJSONArray("data");
    		assertTrue(data.length() > 0);
    		
    		for(int i = 0; i < data.length(); i++)
    		{
    			JSONObject rootGroup = data.getJSONObject(i);
    			if(rootGroup.getString("shortName").equals(TEST_ROOTGROUP))
    			{
    				// This is our test rootgroup
    				assertEquals("shortName wrong", TEST_ROOTGROUP, rootGroup.getString("shortName"));
    				assertEquals("displayName wrong", TEST_ROOTGROUP_DISPLAY_NAME, rootGroup.getString("displayName"));
    				assertEquals("authorityType wrong", "GROUP", rootGroup.getString("authorityType"));
    				assertFalse("test rootgroup is admin group", rootGroup.getBoolean("isAdminGroup"));
    			}
    			if(rootGroup.getString("shortName").equals(ADMIN_GROUP))
    			{
    				//assertTrue("admin group is not admin group", rootGroup.getBoolean("isAdminGroup"));
    			}
    		}	
    	}
    }
    
    /**
     * Detailed test of get group
     */
    public void testGetGroup() throws Exception
    {
    	{
    		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + ADMIN_GROUP), 200);
    		JSONObject top = new JSONObject(response.getContentAsString());
    		System.out.println(response.getContentAsString());
    		JSONObject data = top.getJSONObject("data");
    		assertTrue(data.length() > 0);
    		//assertTrue("admin group is not admin group", data.getBoolean("isAdminGroup"));
    		assertTrue("admin group is not root group", data.getBoolean("isRootGroup"));
    	}
    	
    	{
    		sendRequest(new GetRequest(URL_GROUPS + "/" + "crap"), Status.STATUS_NOT_FOUND);
    	}
    	
    	/**
    	 * Get GROUP B
    	 */
    	{
    		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + TEST_GROUPB), Status.STATUS_OK);
    		JSONObject top = new JSONObject(response.getContentAsString());
    		System.out.println(response.getContentAsString());
    		JSONObject data = top.getJSONObject("data");
    		assertTrue(data.length() > 0);
    		assertFalse("group B is not admin group", data.getBoolean("isAdminGroup"));
    		assertFalse("group B is not root group", data.getBoolean("isRootGroup"));
    	}
    
    }
    
    /**
     * Detailed test of create root group
     */
    public void testCreateRootGroup() throws Exception
    {
    
    }
    
    /**
     * Detailed test of create group
     */
    public void testCreateGroup() throws Exception
    {
    
    }
    
    /**
     * Detailed test of search groups
     *<li>if the optional includeInternal parameter is true then will include internal groups, otherwise internalGroups are not returned.</li>
      <li>If the optional shortNameFilter parameter is set then returns those root groups with a partial match on shortName.</li>
     */
    public void testSearchGroups() throws Exception
    {
    	// Search on partial short name
    	{
		    Response response = sendRequest(new GetRequest(URL_GROUPS + "?shortNameFilter=" + "*ADMIN*"), Status.STATUS_OK);
		    JSONObject top = new JSONObject(response.getContentAsString());
		    System.out.println(response.getContentAsString());
		    JSONArray data = top.getJSONArray("data");
		    assertTrue(data.length() > 0);
    	}
    	
    	// Search on full shortName
		{
		    Response response = sendRequest(new GetRequest(URL_GROUPS + "?shortNameFilter=" + ADMIN_GROUP), Status.STATUS_OK);
		    JSONObject top = new JSONObject(response.getContentAsString());
		    System.out.println(response.getContentAsString());
		    JSONArray data = top.getJSONArray("data");
		    assertTrue(data.length() > 0);
		}
    
    }
    
    /**
     * Detailed test of get Parents
     */
    public void testGetParents() throws Exception
    {
    	/**
    	 * Get all parents for the root group ALFRESCO_ADMINISTRATORS groups which has no parents
    	 */
    	{
    		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + ADMIN_GROUP + "/parents"), Status.STATUS_OK);
    		JSONObject top = new JSONObject(response.getContentAsString());
    		System.out.println(response.getContentAsString());
    		JSONArray data = top.getJSONArray("data");
    		// Top level group has no parents
    		assertTrue("top level group has no parents", data.length() == 0);
    	}
    	
    	/**synetics
    	 * 
    	 * Get GROUP B   Which should be a child of TESTROOT
    	 */
    	{
    		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + TEST_GROUPB + "/parents"), Status.STATUS_OK);
    		JSONObject top = new JSONObject(response.getContentAsString());
    		System.out.println(response.getContentAsString());
    		JSONArray data = top.getJSONArray("data");
    		assertTrue(data.length() > 0);
    	}
    	
       	/**
    	 * Get GROUP D   Which should be a child of GROUPB child of TESTROOT
    	 */
    	{
    		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + TEST_GROUPD + "/parents?level=ALL"), Status.STATUS_OK);
    		JSONObject top = new JSONObject(response.getContentAsString());
    		System.out.println(response.getContentAsString());
    		JSONArray data = top.getJSONArray("data");
    		assertTrue(data.length() >= 2);
    	}
      	/**
    	 * Negative test Get GROUP D level="rubbish"
    	 */
    	{
    		sendRequest(new GetRequest(URL_GROUPS + "/" + TEST_GROUPD + "/parents?level=rubbish"), Status.STATUS_BAD_REQUEST);
    	}
    	
    	/**
    	 * Negative test GROUP(Rubbish) does not exist
    	 */
    	{ 
    		sendRequest(new GetRequest(URL_GROUPS + "/" + "rubbish" + "/parents?level=all"), Status.STATUS_NOT_FOUND);
    	
    	}
    }
    
    /**
     * Detailed test of get Children
     */
    public void testGetChildren() throws Exception
    {
    	/**
    	 * Get All Children of GROUP B
    	 */
    	{
    		System.out.println("Get children of GROUP B");
    		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + TEST_GROUPB + "/children"), Status.STATUS_OK);
    		JSONObject top = new JSONObject(response.getContentAsString());
    		System.out.println(response.getContentAsString());
    		JSONArray data = top.getJSONArray("data");
    		//assertTrue(data.length() > 0);
    	}
    	
//    	/**
//    	 * Get All Children of GROUP B which are GROUPS
//    	 */
//    	{
//    		System.out.println("Get child GROUPS of GROUP B");
//    		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + TEST_GROUPB + "/children?authorityType=GROUP"), Status.STATUS_OK);
//    		JSONObject top = new JSONObject(response.getContentAsString());
//    		System.out.println(response.getContentAsString());
//    		JSONArray data = top.getJSONArray("data");
//    		assertTrue("no child groups of group B", data.length() == 1);
//    		
//    		JSONObject subGroup = data.getJSONObject(0);
//    		assertEquals("shortName wrong", TEST_GROUPD, subGroup.getString("shortName"));
//    		assertEquals("authorityType wrong", "GROUP", subGroup.getString("authorityType"));
//    	}
//    	
//    	/**
//    	 * Get All Children of GROUP B which are USERS
//    	 */
//    	{
//    		System.out.println("Get Child Users of Group B");
//    		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + TEST_GROUPB + "/children?authorityType=USER"), Status.STATUS_OK);
//    		JSONObject top = new JSONObject(response.getContentAsString());
//    		System.out.println(response.getContentAsString());
//    		JSONArray data = top.getJSONArray("data");
//    		//assertTrue(data.length() > 0);
//    	}
    	
    	/**
    	 * Negative test All Children of GROUP B, bad authorityType
    	 */
    	{
    		sendRequest(new GetRequest(URL_GROUPS + "/" + TEST_GROUPB + "/children?authorityType=XXX"), Status.STATUS_BAD_REQUEST);
    	}
    	
    	
    	/**
    	 * Negative test GROUP(Rubbish) does not exist
    	 */
    	{ 
    		sendRequest(new GetRequest(URL_GROUPS + "/" + "rubbish" + "/children"), Status.STATUS_NOT_FOUND);
    	}
    	
    }
}
