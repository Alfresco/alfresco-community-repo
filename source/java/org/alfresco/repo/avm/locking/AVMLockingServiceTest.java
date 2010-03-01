/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.avm.locking;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.util.BulkLoader;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.locking.AVMLock;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.remote.RepoRemote;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.wcm.sandbox.SandboxConstants;
import org.springframework.context.ApplicationContext;

/**
 * Tests for WCM (web project) locking - AVMLockingService & AVMLockingAwareService
 * 
 * @author britt
 */
public class AVMLockingServiceTest extends TestCase
{
    private static ApplicationContext fContext = null;
    
    private static AVMLockingService fLockingService;
    
    private static AVMService fService;
    
    private static AVMSyncService fSyncService;
    
    private static AttributeService fAttributeService;
    
    private static PersonService fPersonService;
    
    private static AuthorityService fAuthorityService;
    
    private static MutableAuthenticationService fAuthenticationService;
    
    private static NodeService fNodeService;
    
    private static RepoRemote fRepoRemote;
    
    private static NodeRef fWebProject;
    
    private static String[] testUsers = {"Buffy", "Willow", "Xander", "Tara", "Spike"};
    
    private static String[] testAuthorities = {"GROUP_Scoobies", "ROLE_SUPER_POWERED", "GROUP_vampires"};
    
    private static final String testWP1 = "alfresco-"+System.currentTimeMillis();
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        if (fContext == null)
        {
            fContext = ApplicationContextHelper.getApplicationContext();
            fLockingService = (AVMLockingService)fContext.getBean("AVMLockingService");
            fService = (AVMService) fContext.getBean("AVMLockingAwareService");
            fSyncService = (AVMSyncService)fContext.getBean("AVMSyncService");
            fAttributeService = (AttributeService)fContext.getBean("AttributeService");
            fPersonService = (PersonService)fContext.getBean("PersonService");
            fAuthorityService = (AuthorityService)fContext.getBean("AuthorityService");
            fAuthenticationService = (MutableAuthenticationService)fContext.getBean("AuthenticationService");
            fNodeService = (NodeService)fContext.getBean("NodeService");
            fRepoRemote = (RepoRemote)fContext.getBean("RepoRemoteService");
        }
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        // Set up a fake web project.
        NodeRef root = fRepoRemote.getRoot();
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(WCMAppModel.PROP_AVMSTORE, testWP1);
        fWebProject = fNodeService.createNode(root, ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, testWP1), 
                WCMAppModel.TYPE_AVMWEBFOLDER, properties).getChildRef();
        
        // Set up sample users groups and roles.
        
        cleanUsersAndGroups();
        
        fAuthenticationService.createAuthentication("Buffy", "Buffy".toCharArray());
        fPersonService.getPerson("Buffy");
        fAuthorityService.createAuthority(AuthorityType.GROUP, "Scoobies");
        fAuthorityService.addAuthority("GROUP_Scoobies", "Buffy");
        fAuthorityService.createAuthority(AuthorityType.ROLE, "SUPER_POWERED");
        fAuthorityService.addAuthority("ROLE_SUPER_POWERED", "Buffy");
        
        fAuthenticationService.createAuthentication("Willow", "Willow".toCharArray());
        fPersonService.getPerson("Willow");
        fAuthorityService.addAuthority("GROUP_Scoobies", "Willow");
        
        fAuthenticationService.createAuthentication("Xander", "Xander".toCharArray());
        fPersonService.getPerson("Xander");
        fAuthorityService.addAuthority("GROUP_Scoobies", "Xander");
        
        fAuthenticationService.createAuthentication("Tara", "Tara".toCharArray());
        fPersonService.getPerson("Tara");
        
        fAuthenticationService.createAuthentication("Spike", "Spike".toCharArray());
        fPersonService.getPerson("Spike");
        fAuthorityService.addAuthority("ROLE_SUPER_POWERED", "Spike");
        fAuthorityService.createAuthority(AuthorityType.GROUP, "vampires");
        fAuthorityService.addAuthority("GROUP_vampires", "Spike");
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        List<String> webProjects = fLockingService.getWebProjects();
        for (String webProject : webProjects)
        {
            if (webProject.equals(testWP1))
            {
                fLockingService.removeStoreLocks(webProject);
                fLockingService.removeWebProject(webProject);
            }
        }
        cleanUsersAndGroups();
        fNodeService.deleteNode(fWebProject);
    }
    
    private void cleanUsersAndGroups()
    {
        for (String testUser : testUsers)
        {
            if (fAuthenticationService.authenticationExists(testUser))
            {
                fAuthenticationService.deleteAuthentication(testUser);
            }
            
            if (fPersonService.personExists(testUser))
            {
                fPersonService.deletePerson(testUser);
            }
        }
        
        for (String testAuthority : testAuthorities)
        {
            if (fAuthorityService.authorityExists(testAuthority))
            {
                fAuthorityService.deleteAuthority(testAuthority);
            }
        }
    }
    
    public void testAll()
    {
        try
        {
            fLockingService.addWebProject(testWP1);
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            List<String> owners = new ArrayList<String>();
            owners.add("Buffy");
            owners.add("Spike");
            AVMLock lock = new AVMLock(testWP1,
                                       "Sunnydale",
                                       "Revello Drive/1630",
                                       AVMLockingService.Type.DISCRETIONARY,
                                       owners);
            fLockingService.lockPath(lock);
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            assertNotNull(fLockingService.getLock(testWP1, "Revello Drive/1630"));
            // assertEquals(1, fLockingService.getUsersLocks("Buffy").size());
            assertEquals(1, fLockingService.getWebProjectLocks(testWP1).size());
            List<String> owners2 = new ArrayList<String>();
            owners2.add("Buffy");
            owners2.add("Willow");
            AVMLock lock2 = new AVMLock(testWP1,
                                        "Sunnydale",
                                        "UC Sunnydale/Stevenson Hall",
                                        AVMLockingService.Type.DISCRETIONARY,
                                        owners2);
            fLockingService.lockPath(lock2);
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            // assertEquals(2, fLockingService.getUsersLocks("Buffy").size());
            assertEquals(2, fLockingService.getWebProjectLocks(testWP1).size());
            System.out.println("Before----------------------------");
            fLockingService.removeLock(testWP1, "Revello Drive/1630");
            System.out.println("After----------------------------");
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            // assertEquals(1, fLockingService.getUsersLocks("Buffy").size());
            assertEquals(1, fLockingService.getWebProjectLocks(testWP1).size());
            fLockingService.removeWebProject(testWP1);
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            // assertEquals(0, fLockingService.getUsersLocks("Spike").size());
            // assertEquals(0, fLockingService.getUsersLocks("Buffy").size());
            // assertEquals(0, fLockingService.getUsersLocks("Willow").size());
            // assertEquals(0, fLockingService.getUsersLocks("Tara").size());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }
    
    public void testRoleBasedLocking()
    {
        try
        {
            fLockingService.addWebProject(testWP1);
            List<String> owners = new ArrayList<String>();
            owners.add("ROLE_SUPER_POWERED");
            owners.add("Tara");
            AVMLock lock = new AVMLock(testWP1, 
                                       "Sunnydale", 
                                       "TheInitiative/Adam/plans.txt", 
                                       AVMLockingService.Type.DISCRETIONARY, 
                                       owners);
            fLockingService.lockPath(lock);
            assertTrue(fLockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Buffy"));
            assertTrue(fLockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Spike"));
            assertFalse(fLockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Willow"));
            assertTrue(fLockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Tara"));
            assertFalse(fLockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Xander"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Buffy"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Spike"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Willow"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Tara"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Xander"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    public void testGroupBasedLocking()
    {
        try
        {
            fLockingService.addWebProject(testWP1);
            List<String> owners = new ArrayList<String>();
            owners.add("GROUP_Scoobies");
            owners.add("Tara");
            AVMLock lock = new AVMLock(testWP1, 
                                       "Sunnydale", 
                                       "TheInitiative/Adam/plans.txt", 
                                       AVMLockingService.Type.DISCRETIONARY, 
                                       owners);
            fLockingService.lockPath(lock);
            assertTrue(fLockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Buffy"));
            assertFalse(fLockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Spike"));
            assertTrue(fLockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Willow"));
            assertTrue(fLockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Tara"));
            assertTrue(fLockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Xander"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Buffy"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Spike"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Willow"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Tara"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Xander"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }
    
    public void testLockModification()
    {
        try
        {
            fLockingService.addWebProject(testWP1);
            List<String> owners = new ArrayList<String>();
            owners.add("GROUP_Scoobies");
            owners.add("Tara");
            AVMLock lock = new AVMLock(testWP1, 
                                       "Sunnydale", 
                                       "TheInitiative/Adam/plans.txt", 
                                       AVMLockingService.Type.DISCRETIONARY, 
                                       owners);
            fLockingService.lockPath(lock);
            assertTrue(fLockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Buffy"));
            assertFalse(fLockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Spike"));
            assertTrue(fLockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Willow"));
            assertTrue(fLockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Tara"));
            assertTrue(fLockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Xander"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Buffy"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Spike"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Willow"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Tara"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Xander"));
            fLockingService.modifyLock(testWP1, "TheInitiative/Adam/plans.txt", "ScrapHeap/Adam/plans.txt", null, null, null);
            assertTrue(fLockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Buffy"));
            assertFalse(fLockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Spike"));
            assertTrue(fLockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Willow"));
            assertTrue(fLockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Tara"));
            assertTrue(fLockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Xander"));
            fLockingService.modifyLock(testWP1, "ScrapHeap/Adam/plans.txt", null, "LA", null, null);
            assertTrue(fLockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Buffy"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Spike"));
            assertTrue(fLockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Willow"));
            assertTrue(fLockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Tara"));
            assertTrue(fLockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Xander"));
            assertFalse(fLockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Buffy"));
            assertFalse(fLockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Spike"));
            assertFalse(fLockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Willow"));
            assertFalse(fLockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Tara"));
            assertFalse(fLockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Xander"));
            List<String> usersToAdd = new ArrayList<String>();
            usersToAdd.add("Spike");
            fLockingService.modifyLock(testWP1, "ScrapHeap/Adam/plans.txt", null, null, null, usersToAdd);
            assertTrue(fLockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Buffy"));
            assertTrue(fLockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Spike"));
            assertTrue(fLockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Willow"));
            assertTrue(fLockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Tara"));
            assertTrue(fLockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Xander"));
            List<String> usersToRemove = new ArrayList<String>();
            usersToRemove.add("GROUP_Scoobies");
            fLockingService.modifyLock(testWP1, "ScrapHeap/Adam/plans.txt", null, null, usersToRemove, null);
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Buffy"));
            assertTrue(fLockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Spike"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Willow"));
            assertTrue(fLockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Tara"));
            assertFalse(fLockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Xander"));
            assertTrue(fLockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", AuthenticationUtil.getAdminUserName()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }
    
    /**
     * Minimal testing of Locking Aware service.
     */
    public void testLockingAwareService() throws Exception
    {
        try
        {
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
            
            fService.createStore("main");
            
            fLockingService.addWebProject("main");
            
            // note: locking applies to WCM web projects, hence relies on WCM sandbox conventions (naming and properties)
            fService.setStoreProperty("main", SandboxConstants.PROP_WEB_PROJECT_NODE_REF, new PropertyValue(DataTypeDefinition.NODE_REF, new NodeRef("workspace://SpacesStore/dummy")));
            
            fService.createStore("main--admin");
            
            setupBasicTree0();

            List<AVMDifference> diffs = fSyncService.compare(-1, "main:/", -1, "main--admin:/", null);
            assertEquals(2, diffs.size());
            assertEquals("[main:/a[-1] > main--admin:/a[-1], main:/d[-1] > main--admin:/d[-1]]", diffs.toString());
            
            fSyncService.update(diffs, null, false, false, false, false, null, null);
            
            RetryingTransactionHelper.RetryingTransactionCallback<Object> cb = new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    BulkLoader loader = new BulkLoader();
                    loader.setAvmService(fService);
                    loader.recursiveLoad("source/java/org/alfresco/repo/avm", "main--admin:/");
                    return null;
                }
            };
            RetryingTransactionHelper helper = (RetryingTransactionHelper) fContext.getBean("retryingTransactionHelper");
            helper.doInTransaction(cb);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
        finally
        {
            fLockingService.removeStoreLocks("main");
            fLockingService.removeWebProject("main");
            
            fService.purgeStore("main--admin");
            fService.purgeStore("main");
            
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        }
    }
    
    // Minimal test of locking with file 'rename' across web projects
    public void testLockingAwareServiceFileRename() throws Exception
    {
        try
        {
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
            
            fService.createStore("wpA");
            fLockingService.addWebProject("wpA");
            
            fService.createStore("wpA--admin");
            
            fService.createStore("wpB");
            fLockingService.addWebProject("wpB");
            
            fService.createStore("wpB--admin");
            
            // note: locking applies to WCM web projects, hence relies on WCM sandbox conventions (naming and properties)
            fService.setStoreProperty("wpA", SandboxConstants.PROP_WEB_PROJECT_NODE_REF, new PropertyValue(DataTypeDefinition.NODE_REF, new NodeRef("workspace://SpacesStore/dummyA")));
            fService.setStoreProperty("wpB", SandboxConstants.PROP_WEB_PROJECT_NODE_REF, new PropertyValue(DataTypeDefinition.NODE_REF, new NodeRef("workspace://SpacesStore/dummyB")));
            
            assertNull(fLockingService.getLock("wpA", "/file1.txt"));
            assertTrue(fLockingService.hasAccess("wpA", "wpA--admin:/file1.txt", "admin"));
            
            fService.createFile("wpA--admin:/", "file1.txt").close();
            
            assertNotNull(fLockingService.getLock("wpA", "/file1.txt"));
            assertEquals("admin", fLockingService.getLock("wpA", "/file1.txt").getOwners().get(0));
            assertTrue(fLockingService.hasAccess("wpA", "wpA--admin:/file1.txt", "admin"));
            
            assertNull(fLockingService.getLock("wpB", "/file1.txt"));
            assertTrue(fLockingService.hasAccess("wpB", "wpB--admin:/file1.txt", "admin"));
            
            // ETHREEOH-1544
            fService.rename("wpA--admin:/", "file1.txt", "wpB--admin:/", "file1.txt");
            
            assertNull(fLockingService.getLock("wpA", "/file1.txt"));
            assertTrue(fLockingService.hasAccess("wpA", "wpA--admin:/file1.txt", "admin"));
            
            assertNotNull(fLockingService.getLock("wpB", "/file1.txt"));
            assertEquals("admin", fLockingService.getLock("wpB", "/file1.txt").getOwners().get(0));
            assertTrue(fLockingService.hasAccess("wpB", "wpB--admin:/file1.txt", "admin"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
        finally
        {
            try { fLockingService.removeStoreLocks("wpA"); } catch (Exception e) {}
            try { fLockingService.removeWebProject("wpA"); } catch (Exception e) {}
            
            if (fService.getStore("wpA--admin") != null) { fService.purgeStore("wpA--admin"); }
            if (fService.getStore("wpA") != null) { fService.purgeStore("wpA"); }
            
            try { fLockingService.removeStoreLocks("wpB"); } catch (Exception e) {}
            try { fLockingService.removeWebProject("wpB"); } catch (Exception e) {}
            
            if (fService.getStore("wpB--admin") != null) { fService.purgeStore("wpB--admin"); }
            if (fService.getStore("wpB") != null) { fService.purgeStore("wpB"); }
            
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        }
    }
    
    /**
     * Setup a basic tree.
     */
    protected void setupBasicTree0()
        throws IOException
    {
        fService.createDirectory("main:/", "a");
        fService.createDirectory("main:/a", "b");
        fService.createDirectory("main:/a/b", "c");
        fService.createDirectory("main:/", "d");
        fService.createDirectory("main:/d", "e");
        fService.createDirectory("main:/d/e", "f");
        
        fService.createFile("main:/a/b/c", "foo").close();
        ContentWriter writer = fService.getContentWriter("main:/a/b/c/foo");
        writer.setEncoding("UTF-8");
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.putContent("I am main:/a/b/c/foo");
        
        fService.createFile("main:/a/b/c", "bar").close();
        writer = fService.getContentWriter("main:/a/b/c/bar");
        // Force a conversion
        writer.setEncoding("UTF-16");
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.putContent("I am main:/a/b/c/bar");
       
        fService.createSnapshot("main", null, null);
    }
}
