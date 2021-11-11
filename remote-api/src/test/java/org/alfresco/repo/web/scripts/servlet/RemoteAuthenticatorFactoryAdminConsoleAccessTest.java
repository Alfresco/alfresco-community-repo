/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.web.scripts.servlet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.management.subsystems.DefaultChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.external.DefaultRemoteUserMapper;
import org.alfresco.repo.security.authentication.external.RemoteUserMapper;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.testing.category.IntermittentlyFailingTests;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.Description;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScript;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration({ "classpath:alfresco/application-context.xml", "classpath:alfresco/web-scripts-application-context.xml",
    "classpath:alfresco/web-scripts-application-context-test.xml" })
public class RemoteAuthenticatorFactoryAdminConsoleAccessTest extends BaseSpringTest
{
    private String proxyHeader = "X-Alfresco-Remote-User";
    public static int setStatusCode;

    protected final Log logger = LogFactory.getLog(getClass());

    private RemoteUserAuthenticatorFactory remoteUserAuthenticatorFactory;
    private BlockingRemoteUserMapper blockingRemoteUserMapper;
    private PersonService personService;
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private AuthorityService authorityService;

    @Before
    public void before() throws Exception
    {
        blockingRemoteUserMapper = new BlockingRemoteUserMapper();

        DefaultChildApplicationContextManager childApplicationContextManager = (DefaultChildApplicationContextManager) applicationContext
            .getBean("Authentication");
        remoteUserAuthenticatorFactory = (RemoteUserAuthenticatorFactory) applicationContext.getBean("webscripts.authenticator.remoteuser");
        remoteUserAuthenticatorFactory.setRemoteUserMapper(blockingRemoteUserMapper);
        remoteUserAuthenticatorFactory
            .setGetRemoteUserTimeoutMilliseconds((long) (BlockingRemoteUserMapper.BLOCKING_FOR_MILLIS / 2));//highly impatient
        personService = (PersonService) applicationContext.getBean("PersonService");

        authenticationService = applicationContext.getBean("AuthenticationService", MutableAuthenticationService.class);
        authenticationComponent = applicationContext.getBean("AuthenticationComponent", AuthenticationComponent.class);
        authorityService = applicationContext.getBean("AuthorityService", AuthorityService.class);

        childApplicationContextManager.stop();
        childApplicationContextManager.setProperty("chain", "external1:external,alfrescoNtlm1:alfrescoNtlm");
        ChildApplicationContextFactory childApplicationContextFactory = childApplicationContextManager.getChildApplicationContextFactory("external1");
        childApplicationContextFactory.stop();
        childApplicationContextFactory.setProperty("external.authentication.proxyUserName", "");
    }

    @Test
    public void testAdminGuestAccess()
    {
        RequiredAuthentication required = RequiredAuthentication.admin;
        boolean isGuest = true;

        final boolean authenticated = authenticateWithGuestParameters(required, isGuest);

        assertFalse("This should not authenticate. Admin access and guest is not a valid combination.", authenticated);
        checkTimeOutFeaturesWasNotUsed();
    }

    @Test
    public void testUserGuestAccess()
    {
        RequiredAuthentication required = RequiredAuthentication.user;
        boolean isGuest = true;

        final boolean authenticated = authenticateWithGuestParameters(required, isGuest);

        assertFalse("This should not authenticate", authenticated);
        checkTimeOutFeaturesWasNotUsed();
    }

    @Test
    public void testGuestGuestAccess()
    {
        RequiredAuthentication required = RequiredAuthentication.guest;
        boolean isGuest = true;

        final boolean authenticated = authenticateWithGuestParameters(required, isGuest);

        assertTrue("This should authenticate", authenticated);
        checkTimeOutFeaturesWasNotUsed();
    }

    @Test
    public void testNoneGuestAccess()
    {
        RequiredAuthentication required = RequiredAuthentication.none;
        boolean isGuest = true;

        final boolean authenticated = authenticateWithGuestParameters(required, isGuest);

        assertFalse("This should not authenticate.", authenticated);
        checkTimeOutFeaturesWasNotUsed();
    }

    @Test
    public void testNoneNotGuestAccess()
    {
        RequiredAuthentication required = RequiredAuthentication.none;
        boolean isGuest = false;

        final boolean authenticated = authenticateWithGuestParameters(RequiredAuthentication.none, false);

        assertFalse("This should not authenticate.", authenticated);
        checkTimeOutFeaturesWasNotUsed();
    }

    @Test
    public void testExternalAuthForAdminPage()
    {
        RequiredAuthentication required = RequiredAuthentication.admin;
        Set<String> families = new HashSet<>();
        families.add("AdminConsole");

        checkExtAuthStillWorks(required, families);
    }

    @Test
    public void testExternalAuthForAdminResource()
    {
        RequiredAuthentication required = RequiredAuthentication.admin;
        Set<String> families = Collections.emptySet();

        checkExtAuthStillWorks(required, families);
    }

    @Test
    public void testExternalAuthForUserResource()
    {
        RequiredAuthentication required = RequiredAuthentication.user;
        Set<String> families = Collections.emptySet();

        checkExtAuthStillWorks(required, families);
    }

    @Test
    public void testAdminCanAccessAdminConsoleScript()
    {
        Set<String> families = new HashSet<>();
        families.add("AdminConsole");
        complexCheckOfScriptCases(families);
    }

    @Category(IntermittentlyFailingTests.class) // ACS-959
    @Test
    public void testAdminCanAccessAdminConsoleHelperScript()
    {
        Set<String> families = new HashSet<>();
        families.add("AdminConsoleHelper");
        complexCheckOfScriptCases(families);
    }

    /**
     * Tested access to the AdminConsole for an non literal admin user
     * but with admin permissions (user added to ALFRESCO_ADMINISTRATORS)
     * and accessing via Basic Auth
     */
    @Test
    public void testUserCanAccessAdminConsoleScript()
    {
        Set<String> families = new HashSet<>();
        families.add("AdminConsole");

        // Run as System to be able to give permissions
        authenticationComponent.setSystemUserAsCurrentUser();

        String username = RandomStringUtils.randomAlphabetic(10);
        String password = RandomStringUtils.randomAlphabetic(10);
        createUser(username, password);
        authorityService.addAuthority("GROUP_ALFRESCO_ADMINISTRATORS", username);

        authenticationComponent.clearCurrentSecurityContext();

        String headerToAdd = getBasicAuthHeader(username, password);

        checkAdminConsoleFamilyWithBasicAuthHeaderPresentUser(families, headerToAdd);

    }

    private void complexCheckOfScriptCases(final Set<String> families)
    {
        final String headerToAdd = "Basic YWRtaW46YWRtaW4="; //admin:admin

        checkGenericResourceAccess();

        checkAdminConsoleFamilyPage(families);

        checkAdminConsoleFamilyPageWithRemoteUserMapperDisabled(families);

        checkAdminConsoleFamilyWithBasicAuthHeaderPresent(families, headerToAdd);

        checkAdminConsoleFamilyWithBasicAuthHeaderButWrongPassword(families);
    }

    private void checkAdminConsoleFamilyWithBasicAuthHeaderButWrongPassword(Set<String> families)
    {
        blockingRemoteUserMapper.reset();
        // now try with bad password
        String headerToAddWithWrongPassword = "Basic YWRtaW46YmliaQ=="; // admin:bibi
        final boolean authenticated = authenticate(families, headerToAddWithWrongPassword);

        assertFalse("It is an AdminConsole webscript now and Admin basic auth header was present BUT with wrong password. Should fail.",
            authenticated);
        assertEquals("Status should be 401", 401, setStatusCode);
        final String message = "The code from blockingRemoteUserMapper shouldn't have been called";
        assertFalse(message, blockingRemoteUserMapper.isWasInterrupted());
        assertEquals(message, blockingRemoteUserMapper.getTimePassed(), 0);
    }

    private void checkAdminConsoleFamilyPage(Set<String> families)
    {
        blockingRemoteUserMapper.reset();
        // now try an admin console family page
        final boolean authenticated = authenticate(families, null);

        assertFalse("It is an AdminConsole webscript now, but Admin basic auth header was not present. It should return 401", authenticated);
        assertEquals("Status should be 401", 401, setStatusCode);
        assertTrue("Because it is an AdminConsole webscript, the interrupt should have been called.", blockingRemoteUserMapper.isWasInterrupted());
        assertTrue("The interrupt should have been called.", blockingRemoteUserMapper.getTimePassed() < BlockingRemoteUserMapper.BLOCKING_FOR_MILLIS);
    }

    private void checkAdminConsoleFamilyWithBasicAuthHeaderPresent(Set<String> families, String headerToAdd)
    {
        blockingRemoteUserMapper.reset();
        // now try with valid basic auth as well
        final boolean authenticated = authenticate(families, headerToAdd);

        assertTrue("It is an AdminConsole webscript now and Admin basic auth header was present. It should succeed.", authenticated);
        // status is not checked here, as it is not returned by this framework we are testing. It should eventually be 200
        final String message = "The code from blockingRemoteUserMapper shouldn't have been called";
        assertFalse(message, blockingRemoteUserMapper.isWasInterrupted());
        assertEquals(message, blockingRemoteUserMapper.getTimePassed(), 0);
    }

    private void checkAdminConsoleFamilyWithBasicAuthHeaderPresentUser(Set<String> families, String headerToAdd)
    {
        blockingRemoteUserMapper.reset();
        // now try with valid basic auth as well
        boolean authenticated = false;

        try {
            authenticated = authenticate(families, headerToAdd);
        } catch (Exception e) {
            logger.error(String.format("The authentication should not require secure context to be set. %s", e.getMessage()), e);
        }

        assertTrue("It is an AdminConsole webscript and a User with Admin access and basic auth header was present. It should succeed.", authenticated);
        // status is not checked here, as it is not returned by this framework we are testing. It should eventually be 200
        final String message = "The code from blockingRemoteUserMapper shouldn't have been called";
        assertFalse(message, blockingRemoteUserMapper.isWasInterrupted());
        assertEquals(message, blockingRemoteUserMapper.getTimePassed(), 0);
    }

    private void checkAdminConsoleFamilyPageWithRemoteUserMapperDisabled(Set<String> families)
    {
        blockingRemoteUserMapper.reset();
        blockingRemoteUserMapper.setEnabled(false);
        // now try an admin console family page
        final boolean authenticated = authenticate(families, null);

        assertFalse("It is an AdminConsole webscript now, but Admin basic auth header was not present. It should return 401", authenticated);
        assertEquals("Status should be 401", 401, setStatusCode);
        assertFalse("The interrupt should have not been called because the RemoteUserMapper is not enabled.",
            blockingRemoteUserMapper.isWasInterrupted());
        assertEquals("RemoteUserMapper not called", blockingRemoteUserMapper.getTimePassed(), 0);
    }

    private void checkGenericResourceAccess()
    {
        blockingRemoteUserMapper.reset();
        // first try a generic resource
        final boolean authenticated = authenticate(Collections.emptySet(), null);

        assertFalse("This should not be authenticated as it is not an Admin Console requested. And no credentials have been provided", authenticated);
        assertFalse("Because it is not an Admin Console, the timeout feature from BasicHttpAuthenticator should not be requested. "
            + "Therefore the interrupt should not have been called. ", blockingRemoteUserMapper.isWasInterrupted());
        assertTrue("No interrupt should have been called.",
            blockingRemoteUserMapper.getTimePassed() > BlockingRemoteUserMapper.BLOCKING_FOR_MILLIS - 1);
    }

    private void checkTimeOutFeaturesWasNotUsed()
    {
        assertFalse("The timeout feature from BasicHttpAuthenticator should not be requested. Therefore the interrupt should not have been called. ",
            blockingRemoteUserMapper.isWasInterrupted());
        assertTrue("No interrupt should have been called.",
            blockingRemoteUserMapper.getTimePassed() > BlockingRemoteUserMapper.BLOCKING_FOR_MILLIS - 1);
    }

    private boolean authenticate(Set<String> families, String headerToAdd)
    {
        WebScriptServletRequest mockRequest = prepareMockRequest(families, headerToAdd);
        WebScriptServletResponse mockResponse = prepareMockResponse();

        Authenticator authenticator = remoteUserAuthenticatorFactory.create(mockRequest, mockResponse);
        return authenticator.authenticate(RequiredAuthentication.admin, false);
    }

    private boolean authenticateWithGuestParameters(RequiredAuthentication required, boolean isGuest)
    {
        blockingRemoteUserMapper.reset();

        WebScriptServletRequest mockRequest = prepareMockRequest(Collections.emptySet(), null);
        WebScriptServletResponse mockResponse = prepareMockResponse();

        Authenticator authenticator = remoteUserAuthenticatorFactory.create(mockRequest, mockResponse);
        return authenticator.authenticate(required, isGuest);
    }

    private WebScriptServletRequest prepareMockRequest(Set<String> families, String headerToAdd)
    {
        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getScheme()).thenReturn("http");
        if (headerToAdd != null)
        {
            when(mockHttpRequest.getHeader("Authorization")).thenReturn(headerToAdd);
        }
        WebScriptServletRequest mockRequest = mock(WebScriptServletRequest.class);
        when(mockRequest.getHttpServletRequest()).thenReturn(mockHttpRequest);

        WebScript mockWebScript = mock(WebScript.class);
        Match mockMatch = new Match("fake", Collections.EMPTY_MAP, "whatever", mockWebScript);
        when(mockRequest.getServiceMatch()).thenReturn(mockMatch);

        Description mockDescription = mock(Description.class);
        when(mockWebScript.getDescription()).thenReturn(mockDescription);
        when(mockDescription.getFamilys()).thenReturn(families);
        return mockRequest;
    }

    private void checkExtAuthStillWorks(RequiredAuthentication required, Set<String> families)
    {
        blockingRemoteUserMapper.reset();

        DefaultRemoteUserMapper defaultRemoteUserMapper = new DefaultRemoteUserMapper();
        defaultRemoteUserMapper.setActive(true);
        defaultRemoteUserMapper.setProxyUserName(null);
        defaultRemoteUserMapper.setPersonService(personService);
        remoteUserAuthenticatorFactory.setRemoteUserMapper(defaultRemoteUserMapper);

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getScheme()).thenReturn("http");

        final String userName = "RAFACAT_usr_" + (int) (Math.random() * 1000);
        when(mockHttpRequest.getHeader(proxyHeader)).thenReturn(userName);

        WebScriptServletRequest mockRequest = mock(WebScriptServletRequest.class);
        when(mockRequest.getHttpServletRequest()).thenReturn(mockHttpRequest);

        WebScript mockWebScript = mock(WebScript.class);
        Match mockMatch = new Match("fake", Collections.EMPTY_MAP, "whatever", mockWebScript);
        when(mockRequest.getServiceMatch()).thenReturn(mockMatch);

        Description mockDescription = mock(Description.class);
        when(mockWebScript.getDescription()).thenReturn(mockDescription);
        when(mockDescription.getFamilys()).thenReturn(families);

        WebScriptServletResponse mockResponse = prepareMockResponse();

        Authenticator authenticator = remoteUserAuthenticatorFactory.create(mockRequest, mockResponse);

        final boolean authenticated = authenticator.authenticate(required, false);

        assertTrue("This should be authenticating with external auth", authenticated);
        assertFalse("We have been using the DefaultRemoteUserMapper, so our BlockingRemoteUserMapper shouldn't have been called",
            blockingRemoteUserMapper.isWasInterrupted());
        assertEquals("BlockingRemoteUserMapper shouldn't have been called", blockingRemoteUserMapper.getTimePassed(), 0);
    }

    private WebScriptServletResponse prepareMockResponse()
    {
        HttpServletResponse mockHttpResponse = mock(HttpServletResponse.class);
        WebScriptServletResponse mockResponse = mock(WebScriptServletResponse.class);
        when(mockResponse.getHttpServletResponse()).thenReturn(mockHttpResponse);
        doAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocation)
            {
                Object[] args = invocation.getArguments();
                if (args != null && args.length == 1)
                {
                    setStatusCode = -1;
                    try
                    {
                        setStatusCode = (int) args[0];
                    }
                    catch (Exception e)
                    {
                        logger.error("Could not get the status code: " + e.getMessage(), e);
                    }
                }
                return null;
            }
        }).when(mockHttpResponse).setStatus(anyInt());
        return mockResponse;
    }

    /**
     * User creation consists of creating a user and an authentication to be compared when a login is asked
     * @param userName
     * @param password
     */
    private void createUser(String userName, String password)
    {
        if (!personService.personExists(userName))
        {
            this.authenticationService.createAuthentication(userName, password.toCharArray());

            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, userName);
            personProps.put(ContentModel.PROP_FIRSTNAME, "myFirstName");
            personProps.put(ContentModel.PROP_LASTNAME, "myLastName");
            personProps.put(ContentModel.PROP_EMAIL, "myFirstName.myLastName@email.com");
            personProps.put(ContentModel.PROP_JOBTITLE, "myJobTitle");
            personProps.put(ContentModel.PROP_JOBTITLE, "myOrganisation");

            this.personService.createPerson(personProps);
        }
    }

    /**
     * Formats the value for a basic auth to be put in headers
     * @param userName
     * @param password
     * @return
     */
    private String getBasicAuthHeader(String userName, String password)
    {
        return String.format("Basic %s", Base64.encodeBase64String(String.format("%s:%s", userName, password).getBytes()));
    }
}

class BlockingRemoteUserMapper implements RemoteUserMapper, ActivateableBean
{
    public static final int BLOCKING_FOR_MILLIS = 1000;
    private volatile boolean wasInterrupted;
    private volatile int timePassed;

    private boolean isEnabled = true;

    @Override
    public String getRemoteUser(HttpServletRequest request)
    {
        long t1 = System.currentTimeMillis();
        try
        {
            Thread.sleep(BLOCKING_FOR_MILLIS);
        }
        catch (InterruptedException ie)
        {
            wasInterrupted = true;
        }
        finally
        {
            timePassed = (int) (System.currentTimeMillis() - t1);
        }
        return null;
    }

    public boolean isWasInterrupted()
    {
        return wasInterrupted;
    }

    public int getTimePassed()
    {
        return timePassed;
    }

    public void reset()
    {
        wasInterrupted = false;
        timePassed = 0;
    }

    @Override
    public boolean isActive()
    {
        return isEnabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.isEnabled = enabled;
    }
}