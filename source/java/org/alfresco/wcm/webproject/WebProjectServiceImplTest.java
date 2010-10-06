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
package org.alfresco.wcm.webproject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.wcm.AbstractWCMServiceImplTest;
import org.alfresco.wcm.asset.AssetInfo;
import org.alfresco.wcm.sandbox.SandboxInfo;
import org.alfresco.wcm.util.WCMUtil;

/**
 * Web Project Service implementation unit test
 * 
 * @author janv
 */
public class WebProjectServiceImplTest extends AbstractWCMServiceImplTest 
{
    // web app names
    private static final String TEST_WEBAPP = "myWebApp";
    
    private static final String TEST_WEBAPP1 = TEST_WEBAPP+"-AppOne";
    private static final String TEST_WEBAPP2 = TEST_WEBAPP+"-AppTwo";
    private static final String TEST_WEBAPP3 = TEST_WEBAPP+"-AppThree";
    
    // groups and additional users
    private static final String TEST_GROUP = "testWebGroup-"+TEST_RUN;

    private static final String USER_FIVE  = TEST_USER+"-Five";
    private static final String USER_SIX   = TEST_USER+"-Six";
    
    private static final String GROUP_ONE  = TEST_GROUP+"-One";
    
    private static final int SCALE_USERS = 5;
    private static final int SCALE_WEBPROJECTS = 5;
    private static final int SCALE_WEBAPPS = 5;
    
    //
    // services
    //
    
    private FileFolderService fileFolderService;
    private AuthorityService authorityService;
    private PermissionService permissionService;
    private AVMService avmService;
    private NodeService nodeService;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        // Get the required services
        fileFolderService = (FileFolderService)ctx.getBean("FileFolderService");
        authorityService = (AuthorityService)ctx.getBean("AuthorityService");
        permissionService = (PermissionService)ctx.getBean("PermissionService");
        avmService = (AVMService)ctx.getBean("AVMService");
        nodeService = (NodeService)ctx.getBean("NodeService");
        
        createUser(USER_FIVE);
        createUser(USER_SIX);
        
        Set<String> userNames = new HashSet<String>(2);
        userNames.add(USER_ONE);
        userNames.add(USER_TWO);
        
        createSimpleGroup(GROUP_ONE, userNames);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        if (CLEAN)
        {
            // Switch back to Admin
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
            
            deleteGroup(GROUP_ONE);
            
            deleteUser(USER_FIVE);
            deleteUser(USER_SIX);
        }
        
        super.tearDown();
    }
    
    private void createSimpleGroup(String shortName, Set<String> userNames)
    {
        String groupName = authorityService.getName(AuthorityType.GROUP, shortName);
        if (authorityService.authorityExists(groupName) == false)
        {
            authorityService.createAuthority(AuthorityType.GROUP, shortName);
            
            for (String userName : userNames)
            {
                authorityService.addAuthority(groupName, userName);
            }
        }
    }
    
    private void deleteGroup(String shortName)
    {
        String groupName = authorityService.getName(AuthorityType.GROUP, shortName);
        if (authorityService.authorityExists(groupName) == true)
        {
            authorityService.deleteAuthority(groupName);
        }
    }
    
    public void testHasWebProjectsRoot()
    {
        // make sure there is a "Web Projects" container node
        assertTrue(wpService.hasWebProjectsRoot());
    }
    
    public void testCreateWebProjectSimple()
    {
        // Create a web project
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-webProjSimple", TEST_WEBPROJ_NAME+"-webProjSimple", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        assertNotNull(wpInfo);
    }
	
    public void testCreateWebProject()
    {
        // Create a web project
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-create", TEST_WEBPROJ_NAME+"-create", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE, null);
        checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-create", TEST_WEBPROJ_NAME+"-create", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE);
        
        // Duplicate web project dns/store name
        try
        {
            // Try to create duplicate web project dns/store (-ve test)
            wpService.createWebProject(TEST_WEBPROJ_DNS+"-create", TEST_WEBPROJ_NAME+"-x", TEST_WEBPROJ_TITLE+"x", TEST_WEBPROJ_DESCRIPTION+"x", TEST_WEBPROJ_DEFAULT_WEBAPP+"x", TEST_WEBPROJ_USE_AS_TEMPLATE, null);
            fail("Shouldn't allow duplicate web project dns/store name");
        }
        catch (AlfrescoRuntimeException exception)
        {
            // Expected
        }
        
        // Duplicate web project folder/name
        try
        {
            // Try to create duplicate web project folder/name (-ve test)
            wpService.createWebProject(TEST_WEBPROJ_DNS+"x", TEST_WEBPROJ_NAME+"-create", TEST_WEBPROJ_TITLE+"x", TEST_WEBPROJ_DESCRIPTION+"x", TEST_WEBPROJ_DEFAULT_WEBAPP+"x", TEST_WEBPROJ_USE_AS_TEMPLATE, null);
            fail("Shouldn't allow duplicate web project folder/name");
        }
        catch (DuplicateChildNodeNameException exception)
        {
            // Expected
        }
        
        // Mangled case
        String dnsName = TEST_WEBPROJ_DNS+"-a.b.c";
        String name = dnsName + " name";
        String mangledDnsName = TEST_WEBPROJ_DNS+"-a-b-c";
        
        wpInfo = wpService.createWebProject(dnsName, name, TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE, null);
        checkWebProjectInfo(wpInfo, mangledDnsName, name, TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE);
        wpInfo = wpService.getWebProject(mangledDnsName);
        checkWebProjectInfo(wpInfo, mangledDnsName, name, TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE);

        // Another mangled case
        dnsName = TEST_WEBPROJ_DNS+"-0é1í2ó3ú4";
        mangledDnsName = TEST_WEBPROJ_DNS+"-0-1-2-3-4";
        
        name = dnsName + " name";
        
        wpInfo = wpService.createWebProject(dnsName, name, TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE, null);
        checkWebProjectInfo(wpInfo, mangledDnsName, name, TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE);
        wpInfo = wpService.getWebProject(mangledDnsName);
        checkWebProjectInfo(wpInfo, mangledDnsName, name, TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE);
        
        // Invalid dns name (with '--')
        dnsName = "my--dns";
        name = dnsName + " name";
        try
        {
            // Try to create invalid web project with invalid dns name (-ve test)
            wpInfo = wpService.createWebProject(dnsName, name, TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE, null);
            fail("Shouldn't be able to create web project with '--'");
        }
        catch (IllegalArgumentException exception)
        {
            // Expected
        }
        
        // Invalid mangled case
        dnsName = "!£$%^&*()_+=-[]{}"; // generates mangled dns name = x---------------x
        name = dnsName + " name";
        try
        {
            // Try to create invalid web project (-ve test)
            wpInfo = wpService.createWebProject(dnsName, name, TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE, null);
            fail("Shouldn't be able to create web project with '--'");
        }
        catch (IllegalArgumentException exception)
        {
            // Expected
        }
    }
    
    // note: requires "add_children" rights on "Web Projects" root space
    // eg. DM coordinator, collaborator or contributor (not editor or consumer)
    public void testCreateWebProjectAsNonAdmin()
    {
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        try
        {
            // Try to create web project (-ve test)
            wpService.createWebProject(TEST_WEBPROJ_DNS+"-createAsNonAdmin", TEST_WEBPROJ_NAME+"-createAsNonAdmin", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE, null);
            fail("Shouldn't allow anyone to create web project by default");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Switch back to Admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        NodeRef wpRootNodeRef = wpService.getWebProjectsRoot();
        
        // note: implies "coordinator", "collaborator" or "contributor" (not "editor" or "consumer") - see permissionsDefinition.xml
        permissionService.setPermission(wpRootNodeRef, USER_ONE, PermissionService.ADD_CHILDREN, true);
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // Create a web project
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-createAsNonAdmin", TEST_WEBPROJ_NAME+"-createAsNonAdmin", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE, null);
        checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-createAsNonAdmin", TEST_WEBPROJ_NAME+"-createAsNonAdmin", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE);
        
        // test list and invite users
        assertEquals(1, wpService.listWebUsers(wpInfo.getStoreId()).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpInfo.getStoreId()).get(USER_ONE));
        
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_TWO, WCMUtil.ROLE_CONTENT_PUBLISHER);
        
        assertEquals(2, wpService.listWebUsers(wpInfo.getStoreId()).size());
        assertEquals(WCMUtil.ROLE_CONTENT_PUBLISHER, wpService.listWebUsers(wpInfo.getStoreId()).get(USER_TWO));
        
        // Switch back to Admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        permissionService.setPermission(wpRootNodeRef, USER_TWO, PermissionService.EDITOR, true);
        permissionService.setPermission(wpRootNodeRef, USER_THREE, PermissionService.CONSUMER, true);
        
        permissionService.setPermission(wpRootNodeRef, USER_FOUR, PermissionService.COORDINATOR, true);
        permissionService.setPermission(wpRootNodeRef, USER_FIVE, PermissionService.CONTRIBUTOR, true);
        permissionService.setPermission(wpRootNodeRef, USER_SIX, "Collaborator", true);
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        try
        {
            // Try to create web project with "editor" rights to web project root (-ve test)
            wpService.createWebProject(TEST_WEBPROJ_DNS+"-ano", TEST_WEBPROJ_NAME+"-ano", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE, null);
            fail("Shouldn't allow anyone to create web project by default");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Switch to USER_THREE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_THREE);
        
        try
        {
            // Try to create web project with "comsumer" rights to web project root (-ve test)
            wpService.createWebProject(TEST_WEBPROJ_DNS+"-ano", TEST_WEBPROJ_NAME+"-ano", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE, null);
            fail("Shouldn't allow anyone to create web project by default");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Switch to USER_FOUR
        AuthenticationUtil.setFullyAuthenticatedUser(USER_FOUR);
        
        // Create a web project
        wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-createAsCoordinator", TEST_WEBPROJ_NAME+"-createAsCoordinator", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE, null);
        checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-createAsCoordinator", TEST_WEBPROJ_NAME+"-createAsCoordinator", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE);

    }
    
    private void checkWebProjectInfo(WebProjectInfo wpInfo, String expectedStoreId, String expectedName, String expectedTitle, 
                                     String expectedDescription, String expectedDefaultWebApp, boolean expectedUseAsTemplate)
    {
        assertNotNull(wpInfo);
        assertEquals(expectedStoreId, wpInfo.getStoreId());
        assertEquals(expectedName, wpInfo.getName());
        assertEquals(expectedTitle, wpInfo.getTitle());
        assertEquals(expectedDescription, wpInfo.getDescription());
        assertEquals(expectedDefaultWebApp, wpInfo.getDefaultWebApp());
        assertEquals(expectedUseAsTemplate, wpInfo.isTemplate());
        assertNotNull(wpInfo.getNodeRef());
    }
    
    public void testListWebProjects() throws Exception
    {
        // As admin, check for existing projects
        List<WebProjectInfo> webProjects = wpService.listWebProjects();
        assertNotNull(webProjects);
        int cnt = webProjects.size();

        // Create some web projects
        wpService.createWebProject(TEST_WEBPROJ_DNS+"-list1", TEST_WEBPROJ_NAME+" list1", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        wpService.createWebProject(TEST_WEBPROJ_DNS+"-list2", TEST_WEBPROJ_NAME+" list2", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, true, null);
        wpService.createWebProject(TEST_WEBPROJ_DNS+"-list3", TEST_WEBPROJ_NAME+" list3", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        wpService.createWebProject(TEST_WEBPROJ_DNS+"-list4", TEST_WEBPROJ_NAME+" list4", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, true, null);
        
        // Get all the web projects for the current user
        webProjects = wpService.listWebProjects();
        assertNotNull(webProjects);
        assertEquals((cnt + 4), webProjects.size());
        
        // Do detailed check of the web project info objects
        for (WebProjectInfo wpInfo : webProjects)
        {
            String wpStoreId = wpInfo.getStoreId();
            if (wpStoreId.equals(TEST_WEBPROJ_DNS+"-list1") == true)
            {
                checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-list1", TEST_WEBPROJ_NAME+" list1", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, WCMUtil.DIR_ROOT, false);
            }
            else if (wpStoreId.equals(TEST_WEBPROJ_DNS+"-list2") == true)
            {
                checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-list2", TEST_WEBPROJ_NAME+" list2", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, true);
            }
            else if (wpStoreId.equals(TEST_WEBPROJ_DNS+"-list3") == true)
            {
                checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-list3", TEST_WEBPROJ_NAME+" list3", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, WCMUtil.DIR_ROOT, false);
            }
            else if (wpStoreId.equals(TEST_WEBPROJ_DNS+"-list4") == true)
            {
                checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-list4", TEST_WEBPROJ_NAME+" list4", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, true);              
            }
            else
            {
                //fail("The web project store id " + wpStoreId + " is not recognised");
                System.out.println("The web project store id " + wpStoreId + " is not recognised");
            }
        }
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // Get all the web projects for the current user
        webProjects = wpService.listWebProjects();
        assertNotNull(webProjects);
        
        if (! webProjects.isEmpty())
        {
            for (WebProjectInfo wpInfo : webProjects)
            {
                String wpStoreId = wpInfo.getStoreId();
                if (wpStoreId.equals(TEST_WEBPROJ_DNS+"-list-1") == true)
                {
                    fail("User should not see "+TEST_WEBPROJ_DNS+"-list-1");
                }
                else if (wpStoreId.equals(TEST_WEBPROJ_DNS+"-list-2") == true)
                {
                    fail("User should not see "+TEST_WEBPROJ_DNS+"-list-2");
                }
                else if (wpStoreId.equals(TEST_WEBPROJ_DNS+"-list-3") == true)
                {
                    fail("User should not see "+TEST_WEBPROJ_DNS+"-list-3");
                }
                else if (wpStoreId.equals(TEST_WEBPROJ_DNS+"-list-4") == true)
                {
                    fail("User should not see "+TEST_WEBPROJ_DNS+"-list-4");
                }
            }
        }
        
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        webProjects = wpService.listWebProjects();
        cnt = webProjects.size();
        
        NodeRef wpRoot = wpService.getWebProjectsRoot();
        
        fileFolderService.create(wpRoot, "a folder "+TEST_RUN, ContentModel.TYPE_FOLDER);
        fileFolderService.create(wpRoot, "some content "+TEST_RUN, ContentModel.TYPE_CONTENT);
        
        webProjects = wpService.listWebProjects();
        assertEquals(cnt, webProjects.size());
    }
    
    public void testGetWebProject()
    {
        // Get a web project that isn't there
        WebProjectInfo wpInfo = wpService.getWebProject(TEST_WEBPROJ_DNS+"-get");
        assertNull(wpInfo);
        
        // Create a web project
        wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-get", TEST_WEBPROJ_NAME+"-get", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE, null);
        
        // Get the web project - test using wpStoreId
        wpInfo = wpService.getWebProject(wpInfo.getStoreId());
        assertNotNull(wpInfo);
        checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-get", TEST_WEBPROJ_NAME+"-get", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE);
        
        // Get the web project - test using wpStoreNodeRef
        wpInfo = wpService.getWebProject(wpInfo.getNodeRef());
        assertNotNull(wpInfo);
        checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-get", TEST_WEBPROJ_NAME+"-get", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_USE_AS_TEMPLATE);
    }
    
    public void testUpdateWebProject()
    {
        WebProjectInfo wpInfo = new WebProjectInfoImpl(TEST_WEBPROJ_DNS+"-update", TEST_WEBPROJ_NAME+"-update", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, false, null, null);
        
        try
        {
            // Try to update a web project that isn't there (-ve test)
            wpService.updateWebProject(wpInfo);
            fail("Shouldn't be able to update a web project that does not exist");
        }
        catch (AlfrescoRuntimeException exception)
        {
            // Expected
        }
        
        // Create a test web project
        wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-update", TEST_WEBPROJ_NAME+"-update", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, true, null);
        
        wpInfo.setName("changedName"+TEST_RUN);
        wpInfo.setTitle("changedTitle");
        wpInfo.setDescription("changedDescription");
        wpInfo.setIsTemplate(false);
        
        // Update the details of the web project
        wpService.updateWebProject(wpInfo);
        wpInfo = wpService.getWebProject(wpInfo.getStoreId());
        checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-update", "changedName"+TEST_RUN, "changedTitle", "changedDescription", TEST_WEBPROJ_DEFAULT_WEBAPP, false);    
    }
    
    public void testDeleteWebProject()
    {
        // Create a test web project
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-delete", TEST_WEBPROJ_NAME+"-delete", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, true, null);
        String wpStoreId = wpInfo.getStoreId();
        assertNotNull(wpService.getWebProject(wpStoreId));
        
        // Create ANOther web project
        WebProjectInfo wpAnoInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-delete ano", TEST_WEBPROJ_NAME+"-delete ano", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, true, null);
        String wpStoreAnoId = wpAnoInfo.getStoreId();
        
        assertEquals(2, sbService.listSandboxes(wpStoreId).size());
        assertEquals(2, sbService.listSandboxes(wpStoreAnoId).size());
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        try
        {
            // Try to delete the web project as a non-content-manager (-ve test)
            wpService.deleteWebProject(wpStoreId);
            fail("Shouldn't be able to delete the web project since not a content manager");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Switch user to System
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        try
        {
            // Try to delete the web project as a non-content-manager (such as System) (-ve test)
            wpService.deleteWebProject(wpStoreId);
            fail("Shouldn't be able to delete the web project since not a content manager");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Switch back to admin
        String adminUser = AuthenticationUtil.getAdminUserName();
        AuthenticationUtil.setFullyAuthenticatedUser(adminUser);
        
        String defaultWebApp = wpInfo.getDefaultWebApp();
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String authorSandboxId = sbInfo.getSandboxId();
        String authorSandboxPath = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        for (int i = 1; i <= 10; i++)
        {
            assetService.createFile(authorSandboxId, authorSandboxPath, "myFile-"+i, null);
            
            String relPath = authorSandboxPath + "/" + "myFile-"+i;
            assertEquals(adminUser, avmLockingService.getLockOwner(wpStoreId, relPath));
        }
        
        // Delete the web project
        wpService.deleteWebProject(wpStoreId);
        assertNull(wpService.getWebProject(wpStoreId));
        
        // Check locks have been removed
        for (int i = 1; i <= 10; i++)
        {
            String relPath = authorSandboxPath + "/" + "myFile-"+i;
            assertNull("Lock still exists: "+relPath, avmLockingService.getLockOwner(wpStoreId, relPath));
        }
        
        assertEquals(0, sbService.listSandboxes(wpStoreId).size());
        assertEquals(2, sbService.listSandboxes(wpStoreAnoId).size());
        
        try
        {
            // Try to delete a web project that isn't there (-ve test)
            wpService.deleteWebProject("someRandomWebProject");
            fail("Shouldn't be able to delete the web project since it does not exist (or is not visible to current user)");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Create another test web project
        wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-delete2", TEST_WEBPROJ_NAME+"-delete2", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, true, null);
        assertNotNull(wpService.getWebProject(wpInfo.getStoreId()));
        
        wpService.inviteWebUser(wpInfo.getNodeRef(), USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, false);
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        try
        {
            // Try to delete a web project that isn't there (-ve test)
            wpService.deleteWebProject(TEST_WEBPROJ_DNS+"-delete2");
            fail("Shouldn't be able to delete the web project since it does not exist (or is not visible to current user)");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // Delete the web project
        wpService.deleteWebProject(TEST_WEBPROJ_DNS+"-delete2");
        
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // Create another test web project
        wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-delete3", TEST_WEBPROJ_NAME+"-delete3", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, true, null);
        wpStoreId = wpInfo.getStoreId();
        NodeRef wpNodeRef = wpInfo.getNodeRef();
        
        defaultWebApp = wpInfo.getDefaultWebApp();
        
        sbInfo = sbService.getAuthorSandbox(wpStoreId);
        authorSandboxId = sbInfo.getSandboxId();
        
        // no changes yet
        List<AssetInfo> assets = sbService.listChangedAll(authorSandboxId, true);
        assertEquals(0, assets.size());
        
        authorSandboxPath = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        for (int i = 1; i <= 100; i++)
        {
            assetService.createFile(authorSandboxId, authorSandboxPath, "myFile-"+i, null);
        }
        
        sbService.submitAll(authorSandboxId, "s1", "s2");
        
        // delete immediately - don't wait for async submit to finish - this should leave an in-flight workflow
        
        wpService.deleteWebProject(wpStoreId);
        
        List<AVMStoreDescriptor> avmStores = avmService.getStores();
        for (AVMStoreDescriptor avmStore : avmStores)
        {
            assertFalse("Unexpected store: "+avmStore.getName(), avmStore.getName().startsWith(wpStoreId));
        }
        
        NodeRef wpArchiveNodeRef = new NodeRef(nodeService.getStoreArchiveNode(wpNodeRef.getStoreRef()).getStoreRef(), wpNodeRef.getId());
        assertFalse(nodeService.exists(wpArchiveNodeRef));
        
        // TODO add more tests when WCM services explicitly support WCM workflows (eg. submit approval)
    }
    
    public void testCreateWebApp()
    {
        // Create a web project with a default webapp
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-createWebApp", TEST_WEBPROJ_NAME+"-createWebApp", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBAPP1, TEST_WEBPROJ_USE_AS_TEMPLATE, null);
        
        // Switch user to System
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        try
        {
            // Try to create another webapp as a non-content-manager (such as System) (-ve test)
            wpService.createWebApp(wpInfo.getStoreId(), TEST_WEBAPP2, TEST_WEBPROJ_DESCRIPTION);
            fail("Shouldn't be able to create a webapp since not a content manager");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // Create another webapp - test using wpStoreId
        wpService.createWebApp(wpInfo.getStoreId(), TEST_WEBAPP2, TEST_WEBPROJ_DESCRIPTION);
        
        try
        {
            // Try to create duplicate webapp (-ve test)
            wpService.createWebApp(wpInfo.getStoreId(), TEST_WEBAPP2, TEST_WEBPROJ_DESCRIPTION);
            fail("Shouldn't allow duplicate webapp name");
        }
        catch (AVMExistsException exception)
        {
            // Expected
        }
        
        // Create another webapp - test using wpNodeRef
        wpService.createWebApp(wpInfo.getNodeRef(), TEST_WEBAPP3, TEST_WEBPROJ_DESCRIPTION);
    }
    
    public void testListWebApps()
    {
        try
        {
            // Try to list webapps (-ve test)
            wpService.listWebApps(new NodeRef("dummy://dummy/dummy"));
            fail("Shouldn't be able to list webapps for a non-existent web project");
        }
        catch (IllegalArgumentException exception)
        {
            // Expected
        }
        
        // Create a web project with default ROOT webapp
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-listWebApps", TEST_WEBPROJ_NAME+"-listWebApps", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        NodeRef wpNodeRef = wpInfo.getNodeRef();
        
        // List web apps - test using wpStoreId
        List<String> webAppNames = wpService.listWebApps(wpInfo.getStoreId());
        assertNotNull(webAppNames);
        assertEquals(1, webAppNames.size());
        assertTrue(webAppNames.contains(WCMUtil.DIR_ROOT));
        
        // List web apps - test using wpNodeRef
        webAppNames = wpService.listWebApps(wpNodeRef);
        assertNotNull(webAppNames);
        assertEquals(1, webAppNames.size());
        assertTrue(webAppNames.contains(WCMUtil.DIR_ROOT));
        
        // Create some more webapps
        wpService.createWebApp(wpNodeRef, TEST_WEBAPP1, TEST_WEBPROJ_DESCRIPTION);
        wpService.createWebApp(wpNodeRef, TEST_WEBAPP2, TEST_WEBPROJ_DESCRIPTION);
        wpService.createWebApp(wpNodeRef, TEST_WEBAPP3, TEST_WEBPROJ_DESCRIPTION);
        
        webAppNames = wpService.listWebApps(wpNodeRef);
        assertEquals(4, webAppNames.size());
        assertTrue(webAppNames.contains(WCMUtil.DIR_ROOT));
        assertTrue(webAppNames.contains(TEST_WEBAPP1));
        assertTrue(webAppNames.contains(TEST_WEBAPP2));
        assertTrue(webAppNames.contains(TEST_WEBAPP3));
        
        wpService.deleteWebApp(wpNodeRef, TEST_WEBAPP1);
        wpService.deleteWebApp(wpNodeRef, TEST_WEBAPP2);
        
        webAppNames = wpService.listWebApps(wpNodeRef);
        assertEquals(2, webAppNames.size());
        assertTrue(webAppNames.contains(WCMUtil.DIR_ROOT));
        assertTrue(webAppNames.contains(TEST_WEBAPP3));
    }
    
    public void testDeleteWebApp()
    {
        // Create a webapp
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-deleteWebApp", TEST_WEBPROJ_NAME+"-deleteWebApp", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBAPP1, TEST_WEBPROJ_USE_AS_TEMPLATE, null);
       
        String wpStoreId = wpInfo.getStoreId();
        NodeRef wpNodeRef = wpInfo.getNodeRef();
        
        // Create a webapp
        wpService.createWebApp(wpNodeRef, TEST_WEBAPP2, TEST_WEBPROJ_DESCRIPTION);
        
        // Create another webapp
        wpService.createWebApp(wpNodeRef, TEST_WEBAPP3, TEST_WEBPROJ_DESCRIPTION);
        
        // Switch user to System
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
               
        try
        {
            // Try to delete the webapp a non-content-manager (such as System) (-ve test)
            wpService.deleteWebApp(wpNodeRef, TEST_WEBAPP2);
            fail("Shouldn't be able to delete the webapp since not a content manager");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        try
        {
            // Try to delete default webapp (-ve test)
            wpService.deleteWebApp(wpNodeRef, TEST_WEBAPP1);
            fail("Shouldn't be able to delete the default webapp");
        }
        catch (AlfrescoRuntimeException exception)
        {
            // Expected
        }
        
        // Change default webapp
        wpInfo = wpService.getWebProject(wpNodeRef);
        wpInfo.setDefaultWebApp(TEST_WEBAPP3);
        wpService.updateWebProject(wpInfo);
        
        // Delete non-default webapp - test using wpStoreId
        wpService.deleteWebApp(wpStoreId, TEST_WEBAPP1);
        
        // Delete another webapp - test using wpNodeRef
        wpService.deleteWebApp(wpNodeRef, TEST_WEBAPP2);
        
        try
        {
            // Try to delete last / default webapp (-ve test)
            wpService.deleteWebApp(wpNodeRef, TEST_WEBAPP3);
            fail("Shouldn't be able to delete the default webapp");
        }
        catch (AlfrescoRuntimeException exception)
        {
            // Expected
        }
        
        // TODO - Test delete of non-empty webapp
        
        try
        {
            // Try to delete a webapp that does not exist (-ve test)
            wpService.deleteWebApp(wpNodeRef, "someRandomWebApp");
            fail("Shouldn't be able to delete the webapp since it does not exist");
        }
        catch (AVMNotFoundException exception)
        {
            // Expected
        }
    }
    
    /**
     * Test inviteWebUsers
     */
    public void testMultiInviteAndListWebUsers()
    {
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
              
        List<WebProjectInfo> webProjects = wpService.listWebProjects();
        assertNotNull(webProjects);
        int userOneWebProjectCount = webProjects.size();
        
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // Create a web project
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-inviteWebUsers", TEST_WEBPROJ_NAME+"-inviteWebUsers", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        NodeRef wpNodeRef = wpInfo.getNodeRef();
        
        assertEquals(1, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(AuthenticationUtil.getAdminUserName()));
        
        Map<String, String> userGroupRoles = new HashMap<String, String>();
        userGroupRoles.put(USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER);
        userGroupRoles.put(USER_TWO, WCMUtil.ROLE_CONTENT_PUBLISHER);
        
        // Invite web users - test using wpStoreId
        wpService.inviteWebUsersGroups(wpInfo.getStoreId(), userGroupRoles);
        
        userGroupRoles = new HashMap<String, String>();
        userGroupRoles.put(USER_THREE, WCMUtil.ROLE_CONTENT_REVIEWER);
        
        // Invite web users - test using wpNodeRef
        wpService.inviteWebUsersGroups(wpNodeRef, userGroupRoles, false);

        assertEquals(4, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(AuthenticationUtil.getAdminUserName()));
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(USER_ONE));
        assertEquals(WCMUtil.ROLE_CONTENT_PUBLISHER, wpService.listWebUsers(wpNodeRef).get(USER_TWO));
        assertEquals(WCMUtil.ROLE_CONTENT_REVIEWER, wpService.listWebUsers(wpNodeRef).get(USER_THREE));
        
        // Switch to USER_ONE (a content manager for this web project)
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        webProjects = wpService.listWebProjects();
        assertEquals(userOneWebProjectCount+1, webProjects.size());
        
        // Start: Test fix ETWOTWO-567
        
        // Test newly invited content manager can invite other
        userGroupRoles = new HashMap<String, String>();
        userGroupRoles.put(USER_FIVE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR);
        
        wpService.inviteWebUsersGroups(wpNodeRef, userGroupRoles, false);
        
        // Finish: Test fix ETWOTWO-567
        
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // Create a web project
        wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-inviteWebUsers2", TEST_WEBPROJ_NAME+"-inviteWebUsers2", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        wpNodeRef = wpInfo.getNodeRef();
        
        assertEquals(1, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(AuthenticationUtil.getAdminUserName()));
        
        userGroupRoles = new HashMap<String, String>();
 
        userGroupRoles.put(authorityService.getName(AuthorityType.GROUP, GROUP_ONE), WCMUtil.ROLE_CONTENT_PUBLISHER);

        // Invite group as a set of (flattened) web users - test using wpStoreId
        wpService.inviteWebUsersGroups(wpInfo.getStoreId(), userGroupRoles);

        assertEquals(3, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(AuthenticationUtil.getAdminUserName()));
        assertEquals(WCMUtil.ROLE_CONTENT_PUBLISHER, wpService.listWebUsers(wpNodeRef).get(USER_ONE));
        assertEquals(WCMUtil.ROLE_CONTENT_PUBLISHER, wpService.listWebUsers(wpNodeRef).get(USER_TWO));
    }
    
    /**
     * Test inviteWebUser - and listWebProjects / listWebUsers / isWebUser
     */
    public void testInviteAndListWebUsers()
    {
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
              
        List<WebProjectInfo> webProjects = wpService.listWebProjects();
        assertNotNull(webProjects);
        int userOneWebProjectCount = webProjects.size();
        
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // Create a web project
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-inviteWebUser1", TEST_WEBPROJ_NAME+"-inviteWebUser1", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        NodeRef wpNodeRef = wpInfo.getNodeRef();
        
        assertTrue(wpService.isWebUser(wpNodeRef, AuthenticationUtil.getAdminUserName()));
        assertFalse(wpService.isWebUser(wpNodeRef, USER_ONE));
        
        assertEquals(1, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(AuthenticationUtil.getAdminUserName()));
        
        // Invite one web user - test using wpStoreId
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_PUBLISHER);
        
        // Invite one web user - test using wpNodeRef
        wpService.inviteWebUser(wpNodeRef, USER_TWO, WCMUtil.ROLE_CONTENT_MANAGER, true);
        
        assertEquals(3, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_PUBLISHER, wpService.listWebUsers(wpNodeRef).get(USER_ONE));
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(USER_TWO));
 
        assertTrue(wpService.isWebUser(wpInfo.getStoreId(), USER_ONE));
        assertTrue(wpService.isWebUser(wpNodeRef, USER_TWO));
        
        // Create another web project
        wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-inviteWebUser2", TEST_WEBPROJ_NAME+"-inviteWebUser2", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        NodeRef wpNodeRef2 = wpInfo.getNodeRef();
        
        assertEquals(1, wpService.listWebUsers(wpNodeRef2).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef2).get(AuthenticationUtil.getAdminUserName()));
        
        assertFalse(wpService.isWebUser(wpInfo.getStoreId(), USER_ONE));
        assertFalse(wpService.isWebUser(wpNodeRef2, USER_TWO));
        
        wpService.inviteWebUser(wpNodeRef2, USER_TWO, WCMUtil.ROLE_CONTENT_PUBLISHER, false);
        wpService.inviteWebUser(wpNodeRef2, USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, false);
        
        assertEquals(3, wpService.listWebUsers(wpNodeRef2).size());
        assertEquals(WCMUtil.ROLE_CONTENT_PUBLISHER, wpService.listWebUsers(wpNodeRef2).get(USER_TWO));
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef2).get(USER_ONE));
        
        assertTrue(wpService.isWebUser(wpInfo.getStoreId(), USER_ONE));
        assertTrue(wpService.isWebUser(wpNodeRef2, USER_TWO));
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
         
        webProjects = wpService.listWebProjects();
        assertEquals(userOneWebProjectCount+2, webProjects.size());
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
          
        try
        {
            // Try to invite web user as a non-content-manager (-ve test)
            wpService.inviteWebUser(wpNodeRef2, USER_THREE, WCMUtil.ROLE_CONTENT_REVIEWER, false);
            fail("Shouldn't be able to invite web user since not a content manager");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }

        /*  System can invite due to ALFCOM-2388 - need to review System in general
        // Switch user to System
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                 
        try
        {
            // Try to invite web user as a non-content-manager (such as System) (-ve test)
            wpService.inviteWebUser(wpNodeRef2, USER_THREE, WCMUtil.ROLE_CONTENT_REVIEWER, false);
            fail("Shouldn't be able to invite web user since not a content manager");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        */
        
        // Test newly invited content manager can invite other
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        assertFalse(wpService.isWebUser(wpNodeRef2, USER_THREE));
        
        // Invite web user
        wpService.inviteWebUser(wpNodeRef2, USER_THREE, WCMUtil.ROLE_CONTENT_REVIEWER, false);
        
        assertTrue(wpService.isWebUser(wpNodeRef2, USER_THREE));
        
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        assertEquals(4, wpService.listWebUsers(wpNodeRef2).size());
        assertEquals(WCMUtil.ROLE_CONTENT_REVIEWER, wpService.listWebUsers(wpNodeRef2).get(USER_THREE));
    }
    
    /**
     * Test uninviteWebUser - and listWebProjects / listWebUsers / isWebUser
     */
    public void testUninviteAndListWebUsers()
    {
        // Switch to USER_FOUR
        AuthenticationUtil.setFullyAuthenticatedUser(USER_FOUR);
        
        List<WebProjectInfo> webProjects = wpService.listWebProjects();
        assertNotNull(webProjects);
        int userFourWebProjectCount = webProjects.size();
        
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // Create a web project
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-uninviteWebUser", TEST_WEBPROJ_NAME+"-uninviteWebUser", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        NodeRef wpNodeRef = wpInfo.getNodeRef();
        
        assertEquals(1, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(AuthenticationUtil.getAdminUserName()));
        
        assertTrue(wpService.isWebUser(wpNodeRef, AuthenticationUtil.getAdminUserName()));
        assertFalse(wpService.isWebUser(wpNodeRef, USER_FOUR));
        assertFalse(wpService.isWebUser(wpNodeRef, USER_ONE));
        
        wpService.inviteWebUser(wpNodeRef, USER_FOUR, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, false);
        
        assertEquals(2, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_CONTRIBUTOR, wpService.listWebUsers(wpNodeRef).get(USER_FOUR));
        assertTrue(wpService.isWebUser(wpNodeRef, USER_FOUR));
        
        wpService.inviteWebUser(wpNodeRef, USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, false);
        
        assertEquals(3, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(USER_ONE));
        assertTrue(wpService.isWebUser(wpNodeRef, USER_ONE));
        
        // Switch to USER_FOUR
        AuthenticationUtil.setFullyAuthenticatedUser(USER_FOUR);
                
        webProjects = wpService.listWebProjects();
        assertEquals(userFourWebProjectCount+1, webProjects.size());
        
        // Switch to USER_TWO
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
                 
        try
        {
            // Try to uninvite web user as a non-content-manager (-ve test)
            wpService.uninviteWebUser(wpNodeRef, USER_FOUR, false);
            fail("Shouldn't be able to uninvite web user since not a content manager");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Switch user to System
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        try
        {
            // Try to uninvite web user as a non-content-manager (such as System) (-ve test)
            wpService.uninviteWebUser(wpNodeRef, USER_FOUR, false);
            fail("Shouldn't be able to uninvite web user since not a content manager");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // Uninvite web user - test using wpStoreId
        wpService.uninviteWebUser(wpInfo.getStoreId(), USER_FOUR);
        
        assertEquals(2, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(null, wpService.listWebUsers(wpNodeRef).get(USER_FOUR));
        assertFalse(wpService.isWebUser(wpNodeRef, USER_FOUR));
        
        // Switch to USER_FOUR
        AuthenticationUtil.setFullyAuthenticatedUser(USER_FOUR);
           
        webProjects = wpService.listWebProjects();
        assertEquals(userFourWebProjectCount, webProjects.size());
        
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        // Content manager can uninvite themself
        // Uninvite web user - test using wpNodeRef
        wpService.uninviteWebUser(wpNodeRef, AuthenticationUtil.getAdminUserName(), false);
            
        // Note: All admin authorities are implicitly a web user and content manager (across all web projects) even if not explicitly invited
        assertEquals(1, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(null, wpService.listWebUsers(wpNodeRef).get(AuthenticationUtil.getAdminUserName()));        
        assertTrue(wpService.isWebUser(wpNodeRef, AuthenticationUtil.getAdminUserName()));
        assertTrue(wpService.isContentManager(wpNodeRef, AuthenticationUtil.getAdminUserName()));
        
        // Switch to USER_ONE
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        assertEquals(1, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(USER_ONE));
        
        // Delete user (in this case, last invited content manager)
        wpService.uninviteWebUser(wpNodeRef, USER_ONE, false);
        assertFalse(wpService.isWebUser(wpNodeRef, USER_ONE));
        
        try
        {
            // Try to delete the web project as a non-content-manager (-ve test)
            wpService.deleteWebProject(wpNodeRef);
            fail("Shouldn't be able to delete the web project since not a content manager");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
              
        // Switch back to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // Note: All admin authorities are implicitly a web user and content manager (across all web projects) even if not explicitly invited
        assertEquals(0, wpService.listWebUsers(wpNodeRef).size());
        assertTrue(wpService.isWebUser(wpNodeRef, AuthenticationUtil.getAdminUserName()));
        assertTrue(wpService.isContentManager(wpInfo.getStoreId(), AuthenticationUtil.getAdminUserName()));
        
        // delete web project
        wpService.deleteWebProject(wpNodeRef);
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
            for (int j = 1; j <= SCALE_WEBAPPS; j++)
            {
                wpService.createWebApp(wpInfo.getNodeRef(), TEST_WEBAPP+"-"+j, TEST_WEBAPP+"-"+j);
            }
        }
        
        System.out.println("testPseudoScaleTest: created additional "+SCALE_WEBAPPS+" web apps in each of "+SCALE_WEBPROJECTS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");
      
        split = System.currentTimeMillis();
        
        for (int i = 1; i <= SCALE_WEBPROJECTS; i++)
        {
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_WEBPROJ_DNS+"-"+i);
            Map<String, String> userRoles = new HashMap<String, String>(SCALE_USERS);
            for (int j = 1; j <= SCALE_USERS; j++)
            {
                userRoles.put(TEST_USER+"-"+j, WCMUtil.ROLE_CONTENT_MANAGER);
            }
            wpService.inviteWebUsersGroups(wpInfo.getNodeRef(), userRoles, false);
        }
        
        System.out.println("testPseudoScaleTest: invited "+SCALE_USERS+" content managers to each of "+SCALE_WEBPROJECTS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");

        split = System.currentTimeMillis();
        
        for (int i = 1; i <= SCALE_USERS; i++)
        {
            assertEquals(SCALE_WEBPROJECTS, wpService.listWebProjects(TEST_USER+"-"+i).size());
        }
        
        System.out.println("testPseudoScaleTest: list web projects for "+SCALE_USERS+" content managers in "+(System.currentTimeMillis()-split)+" msecs");

        split = System.currentTimeMillis();
        
        for (int i = 1; i <= SCALE_WEBPROJECTS; i++)
        {
            WebProjectInfo wpInfo = wpService.getWebProject(TEST_WEBPROJ_DNS+"-"+i);
            wpService.deleteWebProject(wpInfo.getNodeRef());
        }
        
        System.out.println("testPseudoScaleTest: deleted "+SCALE_USERS+" web projects in "+(System.currentTimeMillis()-split)+" msecs");

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
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/wcm/script/test_webProjectService.js");
        scriptService.executeScript(location, new HashMap<String, Object>(0));
    }
    */
}
