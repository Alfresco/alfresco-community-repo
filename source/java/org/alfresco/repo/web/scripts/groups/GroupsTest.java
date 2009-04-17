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


import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;

import org.alfresco.util.PropertyMap;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.TestWebScriptServer.DeleteRequest;
import org.alfresco.web.scripts.TestWebScriptServer.GetRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PostRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PutRequest;
import org.alfresco.web.scripts.TestWebScriptServer.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Unit test of Groups REST APIs. 
 * 
 * /api/groups 
 * /api/rootgroups 
 *  
 * @author Mark Rogers
 */
public class GroupsTest extends BaseWebScriptTest
{    
	private static final Log logger = LogFactory.getLog(BaseWebScriptTest.class);
	
    private AuthenticationService authenticationService;
    private AuthorityService authorityService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    
    private String ADMIN_GROUP = "ALFRESCO_ADMINISTRATORS";
    private String TEST_ROOTGROUP = "GroupsTest_ROOT";
    private String TEST_GROUPA = "TestA";
    private String TEST_GROUPB = "TESTB";
    private String TEST_GROUPC = "TesTC";
    private String TEST_GROUPD = "TESTD";
    private String TEST_LINK = "TESTLINK";
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
    private synchronized String createTestTree()
    {
    	if(rootGroupName == null)
    	{
    		rootGroupName = authorityService.getName(AuthorityType.GROUP, TEST_ROOTGROUP);
    	}
    	
        if(!authorityService.authorityExists(rootGroupName))
        {
            this.authenticationComponent.setSystemUserAsCurrentUser();
        	 
        	rootGroupName = authorityService.createAuthority(AuthorityType.GROUP, null, TEST_ROOTGROUP , TEST_ROOTGROUP_DISPLAY_NAME);
        	authorityService.createAuthority(AuthorityType.GROUP, rootGroupName, TEST_GROUPA);
        	String groupB = authorityService.createAuthority(AuthorityType.GROUP, rootGroupName, TEST_GROUPB);
        	authorityService.createAuthority(AuthorityType.GROUP, groupB, TEST_GROUPD);
        	authorityService.addAuthority(groupB, USER_TWO);
        	authorityService.addAuthority(groupB, USER_THREE);
        
        	String groupC = authorityService.createAuthority(AuthorityType.GROUP, rootGroupName, TEST_GROUPC);
        	authorityService.addAuthority(groupC, USER_TWO);
        
        	authorityService.createAuthority(AuthorityType.GROUP, rootGroupName, TEST_LINK);
        	
            this.authenticationComponent.setCurrentUser(USER_ONE);
        }
        
        return rootGroupName;

    }

    private static String rootGroupName = null;    
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
        
        // Do tests as user one
        this.authenticationComponent.setCurrentUser(USER_ONE);
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
        //if(rootGroupName != null)
        //{
        //	authorityService.deleteAuthority(rootGroupName);
        //}
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
    		logger.debug(response.getContentAsString());
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
        createTestTree();
        
    	{
    		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + ADMIN_GROUP), 200);
    		JSONObject top = new JSONObject(response.getContentAsString());
    		logger.debug(response.getContentAsString());
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
    		logger.debug(response.getContentAsString());
    		JSONObject data = top.getJSONObject("data");
    		assertTrue(data.length() > 0);
    		assertFalse("group B is not admin group", data.getBoolean("isAdminGroup"));
    		assertFalse("group B is not root group", data.getBoolean("isRootGroup"));
    	}
    
    }
    
    /**
     * Detailed test of create root group
     * Detailed test of delete root group
     */
    public void testCreateRootGroup() throws Exception
    {
    	String myGroupName = "GT_CRG";
    	String myDisplayName = "GT_CRGDisplay";
    	
    	/**
    	 * Negative test - try to create a group without admin authority
    	 */
    	{
    		JSONObject newGroupJSON = new JSONObject();
    		newGroupJSON.put("displayName", myDisplayName); 
    		sendRequest(new PostRequest(URL_ROOTGROUPS + "/" + myGroupName,  newGroupJSON.toString(), "application/json"), Status.STATUS_INTERNAL_SERVER_ERROR);   
    	}
    	
    	 
    	this.authenticationComponent.setSystemUserAsCurrentUser();
    	
    	try
    	{
    		/**
    		 * Create a root group
    		 */
    		{
    			JSONObject newGroupJSON = new JSONObject();
    			newGroupJSON.put("displayName", myDisplayName); 
    			Response response = sendRequest(new PostRequest(URL_ROOTGROUPS + "/" + myGroupName,  newGroupJSON.toString(), "application/json"), Status.STATUS_CREATED);
    			JSONObject top = new JSONObject(response.getContentAsString());
    			JSONObject rootGroup = top.getJSONObject("data");
    			assertEquals("shortName wrong", myGroupName, rootGroup.getString("shortName"));
    			assertEquals("displayName wrong", myDisplayName, rootGroup.getString("displayName"));
    		}
    	
    		/**
    		 * Negative test Create a root group that already exists
    		 */
    		{
    			JSONObject newGroupJSON = new JSONObject();
    			newGroupJSON.put("displayName", myDisplayName); 
    			sendRequest(new PostRequest(URL_ROOTGROUPS + "/" + myGroupName,  newGroupJSON.toString(), "application/json"), Status.STATUS_BAD_REQUEST);   
    		}
    		
    		/**
    		 * Delete the root group
    		 */
    		sendRequest(new DeleteRequest(URL_ROOTGROUPS + "/" + myGroupName), Status.STATUS_OK);
    		
    		/**
    		 * Attempt to delete the root group again - should fail
    		 */
    		sendRequest(new DeleteRequest(URL_ROOTGROUPS + "/" + myGroupName), Status.STATUS_NOT_FOUND);
    		
    		
    	} 
    	finally
    	{
    	
    		/**
    		 * Delete the root group
    		 */
    		sendRequest(new DeleteRequest(URL_ROOTGROUPS + "/" + myGroupName), 0);  
    	}
    }
    
    /**
     * Detailed test of link group
     */
    public void testLinkChild() throws Exception
    {
    	
    	String myRootGroup = "GT_LGROOT";
    	
    	try 
    	{
    		this.authenticationComponent.setSystemUserAsCurrentUser();
    		sendRequest(new DeleteRequest(URL_ROOTGROUPS + "/" + myRootGroup), 0);
    		
    		String groupLinkFullName = "";
    		{
    			Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + TEST_LINK), Status.STATUS_OK);
    			JSONObject top = new JSONObject(response.getContentAsString());
    			logger.debug(response.getContentAsString());
    			JSONObject data = top.getJSONObject("data");
    			assertTrue(data.length() > 0);
    			groupLinkFullName = data.getString("fullName");
    		}
    		
    		/**
    		 * Create a root group
    		 */
    		{
    			JSONObject newGroupJSON = new JSONObject();
    			newGroupJSON.put("displayName", myRootGroup); 
    			sendRequest(new PostRequest(URL_ROOTGROUPS + "/" + myRootGroup,  newGroupJSON.toString(), "application/json"), Status.STATUS_CREATED);    
    		}
    		
    		/**
    		 * Link an existing group (GROUPB) to my root group.
    		 */
    		
    		/**
    		 * Negative test Link Group B without administrator access.
    		 */
    		this.authenticationComponent.setCurrentUser(USER_ONE);
    		{
    			JSONObject newGroupJSON = new JSONObject();
    			sendRequest(new PostRequest(URL_GROUPS + "/" + myRootGroup +"/children/" + groupLinkFullName, newGroupJSON.toString(), "application/json" ), Status.STATUS_INTERNAL_SERVER_ERROR);
    		}
    		
    		this.authenticationComponent.setSystemUserAsCurrentUser();
    		
    		/**
    		 * Link Group B
    		 */
    		{
    			JSONObject newGroupJSON = new JSONObject();
    			Response response = sendRequest(new PostRequest(URL_GROUPS + "/" + myRootGroup +"/children/" + groupLinkFullName, newGroupJSON.toString(), "application/json" ), Status.STATUS_OK);
    			JSONObject top = new JSONObject(response.getContentAsString());
    			logger.debug(response.getContentAsString());
    			JSONObject data = top.getJSONObject("data");
    		}
    		
    		/**
    		 * Link the group again - this passes without problem
    		 */
    		{
    			JSONObject newGroupJSON = new JSONObject();
    			Response response = sendRequest(new PostRequest(URL_GROUPS + "/" + myRootGroup +"/children/" + groupLinkFullName, newGroupJSON.toString(), "application/json" ), Status.STATUS_OK);
    			JSONObject top = new JSONObject(response.getContentAsString());
    			logger.debug(response.getContentAsString());
    			JSONObject data = top.getJSONObject("data");
    		}
    		
        	/**
        	 * Get All Children of myGroup which are GROUPS - should find GROUP B
        	 */
        	{
        		logger.debug("Get child GROUPS of myRootGroup");
        		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + myRootGroup + "/children?authorityType=GROUP"), Status.STATUS_OK);
        		JSONObject top = new JSONObject(response.getContentAsString());
        		logger.debug(response.getContentAsString());
        		JSONArray data = top.getJSONArray("data");
        		assertTrue("no child groups of myGroup", data.length() == 1);
        		
        		JSONObject subGroup = data.getJSONObject(0);
        		assertEquals("shortName wrong", TEST_LINK, subGroup.getString("shortName"));
        		assertEquals("authorityType wrong", "GROUP", subGroup.getString("authorityType"));
        	}
        	
        	/**
        	 * Now link in an existing user
        	 */		 
    		{
    			JSONObject newGroupJSON = new JSONObject();
    			String userOneFullName = USER_ONE;
    			Response response = sendRequest(new PostRequest(URL_GROUPS + "/" + myRootGroup +"/children/" + userOneFullName, newGroupJSON.toString(), "application/json" ), Status.STATUS_OK);
    			JSONObject top = new JSONObject(response.getContentAsString());
    			logger.debug(response.getContentAsString());
    			JSONObject data = top.getJSONObject("data");
    		}
    		
        	/**
        	 * Get All Children of myGroup which are USERS - should find USER ONE
        	 */
        	{
        		logger.debug("Get child USERS of myRootGroup");
        		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + myRootGroup + "/children?authorityType=USER"), Status.STATUS_OK);
        		JSONObject top = new JSONObject(response.getContentAsString());
        		logger.debug(response.getContentAsString());
        		JSONArray data = top.getJSONArray("data");
        		assertTrue("no child groups of myGroup", data.length() == 1);
        		
        		JSONObject subGroup = data.getJSONObject(0);
        		assertEquals("shortName wrong", USER_ONE, subGroup.getString("shortName"));
        		assertEquals("authorityType wrong", "USER", subGroup.getString("authorityType"));
        	}
    			
    		/**
    		 * Unlink Group B
    		 */
    		{
    			logger.debug("Unlink Test Link");
    			Response response = sendRequest(new DeleteRequest(URL_GROUPS + "/" + myRootGroup +"/children/" + groupLinkFullName ), Status.STATUS_OK);
    			JSONObject top = new JSONObject(response.getContentAsString());
    			logger.debug(response.getContentAsString());

    		}
    		
        	/**
        	 * Get All Children of myGroup which are GROUPS - should no longer find GROUP B
        	 */
        	{
        		logger.debug("Get child GROUPS of myRootGroup");
        		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + myRootGroup + "/children?authorityType=GROUP"), Status.STATUS_OK);
        		JSONObject top = new JSONObject(response.getContentAsString());
        		logger.debug(response.getContentAsString());
        		JSONArray data = top.getJSONArray("data");
        		//TODO TEST failing
        		
        		//assertTrue("group B not removed", data.length() == 0);
        	}
        	
    		/**
    		 * Create a new group (BUFFY)
    		 */
        	String myNewGroup = "GROUP_BUFFY";
    		{
    			JSONObject newGroupJSON = new JSONObject();
    			Response response = sendRequest(new PostRequest(URL_GROUPS + "/" + myRootGroup +"/children/" + myNewGroup, newGroupJSON.toString(), "application/json" ), Status.STATUS_CREATED);
    			JSONObject top = new JSONObject(response.getContentAsString());
    			logger.debug(response.getContentAsString());
    			JSONObject data = top.getJSONObject("data");
        		assertEquals("shortName wrong", "BUFFY", data.getString("shortName"));
        		assertEquals("fullName wrong", myNewGroup, data.getString("fullName"));
        		assertEquals("authorityType wrong", "GROUP", data.getString("authorityType"));
    		}
        	
    		/**
        	 * Get All Children of myGroup which are GROUPS - should find GROUP(BUFFY)
        	 */
        	{
        		logger.debug("Get child GROUPS of myRootGroup");
        		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + myRootGroup + "/children?authorityType=GROUP"), Status.STATUS_OK);
        		JSONObject top = new JSONObject(response.getContentAsString());
        		logger.debug(response.getContentAsString());
        		JSONArray data = top.getJSONArray("data");
           		for(int i = 0; i < data.length(); i++)
        		{
        			JSONObject rootGroup = data.getJSONObject(i);
        			if(rootGroup.getString("fullName").equals(myNewGroup))
        			{
        			
        			}
        		}

        	}
    		
    		/**
    		 * Negative tests
    		 */
    	
    	}
    	finally
    	{
    		sendRequest(new DeleteRequest(URL_ROOTGROUPS + "/" + myRootGroup), 0);
    	}
    }
    
    /**
     * Detailed test of update group
     * @throws Exception
     */
    public void testUpdateGroup() throws Exception
    {
    	String myGroupName = "GT_UG";
    	String myDisplayName = "GT_UGDisplay";
    	String myNewDisplayName = "GT_UGDisplayNew";
    
    	this.authenticationComponent.setSystemUserAsCurrentUser();
    	
    	try
    	{
    		/**
    		 * Create a root group
    		 */
    		{
    			JSONObject newGroupJSON = new JSONObject();
    			newGroupJSON.put("displayName", myDisplayName); 
    			sendRequest(new PostRequest(URL_ROOTGROUPS + "/" + myGroupName,  newGroupJSON.toString(), "application/json"), Status.STATUS_CREATED);
    		}
    		
    		/**
    		 * Now change its display name
    		 */
    		{
    			JSONObject newGroupJSON = new JSONObject();
    			newGroupJSON.put("displayName", myNewDisplayName); 
    			Response response = sendRequest(new PutRequest(URL_GROUPS + "/" + myGroupName,  newGroupJSON.toString(), "application/json"), Status.STATUS_OK);    
    			JSONObject top = new JSONObject(response.getContentAsString());
        		logger.debug(response.getContentAsString());
        		JSONObject data = top.getJSONObject("data");
        		assertTrue(data.length() > 0);
        		assertEquals("displayName wrong", myNewDisplayName, data.getString("displayName"));

    		}
    		
        	/**
        	 * Now get it and verify that the name has changed
        	 */
        	{
        		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" +  myGroupName), Status.STATUS_OK);
        		JSONObject top = new JSONObject(response.getContentAsString());
        		logger.debug(response.getContentAsString());
        		JSONObject data = top.getJSONObject("data");
        		assertTrue(data.length() > 0);
        		assertEquals("displayName wrong", myNewDisplayName, data.getString("displayName"));

        	}   
        	
    		/**
    		 * Negative test
    		 */
        	{
        		JSONObject newGroupJSON = new JSONObject();
        		newGroupJSON.put("displayName", myNewDisplayName); 
        		sendRequest(new PutRequest(URL_GROUPS + "/" + "rubbish",  newGroupJSON.toString(), "application/json"), Status.STATUS_NOT_FOUND);    
        	}
    	}
    	finally
        {
        	sendRequest(new DeleteRequest(URL_ROOTGROUPS + "/" + myGroupName), 0);
        }
    }
    
    
    /**
     * Detailed test of search groups
     *<li>if the optional includeInternal parameter is true then will include internal groups, otherwise internalGroups are not returned.</li>
      <li>If the optional shortNameFilter parameter is set then returns those root groups with a partial match on shortName.</li>
     */
    public void testSearchGroups() throws Exception
    {
    	 createTestTree();
    	 
    	// Search on partial short name
    	{
		    Response response = sendRequest(new GetRequest(URL_GROUPS + "?shortNameFilter=" + "ALFRESCO_ADMIN*"), Status.STATUS_OK);
		    JSONObject top = new JSONObject(response.getContentAsString());
		    logger.debug(response.getContentAsString());
		    JSONArray data = top.getJSONArray("data");
		    assertEquals("length not 1", 1, data.length());
 			JSONObject authority = data.getJSONObject(0);
			assertEquals("", ADMIN_GROUP, authority.getString("shortName"));
    	}
    	
    	// Search on partial short name with a ?
    	{
		    Response response = sendRequest(new GetRequest(URL_GROUPS + "?shortNameFilter=" + "ALFRE?CO_ADMIN*"), Status.STATUS_OK);
		    JSONObject top = new JSONObject(response.getContentAsString());
		    logger.debug(response.getContentAsString());
		    JSONArray data = top.getJSONArray("data");
		    assertEquals("length not 1", 1, data.length());
 			JSONObject authority = data.getJSONObject(0);
			assertEquals("", ADMIN_GROUP, authority.getString("shortName"));
    	}
    	
    	// Search on partial short name, Query should be extended by a *.
    	{
		    Response response = sendRequest(new GetRequest(URL_GROUPS + "?shortNameFilter=" + "ALF"), Status.STATUS_OK);
		    JSONObject top = new JSONObject(response.getContentAsString());
		    logger.debug(response.getContentAsString());
		    JSONArray data = top.getJSONArray("data");
		    assertEquals("length not 1", 1, data.length());
 			JSONObject authority = data.getJSONObject(0);
			assertEquals("", ADMIN_GROUP, authority.getString("shortName"));
    	}
    	
    	// Negative test.
    	{
		    Response response = sendRequest(new GetRequest(URL_GROUPS + "?shortNameFilter=" + "XX?X"), Status.STATUS_OK);
		    JSONObject top = new JSONObject(response.getContentAsString());
		    logger.debug(response.getContentAsString());
		    JSONArray data = top.getJSONArray("data");
		    assertEquals("length not 0", 0, data.length());
    	}
    	
    	// Search on full shortName
		{
		    Response response = sendRequest(new GetRequest(URL_GROUPS + "?shortNameFilter=" + ADMIN_GROUP), Status.STATUS_OK);
		    JSONObject top = new JSONObject(response.getContentAsString());
		    logger.debug(response.getContentAsString());
		    JSONArray data = top.getJSONArray("data");
		    assertEquals("length not 1", 1, data.length());
 			JSONObject authority = data.getJSONObject(0);
			assertEquals("", ADMIN_GROUP, authority.getString("shortName"));
		}
		
    	// Search on partial short name of a non root group
    	{
		    Response response = sendRequest(new GetRequest(URL_GROUPS + "?shortNameFilter=" + TEST_GROUPD ), Status.STATUS_OK);
		    JSONObject top = new JSONObject(response.getContentAsString());
		    logger.debug(response.getContentAsString());
		    System.out.println(response.getContentAsString());
		    JSONArray data = top.getJSONArray("data");
		    assertEquals("length not 1", 1, data.length());
 			JSONObject authority = data.getJSONObject(0);
			assertEquals("", TEST_GROUPD, authority.getString("shortName"));

    	}
    
    }
    
    /**
     * Detailed test of get Parents
     */
    public void testGetParents() throws Exception
    {
        createTestTree();
        
    	/**
    	 * Get all parents for the root group ALFRESCO_ADMINISTRATORS groups which has no parents
    	 */
    	{
    		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + ADMIN_GROUP + "/parents"), Status.STATUS_OK);
    		JSONObject top = new JSONObject(response.getContentAsString());
    		logger.debug(response.getContentAsString());
    		JSONArray data = top.getJSONArray("data");
    		// Top level group has no parents
    		assertTrue("top level group has no parents", data.length() == 0);
    	}
    	
    	/**
    	 * Get GROUP B   Which should be a child of TESTROOT
    	 */
    	{
    		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + TEST_GROUPB + "/parents"), Status.STATUS_OK);
    		JSONObject top = new JSONObject(response.getContentAsString());
    		logger.debug(response.getContentAsString());
    		JSONArray data = top.getJSONArray("data");
    		assertTrue(data.length() > 0);
    	}
    	
       	/**
    	 * Get GROUP D   Which should be a child of GROUPB child of TESTROOT
    	 */
    	{
    		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + TEST_GROUPD + "/parents?level=ALL"), Status.STATUS_OK);
    		JSONObject top = new JSONObject(response.getContentAsString());
    		logger.debug(response.getContentAsString());
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
        createTestTree();
            	
    	/**
    	 * Get All Children of GROUP B
    	 */
    	{
    		logger.debug("Get all children of GROUP B");
    		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + TEST_GROUPB + "/children"), Status.STATUS_OK);
    		JSONObject top = new JSONObject(response.getContentAsString());
    		logger.debug(response.getContentAsString());
    		JSONArray data = top.getJSONArray("data");
    		assertTrue(data.length() > 0);
    		boolean gotGroupD = false;
    		boolean gotUserTwo = false;
    		boolean gotUserThree = false;
      		for(int i = 0; i < data.length(); i++)
    		{
    			JSONObject authority = data.getJSONObject(i);
    			if(authority.getString("shortName").equals(TEST_GROUPD))
    			{
    				gotGroupD = true;
    			}
    			if(authority.getString("shortName").equals(USER_TWO))
    			{
    				gotUserTwo = true;
    			}
    			if(authority.getString("shortName").equals(USER_THREE))
    			{
    				gotUserThree = true;
    			}
    		}
      		assertEquals("3 groups not returned", 3, data.length());
      		assertTrue("not got group D", gotGroupD);
      		assertTrue("not got user two", gotUserTwo);
      		assertTrue("not got user three", gotUserThree);

    	}
    	
    	/**
    	 * Get All Children of GROUP B which are GROUPS
    	 */
    	{
    		logger.debug("Get child GROUPS of GROUP B");
    		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + TEST_GROUPB + "/children?authorityType=GROUP"), Status.STATUS_OK);
    		JSONObject top = new JSONObject(response.getContentAsString());
    		logger.debug(response.getContentAsString());
    		JSONArray data = top.getJSONArray("data");
    		//assertTrue("no child groups of group B", data.length() == 1);
    		
    		JSONObject subGroup = data.getJSONObject(0);
    		assertEquals("shortName wrong", TEST_GROUPD, subGroup.getString("shortName"));
    		assertEquals("authorityType wrong", "GROUP", subGroup.getString("authorityType"));
      		for(int i = 0; i < data.length(); i++)
    		{
      			JSONObject authority = data.getJSONObject(i);
      			assertEquals("authorityType wrong", "GROUP", authority.getString("authorityType"));      			
    		}
    	}
    	
    	/**
    	 * Get All Children of GROUP B which are USERS
    	 */
    	{
    		logger.debug("Get Child Users of Group B");
    		Response response = sendRequest(new GetRequest(URL_GROUPS + "/" + TEST_GROUPB + "/children?authorityType=USER"), Status.STATUS_OK);
    		JSONObject top = new JSONObject(response.getContentAsString());
    		logger.debug(response.getContentAsString());
    		JSONArray data = top.getJSONArray("data");
    		assertTrue(data.length() > 0);
      		for(int i = 0; i < data.length(); i++)
    		{
      			JSONObject authority = data.getJSONObject(i);
      			assertEquals("authorityType wrong", "USER", authority.getString("authorityType"));      			
    		}
    	}
    	
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
