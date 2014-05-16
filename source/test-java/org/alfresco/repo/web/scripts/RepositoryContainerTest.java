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
package org.alfresco.repo.web.scripts;

import java.util.Arrays;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.CronTriggerBean;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import static org.springframework.extensions.webscripts.Status.*;

/**
 * Unit test to test runas function
 * 
 * @author David Ward
 */
public class RepositoryContainerTest extends BaseWebScriptTest
{
    private MutableAuthenticationService authenticationService;
    private PersonService personService;
    private AuthenticationComponent authenticationComponent;
    
    private static final String USER_ONE = "RunAsOne";
    private static final String USER_TWO = "RunAsTwo";
    private static final String SUCCESS = "success";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.authenticationService = (MutableAuthenticationService) getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService) getServer().getApplicationContext().getBean("PersonService");

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        // Create users
        createUser(USER_ONE);
        createUser(USER_TWO);
    }

    private void createUser(String userName)
    {
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            this.authenticationService.createAuthentication(userName, "PWD".toCharArray());

            PropertyMap ppOne = new PropertyMap(5);
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
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
    }
    
    
    /**
     * Person should be current user irrespective of runas user.
     */
    public void testRunAsAdmin() throws Exception
    {
        authenticationComponent.setCurrentUser(USER_ONE);
        
        // No runas specified within our webscript descriptor
        Response response = sendRequest(new GetRequest("/test/runas"), STATUS_OK);
        assertEquals(USER_ONE, response.getContentAsString());

        authenticationComponent.setCurrentUser(USER_TWO);
        
        // runas "Admin" specified within our webscript descriptor
        response = sendRequest(new GetRequest("/test/runasadmin"), STATUS_OK);
        assertEquals(USER_TWO, response.getContentAsString());
        
        authenticationComponent.setSystemUserAsCurrentUser();
    }

    
    public void testReset() throws Exception
    {
        RepositoryContainer repoContainer = (RepositoryContainer) getServer().getApplicationContext().getBean("webscripts.container");
        repoContainer.reset();
    }
    
    /*
     * Test for MNT-11237 : CMIS uploading file larger the 4mb fails
     * 
     * Tests that requests with content larger than 4mb (default memoryThreshold value) can be successfully handled by repository container 
     */
    public void testLargeContentRequest() throws Exception
    {
        authenticationComponent.setCurrentUser(USER_ONE);
        
        // create the 5 mb size buffer of zero bytes
        byte[] content = new byte[5 * 1024 * 1024];
        Arrays.fill(content, (byte)0);
        
        // chek that we can upload file larger than 4 mb
        Response response = sendRequest(new PutRequest("/test/largecontenttest", content, "text/plain"), STATUS_OK);
        assertEquals(SUCCESS, response.getContentAsString());
        
        // trigger the webscript temp folder cleaner job
        CronTriggerBean webscriptsTempFileCleanerJobTrigger = (CronTriggerBean) getServer().getApplicationContext().getBean("webscripts.tempFileCleanerTrigger");
        
        webscriptsTempFileCleanerJobTrigger.getScheduler().triggerJobWithVolatileTrigger(
                webscriptsTempFileCleanerJobTrigger.getJobDetail().getName(),
                webscriptsTempFileCleanerJobTrigger.getJobDetail().getGroup(),
                webscriptsTempFileCleanerJobTrigger.getJobDetail().getJobDataMap());
        
        // check that we still can upload file larger than 4 mb, i.e. ensure that cleaner has not deleted temp folder
        response = sendRequest(new PutRequest("/test/largecontenttest", content, "text/plain"), STATUS_OK);
        assertEquals(SUCCESS, response.getContentAsString());
    }
}
