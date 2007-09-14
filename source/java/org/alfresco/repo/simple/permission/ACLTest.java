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

package org.alfresco.repo.simple.permission;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.simple.permission.ACL;
import org.alfresco.service.simple.permission.AuthorityCapabilityRegistry;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import junit.framework.TestCase;

/**
 * Rudimentary test of ACLs.
 * @author britt
 */
public class ACLTest extends TestCase
{
    private static FileSystemXmlApplicationContext fContext = null;
    
    private static PersonService fPersonService;
    
    private static AuthorityService fAuthorityService;
    
    private static AuthenticationService fAuthenticationService;
    
    private static AuthenticationComponent fAuthenticationComponent;
    
    private static AuthorityCapabilityRegistry fCapabilityRegistry;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        if (fContext == null)
        {
            fContext = new FileSystemXmlApplicationContext("config/alfresco/application-context.xml");
            fPersonService = (PersonService)fContext.getBean("PersonService");
            fAuthorityService = (AuthorityService)fContext.getBean("AuthorityService");
            fAuthenticationService = (AuthenticationService)fContext.getBean("AuthenticationService");
            fAuthenticationComponent = (AuthenticationComponent)fContext.getBean("AuthenticationComponent");
            fAuthenticationComponent.setSystemUserAsCurrentUser();
            fCapabilityRegistry = (AuthorityCapabilityRegistry)fContext.getBean("authorityCapabilityRegistry");
        }
        // Set up sample users groups and roles.
        fAuthenticationService.createAuthentication("Buffy", "Buffy".toCharArray());
        fPersonService.getPerson("Buffy");
        fAuthorityService.createAuthority(AuthorityType.GROUP, null, "Scoobies");
        fAuthorityService.addAuthority("GROUP_Scoobies", "Buffy");
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
        fAuthorityService.createAuthority(AuthorityType.GROUP, null, "vampires");
        fAuthorityService.addAuthority("GROUP_vampires", "Spike");
        fAuthorityService.createAuthority(AuthorityType.GROUP, null, "soulless");
        fAuthorityService.addAuthority("GROUP_soulless", "Spike");
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
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
        fAuthorityService.deleteAuthority("GROUP_vampires");
        fAuthorityService.deleteAuthority("GROUP_soulless");
    }
    
    public void testBasic()
    {
        try
        {
            Set<String> allCaps = fCapabilityRegistry.getAllCapabilities();
            System.out.println(allCaps);
            System.out.println(fCapabilityRegistry.getAllAuthorities());
            ACL acl = new ACLImpl(true);
            acl.allow("read", "GROUP_Scoobies", "GROUP_vampires");
            acl.allow("write", "GROUP_Scoobies", "GROUP_vampires");
            acl.allow("delete", "GROUP_Scoobies", "GROUP_vampires");
            acl.allow("shimmy", "GROUP_Scoobies", "GROUP_vampires");
            acl.allow("shake", "GROUP_vampires", "Tara");
            acl.deny("delete", "Xander", "GROUP_soulless");
            acl.deny("shake", "Spike");
            checkEvaluation(allCaps, acl, "Spike");
            checkEvaluation(allCaps, acl, "Tara");
            checkEvaluation(allCaps, acl, "Xander");
            checkEvaluation(allCaps, acl, "Buffy");
            String stringRep = acl.getStringRepresentation();
            System.out.println(stringRep);
            ACL acl2 = new ACLImpl(stringRep);
            System.out.println(acl2.getStringRepresentation());
            checkEvaluation(allCaps, acl2, "Spike");
            checkEvaluation(allCaps, acl2, "Tara");
            checkEvaluation(allCaps, acl2, "Xander");
            checkEvaluation(allCaps, acl2, "Buffy");
            System.out.println(acl2.getStringRepresentation());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    private void checkEvaluation(Set<String> allCaps, ACL acl, String authority)
    {
        Set<String> caps = acl.getCapabilities(authority, false);
        System.out.println(caps);
        for (String cap : caps)
        {
            assertTrue(acl.can(authority, false, cap));
        }
        Set<String> inverse = new HashSet<String>(allCaps);
        inverse.removeAll(caps);
        for (String cap : inverse)
        {
            assertFalse(acl.can(authority, false, cap));
        }
    }
}
