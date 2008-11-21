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
package org.alfresco.wcm.webproject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.alfresco.wcm.util.WCMUtil;
import org.springframework.context.ApplicationContext;

/**
 * Web Project Service implementation unit test
 * 
 * @author janv
 */
public class WebProjectServiceImplTest extends TestCase 
{
    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    //
    // test data
    //
    
    private static final String TEST_RUN = ""+System.currentTimeMillis();
    private static final boolean CLEAN = true; // cleanup during teardown
    
    // base web project dns / name 
    private static final String TEST_WEBPROJ_DNS  = "testWebProjDNS-"+TEST_RUN;
    private static final String TEST_WEBPROJ_NAME = "test Web Project Display Name - "+TEST_RUN;
    
    // web app names
    private static final String TEST_WEBAPP = "myWebApp";
    
    private static final String TEST_WEBAPP1 = TEST_WEBAPP+"-AppOne";
    private static final String TEST_WEBAPP2 = TEST_WEBAPP+"-AppTwo";
    private static final String TEST_WEBAPP3 = TEST_WEBAPP+"-AppThree";
    
    // general
    private static final String TEST_TITLE = "This is my title";
    private static final String TEST_DESCRIPTION = "This is my description";
    private static final String TEST_DEFAULT_WEBAPP = "defWebApp";
    private static final boolean TEST_USE_AS_TEMPLATE = true;
    
    private static final String USER_ADMIN = "admin";
    
    private static final String TEST_USER = "testWebProjUser-"+TEST_RUN;
    private static final String TEST_GROUP = "testWebProjGroup-"+TEST_RUN;
    
    private static final String USER_ONE   = TEST_USER+"-One";
    private static final String USER_TWO   = TEST_USER+"-Two";
    private static final String USER_THREE = TEST_USER+"-Three";
    private static final String USER_FOUR  = TEST_USER+"-Four";
    private static final String USER_FIVE  = TEST_USER+"-Five";
    
    private static final String GROUP_ONE  = TEST_GROUP+"-One";
    
    private static final int SCALE_USERS = 5;
    private static final int SCALE_WEBPROJECTS = 5;
    private static final int SCALE_WEBAPPS = 5;
    
    
    //
    // services
    //
    
    private WebProjectService wpService;
    private AuthenticationService authenticationService;
    private PersonService personService;
    private FileFolderService fileFolderService;
    private AuthorityService authorityService;

    
    @Override
    protected void setUp() throws Exception
    {
        // Get the required services
        wpService = (WebProjectService)ctx.getBean("WebProjectService");
        authenticationService = (AuthenticationService)ctx.getBean("AuthenticationService");
        personService = (PersonService)ctx.getBean("PersonService");
        fileFolderService = (FileFolderService)ctx.getBean("FileFolderService");
        authorityService = (AuthorityService)ctx.getBean("AuthorityService");
       
        // By default run as Admin
        AuthenticationUtil.setCurrentUser(USER_ADMIN);
        
        createUser(USER_ONE);
        createUser(USER_TWO);
        createUser(USER_THREE);
        createUser(USER_FOUR);
        createUser(USER_FIVE);
        
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
            AuthenticationUtil.setCurrentUser(USER_ADMIN);
            
            List<WebProjectInfo> webProjects = wpService.listWebProjects();
            for (WebProjectInfo wpInfo : webProjects)
            {
                if (wpInfo.getStoreId().startsWith(TEST_WEBPROJ_DNS))
                {
                    wpService.deleteWebProject(wpInfo.getNodeRef());
                }
            }
            
            deleteGroup(GROUP_ONE);
            
            deleteUser(USER_ONE);
            deleteUser(USER_TWO);
            deleteUser(USER_THREE);
            deleteUser(USER_FOUR);
            deleteUser(USER_FIVE);
            
            NodeRef wpRoot = wpService.getWebProjectsRoot();
            List<FileInfo> list = fileFolderService.list(wpRoot);
            for (FileInfo fileOrFolder : list)
            {
                if (fileOrFolder.getName().contains(TEST_RUN))
                {
                    fileFolderService.delete(fileOrFolder.getNodeRef());
                }
            }
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
    
    private void createSimpleGroup(String shortName, Set<String> userNames)
    {
        String groupName = authorityService.getName(AuthorityType.GROUP, shortName);
        if (authorityService.authorityExists(groupName) == false)
        {
            authorityService.createAuthority(AuthorityType.GROUP, null, shortName);
            
            for (String userName : userNames)
            {
                authorityService.addAuthority(groupName, userName);
            }
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
    
    private void deleteGroup(String shortName)
    {
        String groupName = authorityService.getName(AuthorityType.GROUP, shortName);
        if (authorityService.authorityExists(groupName) == true)
        {
            authorityService.deleteAuthority(groupName);
        }
    }
    
    public void testCreateWebProjectSimple()
    {
        // Create a web project
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-simple", TEST_WEBPROJ_NAME+"-simple", TEST_TITLE, TEST_DESCRIPTION);
        assertNotNull(wpInfo);
    }
	
    public void testCreateWebProject()
    {
        // Create a web project
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-create", TEST_WEBPROJ_NAME+"-create", TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, TEST_USE_AS_TEMPLATE, null);
        checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-create", TEST_WEBPROJ_NAME+"-create", TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, TEST_USE_AS_TEMPLATE);
        
        // Duplicate web project dns/store name
        try
        {
            // Try to create duplicate web project dns/store (-ve test)
            wpService.createWebProject(TEST_WEBPROJ_DNS+"-create", TEST_WEBPROJ_NAME+"-x", TEST_TITLE+"x", TEST_DESCRIPTION+"x", TEST_DEFAULT_WEBAPP+"x", TEST_USE_AS_TEMPLATE, null);
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
            wpService.createWebProject(TEST_WEBPROJ_DNS+"x", TEST_WEBPROJ_NAME+"-create", TEST_TITLE+"x", TEST_DESCRIPTION+"x", TEST_DEFAULT_WEBAPP+"x", TEST_USE_AS_TEMPLATE, null);
            fail("Shouldn't allow duplicate web project folder/name");
        }
        catch (DuplicateChildNodeNameException exception)
        {
            // Expected
        }
        
        // Mangled case
        String dnsName = TEST_WEBPROJ_DNS+"some.unexpected.chars";
        String name = dnsName + " name";
        String mangledDnsName = TEST_WEBPROJ_DNS+"some-unexpected-chars";
        
        wpInfo = wpService.createWebProject(dnsName, name, TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, TEST_USE_AS_TEMPLATE, null);
        checkWebProjectInfo(wpInfo, mangledDnsName, name, TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, TEST_USE_AS_TEMPLATE);
        wpInfo = wpService.getWebProject(mangledDnsName);
        checkWebProjectInfo(wpInfo, mangledDnsName, name, TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, TEST_USE_AS_TEMPLATE);

        // Another mangled case
        dnsName = TEST_WEBPROJ_DNS+"some.moreé1í2ó3ú4Á5É6Í7Ó8Ú9";
        mangledDnsName = TEST_WEBPROJ_DNS+"some-more-1-2-3-4-5-6-7-8-9";
        
        name = dnsName + " name";
        
        wpInfo = wpService.createWebProject(dnsName, name, TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, TEST_USE_AS_TEMPLATE, null);
        checkWebProjectInfo(wpInfo, mangledDnsName, name, TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, TEST_USE_AS_TEMPLATE);
        wpInfo = wpService.getWebProject(mangledDnsName);
        checkWebProjectInfo(wpInfo, mangledDnsName, name, TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, TEST_USE_AS_TEMPLATE);
        
        // Invalid dns name (with '--')
        dnsName = "my--dns";
        name = dnsName + " name";
        try
        {
            // Try to create invalid web project with invalid dns name (-ve test)
            wpInfo = wpService.createWebProject(dnsName, name, TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, TEST_USE_AS_TEMPLATE, null);
            fail("Shouldn't be able to create web project with '--'");
        }
        catch (AlfrescoRuntimeException exception)
        {
            // Expected
        }
        
        // Invalid mangled case
        dnsName = "!£$%^&*()_+=-[]{}"; // generates mangled dns name = x---------------x
        name = dnsName + " name";
        try
        {
            // Try to create invalid web project (-ve test)
            wpInfo = wpService.createWebProject(dnsName, name, TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, TEST_USE_AS_TEMPLATE, null);
            fail("Shouldn't be able to create web project with '--'");
        }
        catch (AlfrescoRuntimeException exception)
        {
            // Expected
        }
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
        wpService.createWebProject(TEST_WEBPROJ_DNS+"-list1", TEST_WEBPROJ_NAME+" list1", TEST_TITLE, TEST_DESCRIPTION);
        wpService.createWebProject(TEST_WEBPROJ_DNS+"-list2", TEST_WEBPROJ_NAME+" list2", TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, true, null);
        wpService.createWebProject(TEST_WEBPROJ_DNS+"-list3", TEST_WEBPROJ_NAME+" list3", TEST_TITLE, TEST_DESCRIPTION);
        wpService.createWebProject(TEST_WEBPROJ_DNS+"-list4", TEST_WEBPROJ_NAME+" list4", TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, true, null);
        
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
                checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-list1", TEST_WEBPROJ_NAME+" list1", TEST_TITLE, TEST_DESCRIPTION, WCMUtil.DIR_ROOT, false);
            }
            else if (wpStoreId.equals(TEST_WEBPROJ_DNS+"-list2") == true)
            {
                checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-list2", TEST_WEBPROJ_NAME+" list2", TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, true);
            }
            else if (wpStoreId.equals(TEST_WEBPROJ_DNS+"-list3") == true)
            {
                checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-list3", TEST_WEBPROJ_NAME+" list3", TEST_TITLE, TEST_DESCRIPTION, WCMUtil.DIR_ROOT, false);
            }
            else if (wpStoreId.equals(TEST_WEBPROJ_DNS+"-list4") == true)
            {
                checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-list4", TEST_WEBPROJ_NAME+" list4", TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, true);              
            }
            else
            {
                //fail("The web project store id " + wpStoreId + " is not recognised");
                System.out.println("The web project store id " + wpStoreId + " is not recognised");
            }
        }
        
        // Switch to USER_ONE
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
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
        AuthenticationUtil.setCurrentUser(USER_ADMIN);
        
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
        wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-get", TEST_WEBPROJ_NAME+"-get", TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, TEST_USE_AS_TEMPLATE, null);
        
        // Get the web project - test using wpStoreId
        wpInfo = wpService.getWebProject(wpInfo.getStoreId());
        assertNotNull(wpInfo);
        checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-get", TEST_WEBPROJ_NAME+"-get", TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, TEST_USE_AS_TEMPLATE);
        
        // Get the web project - test using wpStoreNodeRef
        wpInfo = wpService.getWebProject(wpInfo.getNodeRef());
        assertNotNull(wpInfo);
        checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-get", TEST_WEBPROJ_NAME+"-get", TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, TEST_USE_AS_TEMPLATE);
    }
    
    public void testUpdateWebProject()
    {
        WebProjectInfo wpInfo = new WebProjectInfoImpl(TEST_WEBPROJ_DNS+"-update", TEST_WEBPROJ_NAME+"-update", TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, false, null);
        
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
        wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-update", TEST_WEBPROJ_NAME+"-update", TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, true, null);
        
        wpInfo.setName("changedName"+TEST_RUN);
        wpInfo.setTitle("changedTitle");
        wpInfo.setDescription("changedDescription");
        wpInfo.setIsTemplate(false);
        
        // Update the details of the web project
        wpService.updateWebProject(wpInfo);
        wpInfo = wpService.getWebProject(wpInfo.getStoreId());
        checkWebProjectInfo(wpInfo, TEST_WEBPROJ_DNS+"-update", "changedName"+TEST_RUN, "changedTitle", "changedDescription", TEST_DEFAULT_WEBAPP, false);    
    }
    
    public void testDeleteWebProject()
    {
        // Create a test web project
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-delete", TEST_WEBPROJ_NAME+"-delete", TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, true, null);
        String wpStoreId = wpInfo.getStoreId();
        assertNotNull(wpService.getWebProject(wpStoreId));
        
        // Switch to USER_ONE
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
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
        AuthenticationUtil.setSystemUserAsCurrentUser();
        
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
        AuthenticationUtil.setCurrentUser(USER_ADMIN);

        // Delete the web project
        wpService.deleteWebProject(wpStoreId);
        assertNull(wpService.getWebProject(wpStoreId));
        
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
        wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-delete2", TEST_WEBPROJ_NAME+"-delete2", TEST_TITLE, TEST_DESCRIPTION, TEST_DEFAULT_WEBAPP, true, null);
        assertNotNull(wpService.getWebProject(wpInfo.getStoreId()));
        
        wpService.inviteWebUser(wpInfo.getNodeRef(), USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, false);
        
        // Switch to USER_TWO
        AuthenticationUtil.setCurrentUser(USER_TWO);
        
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
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
        // Delete the web project
        wpService.deleteWebProject(TEST_WEBPROJ_DNS+"-delete2");
    }
    
    public void testCreateWebApp()
    {
        // Create a web project with a default webapp
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-createWebApp", TEST_WEBPROJ_NAME+"-createWebApp", TEST_TITLE, TEST_DESCRIPTION, TEST_WEBAPP1, TEST_USE_AS_TEMPLATE, null);
        
        // Switch user to System
        AuthenticationUtil.setSystemUserAsCurrentUser();
        
        try
        {
            // Try to create another webapp as a non-content-manager (such as System) (-ve test)
            wpService.createWebApp(wpInfo.getStoreId(), TEST_WEBAPP2, TEST_DESCRIPTION);
            fail("Shouldn't be able to create a webapp since not a content manager");
        }
        catch (AccessDeniedException exception)
        {
            // Expected
        }
        
        // Switch back to admin
        AuthenticationUtil.setCurrentUser(USER_ADMIN);
        
        // Create another webapp - test using wpStoreId
        wpService.createWebApp(wpInfo.getStoreId(), TEST_WEBAPP2, TEST_DESCRIPTION);
        
        try
        {
            // Try to create duplicate webapp (-ve test)
            wpService.createWebApp(wpInfo.getStoreId(), TEST_WEBAPP2, TEST_DESCRIPTION);
            fail("Shouldn't allow duplicate webapp name");
        }
        catch (AVMExistsException exception)
        {
            // Expected
        }
        
        // Create another webapp - test using wpNodeRef
        wpService.createWebApp(wpInfo.getNodeRef(), TEST_WEBAPP3, TEST_DESCRIPTION);
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
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-listWebApps", TEST_WEBPROJ_NAME+"-listWebApps", TEST_TITLE, TEST_DESCRIPTION);
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
        wpService.createWebApp(wpNodeRef, TEST_WEBAPP1, TEST_DESCRIPTION);
        wpService.createWebApp(wpNodeRef, TEST_WEBAPP2, TEST_DESCRIPTION);
        wpService.createWebApp(wpNodeRef, TEST_WEBAPP3, TEST_DESCRIPTION);
        
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
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-deleteWebApp", TEST_WEBPROJ_NAME+"-deleteWebApp", TEST_TITLE, TEST_DESCRIPTION, TEST_WEBAPP1, TEST_USE_AS_TEMPLATE, null);
       
        String wpStoreId = wpInfo.getStoreId();
        NodeRef wpNodeRef = wpInfo.getNodeRef();
        
        // Create a webapp
        wpService.createWebApp(wpNodeRef, TEST_WEBAPP2, TEST_DESCRIPTION);
        
        // Create another webapp
        wpService.createWebApp(wpNodeRef, TEST_WEBAPP3, TEST_DESCRIPTION);
        
        // Switch user to System
        AuthenticationUtil.setSystemUserAsCurrentUser();
               
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
        AuthenticationUtil.setCurrentUser(USER_ADMIN);
        
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
        AuthenticationUtil.setCurrentUser(USER_ONE);
              
        List<WebProjectInfo> webProjects = wpService.listWebProjects();
        assertNotNull(webProjects);
        int userOneWebProjectCount = webProjects.size();
        
        // Switch back to admin
        AuthenticationUtil.setCurrentUser(USER_ADMIN);
        
        // Create a web project
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-inviteWebUsers", TEST_WEBPROJ_NAME+"-inviteWebUsers", TEST_TITLE, TEST_DESCRIPTION);
        NodeRef wpNodeRef = wpInfo.getNodeRef();
        
        assertEquals(1, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(USER_ADMIN));
        
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
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(USER_ADMIN));
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(USER_ONE));
        assertEquals(WCMUtil.ROLE_CONTENT_PUBLISHER, wpService.listWebUsers(wpNodeRef).get(USER_TWO));
        assertEquals(WCMUtil.ROLE_CONTENT_REVIEWER, wpService.listWebUsers(wpNodeRef).get(USER_THREE));
        
        // Switch to USER_ONE (a content manager for this web project)
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
        webProjects = wpService.listWebProjects();
        assertEquals(userOneWebProjectCount+1, webProjects.size());
        
        // Start: Test fix ETWOTWO-567
        
        // Test newly invited content manager can invite other
        userGroupRoles = new HashMap<String, String>();
        userGroupRoles.put(USER_FIVE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR);
        
        wpService.inviteWebUsersGroups(wpNodeRef, userGroupRoles, false);
        
        // Finish: Test fix ETWOTWO-567
        
        // Switch back to admin
        AuthenticationUtil.setCurrentUser(USER_ADMIN);
        
        // Create a web project
        wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-inviteWebUsers2", TEST_WEBPROJ_NAME+"-inviteWebUsers2", TEST_TITLE, TEST_DESCRIPTION);
        wpNodeRef = wpInfo.getNodeRef();
        
        assertEquals(1, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(USER_ADMIN));
        
        userGroupRoles = new HashMap<String, String>();
 
        userGroupRoles.put(authorityService.getName(AuthorityType.GROUP, GROUP_ONE), WCMUtil.ROLE_CONTENT_PUBLISHER);

        // Invite group as a set of (flattened) web users - test using wpStoreId
        wpService.inviteWebUsersGroups(wpInfo.getStoreId(), userGroupRoles);

        assertEquals(3, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(USER_ADMIN));
        assertEquals(WCMUtil.ROLE_CONTENT_PUBLISHER, wpService.listWebUsers(wpNodeRef).get(USER_ONE));
        assertEquals(WCMUtil.ROLE_CONTENT_PUBLISHER, wpService.listWebUsers(wpNodeRef).get(USER_TWO));
    }
    
    /**
     * Test inviteWebUser - and listWebProjects / listWebUsers
     */
    public void testInviteAndListWebUsers()
    {
        // Switch to USER_ONE
        AuthenticationUtil.setCurrentUser(USER_ONE);
              
        List<WebProjectInfo> webProjects = wpService.listWebProjects();
        assertNotNull(webProjects);
        int userOneWebProjectCount = webProjects.size();
        
        // Switch back to admin
        AuthenticationUtil.setCurrentUser(USER_ADMIN);
        
        // Create a web project
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-inviteWebUser1", TEST_WEBPROJ_NAME+"-inviteWebUser1", TEST_TITLE, TEST_DESCRIPTION);
        NodeRef wpNodeRef = wpInfo.getNodeRef();
        
        assertEquals(1, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(USER_ADMIN));
        
        // Invite one web user - test using wpStoreId
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_PUBLISHER);
        
        // Invite one web user - test using wpNodeRef
        wpService.inviteWebUser(wpNodeRef, USER_TWO, WCMUtil.ROLE_CONTENT_MANAGER, true);
        
        assertEquals(3, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_PUBLISHER, wpService.listWebUsers(wpNodeRef).get(USER_ONE));
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(USER_TWO));
        
        // Create another web project
        wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-inviteWebUser2", TEST_WEBPROJ_NAME+"-inviteWebUser2", TEST_TITLE, TEST_DESCRIPTION);
        NodeRef wpNodeRef2 = wpInfo.getNodeRef();
        
        assertEquals(1, wpService.listWebUsers(wpNodeRef2).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef2).get(USER_ADMIN));
        
        wpService.inviteWebUser(wpNodeRef2, USER_TWO, WCMUtil.ROLE_CONTENT_PUBLISHER, false);
        wpService.inviteWebUser(wpNodeRef2, USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, false);
        
        assertEquals(3, wpService.listWebUsers(wpNodeRef2).size());
        assertEquals(WCMUtil.ROLE_CONTENT_PUBLISHER, wpService.listWebUsers(wpNodeRef2).get(USER_TWO));
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef2).get(USER_ONE));
        
        // Switch to USER_ONE
        AuthenticationUtil.setCurrentUser(USER_ONE);
         
        webProjects = wpService.listWebProjects();
        assertEquals(userOneWebProjectCount+2, webProjects.size());
        
        // Switch to USER_TWO
        AuthenticationUtil.setCurrentUser(USER_TWO);
          
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

        // Switch user to System
        AuthenticationUtil.setSystemUserAsCurrentUser();
                 
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
        
        // Test newly invited content manager can invite other
        
        // Switch to USER_ONE
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
        // Invite web user
        wpService.inviteWebUser(wpNodeRef2, USER_THREE, WCMUtil.ROLE_CONTENT_REVIEWER, false);
        
        // Switch back to admin
        AuthenticationUtil.setCurrentUser(USER_ADMIN);
        
        assertEquals(4, wpService.listWebUsers(wpNodeRef2).size());
        assertEquals(WCMUtil.ROLE_CONTENT_REVIEWER, wpService.listWebUsers(wpNodeRef2).get(USER_THREE));
    }
    
    /**
     * Test uninviteWebUser - and listWebProjects / listWebUsers
     */
    public void testUninviteAndListWebUsers()
    {
        // Switch to USER_FOUR
        AuthenticationUtil.setCurrentUser(USER_FOUR);
                 
        List<WebProjectInfo> webProjects = wpService.listWebProjects();
        assertNotNull(webProjects);
        assertEquals(0, webProjects.size());
        
        // Switch back to admin
        AuthenticationUtil.setCurrentUser(USER_ADMIN);
        
        // Create a web project
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-uninviteWebUser", TEST_WEBPROJ_NAME+"-uninviteWebUser", TEST_TITLE, TEST_DESCRIPTION);
        NodeRef wpNodeRef = wpInfo.getNodeRef();
        
        assertEquals(1, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(USER_ADMIN));
        
        wpService.inviteWebUser(wpNodeRef, USER_FOUR, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, false);
        
        assertEquals(2, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_CONTRIBUTOR, wpService.listWebUsers(wpNodeRef).get(USER_FOUR));
        
        wpService.inviteWebUser(wpNodeRef, USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, false);
        
        assertEquals(3, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(USER_ONE));
        
        // Switch to USER_FOUR
        AuthenticationUtil.setCurrentUser(USER_FOUR);
                
        webProjects = wpService.listWebProjects();
        assertEquals(1, webProjects.size());
        
        // Switch to USER_TWO
        AuthenticationUtil.setCurrentUser(USER_TWO);
                 
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
        AuthenticationUtil.setSystemUserAsCurrentUser();
        
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
        AuthenticationUtil.setCurrentUser(USER_ADMIN);
        
        // Uninvite web user - test using wpStoreId
        wpService.uninviteWebUser(wpInfo.getStoreId(), USER_FOUR);
        
        assertEquals(2, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(null, wpService.listWebUsers(wpNodeRef).get(USER_FOUR));
        
        // Switch to USER_FOUR
        AuthenticationUtil.setCurrentUser(USER_FOUR);
           
        webProjects = wpService.listWebProjects();
        assertEquals(0, webProjects.size());
        
        // Switch back to admin
        AuthenticationUtil.setCurrentUser(USER_ADMIN);

        // Content manager can uninvite themself
        // Uninvite web user - test using wpNodeRef
        wpService.uninviteWebUser(wpNodeRef, USER_ADMIN, false);
        
        assertEquals(1, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(null, wpService.listWebUsers(wpNodeRef).get(USER_ADMIN));
        
        // Switch to USER_ONE
        AuthenticationUtil.setCurrentUser(USER_ONE);
        
        assertEquals(1, wpService.listWebUsers(wpNodeRef).size());
        assertEquals(WCMUtil.ROLE_CONTENT_MANAGER, wpService.listWebUsers(wpNodeRef).get(USER_ONE));
        
        // Delete user (in this case, last invited content manager)
        wpService.uninviteWebUser(wpNodeRef, USER_ONE, false);
        
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
        AuthenticationUtil.setCurrentUser(USER_ADMIN);
        
        // Note: All admin authorities are implicitly content managers (across all web projects) even if not explicitly invited
        assertTrue(wpService.isContentManager(wpInfo.getStoreId(), USER_ADMIN));
        
        assertEquals(0, wpService.listWebUsers(wpNodeRef).size());
        
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
            wpService.createWebProject(TEST_WEBPROJ_DNS+"-"+i, TEST_WEBPROJ_NAME+"-"+i, TEST_TITLE, TEST_DESCRIPTION); // ignore return
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
