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
package org.alfresco.wcm.sandbox;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.config.JNDIConstants;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.AVMNodeType;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.wcm.AbstractWCMServiceImplTest;
import org.alfresco.wcm.actions.WCMSandboxRevertSnapshotAction;
import org.alfresco.wcm.actions.WCMSandboxSubmitAction;
import org.alfresco.wcm.actions.WCMSandboxUndoAction;
import org.alfresco.wcm.asset.AssetInfo;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Sandbox Service implementation unit test
 * 
 * @author janv
 */
public class SandboxServiceImplTest extends AbstractWCMServiceImplTest
{
    private static Log logger = LogFactory.getLog(SandboxServiceImplTest.class);
    
    // base sandbox
    private static final String TEST_SANDBOX = TEST_WEBPROJ_DNS+"-sandbox";
    
    private static final int SCALE_USERS = 5;
    private static final int SCALE_WEBPROJECTS = 2;
    
    //
    // services
    //
    
    private AVMService avmService; // non-locking-aware
    
    //private AVMService avmLockingAwareService;
    //private AVMService avmNonLockingAwareService;

    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        // Get the required services
        avmService = (AVMService)ctx.getBean("AVMService");
        
        // WCM locking
        //avmLockingAwareService = (AVMService)ctx.getBean("AVMLockingAwareService");
        
        // without WCM locking
        //avmNonLockingAwareService = (AVMService)ctx.getBean("AVMService");
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        if (CLEAN)
        {
            // Switch back to Admin
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
            
            deleteUser(USER_ONE);
            deleteUser(USER_TWO);
            deleteUser(USER_THREE);
            deleteUser(USER_FOUR);
        }
        
        super.tearDown();
    }
    
    public void testSimple()
    {
        int storeCnt = avmService.getStores().size();
        
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-sandboxSimple", TEST_WEBPROJ_NAME+"-sandboxSimple", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
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
        WebProjectInfo wpInfo1 = wpService.createWebProject(TEST_SANDBOX+"-create-author", TEST_WEBPROJ_NAME+"-author", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        
        String expectedUserSandboxId = TEST_SANDBOX+"-create-author" + "--" + AuthenticationUtil.getAdminUserName();
        
        SandboxInfo sbInfo1 = sbService.getAuthorSandbox(wpInfo1.getStoreId());
        checkSandboxInfo(sbInfo1, expectedUserSandboxId, AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getAdminUserName(), expectedUserSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
        
        sbInfo1 = sbService.getAuthorSandbox(wpInfo1.getStoreId(), USER_ONE);
        assertNull(sbInfo1);
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        sbInfo1 = sbService.getAuthorSandbox(wpInfo1.getStoreId());
        assertNull(sbInfo1);
        
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // Invite web user
        wpService.inviteWebUser(wpInfo1.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER);
        
        // Create author sandbox for user one - admin is the creator
        sbInfo1 = sbService.createAuthorSandbox(wpInfo1.getStoreId(), USER_ONE);

        expectedUserSandboxId = TEST_SANDBOX+"-create-author" + "--" + USER_ONE;
        
        sbInfo1 = sbService.getAuthorSandbox(wpInfo1.getStoreId(), USER_ONE);
        checkSandboxInfo(sbInfo1, expectedUserSandboxId, USER_ONE, AuthenticationUtil.getAdminUserName(), expectedUserSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // Get author sandbox
        sbInfo1 = sbService.getAuthorSandbox(wpInfo1.getStoreId());
        checkSandboxInfo(sbInfo1, expectedUserSandboxId, USER_ONE, AuthenticationUtil.getAdminUserName(), expectedUserSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
        
        String userSandboxId = sbInfo1.getSandboxId();
        
        // Get (author) sandbox
        sbInfo1 = sbService.getSandbox(userSandboxId);
        checkSandboxInfo(sbInfo1, expectedUserSandboxId, USER_ONE, AuthenticationUtil.getAdminUserName(), expectedUserSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
        
        // Should return same as before
        sbInfo1 = sbService.createAuthorSandbox(wpInfo1.getStoreId());
        checkSandboxInfo(sbInfo1, expectedUserSandboxId, USER_ONE, AuthenticationUtil.getAdminUserName(), expectedUserSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
        
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
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
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
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-list", TEST_WEBPROJ_NAME+" list", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        String wpStoreId = wpInfo.getStoreId();
        
        // Create ANOther web project
        WebProjectInfo wpAnoInfo = wpService.createWebProject(TEST_SANDBOX+"-list ano", TEST_WEBPROJ_NAME+" list ano", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        String wpStoreAnoId = wpAnoInfo.getStoreId();
        
        List<SandboxInfo> sbInfos = sbService.listSandboxes(wpStoreId);
        assertEquals(2, sbInfos.size()); // staging sandbox, author sandbox (for admin)
        
        assertEquals(2, sbService.listSandboxes(wpStoreAnoId).size());
        
        String expectedUserSandboxId = TEST_SANDBOX+"-list" + "--" + AuthenticationUtil.getAdminUserName();
        
        // Do detailed check of the sandbox info objects
        for (SandboxInfo sbInfo : sbInfos)
        {
            QName sbType = sbInfo.getSandboxType();
            
            if (sbType.equals(SandboxConstants.PROP_SANDBOX_STAGING_MAIN) == true)
            {
                checkSandboxInfo(sbInfo, TEST_SANDBOX+"-list", TEST_SANDBOX+"-list", AuthenticationUtil.getAdminUserName(), TEST_SANDBOX+"-list", SandboxConstants.PROP_SANDBOX_STAGING_MAIN);
            }
            else if (sbType.equals(SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN) == true)
            {
                checkSandboxInfo(sbInfo, expectedUserSandboxId, AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getAdminUserName(), expectedUserSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
            }
            else
            {
                fail("The sandbox store id " + sbInfo.getSandboxId() + " is not recognised");
            }
        }
        
        // test roles
        
        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, true);
        wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_PUBLISHER, true);
        wpService.inviteWebUser(wpStoreId, USER_THREE, WCMUtil.ROLE_CONTENT_REVIEWER, true);
        wpService.inviteWebUser(wpStoreId, USER_FOUR, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        
        // admin can list all sandboxes
        sbInfos = sbService.listSandboxes(wpInfo.getStoreId());
        assertEquals(6, sbInfos.size());
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // content manager can list all sandboxes
        sbInfos = sbService.listSandboxes(wpInfo.getStoreId());
        assertEquals(6, sbInfos.size());
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);

        // Content publisher - can list all sandboxes
        sbInfos = sbService.listSandboxes(wpInfo.getStoreId());
        assertEquals(6, sbInfos.size());
        
        // Switch to USER_THREE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_THREE);
        
        // Content reviewer - can only list own sandbox and staging
        sbInfos = sbService.listSandboxes(wpInfo.getStoreId());
        assertEquals(2, sbInfos.size());
               
        // Switch to USER_FOUR
        AuthenticationUtil.setFullyAuthenticatedUser(USER_FOUR);
        
        // Content contributor - can only list own sandbox and staging
        sbInfos = sbService.listSandboxes(wpInfo.getStoreId());
        assertEquals(2, sbInfos.size());
    }
    
    public void testGetSandbox()
    {
        // Get a sandbox that isn't there
        SandboxInfo sbInfo = sbService.getSandbox(TEST_SANDBOX+"-get");
        assertNull(sbInfo);
        
        // Create web project - implicitly creates staging sandbox and also admin sandbox (author sandbox for web project creator)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-get", TEST_WEBPROJ_NAME+" get", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        String wpStoreId = wpInfo.getStoreId();
        
        // Get staging sandbox
        sbInfo = sbService.getStagingSandbox(wpInfo.getStoreId());
        checkSandboxInfo(sbInfo, TEST_SANDBOX+"-get", TEST_SANDBOX+"-get", AuthenticationUtil.getAdminUserName(), TEST_SANDBOX+"-get", SandboxConstants.PROP_SANDBOX_STAGING_MAIN);
        
        // Get (staging) sandbox
        String stagingSandboxId = wpInfo.getStagingStoreName();
        sbInfo = sbService.getSandbox(stagingSandboxId);
        checkSandboxInfo(sbInfo, TEST_SANDBOX+"-get", TEST_SANDBOX+"-get", AuthenticationUtil.getAdminUserName(), TEST_SANDBOX+"-get", SandboxConstants.PROP_SANDBOX_STAGING_MAIN);

        // Get (author) sandbox
        sbInfo = sbService.getAuthorSandbox(wpStoreId);      
        sbInfo = sbService.getSandbox(sbInfo.getSandboxId());       
        String userSandboxId = TEST_SANDBOX+"-get" + "--" + AuthenticationUtil.getAdminUserName();
        checkSandboxInfo(sbInfo, userSandboxId, AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getAdminUserName(), userSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);

        // test roles
        
        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, true);
        wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_PUBLISHER, true);
        wpService.inviteWebUser(wpStoreId, USER_THREE, WCMUtil.ROLE_CONTENT_REVIEWER, true);
        wpService.inviteWebUser(wpStoreId, USER_FOUR, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        
        // admin can get any sandbox
        userSandboxId = TEST_SANDBOX+"-get" + "--" + USER_THREE;
        sbInfo = sbService.getSandbox(userSandboxId);
        checkSandboxInfo(sbInfo, userSandboxId, USER_THREE, AuthenticationUtil.getAdminUserName(), userSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // content manager can get any (author) sandbox
        userSandboxId = TEST_SANDBOX+"-get" + "--" + USER_THREE;
        sbInfo = sbService.getSandbox(userSandboxId);
        checkSandboxInfo(sbInfo, userSandboxId, USER_THREE, AuthenticationUtil.getAdminUserName(), userSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        // content publisher can get any (author) sandbox
        userSandboxId = TEST_SANDBOX+"-get" + "--" + USER_THREE;
        sbInfo = sbService.getSandbox(userSandboxId);
        checkSandboxInfo(sbInfo, userSandboxId, USER_THREE, AuthenticationUtil.getAdminUserName(), userSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
        
        // Switch to USER_THREE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_THREE);
        
        try
        {
            // Content reviewer - try to get another user's sandbox (-ve test)
            userSandboxId = TEST_SANDBOX+"-get" + "--" + USER_TWO;
            sbInfo = sbService.getSandbox(userSandboxId);
            fail("Shouldn't be able to get another author's sandbox");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Switch to USER_FOUR
        AuthenticationUtil.setFullyAuthenticatedUser(USER_FOUR);
        
        try
        {
            // Content contributor - try to get another user's sandbox (-ve test)
            userSandboxId = TEST_SANDBOX+"-get" + "--" + USER_THREE;
            sbInfo = sbService.getSandbox(userSandboxId);
            fail("Shouldn't be able to get another author's sandbox");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
    }
    
    public void testIsSandboxType()
    {
        // Get a sandbox that isn't there
        SandboxInfo sbInfo = sbService.getSandbox(TEST_SANDBOX+"-is");
        assertNull(sbInfo);
        
        // Create web project - implicitly creates staging sandbox and also admin sandbox (author sandbox for web project creator)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-is", TEST_WEBPROJ_NAME+" is", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);

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
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-delete", TEST_WEBPROJ_NAME+" delete", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        String wpStoreId = wpInfo.getStoreId();
        
        // Create ANOther web project
        WebProjectInfo wpAnoInfo = wpService.createWebProject(TEST_SANDBOX+"-delete ano", TEST_WEBPROJ_NAME+" delete ano", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        String wpStoreAnoId = wpAnoInfo.getStoreId();
        
        assertEquals(2, sbService.listSandboxes(wpStoreId).size());
        assertEquals(2, sbService.listSandboxes(wpStoreAnoId).size());
        
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
        sbInfo = sbService.getAuthorSandbox(wpStoreId);
        sbService.deleteSandbox(sbInfo.getSandboxId());
        
        assertEquals(1, sbService.listSandboxes(wpStoreId).size());
        assertEquals(2, sbService.listSandboxes(wpStoreAnoId).size());
        
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
        
        assertEquals(4, sbService.listSandboxes(wpStoreId).size());
        
        sbInfo = sbService.getAuthorSandbox(wpStoreId);
        assertNotNull(sbInfo);
        
        String defaultWebApp = wpInfo.getDefaultWebApp();
        String authorSandboxId = sbInfo.getSandboxId();
        String authorSandboxPath = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        for (int i = 1; i <= 10; i++)
        {
            assetService.createFile(authorSandboxId, authorSandboxPath, "myFile-"+i, null);
            
            String relPath = authorSandboxPath + "/" + "myFile-"+i;
            assertEquals(USER_TWO, avmLockingService.getLockOwner(wpStoreId, relPath));
        }
        
        // can delete own sandbox
        sbService.deleteSandbox(sbInfo.getSandboxId());
        assertNull(sbService.getSandbox(sbInfo.getSandboxId()));
        
        // Check locks have been removed
        for (int i = 1; i <= 10; i++)
        {
            String relPath = authorSandboxPath + "/" + "myFile-"+i;
            assertNull("Lock still exists: "+relPath, avmLockingService.getLockOwner(wpStoreId, relPath));
        }
        
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
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-listNewItems1", TEST_WEBPROJ_NAME+" listNewItems1", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        String wpStoreId = wpInfo.getStoreId();
        
        assertEquals(2, sbService.listSandboxes(wpStoreId).size());
        
        // add web app (in addition to default ROOT web app)
        String myWebApp = "myWebApp";
        wpService.createWebApp(wpStoreId, myWebApp, "this is my web app");
        
        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR);
        SandboxInfo sbInfo = sbService.createAuthorSandbox(wpStoreId, USER_ONE);
        String userOneSandboxId = sbInfo.getSandboxId();
        
        wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_PUBLISHER);
        sbInfo = sbService.createAuthorSandbox(wpStoreId, USER_TWO);
        String userTwoSandboxId = sbInfo.getSandboxId();
        
        wpService.inviteWebUser(wpStoreId, USER_THREE, WCMUtil.ROLE_CONTENT_MANAGER);
        sbService.createAuthorSandbox(wpStoreId, USER_THREE);
        
        wpService.inviteWebUser(wpStoreId, USER_FOUR, WCMUtil.ROLE_CONTENT_REVIEWER, true);
   
        assertEquals(6, sbService.listSandboxes(wpStoreId).size());
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        assertEquals(2, sbService.listSandboxes(wpStoreId).size());
        
        sbInfo = sbService.getAuthorSandbox(wpStoreId);
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
        
        // test roles
        
        // Switch to AuthenticationUtil.getAdminUserName()
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        assertEquals(6, sbService.listSandboxes(wpStoreId).size());
        
        // admin (Content Manager) should be able to list another user's changes
        assets = sbService.listChangedAll(userOneSandboxId, true);
        assertEquals(2, assets.size());
        
        // Switch to USER_THREE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_THREE);
        
        assertEquals(6, sbService.listSandboxes(wpStoreId).size());
        
        // Content Manager should be able to list another user's changes
        assets = sbService.listChangedAll(userOneSandboxId, true);
        assertEquals(2, assets.size());
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        try
        {
            // Content contributor - try to list changes in another user's sandbox (-ve test)
            assets = sbService.listChangedAll(userTwoSandboxId, true);
            fail("Shouldn't be able to list another author's sandbox changes");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        // Content Publisher should be able to list another user's changes
        assets = sbService.listChangedAll(userOneSandboxId, true);
        assertEquals(2, assets.size());
        
        // Switch to USER_FOUR
        AuthenticationUtil.setFullyAuthenticatedUser(USER_FOUR);
        
        try
        {
            // Content reviewer - try to list changes in another user's sandbox (-ve test)
            assets = sbService.listChangedAll(userOneSandboxId, true);
            fail("Shouldn't be able to list another author's sandbox changes");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
    }
    
    // list changed (in this test, new) assets in two different user sandboxes compared to each other
    public void testListNewItems2()
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-listNewItems2", TEST_WEBPROJ_NAME+" listNewItems2", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
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
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
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
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-listNewItems2", TEST_WEBPROJ_NAME+" listNewItems2", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
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
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
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
    public void testSubmitNewItems1() throws InterruptedException
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-submitNewItems1", TEST_WEBPROJ_NAME+" submitNewItems1", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        String wpStoreId = wpInfo.getStoreId();
        String webApp = wpInfo.getDefaultWebApp();
        String stagingSandboxId = wpInfo.getStagingStoreName();
        
        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_PUBLISHER, true);
        wpService.inviteWebUser(wpStoreId, USER_THREE, WCMUtil.ROLE_CONTENT_MANAGER, true);
        wpService.inviteWebUser(wpStoreId, USER_FOUR, WCMUtil.ROLE_CONTENT_REVIEWER, true);
        
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
        
        pollForSnapshotCount(stagingSandboxId, 1);
        
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
        
        // test roles
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // admin (content manager) can submit any sandbox
        String userSandboxId = wpStoreId + "--" + USER_THREE;
        sbService.submitAll(userSandboxId, "my submit", null);
        
        // Switch to USER_THREE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_THREE);
        
        // content manager can submit any (author) sandbox
        userSandboxId = wpStoreId + "--" + USER_ONE;
        sbService.submitAll(userSandboxId, "my submit", null);
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        try
        {
            // Content contributor - try to submit another user's sandbox (-ve test)
            userSandboxId = wpStoreId + "--" + USER_THREE;
            List<AssetInfo> noAssets = new ArrayList<AssetInfo>(0);
            sbService.submitListAssets(userSandboxId, noAssets, "my submit", null);
            fail("Shouldn't be able to submit another author's sandbox");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        // Content publisher - can submit other sandboxes (eg. submit all)
        userSandboxId = wpStoreId + "--" + USER_ONE;
        sbService.submitAll(userSandboxId, "my submit", null);
        
        // Switch to USER_FOUR
        AuthenticationUtil.setFullyAuthenticatedUser(USER_FOUR);
        
        try
        {
            // Content reviewer - try to submit another user's sandbox (-ve test)
            userSandboxId = TEST_SANDBOX+"-get" + "--" + USER_THREE;
            sbService.submitAll(userSandboxId, "my submit", null);
            fail("Shouldn't be able to submit another author's sandbox");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
    }
    
    // submit changed assets in user sandbox to staging sandbox
    public void xtestSubmitChangedAssets1() throws IOException, InterruptedException
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-submitChangedAssets1", TEST_WEBPROJ_NAME+" submitChangedAssets1", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
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
        
        pollForSnapshotCount(stagingSandboxId, 1);
        
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
        sbService.submitWebApp(authorSandboxId, webApp, "my label", null);
        
        pollForSnapshotCount(stagingSandboxId, 2);
        
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
    
    // submit "all" changed assets in user sandbox to staging sandbox (not using default webapp)
    public void testSubmitChangedAssets2() throws IOException, InterruptedException
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-submitChangedAssets1", TEST_WEBPROJ_NAME+" submitChangedAssets1", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreId = wpInfo.getStoreId();
        final String stagingSandboxId = wpInfo.getStagingStoreName();
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String authorSandboxId = sbInfo.getSandboxId();
        
        String rootPath = sbInfo.getSandboxRootPath(); // currently /www/avm_webapps
        
        // no changes yet
        List<AssetInfo> assets = sbService.listChangedAll(authorSandboxId, true);
        assertEquals(0, assets.size());
        
        assetService.createFolder(authorSandboxId, rootPath, "a", null);
        assetService.createFolder(authorSandboxId, rootPath+"/a", "b", null);
        assetService.createFolder(authorSandboxId, rootPath+"/a/b", "c", null);

        final String MYFILE1 = "This is foo";
        ContentWriter writer = assetService.createFile(authorSandboxId, rootPath+"/a/b/c", "foo", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE1);
        
        final String MYFILE2 = "This is bar";
        writer = assetService.createFile(authorSandboxId, rootPath+"/a/b/c", "bar", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE2);
                
        assets = sbService.listChangedAll(authorSandboxId, true);
        assertEquals(1, assets.size());
        
        // check staging before
        assertEquals(1, assetService.listAssets(stagingSandboxId, -1, rootPath, false).size()); // note: currently includes default webapp ('ROOT')
        
        // submit (new assets) !
        sbService.submitAll(authorSandboxId, "a submit label", "a submit comment");
        
        pollForSnapshotCount(stagingSandboxId, 1);
        
        // check staging after
        List<AssetInfo> listing = assetService.listAssets(stagingSandboxId, -1, rootPath, false);
        assertEquals(2, listing.size()); // 'a' and 'ROOT'
        
        // no changes in sandbox
        assets = sbService.listChangedAll(authorSandboxId, true);
        assertEquals(0, assets.size());
        
        final String MYFILE3 = "This is figs";
        writer = assetService.createFile(authorSandboxId, rootPath, "figs", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE3);
        
        final String MYFILE1_MODIFIED = "This is foo ... modified";
        writer = assetService.getContentWriter(assetService.getAsset(authorSandboxId, rootPath+"/a/b/c/foo"));
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE1_MODIFIED);
        
        assetService.deleteAsset(assetService.getAsset(authorSandboxId, rootPath+"/a/b/c/bar"));
                
        assets = sbService.listChangedAll(authorSandboxId, true);
        assertEquals(3, assets.size());
        
        // check staging before
        listing = assetService.listAssets(stagingSandboxId, -1, rootPath, false);
        assertEquals(2, listing.size());  // 'a' and 'ROOT'
        
        // submit all (modified assets) !
        sbService.submitAll(authorSandboxId, "my label", null);
        
        pollForSnapshotCount(stagingSandboxId, 2);
        
        assets = sbService.listChangedAll(authorSandboxId, true);
        assertEquals(0, assets.size());
        
        // check staging after
        listing = assetService.listAssets(stagingSandboxId, -1, rootPath, false);
        assertEquals(3, listing.size());  // 'figs', 'a' and 'ROOT'
    }
    
    // submit deleted assets in user sandbox to staging sandbox
    public void testSubmitDeletedItems1() throws IOException, InterruptedException
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-submitDeletedItems1", TEST_WEBPROJ_NAME+" submitDeletedItems1", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
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
        
        pollForSnapshotCount(stagingSandboxId, 1);
        
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
        sbService.submitWebApp(authorSandboxId, webApp, "my label", null);
        
        pollForSnapshotCount(stagingSandboxId, 2);
        
        assets = sbService.listChangedWebApp(authorSandboxId, webApp, false);
        assertEquals(0, assets.size());
        
        // check staging after
        assertNull(assetService.getAssetWebApp(stagingSandboxId, webApp, "/myFile1"));
        assertNull(assetService.getAssetWebApp(stagingSandboxId, webApp, "/myDir1/myDir2"));
        
        assertNotNull(assetService.getAssetWebApp(stagingSandboxId, webApp, "/myDir1"));
        assertNotNull(assetService.getAssetWebApp(stagingSandboxId, webApp, "/myDir1/myFile2"));
    }
    
    public void testSubmitDeletedItemsWithLD() throws IOException, InterruptedException
    {
        // Create Web Project A
        
        WebProjectInfo wpInfoA = wpService.createWebProject(TEST_SANDBOX+"-A", TEST_WEBPROJ_NAME+" A", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreIdA = wpInfoA.getStoreId();
        final String webAppA = wpInfoA.getDefaultWebApp();
        final String stagingSandboxIdA = wpInfoA.getStagingStoreName();
        
        SandboxInfo sbInfoA = sbService.getAuthorSandbox(wpStoreIdA);
        String authorSandboxIdA = sbInfoA.getSandboxId();
        
        // no assets
        String stagingSandboxPathA = sbInfoA.getSandboxRootPath() + "/" + webAppA;
        assertEquals(0, assetService.listAssets(stagingSandboxIdA, -1, stagingSandboxPathA, false).size());
        
        // no changes yet
        List<AssetInfo> assets = sbService.listChangedAll(authorSandboxIdA, true);
        assertEquals(0, assets.size());
        
        String authorSandboxPathA = sbInfoA.getSandboxRootPath() + "/" + webAppA;
        
        assetService.createFolder(authorSandboxIdA, authorSandboxPathA, "test", null);
        
        final String MYFILE = "This is testfile.txt in AAA";
        ContentWriter writer = assetService.createFile(authorSandboxIdA, authorSandboxPathA+"/test", "testfile.txt", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE);
        
        assertEquals(1, assetService.listAssets(authorSandboxIdA, -1, authorSandboxPathA, false).size());
        assertEquals(1, assetService.listAssets(authorSandboxIdA, -1, authorSandboxPathA+"/test", false).size());
        
        assets = sbService.listChangedWebApp(authorSandboxIdA, webAppA, false);
        assertEquals(1, assets.size());
        
        // check staging before
        assertEquals(0, assetService.listAssets(stagingSandboxIdA, -1, stagingSandboxPathA, false).size());
        
        // submit (new assets) !
        sbService.submitWebApp(authorSandboxIdA, webAppA, "A1", "A1");
        
        pollForSnapshotCount(stagingSandboxIdA, 1);
        
        assets = sbService.listChangedWebApp(authorSandboxIdA, webAppA, false);
        assertEquals(0, assets.size());
        
        // check staging after
        List<AssetInfo> listing = assetService.listAssets(stagingSandboxIdA, -1, stagingSandboxPathA, false);
        assertEquals(1, listing.size());
        
        listing = assetService.listAssets(stagingSandboxIdA, -1, stagingSandboxPathA+"/test", false);
        assertEquals(1, listing.size());
        
        // Create Web Project B
        
        WebProjectInfo wpInfoB = wpService.createWebProject(TEST_SANDBOX+"-B", TEST_WEBPROJ_NAME+" B", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreIdB = wpInfoB.getStoreId();
        final String webAppB = wpInfoB.getDefaultWebApp();
        final String stagingSandboxIdB = wpInfoB.getStagingStoreName();
        
        SandboxInfo sbInfoB = sbService.getAuthorSandbox(wpStoreIdB);
        String authorSandboxIdB = sbInfoB.getSandboxId();
        
        // no assets
        String stagingSandboxPathB = sbInfoB.getSandboxRootPath() + "/" + webAppB;
        assertEquals(0, assetService.listAssets(stagingSandboxIdB, -1, stagingSandboxPathB, false).size());
        
        // no changes yet
        assets = sbService.listChangedAll(authorSandboxIdB, true);
        assertEquals(0, assets.size());
        
        // drop to AVM to create WCM layered folder (not supported via WCM services)
        avmService.createLayeredDirectory(wpStoreIdA+":"+stagingSandboxPathA+"/test", wpStoreIdB+":"+stagingSandboxPathB, "test");
        
        String authorSandboxPathB = sbInfoB.getSandboxRootPath() + "/" + webAppB;
        
        assertEquals(1, assetService.listAssets(authorSandboxIdB, -1, authorSandboxPathB, false).size());
        assertEquals(1, assetService.listAssets(authorSandboxIdB, -1, authorSandboxPathB+"/test", false).size());
        
        // modify file
        final String MYFILE_MODIFIED = "This is testfile.txt modified in BBB";
        
        writer = assetService.getContentWriter(assetService.getAssetWebApp(authorSandboxIdB, webAppB+"/test", "/testfile.txt"));
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE_MODIFIED);
        
        // submit (modified asset)
        sbService.submitWebApp(authorSandboxIdB, webAppB, "B1", "B1");
        
        pollForSnapshotCount(stagingSandboxIdB, 1);
        
        // Switch back to Web Project A
        
        // delete folder
        assetService.deleteAsset(assetService.getAssetWebApp(authorSandboxIdA, webAppA, "test"));
        
        // submit (deleted asset)
        sbService.submitWebApp(authorSandboxIdA, webAppA, "A2", "A2");
        
        pollForSnapshotCount(stagingSandboxIdA, 2);
        
        // Switch back to Web Project B
        
        // delete file
        assetService.deleteAsset(assetService.getAssetWebApp(authorSandboxIdB, webAppB+"/test", "testfile.txt"));
        
        // submit (deleted asset)
        // ETHREEOH_2581
        sbService.submitWebApp(authorSandboxIdB, webAppB, "B2", "B2");
        
        pollForSnapshotCount(stagingSandboxIdB, 2);
    }
    
    public void testSubmitUpdatedItemWithLF() throws IOException, InterruptedException
    {
        // Create Web Project A
        
        WebProjectInfo wpInfoA = wpService.createWebProject(TEST_SANDBOX+"-A", TEST_WEBPROJ_NAME+" A", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreIdA = wpInfoA.getStoreId();
        final String webAppA = wpInfoA.getDefaultWebApp();
        final String stagingSandboxIdA = wpInfoA.getStagingStoreName();
        
        SandboxInfo sbInfoA = sbService.getAuthorSandbox(wpStoreIdA);
        String authorSandboxIdA = sbInfoA.getSandboxId();
        
        // no assets
        String stagingSandboxPathA = sbInfoA.getSandboxRootPath() + "/" + webAppA;
        assertEquals(0, assetService.listAssets(stagingSandboxIdA, -1, stagingSandboxPathA, false).size());
        
        // no changes yet
        List<AssetInfo> assets = sbService.listChangedAll(authorSandboxIdA, true);
        assertEquals(0, assets.size());
        
        String authorSandboxPathA = sbInfoA.getSandboxRootPath() + "/" + webAppA;
        
        final String MYFILE = "This is testfile.txt in AAA";
        ContentWriter writer = assetService.createFile(authorSandboxIdA, authorSandboxPathA+"/", "testfile.txt", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE);
        
        assertEquals(1, assetService.listAssets(authorSandboxIdA, -1, authorSandboxPathA, false).size());
        
        assets = sbService.listChangedWebApp(authorSandboxIdA, webAppA, false);
        assertEquals(1, assets.size());
        
        // check staging before
        assertEquals(0, assetService.listAssets(stagingSandboxIdA, -1, stagingSandboxPathA, false).size());
        
        // submit (new assets) !
        sbService.submitWebApp(authorSandboxIdA, webAppA, "A1", "A1");
        
        // wait for submit to complete
        pollForSnapshotCount(stagingSandboxIdA, 1);
        
        assets = sbService.listChangedWebApp(authorSandboxIdA, webAppA, false);
        assertEquals(0, assets.size());
        
        // check staging after
        List<AssetInfo> listing = assetService.listAssets(stagingSandboxIdA, -1, stagingSandboxPathA, false);
        assertEquals(1, listing.size());
        
        // Create Web Project B
        
        WebProjectInfo wpInfoB = wpService.createWebProject(TEST_SANDBOX+"-B", TEST_WEBPROJ_NAME+" B", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreIdB = wpInfoB.getStoreId();
        final String webAppB = wpInfoB.getDefaultWebApp();
        final String stagingSandboxIdB = wpInfoB.getStagingStoreName();
        
        SandboxInfo sbInfoB = sbService.getAuthorSandbox(wpStoreIdB);
        String authorSandboxIdB = sbInfoB.getSandboxId();
        
        // no assets
        String stagingSandboxPathB = sbInfoB.getSandboxRootPath() + "/" + webAppB;
        assertEquals(0, assetService.listAssets(stagingSandboxIdB, -1, stagingSandboxPathB, false).size());
        
        // no changes yet
        assets = sbService.listChangedAll(authorSandboxIdB, true);
        assertEquals(0, assets.size());
        
        // drop to AVM to create WCM layered file (not supported via WCM services)
        avmService.createLayeredFile(wpStoreIdA+":"+stagingSandboxPathA+"/testfile.txt", wpStoreIdB+":"+stagingSandboxPathB, "testfile.txt");
        
        String authorSandboxPathB = sbInfoB.getSandboxRootPath() + "/" + webAppB;
        
        assertEquals(1, assetService.listAssets(authorSandboxIdB, -1, authorSandboxPathB, false).size());
        
        assets = sbService.listChangedWebApp(authorSandboxIdB, webAppB, false);
        assertEquals(0, assets.size());
        
        // modify file
        final String MYFILE_MODIFIED = "This is testfile.txt modified in BBB";
        
        writer = assetService.getContentWriter(assetService.getAssetWebApp(authorSandboxIdB, webAppB+"/", "/testfile.txt"));
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE_MODIFIED);
        
        assets = sbService.listChangedWebApp(authorSandboxIdB, webAppB, false);
        assertEquals(1, assets.size());
        
        // ETHREEOH-2836
        assertEquals(AVMDifference.NEWER, assets.get(0).getDiffCode());
        
        // initiate submit (modified asset)
        sbService.submitWebApp(authorSandboxIdB, webAppB, "B1", "B1");
        
        // wait for submit to complete
        pollForSnapshotCount(stagingSandboxIdB, 1);
        
        assets = sbService.listChangedWebApp(authorSandboxIdB, webAppB, false);
        assertEquals(0, assets.size());
    }
    
    
    public void testSubmitDeletedItemsWithLF1() throws IOException, InterruptedException
    {
        // Create Web Project A
        
        WebProjectInfo wpInfoA = wpService.createWebProject(TEST_SANDBOX+"-A", TEST_WEBPROJ_NAME+" A", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreIdA = wpInfoA.getStoreId();
        final String webAppA = wpInfoA.getDefaultWebApp();
        final String stagingSandboxIdA = wpInfoA.getStagingStoreName();
        
        SandboxInfo sbInfoA = sbService.getAuthorSandbox(wpStoreIdA);
        String authorSandboxIdA = sbInfoA.getSandboxId();
        
        // no assets
        String stagingSandboxPathA = sbInfoA.getSandboxRootPath() + "/" + webAppA;
        assertEquals(0, assetService.listAssets(stagingSandboxIdA, -1, stagingSandboxPathA, false).size());
        
        // no changes yet
        List<AssetInfo> assets = sbService.listChangedAll(authorSandboxIdA, true);
        assertEquals(0, assets.size());
        
        String authorSandboxPathA = sbInfoA.getSandboxRootPath() + "/" + webAppA;
        
        // create file in A
        final String MYFILE_A = "This is a.txt in A";
        ContentWriter writer = assetService.createFile(authorSandboxIdA, authorSandboxPathA, "a.txt", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE_A);
        
        assertEquals(1, assetService.listAssets(authorSandboxIdA, -1, authorSandboxPathA, false).size());
        
        assets = sbService.listChangedWebApp(authorSandboxIdA, webAppA, false);
        assertEquals(1, assets.size());
        
        // check staging A before
        assertEquals(0, assetService.listAssets(stagingSandboxIdA, -1, stagingSandboxPathA, false).size());
        
        // submit (new assets) !
        sbService.submitWebApp(authorSandboxIdA, webAppA, "A1", "A1");
        
        pollForSnapshotCount(stagingSandboxIdA, 1);
        
        assets = sbService.listChangedWebApp(authorSandboxIdA, webAppA, false);
        assertEquals(0, assets.size());
        
        // check staging A after
        List<AssetInfo> listing = assetService.listAssets(stagingSandboxIdA, -1, stagingSandboxPathA, false);
        assertEquals(1, listing.size());
        
        listing = assetService.listAssets(stagingSandboxIdA, -1, stagingSandboxPathA, false);
        assertEquals(1, listing.size());
        
        // Create Web Project B
        
        WebProjectInfo wpInfoB = wpService.createWebProject(TEST_SANDBOX+"-B", TEST_WEBPROJ_NAME+" B", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreIdB = wpInfoB.getStoreId();
        final String webAppB = wpInfoB.getDefaultWebApp();
        final String stagingSandboxIdB = wpInfoB.getStagingStoreName();
        
        SandboxInfo sbInfoB = sbService.getAuthorSandbox(wpStoreIdB);
        String authorSandboxIdB = sbInfoB.getSandboxId();
        
        // no assets
        String stagingSandboxPathB = sbInfoB.getSandboxRootPath() + "/" + webAppB;
        assertEquals(0, assetService.listAssets(stagingSandboxIdB, -1, stagingSandboxPathB, false).size());
        
        // no changes yet
        assets = sbService.listChangedAll(authorSandboxIdB, true);
        assertEquals(0, assets.size());
        
        // drop to AVM to create WCM layered file (not supported via WCM services)
        avmService.createLayeredFile(wpStoreIdA+":"+stagingSandboxPathA+"/a.txt", wpStoreIdB+":"+stagingSandboxPathB, "a.txt");
        
        String authorSandboxPathB = sbInfoB.getSandboxRootPath() + "/" + webAppB;
        
        assertEquals(1, assetService.listAssets(authorSandboxIdB, -1, authorSandboxPathB, false).size());
        
        // delete layered file a.txt from B (admin sandbox)
        assetService.deleteAsset(assetService.getAssetWebApp(authorSandboxIdB, webAppB, "a.txt"));
        
        assertEquals(1, sbService.listChangedAll(authorSandboxIdB, true).size());
        
        // submit (deleted asset)
        sbService.submitWebApp(authorSandboxIdB, webAppB, "B2", "B2");
        
        pollForSnapshotCount(stagingSandboxIdB, 1);
        
        assertEquals(0, sbService.listChangedAll(authorSandboxIdB, true).size());
        
        // check staging B after
        assertEquals(0, assetService.listAssets(stagingSandboxIdB, -1, stagingSandboxPathB, false).size());
    }
    
    public void testSubmitDeletedItemsWithLF2() throws IOException, InterruptedException
    {
        // Create Web Project A
        
        WebProjectInfo wpInfoA = wpService.createWebProject(TEST_SANDBOX+"-A", TEST_WEBPROJ_NAME+" A", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreIdA = wpInfoA.getStoreId();
        final String webAppA = wpInfoA.getDefaultWebApp();
        final String stagingSandboxIdA = wpInfoA.getStagingStoreName();
        
        SandboxInfo sbInfoA = sbService.getAuthorSandbox(wpStoreIdA);
        String authorSandboxIdA = sbInfoA.getSandboxId();
        
        // no assets
        String stagingSandboxPathA = sbInfoA.getSandboxRootPath() + "/" + webAppA;
        assertEquals(0, assetService.listAssets(stagingSandboxIdA, -1, stagingSandboxPathA, false).size());
        
        // no changes yet
        List<AssetInfo> assets = sbService.listChangedAll(authorSandboxIdA, true);
        assertEquals(0, assets.size());
        
        String authorSandboxPathA = sbInfoA.getSandboxRootPath() + "/" + webAppA;
        
        // create file in A
        final String MYFILE_A = "This is a.txt in A";
        ContentWriter writer = assetService.createFile(authorSandboxIdA, authorSandboxPathA, "a.txt", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE_A);
        
        assertEquals(1, assetService.listAssets(authorSandboxIdA, -1, authorSandboxPathA, false).size());
        
        assets = sbService.listChangedWebApp(authorSandboxIdA, webAppA, false);
        assertEquals(1, assets.size());
        
        // check staging A before
        assertEquals(0, assetService.listAssets(stagingSandboxIdA, -1, stagingSandboxPathA, false).size());
        
        // submit (new assets) !
        sbService.submitWebApp(authorSandboxIdA, webAppA, "A1", "A1");
        
        pollForSnapshotCount(stagingSandboxIdA, 1);
        
        assets = sbService.listChangedWebApp(authorSandboxIdA, webAppA, false);
        assertEquals(0, assets.size());
        
        // check staging A after
        List<AssetInfo> listing = assetService.listAssets(stagingSandboxIdA, -1, stagingSandboxPathA, false);
        assertEquals(1, listing.size());
        
        listing = assetService.listAssets(stagingSandboxIdA, -1, stagingSandboxPathA, false);
        assertEquals(1, listing.size());
        
        // Create Web Project B
        
        WebProjectInfo wpInfoB = wpService.createWebProject(TEST_SANDBOX+"-B", TEST_WEBPROJ_NAME+" B", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreIdB = wpInfoB.getStoreId();
        final String webAppB = wpInfoB.getDefaultWebApp();
        final String stagingSandboxIdB = wpInfoB.getStagingStoreName();
        
        SandboxInfo sbInfoB = sbService.getAuthorSandbox(wpStoreIdB);
        String authorSandboxIdB = sbInfoB.getSandboxId();
        
        // no assets
        String stagingSandboxPathB = sbInfoB.getSandboxRootPath() + "/" + webAppB;
        assertEquals(0, assetService.listAssets(stagingSandboxIdB, -1, stagingSandboxPathB, false).size());
        
        // no changes yet
        assets = sbService.listChangedAll(authorSandboxIdB, true);
        assertEquals(0, assets.size());
        
        // drop to AVM to create WCM layered file (not supported via WCM services)
        avmService.createLayeredFile(wpStoreIdA+":"+stagingSandboxPathA+"/a.txt", wpStoreIdB+":"+stagingSandboxPathB, "a.txt");
        
        String authorSandboxPathB = sbInfoB.getSandboxRootPath() + "/" + webAppB;
        
        assertEquals(1, assetService.listAssets(authorSandboxIdB, -1, authorSandboxPathB, false).size());
        
        // create file in B
        final String MYFILE_B = "This is b.txt in B";
        writer = assetService.createFile(authorSandboxIdB, authorSandboxPathB, "b.txt", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE_B);
        
        logger.debug("created file b.txt in B admin sandbox");
        
        recursiveList(stagingSandboxIdA);
        recursiveList(stagingSandboxIdB);
        recursiveList(authorSandboxIdB);
        
        // submit (created asset)
        sbService.submitWebApp(authorSandboxIdB, webAppB, "B1", "B1");
        
        logger.debug("submit initiated: created file b.txt in B staging sandbox");
        
        recursiveList(stagingSandboxIdA);
        recursiveList(stagingSandboxIdB);
        recursiveList(authorSandboxIdB);
        
        pollForSnapshotCount(stagingSandboxIdB, 1);
        
        logger.debug("submit completed: created file b.txt in B staging sandbox");
        
        recursiveList(stagingSandboxIdA);
        recursiveList(stagingSandboxIdB);
        recursiveList(authorSandboxIdB);
        
        // check staging B after
        assertEquals(2, assetService.listAssets(stagingSandboxIdB, -1, stagingSandboxPathB, false).size());
        // delete layered file a.txt from B (admin sandbox)
        assetService.deleteAsset(assetService.getAssetWebApp(authorSandboxIdB, webAppB, "a.txt"));
        
        logger.debug("deleted file a.txt from B admin sandbox");
        
        recursiveList(stagingSandboxIdA);
        recursiveList(stagingSandboxIdB);
        recursiveList(authorSandboxIdB);
        
        assertEquals(1, sbService.listChangedAll(authorSandboxIdB, true).size());
        
        // ETHREEOH-2868
        // submit (deleted asset)
        sbService.submitWebApp(authorSandboxIdB, webAppB, "B2", "B2");
        
        logger.debug("submit initiated: deleted file a.txt in B staging sandbox");
        
        recursiveList(stagingSandboxIdA);
        recursiveList(stagingSandboxIdB);
        recursiveList(authorSandboxIdB);
        
        pollForSnapshotCount(stagingSandboxIdB, 2);
        
        logger.debug("submit completed: deleted file a.txt in B staging sandbox");
        
        recursiveList(stagingSandboxIdA);
        recursiveList(stagingSandboxIdB);
        recursiveList(authorSandboxIdB);
        
        assertEquals(0, sbService.listChangedAll(authorSandboxIdB, true).size());
        
        // check staging B after
        assertEquals(1, assetService.listAssets(stagingSandboxIdB, -1, stagingSandboxPathB, false).size());
    }
    
    // revert/undo (changed) assets in user sandbox
    public void testUndo() throws IOException, InterruptedException
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-revertChangedAssets", TEST_WEBPROJ_NAME+" revertChangedAssets", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreId = wpInfo.getStoreId();
        final String webApp = wpInfo.getDefaultWebApp();
        final String stagingSandboxId = wpInfo.getStagingStoreName();
        
        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_PUBLISHER, true);
        wpService.inviteWebUser(wpStoreId, USER_THREE, WCMUtil.ROLE_CONTENT_MANAGER, true);
        wpService.inviteWebUser(wpStoreId, USER_FOUR, WCMUtil.ROLE_CONTENT_REVIEWER, true);
        
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
        
        pollForSnapshotCount(stagingSandboxId, 1);
        
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
        
        // test roles
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // admin (content manager) can revert any sandbox
        String userSandboxId = wpStoreId + "--" + USER_THREE;
        sbService.revertAll(userSandboxId);
        
        // Switch to USER_THREE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_THREE);
        
        // content manager can revert any (author) sandbox
        userSandboxId = wpStoreId + "--" + USER_ONE;
        sbService.revertAll(userSandboxId);
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        try
        {
            // Content contributor - try to revert another user's sandbox (-ve test)
            userSandboxId = wpStoreId + "--" + USER_THREE;
            List<AssetInfo> noAssets = new ArrayList<AssetInfo>(0);
            sbService.revertListAssets(userSandboxId, noAssets);
            fail("Shouldn't be able to revert another author's sandbox");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        // TODO - requires more testing - eg. revert some changes
        
        // Content publisher - can revert another user's sandbox
        userSandboxId = wpStoreId + "--" + USER_ONE;
        sbService.revertAll(userSandboxId);
        
        // Switch to USER_FOUR
        AuthenticationUtil.setFullyAuthenticatedUser(USER_FOUR);
        
        try
        {
            // Content reviewer - try to revert another user's sandbox (-ve test)
            userSandboxId = TEST_SANDBOX+"-get" + "--" + USER_THREE;
            sbService.revertAll(userSandboxId);
            fail("Shouldn't be able to revert another author's sandbox");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
    }
    
    public void testListSnapshots() throws IOException, InterruptedException
    {
        Date fromDate = new Date();
        
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-listSnapshots", TEST_WEBPROJ_NAME+" listSnapshots", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
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
        
        pollForSnapshotCount(stagingSandboxId, 1);
        
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
        
        pollForSnapshotCount(stagingSandboxId, 2);
        
        // check staging after
        listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(4, listing.size());
        
        sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(2, sbVersions.size());
    }
   
    public void testRevertSnapshot1() throws IOException, InterruptedException
    {
        Date fromDate = new Date();
        
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-revertSnapshot", TEST_WEBPROJ_NAME+" revertSnapshot", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
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
        
        pollForSnapshotCount(stagingSandboxId, 1);
        
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
        
        pollForSnapshotCount(stagingSandboxId, 2);
        
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
        
        pollForSnapshotCount(stagingSandboxId, 3);
        
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
    
    public void testRevertSnapshot2() throws IOException, InterruptedException
    {
        Date fromDate = new Date();
        
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-revertSnapshot2", TEST_WEBPROJ_NAME+" revertSnapshot2", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreId = wpInfo.getStoreId();
        final String webApp = wpInfo.getDefaultWebApp();
        final String stagingSandboxId = wpInfo.getStagingStoreName();
        
        SandboxInfo sbInfo = sbService.getStagingSandbox(wpStoreId);
        String stagingSandboxPath = sbInfo.getSandboxRootPath() + "/" + webApp;
        
        sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String authorSandboxId = sbInfo.getSandboxId();
        
        String authorSandboxPath = sbInfo.getSandboxRootPath() + "/" + webApp;
        
        List<SandboxVersion> sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(0, sbVersions.size());
        
        assetService.createFile(authorSandboxId, authorSandboxPath, "c1.txt", null);
        sbService.submitWebApp(authorSandboxId, webApp, "s1", "s1");
        
        pollForSnapshotCount(stagingSandboxId, 1);
        
        List<AssetInfo> listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(1, listing.size());
        for (AssetInfo asset : listing)
        {
            if (asset.getName().equals("c1.txt") && asset.isFile())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
        
        assetService.createFile(authorSandboxId, authorSandboxPath, "c2.txt", null);
        sbService.submitWebApp(authorSandboxId, webApp, "s2", "s2");
        
        pollForSnapshotCount(stagingSandboxId, 2);
        
        listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(2, listing.size());
        for (AssetInfo asset : listing)
        {
            if (asset.getName().equals("c1.txt") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("c2.txt") && asset.isFile())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
        
        sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        int snapshotVersionId2 = sbVersions.get(0).getVersion();
        
        assetService.createFile(authorSandboxId, authorSandboxPath, "c3.txt", null);
        sbService.submitWebApp(authorSandboxId, webApp, "s3", "s3");
        
        pollForSnapshotCount(stagingSandboxId, 3);
        
        listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(3, listing.size());
        for (AssetInfo asset : listing)
        {
            if (asset.getName().equals("c1.txt") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("c2.txt") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("c3.txt") && asset.isFile())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
        
        sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        int snapshotVersionId3 = sbVersions.get(0).getVersion();
        
        assetService.createFile(authorSandboxId, authorSandboxPath, "c4.txt", null);
        sbService.submitWebApp(authorSandboxId, webApp, "s4", "s4");
        
        pollForSnapshotCount(stagingSandboxId, 4);
        
        listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(4, listing.size());
        for (AssetInfo asset : listing)
        {
            if (asset.getName().equals("c1.txt") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("c2.txt") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("c3.txt") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("c4.txt") && asset.isFile())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
        
        AssetInfo file = assetService.getAsset(authorSandboxId, authorSandboxPath+"/c2.txt");
        assetService.deleteAsset(file);
        sbService.submitWebApp(authorSandboxId, webApp, "s5", "s5");
        
        pollForSnapshotCount(stagingSandboxId, 5);
        
        listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(3, listing.size());
        for (AssetInfo asset : listing)
        {
            if (asset.getName().equals("c1.txt") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("c3.txt") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("c4.txt") && asset.isFile())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
        
        // revert to snapshot
        // ETWOTWO-1244
        sbService.revertSnapshot(stagingSandboxId, snapshotVersionId2);
        
        listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(2, listing.size());
        for (AssetInfo asset : listing)
        {
            if (asset.getName().equals("c1.txt") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("c2.txt") && asset.isFile())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
        
        // revert to snapshot
        // ETWOTWO-1244
        sbService.revertSnapshot(stagingSandboxId, snapshotVersionId3);
        
        listing = assetService.listAssets(stagingSandboxId, -1, stagingSandboxPath, false);
        assertEquals(3, listing.size());
        for (AssetInfo asset : listing)
        {
            if (asset.getName().equals("c1.txt") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("c2.txt") && asset.isFile())
            {
                continue;
            }
            else if (asset.getName().equals("c3.txt") && asset.isFile())
            {
                continue;
            }
            else
            {
                fail("The asset '" + asset.getName() + "' is not recognised");
            }
        }
    }
    
    // submit sandbox
    public void testSubmitAction() throws Exception
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-submitAction", TEST_WEBPROJ_NAME+" submitAction", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        String wpStoreId = wpInfo.getStoreId();
        String webApp = wpInfo.getDefaultWebApp();
        String stagingSandboxId = wpInfo.getStagingStoreName();
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        final String sbStoreId = sbInfo.getSandboxId();
        
        assetService.createFolderWebApp(sbStoreId, webApp, "/", "a");
        assetService.createFolderWebApp(sbStoreId, webApp, "/a", "b");
        assetService.createFolderWebApp(sbStoreId, webApp, "/a/b", "c");
        
        assetService.createFileWebApp(sbStoreId, webApp, "/a/b/c", "foo");
        assetService.createFileWebApp(sbStoreId, webApp, "/a/b/c", "bar");
        
        List<AssetInfo> changedAssets = sbService.listChangedWebApp(sbStoreId, webApp, false);
        assertEquals(1, changedAssets.size());
        assertEquals(sbInfo.getSandboxRootPath()+"/"+webApp+"/a", changedAssets.get(0).getPath());
        
        final ActionImpl action = new ActionImpl(null, GUID.generate(), WCMSandboxSubmitAction.NAME);
        
        action.setParameterValue(WCMSandboxUndoAction.PARAM_SANDBOX_ID, sbStoreId);
        action.setParameterValue(WCMSandboxUndoAction.PARAM_PATH_LIST, null);
        
        final WCMSandboxSubmitAction submit = (WCMSandboxSubmitAction)ctx.getBean(WCMSandboxSubmitAction.NAME);
        
        class TxnWork implements RetryingTransactionCallback<Object>
        {
            public Object execute() throws Exception
            {
                submit.execute(action, null);
                return null;
            }
        };
        TransactionService transactionService = (TransactionService) ctx.getBean("transactionService");
        
        // first submit - all (note: within /www/avm_webapps)
        transactionService.getRetryingTransactionHelper().doInTransaction(new TxnWork());
        
        pollForSnapshotCount(stagingSandboxId, 1);
        
        assetService.createFile(sbStoreId, JNDIConstants.DIR_DEFAULT_WWW, "figs", null);
        
        AssetInfo fileAsset = assetService.getAssetWebApp(sbStoreId, webApp, "/a/b/c/foo");
        assetService.getContentWriter(fileAsset).getContentOutputStream().close();
        
        fileAsset = assetService.getAssetWebApp(sbStoreId, webApp, "/a/b/c/bar");
        assetService.deleteAsset(fileAsset);
        
        changedAssets = sbService.listChanged(sbStoreId, JNDIConstants.DIR_DEFAULT_WWW, true);
        assertEquals(3, changedAssets.size());
        
        assertEquals(AVMDifference.NEWER, changedAssets.get(0).getDiffCode());
        assertEquals(sbInfo.getSandboxRootPath()+"/"+webApp+"/a/b/c/bar", changedAssets.get(0).getPath());
        
        assertEquals(AVMDifference.NEWER, changedAssets.get(1).getDiffCode());
        assertEquals(sbInfo.getSandboxRootPath()+"/"+webApp+"/a/b/c/foo", changedAssets.get(1).getPath());
        
        assertEquals(AVMDifference.NEWER, changedAssets.get(2).getDiffCode());
        assertEquals("/"+ JNDIConstants.DIR_DEFAULT_WWW+"/figs", changedAssets.get(2).getPath());
        
        List<String> paths = new ArrayList<String>();
        paths.add(changedAssets.get(0).getPath());
        paths.add(changedAssets.get(1).getPath());
        paths.add(changedAssets.get(2).getPath());
        
        // second submit - list (note: including above /www/avm_webapps)
        action.setParameterValue(WCMSandboxUndoAction.PARAM_PATH_LIST, (Serializable)paths);
        action.setParameterValue(WCMSandboxUndoAction.PARAM_SANDBOX_ID, sbStoreId);
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new TxnWork());
        
        pollForSnapshotCount(stagingSandboxId, 2);
        
        changedAssets = sbService.listChanged(sbStoreId, JNDIConstants.DIR_DEFAULT_WWW, true);
        assertEquals(0, changedAssets.size());
    }
    
    // revert/undo sandbox
    public void testUndoAction() throws Exception
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-revertListAction", TEST_WEBPROJ_NAME+" revertListAction", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        String wpStoreId = wpInfo.getStoreId();
        String webApp = wpInfo.getDefaultWebApp();
        String stagingSandboxId = wpInfo.getStagingStoreName();
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        final String sbStoreId = sbInfo.getSandboxId();
        
        assetService.createFolderWebApp(sbStoreId, webApp, "/", "a");
        assetService.createFolderWebApp(sbStoreId, webApp, "/a", "b");
        assetService.createFolderWebApp(sbStoreId, webApp, "/a/b", "c");
        
        List<AssetInfo> changedAssets = sbService.listChanged(sbStoreId, JNDIConstants.DIR_DEFAULT_WWW, true);
        assertEquals(1, changedAssets.size());
        
        assertEquals(AVMDifference.NEWER, changedAssets.get(0).getDiffCode());
        assertEquals(sbInfo.getSandboxRootPath()+"/"+webApp+"/a", changedAssets.get(0).getPath());
        
        sbService.submitWebApp(sbStoreId, webApp, "submitLabel", "submitDescription");
        
        pollForSnapshotCount(stagingSandboxId, 1);
        
        assetService.createFileWebApp(sbStoreId, webApp, "/a/b/c", "foo");
        
        changedAssets = sbService.listChanged(sbStoreId, JNDIConstants.DIR_DEFAULT_WWW, true);
        assertEquals(1, changedAssets.size());
        
        assertEquals(AVMDifference.NEWER, changedAssets.get(0).getDiffCode());
        assertEquals(sbInfo.getSandboxRootPath()+"/"+webApp+"/a/b/c/foo", changedAssets.get(0).getPath());
        
        sbService.submitWebApp(sbStoreId, webApp, "submitLabel", "submitDescription");
        
        pollForSnapshotCount(stagingSandboxId, 2);
        
        assetService.createFileWebApp(sbStoreId, webApp, "/a/b/c", "bar");
        
        assertNotNull(assetService.getAssetWebApp(sbStoreId, webApp, "/a/b/c/bar"));
        
        changedAssets = sbService.listChanged(sbStoreId, JNDIConstants.DIR_DEFAULT_WWW, true);
        assertEquals(1, changedAssets.size());
        
        assertEquals(AVMDifference.NEWER, changedAssets.get(0).getDiffCode());
        assertEquals(sbInfo.getSandboxRootPath()+"/"+webApp+"/a/b/c/bar", changedAssets.get(0).getPath());
        
        List<SandboxVersion> snapshotVersions = sbService.listSnapshots(sbStoreId, false);
        assertEquals(2, snapshotVersions.size());
        
        final ActionImpl action = new ActionImpl(null, GUID.generate(), WCMSandboxUndoAction.NAME);
        
        List<String> paths = new ArrayList<String>();
        paths.add(sbInfo.getSandboxRootPath()+"/"+webApp+"/a/b/c/bar");
        
        action.setParameterValue(WCMSandboxUndoAction.PARAM_PATH_LIST, (Serializable)paths);
        action.setParameterValue(WCMSandboxUndoAction.PARAM_SANDBOX_ID, sbStoreId);
        
        final WCMSandboxUndoAction revertList = (WCMSandboxUndoAction)ctx.getBean(WCMSandboxUndoAction.NAME);
        
        class TxnWork implements RetryingTransactionCallback<Object>
        {
            public Object execute() throws Exception
            {
                revertList.execute(action, null);
                return null;
            }
        };
        TransactionService transactionService = (TransactionService) ctx.getBean("transactionService");
        transactionService.getRetryingTransactionHelper().doInTransaction(new TxnWork());
        
        pollForSnapshotCount(stagingSandboxId, 2);
        
        snapshotVersions = sbService.listSnapshots(sbStoreId, false);
        assertEquals(2, snapshotVersions.size());
        
        changedAssets = sbService.listChanged(sbStoreId, JNDIConstants.DIR_DEFAULT_WWW, true);
        assertEquals(0, changedAssets.size());
        
        assertNotNull(assetService.getAssetWebApp(sbStoreId, webApp, "/a/b/c/foo"));
        assertNull(assetService.getAssetWebApp(sbStoreId, webApp, "/a/b/c/bar"));
    }
    
    public void testRevertSnapshotAction() throws Exception
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_SANDBOX+"-revertSnapshotAction", TEST_WEBPROJ_NAME+" revertSnapshotAction", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        String wpStoreId = wpInfo.getStoreId();
        String webApp = wpInfo.getDefaultWebApp();
        final String stagingStoreId = wpInfo.getStagingStoreName();
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        final String sbStoreId = sbInfo.getSandboxId();
        
        assetService.createFolderWebApp(sbStoreId, webApp, "/", "a");
        assetService.createFolderWebApp(sbStoreId, webApp, "/a", "b");
        assetService.createFolderWebApp(sbStoreId, webApp, "/a/b", "c");
        
        List<AssetInfo> changedAssets = sbService.listChanged(sbStoreId, JNDIConstants.DIR_DEFAULT_WWW, true);
        assertEquals(1, changedAssets.size());
        
        assertEquals(AVMDifference.NEWER, changedAssets.get(0).getDiffCode());
        assertEquals(sbInfo.getSandboxRootPath()+"/"+webApp+"/a", changedAssets.get(0).getPath());
        
        sbService.submitWebApp(sbStoreId, webApp, "submitLabel", "submitDescription");
        
        pollForSnapshotCount(stagingStoreId, 1);
        
        assetService.createFileWebApp(sbStoreId, webApp, "/a/b/c", "foo");
        
        changedAssets = sbService.listChanged(sbStoreId, JNDIConstants.DIR_DEFAULT_WWW, true);
        assertEquals(1, changedAssets.size());
        
        assertEquals(AVMDifference.NEWER, changedAssets.get(0).getDiffCode());
        assertEquals(sbInfo.getSandboxRootPath()+"/"+webApp+"/a/b/c/foo", changedAssets.get(0).getPath());
        
        sbService.submitWebApp(sbStoreId, webApp, "submitLabel", "submitDescription");
        
        pollForSnapshotCount(stagingStoreId, 2);
        
        assetService.createFileWebApp(sbStoreId, webApp, "/a/b/c", "bar");
        
        changedAssets = sbService.listChanged(sbStoreId, JNDIConstants.DIR_DEFAULT_WWW, true);
        assertEquals(1, changedAssets.size());
        
        assertEquals(AVMDifference.NEWER, changedAssets.get(0).getDiffCode());
        assertEquals(sbInfo.getSandboxRootPath()+"/"+webApp+"/a/b/c/bar", changedAssets.get(0).getPath());
        
        sbService.submitWebApp(sbStoreId, webApp, "submitLabel", "submitDescription");
        
        pollForSnapshotCount(stagingStoreId, 3);
        
        List<SandboxVersion> snapshotVersions = sbService.listSnapshots(stagingStoreId, false);
        assertEquals(3, snapshotVersions.size());
        
        assertNotNull(assetService.getAssetWebApp(sbStoreId, webApp, "/a/b/c/foo"));
        assertNotNull(assetService.getAssetWebApp(sbStoreId, webApp, "/a/b/c/bar"));
        
        final ActionImpl action = new ActionImpl(null, GUID.generate(), WCMSandboxRevertSnapshotAction.NAME);
        action.setParameterValue(WCMSandboxRevertSnapshotAction.PARAM_VERSION, snapshotVersions.get(2).getVersion());
        
        final WCMSandboxRevertSnapshotAction revertSnapshot = (WCMSandboxRevertSnapshotAction)ctx.getBean(WCMSandboxRevertSnapshotAction.NAME);
        
        class TxnWork implements RetryingTransactionCallback<Object>
        {
            public Object execute() throws Exception
            {
                revertSnapshot.execute(action, AVMNodeConverter.ToNodeRef(-1, stagingStoreId+":/"));
                return null;
            }
        };
        TransactionService transactionService = (TransactionService) ctx.getBean("transactionService");
        transactionService.getRetryingTransactionHelper().doInTransaction(new TxnWork());
        
        pollForSnapshotCount(stagingStoreId, 4);
        
        snapshotVersions = sbService.listSnapshots(stagingStoreId, false);
        assertEquals(4, snapshotVersions.size());
        
        changedAssets = sbService.listChanged(sbStoreId, JNDIConstants.DIR_DEFAULT_WWW, true);
        assertEquals(0, changedAssets.size());
        
        assertNull(assetService.getAssetWebApp(sbStoreId, webApp, "/a/b/c/foo"));
        assertNull(assetService.getAssetWebApp(sbStoreId, webApp, "/a/b/c/bar"));
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
            wpService.createWebProject(TEST_SANDBOX+"-"+i, TEST_WEBPROJ_NAME+"-"+i, TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION); // ignore return
        }
        
        System.out.println("testPseudoScaleTest: created "+SCALE_WEBPROJECTS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");
        
        split = System.currentTimeMillis();
        
        for (int i = 1; i <= SCALE_WEBPROJECTS; i++)
        {
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_SANDBOX+"-"+i);
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
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_SANDBOX+"-"+i);
            assertEquals(SCALE_USERS+2, sbService.listSandboxes(wpInfo.getStoreId()).size()); // including staging sandbox and admin sandbox (web project creator)
        }

        System.out.println("testPseudoScaleTest: list sandboxes for admin for each of "+SCALE_WEBPROJECTS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");

        split = System.currentTimeMillis();
        
        for (int i = 1; i <= SCALE_WEBPROJECTS; i++)
        {
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_SANDBOX+"-"+i);
            assertEquals(SCALE_USERS+1, wpService.listWebUsers(wpInfo.getStoreId()).size()); // including admin user (web project creator)
        }

        System.out.println("testPseudoScaleTest: list web users for admin for each of "+SCALE_WEBPROJECTS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");

        split = System.currentTimeMillis();

        for (int i = 1; i <= SCALE_WEBPROJECTS; i++)
        {
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_SANDBOX+"-"+i);
            
            for (int j = 1; j <= SCALE_USERS; j++)
            {
                AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER+"-"+j);
                assertEquals(SCALE_USERS+2, sbService.listSandboxes(wpInfo.getStoreId()).size()); // including staging sandbox and admin sandbox (web project creator)
            }
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        } 
        
        System.out.println("testPseudoScaleTest: list sandboxes for "+SCALE_USERS+" content managers for each of "+SCALE_WEBPROJECTS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");
        
        split = System.currentTimeMillis();

        for (int i = 1; i <= SCALE_WEBPROJECTS; i++)
        {
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_SANDBOX+"-"+i);
            
            for (int j = 1; j <= SCALE_USERS; j++)
            {
                AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER+"-"+j);
                assertEquals(SCALE_USERS+1, wpService.listWebUsers(wpInfo.getStoreId()).size()); // including admin user (web project creator)
            }
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        }
        
        System.out.println("testPseudoScaleTest: list web users for "+SCALE_USERS+" content managers for each of "+SCALE_WEBPROJECTS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");
      
        split = System.currentTimeMillis();
        
        for (int i = 1; i <= SCALE_WEBPROJECTS; i++)
        {
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_SANDBOX+"-"+i);
            
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
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_SANDBOX+"-"+i);
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
    
    protected void recursiveList(String store)
    {
        String list = recursiveList(store, -1, true);
        if (logger.isDebugEnabled())
        { 
            logger.debug(store+":");
            logger.debug(list);
        }
    }
    
    /**
     * Helper to write a recursive listing of an AVMStore at a given version.
     * @param repoName The name of the AVMStore.
     * @param version The version to look under.
     */
    protected String recursiveList(String repoName, int version, boolean followLinks)
    {
        return recursiveList(repoName + ":/", version, 0, followLinks);
    }
    
    /**
     * Recursive list the given path.
     * @param path The path.
     * @param version The version.
     * @param indent The current indent level.
     */
    protected String recursiveList(String path, int version, int indent, boolean followLinks)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indent; i++)
        {
            builder.append(' ');
        }
        builder.append(path.substring(path.lastIndexOf('/') + 1));
        builder.append(' ');
        AVMNodeDescriptor desc = avmService.lookup(version, path, true);
        builder.append(desc.toString());
        builder.append('\n');
        if (desc.getType() == AVMNodeType.PLAIN_DIRECTORY ||
            (desc.getType() == AVMNodeType.LAYERED_DIRECTORY && followLinks))
        {
            String basename = path.endsWith("/") ? path : path + "/";
            Map<String, AVMNodeDescriptor> listing = avmService.getDirectoryListing(version, path);
            for (String name : listing.keySet())
            {
                if (logger.isTraceEnabled()) { logger.trace(name); }
                builder.append(recursiveList(basename + name, version, indent + 2, followLinks));
            }
        }
        return builder.toString();
    }
}
