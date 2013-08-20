/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.remoteticket;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.remoteconnector.LocalWebScriptConnectorServiceImpl;
import org.alfresco.repo.remotecredentials.PasswordCredentialsInfoImpl;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.remotecredentials.BaseCredentialsInfo;
import org.alfresco.service.cmr.remotecredentials.RemoteCredentialsService;
import org.alfresco.service.cmr.remoteticket.NoCredentialsFoundException;
import org.alfresco.service.cmr.remoteticket.NoSuchSystemException;
import org.alfresco.service.cmr.remoteticket.RemoteAlfrescoTicketInfo;
import org.alfresco.service.cmr.remoteticket.RemoteAlfrescoTicketService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;

/**
 * Tests for {@link RemoteAlfrescoTicketServiceImpl}, which work by
 *  looping back to the local repo. Because this tests talks to local
 *  webscripts, it needs to be run in the Remote API package
 *  
 * TODO Test OAuth support, once added
 * 
 * @author Nick Burch
 * @since 4.0.2
 */
public class RemoteAlfrescoTicketServiceTest extends BaseWebScriptTest
{
    private static final String TEST_REMOTE_SYSTEM_ID = "testingRemoteSystem";
    private static final String INVALID_REMOTE_SYSTEM_ID = "testingInvalidRemoteSystem";
    
    private MutableAuthenticationService authenticationService;
    private PersonService personService;
    
    private RemoteAlfrescoTicketService remoteAlfrescoTicketService;
    private RemoteCredentialsService remoteCredentialsService;
    private SimpleCache<String, String> ticketsCache;
    
    private static final String USER_ONE = "UserOneSecondToo";
    private static final String USER_TWO = "UserTwoSecondToo";
    private static final String PASSWORD = "passwordTEST";
    
    // General methods

    @Override
    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        
        this.remoteAlfrescoTicketService = (RemoteAlfrescoTicketService)getServer().getApplicationContext().getBean("remoteAlfrescoTicketService");
        this.remoteCredentialsService = (RemoteCredentialsService)getServer().getApplicationContext().getBean("RemoteCredentialsService");
        this.ticketsCache = (SimpleCache<String, String>)getServer().getApplicationContext().getBean("remoteAlfrescoTicketService.ticketsCache");
        
        // Do the setup as admin
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        
        // Add our local system as a remote service
        remoteAlfrescoTicketService.registerRemoteSystem(TEST_REMOTE_SYSTEM_ID, "http://localhost:8080/alfresco/service/", null);
        
        // Wire up the loop-back connector
        ((RemoteAlfrescoTicketServiceImpl)remoteAlfrescoTicketService).setRemoteConnectorService(
                new LocalWebScriptConnectorServiceImpl(this));
        
        // Ensure the invalid one isn't registered
        remoteAlfrescoTicketService.registerRemoteSystem(INVALID_REMOTE_SYSTEM_ID, null, null);
        
        // Create users
        createUser(USER_ONE);
        createUser(USER_TWO);

        // Do tests as first user
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        // Admin user required to delete user
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        
        // Delete the users, which also zaps their credentials
        if(personService.personExists(USER_ONE))
        {
           personService.deletePerson(USER_ONE);
        }
        if(this.authenticationService.authenticationExists(USER_ONE))
        {
           this.authenticationService.deleteAuthentication(USER_ONE);
        }
        
        if(personService.personExists(USER_TWO))
        {
           personService.deletePerson(USER_TWO);
        }
        if(this.authenticationService.authenticationExists(USER_TWO))
        {
           this.authenticationService.deleteAuthentication(USER_TWO);
        }
        
        // Unregister the system
        remoteAlfrescoTicketService.registerRemoteSystem(TEST_REMOTE_SYSTEM_ID, null, null);
    }
    
    private void createUser(String userName)
    {
        // if user with given user name doesn't already exist then create user
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            // create user
            this.authenticationService.createAuthentication(userName, PASSWORD.toCharArray());
            
            // create person properties
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, userName);
            personProps.put(ContentModel.PROP_FIRSTNAME, "First");
            personProps.put(ContentModel.PROP_LASTNAME, "Last");
            personProps.put(ContentModel.PROP_EMAIL, "FirstName123.LastName123@email.com");
            personProps.put(ContentModel.PROP_JOBTITLE, "JobTitle123");
            personProps.put(ContentModel.PROP_JOBTITLE, "Organisation123");
            
            // create person node for user
            this.personService.createPerson(personProps);
        }
    }
    
    /**
     * Getting, storing and fetching credentials
     */
    public void testGetStoreGetCredentials() throws Exception
    {
        // Run this test initially as the first user
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);

        
        // First, try an invalid system
        try
        {
            remoteAlfrescoTicketService.getRemoteCredentials(INVALID_REMOTE_SYSTEM_ID);
            fail("Shouldn't work for an invalid system");
        }
        catch(NoSuchSystemException e) {}
        try
        {
            remoteAlfrescoTicketService.storeRemoteCredentials(INVALID_REMOTE_SYSTEM_ID, null, null);
            fail("Shouldn't work for an invalid system");
        }
        catch(NoSuchSystemException e) {}
        
        
        // Our user starts out without credentials
        BaseCredentialsInfo credentials = 
            remoteAlfrescoTicketService.getRemoteCredentials(TEST_REMOTE_SYSTEM_ID);
        assertEquals(null, credentials);
        
        
        // Try to store some invalid credentials (real user, but password wrong)
        try
        {
            remoteAlfrescoTicketService.storeRemoteCredentials(TEST_REMOTE_SYSTEM_ID, USER_ONE, "invalid");
            fail("Credentials invalid, shouldn't be allowed");
        }
        catch (AuthenticationException e) {}
        
        // And an invalid user
        try
        {
            remoteAlfrescoTicketService.storeRemoteCredentials(TEST_REMOTE_SYSTEM_ID, "thisUSERdoesNOTexist", "invalid");
            fail("Credentials invalid, shouldn't be allowed");
        }
        catch (AuthenticationException e) {}

        
        // Still none there
        credentials = remoteAlfrescoTicketService.getRemoteCredentials(TEST_REMOTE_SYSTEM_ID);
        assertEquals(null, credentials);
        
        
        // Store some valid ones
        credentials = remoteAlfrescoTicketService.storeRemoteCredentials(TEST_REMOTE_SYSTEM_ID, USER_ONE, PASSWORD);
        assertNotNull(credentials);
        assertEquals(TEST_REMOTE_SYSTEM_ID, credentials.getRemoteSystemName());
        assertEquals(USER_ONE, credentials.getRemoteUsername());
        
        // Check we can find them
        credentials = remoteAlfrescoTicketService.getRemoteCredentials(TEST_REMOTE_SYSTEM_ID);
        assertNotNull(credentials);
        assertEquals(TEST_REMOTE_SYSTEM_ID, credentials.getRemoteSystemName());
        assertEquals(USER_ONE, credentials.getRemoteUsername());
        
        
        // Store some different, valid credentials for the user
        credentials = remoteAlfrescoTicketService.storeRemoteCredentials(TEST_REMOTE_SYSTEM_ID, USER_TWO, PASSWORD);
        assertNotNull(credentials);
        assertEquals(TEST_REMOTE_SYSTEM_ID, credentials.getRemoteSystemName());
        assertEquals(USER_TWO, credentials.getRemoteUsername());
        
        // Check we see the change
        credentials = remoteAlfrescoTicketService.getRemoteCredentials(TEST_REMOTE_SYSTEM_ID);
        assertNotNull(credentials);
        assertEquals(TEST_REMOTE_SYSTEM_ID, credentials.getRemoteSystemName());
        assertEquals(USER_TWO, credentials.getRemoteUsername());
        
        
        // Switch to the other user, no credentials there
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        credentials = remoteAlfrescoTicketService.getRemoteCredentials(TEST_REMOTE_SYSTEM_ID);
        assertEquals(null, credentials);
        
        
        // Switch back, and delete
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        credentials = remoteAlfrescoTicketService.getRemoteCredentials(TEST_REMOTE_SYSTEM_ID);
        assertNotNull(credentials);
        
        boolean deleted = remoteAlfrescoTicketService.deleteRemoteCredentials(TEST_REMOTE_SYSTEM_ID);
        assertEquals(true, deleted);
        
        // Will have gone
        credentials = remoteAlfrescoTicketService.getRemoteCredentials(TEST_REMOTE_SYSTEM_ID);
        assertEquals(null, credentials);
        
        // Double delete is reported
        deleted = remoteAlfrescoTicketService.deleteRemoteCredentials(TEST_REMOTE_SYSTEM_ID);
        assertEquals(false, deleted);
    }
    
    /**
     * Getting cached and non-cached credentials
     */
    public void testGetTicket() throws Exception
    {
        // Run this test initially as the first user
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);

        
        // First, try an invalid system
        try
        {
            remoteAlfrescoTicketService.getAlfrescoTicket(INVALID_REMOTE_SYSTEM_ID);
            fail("Shouldn't work for an invalid system");
        }
        catch(NoSuchSystemException e) {}
        try
        {
            remoteAlfrescoTicketService.refetchAlfrescoTicket(INVALID_REMOTE_SYSTEM_ID);
            fail("Shouldn't work for an invalid system");
        }
        catch(NoSuchSystemException e) {}
        
        
        // Can't get or refresh if no credentials exist 
        try
        {
            remoteAlfrescoTicketService.getAlfrescoTicket(TEST_REMOTE_SYSTEM_ID);
            fail("Shouldn't work when no credentials");
        }
        catch(NoCredentialsFoundException e) {}
        try
        {
            remoteAlfrescoTicketService.refetchAlfrescoTicket(TEST_REMOTE_SYSTEM_ID);
            fail("Shouldn't work when no credentials");
        }
        catch(NoCredentialsFoundException e) {}

        
        // Have some stored
        remoteAlfrescoTicketService.storeRemoteCredentials(TEST_REMOTE_SYSTEM_ID, USER_ONE, PASSWORD);

        
        // A ticket will now exist
        RemoteAlfrescoTicketInfo ticket = remoteAlfrescoTicketService.getAlfrescoTicket(TEST_REMOTE_SYSTEM_ID);
        assertNotNull(ticket);
        assertNotNull(ticket.getAsUrlParameters());
        
        
        // Ask again, will get the same one
        RemoteAlfrescoTicketInfo ticket2 = remoteAlfrescoTicketService.getAlfrescoTicket(TEST_REMOTE_SYSTEM_ID);
        assertNotNull(ticket2);
        assertEquals(ticket.getAsUrlParameters(), ticket2.getAsUrlParameters());
        
        
        // Force a re-fetch, will get another
        RemoteAlfrescoTicketInfo ticket3 = remoteAlfrescoTicketService.refetchAlfrescoTicket(TEST_REMOTE_SYSTEM_ID);
        assertNotNull(ticket3);
        assertNotSame(ticket.getAsUrlParameters(), ticket3.getAsUrlParameters());
        
        // Ask for the ticket again, get the 2nd one again
        RemoteAlfrescoTicketInfo ticket4 = remoteAlfrescoTicketService.getAlfrescoTicket(TEST_REMOTE_SYSTEM_ID);
        assertNotNull(ticket4);
        assertEquals(ticket3.getAsUrlParameters(), ticket4.getAsUrlParameters());
        
        
        // Zap from the cache, will trigger another to be fetched
        ticketsCache.clear();
        
        RemoteAlfrescoTicketInfo ticket5 = remoteAlfrescoTicketService.getAlfrescoTicket(TEST_REMOTE_SYSTEM_ID);
        assertNotNull(ticket5);
        assertNotSame(ticket.getAsUrlParameters(), ticket5.getAsUrlParameters());
        assertNotSame(ticket3.getAsUrlParameters(), ticket5.getAsUrlParameters());
        
        
        // Change the password so it's no longer valid
        PasswordCredentialsInfoImpl creds = (PasswordCredentialsInfoImpl)remoteCredentialsService.getPersonCredentials(TEST_REMOTE_SYSTEM_ID);
        assertNotNull(creds);
        creds.setRemotePassword("INVALID");
        remoteCredentialsService.updateCredentials(creds);
        
        // Currently will be marked as still working
        assertEquals(true, creds.getLastAuthenticationSucceeded());
        
        
        // Get will work, as ticket was previously cached
        RemoteAlfrescoTicketInfo ticket6 = remoteAlfrescoTicketService.getAlfrescoTicket(TEST_REMOTE_SYSTEM_ID);
        assertNotNull(ticket6);
        assertEquals(ticket5.getAsUrlParameters(), ticket6.getAsUrlParameters());
        
        // Re-fetch will fail with authentication error
        try
        {
            remoteAlfrescoTicketService.refetchAlfrescoTicket(TEST_REMOTE_SYSTEM_ID);
            fail("Shouldn't be able to refetch with wrong details");
        }
        catch(AuthenticationException e) {}
        
        // Now a get will fail too, as the cache will be invalidated
        try
        {
            remoteAlfrescoTicketService.getAlfrescoTicket(TEST_REMOTE_SYSTEM_ID);
            fail("Shouldn't be able to get after refresh with wrong details");
        }
        catch(AuthenticationException e) {}
        
        
        // If we check the credentials, will now be marked as failing
        creds = (PasswordCredentialsInfoImpl)remoteCredentialsService.getPersonCredentials(TEST_REMOTE_SYSTEM_ID);
        assertEquals(false, creds.getLastAuthenticationSucceeded());
        
        
        // Change the password back to what it should be, and re-get
        creds.setRemotePassword(PASSWORD);
        remoteCredentialsService.updateCredentials(creds);
        
        RemoteAlfrescoTicketInfo ticket7 = remoteAlfrescoTicketService.getAlfrescoTicket(TEST_REMOTE_SYSTEM_ID);
        assertNotNull(ticket7);
        assertNotSame(ticket.getAsUrlParameters(),  ticket7.getAsUrlParameters());
        assertNotSame(ticket3.getAsUrlParameters(), ticket7.getAsUrlParameters());
        assertNotSame(ticket5.getAsUrlParameters(), ticket7.getAsUrlParameters());
        
        // Should now be marked as working again
        creds = (PasswordCredentialsInfoImpl)remoteCredentialsService.getPersonCredentials(TEST_REMOTE_SYSTEM_ID);
        assertEquals(true, creds.getLastAuthenticationSucceeded());
        
        
        // Check that failure can be marked in a read only transaction
        creds.setRemotePassword("INVALID");
        remoteCredentialsService.updateCredentials(creds);

        try
        {
            remoteAlfrescoTicketService.refetchAlfrescoTicket(TEST_REMOTE_SYSTEM_ID);
            fail("Shouldn't be able to refetch with wrong details");
        }
        catch(AuthenticationException e) {}

        // Check it was still marked as invalid, despite a read only transaction
        creds = (PasswordCredentialsInfoImpl)remoteCredentialsService.getPersonCredentials(TEST_REMOTE_SYSTEM_ID);
        assertEquals(false, creds.getLastAuthenticationSucceeded());
    }
}
