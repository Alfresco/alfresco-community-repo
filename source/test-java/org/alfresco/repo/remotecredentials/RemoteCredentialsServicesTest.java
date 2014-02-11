/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.remotecredentials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.remotecredentials.BaseCredentialsInfo;
import org.alfresco.service.cmr.remotecredentials.PasswordCredentialsInfo;
import org.alfresco.service.cmr.remotecredentials.RemoteCredentialsService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.TemporaryNodes;
import org.alfresco.util.test.junitrules.WellKnownNodes;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * Test cases for {@link RemoteCredentialsServiceImpl} and friends.
 * 
 * Note - this test will largely use a test shared credentials
 *  container, but one test puts things into the real credentials folder
 * 
 * @author Nick Burch
 * @since Odin
 */
@Category(OwnJVMTestsCategory.class)
public class RemoteCredentialsServicesTest
{
    private static final String TEST_REMOTE_SYSTEM_ONE = "TestRemoteSystemOne";
    private static final String TEST_REMOTE_SYSTEM_TWO = "TestRemoteSystemTwo";
    private static final String TEST_REMOTE_SYSTEM_THREE = "aAaAaTestRemoteSystemThree";
    
    private static final String TEST_REMOTE_USERNAME_ONE   = "test@example.com";
    private static final String TEST_REMOTE_USERNAME_TWO   = "test2@example.com";
    private static final String TEST_REMOTE_USERNAME_THREE = "test3@example.com";
    
    private static final String SHARED_SYSTEM_CONTAINER_NAME = "test-remote-credentials";

    // Rule to initialise the default Alfresco spring configuration
    @ClassRule public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();
    
    // A rule to help find well known nodes in the system
    @ClassRule public static WellKnownNodes knownNodes = new WellKnownNodes(APP_CONTEXT_INIT);
    
    // A rule to manage test nodes use in each test method
    @ClassRule public static TemporaryNodes classTestNodes = new TemporaryNodes(APP_CONTEXT_INIT);
    @Rule public TemporaryNodes testNodes = new TemporaryNodes(APP_CONTEXT_INIT);
    
    // injected services
    private static MutableAuthenticationService AUTHENTICATION_SERVICE;
    private static BehaviourFilter              BEHAVIOUR_FILTER;
    private static RemoteCredentialsService     REMOTE_CREDENTIALS_SERVICE;
    private static RemoteCredentialsService     PRIVATE_REMOTE_CREDENTIALS_SERVICE;
    private static DictionaryService            DICTIONARY_SERVICE;
    private static NodeService                  NODE_SERVICE;
    private static NodeService                  PUBLIC_NODE_SERVICE;
    private static NamespaceService             NAMESPACE_SERVICE;
    private static Repository                   REPOSITORY_HELPER;
    private static PersonService                PERSON_SERVICE;
    private static RetryingTransactionHelper    TRANSACTION_HELPER;
    private static TransactionService           TRANSACTION_SERVICE;
    private static PermissionService            PERMISSION_SERVICE;
    
    private static final String TEST_USER_ONE = RemoteCredentialsServicesTest.class.getSimpleName() + "_testuser1";
    private static final String TEST_USER_TWO = RemoteCredentialsServicesTest.class.getSimpleName() + "_testuser2";
    private static final String TEST_USER_THREE = RemoteCredentialsServicesTest.class.getSimpleName() + "_testuser3";
    private static final String ADMIN_USER = AuthenticationUtil.getAdminUserName();

    @BeforeClass public static void initTestsContext() throws Exception
    {
        ApplicationContext testContext = APP_CONTEXT_INIT.getApplicationContext();
        
        PRIVATE_REMOTE_CREDENTIALS_SERVICE = (RemoteCredentialsService)testContext.getBean("remoteCredentialsService");
        REMOTE_CREDENTIALS_SERVICE = (RemoteCredentialsService)testContext.getBean("RemoteCredentialsService");
        
        AUTHENTICATION_SERVICE = (MutableAuthenticationService)testContext.getBean("authenticationService");
        BEHAVIOUR_FILTER       = (BehaviourFilter)testContext.getBean("policyBehaviourFilter");
        DICTIONARY_SERVICE     = (DictionaryService)testContext.getBean("dictionaryService");
        NAMESPACE_SERVICE      = (NamespaceService)testContext.getBean("namespaceService");
        REPOSITORY_HELPER      = (Repository)testContext.getBean("repositoryHelper");
        NODE_SERVICE           = (NodeService)testContext.getBean("nodeService");
        PUBLIC_NODE_SERVICE    = (NodeService)testContext.getBean("NodeService");
        PERSON_SERVICE         = (PersonService)testContext.getBean("personService");
        TRANSACTION_HELPER     = (RetryingTransactionHelper)testContext.getBean("retryingTransactionHelper");
        TRANSACTION_SERVICE    = (TransactionService)testContext.getBean("TransactionService");
        PERMISSION_SERVICE     = (PermissionService)testContext.getBean("permissionService");

        // Switch to a test shared system container
        RemoteCredentialsServiceImpl.setSharedCredentialsSystemContainerName(SHARED_SYSTEM_CONTAINER_NAME);
    }
    
    @Before public void setupUsers() throws Exception
    {
        // Do the setup as admin
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        createUser(TEST_USER_ONE);
        createUser(TEST_USER_TWO);
        createUser(TEST_USER_THREE);
        
        // We need to create the test site as the test user so that they can contribute content to it in tests below.
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
    }

    /**
     * Tests that read only methods don't create the shared credentials
     *  container, but that write ones will do.
     */
    @Test public void testSharedCredentialsContainer() throws Exception
    {
        // Run as a test user
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
        
        
        // To start with, the container shouldn't be there
        NodeRef container = ((RemoteCredentialsServiceImpl)PRIVATE_REMOTE_CREDENTIALS_SERVICE).getSharedContainerNodeRef(false);
        if (container != null)
        {
            // Tidy up
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            
            // Zap the container
            PUBLIC_NODE_SERVICE.deleteNode(container);
        }
        
        
        // Run as a test user
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
        
        // Ask for the list of shared remote systems
        REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        
        // Won't have been created by a read
        container = ((RemoteCredentialsServiceImpl)PRIVATE_REMOTE_CREDENTIALS_SERVICE).getSharedContainerNodeRef(false);
        assertEquals(null, container);
        
        
        // Try to store some credentials
        PasswordCredentialsInfo credentials = new PasswordCredentialsInfoImpl();
        REMOTE_CREDENTIALS_SERVICE.createSharedCredentials(TEST_REMOTE_SYSTEM_ONE, credentials);
        
        // It will now exist
        container = ((RemoteCredentialsServiceImpl)PRIVATE_REMOTE_CREDENTIALS_SERVICE).getSharedContainerNodeRef(false);
        assertNotNull(container);
        
        // Should have a marker aspect, and the specified name
        Set<QName> cAspects = PUBLIC_NODE_SERVICE.getAspects(container);
        assertEquals("Aspect missing, found " + cAspects, true, 
                cAspects.contains(RemoteCredentialsModel.ASPECT_REMOTE_CREDENTIALS_SYSTEM_CONTAINER));
        assertEquals(SHARED_SYSTEM_CONTAINER_NAME, PUBLIC_NODE_SERVICE.getProperty(container, ContentModel.PROP_NAME));
        
        // Should have single node in it
        assertEquals(1, PUBLIC_NODE_SERVICE.getChildAssocs(container).size());
        
        
        // Tidy up
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        
        // Zap the container
        PUBLIC_NODE_SERVICE.deleteNode(container);
    }
    
    /**
     * Creating shared and personal credentials, then checking how this
     *  affects the listing of Remote Systems
     */
    @Test public void testCreateCredentialsAndSystemListing() throws Exception
    {
        PagingResults<String> systems = null;
        
        // Run as a test user
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);

        
        // Initially both should be empty
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals("No systems should be found, got " + systems.getPage(), 
                     0, systems.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals("No systems should be found, got " + systems.getPage(), 
                     0, systems.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listAllRemoteSystems(new PagingRequest(10));
        assertEquals("No systems should be found, got " + systems.getPage(), 
                     0, systems.getPage().size());
        
        
        // Create one for the person
        PasswordCredentialsInfoImpl credentials = new PasswordCredentialsInfoImpl();
        credentials.setRemoteUsername(TEST_REMOTE_USERNAME_ONE);
        REMOTE_CREDENTIALS_SERVICE.createPersonCredentials(TEST_REMOTE_SYSTEM_ONE, credentials);
        
        // Check it shows up
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals("Unexpected systems " + systems.getPage(),
                     1, systems.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals("Unexpected systems " + systems.getPage(),
                     0, systems.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listAllRemoteSystems(new PagingRequest(10));
        assertEquals("Unexpected systems " + systems.getPage(),
                     1, systems.getPage().size());
        
        
        // Switch to another user, check it doesn't
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
        
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals(0, systems.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals(0, systems.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listAllRemoteSystems(new PagingRequest(10));
        assertEquals(0, systems.getPage().size());
        
        
        // Create both personal and shared ones as the current user
        credentials = new PasswordCredentialsInfoImpl();
        credentials.setRemoteUsername(TEST_REMOTE_USERNAME_TWO);
        REMOTE_CREDENTIALS_SERVICE.createPersonCredentials(TEST_REMOTE_SYSTEM_TWO, credentials);
        
        credentials = new PasswordCredentialsInfoImpl();
        credentials.setRemoteUsername(TEST_REMOTE_USERNAME_THREE);
        REMOTE_CREDENTIALS_SERVICE.createPersonCredentials(TEST_REMOTE_SYSTEM_THREE, credentials);
        
        credentials = new PasswordCredentialsInfoImpl();
        credentials.setRemoteUsername(TEST_REMOTE_USERNAME_THREE);
        BaseCredentialsInfo cc = REMOTE_CREDENTIALS_SERVICE.createSharedCredentials(TEST_REMOTE_SYSTEM_THREE, credentials);
        
        // Check as the user who created these
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals(2, systems.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listAllRemoteSystems(new PagingRequest(10));
        assertEquals(2, systems.getPage().size());
        
        
        // Check as the first user, they should see the shared one
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
        
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listAllRemoteSystems(new PagingRequest(10));
        assertEquals(2, systems.getPage().size());
        
        
        // Change the shared permissions, see it goes away
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        PERMISSION_SERVICE.setInheritParentPermissions(cc.getRemoteSystemContainerNodeRef(), false);
        
        
        // Check as the owning user, will still see all of them
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals(2, systems.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listAllRemoteSystems(new PagingRequest(10));
        assertEquals(2, systems.getPage().size());
        
        
        // Check as the other user, shared will have gone as we lost read permissions
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
        
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals(0, systems.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listAllRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        
        
        // Finally, check the listings have the correct things in them
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals(2, systems.getPage().size());
        assertEquals(true, systems.getPage().contains(TEST_REMOTE_SYSTEM_TWO));
        assertEquals(true, systems.getPage().contains(TEST_REMOTE_SYSTEM_THREE));
        
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        assertEquals(true, systems.getPage().contains(TEST_REMOTE_SYSTEM_THREE));
        
        systems = REMOTE_CREDENTIALS_SERVICE.listAllRemoteSystems(new PagingRequest(10));
        assertEquals(2, systems.getPage().size());
        assertEquals(true, systems.getPage().contains(TEST_REMOTE_SYSTEM_TWO));
        assertEquals(true, systems.getPage().contains(TEST_REMOTE_SYSTEM_THREE));
    }
    
    /** Test CRUD on person credentials, with listing */
    @Test public void testPersonCredentialsCRUD() throws Exception
    {
        PagingResults<String> systems = null;
        PagingResults<? extends BaseCredentialsInfo> creds = null;
        
        
        // Run as a test user
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);

        // Initially both should be empty empty
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals("No systems should be found, got " + systems.getPage(), 
                     0, systems.getPage().size());
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals("No systems should be found, got " + systems.getPage(), 
                     0, systems.getPage().size());
        
        
        // Create for a person
        PasswordCredentialsInfoImpl pwCred = new PasswordCredentialsInfoImpl();
        pwCred.setRemoteUsername(TEST_REMOTE_USERNAME_ONE);
        BaseCredentialsInfo credentials = REMOTE_CREDENTIALS_SERVICE.createPersonCredentials(TEST_REMOTE_SYSTEM_ONE, pwCred);
        
        // Check the new object was populated properly
        assertNotNull(credentials);
        assertNotNull(credentials.getNodeRef());
        assertNotNull(credentials.getRemoteSystemContainerNodeRef());
        assertEquals(TEST_REMOTE_SYSTEM_ONE, credentials.getRemoteSystemName());
        assertEquals(TEST_REMOTE_USERNAME_ONE, credentials.getRemoteUsername());
        assertEquals(RemoteCredentialsModel.TYPE_PASSWORD_CREDENTIALS, credentials.getCredentialsType());
        
        
        // Fetch and re-check
        credentials = REMOTE_CREDENTIALS_SERVICE.getPersonCredentials(TEST_REMOTE_SYSTEM_ONE);
        assertNotNull(credentials);
        assertNotNull(credentials.getNodeRef());
        assertNotNull(credentials.getRemoteSystemContainerNodeRef());
        assertEquals(TEST_REMOTE_SYSTEM_ONE, credentials.getRemoteSystemName());
        assertEquals(TEST_REMOTE_USERNAME_ONE, credentials.getRemoteUsername());
        assertEquals(RemoteCredentialsModel.TYPE_PASSWORD_CREDENTIALS, credentials.getCredentialsType());
        
        // Won't be there for non-existent systems
        credentials = REMOTE_CREDENTIALS_SERVICE.getPersonCredentials(TEST_REMOTE_SYSTEM_TWO);
        assertEquals(null, credentials);
        credentials = REMOTE_CREDENTIALS_SERVICE.getPersonCredentials(TEST_REMOTE_SYSTEM_THREE);
        assertEquals(null, credentials);
        
        
        // Update
        credentials = REMOTE_CREDENTIALS_SERVICE.getPersonCredentials(TEST_REMOTE_SYSTEM_ONE);
        assertEquals(PasswordCredentialsInfoImpl.class, credentials.getClass());
        pwCred = (PasswordCredentialsInfoImpl)credentials;
        
        pwCred.setRemoteUsername(TEST_REMOTE_USERNAME_TWO);
        pwCred.setRemotePassword("testing");
        
        credentials = REMOTE_CREDENTIALS_SERVICE.updateCredentials(pwCred);
        assertNotNull(credentials);
        assertEquals(TEST_REMOTE_USERNAME_TWO, credentials.getRemoteUsername());
        
        // Fetch and re-check
        pwCred = (PasswordCredentialsInfoImpl)REMOTE_CREDENTIALS_SERVICE.getPersonCredentials(TEST_REMOTE_SYSTEM_ONE);
        assertNotNull(pwCred);
        assertEquals(TEST_REMOTE_USERNAME_TWO, pwCred.getRemoteUsername());
        assertEquals("testing", pwCred.getRemotePassword());
        
        
        // Update the auth worked flag
        credentials = REMOTE_CREDENTIALS_SERVICE.getPersonCredentials(TEST_REMOTE_SYSTEM_ONE);
        assertEquals(true, credentials.getLastAuthenticationSucceeded());
        
        // To the same thing
        credentials = REMOTE_CREDENTIALS_SERVICE.updateCredentialsAuthenticationSucceeded(true, credentials);
        assertEquals(true, credentials.getLastAuthenticationSucceeded());
        
        credentials = REMOTE_CREDENTIALS_SERVICE.getPersonCredentials(TEST_REMOTE_SYSTEM_ONE);
        assertEquals(true, credentials.getLastAuthenticationSucceeded());
        
        // To a different things
        credentials = REMOTE_CREDENTIALS_SERVICE.updateCredentialsAuthenticationSucceeded(false, credentials);
        assertEquals(false, credentials.getLastAuthenticationSucceeded());
        
        credentials = REMOTE_CREDENTIALS_SERVICE.getPersonCredentials(TEST_REMOTE_SYSTEM_ONE);
        assertEquals(false, credentials.getLastAuthenticationSucceeded());
        
        // And back
        credentials = REMOTE_CREDENTIALS_SERVICE.updateCredentialsAuthenticationSucceeded(true, credentials);
        assertEquals(true, credentials.getLastAuthenticationSucceeded());
        
        credentials = REMOTE_CREDENTIALS_SERVICE.getPersonCredentials(TEST_REMOTE_SYSTEM_ONE);
        assertEquals(true, credentials.getLastAuthenticationSucceeded());
        
        
        // List remote systems
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals(0, systems.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listAllRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        
        
        // List the credentials
        creds = REMOTE_CREDENTIALS_SERVICE.listPersonCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(1, creds.getPage().size());
        assertEquals(TEST_REMOTE_USERNAME_TWO, creds.getPage().get(0).getRemoteUsername());
        
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(0, creds.getPage().size());
        
        creds = REMOTE_CREDENTIALS_SERVICE.listAllCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(1, creds.getPage().size());
        assertEquals(TEST_REMOTE_USERNAME_TWO, creds.getPage().get(0).getRemoteUsername());
        
        
        // Delete
        REMOTE_CREDENTIALS_SERVICE.deleteCredentials(credentials);
        
        credentials = REMOTE_CREDENTIALS_SERVICE.getPersonCredentials(TEST_REMOTE_SYSTEM_ONE);
        assertEquals(null, credentials);
        
        
        // List again - credentials should have gone, but system remains
        creds = REMOTE_CREDENTIALS_SERVICE.listPersonCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(0, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(0, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listAllCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(0, creds.getPage().size());
        
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals(0, systems.getPage().size());
        systems = REMOTE_CREDENTIALS_SERVICE.listAllRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        

        // Create credentials of Password, OAuth1 and OAuth2 types
        pwCred = new PasswordCredentialsInfoImpl();
        pwCred.setRemoteUsername(TEST_REMOTE_USERNAME_ONE);
        REMOTE_CREDENTIALS_SERVICE.createPersonCredentials(TEST_REMOTE_SYSTEM_TWO, pwCred);
        
        OAuth1CredentialsInfoImpl oa1Cred = new OAuth1CredentialsInfoImpl();
        oa1Cred.setRemoteUsername(TEST_REMOTE_USERNAME_TWO);
        oa1Cred.setOAuthToken("test");
        REMOTE_CREDENTIALS_SERVICE.createPersonCredentials(TEST_REMOTE_SYSTEM_TWO, oa1Cred);
        
        OAuth2CredentialsInfoImpl oa2Cred = new OAuth2CredentialsInfoImpl();
        oa2Cred.setRemoteUsername(TEST_REMOTE_USERNAME_THREE);
        oa2Cred.setOauthAccessToken("testA");
        oa2Cred.setOauthRefreshToken("testR");
        REMOTE_CREDENTIALS_SERVICE.createPersonCredentials(TEST_REMOTE_SYSTEM_TWO, oa2Cred);
    
        
        // List, should see all three sets of credentials
        creds = REMOTE_CREDENTIALS_SERVICE.listPersonCredentials(TEST_REMOTE_SYSTEM_TWO, null, new PagingRequest(10));
        assertEquals(3, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_TWO, null, new PagingRequest(10));
        assertEquals(0, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listAllCredentials(TEST_REMOTE_SYSTEM_TWO, null, new PagingRequest(10));
        assertEquals(3, creds.getPage().size());
        
        // List the systems, still only system one and two
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals(2, systems.getPage().size());
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals(0, systems.getPage().size());
        systems = REMOTE_CREDENTIALS_SERVICE.listAllRemoteSystems(new PagingRequest(10));
        assertEquals(2, systems.getPage().size());
        
        
        // Check we can filter credentials by type
        creds = REMOTE_CREDENTIALS_SERVICE.listPersonCredentials(TEST_REMOTE_SYSTEM_TWO, null, new PagingRequest(10));
        assertEquals(3, creds.getPage().size());
        
        creds = REMOTE_CREDENTIALS_SERVICE.listPersonCredentials(TEST_REMOTE_SYSTEM_TWO, RemoteCredentialsModel.TYPE_PASSWORD_CREDENTIALS, new PagingRequest(10));
        assertEquals(1, creds.getPage().size());
        assertEquals(TEST_REMOTE_USERNAME_ONE, creds.getPage().get(0).getRemoteUsername());
        
        creds = REMOTE_CREDENTIALS_SERVICE.listPersonCredentials(TEST_REMOTE_SYSTEM_TWO, RemoteCredentialsModel.TYPE_OAUTH1_CREDENTIALS, new PagingRequest(10));
        assertEquals(1, creds.getPage().size());
        assertEquals(TEST_REMOTE_USERNAME_TWO, creds.getPage().get(0).getRemoteUsername());
        
        creds = REMOTE_CREDENTIALS_SERVICE.listPersonCredentials(TEST_REMOTE_SYSTEM_TWO, RemoteCredentialsModel.TYPE_OAUTH2_CREDENTIALS, new PagingRequest(10));
        assertEquals(1, creds.getPage().size());
        assertEquals(TEST_REMOTE_USERNAME_THREE, creds.getPage().get(0).getRemoteUsername());
    }
    
    /** Test CRUD on shared credentials, with listing */
    @Test public void testSharedCredentialsCRUD() throws Exception
    {
        PagingResults<String> systems = null;
        PagingResults<? extends BaseCredentialsInfo> creds = null;
        
        
        // Run as a test user
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);

        // Initially both should be empty empty
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals("No systems should be found, got " + systems.getPage(), 
                     0, systems.getPage().size());
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals("No systems should be found, got " + systems.getPage(), 
                     0, systems.getPage().size());
        

        // Create shared
        PasswordCredentialsInfoImpl pwCred = new PasswordCredentialsInfoImpl();
        pwCred.setRemoteUsername(TEST_REMOTE_USERNAME_ONE);
        BaseCredentialsInfo credentials = REMOTE_CREDENTIALS_SERVICE.createSharedCredentials(TEST_REMOTE_SYSTEM_ONE, pwCred);
        
        // Check the new object was populated properly
        assertNotNull(credentials);
        assertNotNull(credentials.getNodeRef());
        assertNotNull(credentials.getRemoteSystemContainerNodeRef());
        assertEquals(TEST_REMOTE_SYSTEM_ONE, credentials.getRemoteSystemName());
        assertEquals(TEST_REMOTE_USERNAME_ONE, credentials.getRemoteUsername());
        assertEquals(RemoteCredentialsModel.TYPE_PASSWORD_CREDENTIALS, credentials.getCredentialsType());
        
        
        // Update
        pwCred = (PasswordCredentialsInfoImpl)credentials;
        pwCred.setRemoteUsername(TEST_REMOTE_USERNAME_TWO);
        credentials = REMOTE_CREDENTIALS_SERVICE.updateCredentials(pwCred);
        
        assertNotNull(credentials);
        assertNotNull(credentials.getNodeRef());
        assertNotNull(credentials.getRemoteSystemContainerNodeRef());
        assertEquals(TEST_REMOTE_SYSTEM_ONE, credentials.getRemoteSystemName());
        assertEquals(TEST_REMOTE_USERNAME_TWO, credentials.getRemoteUsername());
        assertEquals(RemoteCredentialsModel.TYPE_PASSWORD_CREDENTIALS, credentials.getCredentialsType());
        
        
        // List
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals(0, systems.getPage().size());
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        systems = REMOTE_CREDENTIALS_SERVICE.listAllRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        
        creds = REMOTE_CREDENTIALS_SERVICE.listPersonCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(0, creds.getPage().size());
        
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(1, creds.getPage().size());
        assertEquals(TEST_REMOTE_USERNAME_TWO, creds.getPage().get(0).getRemoteUsername());
        
        creds = REMOTE_CREDENTIALS_SERVICE.listAllCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(1, creds.getPage().size());
        assertEquals(TEST_REMOTE_USERNAME_TWO, creds.getPage().get(0).getRemoteUsername());
        
        
        // Delete
        REMOTE_CREDENTIALS_SERVICE.deleteCredentials(credentials);
        
        
        // List, system remains, credentials gone
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals(0, systems.getPage().size());
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        systems = REMOTE_CREDENTIALS_SERVICE.listAllRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        
        creds = REMOTE_CREDENTIALS_SERVICE.listPersonCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(0, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(0, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listAllCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(0, creds.getPage().size());
    }
    
    /** Dedicated permissions and paging tests */
    @Test public void testListingPermissionsAndPaging() throws Exception
    {
        PagingResults<String> systems = null;
        PagingResults<? extends BaseCredentialsInfo> creds = null;
        
        
        // Run as a test user
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);

        // Initially both should be empty empty
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals("No systems should be found, got " + systems.getPage(), 
                     0, systems.getPage().size());
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals("No systems should be found, got " + systems.getPage(), 
                     0, systems.getPage().size());
        

        // Create some credentials as the first user, for systems One and Two
        PasswordCredentialsInfoImpl pwCred = new PasswordCredentialsInfoImpl();
        pwCred.setRemoteUsername(TEST_REMOTE_USERNAME_ONE);
        REMOTE_CREDENTIALS_SERVICE.createSharedCredentials(TEST_REMOTE_SYSTEM_ONE, pwCred);
        
        pwCred = new PasswordCredentialsInfoImpl();
        pwCred.setRemoteUsername(TEST_REMOTE_USERNAME_TWO);
        REMOTE_CREDENTIALS_SERVICE.createSharedCredentials(TEST_REMOTE_SYSTEM_ONE, pwCred);
        
        pwCred = new PasswordCredentialsInfoImpl();
        pwCred.setRemoteUsername(TEST_REMOTE_USERNAME_TWO);
        REMOTE_CREDENTIALS_SERVICE.createSharedCredentials(TEST_REMOTE_SYSTEM_TWO, pwCred);
        
        pwCred = new PasswordCredentialsInfoImpl();
        pwCred.setRemoteUsername(TEST_REMOTE_USERNAME_TWO);
        REMOTE_CREDENTIALS_SERVICE.createPersonCredentials(TEST_REMOTE_SYSTEM_TWO, pwCred);
        
        
        // Switch to the second user, create some credentials on Two and Three
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
        
        pwCred = new PasswordCredentialsInfoImpl();
        pwCred.setRemoteUsername(TEST_REMOTE_USERNAME_TWO);
        REMOTE_CREDENTIALS_SERVICE.createSharedCredentials(TEST_REMOTE_SYSTEM_TWO, pwCred);
        
        pwCred = new PasswordCredentialsInfoImpl();
        pwCred.setRemoteUsername(TEST_REMOTE_USERNAME_THREE);
        REMOTE_CREDENTIALS_SERVICE.createSharedCredentials(TEST_REMOTE_SYSTEM_THREE, pwCred);
        
        pwCred = new PasswordCredentialsInfoImpl();
        pwCred.setRemoteUsername(TEST_REMOTE_USERNAME_THREE);
        REMOTE_CREDENTIALS_SERVICE.createPersonCredentials(TEST_REMOTE_SYSTEM_THREE, pwCred);
        
        
        // Check the listings of remote systems for each user
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        assertEquals(TEST_REMOTE_SYSTEM_TWO, systems.getPage().get(0));
        
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
        systems = REMOTE_CREDENTIALS_SERVICE.listPersonRemoteSystems(new PagingRequest(10));
        assertEquals(1, systems.getPage().size());
        assertEquals(TEST_REMOTE_SYSTEM_THREE, systems.getPage().get(0));
        
        
        // Check the listings of remote systems that are shared - shouldn't matter which user
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals(3, systems.getPage().size());
        
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals(3, systems.getPage().size());
        
        
        // Check the listings of the credentials by user
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
        creds = REMOTE_CREDENTIALS_SERVICE.listPersonCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(0, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listPersonCredentials(TEST_REMOTE_SYSTEM_TWO, null, new PagingRequest(10));
        assertEquals(1, creds.getPage().size());
        assertEquals(TEST_REMOTE_SYSTEM_TWO, creds.getPage().get(0).getRemoteSystemName());
        creds = REMOTE_CREDENTIALS_SERVICE.listPersonCredentials(TEST_REMOTE_SYSTEM_THREE, null, new PagingRequest(10));
        assertEquals(0, creds.getPage().size());
        
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
        creds = REMOTE_CREDENTIALS_SERVICE.listPersonCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(0, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listPersonCredentials(TEST_REMOTE_SYSTEM_TWO, null, new PagingRequest(10));
        assertEquals(0, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listPersonCredentials(TEST_REMOTE_SYSTEM_THREE, null, new PagingRequest(10));
        assertEquals(1, creds.getPage().size());
        assertEquals(TEST_REMOTE_SYSTEM_THREE, creds.getPage().get(0).getRemoteSystemName());
        
        
        // Check the shared listing of credentials, same for both users
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(2, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_TWO, null, new PagingRequest(10));
        assertEquals(2, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_THREE, null, new PagingRequest(10));
        assertEquals(1, creds.getPage().size());
        
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(2, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_TWO, null, new PagingRequest(10));
        assertEquals(2, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_THREE, null, new PagingRequest(10));
        assertEquals(1, creds.getPage().size());
        
        
        // Check the paging of remote systems
        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(10));
        assertEquals(3, systems.getPage().size());
        assertEquals(false, systems.hasMoreItems());
        assertEquals(3, systems.getTotalResultCount().getFirst().intValue());

        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(1));
        assertEquals(1, systems.getPage().size());
        assertEquals(true, systems.hasMoreItems());
        assertEquals(3, systems.getTotalResultCount().getFirst().intValue());

        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(1, 2));
        assertEquals(2, systems.getPage().size());
        assertEquals(false, systems.hasMoreItems());
        assertEquals(3, systems.getTotalResultCount().getFirst().intValue());

        systems = REMOTE_CREDENTIALS_SERVICE.listSharedRemoteSystems(new PagingRequest(2, 2));
        assertEquals(1, systems.getPage().size());
        assertEquals(false, systems.hasMoreItems());
        assertEquals(3, systems.getTotalResultCount().getFirst().intValue());
     
        
        // Check the paging of credentials
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(2, creds.getPage().size());
        assertEquals(false, creds.hasMoreItems());
        assertEquals(2, creds.getTotalResultCount().getFirst().intValue());
        
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(1));
        assertEquals(1, creds.getPage().size());
        assertEquals(true, creds.hasMoreItems());
        assertEquals(2, creds.getTotalResultCount().getFirst().intValue());
        
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(1,1));
        assertEquals(1, creds.getPage().size());
        assertEquals(false, creds.hasMoreItems());
        assertEquals(2, creds.getTotalResultCount().getFirst().intValue());
        
        
        // Tweak shared permissions
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(2, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_TWO, null, new PagingRequest(10));
        assertEquals(2, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_THREE, null, new PagingRequest(10));
        assertEquals(1, creds.getPage().size());

        // Systems One and Two were created by users one
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(1));
        NodeRef sharedS1 = creds.getPage().get(0).getRemoteSystemContainerNodeRef();
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_TWO, null, new PagingRequest(1));
        NodeRef sharedS2 = creds.getPage().get(0).getRemoteSystemContainerNodeRef();
        
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        PERMISSION_SERVICE.setInheritParentPermissions(sharedS1, false);
        PERMISSION_SERVICE.setInheritParentPermissions(sharedS2, false);
        
        
        // Should then only be able to see Three, the one they created
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(0, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_TWO, null, new PagingRequest(10));
        assertEquals(0, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_THREE, null, new PagingRequest(10));
        assertEquals(1, creds.getPage().size());
        
        // User One won't be able to see User Two's shared credentials under S2 under the new permissions
        // They can still see their own credentials for S1 and S2, plus all in S3 (permissions unchanged)
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_ONE, null, new PagingRequest(10));
        assertEquals(2, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_TWO, null, new PagingRequest(10));
        assertEquals(1, creds.getPage().size());
        creds = REMOTE_CREDENTIALS_SERVICE.listSharedCredentials(TEST_REMOTE_SYSTEM_THREE, null, new PagingRequest(10));
        assertEquals(1, creds.getPage().size());
    }
    
    /**
     * Most of the shared credentials container tests work on the test one,
     *  so that things are in a known and empty state.
     * We have this one test that uses the real shared container, just to check
     *  that it's correctly setup and available
     */
    @Test public void testRealSharedCredentialsContainer() throws Exception
    {
        // Create a new instance, using the real container
        RemoteCredentialsServiceImpl realService = new RemoteCredentialsServiceImpl();
        realService.setDictionaryService(DICTIONARY_SERVICE);
        realService.setNamespaceService(NAMESPACE_SERVICE);
        realService.setNodeService(PUBLIC_NODE_SERVICE);
        realService.setRepositoryHelper(REPOSITORY_HELPER);

        for (Entry<QName,RemoteCredentialsInfoFactory> e : ((RemoteCredentialsServiceImpl)PRIVATE_REMOTE_CREDENTIALS_SERVICE).getCredentialsFactories().entrySet() )
        {
            realService.registerCredentialsFactory(e.getKey(), e.getValue());
        }

        
        // Run as a test user
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
        
        // Do a create / fetch / delete step
        PasswordCredentialsInfoImpl pwCredI = new PasswordCredentialsInfoImpl();
        pwCredI.setRemoteUsername(TEST_REMOTE_USERNAME_ONE);
        pwCredI.setRemotePassword(TEST_USER_THREE);
        BaseCredentialsInfo credentials = null;
        
        try
        {
            // Create
            credentials = realService.createSharedCredentials(TEST_REMOTE_SYSTEM_ONE, pwCredI);
            assertEquals(TEST_REMOTE_USERNAME_ONE, credentials.getRemoteUsername());

            // Update
            ((PasswordCredentialsInfoImpl)credentials).setRemoteUsername(TEST_REMOTE_USERNAME_TWO);
            ((PasswordCredentialsInfoImpl)credentials).setRemotePassword(TEST_USER_ONE);
            credentials = realService.updateCredentials(credentials);
            assertEquals(TEST_REMOTE_USERNAME_TWO, credentials.getRemoteUsername());
            
            // Delete
            realService.deleteCredentials(credentials);
            
            // Tidy, and zap the test parent
            PUBLIC_NODE_SERVICE.deleteNode(credentials.getRemoteSystemContainerNodeRef());
            credentials = null;
        }
        finally
        {
            // Tidy up if needed
            if (credentials != null)
            {
                AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
                
                // Zap the credentials themselves
                PUBLIC_NODE_SERVICE.deleteNode(credentials.getNodeRef());
                
                // And their test parent
                PUBLIC_NODE_SERVICE.deleteNode(credentials.getRemoteSystemContainerNodeRef());
            }
        }
    }
    
    // --------------------------------------------------------------------------------
    
    /**
     * By default, all tests are run as the admin user.
     */
    @Before public void setAdminUser()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
    }
    
    @After public void deleteTestNodes() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        
        // Find the shared system container, and zap contents
        NodeRef container = ((RemoteCredentialsServiceImpl)PRIVATE_REMOTE_CREDENTIALS_SERVICE).getSharedContainerNodeRef(false);
        if (container != null)
        {
            List<NodeRef> children = new ArrayList<NodeRef>();
            for (ChildAssociationRef child : PUBLIC_NODE_SERVICE.getChildAssocs(container))
            {
                children.add(child.getChildRef());
            }
            performDeletionOfNodes(children);
        }

        // Zap the users, including any credentials stored for them
        deleteUser(TEST_USER_ONE);
        deleteUser(TEST_USER_TWO);
        deleteUser(TEST_USER_THREE);
    }
    
    @AfterClass public static void remoteTestSharedCredentialsContainer() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        NodeRef container = ((RemoteCredentialsServiceImpl)PRIVATE_REMOTE_CREDENTIALS_SERVICE).getSharedContainerNodeRef(false);
        if (container != null)
        {
            performDeletionOfNodes(Collections.singletonList(container));
        }
    }
    
    /**
     * Deletes the specified NodeRefs, if they exist.
     * @param nodesToDelete
     */
    private static void performDeletionOfNodes(final List<NodeRef> nodesToDelete)
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
           @Override
           public Void execute() throws Throwable
           {
              AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);

              for (NodeRef node : nodesToDelete)
              {
                 if (NODE_SERVICE.exists(node))
                 {
                    NODE_SERVICE.deleteNode(node);
                 }
              }

              return null;
           }
        });
    }
    
    private static void createUser(final String userName)
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
           @Override
           public Void execute() throws Throwable
           {
              if (!AUTHENTICATION_SERVICE.authenticationExists(userName))
              {
                 AUTHENTICATION_SERVICE.createAuthentication(userName, "PWD".toCharArray());
              }

              if (!PERSON_SERVICE.personExists(userName))
              {
                 PropertyMap ppOne = new PropertyMap();
                 ppOne.put(ContentModel.PROP_USERNAME, userName);
                 ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
                 ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
                 ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
                 ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

                 PERSON_SERVICE.createPerson(ppOne);
              }

              return null;
           }
        });
    }

    private static void deleteUser(final String userName)
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
           @Override
           public Void execute() throws Throwable
           {
              if (PERSON_SERVICE.personExists(userName))
              {
                 PERSON_SERVICE.deletePerson(userName);
              }

              return null;
           }
        });
    }
}
