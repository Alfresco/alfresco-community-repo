package org.alfresco.repo.web.scripts.search;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private static Log logger = LogFactory.getLog(PersonSearchTest.class);
    
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    
    private static final String USER_ONE = "PersonSearchTestOne";
    private static final String USER_TWO = "PersonSearchTestTwo";
    private static final String USER_THREE = "PersonSearchTestThree";
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
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
     */
    public void testSearch() throws Exception
    {
    	/*
    	 * Do the first query for default format and all results
    	 */
    	{
    		Response response = sendRequest(new GetRequest("/api/search/person?q=*"), Status.STATUS_OK);
        	logger.debug(response.getContentAsString());
    	}
    	
    	/*
    	 * Same search with HTML format
    	 */
    	{
    		Response response = sendRequest(new GetRequest("/api/search/person.html?q=*"), Status.STATUS_OK);
        	logger.debug(response.getContentAsString());
    	}

    	    	
    	/*
    	 * Negative test - missing mandatory parameter 
    	 * 
    	 * Should really be a INVALID_REQUEST
    	 */
    	sendRequest(new GetRequest("/api/search/person?"), Status.STATUS_INTERNAL_SERVER_ERROR);
    }
    
    public void testPortletSearch() throws Exception
    {
		Response response = sendRequest(new GetRequest("/api/search/person.portlet?q=*"), Status.STATUS_OK);
    	logger.debug(response.getContentAsString());
    }
    
    public void testAtomSearch() throws Exception
    {
		Response response = sendRequest(new GetRequest("/api/search/person.atom?q=*"), Status.STATUS_OK);
    	logger.debug(response.getContentAsString());
    }
}
