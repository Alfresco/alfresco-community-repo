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
package org.alfresco.repo.oauth2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.oauth2.OAuth2CredentialsStoreService;
import org.alfresco.service.cmr.remotecredentials.OAuth2CredentialsInfo;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

@Category(BaseSpringTestsCategory.class)
public class OAuth2CredentialsStoreServiceTest
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private static OAuth2CredentialsStoreService oauth2CredentialsStoreService;
    private static ServiceRegistry serviceRegistry;
    private static RetryingTransactionHelper transactionHelper;
    private static MutableAuthenticationService authenticationService;
    private static PersonService personService;
    
    private static String RemoteSystemId = "Test-OAuth2RemoteSystem";
    
    //New
    private static String AccessToken = "123456789ABC";
    private static String RefreshToken = "CBA987654321";
    private static long dec291999 = 946450800L;
    private static Date ExpiresAt = new Date(dec291999);
    private static Date IssuedAt = new Date(dec291999);
    
    //Updated
    private static String UpdatedAccessToken = "abcdefghi123";
    private static String UpdatedRefreshToken = "321ihgfedcba";
    private static long dec292012 = 1356764400L;
    private static Date UpdatedExpiresAt = new Date(dec292012);
    private static Date UpdatedIssuedAt = new Date(dec292012);
    
    //Users
    private static String TEST_USER_ONE = OAuth2CredentialsStoreService.class.getSimpleName() + "testuser1";
    private static String TEST_USER_TWO = OAuth2CredentialsStoreService.class.getSimpleName() + "testuser2";
    private static final String ADMIN_USER = AuthenticationUtil.getAdminUserName();

    @BeforeClass
    public static void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionHelper = serviceRegistry.getTransactionService().getRetryingTransactionHelper();
        authenticationService = serviceRegistry.getAuthenticationService();
        personService = serviceRegistry.getPersonService();
        oauth2CredentialsStoreService = (OAuth2CredentialsStoreService) ctx.getBean("oauth2CredentialsStoreService");
        
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
    public void testStorePersonalOAuth2Credentials()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
        //Store new credentials
        oauth2CredentialsStoreService.storePersonalOAuth2Credentials(RemoteSystemId, AccessToken, RefreshToken, ExpiresAt, IssuedAt);
        OAuth2CredentialsInfo oAuth2CredentialsInfo = oauth2CredentialsStoreService.getPersonalOAuth2Credentials(RemoteSystemId);
        
        assertEquals("Expect that access tokens will match", AccessToken, oAuth2CredentialsInfo.getOAuthAccessToken());
        assertEquals("Expect the refresh token will match", RefreshToken, oAuth2CredentialsInfo.getOAuthRefreshToken());
        assertEquals("Expect that the expiration date will match", ExpiresAt, oAuth2CredentialsInfo.getOAuthTicketExpiresAt());
        assertEquals("Expect that the issued date will match", IssuedAt, oAuth2CredentialsInfo.getOAuthTicketIssuedAt());
        
        //Update credentials
        oauth2CredentialsStoreService.storePersonalOAuth2Credentials(RemoteSystemId, UpdatedAccessToken, UpdatedRefreshToken, UpdatedExpiresAt, UpdatedIssuedAt);
        OAuth2CredentialsInfo _oAuth2CredentialsInfo = oauth2CredentialsStoreService.getPersonalOAuth2Credentials(RemoteSystemId);
        
        assertEquals("Expect that access tokens will match", UpdatedAccessToken, _oAuth2CredentialsInfo.getOAuthAccessToken());
        assertEquals("Expect the refresh token will match", UpdatedRefreshToken, _oAuth2CredentialsInfo.getOAuthRefreshToken());
        assertEquals("Expect that the expiration date will match", UpdatedExpiresAt, _oAuth2CredentialsInfo.getOAuthTicketExpiresAt());
        assertEquals("Expect that the issued date will match", UpdatedIssuedAt, _oAuth2CredentialsInfo.getOAuthTicketIssuedAt());
    }

    @Test
    public void testStoreSharedOAuth2Credentials()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
        //Store new credentials
        oauth2CredentialsStoreService.storeSharedOAuth2Credentials(RemoteSystemId, AccessToken, RefreshToken, ExpiresAt, IssuedAt);
        
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
        List<OAuth2CredentialsInfo> sharedCredentials = oauth2CredentialsStoreService.listSharedOAuth2Credentials(RemoteSystemId);
        OAuth2CredentialsInfo oAuth2CredentialsInfo = sharedCredentials.get(0);
        
        assertEquals("Expect that access tokens will match", AccessToken, oAuth2CredentialsInfo.getOAuthAccessToken());
        assertEquals("Expect the refresh token will match", RefreshToken, oAuth2CredentialsInfo.getOAuthRefreshToken());
        assertEquals("Expect that the expiration date will match", ExpiresAt, oAuth2CredentialsInfo.getOAuthTicketExpiresAt());
        assertEquals("Expect that the issued date will match", IssuedAt, oAuth2CredentialsInfo.getOAuthTicketIssuedAt());
    }

    @Test (expected=AccessDeniedException.class)
    public void testSecureUpdateSharedOAuth2Credentials()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
        //Update credentials
        List<OAuth2CredentialsInfo> sharedCredentials = oauth2CredentialsStoreService.listSharedOAuth2Credentials(RemoteSystemId);
        OAuth2CredentialsInfo oAuth2CredentialsInfo = sharedCredentials.get(0); 
        oauth2CredentialsStoreService.updateSharedOAuth2Credentials(oAuth2CredentialsInfo, RemoteSystemId, UpdatedAccessToken, UpdatedRefreshToken, UpdatedExpiresAt, UpdatedIssuedAt);
    }
    
    @Test
    public void testUpdateSharedOAuth2CredentialsTestDeletePesonalOAuth2Credentials()
    {
        {
            AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
            //Update credentials
            List<OAuth2CredentialsInfo> sharedCredentials = oauth2CredentialsStoreService.listSharedOAuth2Credentials(RemoteSystemId);
            OAuth2CredentialsInfo oAuth2CredentialsInfo = sharedCredentials.get(0); 
            OAuth2CredentialsInfo _oAuth2CredentialsInfo = oauth2CredentialsStoreService.updateSharedOAuth2Credentials(oAuth2CredentialsInfo, RemoteSystemId, UpdatedAccessToken, UpdatedRefreshToken, UpdatedExpiresAt, UpdatedIssuedAt);
            
            assertEquals("Expect that access tokens will match", UpdatedAccessToken, _oAuth2CredentialsInfo.getOAuthAccessToken());
            assertEquals("Expect the refresh token will match", UpdatedRefreshToken, _oAuth2CredentialsInfo.getOAuthRefreshToken());
            assertEquals("Expect that the expiration date will match", UpdatedExpiresAt, _oAuth2CredentialsInfo.getOAuthTicketExpiresAt());
            assertEquals("Expect that the issued date will match", UpdatedIssuedAt, _oAuth2CredentialsInfo.getOAuthTicketIssuedAt());
        }
    
        //public void testDeletePesonalOAuth2Credentials()
        {
            AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
            boolean deleted = oauth2CredentialsStoreService.deletePersonalOAuth2Credentials(RemoteSystemId);
            
            assertTrue(deleted);
            
            AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
            boolean _deleted = oauth2CredentialsStoreService.deletePersonalOAuth2Credentials(RemoteSystemId);
            
            assertFalse(_deleted);
        }
    }

    @Test(expected=AccessDeniedException.class)
    public void testSecureDeleteSharedOAuth2CredentialsTestDeleteSharedOAuth2Credentials()
    {
        {
            AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_TWO);
            List<OAuth2CredentialsInfo> sharedCredentials = oauth2CredentialsStoreService.listSharedOAuth2Credentials(RemoteSystemId);
            OAuth2CredentialsInfo oAuth2CredentialsInfo = sharedCredentials.get(0);
            oauth2CredentialsStoreService.deleteSharedOAuth2Credentials(RemoteSystemId, oAuth2CredentialsInfo);
        }
        
        //public void testDeleteSharedOAuth2Credentials()
        {
            AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER_ONE);
            List<OAuth2CredentialsInfo> sharedCredentials = oauth2CredentialsStoreService.listSharedOAuth2Credentials(RemoteSystemId);
            OAuth2CredentialsInfo oAuth2CredentialsInfo = sharedCredentials.get(0);
            boolean deleted = oauth2CredentialsStoreService.deleteSharedOAuth2Credentials(RemoteSystemId, oAuth2CredentialsInfo); 
            
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
