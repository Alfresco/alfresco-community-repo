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
package org.alfresco.repo.oauth1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.oauth1.OAuth1CredentialsStoreService;
import org.alfresco.service.cmr.remotecredentials.OAuth1CredentialsInfo;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OAuth1CredentialsStoreServiceTest
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private static OAuth1CredentialsStoreService oauth1CredentialsStoreService;
    private static ServiceRegistry serviceRegistry;
    private static RetryingTransactionHelper transactionHelper;
    private static MutableAuthenticationService authenticationService;
    private static PersonService personService;
    
    private static String RemoteSystemId = "Test-OAuth1RemoteSystem";
    
    //New
    private static String Token = "123456789ABC";
    private static String Secret = "CBA987654321";
    
    //Updated
    private static String UpdatedToken = "abcdefghi123";
    private static String UpdatedSecret = "321ihgfedcba";
    
    //Users
    private static String TEST_USER_ONE = OAuth1CredentialsStoreService.class.getSimpleName() + "testuser1";
    private static String TEST_USER_TWO = OAuth1CredentialsStoreService.class.getSimpleName() + "testuser2";
    private static final String ADMIN_USER = AuthenticationUtil.getAdminUserName();

    @BeforeClass
    public static void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionHelper = serviceRegistry.getTransactionService().getRetryingTransactionHelper();
        authenticationService = serviceRegistry.getAuthenticationService();
        personService = serviceRegistry.getPersonService();
        oauth1CredentialsStoreService = (OAuth1CredentialsStoreService) ctx.getBean("oauth1CredentialsStoreService");
        
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        createUser(TEST_USER_ONE);
        createUser(TEST_USER_TWO);
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        // Do the teardown as admin
        AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER);
        deleteUser(TEST_USER_ONE);
        deleteUser(TEST_USER_TWO);      
    }

    @Test
    public void test1StorePersonalOAuth1Credentials()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
        //Store new credentials
        oauth1CredentialsStoreService.storePersonalOAuth1Credentials(RemoteSystemId, Token, Secret);
        OAuth1CredentialsInfo oAuth1CredentialsInfo = oauth1CredentialsStoreService.getPersonalOAuth1Credentials(RemoteSystemId);
        
        assertEquals("Expect that access tokens will match", Token, oAuth1CredentialsInfo.getOAuthToken());
        assertEquals("Expect the refresh token will match", Secret, oAuth1CredentialsInfo.getOAuthSecret());
        
        //Update credentials
        oauth1CredentialsStoreService.storePersonalOAuth1Credentials(RemoteSystemId, UpdatedToken, UpdatedSecret);
        OAuth1CredentialsInfo _oAuth1CredentialsInfo = oauth1CredentialsStoreService.getPersonalOAuth1Credentials(RemoteSystemId);
        
        assertEquals("Expect that access tokens will match", UpdatedToken, _oAuth1CredentialsInfo.getOAuthToken());
        assertEquals("Expect the refresh token will match", UpdatedSecret, _oAuth1CredentialsInfo.getOAuthSecret());
    }

    @Test
    public void test2StoreSharedOAuth1Credentials()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
        //Store new credentials
        oauth1CredentialsStoreService.storeSharedOAuth1Credentials(RemoteSystemId, Token, Secret);
        
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
        List<OAuth1CredentialsInfo> sharedCredentials = oauth1CredentialsStoreService.listSharedOAuth1Credentials(RemoteSystemId);
        OAuth1CredentialsInfo oAuth1CredentialsInfo = sharedCredentials.get(0);
        
        assertEquals("Expect that access tokens will match", Token, oAuth1CredentialsInfo.getOAuthToken());
        assertEquals("Expect the refresh token will match", Secret, oAuth1CredentialsInfo.getOAuthSecret());
    }

    @Test (expected=AccessDeniedException.class)
    public void test3SecureUpdateSharedOAuth1CredentialsTestUpdateSharedOAuth1Credentials()
    {
        {
            AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
            //Update credentials
            List<OAuth1CredentialsInfo> sharedCredentials = oauth1CredentialsStoreService.listSharedOAuth1Credentials(RemoteSystemId);
            OAuth1CredentialsInfo oAuth1CredentialsInfo = sharedCredentials.get(0); 
            oauth1CredentialsStoreService.updateSharedOAuth1Credentials(oAuth1CredentialsInfo, RemoteSystemId, UpdatedToken, UpdatedSecret);
        }
    
        //public void testUpdateSharedOAuth1Credentials()
        {
            AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
            //Update credentials
            List<OAuth1CredentialsInfo> sharedCredentials = oauth1CredentialsStoreService.listSharedOAuth1Credentials(RemoteSystemId);
            OAuth1CredentialsInfo oAuth1CredentialsInfo = sharedCredentials.get(0); 
            OAuth1CredentialsInfo _oAuth1CredentialsInfo = oauth1CredentialsStoreService.updateSharedOAuth1Credentials(oAuth1CredentialsInfo, RemoteSystemId, UpdatedToken, UpdatedSecret);
            
            assertEquals("Expect that access tokens will match", UpdatedToken, _oAuth1CredentialsInfo.getOAuthToken());
            assertEquals("Expect the refresh token will match", UpdatedSecret, _oAuth1CredentialsInfo.getOAuthSecret());
        }
    }

    @Test
    public void test4DeletePesonalOAuth1Credentials()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
        boolean deleted = oauth1CredentialsStoreService.deletePersonalOAuth1Credentials(RemoteSystemId);
        
        assertTrue(deleted);
        
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
        boolean _deleted = oauth1CredentialsStoreService.deletePersonalOAuth1Credentials(RemoteSystemId);
        
        assertFalse(_deleted);
    }

    @Test(expected=AccessDeniedException.class)
    public void test5SecureDeleteSharedOAuth1CredentialsTestDeleteSharedOAuth1Credentials()
    {
        {
            AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
            List<OAuth1CredentialsInfo> sharedCredentials = oauth1CredentialsStoreService.listSharedOAuth1Credentials(RemoteSystemId);
            OAuth1CredentialsInfo oAuth1CredentialsInfo = sharedCredentials.get(0);
            oauth1CredentialsStoreService.deleteSharedOAuth1Credentials(RemoteSystemId, oAuth1CredentialsInfo);
        }
        //public void testDeleteSharedOAuth1Credentials()
        {
            AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
            List<OAuth1CredentialsInfo> sharedCredentials = oauth1CredentialsStoreService.listSharedOAuth1Credentials(RemoteSystemId);
            OAuth1CredentialsInfo oAuth1CredentialsInfo = sharedCredentials.get(0);
            boolean deleted = oauth1CredentialsStoreService.deleteSharedOAuth1Credentials(RemoteSystemId, oAuth1CredentialsInfo); 
            
            assertTrue(deleted);
        }
    }

    
 // --------------------------------------------------------------------------------
    
    
    private static void createUser(final String userName)
    {
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
           @Override
           public Void execute() throws Throwable
           {
              if (!authenticationService.authenticationExists(userName))
              {
                 authenticationService.createAuthentication(userName, "PWD".toCharArray());
              }

              if (!personService.personExists(userName))
              {
                 PropertyMap ppOne = new PropertyMap();
                 ppOne.put(ContentModel.PROP_USERNAME, userName);
                 ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
                 ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
                 ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
                 ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

                 personService.createPerson(ppOne);
              }

              return null;
           }
        });
    }

    private static void deleteUser(final String userName)
    {
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
           @Override
           public Void execute() throws Throwable
           {
              if (personService.personExists(userName))
              {
                 personService.deletePerson(userName);
              }

              return null;
           }
        });
    }

}
