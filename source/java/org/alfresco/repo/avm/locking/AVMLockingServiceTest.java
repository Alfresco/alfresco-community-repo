/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.avm.locking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.avm.locking.AVMLock;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.remote.RepoRemote;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Tests for AVM locking service.
 * @author britt
 */
public class AVMLockingServiceTest extends TestCase
{
    private static ApplicationContext fContext = null;
    
    private static AVMLockingService fService;
    
    private static AttributeService fAttributeService;
    
    private static PersonService fPersonService;
    
    private static AuthorityService fAuthorityService;
    
    private static MutableAuthenticationService fAuthenticationService;
    
    private static AuthenticationComponent fAuthenticationComponent;
    
    private static NodeService fNodeService;
    
    private static RepoRemote fRepoRemote;
    
    private static NodeRef fWebProject;
    
    private final static String WP = "alfresco-"+System.currentTimeMillis();
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        if (fContext == null)
        {
            fContext = ApplicationContextHelper.getApplicationContext();
            fService = (AVMLockingService)fContext.getBean("AVMLockingService");
            fAttributeService = (AttributeService)fContext.getBean("AttributeService");
            fPersonService = (PersonService)fContext.getBean("PersonService");
            fAuthorityService = (AuthorityService)fContext.getBean("AuthorityService");
            fAuthenticationService = (MutableAuthenticationService)fContext.getBean("AuthenticationService");
            fAuthenticationComponent = (AuthenticationComponent)fContext.getBean("AuthenticationComponent");
            fAuthenticationComponent.setSystemUserAsCurrentUser();
            fNodeService = (NodeService)fContext.getBean("NodeService");
            fRepoRemote = (RepoRemote)fContext.getBean("RepoRemoteService");
        }
        // Set up a fake web project.
        NodeRef root = fRepoRemote.getRoot();
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(WCMAppModel.PROP_AVMSTORE, WP);
        fWebProject = fNodeService.createNode(root, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, WP),
                WCMAppModel.TYPE_AVMWEBFOLDER, properties).getChildRef();
        // Set up sample users groups and roles.
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
        List<String> webProjects = fService.getWebProjects();
        for (String webProject : webProjects)
        {
            if (webProject.equals(WP))
            {
                fService.removeWebProject(WP);
            }
        }
        fAuthenticationService.deleteAuthentication("Buffy");
        fAuthenticationService.deleteAuthentication("Willow");
        fAuthenticationService.deleteAuthentication("Xander");
        fAuthenticationService.deleteAuthentication("Tara");
        fAuthenticationService.deleteAuthentication("Spike");
        fPersonService.deletePerson("Buffy");
        fPersonService.deletePerson("Willow");
        fPersonService.deletePerson("Tara");
        fPersonService.deletePerson("Xander");
        fPersonService.deletePerson("Spike");
        fAuthorityService.deleteAuthority("GROUP_Scoobies");
        fAuthorityService.deleteAuthority("ROLE_SUPER_POWERED");
        fAuthorityService.deleteAuthority("GROUP_vampires");
        fNodeService.deleteNode(fWebProject);
    }
    
    public void testAll()
    {
        try
        {
            fService.addWebProject(WP);
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            List<String> owners = new ArrayList<String>();
            owners.add("Buffy");
            owners.add("Spike");
            AVMLock lock = new AVMLock(WP,
                                       "Sunnydale",
                                       "Revello Drive/1630",
                                       AVMLockingService.Type.DISCRETIONARY,
                                       owners);
            fService.lockPath(lock);
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            assertNotNull(fService.getLock(WP, "Revello Drive/1630"));
            // assertEquals(1, fService.getUsersLocks("Buffy").size());
            assertEquals(1, fService.getWebProjectLocks(WP).size());
            List<String> owners2 = new ArrayList<String>();
            owners2.add("Buffy");
            owners2.add("Willow");
            AVMLock lock2 = new AVMLock(WP,
                                        "Sunnydale",
                                        "UC Sunnydale/Stevenson Hall",
                                        AVMLockingService.Type.DISCRETIONARY,
                                        owners2);
            fService.lockPath(lock2);
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            // assertEquals(2, fService.getUsersLocks("Buffy").size());
            assertEquals(2, fService.getWebProjectLocks(WP).size());
            System.out.println("Before----------------------------");
            fService.removeLock(WP, "Revello Drive/1630");
            System.out.println("After----------------------------");
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            // assertEquals(1, fService.getUsersLocks("Buffy").size());
            assertEquals(1, fService.getWebProjectLocks(WP).size());
            fService.removeWebProject(WP);
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            // assertEquals(0, fService.getUsersLocks("Spike").size());
            // assertEquals(0, fService.getUsersLocks("Buffy").size());
            // assertEquals(0, fService.getUsersLocks("Willow").size());
            // assertEquals(0, fService.getUsersLocks("Tara").size());
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
            fService.addWebProject(WP);
            List<String> owners = new ArrayList<String>();
            owners.add("ROLE_SUPER_POWERED");
            owners.add("Tara");
            AVMLock lock = new AVMLock(WP, 
                                       "Sunnydale", 
                                       "TheInitiative/Adam/plans.txt", 
                                       AVMLockingService.Type.DISCRETIONARY, 
                                       owners);
            fService.lockPath(lock);
            assertTrue(fService.hasAccess(WP, "Sunnydale:/TheInitiative/Adam/plans.txt", "Buffy"));
            assertTrue(fService.hasAccess(WP, "Sunnydale:/TheInitiative/Adam/plans.txt", "Spike"));
            assertFalse(fService.hasAccess(WP, "Sunnydale:/TheInitiative/Adam/plans.txt", "Willow"));
            assertTrue(fService.hasAccess(WP, "Sunnydale:/TheInitiative/Adam/plans.txt", "Tara"));
            assertFalse(fService.hasAccess(WP, "Sunnydale:/TheInitiative/Adam/plans.txt", "Xander"));
            assertFalse(fService.hasAccess(WP, "LA:/TheInitiative/Adam/plans.txt", "Buffy"));
            assertFalse(fService.hasAccess(WP, "LA:/TheInitiative/Adam/plans.txt", "Spike"));
            assertFalse(fService.hasAccess(WP, "LA:/TheInitiative/Adam/plans.txt", "Willow"));
            assertFalse(fService.hasAccess(WP, "LA:/TheInitiative/Adam/plans.txt", "Tara"));
            assertFalse(fService.hasAccess(WP, "LA:/TheInitiative/Adam/plans.txt", "Xander"));
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
            fService.addWebProject(WP);
            List<String> owners = new ArrayList<String>();
            owners.add("GROUP_Scoobies");
            owners.add("Tara");
            AVMLock lock = new AVMLock(WP, 
                                       "Sunnydale", 
                                       "TheInitiative/Adam/plans.txt", 
                                       AVMLockingService.Type.DISCRETIONARY, 
                                       owners);
            fService.lockPath(lock);
            assertTrue(fService.hasAccess(WP, "Sunnydale:/TheInitiative/Adam/plans.txt", "Buffy"));
            assertFalse(fService.hasAccess(WP, "Sunnydale:/TheInitiative/Adam/plans.txt", "Spike"));
            assertTrue(fService.hasAccess(WP, "Sunnydale:/TheInitiative/Adam/plans.txt", "Willow"));
            assertTrue(fService.hasAccess(WP, "Sunnydale:/TheInitiative/Adam/plans.txt", "Tara"));
            assertTrue(fService.hasAccess(WP, "Sunnydale:/TheInitiative/Adam/plans.txt", "Xander"));
            assertFalse(fService.hasAccess(WP, "LA:/TheInitiative/Adam/plans.txt", "Buffy"));
            assertFalse(fService.hasAccess(WP, "LA:/TheInitiative/Adam/plans.txt", "Spike"));
            assertFalse(fService.hasAccess(WP, "LA:/TheInitiative/Adam/plans.txt", "Willow"));
            assertFalse(fService.hasAccess(WP, "LA:/TheInitiative/Adam/plans.txt", "Tara"));
            assertFalse(fService.hasAccess(WP, "LA:/TheInitiative/Adam/plans.txt", "Xander"));
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
            fService.addWebProject(WP);
            List<String> owners = new ArrayList<String>();
            owners.add("GROUP_Scoobies");
            owners.add("Tara");
            AVMLock lock = new AVMLock(WP, 
                                       "Sunnydale", 
                                       "TheInitiative/Adam/plans.txt", 
                                       AVMLockingService.Type.DISCRETIONARY, 
                                       owners);
            fService.lockPath(lock);
            assertTrue(fService.hasAccess(WP, "Sunnydale:/TheInitiative/Adam/plans.txt", "Buffy"));
            assertFalse(fService.hasAccess(WP, "Sunnydale:/TheInitiative/Adam/plans.txt", "Spike"));
            assertTrue(fService.hasAccess(WP, "Sunnydale:/TheInitiative/Adam/plans.txt", "Willow"));
            assertTrue(fService.hasAccess(WP, "Sunnydale:/TheInitiative/Adam/plans.txt", "Tara"));
            assertTrue(fService.hasAccess(WP, "Sunnydale:/TheInitiative/Adam/plans.txt", "Xander"));
            assertFalse(fService.hasAccess(WP, "LA:/TheInitiative/Adam/plans.txt", "Buffy"));
            assertFalse(fService.hasAccess(WP, "LA:/TheInitiative/Adam/plans.txt", "Spike"));
            assertFalse(fService.hasAccess(WP, "LA:/TheInitiative/Adam/plans.txt", "Willow"));
            assertFalse(fService.hasAccess(WP, "LA:/TheInitiative/Adam/plans.txt", "Tara"));
            assertFalse(fService.hasAccess(WP, "LA:/TheInitiative/Adam/plans.txt", "Xander"));
            fService.modifyLock(WP, "TheInitiative/Adam/plans.txt", "ScrapHeap/Adam/plans.txt", null, null, null);
            assertTrue(fService.hasAccess(WP, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Buffy"));
            assertFalse(fService.hasAccess(WP, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Spike"));
            assertTrue(fService.hasAccess(WP, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Willow"));
            assertTrue(fService.hasAccess(WP, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Tara"));
            assertTrue(fService.hasAccess(WP, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Xander"));
            fService.modifyLock(WP, "ScrapHeap/Adam/plans.txt", null, "LA", null, null);
            assertTrue(fService.hasAccess(WP, "LA:/ScrapHeap/Adam/plans.txt", "Buffy"));
            assertFalse(fService.hasAccess(WP, "LA:/ScrapHeap/Adam/plans.txt", "Spike"));
            assertTrue(fService.hasAccess(WP, "LA:/ScrapHeap/Adam/plans.txt", "Willow"));
            assertTrue(fService.hasAccess(WP, "LA:/ScrapHeap/Adam/plans.txt", "Tara"));
            assertTrue(fService.hasAccess(WP, "LA:/ScrapHeap/Adam/plans.txt", "Xander"));
            assertFalse(fService.hasAccess(WP, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Buffy"));
            assertFalse(fService.hasAccess(WP, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Spike"));
            assertFalse(fService.hasAccess(WP, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Willow"));
            assertFalse(fService.hasAccess(WP, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Tara"));
            assertFalse(fService.hasAccess(WP, "Sunnydale:/ScrapHeap/Adam/plans.txt", "Xander"));
            List<String> usersToAdd = new ArrayList<String>();
            usersToAdd.add("Spike");
            fService.modifyLock(WP, "ScrapHeap/Adam/plans.txt", null, null, null, usersToAdd);
            assertTrue(fService.hasAccess(WP, "LA:/ScrapHeap/Adam/plans.txt", "Buffy"));
            assertTrue(fService.hasAccess(WP, "LA:/ScrapHeap/Adam/plans.txt", "Spike"));
            assertTrue(fService.hasAccess(WP, "LA:/ScrapHeap/Adam/plans.txt", "Willow"));
            assertTrue(fService.hasAccess(WP, "LA:/ScrapHeap/Adam/plans.txt", "Tara"));
            assertTrue(fService.hasAccess(WP, "LA:/ScrapHeap/Adam/plans.txt", "Xander"));
            List<String> usersToRemove = new ArrayList<String>();
            usersToRemove.add("GROUP_Scoobies");
            fService.modifyLock(WP, "ScrapHeap/Adam/plans.txt", null, null, usersToRemove, null);
            assertFalse(fService.hasAccess(WP, "LA:/ScrapHeap/Adam/plans.txt", "Buffy"));
            assertTrue(fService.hasAccess(WP, "LA:/ScrapHeap/Adam/plans.txt", "Spike"));
            assertFalse(fService.hasAccess(WP, "LA:/ScrapHeap/Adam/plans.txt", "Willow"));
            assertTrue(fService.hasAccess(WP, "LA:/ScrapHeap/Adam/plans.txt", "Tara"));
            assertFalse(fService.hasAccess(WP, "LA:/ScrapHeap/Adam/plans.txt", "Xander"));
            assertTrue(fService.hasAccess(WP, "LA:/ScrapHeap/Adam/plans.txt", AuthenticationUtil.getAdminUserName()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }
}
