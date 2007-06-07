/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponentImpl;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.avm.locking.AVMLock;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import junit.framework.TestCase;

/**
 * Tests for AVM locking service.
 * @author britt
 */
public class AVMLockingServiceTest extends TestCase
{
    private static FileSystemXmlApplicationContext fContext = null;
    
    private static AVMLockingService fService;
    
    private static AttributeService fAttributeService;
    
    private static PersonService fPersonService;
    
    private static AuthorityService fAuthorityService;
    
    private static AuthenticationService fAuthenticationService;
    
    private static AuthenticationComponent fAuthenticationComponent;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        if (fContext == null)
        {
            fContext = new FileSystemXmlApplicationContext("config/alfresco/application-context.xml");
            fService = (AVMLockingService)fContext.getBean("AVMLockingService");
            fAttributeService = (AttributeService)fContext.getBean("AttributeService");
            fPersonService = (PersonService)fContext.getBean("PersonService");
            fAuthorityService = (AuthorityService)fContext.getBean("AuthorityService");
            fAuthenticationService = (AuthenticationService)fContext.getBean("AuthenticationService");
            fAuthenticationComponent = (AuthenticationComponent)fContext.getBean("AuthenticationComponent");
            fAuthenticationComponent.setSystemUserAsCurrentUser();
        }
        // Set up sample users groups and roles.
        fAuthenticationService.createAuthentication("Buffy", "Buffy".toCharArray());
        fPersonService.getPerson("Buffy");
        fAuthorityService.createAuthority(AuthorityType.GROUP, null, "Scoobies");
        fAuthorityService.addAuthority("GROUP_Scoobies", "Buffy");
        fAuthorityService.createAuthority(AuthorityType.ROLE, null, "SUPER_POWERED");
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
        fAuthorityService.createAuthority(AuthorityType.GROUP, null, "vampires");
        fAuthorityService.addAuthority("GROUP_vampires", "Spike");
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        List<String> keys = fAttributeService.getKeys("");
        for (String key : keys)
        {
            fAttributeService.removeAttribute("", key);
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
    }
    
    public void testAll()
    {
        try
        {
            fService.addWebProject("alfresco");
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            List<String> owners = new ArrayList<String>();
            owners.add("Buffy");
            owners.add("Spike");
            AVMLock lock = new AVMLock("alfresco",
                                       "Sunnydale",
                                       "Revello Drive/1630",
                                       AVMLockingService.Type.DISCRETIONARY,
                                       owners);
            fService.lockPath(lock);
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            assertNotNull(fService.getLock("alfresco", "Revello Drive/1630"));
            assertEquals(1, fService.getUsersLocks("Buffy").size());
            assertEquals(1, fService.getWebProjectLocks("alfresco").size());
            List<String> owners2 = new ArrayList<String>();
            owners2.add("Buffy");
            owners2.add("Willow");
            AVMLock lock2 = new AVMLock("alfresco",
                                        "Sunnydale",
                                        "UC Sunnydale/Stevenson Hall",
                                        AVMLockingService.Type.DISCRETIONARY,
                                        owners2);
            fService.lockPath(lock2);
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            assertEquals(2, fService.getUsersLocks("Buffy").size());
            assertEquals(2, fService.getWebProjectLocks("alfresco").size());
            fService.removeLock("alfresco", "Revello Drive/1630");
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            assertEquals(1, fService.getUsersLocks("Buffy").size());
            assertEquals(1, fService.getWebProjectLocks("alfresco").size());
            fService.removeWebProject("alfresco");
            System.out.println(fAttributeService.getAttribute(".avm_lock_table"));
            assertEquals(0, fService.getUsersLocks("Spike").size());
            assertEquals(0, fService.getUsersLocks("Buffy").size());
            assertEquals(0, fService.getUsersLocks("Willow").size());
            assertEquals(0, fService.getUsersLocks("Tara").size());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }
}
