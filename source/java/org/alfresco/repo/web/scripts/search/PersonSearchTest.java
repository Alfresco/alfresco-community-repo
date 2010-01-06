/*
 * Copyright (C) 2009-2009 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.search;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Unit test for PersonSearch Web Script.
 * 
 *  /alfresco/service/api/search/person?q=* 
 * @author Mark Rogers
 */
public class PersonSearchTest extends BaseWebScriptTest
{    
    private AuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    private AuthorityService authorityService;
    
    private static final String USER_ONE = "PersonSearchTestOne";
    private static final String USER_TWO = "PersonSearchTestTwo";
    private static final String USER_THREE = "PersonSearchTestThree";
    
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
    }

    /**
     * This is a basic sanity check of the search/person script.
     * @throws Exception
     */
    public void testSearch() throws Exception
    {
    	/**
    	 * Do the first query for default format and all results
    	 */
    	{
    		Response response = sendRequest(new GetRequest("/api/search/person?q=*"), Status.STATUS_OK);
//        	JSONObject top = new JSONObject(response.getContentAsString());
//        	logger.debug(response.getContentAsString());
        	System.out.println(response.getContentAsString());
    	}
    	
    	/**
    	 * Same search with HTML format
    	 */
    	{
    		Response response = sendRequest(new GetRequest("/api/search/person.html?q=*"), Status.STATUS_OK);
//        	JSONObject top = new JSONObject(response.getContentAsString());
//        	logger.debug(response.getContentAsString());
        	System.out.println(response.getContentAsString());
    	}

    	    	
    	/**
    	 * Negative test - missing mandatory parameter 
    	 * 
    	 * Should really be a INVALID_REQUEST
    	 */
    	sendRequest(new GetRequest("/api/search/person?"), Status.STATUS_INTERNAL_SERVER_ERROR);
    }
    
    public void testPortletSearch() throws Exception
    {
    	/**
    	 * Do the first query for default format and all results
    	 */
    	{
    		Response response = sendRequest(new GetRequest("/api/search/person.portlet?q=*"), Status.STATUS_OK);
//        	JSONObject top = new JSONObject(response.getContentAsString());
//        	logger.debug(response.getContentAsString());
        	System.out.println(response.getContentAsString());
    	}
    }
    
    public void testAtomSearch() throws Exception
    {
    	/**
    	 * Do the first query for default format and all results
    	 */
    	{
    		Response response = sendRequest(new GetRequest("/api/search/person.atom?q=*"), Status.STATUS_OK);
//        	JSONObject top = new JSONObject(response.getContentAsString());
//        	logger.debug(response.getContentAsString());
        	System.out.println(response.getContentAsString());
    	}
    }

    
    
}
