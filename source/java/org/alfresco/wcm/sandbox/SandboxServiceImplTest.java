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
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
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
    
    private char AVM_STORE_SEPARATOR = ':';
    
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
    private AuthenticationService authenticationService;
    private PersonService personService;
    
    private AVMService avmLockingAwareService;
    private AVMService avmNonLockingAwareService;

    
    @Override
    protected void setUp() throws Exception
    {
        // Get the required services
        wpService = (WebProjectService)ctx.getBean("WebProjectService");
        sbService = (SandboxService)ctx.getBean("SandboxService");
        authenticationService = (AuthenticationService)ctx.getBean("AuthenticationService");
        personService = (PersonService)ctx.getBean("PersonService");
        
        // WCM locking
        avmLockingAwareService = (AVMService)ctx.getBean("AVMLockingAwareService");
        
        // without WCM locking
        avmNonLockingAwareService = (AVMService)ctx.getBean("AVMService");
       
        // By default run as Admin
        AuthenticationUtil.setCurrentUser(USER_ADMIN);
        
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
            AuthenticationUtil.setCurrentUser(USER_ADMIN);
            
            List<WebProjectInfo> webProjects = wpService.listWebProjects();
            for (WebProjectInfo wpInfo : webProjects)
            {
                if (wpInfo.getStoreId().startsWith(TEST_WEBPROJ_DNS))
                {
                    wpService.deleteWebProject(wpInfo.getNodeRef());
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
        int storeCnt = avmLockingAwareService.getStores().size();
        
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-simple", TEST_WEBPROJ_NAME+"-simple", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String wpStoreId = wpInfo.getStoreId();
        
        // list 2 sandboxes
        assertEquals(2, sbService.listSandboxes(wpStoreId).size());
        
        // list 4 extra AVM stores (2 per sandbox)
        assertEquals(storeCnt+4, avmLockingAwareService.getStores().size()); // 2x stating (main,preview), 2x admin author (main, preview)
        
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
        
        assertEquals(storeCnt, avmLockingAwareService.getStores().size());
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
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
        sbInfo1 = sbService.getAuthorSandbox(wpInfo1.getStoreId());
        assertNull(sbInfo1);
        
        // Switch back to admin
        AuthenticationUtil.setCurrentUser(USER_ADMIN);
        
        // Invite web user
        wpService.inviteWebUser(wpInfo1.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER);
        
        // Create author sandbox for user one - admin is the creator
        sbInfo1 = sbService.createAuthorSandbox(wpInfo1.getStoreId(), USER_ONE);

        expectedUserSandboxId = TEST_SANDBOX+"-create-author" + "--" + USER_ONE;
        
        sbInfo1 = sbService.getAuthorSandbox(wpInfo1.getStoreId(), USER_ONE);
        checkSandboxInfo(sbInfo1, expectedUserSandboxId, USER_ONE, USER_ADMIN, expectedUserSandboxId, SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
        
        // Switch to USER_ONE
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
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
        AuthenticationUtil.setCurrentUser(USER_TWO);
        
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
        AuthenticationUtil.setCurrentUser(USER_ADMIN);
        
        // Invite web user
        wpService.inviteWebUser(wpInfo1.getStoreId(), USER_TWO, WCMUtil.ROLE_CONTENT_REVIEWER);
        
        // Switch to USER_TWO
        AuthenticationUtil.setCurrentUser(USER_TWO);
        
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
        catch (AlfrescoRuntimeException exception)
        {
            // Expected
        }
        
        try
        {
            // Try to delete non-existant sandbox (-ve test)
            sbService.deleteSandbox("some-random-staging-sandbox");
            fail("Shouldn't be able to delete non-existant sandbox");
        }
        catch (AVMNotFoundException exception)
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
        AuthenticationUtil.setCurrentUser(USER_TWO);
        
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
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
        assertEquals(3, sbService.listSandboxes(wpStoreId).size());
        
        // content manager can delete others
        sbInfo = sbService.getAuthorSandbox(wpStoreId, USER_THREE);
        sbService.deleteSandbox(sbInfo.getSandboxId());
        
        assertEquals(2, sbService.listSandboxes(wpStoreId).size());
    }
    
    // list changed (in this test, new) items in user sandbox compared to staging sandbox
    public void testListNewItems1()
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-listNewItems1", TEST_WEBPROJ_NAME+" listNewItems1", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        String wpStoreId = wpInfo.getStoreId();
        
        assertEquals(2, sbService.listSandboxes(wpStoreId).size());
        
        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR);
        sbService.createAuthorSandbox(wpStoreId, USER_ONE);
        
        // Switch to USER_ONE
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String sbStoreId = sbInfo.getSandboxId();
        
        // no changes yet
        List<AVMNodeDescriptor> items = sbService.listChangedItems(sbStoreId, true);
        assertEquals(0, items.size());
      
        String authorSandboxRootRelativePath = sbInfo.getSandboxRootPath();
        String authorSandboxWebAppRelativePath = sbInfo.getWebAppsPath() + "/" + wpInfo.getDefaultWebApp();
        
        String authorSandboxRootPath = sbStoreId + AVM_STORE_SEPARATOR + authorSandboxRootRelativePath;
        String authorSandboxWebAppPath = sbStoreId + AVM_STORE_SEPARATOR + authorSandboxWebAppRelativePath;
        
        avmLockingAwareService.createFile(authorSandboxRootPath, "myFile1");
        
        items = sbService.listChangedItems(sbStoreId, false);
        assertEquals(1, items.size());
        assertEquals("myFile1", items.get(0).getName());
        
        avmLockingAwareService.createDirectory(authorSandboxWebAppPath, "myDir1");
        avmLockingAwareService.createFile(authorSandboxWebAppPath+"/myDir1", "myFile2");
        avmLockingAwareService.createDirectory(authorSandboxWebAppPath+"/myDir1", "myDir2");
        avmLockingAwareService.createFile(authorSandboxWebAppPath+"/myDir1/myDir2", "myFile3");
        avmLockingAwareService.createFile(authorSandboxWebAppPath+"/myDir1/myDir2", "myFile4");
        avmLockingAwareService.createDirectory(authorSandboxWebAppPath+"/myDir1", "myDir3");
        
        items = sbService.listChangedItems(sbStoreId, false);
        assertEquals(2, items.size()); // new dir with new dirs/files is returned as single change
        
        for (AVMNodeDescriptor item : items)
        {
            if (item.getName().equals("myFile1") && item.isFile())
            {
                continue;
            }
            else if (item.getName().equals("myDir1") && item.isDirectory())
            {
                continue;
            }
            else
            {
                fail("The item '" + item.getName() + "' is not recognised");
            }
        }
        
        items = sbService.listChangedItemsWebApp(sbStoreId, wpInfo.getDefaultWebApp(), false);
        assertEquals(1, items.size());
        
        for (AVMNodeDescriptor item : items)
        {
            if (item.getName().equals("myDir1") && item.isDirectory())
            {
                continue;
            }
            else
            {
                fail("The item '" + item.getName() + "' is not recognised");
            }
        }
        
        items = sbService.listChangedItemsDir(sbStoreId, authorSandboxWebAppRelativePath+"/myDir1", false);
        assertEquals(1, items.size());
        
        for (AVMNodeDescriptor item : items)
        {
            if (item.getName().equals("myDir1") && item.isDirectory())
            {
                continue;
            }
            else
            {
                fail("The item '" + item.getName() + "' is not recognised");
            }
        }
    }
    
    // list changed (in this test, new) items in two different user sandboxes compared to each other
    public void testListNewItems2()
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-listNewItems2", TEST_WEBPROJ_NAME+" listNewItems2", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        String wpStoreId = wpInfo.getStoreId();

        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        
        // Switch to USER_ONE
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
        SandboxInfo sbInfo1 = sbService.getAuthorSandbox(wpStoreId);
        String sbStoreId = sbInfo1.getSandboxId();
        
        List<AVMNodeDescriptor> items = sbService.listChangedItems(sbStoreId, true);
        assertEquals(0, items.size());
      
        String authorSandboxRootPath = sbStoreId + AVM_STORE_SEPARATOR + sbInfo1.getSandboxRootPath();

        avmLockingAwareService.createFile(authorSandboxRootPath, "myFile1");
        
        items = sbService.listChangedItems(sbStoreId, false);
        assertEquals(1, items.size());
        assertEquals("myFile1", items.get(0).getName());
        
        // Switch to USER_TWO
        AuthenticationUtil.setCurrentUser(USER_TWO);
        
        SandboxInfo sbInfo2 = sbService.getAuthorSandbox(wpStoreId);
        sbStoreId = sbInfo2.getSandboxId();
        
        items = sbService.listChangedItems(sbStoreId, true);
        assertEquals(0, items.size());
      
        authorSandboxRootPath = sbStoreId + AVM_STORE_SEPARATOR + sbInfo2.getSandboxRootPath();

        avmLockingAwareService.createFile(authorSandboxRootPath, "myFile2");
        avmLockingAwareService.createFile(authorSandboxRootPath, "myFile3");
        
        items = sbService.listChangedItems(sbStoreId, false);
        assertEquals(2, items.size());

        for (AVMNodeDescriptor item : items)
        {
            if (item.getName().equals("myFile2") && item.isFile())
            {
                continue;
            }
            else if (item.getName().equals("myFile3") && item.isFile())
            {
                continue;
            }
            else
            {
                fail("The item '" + item.getName() + "' is not recognised");
            }
        }
        
        // Switch back to admin
        AuthenticationUtil.setCurrentUser(USER_ADMIN);
        
        sbInfo1 = sbService.getAuthorSandbox(wpStoreId, USER_ONE);
        sbInfo2 = sbService.getAuthorSandbox(wpStoreId, USER_TWO);
        
        items = sbService.listChangedItems(sbInfo1.getSandboxId(), sbInfo1.getSandboxRootPath(), sbInfo2.getSandboxId(), sbInfo2.getSandboxRootPath(), false);
        assertEquals(1, items.size());
        assertEquals("myFile1", items.get(0).getName());
        
        items = sbService.listChangedItems(sbInfo2.getSandboxId(), sbInfo1.getSandboxRootPath(), sbInfo1.getSandboxId(), sbInfo2.getSandboxRootPath(), false);
        assertEquals(2, items.size());
        
        for (AVMNodeDescriptor item : items)
        {
            if (item.getName().equals("myFile2") && item.isFile())
            {
                continue;
            }
            else if (item.getName().equals("myFile3") && item.isFile())
            {
                continue;
            }
            else
            {
                fail("The item '" + item.getName() + "' is not recognised");
            }
        }
    }
    
    // list changed (in this test, new) items in two different user sandboxes compared to each other - without locking
    public void testListNewItems3()
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-listNewItems2", TEST_WEBPROJ_NAME+" listNewItems2", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        String wpStoreId = wpInfo.getStoreId();

        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        
        // Switch to USER_ONE
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
        SandboxInfo sbInfo1 = sbService.getAuthorSandbox(wpStoreId);
        String sbStoreId = sbInfo1.getSandboxId();
        
        List<AVMNodeDescriptor> items = sbService.listChangedItems(sbStoreId, true);
        assertEquals(0, items.size());
      
        String authorSandboxRootPath = sbStoreId + AVM_STORE_SEPARATOR + sbInfo1.getSandboxRootPath();

        avmNonLockingAwareService.createFile(authorSandboxRootPath, "myFile1");
        
        items = sbService.listChangedItems(sbStoreId, false);
        assertEquals(1, items.size());
        assertEquals("myFile1", items.get(0).getName());
        
        // Switch to USER_TWO
        AuthenticationUtil.setCurrentUser(USER_TWO);
        
        SandboxInfo sbInfo2 = sbService.getAuthorSandbox(wpStoreId);
        sbStoreId = sbInfo2.getSandboxId();
        
        items = sbService.listChangedItems(sbStoreId, true);
        assertEquals(0, items.size());
      
        authorSandboxRootPath = sbStoreId + AVM_STORE_SEPARATOR + sbInfo2.getSandboxRootPath();

        avmNonLockingAwareService.createFile(authorSandboxRootPath, "myFile1"); // allowed, since using base (non-locking-aware) AVM service
        avmNonLockingAwareService.createFile(authorSandboxRootPath, "myFile2");
        avmNonLockingAwareService.createFile(authorSandboxRootPath, "myFile3");
        
        items = sbService.listChangedItems(sbStoreId, false);
        assertEquals(3, items.size());

        for (AVMNodeDescriptor item : items)
        {
            if (item.getName().equals("myFile1") && item.isFile())
            {
                continue;
            }
            else if (item.getName().equals("myFile2") && item.isFile())
            {
                continue;
            }
            else if (item.getName().equals("myFile3") && item.isFile())
            {
                continue;
            }
            else
            {
                fail("The item '" + item.getName() + "' is not recognised");
            }
        }
        
        // Switch back to admin
        AuthenticationUtil.setCurrentUser(USER_ADMIN);
        
        sbInfo1 = sbService.getAuthorSandbox(wpStoreId, USER_ONE);
        sbInfo2 = sbService.getAuthorSandbox(wpStoreId, USER_TWO);
        
        items = sbService.listChangedItems(sbInfo1.getSandboxId(), sbInfo1.getSandboxRootPath(), sbInfo2.getSandboxId(), sbInfo2.getSandboxRootPath(), false);
        assertEquals(1, items.size());
        assertEquals("myFile1", items.get(0).getName());
        
        items = sbService.listChangedItems(sbInfo2.getSandboxId(), sbInfo1.getSandboxRootPath(), sbInfo1.getSandboxId(), sbInfo2.getSandboxRootPath(), false);
        assertEquals(3, items.size());
        
        for (AVMNodeDescriptor item : items)
        {
            if (item.getName().equals("myFile1") && item.isFile())
            {
                continue;
            }
            else if (item.getName().equals("myFile2") && item.isFile())
            {
                continue;
            }
            else if (item.getName().equals("myFile3") && item.isFile())
            {
                continue;
            }
            else
            {
                fail("The item '" + item.getName() + "' is not recognised");
            }
        }
    }
    
    // submit new items in user sandbox to staging sandbox
    public void testSubmitNewItems1()
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-submitNewItems1", TEST_WEBPROJ_NAME+" submitNewItems1", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        String wpStoreId = wpInfo.getStoreId();
        String webApp = wpInfo.getDefaultWebApp();
        String stagingSandboxId = wpInfo.getStagingStoreName();
        
        // Invite web user
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        
        // Switch to USER_ONE
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        List<AVMNodeDescriptor> items = sbService.listChangedItems(authorSandboxId, true);
        assertEquals(0, items.size());
      
        String authorSandboxWebppPath = authorSandboxId + AVM_STORE_SEPARATOR + sbInfo.getWebAppsPath() + "/" + webApp;
        
        avmLockingAwareService.createFile(authorSandboxWebppPath, "myFile1");
        avmLockingAwareService.createDirectory(authorSandboxWebppPath, "myDir1");
        avmLockingAwareService.createFile(authorSandboxWebppPath+"/myDir1", "myFile2");
        avmLockingAwareService.createDirectory(authorSandboxWebppPath+"/myDir1", "myDir2");
        avmLockingAwareService.createFile(authorSandboxWebppPath+"/myDir1/myDir2", "myFile3");
        avmLockingAwareService.createFile(authorSandboxWebppPath+"/myDir1/myDir2", "myFile4");
        avmLockingAwareService.createDirectory(authorSandboxWebppPath+"/myDir1", "myDir3");
        
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(2, items.size()); // new dir with new dirs/files is returned as single change
        
        // check staging before
        String stagingSandboxWebppPath = stagingSandboxId + AVM_STORE_SEPARATOR + sbInfo.getWebAppsPath() + "/" + webApp;
        assertEquals(0, avmLockingAwareService.getDirectoryListing(-1, stagingSandboxWebppPath, false).size());
        
        // submit (new items) !
        sbService.submitAllWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(0, items.size());
        
        // check staging after
        Map<String, AVMNodeDescriptor> listing = avmLockingAwareService.getDirectoryListing(-1, stagingSandboxWebppPath, false);
        assertEquals(2, listing.size());
        
        for (AVMNodeDescriptor item : listing.values())
        {
            if (item.getName().equals("myFile1") && item.isFile())
            {
                continue;
            }
            else if (item.getName().equals("myDir1") && item.isDirectory())
            {
                continue;
            }
            else
            {
                fail("The item '" + item.getName() + "' is not recognised");
            }
        }
    }
    
    // submit changed items in user sandbox to staging sandbox
    public void testSubmitChangedItems1() throws IOException
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-submitChangedItems1", TEST_WEBPROJ_NAME+" submitChangedItems1", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreId = wpInfo.getStoreId();
        final String webApp = wpInfo.getDefaultWebApp();
        final String stagingSandboxId = wpInfo.getStagingStoreName();
        
        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_PUBLISHER, true);
        
        // Switch to USER_ONE
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        List<AVMNodeDescriptor> items = sbService.listChangedItems(authorSandboxId, true);
        assertEquals(0, items.size());
      
        String authorSandboxWebppPath = authorSandboxId + AVM_STORE_SEPARATOR + sbInfo.getWebAppsPath() + "/" + webApp;
        
        final String MYFILE1 = "This is myFile1";
        OutputStream out = avmLockingAwareService.createFile(authorSandboxWebppPath, "myFile1");
        byte [] buff = MYFILE1.getBytes();
        out.write(buff);
        out.close();
        
        avmLockingAwareService.createDirectory(authorSandboxWebppPath, "myDir1");
        
        final String MYFILE2 = "This is myFile2";
        out = avmLockingAwareService.createFile(authorSandboxWebppPath+"/myDir1", "myFile2");
        buff = MYFILE2.getBytes();
        out.write(buff);
        out.close();
                
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(2, items.size());
        
        // check staging before
        String stagingSandboxWebppPath = stagingSandboxId + ":" + sbInfo.getWebAppsPath() + "/" + webApp;
        assertEquals(0, avmLockingAwareService.getDirectoryListing(-1, stagingSandboxWebppPath, false).size());
        
        // submit (new items) !
        sbService.submitAllWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(0, items.size());
        
        // check staging after
        Map<String, AVMNodeDescriptor> listing = avmLockingAwareService.getDirectoryListing(-1, stagingSandboxWebppPath, false);
        assertEquals(2, listing.size());
        
        // Switch to USER_TWO
        AuthenticationUtil.setCurrentUser(USER_TWO);
        
        sbInfo = sbService.getAuthorSandbox(wpStoreId);
        authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        items = sbService.listChangedItems(authorSandboxId, true);
        assertEquals(0, items.size());
      
        authorSandboxWebppPath = authorSandboxId + AVM_STORE_SEPARATOR + sbInfo.getWebAppsPath() + "/" + webApp;
        
        final String MYFILE1_MODIFIED = "This is myFile1 ... modified by "+USER_TWO;
        out = avmLockingAwareService.getFileOutputStream(authorSandboxWebppPath+"/myFile1");
        buff = (MYFILE1_MODIFIED).getBytes();
        out.write(buff);
        out.close();

        final String MYFILE2_MODIFIED = "This is myFile2 ... modified by "+USER_TWO;
        out = avmLockingAwareService.getFileOutputStream(authorSandboxWebppPath+"/myDir1/myFile2");
        buff = (MYFILE2_MODIFIED).getBytes();
        out.write(buff);
        out.close();
                
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(2, items.size());
        
        // check staging before
        stagingSandboxWebppPath = stagingSandboxId + ":" + sbInfo.getWebAppsPath() + "/" + webApp;
        
        InputStream in = avmLockingAwareService.getFileInputStream(-1, stagingSandboxWebppPath+"/myFile1");
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE1, new String(buff, 0, MYFILE1.length())); // assumes 1byte=1char
        
        in = avmLockingAwareService.getFileInputStream(-1, stagingSandboxWebppPath+"/myDir1/myFile2");
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE2, new String(buff, 0, MYFILE2.length()));
        
        // submit (modified items) !
        sbService.submitAllWebApp(authorSandboxId, webApp, null, null);
        
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(0, items.size());
        
        // check staging after
        in = avmLockingAwareService.getFileInputStream(-1, stagingSandboxWebppPath+"/myFile1");
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE1_MODIFIED, new String(buff, 0, MYFILE1_MODIFIED.length()));
        
        in = avmLockingAwareService.getFileInputStream(-1, stagingSandboxWebppPath+"/myDir1/myFile2");
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE2_MODIFIED, new String(buff, 0, MYFILE1_MODIFIED.length()));
    }
    
    // submit deleted items in user sandbox to staging sandbox
    public void testSubmitDeletedItems1() throws IOException
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-submitDeletedItems1", TEST_WEBPROJ_NAME+" submitDeletedItems1", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreId = wpInfo.getStoreId();
        final String webApp = wpInfo.getDefaultWebApp();
        final String stagingSandboxId = wpInfo.getStagingStoreName();
        
        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_PUBLISHER, true);
        
        // Switch to USER_ONE
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        List<AVMNodeDescriptor> items = sbService.listChangedItems(authorSandboxId, true);
        assertEquals(0, items.size());
      
        String authorSandboxWebppPath = authorSandboxId + AVM_STORE_SEPARATOR + sbInfo.getWebAppsPath() + "/" + webApp;
        
        final String MYFILE1 = "This is myFile1";
        OutputStream out = avmLockingAwareService.createFile(authorSandboxWebppPath, "myFile1");
        byte [] buff = MYFILE1.getBytes();
        out.write(buff);
        out.close();
        
        avmLockingAwareService.createDirectory(authorSandboxWebppPath, "myDir1");
        avmLockingAwareService.createDirectory(authorSandboxWebppPath+"/myDir1", "myDir2");
        
        final String MYFILE2 = "This is myFile2";
        out = avmLockingAwareService.createFile(authorSandboxWebppPath+"/myDir1", "myFile2");
        buff = MYFILE2.getBytes();
        out.write(buff);
        out.close();
                
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(2, items.size());
        
        // check staging before
        String stagingSandboxWebppPath = stagingSandboxId + AVM_STORE_SEPARATOR + sbInfo.getWebAppsPath() + "/" + webApp;
        assertEquals(0, avmLockingAwareService.getDirectoryListing(-1, stagingSandboxWebppPath, false).size());
        
        // submit (new items) !
        sbService.submitAllWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(0, items.size());
        
        // check staging after
        Map<String, AVMNodeDescriptor> listing = avmLockingAwareService.getDirectoryListing(-1, stagingSandboxWebppPath, false);
        assertEquals(2, listing.size());
        
        // Switch to USER_TWO
        AuthenticationUtil.setCurrentUser(USER_TWO);
        
        sbInfo = sbService.getAuthorSandbox(wpStoreId);
        authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        items = sbService.listChangedItems(authorSandboxId, true);
        assertEquals(0, items.size());
      
        authorSandboxWebppPath = authorSandboxId + AVM_STORE_SEPARATOR + sbInfo.getWebAppsPath() + "/" + webApp;
        
        avmLockingAwareService.removeNode(authorSandboxWebppPath+"/myFile1");
        avmLockingAwareService.removeNode(authorSandboxWebppPath+"/myDir1/myDir2");
                
        // do not list deleted
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(0, items.size());
        
        // do list deleted
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, true);
        assertEquals(2, items.size());
        
        // check staging before
        stagingSandboxWebppPath = stagingSandboxId + AVM_STORE_SEPARATOR + sbInfo.getWebAppsPath() + "/" + webApp;
        
        assertNotNull(avmLockingAwareService.lookup(-1, stagingSandboxWebppPath+"/myFile1"));
        assertNotNull(avmLockingAwareService.lookup(-1, stagingSandboxWebppPath+"/myDir1"));
        assertNotNull(avmLockingAwareService.lookup(-1, stagingSandboxWebppPath+"/myDir1/myDir2"));
        assertNotNull(avmLockingAwareService.lookup(-1, stagingSandboxWebppPath+"/myDir1/myFile2"));
        
        // submit (deleted items) !
        sbService.submitAllWebApp(authorSandboxId, webApp, null, null);
        
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(0, items.size());
        
        // check staging after
        assertNull(avmLockingAwareService.lookup(-1, stagingSandboxWebppPath+"/myFile1"));
        assertNull(avmLockingAwareService.lookup(-1, stagingSandboxWebppPath+"/myDir1/myDir2"));
        
        assertNotNull(avmLockingAwareService.lookup(-1, stagingSandboxWebppPath+"/myDir1"));
        assertNotNull(avmLockingAwareService.lookup(-1, stagingSandboxWebppPath+"/myDir1/myFile2"));
    }
    
    // revert all (changed) items in user sandbox
    public void testRevertAll() throws IOException
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-revertChangedItems", TEST_WEBPROJ_NAME+" revertChangedItems", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        
        final String wpStoreId = wpInfo.getStoreId();
        final String webApp = wpInfo.getDefaultWebApp();
        final String stagingSandboxId = wpInfo.getStagingStoreName();
        
        // Invite web users
        wpService.inviteWebUser(wpStoreId, USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        wpService.inviteWebUser(wpStoreId, USER_TWO, WCMUtil.ROLE_CONTENT_PUBLISHER, true);
        
        // Switch to USER_ONE
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        List<AVMNodeDescriptor> items = sbService.listChangedItems(authorSandboxId, true);
        assertEquals(0, items.size());
      
        String authorSandboxWebppPath = authorSandboxId + AVM_STORE_SEPARATOR + sbInfo.getWebAppsPath() + "/" + webApp;
        
        final String MYFILE1 = "This is myFile1";
        OutputStream out = avmLockingAwareService.createFile(authorSandboxWebppPath, "myFile1");
        byte [] buff = MYFILE1.getBytes();
        out.write(buff);
        out.close();
        
        avmLockingAwareService.createDirectory(authorSandboxWebppPath, "myDir1");
        
        final String MYFILE2 = "This is myFile2";
        out = avmLockingAwareService.createFile(authorSandboxWebppPath+"/myDir1", "myFile2");
        buff = MYFILE2.getBytes();
        out.write(buff);
        out.close();
                
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(2, items.size());
        
        // check staging before
        String stagingSandboxWebppPath = stagingSandboxId + AVM_STORE_SEPARATOR + sbInfo.getWebAppsPath() + "/" + webApp;
        assertEquals(0, avmLockingAwareService.getDirectoryListing(-1, stagingSandboxWebppPath, false).size());
        
        // submit (new items) !
        sbService.submitAllWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(0, items.size());
        
        // check staging after
        Map<String, AVMNodeDescriptor> listing = avmLockingAwareService.getDirectoryListing(-1, stagingSandboxWebppPath, false);
        assertEquals(2, listing.size());
        
        // Switch to USER_TWO
        AuthenticationUtil.setCurrentUser(USER_TWO);
        
        sbInfo = sbService.getAuthorSandbox(wpStoreId);
        authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        items = sbService.listChangedItems(authorSandboxId, true);
        assertEquals(0, items.size());
      
        authorSandboxWebppPath = authorSandboxId + AVM_STORE_SEPARATOR + sbInfo.getWebAppsPath() + "/" + webApp;
        
        final String MYFILE1_MODIFIED = "This is myFile1 ... modified by "+USER_TWO;
        out = avmLockingAwareService.getFileOutputStream(authorSandboxWebppPath+"/myFile1");
        buff = (MYFILE1_MODIFIED).getBytes();
        out.write(buff);
        out.close();

        final String MYFILE2_MODIFIED = "This is myFile2 ... modified by "+USER_TWO;
        out = avmLockingAwareService.getFileOutputStream(authorSandboxWebppPath+"/myDir1/myFile2");
        buff = (MYFILE2_MODIFIED).getBytes();
        out.write(buff);
        out.close();
                
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(2, items.size());
        
        // check staging before
        stagingSandboxWebppPath = stagingSandboxId + AVM_STORE_SEPARATOR + sbInfo.getWebAppsPath() + "/" + webApp;
        
        InputStream in = avmLockingAwareService.getFileInputStream(-1, stagingSandboxWebppPath+"/myFile1");
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE1, new String(buff, 0, MYFILE1.length())); // assumes 1byte = 1char
        
        in = avmLockingAwareService.getFileInputStream(-1, stagingSandboxWebppPath+"/myDir1/myFile2");
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE2, new String(buff, 0, MYFILE2.length()));
        
        // revert (modified items) !
        sbService.revertAllWebApp(authorSandboxId, webApp);
        
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(0, items.size());
        
        // check staging after
        in = avmLockingAwareService.getFileInputStream(-1, stagingSandboxWebppPath+"/myFile1");
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE1, new String(buff, 0, MYFILE1.length()));
        
        in = avmLockingAwareService.getFileInputStream(-1, stagingSandboxWebppPath+"/myDir1/myFile2");
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
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        List<AVMNodeDescriptor> items = sbService.listChangedItems(authorSandboxId, true);
        assertEquals(0, items.size());
      
        String authorSandboxWebppPath = authorSandboxId + AVM_STORE_SEPARATOR + sbInfo.getWebAppsPath() + "/" + webApp;
        
        avmLockingAwareService.createDirectory(authorSandboxWebppPath, "myDir1");
        avmLockingAwareService.createDirectory(authorSandboxWebppPath, "myDir2");
        avmLockingAwareService.createDirectory(authorSandboxWebppPath, "myDir3");
        
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(3, items.size());
        
        // check staging before
        String stagingSandboxWebppPath = stagingSandboxId + AVM_STORE_SEPARATOR + sbInfo.getWebAppsPath() + "/" + webApp;
        assertEquals(0, avmLockingAwareService.getDirectoryListing(-1, stagingSandboxWebppPath, false).size());
        
        List<VersionDescriptor> sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(0, sbVersions.size());
        
        // submit (new items) !
        sbService.submitAllWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(0, items.size());
        
        // check staging after
        Map<String, AVMNodeDescriptor> listing = avmLockingAwareService.getDirectoryListing(-1, stagingSandboxWebppPath, false);
        assertEquals(3, listing.size());
        
        sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(1, sbVersions.size());
        
        // more changes ...
        avmLockingAwareService.createDirectory(authorSandboxWebppPath, "myDir4");

        // submit (new items) !
        sbService.submitAllWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        // check staging after
        listing = avmLockingAwareService.getDirectoryListing(-1, stagingSandboxWebppPath, false);
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
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
        // Finish: Test ETWOTWO-817
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        List<AVMNodeDescriptor> items = sbService.listChangedItems(authorSandboxId, true);
        assertEquals(0, items.size());
      
        String authorSandboxWebppPath = authorSandboxId + AVM_STORE_SEPARATOR + sbInfo.getWebAppsPath() + "/" + webApp;
        
        avmLockingAwareService.createDirectory(authorSandboxWebppPath, "myDir1");
        
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(1, items.size());
        
        // check staging before
        String stagingSandboxWebppPath = stagingSandboxId + AVM_STORE_SEPARATOR + sbInfo.getWebAppsPath() + "/" + webApp;
        assertEquals(0, avmLockingAwareService.getDirectoryListing(-1, stagingSandboxWebppPath, false).size());
        
        List<VersionDescriptor> sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(0, sbVersions.size());
        
        // submit (new items) !
        sbService.submitAllWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        items = sbService.listChangedItemsWebApp(authorSandboxId, webApp, false);
        assertEquals(0, items.size());
        
        // check staging after
        Map<String, AVMNodeDescriptor> listing = avmLockingAwareService.getDirectoryListing(-1, stagingSandboxWebppPath, false);
        assertEquals(1, listing.size());
        for (AVMNodeDescriptor item : listing.values())
        {
            if (item.getName().equals("myDir1") && item.isDirectory())
            {
                continue;
            }
            else
            {
                fail("The item '" + item.getName() + "' is not recognised");
            }
        }
        
        sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(1, sbVersions.size());
        
        // more changes ...
        avmLockingAwareService.createDirectory(authorSandboxWebppPath, "myDir2");

        // submit (new items) !
        sbService.submitAllWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        // check staging after
        listing = avmLockingAwareService.getDirectoryListing(-1, stagingSandboxWebppPath, false);
        assertEquals(2, listing.size());
        for (AVMNodeDescriptor item : listing.values())
        {
            if (item.getName().equals("myDir1") && item.isDirectory())
            {
                continue;
            }
            else if (item.getName().equals("myDir2") && item.isDirectory())
            {
                continue;
            }
            else
            {
                fail("The item '" + item.getName() + "' is not recognised");
            }
        }
        
        sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(2, sbVersions.size());
        
        // more changes ...
        avmLockingAwareService.createDirectory(authorSandboxWebppPath, "myDir3");

        // submit (new items) !
        sbService.submitAllWebApp(authorSandboxId, webApp, "a submit label", "a submit comment");
        
        // check staging after
        listing = avmLockingAwareService.getDirectoryListing(-1, stagingSandboxWebppPath, false);
        assertEquals(3, listing.size());
        for (AVMNodeDescriptor item : listing.values())
        {
            if (item.getName().equals("myDir1") && item.isDirectory())
            {
                continue;
            }
            else if (item.getName().equals("myDir2") && item.isDirectory())
            {
                continue;
            }
            else if (item.getName().equals("myDir3") && item.isDirectory())
            {
                continue;
            }
            else
            {
                fail("The item '" + item.getName() + "' is not recognised");
            }
        }
        
        sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(3, sbVersions.size());
        
        // revert to snapshot ...
        
        VersionDescriptor version = sbVersions.get(1);
        int versionId = version.getVersionID();
        
        sbService.revertSnapshot(stagingSandboxId, versionId);
        
        sbVersions = sbService.listSnapshots(stagingSandboxId, fromDate, new Date(), false);
        assertEquals(4, sbVersions.size());
        
        // check staging after
        listing = avmLockingAwareService.getDirectoryListing(-1, stagingSandboxWebppPath, false);
        assertEquals(2, listing.size());
        for (AVMNodeDescriptor item : listing.values())
        {
            if (item.getName().equals("myDir1") && item.isDirectory())
            {
                continue;
            }
            else if (item.getName().equals("myDir2") && item.isDirectory())
            {
                continue;
            }
            else
            {
                fail("The item '" + item.getName() + "' is not recognised");
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
                AuthenticationUtil.setCurrentUser(TEST_USER+"-"+j);
                assertEquals(SCALE_USERS+2, sbService.listSandboxes(wpInfo.getStoreId()).size()); // including staging sandbox and admin sandbox (web project creator)
            }
            AuthenticationUtil.setCurrentUser(USER_ADMIN);
        } 
        
        System.out.println("testPseudoScaleTest: list sandboxes for "+SCALE_USERS+" content managers for each of "+SCALE_WEBPROJECTS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");
        
        split = System.currentTimeMillis();

        for (int i = 1; i <= SCALE_WEBPROJECTS; i++)
        {
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_WEBPROJ_DNS+"-"+i);
            
            for (int j = 1; j <= SCALE_USERS; j++)
            {
                AuthenticationUtil.setCurrentUser(TEST_USER+"-"+j);
                assertEquals(SCALE_USERS+1, wpService.listWebUsers(wpInfo.getStoreId()).size()); // including admin user (web project creator)
            }
            AuthenticationUtil.setCurrentUser(USER_ADMIN);
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
