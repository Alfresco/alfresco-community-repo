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
package org.alfresco.rest.api.tests;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.management.subsystems.DefaultChildApplicationContextManager;
import org.alfresco.repo.security.authentication.external.DefaultRemoteUserMapper;
import org.alfresco.repo.security.authentication.external.RemoteUserMapper;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceRemoteUserMapper;
import org.alfresco.repo.transaction.TransactionServiceImpl;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.PublicApiAuthenticatorFactory;
import org.alfresco.rest.api.impl.AuthenticationsImpl;
import org.alfresco.rest.api.model.LoginTicket;
import org.alfresco.rest.api.model.LoginTicketResponse;
import org.alfresco.rest.api.sites.SiteEntityResource;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * V1 REST API tests for authentication Tickets
 *
 * @author Jamal Kaabi-Mofrad
 */
public class AuthenticationsTest extends AbstractSingleNetworkSiteTest
{
    private static final String TICKETS_URL = "tickets";
    private static final String TICKETS_API_NAME = "authentication";
    private PublicApiAuthenticatorFactory authFactory;
    private TransactionServiceImpl transactionService;

    @Before
    public void setUpAuthTest()
    {
        authFactory = (PublicApiAuthenticatorFactory) applicationContext.getBean("publicapi.authenticator");
        transactionService = (TransactionServiceImpl) applicationContext.getBean("transactionService");
    }

    @Test
    public void canDisableBasicAuthChallenge() throws Exception
    {
        authFactory.setUseBasicAuth(false);

        // Expect to be challenged for an AlfTicket (REPO-2575)
        testAuthChallenge("AlfTicket");
    }

    @Test
    public void canEnableBasicAuthChallenge() throws Exception
    {
        authFactory.setUseBasicAuth(true);

        // Expect to be challenged for Basic auth.
        testAuthChallenge("Basic");
    }

    private void testAuthChallenge(String expectedScheme) throws Exception
    {
        // Unauthorized call
        setRequestContext(null);

        HttpResponse response = getAll(SiteEntityResource.class, getPaging(0, 100), null, 401);
        String authenticateHeader = response.getHeaders().get("WWW-Authenticate");
        assertNotNull("Expected an authentication challenge", authenticateHeader);
        String authScheme = authenticateHeader.split(" ")[0]; // Other parts may contain, e.g. realm="..."
        assertEquals(expectedScheme, authScheme);
    }

    /**
     * Tests login (create ticket), logout (delete ticket), and validate (get ticket).
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/authentication/versions/1/tickets}
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/authentication/versions/1/tickets/-me-}
     *
     * <p>DELETE:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/authentication/versions/1/tickets/-me-}
     */
    @Test
    public void testCreateValidateDeleteTicketAlfTicketParameter() throws Exception
    {
        Paging paging = getPaging(0, 100);

        setRequestContext(null);

        // Unauthorized call
        getAll(SiteEntityResource.class, paging, null, 401);

        /*
         *  user1 login - via alf_ticket parameter
         */

        // User1 login request
        LoginTicket loginRequest = new LoginTicket();
        // Invalid login details
        post(TICKETS_URL, RestApiUtil.toJsonAsString(loginRequest), null, null, TICKETS_API_NAME, 400);

        loginRequest.setUserId(null);
        loginRequest.setPassword("user1Password");
        // Invalid login details
        post(TICKETS_URL, RestApiUtil.toJsonAsString(loginRequest), null, null, TICKETS_API_NAME, 400);

        loginRequest.setUserId(user1);
        loginRequest.setPassword(null);
        // Invalid login details
        post(TICKETS_URL, RestApiUtil.toJsonAsString(loginRequest), null, null, TICKETS_API_NAME, 400);

        loginRequest.setUserId(user1);
        loginRequest.setPassword("user1Password");
        // Authenticate and create a ticket
        HttpResponse response = post(TICKETS_URL, RestApiUtil.toJsonAsString(loginRequest), null, null, TICKETS_API_NAME, 201);
        LoginTicketResponse loginResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), LoginTicketResponse.class);
        assertNotNull(loginResponse.getId());
        assertNotNull(loginResponse.getUserId());

        // Get list of sites by appending the alf_ticket to the URL
        // e.g. .../alfresco/versions/1/sites/?alf_ticket=TICKET_57866258ea56c28491bb3e75d8355ebf6fbaaa23
        Map<String, String> ticket = Collections.singletonMap("alf_ticket", loginResponse.getId());
        getAll(SiteEntityResource.class, paging, ticket, 200);

        // Unauthorized - Invalid ticket
        getAll(SiteEntityResource.class, paging, Collections.singletonMap("alf_ticket", "TICKET_" + System.currentTimeMillis()), 401);

        // Validate ticket - Invalid parameter. Only '-me-' is supported
        getSingle(TICKETS_URL, loginResponse.getId(), ticket, null, TICKETS_API_NAME, 400);

        // Validate ticket
        response = getSingle(TICKETS_URL, People.DEFAULT_USER, ticket, null, TICKETS_API_NAME, 200);
        LoginTicketResponse validatedTicket = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), LoginTicketResponse.class);
        assertEquals(loginResponse.getId(), validatedTicket.getId());

        // Validate ticket - Invalid parameter. Only '-me-' is supported
        getSingle(TICKETS_URL, loginResponse.getId(), ticket, null, TICKETS_API_NAME, 400);

        // Delete the ticket  - Logout
        delete(TICKETS_URL, People.DEFAULT_USER, ticket, null, TICKETS_API_NAME, 204);

        // Validate ticket - 401 as ticket has been invalidated so the API call is unauthorized
        getSingle(TICKETS_URL, People.DEFAULT_USER, ticket, null, TICKETS_API_NAME, 401);

        setRequestContext(user1);

        // Check the ticket has been invalidated - the difference with the above is that the API call is authorized
        response = getSingle(TICKETS_URL, People.DEFAULT_USER, ticket, null, TICKETS_API_NAME, 404);
        PublicApiClient.ExpectedErrorResponse error = RestApiUtil.parseErrorResponse(response.getJsonResponse());
        // Double check that we've retrieved a standard error response (REPO-1773)
        assertEquals(404, error.getStatusCode());

        // Ticket has already been invalidated
        delete(TICKETS_URL, People.DEFAULT_USER, ticket, null, TICKETS_API_NAME, 404);

        setRequestContext(null);

        // Get list of site by appending the invalidated ticket
        getAll(SiteEntityResource.class, paging, ticket, 401);
    }

    /**
     * Tests login (create ticket), logout (delete ticket), and validate (get ticket).
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/authentication/versions/1/tickets}
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/authentication/versions/1/tickets/-me-}
     *
     * <p>DELETE:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/authentication/versions/1/tickets/-me-}
     */
    @Test
    public void testCreateValidateDeleteTicketViaBasicAuthHeader() throws Exception
    {
        /*
         *  user2 login - Via Authorization header
         */
        Paging paging = getPaging(0, 100);

        setRequestContext(null);

        // Unauthorized call
        getAll(SiteEntityResource.class, paging, null, 401);

        // login request
        LoginTicket loginRequest = new LoginTicket();
        // Invalid login details
        post(TICKETS_URL, RestApiUtil.toJsonAsString(loginRequest), null, null, TICKETS_API_NAME, 400);

        loginRequest.setUserId(null);
        loginRequest.setPassword("user1Password");
        // Invalid login details
        post(TICKETS_URL, RestApiUtil.toJsonAsString(loginRequest), null, null, TICKETS_API_NAME, 400);

        setRequestContext(user2);

        // User2 create a folder within his home folder (-my-)
        Folder folderResp = createFolder(Nodes.PATH_MY, "F2", null);
        assertNotNull(folderResp.getId());

        setRequestContext(null);

        getAll(getNodeChildrenUrl(Nodes.PATH_MY), paging, 401);

        // User2 login request
        loginRequest = new LoginTicket();
        loginRequest.setUserId(user2);
        loginRequest.setPassword("wrongPassword");
        // Authentication failed - wrong password
        post(TICKETS_URL, RestApiUtil.toJsonAsString(loginRequest), null, null, TICKETS_API_NAME, 403);

        loginRequest.setUserId(user1);
        loginRequest.setPassword("user2Password");
        // Authentication failed - userId/password mismatch
        post(TICKETS_URL, RestApiUtil.toJsonAsString(loginRequest), null, null, TICKETS_API_NAME, 403);

        // Set the correct details
        loginRequest.setUserId(user2);
        loginRequest.setPassword("user2Password");
        // Authenticate and create a ticket
        HttpResponse response = post(TICKETS_URL, RestApiUtil.toJsonAsString(loginRequest), null, null, TICKETS_API_NAME, 201);
        LoginTicketResponse loginResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), LoginTicketResponse.class);
        assertNotNull(loginResponse.getId());
        assertNotNull(loginResponse.getUserId());

        String encodedTicket = encodeB64(loginResponse.getId());
        // Set the authorization (encoded ticket only) header rather than appending the ticket to the URL
        Map<String, String> header = Collections.singletonMap("Authorization", "Basic " + encodedTicket);
        // Get children of user2 home folder
        response = getAll(getNodeChildrenUrl(Nodes.PATH_MY), paging, null, header, 200);
        List<Document> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
        assertEquals(1, nodes.size());

        // Validate ticket - Invalid parameter. Only '-me-' is supported
        getSingle(TICKETS_URL, loginResponse.getId(), null, header, TICKETS_API_NAME, 400);

        // Validate ticket - user2
        response = getSingle(TICKETS_URL, People.DEFAULT_USER, null, header, TICKETS_API_NAME, 200);
        LoginTicketResponse validatedTicket = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), LoginTicketResponse.class);
        assertEquals(loginResponse.getId(), validatedTicket.getId());

        // now use the "bearer" keyword with the alf-ticket  - should not succeed
        header = Collections.singletonMap("Authorization", "bearer " + encodedTicket);
        response = getSingle(TICKETS_URL, People.DEFAULT_USER, null, header, TICKETS_API_NAME, 401);

        // now send some junk  - should not succeed
        header = Collections.singletonMap("Authorization", "junk " + encodedTicket);
        response = getSingle(TICKETS_URL, People.DEFAULT_USER, null, header, TICKETS_API_NAME, 401);

        // Try list children for user2 again.
        // Encode Alfresco predefined userId for ticket authentication, ROLE_TICKET, and the ticket
        String encodedUserIdAndTicket = encodeB64("ROLE_TICKET:" + loginResponse.getId());
        // Set the authorization (encoded userId:ticket) header rather than appending the ticket to the URL
        header = Collections.singletonMap("Authorization", "Basic " + encodedUserIdAndTicket);
        // Get children of user2 home folder
        response = getAll(getNodeChildrenUrl(Nodes.PATH_MY), paging, null, header, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
        assertEquals(1, nodes.size());

        // now use the "bearer" keyword with the alf-ticket  - should not succeed
        encodedUserIdAndTicket = encodeB64("ROLE_TICKET:" + loginResponse.getId());
        // Set the authorization (encoded userId:ticket) header rather than appending the ticket to the URL
        header = Collections.singletonMap("Authorization", "bearer " + encodedUserIdAndTicket);
        // Get children of user2 home folder
        response = getAll(getNodeChildrenUrl(Nodes.PATH_MY), paging, null, header, 401);

        // Try list children for user2 again - appending ticket
        Map<String, String> ticket = Collections.singletonMap("alf_ticket", loginResponse.getId());
        response = getAll(getNodeChildrenUrl(Nodes.PATH_MY), paging, ticket, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
        assertEquals(1, nodes.size());

        setRequestContext(user2);

        // Try to validate the ticket without supplying the Authorization header or the alf_ticket param
        getSingle(TICKETS_URL, People.DEFAULT_USER, null, null, TICKETS_API_NAME, 400);

        setRequestContext(null);

        // Delete the ticket  - Invalid parameter. Only '-me-' is supported
        header = Collections.singletonMap("Authorization", "Basic " + encodedUserIdAndTicket);
        delete(TICKETS_URL, loginResponse.getId(), null, header, TICKETS_API_NAME, 400);

        // Delete the ticket  - Logout
        delete(TICKETS_URL, People.DEFAULT_USER, null, header, TICKETS_API_NAME, 204);

        // Get children of user2 home folder - invalidated ticket
        getAll(getNodeChildrenUrl(Nodes.PATH_MY), paging, null, header, 401);
    }

    @Test
    public void testGetTicketViaBearerAuthHeaderWithExternalAuthentication() throws Exception
    {
        RemoteUserMapper originalRemoteUserMapper = (RemoteUserMapper) applicationContext.getBean("RemoteUserMapper");
        try
        {
            checkGetTicketViaBearerAuthHeader(false);
        }
        finally
        {
            //reset authentication chain
            resetAuthentication(originalRemoteUserMapper);
        }
    }

    @Test
    public void testGetTicketViaBearerAuthHeaderWithAlfrescoIdentityService() throws Exception
    {
        RemoteUserMapper originalRemoteUserMapper = (RemoteUserMapper) applicationContext.getBean("RemoteUserMapper");
        try
        {
            checkGetTicketViaBearerAuthHeader(true);
        }
        finally
        {
            //reset authentication chain
            resetAuthentication(originalRemoteUserMapper);
        }
    }

    // REPO-4168 / MNT-20308
    @Test
    public void testTicketLoginSystemReadOnly() throws Exception
    {
        QName veto = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "TestVeto");

        try
        {
            // Set transactions to read-only
            transactionService.setAllowWrite(false, veto);

            // Set the login details
            LoginTicket loginRequest = new LoginTicket();
            loginRequest.setUserId(user1);
            loginRequest.setPassword("user1Password");

            // Authenticate and create a ticket
            HttpResponse response = post(TICKETS_URL, RestApiUtil.toJsonAsString(loginRequest), null, null, TICKETS_API_NAME, 201);
            LoginTicketResponse validatedTicket = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), LoginTicketResponse.class);
            Map<String, String> ticket = Collections.singletonMap("alf_ticket", validatedTicket.getId());

            // Delete the ticket - logout
            delete(TICKETS_URL, People.DEFAULT_USER, ticket, null, TICKETS_API_NAME, 204);
        }
        finally
        {
            // Set transactions back to write allow write
            transactionService.setAllowWrite(true, veto);
        }
    }

    private void resetAuthentication(RemoteUserMapper originalRemoteUserMapper)
    {
        DefaultChildApplicationContextManager childApplicationContextManager = (DefaultChildApplicationContextManager) applicationContext
            .getBean("Authentication");
        PublicApiAuthenticatorFactory publicApiAuthenticatorFactory = (PublicApiAuthenticatorFactory) applicationContext
            .getBean("publicapi.authenticator");
        String chain = "alfrescoNtlm1:alfrescoNtlm";

        childApplicationContextManager.stop();
        childApplicationContextManager.setProperty("chain", chain);
        ChildApplicationContextFactory childApplicationContextFactory = childApplicationContextManager
            .getChildApplicationContextFactory("alfrescoNtlm1");
        childApplicationContextFactory.stop();
        childApplicationContextFactory.start();

        publicApiAuthenticatorFactory = (PublicApiAuthenticatorFactory) applicationContext.getBean("publicapi.authenticator");
        publicApiAuthenticatorFactory.setRemoteUserMapper(originalRemoteUserMapper);

        AuthenticationsImpl authentications = (AuthenticationsImpl) applicationContext.getBean("authentications");
        authentications.setRemoteUserMapper(originalRemoteUserMapper);
    }

    /**
     * @param useIdentityService if not true we use "external" authentication in the chain,
     *                           if it is true we use "identity-service"
     */
    private void checkGetTicketViaBearerAuthHeader(boolean useIdentityService) throws Exception
    {
        final String folderName = "F2_" + GUID.generate();
        Paging paging = getPaging(0, 100);
        LoginTicket loginRequest = null;
        LoginTicketResponse validatedTicket = null;
        HttpResponse response = null;
        Map<String, String> header = new HashMap<>();

        runPreCheckToEnsureBasicFunctionalityWorks(folderName, paging);

        RemoteUserMapper remoteUserMapper = createRemoteUserMapperToUseForTheTest(useIdentityService);

        setupAuthChainForTest(useIdentityService, remoteUserMapper);

        if (!useIdentityService)
        {
            // these tests run by default with multi tenancy enabled
            header.put("X-Alfresco-Remote-User", buildUserNameMultiTenancyAware());

            response = getAll(getNodeChildrenUrl(Nodes.PATH_MY), paging, null, header, 200);
            List<Document> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
            assertEquals(0, nodes.size()); // this is "someUserName" user home, and it should be empty
        }

        // check that without an Authorization header, we still can't get the ticket
        getSingle(TICKETS_URL, People.DEFAULT_USER, null, header, TICKETS_API_NAME, 400);

        Map<String, String> headersWtihBasicAuth = new HashMap<>(header);
        headersWtihBasicAuth.put("Authorization", "basic " + encodeB64("someRandomString"));
        // "someRandomString" will be considered the ticket, and that is not valid still
        getSingle(TICKETS_URL, People.DEFAULT_USER, null, headersWtihBasicAuth, TICKETS_API_NAME, 404);
        checkRemoteUserMapperWasCalled(useIdentityService);

        reset(useIdentityService);
        headersWtihBasicAuth = new HashMap<>(header);
        headersWtihBasicAuth.put("Authorization", "basic " + encodeB64(user2 + ":user2password"));
        // only "Ticket base authentication required." is accepted
        getSingle(TICKETS_URL, People.DEFAULT_USER, null, headersWtihBasicAuth, TICKETS_API_NAME, 400);
        checkRemoteUserMapperWasCalled(useIdentityService);

        // now, for the big test. use "someOtherRandomString" as the ticket, because we override the IdentityServiceRemoteUserMapper in our test
        reset(useIdentityService);
        header.put("Authorization", "bearer " + "someOtherRandomString");
        // NOTE: external authentication (using the DefaultRemoteUserMapper) could be used to login
        // if you include some value in the "bearer" authorization header;
        // We consider this not to be a big problem since we trust external uses with any api call
        response = getSingle(TICKETS_URL, People.DEFAULT_USER, null, header, TICKETS_API_NAME, 200);
        validatedTicket = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), LoginTicketResponse.class);
        assertNotNull("We should have gotten a valid ticket id", validatedTicket.getId());
        checkRemoteUserMapperWasCalled(useIdentityService);

        reset(useIdentityService);
    }

    private String buildUserNameMultiTenancyAware()
    {
        return "someUserName" + ((useDefaultNetwork) ? "" : ("@" + this.getClass().getName().toLowerCase()));
    }

    private void runPreCheckToEnsureBasicFunctionalityWorks(String folderName, Paging paging) throws Exception
    {
        setRequestContext(null);
        getAll(SiteEntityResource.class, paging, null, 401);

        setRequestContext(user2);
        // User2 create a folder within his home folder (-my-)
        Folder folderResp = createFolder(Nodes.PATH_MY, folderName, null);
        assertNotNull(folderResp.getId());

        setRequestContext(null);
        getAll(getNodeChildrenUrl(Nodes.PATH_MY), paging, 401);
    }

    private void setupAuthChainForTest(boolean useIdentityService, RemoteUserMapper remoteUserMapper)
    {
        PublicApiAuthenticatorFactory publicApiAuthenticatorFactory = (PublicApiAuthenticatorFactory) applicationContext
            .getBean("publicapi.authenticator");
        publicApiAuthenticatorFactory.setRemoteUserMapper(remoteUserMapper);

        AuthenticationsImpl authentications = (AuthenticationsImpl) applicationContext.getBean("authentications");
        authentications.setRemoteUserMapper(remoteUserMapper);

        String chain = useIdentityService ?
            "identity-service1:identity-service,alfrescoNtlm1:alfrescoNtlm" :
            "external1:external,alfrescoNtlm1:alfrescoNtlm";
        String chainId = useIdentityService ? "identity-service1" : "external1";

        DefaultChildApplicationContextManager childApplicationContextManager = (DefaultChildApplicationContextManager) applicationContext
            .getBean("Authentication");

        childApplicationContextManager.stop();
        childApplicationContextManager.setProperty("chain", chain);
        ChildApplicationContextFactory childApplicationContextFactory = childApplicationContextManager.getChildApplicationContextFactory(chainId);
        childApplicationContextFactory.stop();
        childApplicationContextFactory.setProperty("external.authentication.proxyUserName", "");
        childApplicationContextFactory.start();
    }

    private RemoteUserMapper createRemoteUserMapperToUseForTheTest(boolean useIdentityService)
    {
        PersonService personServiceLocal = (PersonService) applicationContext.getBean("PersonService");
        RemoteUserMapper remoteUserMapper;
        if (useIdentityService)
        {
            InterceptingIdentityRemoteUserMapper interceptingRemoteUserMapper = new InterceptingIdentityRemoteUserMapper();
            interceptingRemoteUserMapper.setActive(true);
            interceptingRemoteUserMapper.setPersonService(personServiceLocal);
            interceptingRemoteUserMapper.setIdentityServiceDeployment(null);
            interceptingRemoteUserMapper.setUserIdToReturn(user2);
            remoteUserMapper = interceptingRemoteUserMapper;
        }
        else
        {
            DefaultRemoteUserMapper interceptingRemoteUserMapper = new InterceptingDefaultRemoteUserMapper();
            interceptingRemoteUserMapper.setActive(true);
            interceptingRemoteUserMapper.setPersonService(personServiceLocal);
            interceptingRemoteUserMapper.setProxyUserName(null);
            remoteUserMapper = interceptingRemoteUserMapper;
        }
        return remoteUserMapper;
    }

    private void checkRemoteUserMapperWasCalled(boolean useIdentityService)
    {
        if (useIdentityService)
        {
            assertTrue(InterceptingIdentityRemoteUserMapper.isGetRemoteUserCalled());
        }
        else
        {
            assertTrue(InterceptingDefaultRemoteUserMapper.isGetRemoteUserCalled());
        }
    }

    private void reset(boolean useIdentityService)
    {
        if (useIdentityService)
        {
            InterceptingIdentityRemoteUserMapper.reset();
        }
        else
        {
            InterceptingDefaultRemoteUserMapper.reset();
        }
    }

    private String encodeB64(String str)
    {
        return Base64.encodeBase64String(str.getBytes());
    }

    @Override
    public String getScope()
    {
        return "public";
    }
}

class InterceptingDefaultRemoteUserMapper extends DefaultRemoteUserMapper
{
    static private volatile boolean getRemoteUserCalled;

    public static void reset()
    {
        getRemoteUserCalled = false;
    }

    public static boolean isGetRemoteUserCalled()
    {
        return getRemoteUserCalled;
    }

    @Override
    public String getRemoteUser(HttpServletRequest request)
    {
        getRemoteUserCalled = true;
        return super.getRemoteUser(request);
    }
}

class InterceptingIdentityRemoteUserMapper extends IdentityServiceRemoteUserMapper
{
    static private volatile boolean getRemoteUserCalled;

    public static void reset()
    {
        getRemoteUserCalled = false;
    }

    public static boolean isGetRemoteUserCalled()
    {
        return getRemoteUserCalled;
    }

    String userIdToReturn;

    public void setUserIdToReturn(String userId)
    {
        userIdToReturn = userId;
    }

    @Override
    public String getRemoteUser(HttpServletRequest request)
    {
        getRemoteUserCalled = true;
        if (userIdToReturn != null)
        {
            return userIdToReturn;
        }
        return super.getRemoteUser(request);
    }
}