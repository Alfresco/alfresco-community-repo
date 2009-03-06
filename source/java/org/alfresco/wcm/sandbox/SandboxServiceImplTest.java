/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.wcm.sandbox;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.alfresco.wcm.asset.AssetInfo;
import org.alfresco.wcm.asset.AssetService;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.alfresco.wcm.webproject.WebProjectService;
import org.springframework.context.ApplicationContext;

/**
 * Sandbox Service implementation unit test
 * 
 * @author janv
 */
public class SandboxServiceImplTest extends TestCase 
{
    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    //
    // test data
    //
    
    private static final String TEST_RUN = ""+System.currentTimeMillis();
    private static final boolean CLEAN = true; // cleanup during teardown
    
    // base web project
    private static final String TEST_WEBPROJ_DNS  = "testSandbox-"+TEST_RUN;
    private static final String TEST_WEBPROJ_NAME = "testSandbox Web Project Display Name - "+TEST_RUN;
    private static final String TEST_WEBPROJ_TITLE = "This is my title";
    private static final String TEST_WEBPROJ_DESCRIPTION = "This is my description";
    private static final String TEST_WEBPROJ_DEFAULT_WEBAPP = WCMUtil.DIR_ROOT;
    //private static final boolean TEST_WEBPROJ_USE_AS_TEMPLATE = true;
    private static final boolean TEST_WEBPROJ_DONT_USE_AS_TEMPLATE = false;
    
    // base sandbox
    private static final String TEST_SANDBOX = TEST_WEBPROJ_DNS;
    

    private static final String USER_ADMIN = "admin";
    
    private static final String TEST_USER = "testSandboxUser-"+TEST_RUN;
    
    private static final String USER_ONE   = TEST_USER+"-One";
    private static final String USER_TWO   = TEST_USER+"-Two";
    private static final String USER_THREE = TEST_USER+"-Three";
    
    private static final int SCALE_USERS = 5;
    private static final int SCALE_WEBPROJECTS = 2;
    
    //
    // services
    //
    
    private WebProjectService wpService;
    private SandboxService sbService;
    private AssetService assetService;
    
    private AuthenticationService authenticationService;
    private PersonService personService;
    
    // TODO: temporary - remove from here when r13170 is merged from V3.1->HEAD
    private TransactionService transactionService;
    
    private AVMService avmService; // non-locking-aware
    
    //private AVMService avmLockingAwareService;
    //private AVMService avmNonLockingAwareService;

    
    @Override
    protected void setUp() throws Exception
    {
        // Get the required services
        wpService = (WebProjectService)ctx.getBean("WebProjectService");
        sbService = (SandboxService)ctx.getBean("SandboxService");
        assetService = (AssetService)ctx.getBean("AssetService");
        
        authenticationService = (AuthenticationService)ctx.getBean("AuthenticationService");
        personService = (PersonService)ctx.getBean("PersonService");
        
        avmService = (AVMService)ctx.getBean("AVMService");
        
        // TODO: temporary - remove from here when r13170 is merged from V3.1->HEAD
        transactionService = (TransactionService)ctx.getBean("TransactionService");
        
        // WCM locking
        //avmLockingAwareService = (AVMService)ctx.getBean("AVMLockingAwareService");
        
        // without WCM locking
        //avmNonLockingAwareService = (AVMService)ctx.getBean("AVMService");
       
        // By default run as Admin
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
        
        createUser(USER_ONE);
        createUser(USER_TWO);
        createUser(USER_THREE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        if (CLEAN)
        {
            // Switch back to Admin
            AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
            
            List<WebProjectInfo> webProjects = wpService.listWebProjects();
            for (final WebProjectInfo wpInfo : webProjects)
            {
                if (wpInfo.getStoreId().startsWith(TEST_WEBPROJ_DNS))
                {
                    // TODO: temporary - remove from here when r13170 is merged from V3.1->HEAD
                    
                    // note: added retry for now, due to intermittent concurrent update (during tearDown) possibly due to OrphanReaper ?
                    // org.hibernate.StaleObjectStateException: Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect): [org.alfresco.repo.avm.PlainFileNodeImpl#3752]
                    RetryingTransactionCallback<Object> deleteWebProjectWork = new RetryingTransactionCallback<Object>()
                    {
                        public Object execute() throws Exception
                        {
                            wpService.deleteWebProject(wpInfo.getNodeRef());
                            return null;
                        }
                    };
                    transactionService.getRetryingTransactionHelper().doInTransaction(deleteWebProjectWork);

                }
            }

            deleteUser(USER_ONE);
            deleteUser(USER_TWO);
            deleteUser(USER_THREE);
        }
        
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
    }
    
    private void createUser(String userName)
    {
        if (authenticationService.authenticationExists(userName) == false)
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());
            
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            personService.createPerson(ppOne);
        }
    }
    
    private void deleteUser(String userName)
    {
        if (authenticationService.authenticationExists(userName) == true)
        {
            personService.deletePerson(userName);
            authenticationService.deleteAuthentication(userName);
        }
    }
    
    public void testSimple()
    {
        int storeCnt = avmService.getStores().size();
        
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-simple", TEST_WEBPROJ_NAME+"-simple", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String wpStoreId = wpInfo.getStoreId();
        
        // list 2 sandboxes
        assertEquals(2, sbService.listSandboxes(wpStoreId).size());
        
        // list 4 extra AVM stores (2 per sandbox)
        assertEquals(storeCnt+4, avmService.getStores().size()); // 2x stating (main,preview), 2x admin author (main, preview)
        
        // get admin's sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        assertNotNull(sbInfo);
        
        // get staging sandbox
        sbInfo = sbService.getStagingSandbox(wpStoreId);
        assertNotNull(sbInfo);

        // invite user one to the web project and do not implicitly create user one's sandbox
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_PUBLISHER, false);
        assertEquals(2, sbService.listSandboxes(wpStoreId).size());
        
        sbInfo = sbService.createAuthorSandbox(wpStoreId, USER_TWO);
        assertEquals(3, sbService.listSandboxes(wpStoreId).size());
        
        sbInfo = sbService.getSandbox(sbInfo.getSandboxId());
        sbService.deleteSandbox(sbInfo.getSandboxId());
        assertEquals(2, sbService.listSandboxes(wpStoreId).size());
        
        // delete admin's sandbox
        sbService.deleteSandbox(sbService.getAuthorSandbox(wpStoreId).getSandboxId());
        assertEquals(1, sbService.listSandboxes(wpStoreId).size());
        
        // delete web project (also deletes staging sandbox)
        wpService.deleteWebProject(wpStoreId);
        
        assertEquals(storeCnt, avmService.getStores().size());
    }
	
    public void testCreateAuthorSandbox()
    {
        // Create a web project
        WebProjectInfo wpInfo1 = wpService.createWebProject(TEST_WEBPROJ_DNS+"-create-author", TEST_WEBPROJ_NAME+"-author", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        
        String expectedUserSandboxId = TEST_SANDBOX+"-create-author" + "--" + USER_ADMIN;
        
        SandboxInfo sbInfo1 = sbService.getAuthorSandbox(wpInfo1.getStoreId());
        checkSandboxInfo(sbInfo1, expectedUserSandboxId, USER_ADMIN, USER_ADMIN, expectedUserSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
        
        sbInfo1 = sbService.getAuthorSandbox(wpInfo1.getStoreId(), USER_ONE);
        assertNull(sbInfo1);
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        sbInfo1 = sbService.getAuthorSandbox(wpInfo1.getStoreId());
        assertNull(sbInfo1);
        
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
        
        // Invite web user
        wpService.inviteWebUser(wpInfo1.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER);
        
        // Create author sandbox for user one - admin is the creator
        sbInfo1 = sbService.createAuthorSandbox(wpInfo1.getStoreId(), USER_ONE);

        expectedUserSandboxId = TEST_SANDBOX+"-create-author" + "--" + USER_ONE;
        
        sbInfo1 = sbService.getAuthorSandbox(wpInfo1.getStoreId(), USER_ONE);
        checkSandboxInfo(sbInfo1, expectedUserSandboxId, USER_ONE, USER_ADMIN, expectedUserSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // Get author sandbox
        sbInfo1 = sbService.getAuthorSandbox(wpInfo1.getStoreId());
        checkSandboxInfo(sbInfo1, expectedUserSandboxId, USER_ONE, USER_ADMIN, expectedUserSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
        
        String userSandboxId = sbInfo1.getSandboxId();
        
        // Get (author) sandbox
        sbInfo1 = sbService.getSandbox(userSandboxId);
        checkSandboxInfo(sbInfo1, expectedUserSandboxId, USER_ONE, USER_ADMIN, expectedUserSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
        
        // Should return same as before
        sbInfo1 = sbService.createAuthorSandbox(wpInfo1.getStoreId());
        checkSandboxInfo(sbInfo1, expectedUserSandboxId, USER_ONE, USER_ADMIN, expectedUserSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        try
        {
            // Try to create author sandbox as a non-web user (-ve test)
            sbService.createAuthorSandbox(wpInfo1.getStoreId()); // ignore return
            fail("Shouldn't be able to create author store since not a web user");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
        
        // Invite web user
        wpService.inviteWebUser(wpInfo1.getStoreId(), USER_TWO, WCMUtil.ROLE_CONTENT_REVIEWER);
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        // Get author sandbox
        sbInfo1 = sbService.getAuthorSandbox(wpInfo1.getStoreId());
        assertNull(sbInfo1);
        
        expectedUserSandboxId = TEST_SANDBOX+"-create-author" + "--" + USER_TWO;
        
        // Create own sandbox - user two is the creator
        sbInfo1 = sbService.createAuthorSandbox(wpInfo1.getStoreId());
        checkSandboxInfo(sbInfo1, expectedUserSandboxId, USER_TWO, USER_TWO, expectedUserSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
    }
    
    private void checkSandboxInfo(SandboxInfo sbInfo, String expectedStoreId, String expectedName, String expectedCreator, String expectedMainStoreName, QName expectedSandboxType)
    {
        assertNotNull(sbInfo);
        assertEquals(expectedStoreId, sbInfo.getSandboxId());
        assertEquals(expectedName, sbInfo.getName());
        assertEquals(expectedCreator, sbInfo.getCreator());
        assertNotNull(sbInfo.getCreatedDate());
        assertEquals(expectedMainStoreName, sbInfo.getMainStoreName());
        assertEquals(expectedSandboxType, sbInfo.getSandboxType());
    }
    
    public void testListSandboxes() throws Exception
    {
        // Create web project - implicitly creates staging sandbox and also author sandbox for web project creator (in this case, admin)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-list", TEST_WEBPROJ_NAME+" list", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        List<SandboxInfo> sbInfos = sbService.listSandboxes(wpInfo.getStoreId());
        assertEquals(2, sbInfos.size()); // staging sandbox, author sandbox (for admin)
        
        String expectedUserSandboxId = TEST_SANDBOX+"-list" + "--" + USER_ADMIN;
        
        // Do detailed check of the sandbox info objects
        for (SandboxInfo sbInfo : sbInfos)
        {
            QName sbType = sbInfo.getSandboxType();
            
            if (sbType.equals(SandboxConstants.PROP_SANDBOX_STAGING_MAIN) == true)
            {
                checkSandboxInfo(sbInfo, TEST_SANDBOX+"-list", TEST_SANDBOX+"-list", USER_ADMIN, TEST_SANDBOX+"-list", SandboxConstants.PROP_SANDBOX_STAGING_MAIN);
            }
            else if (sbType.equals(SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN) == true)
            {
                checkSandboxInfo(sbInfo, expectedUserSandboxId, USER_ADMIN, USER_ADMIN, expectedUserSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
            }
            else
            {
                fail("The sandbox store id " + sbInfo.getSandboxId() + " is not recognised");
            }
        }
        
        // TODO add more here
    }
    
    public void testGetSandbox()
    {
        // Get a sandbox that isn't there
        SandboxInfo sbInfo = sbService.getSandbox(TEST_SANDBOX+"-get");
        assertNull(sbInfo);
        
        // Create web project - implicitly creates staging sandbox and also admin sandbox (author sandbox for web project creator)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-get", TEST_WEBPROJ_NAME+" get", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);

        // Get staging sandbox
        sbInfo = sbService.getStagingSandbox(wpInfo.getStoreId());
        checkSandboxInfo(sbInfo, TEST_SANDBOX+"-get", TEST_SANDBOX+"-get", USER_ADMIN, TEST_SANDBOX+"-get", SandboxConstants.PROP_SANDBOX_STAGING_MAIN);
        
         // Get (staging) sandbox
        String stagingSandboxId = wpInfo.getStagingStoreName();
        sbInfo = sbService.getSandbox(stagingSandboxId);
        checkSandboxInfo(sbInfo, TEST_SANDBOX+"-get", TEST_SANDBOX+"-get", USER_ADMIN, TEST_SANDBOX+"-get", SandboxConstants.PROP_SANDBOX_STAGING_MAIN);
    }
    
    public void testIsSandboxType()
    {
        // Get a sandbox that isn't there
        SandboxInfo sbInfo = sbService.getSandbox(TEST_SANDBOX+"-is");
        assertNull(sbInfo);
        
        // Create web project - implicitly creates staging sandbox and also admin sandbox (author sandbox for web project creator)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-is", TEST_WEBPROJ_NAME+" is", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);

        // Get staging sandbox
        sbInfo = sbService.getStagingSandbox(wpInfo.getStoreId());
        
        assertTrue(sbService.isSandboxType(sbInfo.getSandboxId(), SandboxConstants.PROP_SANDBOX_STAGING_MAIN));
        assertFalse(sbService.isSandboxType(sbInfo.getSandboxId(), SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN));
     
        // Get author sandbox
        sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        
        assertTrue(sbService.isSandboxType(sbInfo.getSandboxId(), SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN));
        assertFalse(sbService.isSandboxType(sbInfo.getSandboxId(), SandboxConstants.PROP_SANDBOX_STAGING_MAIN));
    }
    
    public void testDeleteSandbox()
    {
        // Create web project - implicitly creates staging sandbox and also admin sandbox (author sandbox for web project creator)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-delete", TEST_WEBPROJ_NAME+" delete", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        String wpStoreId = wpInfo.getStoreId();
        
        assertEquals(2, sbService.listSandboxes(wpStoreId).size());
        
        // Get staging sandbox
        SandboxInfo sbInfo = sbService.getStagingSandbox(wpStoreId);
        
        try
        {
            // Try to delete staging sandbox (-ve test)
            sbService.deleteSandbox(sbInfo.getSandboxId());
            fail("Shouldn't be able to delete staging sandbox");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        try
        {
            // Try to delete non-existant sandbox (-ve test)
            sbService.deleteSandbox("some-random-staging-sandbox");
            fail("Shouldn't be able to delete non-existant sandbox");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Get admin author sandbox
        sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        sbService.deleteSandbox(sbInfo.getSandboxId());
        
        assertEquals(1, sbService.listSandboxes(wpInfo.getStoreId()).size());
        
        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER);
        wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_PUBLISHER);
        wpService.inviteWebUser(wpStoreId, USER_THREE, WCMUtil.ROLE_CONTENT_REVIEWER, true);
        
        assertEquals(2, sbService.listSandboxes(wpStoreId).size());
        
        sbService.createAuthorSandbox(wpStoreId, USER_ONE);
        sbService.createAuthorSandbox(wpStoreId, USER_TWO);
        
        assertEquals(4, sbService.listSandboxes(wpStoreId).size());
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        // NOTE: content publisher can list other sandboxes
        assertEquals(4, sbService.listSandboxes(wpStoreId).size());
        
        sbInfo = sbService.getAuthorSandbox(wpStoreId);
        assertNotNull(sbInfo);
        
        // can delete own sandbox
        sbService.deleteSandbox(sbInfo.getSandboxId());
        
        assertEquals(3, sbService.listSandboxes(wpStoreId).size());
        
        sbInfo = sbService.getAuthorSandbox(wpStoreId);
        assertNull(sbInfo);
        
        // but not others
        try
        {
            // Try to delete another author's sandbox as a non-content manager (-ve test)
            sbService.deleteSandbox(wpInfo.getStoreId()+"--"+USER_THREE);
            fail("Shouldn't be able to delete another author's sandbox");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }  
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        assertEquals(3, sbService.listSandboxes(wpStoreId).size());
        
        // content manager can delete others
        sbInfo = sbService.getAuthorSandbox(wpStoreId, USER_THREE);
        sbService.deleteSandbox(sbInfo.getSandboxId());
        
        assertEquals(2, sbService.listSandboxes(wpStoreId).size());
    }
    
    // list changed (in this test, new) assets in user sandbox compared to staging sandbox
    public void testListNewItems1()
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-listNewItems1", TEST_WEBPROJ_NAME+" listNewItems1", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        String wpStoreId = wpInfo.getStoreId();
        
        assertEquals(2, sbService.listSandboxes(wpStoreId).size());
        
        // add web app (in addition to default ROOT web app)
        String myWebApp = "myWebApp";
        wpService.createWebApp(wpStoreId, myWebApp, "this is my web app");
        
        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR);
        sbService.createAuthorSandbox(wpStoreId, USER_ONE);
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String sbStoreId = sbInfo.getSandboxId();
        
        // no changes yet
        List<AssetInfo> assets = sbService.listChangedAll(sbStoreId, true);
        assertEquals(0, assets.size());
      
        String authorSandboxMyWebAppRelativePath = sbInfo.getSandboxRootPath() + "/" + myWebApp; // in this case, my web app is 'myWebApp'
        String authorSandboxDefaultWebAppRelativePath = sbInfo.getSandboxRootPath() + "/" + wpInfo.getDefaultWebApp(); // in this case, default web app is 'ROOT'

        assetService.createFile(sbStoreId, authorSandboxMyWebAppRelativePath, "myFile1", null);
        
        assets = sbService.listChangedAll(sbStoreId, false);
        assertEquals(1, assets.size());
        assertEquals("myFile1", assets.get(0).getName());
        
        assetService.createFolder(sbStoreId, authorSandboxDefaultWebAppRelativePath, "myDir1", null);
        assetService.createFile(sbStoreId, authorSandboxDefaultWebAppRelativePath+"/myDir1", "myFile2", null);
        assetService.createFolder(sbStoreId, authorSandboxDefaultWebAppRelativePath+"/myDir1", "myDir2", null);
        assetService.createFile(sbStoreId, authorSandboxDefaultWebAppRelativePath+"/myDir1/myDir2", "myFile3", null);
        assetService.createFile(sbStoreId, authorSandboxDefaultWebAppRelativePath+"/myDir1/myDir2", "myFile4", null);
        assetService.createFolder(sbStoreId, authorSandboxDefaultWebAppRelativePath+"/myDir1", "myDir3", null);
        
        assets = sbService.listChangedAll(sbStoreId, false);
        assertEquals(2, assets.size()); // new dir with new dirs/files is returned as single change
        
        for (AssetInfo asset : assets)
        {
            if (asset.getName().equals("myFile1") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("myDir1") && asset.isFolder())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
        
        assets = sbService.listChangedWebApp(sbStoreId, wpInfo.getDefaultWebApp(), false);
        assertEquals(1, assets.size());
        
        for (AssetInfo asset : assets)
        {
            if (asset.getName().equals("myDir1") && asset.isFolder())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
        
        assets = sbService.listChanged(sbStoreId, authorSandboxDefaultWebAppRelativePath+"/myDir1", false);
        assertEquals(1, assets.size());
        
        for (AssetInfo asset : assets)
        {
            if (asset.getName().equals("myDir1") && asset.isFolder())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
    }
    
    // list changed (in this test, new) assets in two different user sandboxes compared to each other
    public void testListNewItems2()
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-listNewItems2", TEST_WEBPROJ_NAME+" listNewItems2", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        String wpStoreId = wpInfo.getStoreId();

        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        SandboxInfo sbInfo1 = sbService.getAuthorSandbox(wpStoreId);
        String sbStoreId = sbInfo1.getSandboxId();
        
        List<AssetInfo> assets = sbService.listChangedAll(sbStoreId, true);
        assertEquals(0, assets.size());
        
        assetService.createFile(sbStoreId, sbInfo1.getSandboxRootPath(), "myFile1", null);
        
        assets = sbService.listChangedAll(sbStoreId, false);
        assertEquals(1, assets.size());
        assertEquals("myFile1", assets.get(0).getName());
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        SandboxInfo sbInfo2 = sbService.getAuthorSandbox(wpStoreId);
        sbStoreId = sbInfo2.getSandboxId();
        
        assets = sbService.listChangedAll(sbStoreId, true);
        assertEquals(0, assets.size());
        
        assetService.createFile(sbStoreId, sbInfo2.getSandboxRootPath(), "myFile2", null);
        assetService.createFile(sbStoreId, sbInfo2.getSandboxRootPath(), "myFile3", null);
        
        assets = sbService.listChangedAll(sbStoreId, false);
        assertEquals(2, assets.size());

        for (AssetInfo asset : assets)
        {
            if (asset.getName().equals("myFile2") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("myFile3") && asset.isFile())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
        
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
        
        sbInfo1 = sbService.getAuthorSandbox(wpStoreId, USER_ONE);
        sbInfo2 = sbService.getAuthorSandbox(wpStoreId, USER_TWO);
        
        assets = sbService.listChanged(sbInfo1.getSandboxId(), sbInfo1.getSandboxRootPath(), sbInfo2.getSandboxId(), sbInfo2.getSandboxRootPath(), false);
        assertEquals(1, assets.size());
        assertEquals("myFile1", assets.get(0).getName());
        
        assets = sbService.listChanged(sbInfo2.getSandboxId(), sbInfo2.getSandboxRootPath(), sbInfo1.getSandboxId(), sbInfo1.getSandboxRootPath(), false);
        assertEquals(2, assets.size());
        
        for (AssetInfo asset : assets)
        {
            if (asset.getName().equals("myFile2") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("myFile3") && asset.isFile())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
    }
    
    /*
    // list changed (in this test, new) assets in two different user sandboxes compared to each other - without locking
    public void testListNewItems3()
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-listNewItems2", TEST_WEBPROJ_NAME+" listNewItems2", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        String wpStoreId = wpInfo.getStoreId();

        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        SandboxInfo sbInfo1 = sbService.getAuthorSandbox(wpStoreId);
        String sbStoreId = sbInfo1.getSandboxId();
        
        List<AssetInfo> assets = sbService.listChangedAll(sbStoreId, true);
        assertEquals(0, assets.size());
      
        String authorSandboxRootPath = sbStoreId + AVM_STORE_SEPARATOR + sbInfo1.getSandboxRootPath();

        avmNonLockingAwareService.createFile(authorSandboxRootPath, "myFile1");
        
        assets = sbService.listChangedAll(sbStoreId, false);
        assertEquals(1, assets.size());
        assertEquals("myFile1", assets.get(0).getName());
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        SandboxInfo sbInfo2 = sbService.getAuthorSandbox(wpStoreId);
        sbStoreId = sbInfo2.getSandboxId();
        
        assets = sbService.listChangedAll(sbStoreId, true);
        assertEquals(0, assets.size());
      
        authorSandboxRootPath = sbStoreId + AVM_STORE_SEPARATOR + sbInfo2.getSandboxRootPath();

        avmNonLockingAwareService.createFile(authorSandboxRootPath, "myFile1"); // allowed, since using base (non-locking-aware) AVM service
        avmNonLockingAwareService.createFile(authorSandboxRootPath, "myFile2");
        avmNonLockingAwareService.createFile(authorSandboxRootPath, "myFile3");
        
        assets = sbService.listChangedAll(sbStoreId, false);
        assertEquals(3, assets.size());

        for (AssetInfo asset : assets)
        {
            if (asset.getName().equals("myFile1") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("myFile2") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("myFile3") && asset.isFile())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
        
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
        
        sbInfo1 = sbService.getAuthorSandbox(wpStoreId, USER_ONE);
        sbInfo2 = sbService.getAuthorSandbox(wpStoreId, USER_TWO);
        
        assets = sbService.listChanged(sbInfo1.getSandboxId(), sbInfo1.getSandboxRootPath(), sbInfo2.getSandboxId(), sbInfo2.getSandboxRootPath(), false);
        assertEquals(1, assets.size());
        assertEquals("myFile1", assets.get(0).getName());
        
        assets = sbService.listChanged(sbInfo2.getSandboxId(), sbInfo1.getSandboxRootPath(), sbInfo1.getSandboxId(), sbInfo2.getSandboxRootPath(), false);
        assertEquals(3, assets.size());
        
        for (AssetInfo asset : assets)
        {
            if (asset.getName().equals("myFile1") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("myFile2") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("myFile3") && asset.isFile())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
    }
    */
    
    // submit new assets in user sandbox to staging sandbox
    public void testSubmitNewItems1()
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-submitNewItems1", TEST_WEBPROJ_NAME+" submitNewItems1", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        String wpStoreId = wpInfo.getStoreId();
        String webApp = wpInfo.getDefaultWebApp();
        String stagingSandboxId = wpInfo.getStagingStoreName();
        
        // Invite web user
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        List<AssetInfo> assets = sbService.listChangedAll(authorSandboxId, true);
        assertEquals(0, assets.size());
      
        String authorSandboxPath = sbInfo.getSandboxRootPath() + "/" + webApp;
        
        assetService.createFile(authorSandboxId, authorSandboxPath, "myFile1", null);
        assetService.createFolder(authorSandboxId, authorSandboxPath, "myDir1", null);
        assetService.createFile(authorSandboxId, authorSandboxPath+"/myDir1", "myFile2", null);
        assetService.createFolder(authorSandboxId, authorSandboxPath+"/myDir1", "myDir2", null);
        assetService.createFile(authorSandboxId, authorSandboxPath+"/myDir1/myDir2", "myFile3", null);
        assetService.createFile(authorSandboxId, authorSandboxPath+"/myDir1/myDir2", "myFile4", null);
        assetService.createFolder(authorSandboxId, authorSandboxPath+"/myDir1", "myDir3", null);
        
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(2, assets.size()); // new dir with new dirs/files is returned as single change
        
        // check staging before
        String stagingSandboxPath = sbInfo.getSandboxRootPath() + "/" + webApp;
        assertEquals(0, assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false).size());
        
        // submit (new assets) !
        sbService.submitWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(0, assets.size());
        
        // check staging after
        List<AssetInfo> listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(2, listing.size());
        
        for (AssetInfo asset : listing)
        {
            if (asset.getName().equals("myFile1") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("myDir1") && asset.isFolder())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
    }
    
    // submit changed assets in user sandbox to staging sandbox
    public void testSubmitChangedAssets1() throws IOException
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-submitChangedAssets1", TEST_WEBPROJ_NAME+" submitChangedAssets1", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreId = wpInfo.getStoreId();
        final String webApp = wpInfo.getDefaultWebApp();
        final String stagingSandboxId = wpInfo.getStagingStoreName();
        
        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_PUBLISHER, true);
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        List<AssetInfo> assets = sbService.listChangedAll(authorSandboxId, true);
        assertEquals(0, assets.size());
      
        //String authorSandboxWebppPath = authorSandboxId + AVM_STORE_SEPARATOR + sbInfo.getSandboxRootPath() + "/" + webApp;
        
        final String MYFILE1 = "This is myFile1";
        ContentWriter writer = assetService.createFileWebApp(authorSandboxId, webApp, "/", "myFile1");
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE1);
        
        assetService.createFolderWebApp(authorSandboxId, webApp, "/", "myDir1");
        
        final String MYFILE2 = "This is myFile2";
        writer = assetService.createFileWebApp(authorSandboxId, webApp, "/myDir1", "myFile2");
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE2);
                
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(2, assets.size());
        
        // check staging before
        String stagingSandboxPath = sbInfo.getSandboxRootPath() + "/" + webApp;
        assertEquals(0, assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false).size());
        
        // submit (new assets) !
        sbService.submitWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(0, assets.size());
        
        // check staging after
        List<AssetInfo> listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(2, listing.size());
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        sbInfo = sbService.getAuthorSandbox(wpStoreId);
        authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        assets = sbService.listChangedAll(authorSandboxId, true);
        assertEquals(0, assets.size());
        
        final String MYFILE1_MODIFIED = "This is myFile1 ... modified by "+USER_TWO;
        
        writer = assetService.getContentWriter(assetService.getAssetWebApp(authorSandboxId, webApp, "/myFile1"));
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE1_MODIFIED);

        final String MYFILE2_MODIFIED = "This is myFile2 ... modified by "+USER_TWO;
        writer = assetService.getContentWriter(assetService.getAssetWebApp(authorSandboxId, webApp, "/myDir1/myFile2"));
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE2_MODIFIED);
                
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(2, assets.size());
        
        // check staging before
        stagingSandboxPath = sbInfo.getSandboxRootPath() + "/" + webApp;
        
        ContentReader reader = assetService.getContentReader(assetService.getAsset(stagingSandboxId, -1, stagingSandboxPath+"/myFile1", false));
        InputStream in = reader.getContentInputStream();
        byte[] buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE1, new String(buff, 0, MYFILE1.length())); // assumes 1byte=1char
        
        reader = assetService.getContentReader(assetService.getAsset(stagingSandboxId, -1, stagingSandboxPath+"/myDir1/myFile2", false));
        in = reader.getContentInputStream();
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE2, new String(buff, 0, MYFILE2.length()));
        
        // submit (modified assets) !
        sbService.submitWebApp(authorSandboxId, webApp, null, null);
        
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(0, assets.size());
        
        // check staging after
        reader = assetService.getContentReader(assetService.getAsset(stagingSandboxId, -1, stagingSandboxPath+"/myFile1", false));
        in = reader.getContentInputStream();
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE1_MODIFIED, new String(buff, 0, MYFILE1_MODIFIED.length()));
        
        reader = assetService.getContentReader(assetService.getAsset(stagingSandboxId, -1, stagingSandboxPath+"/myDir1/myFile2", false));
        in = reader.getContentInputStream();
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE2_MODIFIED, new String(buff, 0, MYFILE1_MODIFIED.length()));
    }
    
    // submit deleted assets in user sandbox to staging sandbox
    public void testSubmitDeletedItems1() throws IOException
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-submitDeletedItems1", TEST_WEBPROJ_NAME+" submitDeletedItems1", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreId = wpInfo.getStoreId();
        final String webApp = wpInfo.getDefaultWebApp();
        final String stagingSandboxId = wpInfo.getStagingStoreName();
        
        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_MANAGER, true); // note: publisher does not have permission to delete
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        List<AssetInfo> assets = sbService.listChangedAll(authorSandboxId, true);
        assertEquals(0, assets.size());
      
        String authorSandboxPath = sbInfo.getSandboxRootPath() + "/" + webApp;
        
        final String MYFILE1 = "This is myFile1";
        ContentWriter writer = assetService.createFile(authorSandboxId, authorSandboxPath, "myFile1", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE1);
        
        assetService.createFolder(authorSandboxId, authorSandboxPath, "myDir1", null);
        assetService.createFolder(authorSandboxId, authorSandboxPath+"/myDir1", "myDir2", null);
        
        final String MYFILE2 = "This is myFile2";
        writer = assetService.createFile(authorSandboxId, authorSandboxPath+"/myDir1", "myFile2", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE2);
                
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(2, assets.size());
        
        // check staging before
        String stagingSandboxPath = sbInfo.getSandboxRootPath() + "/" + webApp;
        assertEquals(0, assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false).size());
        
        // submit (new assets) !
        sbService.submitWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(0, assets.size());
        
        // check staging after
        List<AssetInfo> listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(2, listing.size());
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        sbInfo = sbService.getAuthorSandbox(wpStoreId);
        authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        assets = sbService.listChangedAll(authorSandboxId, true);
        assertEquals(0, assets.size());
      
        //authorSandboxWebppPath = authorSandboxId + AVM_STORE_SEPARATOR + sbInfo.getSandboxRootPath() + "/" + webApp;
        
        
        assetService.deleteAsset(assetService.getAssetWebApp(authorSandboxId, webApp, "myFile1"));
        assetService.deleteAsset(assetService.getAssetWebApp(authorSandboxId, webApp, "/myDir1/myDir2"));
                
        // do not list deleted
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(0, assets.size());
        
        // do list deleted
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, true);
        assertEquals(2, assets.size());
        
        // check staging before
        //stagingSandboxWebppPath = stagingSandboxId + AVM_STORE_SEPARATOR + sbInfo.getSandboxRootPath() + "/" + webApp;
        
        assertNotNull(assetService.getAssetWebApp(stagingSandboxId, webApp, "/myFile1"));
        assertNotNull(assetService.getAssetWebApp(stagingSandboxId, webApp, "/myDir1"));
        assertNotNull(assetService.getAssetWebApp(stagingSandboxId, webApp, "/myDir1/myDir2"));
        assertNotNull(assetService.getAssetWebApp(stagingSandboxId, webApp, "/myDir1/myFile2"));
        
        // submit (deleted assets) !
        sbService.submitWebApp(authorSandboxId, webApp, null, null);
        
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(0, assets.size());
        
        // check staging after
        assertNull(assetService.getAssetWebApp(stagingSandboxId, webApp, "/myFile1"));
        assertNull(assetService.getAssetWebApp(stagingSandboxId, webApp, "/myDir1/myDir2"));
        
        assertNotNull(assetService.getAssetWebApp(stagingSandboxId, webApp, "/myDir1"));
        assertNotNull(assetService.getAssetWebApp(stagingSandboxId, webApp, "/myDir1/myFile2"));
    }
    
    // revert all (changed) assets in user sandbox
    public void testRevertAll() throws IOException
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-revertChangedAssets", TEST_WEBPROJ_NAME+" revertChangedAssets", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreId = wpInfo.getStoreId();
        final String webApp = wpInfo.getDefaultWebApp();
        final String stagingSandboxId = wpInfo.getStagingStoreName();
        
        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        
        // TODO - pending fix for ETWOTWO-981
        //wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_PUBLISHER, true);
        
        wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_MANAGER, true);
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        List<AssetInfo> assets = sbService.listChangedAll(authorSandboxId, true);
        assertEquals(0, assets.size());
      
        String authorSandboxPath = sbInfo.getSandboxRootPath() + "/" + webApp;
        
        final String MYFILE1 = "This is myFile1";
        ContentWriter writer = assetService.createFile(authorSandboxId, authorSandboxPath, "myFile1", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE1);
        
        assetService.createFolder(authorSandboxId, authorSandboxPath, "myDir1", null);
        
        final String MYFILE2 = "This is myFile2";
        writer = assetService.createFile(authorSandboxId, authorSandboxPath+"/myDir1", "myFile2", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE2);
                
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(2, assets.size());
        
        // check staging before
        String stagingSandboxPath = sbInfo.getSandboxRootPath() + "/" + webApp;
        assertEquals(0, assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false).size());
        
        // submit (new assets) !
        sbService.submitWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(0, assets.size());
        
        // check staging after
        List<AssetInfo> listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(2, listing.size());
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        sbInfo = sbService.getAuthorSandbox(wpStoreId);
        authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        assets = sbService.listChangedAll(authorSandboxId, true);
        assertEquals(0, assets.size());
      
        //authorSandboxWebppPath = authorSandboxId + AVM_STORE_SEPARATOR + sbInfo.getSandboxRootPath() + "/" + webApp;
        
        final String MYFILE1_MODIFIED = "This is myFile1 ... modified by "+USER_TWO;
        writer = assetService.getContentWriter(assetService.getAssetWebApp(authorSandboxId, webApp, "/myFile1"));
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE1_MODIFIED);

        final String MYFILE2_MODIFIED = "This is myFile2 ... modified by "+USER_TWO;
        writer = assetService.getContentWriter(assetService.getAssetWebApp(authorSandboxId, webApp, "/myDir1/myFile2"));
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE2_MODIFIED);
                
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(2, assets.size());
        
        // check staging before
        stagingSandboxPath = sbInfo.getSandboxRootPath() + "/" + webApp;
        
        ContentReader reader = assetService.getContentReader(assetService. getAsset(stagingSandboxId, -1, stagingSandboxPath+"/myFile1", false));
        InputStream in = reader.getContentInputStream();
        byte[] buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE1, new String(buff, 0, MYFILE1.length())); // assumes 1byte = 1char
        
        reader = assetService.getContentReader(assetService. getAsset(stagingSandboxId, -1, stagingSandboxPath+"/myDir1/myFile2", false));
        in = reader.getContentInputStream();
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE2, new String(buff, 0, MYFILE2.length()));
        
        // revert (modified assets) !
        sbService.revertWebApp(authorSandboxId, webApp);
        
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(0, assets.size());
        
        // check staging after
        reader = assetService.getContentReader(assetService.getAsset(stagingSandboxId, -1, stagingSandboxPath+"/myFile1", false));
        in = reader.getContentInputStream();
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE1, new String(buff, 0, MYFILE1.length()));
        
        reader = assetService.getContentReader(assetService.getAsset(stagingSandboxId, -1, stagingSandboxPath+"/myDir1/myFile2", false));
        in = reader.getContentInputStream();
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE2, new String(buff, 0, MYFILE2.length()));
    }
    
    public void testListSnapshots() throws IOException
    {
        Date fromDate = new Date();
        
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-listSnapshots", TEST_WEBPROJ_NAME+" listSnapshots", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreId = wpInfo.getStoreId();
        final String webApp = wpInfo.getDefaultWebApp();
        final String stagingSandboxId = wpInfo.getStagingStoreName();
        
        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, true);
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        List<AssetInfo> assets = sbService.listChangedAll(authorSandboxId, true);
        assertEquals(0, assets.size());
      
        assetService.createFolderWebApp(authorSandboxId, webApp, "/", "myDir1");
        assetService.createFolderWebApp(authorSandboxId, webApp, "/", "myDir2");
        assetService.createFolderWebApp(authorSandboxId, webApp, "/", "myDir3");
        
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(3, assets.size());
        
        // check staging before
        String stagingSandboxPath = sbInfo.getSandboxRootPath() + "/" + webApp;
        assertEquals(0, assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false).size());
        
        List<SandboxVersion> sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(0, sbVersions.size());
        
        // submit (new assets) !
        sbService.submitWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(0, assets.size());
        
        // check staging after
        List<AssetInfo> listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(3, listing.size());
        
        sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(1, sbVersions.size());
        
        // more changes ...
        assetService.createFolderWebApp(authorSandboxId, webApp, "/", "myDir4");

        // submit (new assets) !
        sbService.submitWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        // check staging after
        listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(4, listing.size());
        
        sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(2, sbVersions.size());
    }
   
    public void testRevertSnapshot() throws IOException
    {
        Date fromDate = new Date();
        
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-revertSnapshot", TEST_WEBPROJ_NAME+" revertSnapshot", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreId = wpInfo.getStoreId();
        final String webApp = wpInfo.getDefaultWebApp();
        final String stagingSandboxId = wpInfo.getStagingStoreName();
        
        // Start: Test ETWOTWO-817
        
        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, true);
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // Finish: Test ETWOTWO-817
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        List<AssetInfo> assets = sbService.listChangedAll(authorSandboxId, true);
        assertEquals(0, assets.size());
      
        String authorSandboxPath = sbInfo.getSandboxRootPath() + "/" + webApp;
        
        assetService.createFolder(authorSandboxId, authorSandboxPath, "myDir1", null);
        
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(1, assets.size());
        
        // check staging before
        String stagingSandboxPath = sbInfo.getSandboxRootPath() + "/" + webApp;
        assertEquals(0, assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false).size());
        
        List<SandboxVersion> sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(0, sbVersions.size());
        
        // submit (new assets) !
        sbService.submitWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(0, assets.size());
        
        // check staging after
        List<AssetInfo> listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(1, listing.size());
        for (AssetInfo asset : listing)
        {
            if (asset.getName().equals("myDir1") && asset.isFolder())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
        
        sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(1, sbVersions.size());
        
        // more changes ...
        assetService.createFolder(authorSandboxId, authorSandboxPath, "myDir2", null);

        // submit (new assets) !
        sbService.submitWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        // check staging after
        listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(2, listing.size());
        for (AssetInfo asset : listing)
        {
            if (asset.getName().equals("myDir1") && asset.isFolder())
            {
                continue;
            }
            else if (asset.getName().equals("myDir2") && asset.isFolder())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
        
        sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(2, sbVersions.size());
        
        // more changes ...
        assetService.createFolderWebApp(authorSandboxId, webApp, "/", "myDir3");

        // submit (new assets) !
        sbService.submitWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        // check staging after
        listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(3, listing.size());
        for (AssetInfo asset : listing)
        {
            if (asset.getName().equals("myDir1") && asset.isFolder())
            {
                continue;
            }
            else if (asset.getName().equals("myDir2") && asset.isFolder())
            {
                continue;
            }
            else if (asset.getName().equals("myDir3") && asset.isFolder())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
        
        sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(3, sbVersions.size());
        
        // revert to snapshot ...
        
        SandboxVersion version = sbVersions.get(1);
        int versionId = version.getVersion();
        
        sbService.revertSnapshot(stagingSandboxId, versionId);
        
        sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(4, sbVersions.size());
        
        // check staging after
        listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(2, listing.size());
        for (AssetInfo asset : listing)
        {
            if (asset.getName().equals("myDir1") && asset.isFolder())
            {
                continue;
            }
            else if (asset.getName().equals("myDir2") && asset.isFolder())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
    }
    
    public void testPseudoScaleTest()
    {
        long start = System.currentTimeMillis();
        
        long split = start;
        
        for (int i = 1; i <= SCALE_USERS; i++)
        {
            createUser(TEST_USER+"-"+i);
        }
        
        System.out.println("testPseudoScaleTest: created "+SCALE_USERS+" users in "+(System.currentTimeMillis()-split)+" msecs");
        
        split = System.currentTimeMillis();
        
        for (int i = 1; i <= SCALE_WEBPROJECTS; i++)
        {
            wpService.createWebProject(TEST_WEBPROJ_DNS+"-"+i, TEST_WEBPROJ_NAME+"-"+i, TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION); // ignore return
        }
        
        System.out.println("testPseudoScaleTest: created "+SCALE_WEBPROJECTS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");
        
        split = System.currentTimeMillis();
        
        for (int i = 1; i <= SCALE_WEBPROJECTS; i++)
        {
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_WEBPROJ_DNS+"-"+i);
            Map<String, String> userRoles = new HashMap<String, String>(SCALE_USERS);
            for (int j = 1; j <= SCALE_USERS; j++)
            {
                userRoles.put(TEST_USER+"-"+j, WCMUtil.ROLE_CONTENT_MANAGER);
            }
            wpService.inviteWebUsersGroups(wpInfo.getNodeRef(), userRoles, true);
        }
        
        System.out.println("testPseudoScaleTest: invited "+SCALE_USERS+" content managers (and created user sandboxes) to each of "+SCALE_WEBPROJECTS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");
        
        split = System.currentTimeMillis();
        
        for (int i = 1; i <= SCALE_WEBPROJECTS; i++)
        {
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_WEBPROJ_DNS+"-"+i);
            assertEquals(SCALE_USERS+2, sbService.listSandboxes(wpInfo.getStoreId()).size()); // including staging sandbox and admin sandbox (web project creator)
        }

        System.out.println("testPseudoScaleTest: list sandboxes for admin for each of "+SCALE_WEBPROJECTS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");

        split = System.currentTimeMillis();
        
        for (int i = 1; i <= SCALE_WEBPROJECTS; i++)
        {
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_WEBPROJ_DNS+"-"+i);
            assertEquals(SCALE_USERS+1, wpService.listWebUsers(wpInfo.getStoreId()).size()); // including admin user (web project creator)
        }

        System.out.println("testPseudoScaleTest: list web users for admin for each of "+SCALE_WEBPROJECTS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");

        split = System.currentTimeMillis();

        for (int i = 1; i <= SCALE_WEBPROJECTS; i++)
        {
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_WEBPROJ_DNS+"-"+i);
            
            for (int j = 1; j <= SCALE_USERS; j++)
            {
                AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER+"-"+j);
                assertEquals(SCALE_USERS+2, sbService.listSandboxes(wpInfo.getStoreId()).size()); // including staging sandbox and admin sandbox (web project creator)
            }
            AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
        } 
        
        System.out.println("testPseudoScaleTest: list sandboxes for "+SCALE_USERS+" content managers for each of "+SCALE_WEBPROJECTS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");
        
        split = System.currentTimeMillis();

        for (int i = 1; i <= SCALE_WEBPROJECTS; i++)
        {
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_WEBPROJ_DNS+"-"+i);
            
            for (int j = 1; j <= SCALE_USERS; j++)
            {
                AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER+"-"+j);
                assertEquals(SCALE_USERS+1, wpService.listWebUsers(wpInfo.getStoreId()).size()); // including admin user (web project creator)
            }
            AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
        }
        
        System.out.println("testPseudoScaleTest: list web users for "+SCALE_USERS+" content managers for each of "+SCALE_WEBPROJECTS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");
      
        split = System.currentTimeMillis();
        
        for (int i = 1; i <= SCALE_WEBPROJECTS; i++)
        {
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_WEBPROJ_DNS+"-"+i);
            
            for (int j = 1; j <= SCALE_USERS; j++)
            {
                SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId(), TEST_USER+"-"+j);
                sbService.deleteSandbox(sbInfo.getSandboxId());
            }
        }
        
        System.out.println("testPseudoScaleTest: deleted "+SCALE_USERS+" author sandboxes for each of "+SCALE_WEBPROJECTS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");

        split = System.currentTimeMillis();
        
        for (int i = 1; i <= SCALE_WEBPROJECTS; i++)
        {
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_WEBPROJ_DNS+"-"+i);
            wpService.deleteWebProject(wpInfo.getNodeRef());
        }
        
        System.out.println("testPseudoScaleTest: deleted "+SCALE_WEBPROJECTS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");

        split = System.currentTimeMillis();
        
        for (int i = 1; i <= SCALE_USERS; i++)
        {
            deleteUser(TEST_USER+"-"+i);
        }
        
        System.out.println("testPseudoScaleTest: deleted "+SCALE_USERS+" users in "+(System.currentTimeMillis()-split)+" msecs");
    }
    
        
    /*
    // == Test the JavaScript API ==
    
    public void testJSAPI() throws Exception
    {
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/wcm/script/test_sandboxService.js");
        scriptService.executeScript(location, new HashMap<String, Object>(0));
    }
    */
}
