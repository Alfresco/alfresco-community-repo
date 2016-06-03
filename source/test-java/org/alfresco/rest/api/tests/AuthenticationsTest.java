/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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

package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.LoginTicket;
import org.alfresco.rest.api.model.LoginTicketResponse;
import org.alfresco.rest.api.sites.SiteEntityResource;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class AuthenticationsTest extends AbstractBaseApiTest
{
    private String user1;
    private String user2;
    private List<String> users = new ArrayList<>();
    protected MutableAuthenticationService authenticationService;
    protected PersonService personService;

    @Before
    public void setup() throws Exception
    {
        authenticationService = applicationContext.getBean("authenticationService", MutableAuthenticationService.class);
        personService = applicationContext.getBean("personService", PersonService.class);

        user1 = createUser("user1" + System.currentTimeMillis(), "user1Password");
        user2 = createUser("user2" + System.currentTimeMillis(), "user2Password");

        users.add(user1);
        users.add(user2);
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    @After
    public void tearDown() throws Exception
    {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        for (final String user : users)
        {
            transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    if (personService.personExists(user))
                    {
                        authenticationService.deleteAuthentication(user);
                        personService.deletePerson(user);
                    }
                    return null;
                }
            });
        }
        users.clear();
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    /**
     * Tests login (create ticket), logout (delete ticket), and validate (get ticket).
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/tickets}
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/tickets/<ticket>}
     *
     * <p>DELETE:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/tickets/<ticket>}
     */
    @Test
    public void testCreateValidateDeleteTicket() throws Exception
    {
        Paging paging = getPaging(0, 100);
        // Unauthorized call
        getAll(SiteEntityResource.class, null, paging, null, 401);

        /*
         *  user1 login
         */

        // User1 login request
        LoginTicket loginRequest = new LoginTicket();
        // Invalid login details
        post("tickets", null, RestApiUtil.toJsonAsString(loginRequest), 400);

        loginRequest.setUsername(null);
        loginRequest.setPassword("user1Password");
        // Invalid login details
        post("tickets", null, RestApiUtil.toJsonAsString(loginRequest), 400);

        loginRequest.setUsername(user1);
        loginRequest.setPassword(null);
        // Invalid login details
        post("tickets", null, RestApiUtil.toJsonAsString(loginRequest), 400);

        loginRequest.setUsername(user1);
        loginRequest.setPassword("user1Password");
        // Authenticate and create a ticket
        HttpResponse response = post("tickets", null, RestApiUtil.toJsonAsString(loginRequest), 201);
        LoginTicketResponse loginResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), LoginTicketResponse.class);
        assertNotNull(loginResponse.getTicket());
        assertNotNull(loginResponse.getUsername());

        // Get list of sites by appending the alf_ticket to the URL
        // e.g. .../alfresco/versions/1/sites/?alf_ticket=TICKET_57866258ea56c28491bb3e75d8355ebf6fbaaa23
        Map<String, String> ticket = Collections.singletonMap("alf_ticket", loginResponse.getTicket());
        getAll(SiteEntityResource.class, null, paging, ticket, 200);

        // Validate ticket
        response = getSingle("tickets", null, loginResponse.getTicket(), ticket, 200);
        LoginTicketResponse validatedTicket = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), LoginTicketResponse.class);
        assertEquals(loginResponse.getTicket(), validatedTicket.getTicket());

        // Validate ticket - non-existent ticket
        getSingle("tickets", null, "TICKET_" + System.currentTimeMillis(), ticket, 404);

        // Delete the ticket  - Logout
        delete("tickets", null, loginResponse.getTicket(), ticket, 204);

        // Validate ticket - 401 as ticket has been invalidated so the API call is unauthorized
        getSingle("tickets", null, loginResponse.getTicket(), ticket, 401);
        // Check the ticket has been invalidated - the difference with the above is that the API call is authorized
        getSingle("tickets", user1, loginResponse.getTicket(), ticket, 404);

        // Ticket has already been invalidated
        delete("tickets", user1, loginResponse.getTicket(), ticket, 404);

        // Get list of site by appending the invalidated ticket
        getAll(SiteEntityResource.class, null, paging, ticket, 401);


        /*
         *  user2 login
         */

        // User2 create a folder within his home folder (-my-)
        Folder folderResp = createFolder(user2, Nodes.PATH_MY, "F2", null);
        assertNotNull(folderResp.getId());

        getAll(getNodeChildrenUrl(Nodes.PATH_MY), null, paging, 401);

        // User2 login request
        loginRequest = new LoginTicket();
        loginRequest.setUsername(user2);
        loginRequest.setPassword("wrongPassword");
        // Authentication failed - wrong password
        post("tickets", null, RestApiUtil.toJsonAsString(loginRequest), 403);

        loginRequest.setUsername(user1);
        loginRequest.setPassword("user2Password");
        // Authentication failed - username/password mismatch
        post("tickets", null, RestApiUtil.toJsonAsString(loginRequest), 403);

        // Set the correct details
        loginRequest.setUsername(user2);
        loginRequest.setPassword("user2Password");
        // Authenticate and create a ticket
        response = post("tickets", null, RestApiUtil.toJsonAsString(loginRequest), 201);
        loginResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), LoginTicketResponse.class);
        assertNotNull(loginResponse.getTicket());
        assertNotNull(loginResponse.getUsername());

        String encodedTicket = Base64.encodeBase64String(loginResponse.getTicket().getBytes());
        // Set the authorization (encoded ticket only) header rather than appending the ticket to the URL
        Map<String, String> header = Collections.singletonMap("Authorization", "Basic " + encodedTicket);
        // Get children of user2 home folder
        response = getAll(getNodeChildrenUrl(Nodes.PATH_MY), null, paging, null, header, 200);
        List<Document> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
        assertEquals(1, nodes.size());

        // Validate ticket - user2
        response = getSingle("tickets", null, loginResponse.getTicket(), null, header, 200);
        validatedTicket = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), LoginTicketResponse.class);
        assertEquals(loginResponse.getTicket(), validatedTicket.getTicket());

        // Try list children for user2 again.
        // Encode Alfresco predefined username for ticket authentication, ROLE_TICKET, and the ticket
        String encodedUsernameAndTicket = Base64.encodeBase64String(("ROLE_TICKET:" + loginResponse.getTicket()).getBytes());
        // Set the authorization (encoded username:ticket) header rather than appending the ticket to the URL
        header = Collections.singletonMap("Authorization", "Basic " + encodedUsernameAndTicket);
        // Get children of user2 home folder
        response = getAll(getNodeChildrenUrl(Nodes.PATH_MY), null, paging, null, header, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
        assertEquals(1, nodes.size());

        // Try list children for user2 again - appending ticket
        ticket = Collections.singletonMap("alf_ticket", loginResponse.getTicket());
        response = getAll(getNodeChildrenUrl(Nodes.PATH_MY), null, paging, ticket, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
        assertEquals(1, nodes.size());

        // Delete the ticket  - Logout
        header = Collections.singletonMap("Authorization", "Basic " + encodedUsernameAndTicket);
        delete("tickets", null, loginResponse.getTicket(), null, header, 204);

        // Get children of user2 home folder - invalidated ticket
        getAll(getNodeChildrenUrl(Nodes.PATH_MY), null, paging, null, header, 401);

        /*
         * user1 and user2 login
         */
        loginRequest = new LoginTicket();
        loginRequest.setUsername(user1);
        loginRequest.setPassword("user1Password");
        // Authenticate and create a ticket
        response = post("tickets", null, RestApiUtil.toJsonAsString(loginRequest), 201);
        LoginTicketResponse user1_loginResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), LoginTicketResponse.class);
        Map<String, String> user1_ticket = Collections.singletonMap("alf_ticket", user1_loginResponse.getTicket());

        loginRequest = new LoginTicket();
        loginRequest.setUsername(user2);
        loginRequest.setPassword("user2Password");
        // Authenticate and create a ticket
        response = post("tickets", null, RestApiUtil.toJsonAsString(loginRequest), 201);
        LoginTicketResponse user2_loginResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), LoginTicketResponse.class);
        Map<String, String> user2_ticket = Collections.singletonMap("alf_ticket", user2_loginResponse.getTicket());

        // Validate ticket - user1 tries to validate user2's ticket
        getSingle("tickets", null, user2_loginResponse.getTicket(), user1_ticket, 404);

        // Check that user2 ticket is still valid
        getSingle("tickets", null, user2_loginResponse.getTicket(), user2_ticket, 200);

        // User1 tries to delete user2's ticket
        delete("tickets", null, user2_loginResponse.getTicket(), user1_ticket, 404);

        // User1 logs out
        delete("tickets", null, user1_loginResponse.getTicket(), user1_ticket, 204);

        // User2 logs out
        delete("tickets", null, user2_loginResponse.getTicket(), user2_ticket, 204);
    }

    @Override
    public String getScope()
    {
        return "public";
    }
}
