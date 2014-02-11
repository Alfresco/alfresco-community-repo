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
package org.alfresco.repo.avm.locking;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
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
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.locking.AVMLockingException;
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
import org.alfresco.test_category.LegacyCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.wcm.sandbox.SandboxConstants;
import org.alfresco.wcm.util.WCMUtil;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * Tests for WCM (web project) locking - AVMLockingService & AVMLockingAwareService
 * 
 * @author Derek Hulley, janv
 */
@Category(LegacyCategory.class)
public class AVMLockingServiceTest extends TestCase
{
    private static String[] TESTUSERS = {"Buffy", "Willow", "Xander", "Tara", "Spike"};
    private static String[] TESTAUTHORITIES = {"GROUP_Scoobies", "ROLE_SUPER_POWERED", "GROUP_vampires"};
    
    private ApplicationContext ctx = null;
    
    private static AVMLockingService lockingService;
    private static AVMService avmService;
    private static AVMSyncService syncService;
    private static PersonService personService;
    private static AuthorityService authorityService;
    private static MutableAuthenticationService authenticationService;
    private static NodeService nodeService;
    private static RepoRemote repoRemoteService;
    
    private static final String testWP1 = "alfresco-"+System.currentTimeMillis();
    private NodeRef testWP1NodeRef;
    
    @Override
    protected void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext();
        lockingService = (AVMLockingService)ctx.getBean("AVMLockingService");
        avmService = (AVMService) ctx.getBean("AVMLockingAwareService");
        syncService = (AVMSyncService)ctx.getBean("AVMSyncService");
        personService = (PersonService)ctx.getBean("PersonService");
        authorityService = (AuthorityService)ctx.getBean("AuthorityService");
        authenticationService = (MutableAuthenticationService)ctx.getBean("AuthenticationService");
        nodeService = (NodeService)ctx.getBean("NodeService");
        repoRemoteService = (RepoRemote)ctx.getBean("RepoRemoteService");
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        // Set up a fake web project.
        NodeRef root = repoRemoteService.getRoot();
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(WCMAppModel.PROP_AVMSTORE, testWP1);
        testWP1NodeRef = nodeService.createNode(root, ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, testWP1), 
                WCMAppModel.TYPE_AVMWEBFOLDER, properties).getChildRef();
        
        // Set up sample users groups and roles.
        
        cleanUsersAndGroups();
        
        authenticationService.createAuthentication("Buffy", "Buffy".toCharArray());
        personService.getPerson("Buffy");
        authorityService.createAuthority(AuthorityType.GROUP, "Scoobies");
        authorityService.addAuthority("GROUP_Scoobies", "Buffy");
        authorityService.createAuthority(AuthorityType.ROLE, "SUPER_POWERED");
        authorityService.addAuthority("ROLE_SUPER_POWERED", "Buffy");
        
        authenticationService.createAuthentication("Willow", "Willow".toCharArray());
        personService.getPerson("Willow");
        authorityService.addAuthority("GROUP_Scoobies", "Willow");
        
        authenticationService.createAuthentication("Xander", "Xander".toCharArray());
        personService.getPerson("Xander");
        authorityService.addAuthority("GROUP_Scoobies", "Xander");
        
        authenticationService.createAuthentication("Tara", "Tara".toCharArray());
        personService.getPerson("Tara");
        
        authenticationService.createAuthentication("Spike", "Spike".toCharArray());
        personService.getPerson("Spike");
        authorityService.addAuthority("ROLE_SUPER_POWERED", "Spike");
        authorityService.createAuthority(AuthorityType.GROUP, "vampires");
        authorityService.addAuthority("GROUP_vampires", "Spike");
    }

    @Override
    protected void tearDown() throws Exception
    {
        lockingService.removeLocks(testWP1);
        cleanUsersAndGroups();
        nodeService.deleteNode(testWP1NodeRef);
    }
    
    private void cleanUsersAndGroups()
    {
        for (String testUser : TESTUSERS)
        {
            if (authenticationService.authenticationExists(testUser))
            {
                authenticationService.deleteAuthentication(testUser);
            }
            
            if (personService.personExists(testUser))
            {
                personService.deletePerson(testUser);
            }
        }
        
        for (String testAuthority : TESTAUTHORITIES)
        {
            if (authorityService.authorityExists(testAuthority))
            {
                authorityService.deleteAuthority(testAuthority);
            }
        }
    }
    
    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();
    
    public void testAll() throws Exception
    {
        lockingService.lock(testWP1, "Revello Drive/1630", TESTUSERS[0], EMPTY_MAP);
        assertEquals(lockingService.getLockOwner(testWP1, "Revello Drive/1630"), TESTUSERS[0]);
        lockingService.removeLock(testWP1, "Revello Drive/1630");
        assertNull(lockingService.getLockOwner(testWP1, "Revello Drive/1630"));

        lockingService.lock(testWP1, "UC Sunnydale/Stevenson Hall", TESTUSERS[0], EMPTY_MAP);
        assertEquals(lockingService.getLockOwner(testWP1, "UC Sunnydale/Stevenson Hall"), TESTUSERS[0]);

        try
        {
            lockingService.lock(testWP1, "UC Sunnydale/Stevenson Hall", TESTUSERS[1], EMPTY_MAP);
            fail("Failed to detect existing lock");
        }
        catch (AVMLockingException e)
        {
            // Expected
        }
    }
    
    @SuppressWarnings("deprecation")
    public void testRoleBasedLocking()
    {
        Map<String, String> lockData = Collections.singletonMap(WCMUtil.LOCK_KEY_STORE_NAME, "Sunnydale");
        
        // lock owner = "ROLE_SUPER_POWERED"
        lockingService.lock(testWP1, "TheInitiative/Adam/plans.txt", TESTAUTHORITIES[1], lockData);
        
        assertTrue(lockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Buffy"));
        assertTrue(lockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Spike"));
        assertFalse(lockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Willow"));
        assertFalse(lockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Tara")); // Tara does not belong to ROLE_SUPER_POWERED
        assertFalse(lockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Xander"));
        
        assertFalse(lockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Buffy"));
        assertFalse(lockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Spike"));
        assertFalse(lockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Willow"));
        assertFalse(lockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Tara"));
        assertFalse(lockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Xander"));
    }

    @SuppressWarnings("deprecation")
    public void testGroupBasedLocking()
    {
        Map<String, String> lockData = Collections.singletonMap(WCMUtil.LOCK_KEY_STORE_NAME, "Sunnydale");
        
        // lock owner = "GROUP_Scoobies"
        lockingService.lock(testWP1, "TheInitiative/Adam/plans.txt", TESTAUTHORITIES[0], lockData);
        
        assertTrue(lockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Buffy"));
        assertFalse(lockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Spike"));
        assertTrue(lockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Willow"));
        assertFalse(lockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Tara")); // Tara does not belong to GROUP_Scoobies
        assertTrue(lockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Xander"));
        
        assertFalse(lockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Buffy"));
        assertFalse(lockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Spike"));
        assertFalse(lockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Willow"));
        assertFalse(lockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Tara"));
        assertFalse(lockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Xander"));
    }
    
    @SuppressWarnings("deprecation")
    public void testLockModification()
    {
        Map<String, String> lockData = Collections.singletonMap(WCMUtil.LOCK_KEY_STORE_NAME, "Sunnydale");
        
        // lock owner = "GROUP_Scoobies"
        lockingService.lock(testWP1, "TheInitiative/Adam/plans.txt", TESTAUTHORITIES[0], lockData);
        
        assertTrue(lockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Buffy"));
        assertFalse(lockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Spike"));
        assertTrue(lockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Willow"));
        assertFalse(lockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Tara")); // Tara does not belong to GROUP_Scoobies
        assertTrue(lockingService.hasAccess(testWP1, "Sunnydale:/TheInitiative/Adam/plans.txt", "Xander"));
        
        assertFalse(lockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Buffy"));
        assertFalse(lockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Spike"));
        assertFalse(lockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Willow"));
        assertFalse(lockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Tara"));
        assertFalse(lockingService.hasAccess(testWP1, "LA:/TheInitiative/Adam/plans.txt", "Xander"));
        
        lockData = Collections.singletonMap(WCMUtil.LOCK_KEY_STORE_NAME, "Sunnydale");
        
        // lock owner = "GROUP_Scoobies"
        lockingService.modifyLock(
                testWP1, "TheInitiative/Adam/plans.txt", TESTAUTHORITIES[0],
                testWP1, "ScrapHeap/Adam/plans.txt", lockData);
        
        assertTrue(lockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Buffy"));
        assertFalse(lockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Spike"));
        assertTrue(lockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Willow"));
        assertFalse(lockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Tara")); // Tara does not belong to GROUP_Scoobies
        assertTrue(lockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Xander"));
        
        lockData = Collections.singletonMap(WCMUtil.LOCK_KEY_STORE_NAME, "LA");
        
        lockingService.modifyLock(
                testWP1, "ScrapHeap/Adam/plans.txt", TESTAUTHORITIES[0], 
                testWP1, "ScrapHeap/Adam/plans.txt", lockData);
        
        assertTrue(lockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Buffy"));
        assertFalse(lockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Spike"));
        assertTrue(lockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Willow"));
        assertFalse(lockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Tara")); // Tara does not belong to GROUP_Scoobies
        assertTrue(lockingService.hasAccess(testWP1, "LA:/ScrapHeap/Adam/plans.txt", "Xander"));
        
        assertFalse(lockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Buffy"));
        assertFalse(lockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Spike"));
        assertFalse(lockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Willow"));
        assertFalse(lockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Tara"));
        assertFalse(lockingService.hasAccess(testWP1, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Xander"));
    }
    
    /**
     * Minimal testing of Locking Aware service.
     */
    public void testLockingAwareService() throws Exception
    {
        try
        {
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
            
            if (avmService.getStore("main") == null)
            {
                avmService.createStore("main");
            }
            
            // note: locking applies to WCM web projects, hence relies on WCM sandbox conventions (naming and properties)
            avmService.setStoreProperty("main", SandboxConstants.PROP_WEB_PROJECT_NODE_REF, new PropertyValue(DataTypeDefinition.NODE_REF, new NodeRef("workspace://SpacesStore/dummy")));
            
            if (avmService.getStore("main--admin") == null)
            {
                avmService.createStore("main--admin");
            }
            
            setupBasicTree0();

            List<AVMDifference> diffs = syncService.compare(-1, "main:/", -1, "main--admin:/", null);
            assertEquals(2, diffs.size());
            assertEquals("[main:/a[-1] > main--admin:/a[-1], main:/d[-1] > main--admin:/d[-1]]", diffs.toString());
            
            syncService.update(diffs, null, false, false, false, false, null, null);
            
            RetryingTransactionHelper.RetryingTransactionCallback<Object> cb = new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    BulkLoader loader = new BulkLoader();
                    loader.setAvmService(avmService);
                    loader.recursiveLoad(
                        System.getProperty("alfresco.java.sources.dir", "source/java") + "/org/alfresco/repo/avm",
                        "main--admin:/");
                    return null;
                }
            };
            RetryingTransactionHelper helper = (RetryingTransactionHelper) ctx.getBean("retryingTransactionHelper");
            helper.doInTransaction(cb);
        }
        finally
        {
            lockingService.removeLocks("main");
            
            avmService.purgeStore("main--admin");
            avmService.purgeStore("main");
            
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        }
    }
    
    // Minimal test of locking with file 'rename' across web projects
    @SuppressWarnings("deprecation")
    public void testLockingAwareServiceFileRename() throws Exception
    {
        lockingService.removeLocks("wpA");
        lockingService.removeLocks("wpB");
        try
        {
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
            
            avmService.createStore("wpA");
            avmService.createStore("wpA--admin");
            
            avmService.createStore("wpB");
            avmService.createStore("wpB--admin");
            
            // note: locking applies to WCM web projects, hence relies on WCM sandbox conventions (naming and properties)
            avmService.setStoreProperty("wpA", SandboxConstants.PROP_WEB_PROJECT_NODE_REF, new PropertyValue(DataTypeDefinition.NODE_REF, new NodeRef("workspace://SpacesStore/dummyA")));
            avmService.setStoreProperty("wpB", SandboxConstants.PROP_WEB_PROJECT_NODE_REF, new PropertyValue(DataTypeDefinition.NODE_REF, new NodeRef("workspace://SpacesStore/dummyB")));
            
            assertNull(lockingService.getLockOwner("wpA", "/file1.txt"));
            assertTrue(lockingService.hasAccess("wpA", "wpA--admin:/file1.txt", "admin"));
            
            avmService.createFile("wpA--admin:/", "file1.txt").close();
            
            assertNotNull(lockingService.getLockOwner("wpA", "/file1.txt"));
            assertEquals("admin", lockingService.getLockOwner("wpA", "/file1.txt"));
            assertTrue(lockingService.hasAccess("wpA", "wpA--admin:/file1.txt", "admin"));
            
            assertNull(lockingService.getLockOwner("wpB", "/file1.txt"));
            assertTrue(lockingService.hasAccess("wpB", "wpB--admin:/file1.txt", "admin"));
            
            // ETHREEOH-1544
            avmService.rename("wpA--admin:/", "file1.txt", "wpB--admin:/", "file1.txt");
            
            assertNull(lockingService.getLockOwner("wpA", "/file1.txt"));
            assertTrue(lockingService.hasAccess("wpA", "wpA--admin:/file1.txt", "admin"));
            
            assertNotNull(lockingService.getLockOwner("wpB", "/file1.txt"));
            assertEquals("admin", lockingService.getLockOwner("wpB", "/file1.txt"));
            assertTrue(lockingService.hasAccess("wpB", "wpB--admin:/file1.txt", "admin"));
        }
        finally
        {
            try { lockingService.removeLocks("wpA"); } catch (Exception e) {}
            
            if (avmService.getStore("wpA--admin") != null) { avmService.purgeStore("wpA--admin"); }
            if (avmService.getStore("wpA") != null) { avmService.purgeStore("wpA"); }
            
            try { lockingService.removeLocks("wpB"); } catch (Exception e) {}
            
            if (avmService.getStore("wpB--admin") != null) { avmService.purgeStore("wpB--admin"); }
            if (avmService.getStore("wpB") != null) { avmService.purgeStore("wpB"); }
            
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        }
    }
    
    /**
     * Setup a basic tree.
     */
    protected void setupBasicTree0()
        throws IOException
    {
        avmService.createDirectory("main:/", "a");
        avmService.createDirectory("main:/a", "b");
        avmService.createDirectory("main:/a/b", "c");
        avmService.createDirectory("main:/", "d");
        avmService.createDirectory("main:/d", "e");
        avmService.createDirectory("main:/d/e", "f");
        
        avmService.createFile("main:/a/b/c", "foo").close();
        ContentWriter writer = avmService.getContentWriter("main:/a/b/c/foo", true);
        writer.setEncoding("UTF-8");
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.putContent("I am main:/a/b/c/foo");
        
        avmService.createFile("main:/a/b/c", "bar").close();
        writer = avmService.getContentWriter("main:/a/b/c/bar", true);
        // Force a conversion
        writer.setEncoding("UTF-16");
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.putContent("I am main:/a/b/c/bar");
       
        avmService.createSnapshot("main", null, null);
    }
}
