/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.FormException;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.CronTriggerBean;
import org.alfresco.util.PropertyMap;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import static org.mockito.Matchers.any;
import static org.springframework.extensions.webscripts.Status.STATUS_OK;

/**
 * Unit test to test runas function
 * 
 * @author David Ward
 */
public class RepositoryContainerTest extends BaseWebScriptTest
{
    private static final Pattern HIDDEN_EXCEPTION_PATTERN = Pattern.compile("Server error \\(\\d{8}\\)\\.  Details can be found in the server logs\\.");

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

    public void testHideExceptions() throws Exception
    {
        final String messageFormException = "Failed to persist field 'prop_cm_name'";
        final String messageAuthenticationException = "Authentication failed for Web Script";

        RepositoryContainer repoContainer = (RepositoryContainer) getServer().getApplicationContext().getBean("webscripts.container");
        RepositoryContainer repoContainerMock = Mockito.spy(repoContainer);

        Class<?>[] defaultConfiguration = repoContainerMock.getNotPublicExceptions();

        try
        {
            List<Class<?>> testExceptoins = new LinkedList<Class<?>>();
            testExceptoins.add(SQLException.class);
            repoContainerMock.setNotPublicExceptions(testExceptoins);

            // case: AlfrescoRuntimeException with SQLException cause
            Mockito.doAnswer(new Answer<Object>()
            {
                public Object answer(InvocationOnMock invocation)
                {
                    throw new AlfrescoRuntimeException("AlfrescoRuntimeException", new SQLException("SQLException"));
                }
            }).when(repoContainerMock).executeScriptInternal(any(WebScriptRequest.class), any(WebScriptResponse.class), any(Authenticator.class));
            try
            {
                repoContainerMock.executeScript(null, null, null);
            }
            catch (Exception e)
            {
                assertNull("SQLException cause should be hidden for client", ExceptionStackUtil.getCause(e, new Class[] { SQLException.class }));
                assertTrue("Details should be in the server logs.", HIDDEN_EXCEPTION_PATTERN.matcher(e.getMessage()).matches());
            }

            // case: AlfrescoRuntimeException with NOT SQLException cause
            Mockito.doAnswer(new Answer<Object>()
            {
                public Object answer(InvocationOnMock invocation)
                {
                    throw new AlfrescoRuntimeException("AlfrescoRuntimeException", new NullPointerException());
                }
            }).when(repoContainerMock).executeScriptInternal(any(WebScriptRequest.class), any(WebScriptResponse.class), any(Authenticator.class));
            try
            {
                repoContainerMock.executeScript(null, null, null);
            }
            catch (Exception e)
            {
                assertNotNull("NullPointerException cause should be visible for client", ExceptionStackUtil.getCause(e, new Class[] { NullPointerException.class }));
                assertFalse("Details should be available for client", HIDDEN_EXCEPTION_PATTERN.matcher(e.getMessage()).matches());
            }

            // case: RuntimeException with SQLException cause
            Mockito.doAnswer(new Answer<Object>()
            {
                public Object answer(InvocationOnMock invocation)
                {
                    throw new RuntimeException("AlfrescoRuntimeException", new SQLException("SQLException"));
                }
            }).when(repoContainerMock).executeScriptInternal(any(WebScriptRequest.class), any(WebScriptResponse.class), any(Authenticator.class));
            try
            {
                repoContainerMock.executeScript(null, null, null);
            }
            catch (Exception e)
            {
                assertNull("SQLException cause should be hidden for client", ExceptionStackUtil.getCause(e, new Class[] { SQLException.class }));
                assertTrue("Details should be in the server logs.", HIDDEN_EXCEPTION_PATTERN.matcher(e.getMessage()).matches());
            }

            // case: FormException
            Mockito.doAnswer(new Answer<Object>()
            {
                public Object answer(InvocationOnMock invocation)
                {
                    throw new FormException(messageFormException);
                }
            }).when(repoContainerMock).executeScriptInternal(any(WebScriptRequest.class), any(WebScriptResponse.class), any(Authenticator.class));
            try
            {
                repoContainerMock.executeScript(null, null, null);
            }
            catch (Exception e)
            {
                assertTrue("FormException should be visible for client", e instanceof FormException);
                assertFalse("Details should be available for client", HIDDEN_EXCEPTION_PATTERN.matcher(e.getMessage()).matches());
                assertTrue("Actual message should be available for client", e.getMessage().contains(messageFormException));
            }

            // case: WebScriptException
            Mockito.doAnswer(new Answer<Object>()
            {
                public Object answer(InvocationOnMock invocation)
                {
                    throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, messageAuthenticationException);
                }
            }).when(repoContainerMock).executeScriptInternal(any(WebScriptRequest.class), any(WebScriptResponse.class), any(Authenticator.class));
            try
            {
                repoContainerMock.executeScript(null, null, null);
            }
            catch (Exception e)
            {
                assertTrue("WebScriptException should be visible for client", e instanceof WebScriptException);
                assertFalse("Details should be available for client", HIDDEN_EXCEPTION_PATTERN.matcher(e.getMessage()).matches());
                assertTrue("Actual message should be available for client", e.getMessage().contains(messageAuthenticationException));
            }
        }
        finally
        {
            repoContainerMock.setNotPublicExceptions(Arrays.asList(defaultConfiguration));
        }
    }

    public void testHideExceptionsConfiguration() throws Exception
    {
        final StringBuilder commandExecutionResult = new StringBuilder(128);
        commandExecutionResult.append("Execution result: \n").append("   os:         ").append(System.getProperty("os.name")).append("\n").append("   command:    <TEST_COMMAND>")
                .append("   succeeded:  ").append(false).append("\n").append("   exit code:  ").append(1).append("\n").append("   out:        ").append("<EMPTY>").append("\n")
                .append("   err:        ").append("ERROR");

        RepositoryContainer repoContainer = (RepositoryContainer) getServer().getApplicationContext().getBean("webscripts.container");
        RepositoryContainer repoContainerMock = Mockito.spy(repoContainer);

        Class<?>[] notPublicExceptions = repoContainerMock.getNotPublicExceptions();
        List<Class<?>> defaultConfiguration = Arrays.asList(notPublicExceptions);

        List<Class<?>> testExceptions = new LinkedList<Class<?>>();
        testExceptions.add(SQLException.class);
        testExceptions.add(ContentIOException.class);
        repoContainerMock.setNotPublicExceptions(testExceptions);

        try
        {
            // Case: MNT-12794 - test default configuration
            Mockito.doAnswer(new Answer<Object>()
            {
                public Object answer(InvocationOnMock invocation)
                {
                    throw new ContentIOException(commandExecutionResult.toString());
                }
            }).when(repoContainerMock).executeScriptInternal(any(WebScriptRequest.class), any(WebScriptResponse.class), any(Authenticator.class));

            try
            {
                repoContainerMock.executeScript(null, null, null);
            }
            catch (Exception e)
            {
                assertNull("'ContentIOException' cause should be hidden for client", ExceptionStackUtil.getCause(e, new Class[] { ContentIOException.class }));
                assertTrue("Details should be in the server logs.", HIDDEN_EXCEPTION_PATTERN.matcher(e.getMessage()).matches());
            }

            // Case: MNT-12794 - ContentIOException in AlfrescoRuntimeException and default configuration
            Mockito.doAnswer(new Answer<Object>()
            {
                public Object answer(InvocationOnMock invocation)
                {

                    throw new AlfrescoRuntimeException("AlfrescoRuntimeException", new ContentIOException(commandExecutionResult.toString()));
                }
            }).when(repoContainerMock).executeScriptInternal(any(WebScriptRequest.class), any(WebScriptResponse.class), any(Authenticator.class));

            try
            {
                repoContainerMock.executeScript(null, null, null);
            }
            catch (Exception e)
            {
                assertNull("'ContentIOException' cause should be hidden for client", ExceptionStackUtil.getCause(e, new Class[] { ContentIOException.class }));
                assertTrue("Details should be in the server logs.", HIDDEN_EXCEPTION_PATTERN.matcher(e.getMessage()).matches());
            }

            // case: AlfrescoRuntimeException without ContentIOException
            Mockito.doAnswer(new Answer<Object>()
            {
                public Object answer(InvocationOnMock invocation)
                {
                    throw new AlfrescoRuntimeException("AlfrescoRuntimeException", new NullPointerException("NullPointerException"));
                }
            }).when(repoContainerMock).executeScriptInternal(any(WebScriptRequest.class), any(WebScriptResponse.class), any(Authenticator.class));

            try
            {
                repoContainerMock.executeScript(null, null, null);
            }
            catch (Exception e)
            {
                assertNotNull("'NullPointerException' cause should be visible for client", ExceptionStackUtil.getCause(e, new Class[] { NullPointerException.class }));
                assertFalse("Details should be available for client", HIDDEN_EXCEPTION_PATTERN.matcher(e.getMessage()).matches());
            }

            // case: RuntimeException with several different exceptions which should be shown
            List<Class<?>> updatedNotPublicExceptions = new LinkedList<Class<?>>(defaultConfiguration);
            updatedNotPublicExceptions.add(NullPointerException.class);
            repoContainerMock.setNotPublicExceptions(updatedNotPublicExceptions);

            Mockito.doAnswer(new Answer<Object>()
            {
                public Object answer(InvocationOnMock invocation)
                {
                    throw new AlfrescoRuntimeException("AlfrescoRuntimeException", new NullPointerException("NullPointerException"));
                }
            }).when(repoContainerMock).executeScriptInternal(any(WebScriptRequest.class), any(WebScriptResponse.class), any(Authenticator.class));

            try
            {
                repoContainerMock.executeScript(null, null, null);
            }
            catch (Exception e)
            {
                assertNull("'NullPointerException' cause should be hidden for client", ExceptionStackUtil.getCause(e, new Class[] { NullPointerException.class }));
                assertTrue("Details should be in the server logs.", HIDDEN_EXCEPTION_PATTERN.matcher(e.getMessage()).matches());
            }

            // case: all exceptions must be shown
            repoContainerMock.setNotPublicExceptions(null);

            Mockito.doAnswer(new Answer<Object>()
            {
                public Object answer(InvocationOnMock invocation)
                {
                    throw new AlfrescoRuntimeException("AlfrescoRuntimeException", new ContentIOException(commandExecutionResult.toString(), new NullPointerException(
                            "NullPointerException")));
                }
            }).when(repoContainerMock).executeScriptInternal(any(WebScriptRequest.class), any(WebScriptResponse.class), any(Authenticator.class));

            try
            {
                repoContainerMock.executeScript(null, null, null);
            }
            catch (Exception e)
            {
                assertTrue("'AlfrescoRuntimeException' cause should not be hidden for client. Exception class: " + e.getClass().getName(), e instanceof AlfrescoRuntimeException);
                assertNotNull("'ContentIOException' cause should not be hidden for client", ExceptionStackUtil.getCause(e, new Class[] { ContentIOException.class }));
                assertNotNull("'NullPointerException' cause should not be hidden for client", ExceptionStackUtil.getCause(e, new Class[] { NullPointerException.class }));
                assertFalse("Details should be available for client", HIDDEN_EXCEPTION_PATTERN.matcher(e.getMessage()).matches());
            }

            // case: all exceptions must be shown
            repoContainerMock.setNotPublicExceptions(defaultConfiguration);
            repoContainerMock.setNotPublicExceptions(new LinkedList<Class<?>>());

            Mockito.doAnswer(new Answer<Object>()
            {
                public Object answer(InvocationOnMock invocation)
                {
                    throw new AlfrescoRuntimeException("AlfrescoRuntimeException", new ContentIOException(commandExecutionResult.toString(), new NullPointerException(
                            "NullPointerException")));
                }
            }).when(repoContainerMock).executeScriptInternal(any(WebScriptRequest.class), any(WebScriptResponse.class), any(Authenticator.class));

            try
            {
                repoContainerMock.executeScript(null, null, null);
            }
            catch (Exception e)
            {
                assertTrue("'AlfrescoRuntimeException' cause should not be hidden for client. Exception class: " + e.getClass().getName(), e instanceof AlfrescoRuntimeException);
                assertNotNull("'ContentIOException' cause should not be hidden for client", ExceptionStackUtil.getCause(e, new Class[] { ContentIOException.class }));
                assertNotNull("'NullPointerException' cause should not be hidden for client", ExceptionStackUtil.getCause(e, new Class[] { NullPointerException.class }));
                assertFalse("Details should be available for client", HIDDEN_EXCEPTION_PATTERN.matcher(e.getMessage()).matches());
            }
        }
        finally
        {
            repoContainerMock.setNotPublicExceptions(defaultConfiguration);
        }
    }
}
