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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.authority;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

public class AuthorityServiceTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private AuthenticationComponent authenticationComponent;
    
    private AuthenticationComponent authenticationComponentImpl;

    private AuthenticationService authenticationService;
    
    private MutableAuthenticationDao authenticationDAO;

    private AuthorityService authorityService;

    private AuthorityService pubAuthorityService;

    private PersonService personService;

    private UserTransaction tx;

    public AuthorityServiceTest()
    {
        super();

    }

    public void setUp() throws Exception
    {
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        authenticationComponentImpl = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        authenticationService = (AuthenticationService) ctx.getBean("authenticationService");
        authorityService = (AuthorityService) ctx.getBean("authorityService");
        pubAuthorityService = (AuthorityService) ctx.getBean("AuthorityService");
        personService = (PersonService) ctx.getBean("personService");
        authenticationDAO = (MutableAuthenticationDao) ctx.getBean("authenticationDao");
        
        authenticationComponentImpl.setSystemUserAsCurrentUser();
        
        TransactionService transactionService = (TransactionService) ctx.getBean(ServiceRegistry.TRANSACTION_SERVICE
                .getLocalName());
        tx = transactionService.getUserTransaction();
        tx.begin();

        if (!authenticationDAO.userExists("andy"))
        {
            authenticationService.createAuthentication("andy", "andy".toCharArray());
        }

        if (!authenticationDAO.userExists("admin"))
        {
            authenticationService.createAuthentication("admin", "admin".toCharArray());
        }

        if (!authenticationDAO.userExists("administrator"))
        {
            authenticationService.createAuthentication("administrator", "administrator".toCharArray());
        }
        
    }

    @Override
    protected void tearDown() throws Exception
    {
        authenticationComponentImpl.clearCurrentSecurityContext();
        tx.rollback();
        super.tearDown();
    }

    public void testNonAdminUser()
    {
        authenticationComponent.setCurrentUser("andy");
        assertFalse(authorityService.hasAdminAuthority());
        assertFalse(pubAuthorityService.hasAdminAuthority());
        assertEquals(1, authorityService.getAuthorities().size());
    }

    public void testAdminUser()
    {
        authenticationComponent.setCurrentUser("admin");
        assertTrue(authorityService.hasAdminAuthority());
        assertTrue(pubAuthorityService.hasAdminAuthority());
        assertEquals(2, authorityService.getAuthorities().size());

        authenticationComponent.setCurrentUser("administrator");
        assertTrue(authorityService.hasAdminAuthority());
        assertTrue(pubAuthorityService.hasAdminAuthority());
        assertEquals(2, authorityService.getAuthorities().size());
    }

    public void testAuthorities()
    {
        assertEquals(1, pubAuthorityService.getAllAuthorities(AuthorityType.ADMIN).size());
        assertTrue(pubAuthorityService.getAllAuthorities(AuthorityType.ADMIN).contains(
                PermissionService.ADMINISTRATOR_AUTHORITY));
        assertEquals(1, pubAuthorityService.getAllAuthorities(AuthorityType.EVERYONE).size());
        assertTrue(pubAuthorityService.getAllAuthorities(AuthorityType.EVERYONE).contains(
                PermissionService.ALL_AUTHORITIES));
        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertFalse(pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).contains(
                PermissionService.ALL_AUTHORITIES));
        assertEquals(1, pubAuthorityService.getAllAuthorities(AuthorityType.GUEST).size());
        assertTrue(pubAuthorityService.getAllAuthorities(AuthorityType.GUEST).contains(PermissionService.GUEST_AUTHORITY));
        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.OWNER).size());
        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(personService.getAllPeople().size(), pubAuthorityService.getAllAuthorities(AuthorityType.USER)
                .size());

    }

    public void testCreateAdminAuth()
    {
        try
        {
            pubAuthorityService.createAuthority(AuthorityType.ADMIN, null, "woof");
            fail("Should not be able to create an admin authority");
        }
        catch (AuthorityException ae)
        {

        }
    }

    public void testCreateEveryoneAuth()
    {
        try
        {
            pubAuthorityService.createAuthority(AuthorityType.EVERYONE, null, "woof");
            fail("Should not be able to create an everyone authority");
        }
        catch (AuthorityException ae)
        {

        }
    }

    public void testCreateGuestAuth()
    {
        try
        {
            pubAuthorityService.createAuthority(AuthorityType.GUEST, null, "woof");
            fail("Should not be able to create an guest authority");
        }
        catch (AuthorityException ae)
        {

        }
    }

    public void testCreateOwnerAuth()
    {
        try
        {
            pubAuthorityService.createAuthority(AuthorityType.OWNER, null, "woof");
            fail("Should not be able to create an owner authority");
        }
        catch (AuthorityException ae)
        {

        }
    }

    public void testCreateUserAuth()
    {
        try
        {
            pubAuthorityService.createAuthority(AuthorityType.USER, null, "woof");
            fail("Should not be able to create an user authority");
        }
        catch (AuthorityException ae)
        {

        }
    }

    public void testCreateRootAuth()
    {
        String auth;

        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "woof");
        assertEquals(1, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        pubAuthorityService.deleteAuthority(auth);
        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());

        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        auth = pubAuthorityService.createAuthority(AuthorityType.ROLE, null, "woof");
        assertEquals(1, pubAuthorityService.getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        pubAuthorityService.deleteAuthority(auth);
        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
    }

    public void testCreateAuth()
    {
        String auth1;
        String auth2;
        String auth3;
        String auth4;
        String auth5;

        assertFalse(pubAuthorityService.authorityExists(pubAuthorityService.getName(AuthorityType.GROUP, "one")));
        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth1 = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "one");
        assertTrue(pubAuthorityService.authorityExists(auth1));
        assertEquals(1, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth2 = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "two");
        assertEquals(2, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth3 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth1, "three");
        assertEquals(3, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth4 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth1, "four");
        assertEquals(4, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth5 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth2, "five");
        assertEquals(5, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());

        pubAuthorityService.deleteAuthority(auth5);
        assertEquals(4, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        pubAuthorityService.deleteAuthority(auth4);
        assertEquals(3, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        pubAuthorityService.deleteAuthority(auth3);
        assertEquals(2, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        pubAuthorityService.deleteAuthority(auth2);
        assertEquals(1, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        pubAuthorityService.deleteAuthority(auth1);
        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());

        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        auth1 = pubAuthorityService.createAuthority(AuthorityType.ROLE, null, "one");
        assertEquals(1, pubAuthorityService.getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        auth2 = pubAuthorityService.createAuthority(AuthorityType.ROLE, null, "two");
        assertEquals(2, pubAuthorityService.getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        auth3 = pubAuthorityService.createAuthority(AuthorityType.ROLE, auth1, "three");
        assertEquals(3, pubAuthorityService.getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        auth4 = pubAuthorityService.createAuthority(AuthorityType.ROLE, auth1, "four");
        assertEquals(4, pubAuthorityService.getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        auth5 = pubAuthorityService.createAuthority(AuthorityType.ROLE, auth2, "five");
        assertEquals(5, pubAuthorityService.getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());

        pubAuthorityService.deleteAuthority(auth5);
        assertEquals(4, pubAuthorityService.getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        pubAuthorityService.deleteAuthority(auth4);
        assertEquals(3, pubAuthorityService.getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        pubAuthorityService.deleteAuthority(auth3);
        assertEquals(2, pubAuthorityService.getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        pubAuthorityService.deleteAuthority(auth2);
        assertEquals(1, pubAuthorityService.getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        pubAuthorityService.deleteAuthority(auth1);
        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
    }

    public void testCreateAuthTree()
    {
        personService.getPerson("andy");
        
        String auth1;
        String auth2;
        String auth3;
        String auth4;
        String auth5;

        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth1 = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "one");
        assertEquals("GROUP_one", auth1);
        assertEquals(1, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth2 = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "two");
        assertEquals("GROUP_two", auth2);
        assertEquals(2, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth3 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth1, "three");
        assertEquals("GROUP_three", auth3);
        assertEquals(3, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth4 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth1, "four");
        assertEquals("GROUP_four", auth4);
        assertEquals(4, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth5 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth2, "five");
        assertEquals("GROUP_five", auth5);
        assertEquals(5, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());

        assertEquals(3, pubAuthorityService.getAllAuthorities(AuthorityType.USER).size());
        pubAuthorityService.addAuthority(auth5, "andy");
        assertEquals(5, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        // The next call looks for people not users :-)
        assertEquals(3, pubAuthorityService.getAllAuthorities(AuthorityType.USER).size());
        assertEquals(2, pubAuthorityService.getContainingAuthorities(null, "andy", false).size());
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "andy", false).contains(auth5));
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "andy", false).contains(auth2));
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, auth5, false).size());
        assertTrue(pubAuthorityService.getContainingAuthorities(null, auth5, false).contains(auth2));

        assertEquals(2, pubAuthorityService.getContainedAuthorities(null, auth2, false).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth2, false).contains(auth5));
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth2, false).contains("andy"));

        assertEquals(1, pubAuthorityService.getContainedAuthorities(null, auth5, false).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth5, false).contains("andy"));

        pubAuthorityService.removeAuthority(auth5, "andy");
        assertEquals(5, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        // The next call looks for people not users :-)
        assertEquals(3, pubAuthorityService.getAllAuthorities(AuthorityType.USER).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(null, "andy", false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, auth5, false).size());
        assertTrue(pubAuthorityService.getContainingAuthorities(null, auth5, false).contains(auth2));

        assertEquals(1, pubAuthorityService.getContainedAuthorities(null, auth2, false).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth2, false).contains(auth5));

        assertEquals(0, pubAuthorityService.getContainedAuthorities(null, auth5, false).size());
    }

    public void testCreateAuthNet()
    {
        personService.getPerson("andy");
        
        String auth1;
        String auth2;
        String auth3;
        String auth4;
        String auth5;

        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth1 = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "one");
        assertEquals(1, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth2 = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "two");
        assertEquals(2, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth3 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth1, "three");
        assertEquals(3, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth4 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth1, "four");
        assertEquals(4, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth5 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth2, "five");
        assertEquals(5, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());

        assertEquals(3, pubAuthorityService.getAllAuthorities(AuthorityType.USER).size());
        pubAuthorityService.addAuthority(auth5, "andy");
        pubAuthorityService.addAuthority(auth1, "andy");
        
        assertEquals(5, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        // The next call looks for people not users :-)
        assertEquals(3, pubAuthorityService.getAllAuthorities(AuthorityType.USER).size());
        assertEquals(3, pubAuthorityService.getContainingAuthorities(null, "andy", false).size());
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "andy", false).contains(auth5));
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "andy", false).contains(auth2));
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "andy", false).contains(auth1));
        
        assertEquals(2, pubAuthorityService.getContainedAuthorities(null, auth2, false).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth2, false).contains(auth5));
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth2, false).contains("andy"));
        assertEquals(3, pubAuthorityService.getContainedAuthorities(null, auth1, false).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth1, false).contains(auth3));
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth1, false).contains(auth4));
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth1, false).contains("andy"));

        pubAuthorityService.removeAuthority(auth1, "andy");
        
        assertEquals(5, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        // The next call looks for people not users :-)
        assertEquals(3, pubAuthorityService.getAllAuthorities(AuthorityType.USER).size());
        assertEquals(2, pubAuthorityService.getContainingAuthorities(null, "andy", false).size());
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "andy", false).contains(auth5));
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "andy", false).contains(auth2));
        
        assertEquals(2, pubAuthorityService.getContainedAuthorities(null, auth2, false).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth2, false).contains(auth5));
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth2, false).contains("andy"));
        assertEquals(2, pubAuthorityService.getContainedAuthorities(null, auth1, false).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth1, false).contains(auth3));
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth1, false).contains(auth4));        
    }
    
    public void testCreateAuthNet2()
    {
        personService.getPerson("andy");
        
        String auth1;
        String auth2;
        String auth3;
        String auth4;
        String auth5;

        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth1 = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "one");
        assertEquals(1, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth2 = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "two");
        assertEquals(2, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth3 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth1, "three");
        assertEquals(3, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth4 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth1, "four");
        assertEquals(4, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth5 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth2, "five");
        assertEquals(5, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        
        assertEquals(3, pubAuthorityService.getAllAuthorities(AuthorityType.USER).size());
        pubAuthorityService.addAuthority(auth5, "andy");
        pubAuthorityService.addAuthority(auth1, "andy");
        
        assertEquals(5, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        // The next call looks for people not users :-)
        assertEquals(3, pubAuthorityService.getAllAuthorities(AuthorityType.USER).size());
        assertEquals(3, pubAuthorityService.getContainingAuthorities(null, "andy", false).size());
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "andy", false).contains(auth5));
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "andy", false).contains(auth2));
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "andy", false).contains(auth1));
        
        assertEquals(2, pubAuthorityService.getContainedAuthorities(null, auth2, false).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth2, false).contains(auth5));
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth2, false).contains("andy"));
        assertEquals(3, pubAuthorityService.getContainedAuthorities(null, auth1, false).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth1, false).contains(auth3));
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth1, false).contains(auth4));
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth1, false).contains("andy"));
        
        
        pubAuthorityService.addAuthority(auth3, auth2);
        
        assertEquals(5, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        // The next call looks for people not users :-)
        assertEquals(3, pubAuthorityService.getAllAuthorities(AuthorityType.USER).size());
        assertEquals(4, pubAuthorityService.getContainingAuthorities(null, "andy", false).size());
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "andy", false).contains(auth5));
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "andy", false).contains(auth2));
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "andy", false).contains(auth1));
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "andy", false).contains(auth3));
        
        assertEquals(2, pubAuthorityService.getContainedAuthorities(null, auth2, false).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth2, false).contains(auth5));
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth2, false).contains("andy"));
        assertEquals(5, pubAuthorityService.getContainedAuthorities(null, auth1, false).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth1, false).contains(auth3));
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth1, false).contains(auth4));
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth1, false).contains(auth2));
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth1, false).contains(auth5));
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth1, false).contains("andy"));
        
    }

    
    public void test_AR_1510()
    {
        personService.getPerson("andy1");
        personService.getPerson("andy2");
        personService.getPerson("andy3");
        personService.getPerson("andy4");
        personService.getPerson("andy5");
        personService.getPerson("andy6");
        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        String auth1 = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "one");
        pubAuthorityService.addAuthority(auth1, "andy1");
        String auth2 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth1, "two");
        pubAuthorityService.addAuthority(auth2, "andy1");
        pubAuthorityService.addAuthority(auth2, "andy2");
        String auth3 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth2, "three");
        pubAuthorityService.addAuthority(auth3, "andy3");
        String auth4 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth3, "four");
        pubAuthorityService.addAuthority(auth4, "andy1");
        pubAuthorityService.addAuthority(auth4, "andy4");
        String auth5 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth4, "five");
        pubAuthorityService.addAuthority(auth5, "andy1");
        pubAuthorityService.addAuthority(auth5, "andy5");
        String auth6 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth3, "six");
        pubAuthorityService.addAuthority(auth6, "andy1");
        pubAuthorityService.addAuthority(auth6, "andy5");
        pubAuthorityService.addAuthority(auth6, "andy6");
       
        assertEquals(2, pubAuthorityService.getContainedAuthorities(null, auth1, true).size());
        assertEquals(11, pubAuthorityService.getContainedAuthorities(null, auth1, false).size());
        assertEquals(3, pubAuthorityService.getContainedAuthorities(null, auth2, true).size());
        assertEquals(10, pubAuthorityService.getContainedAuthorities(null, auth2, false).size());
        assertEquals(3, pubAuthorityService.getContainedAuthorities(null, auth3, true).size());
        assertEquals(8, pubAuthorityService.getContainedAuthorities(null, auth3, false).size());
        assertEquals(3, pubAuthorityService.getContainedAuthorities(null, auth4, true).size());
        assertEquals(4, pubAuthorityService.getContainedAuthorities(null, auth4, false).size());
        assertEquals(2, pubAuthorityService.getContainedAuthorities(null, auth5, true).size());
        assertEquals(2, pubAuthorityService.getContainedAuthorities(null, auth5, false).size());
        assertEquals(3, pubAuthorityService.getContainedAuthorities(null, auth6, true).size());
        assertEquals(3, pubAuthorityService.getContainedAuthorities(null, auth6, false).size());
        
        assertEquals(0, pubAuthorityService.getContainedAuthorities(null, "andy1", true).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(null, "andy1", false).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(null, "andy2", true).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(null, "andy2", false).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(null, "andy3", true).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(null, "andy3", false).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(null, "andy4", true).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(null, "andy4", false).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(null, "andy5", true).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(null, "andy5", false).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(null, "andy6", true).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(null, "andy6", false).size());
     
        assertEquals(1, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth1, true).size());
        assertEquals(5, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth1, false).size());
        assertEquals(1, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth2, true).size());
        assertEquals(4, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth2, false).size());
        assertEquals(2, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth3, true).size());
        assertEquals(3, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth3, false).size());
        assertEquals(1, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth4, true).size());
        assertEquals(1, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth4, false).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth5, true).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth5, false).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth6, true).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth6, false).size());
        
        assertEquals(1, pubAuthorityService.getContainedAuthorities(AuthorityType.USER, auth1, true).size());
        assertEquals(6, pubAuthorityService.getContainedAuthorities(AuthorityType.USER, auth1, false).size());
        assertEquals(2, pubAuthorityService.getContainedAuthorities(AuthorityType.USER, auth2, true).size());
        assertEquals(6, pubAuthorityService.getContainedAuthorities(AuthorityType.USER, auth2, false).size());
        assertEquals(1, pubAuthorityService.getContainedAuthorities(AuthorityType.USER, auth3, true).size());
        assertEquals(5, pubAuthorityService.getContainedAuthorities(AuthorityType.USER, auth3, false).size());
        assertEquals(2, pubAuthorityService.getContainedAuthorities(AuthorityType.USER, auth4, true).size());
        assertEquals(3, pubAuthorityService.getContainedAuthorities(AuthorityType.USER, auth4, false).size());
        assertEquals(2, pubAuthorityService.getContainedAuthorities(AuthorityType.USER, auth5, true).size());
        assertEquals(2, pubAuthorityService.getContainedAuthorities(AuthorityType.USER, auth5, false).size());
        assertEquals(3, pubAuthorityService.getContainedAuthorities(AuthorityType.USER, auth6, true).size());
        assertEquals(3, pubAuthorityService.getContainedAuthorities(AuthorityType.USER, auth6, false).size());
        
        // containing
        
        assertEquals(0, pubAuthorityService.getContainingAuthorities(null, auth1, true).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(null, auth1, false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, auth2, true).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, auth2, false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, auth3, true).size());
        assertEquals(2, pubAuthorityService.getContainingAuthorities(null, auth3, false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, auth4, true).size());
        assertEquals(3, pubAuthorityService.getContainingAuthorities(null, auth4, false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, auth5, true).size());
        assertEquals(4, pubAuthorityService.getContainingAuthorities(null, auth5, false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, auth6, true).size());
        assertEquals(3, pubAuthorityService.getContainingAuthorities(null, auth6, false).size());
        
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, auth1, true).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, auth1, false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, auth2, true).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, auth2, false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, auth3, true).size());
        assertEquals(2, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, auth3, false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, auth4, true).size());
        assertEquals(3, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, auth4, false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, auth5, true).size());
        assertEquals(4, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, auth5, false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, auth6, true).size());
        assertEquals(3, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, auth6, false).size());
        
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, auth1, true).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, auth1, false).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, auth2, true).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, auth2, false).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, auth3, true).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, auth3, false).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, auth4, true).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, auth4, false).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, auth5, true).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, auth5, false).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, auth6, true).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, auth6, false).size());
        
        
        assertEquals(5, pubAuthorityService.getContainingAuthorities(null, "andy1", true).size());
        assertEquals(6, pubAuthorityService.getContainingAuthorities(null, "andy1", false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, "andy2", true).size());
        assertEquals(2, pubAuthorityService.getContainingAuthorities(null, "andy2", false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, "andy3", true).size());
        assertEquals(3, pubAuthorityService.getContainingAuthorities(null, "andy3", false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, "andy4", true).size());
        assertEquals(4, pubAuthorityService.getContainingAuthorities(null, "andy4", false).size());
        assertEquals(2, pubAuthorityService.getContainingAuthorities(null, "andy5", true).size());
        assertEquals(6, pubAuthorityService.getContainingAuthorities(null, "andy5", false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, "andy6", true).size());
        assertEquals(4, pubAuthorityService.getContainingAuthorities(null, "andy6", false).size());
        
        assertEquals(5, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, "andy1", true).size());
        assertEquals(6, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, "andy1", false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, "andy2", true).size());
        assertEquals(2, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, "andy2", false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, "andy3", true).size());
        assertEquals(3, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, "andy3", false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, "andy4", true).size());
        assertEquals(4, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, "andy4", false).size());
        assertEquals(2, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, "andy5", true).size());
        assertEquals(6, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, "andy5", false).size());
        assertEquals(1, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, "andy6", true).size());
        assertEquals(4, pubAuthorityService.getContainingAuthorities(AuthorityType.GROUP, "andy6", false).size());
        
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, "andy1", true).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, "andy1", false).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, "andy2", true).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, "andy2", false).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, "andy3", true).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, "andy3", false).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, "andy4", true).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, "andy4", false).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, "andy5", true).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, "andy5", false).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, "andy6", true).size());
        assertEquals(0, pubAuthorityService.getContainingAuthorities(AuthorityType.USER, "andy6", false).size());
    }
    
    
    
    /**
     * Test toknisation of group members
     *
     */
    public void test_AR_1517__AND__AR_1411()
    {
        personService.getPerson("1234");
        assertTrue(personService.personExists("1234"));
        personService.getPerson("Loon");
        assertTrue(personService.personExists("Loon"));
        personService.getPerson("andy");
        assertTrue(personService.personExists("andy"));
        personService.createPerson(createDefaultProperties("Novalike", "Nova", "Like", "Nove@Like", "Sun", null));
        assertTrue(personService.personExists("Novalike"));
        personService.getPerson("1andy");
        assertTrue(personService.personExists("1andy"));
        personService.getPerson("andy2");
        assertTrue(personService.personExists("andy2"));
        personService.getPerson("an3dy");
        assertTrue(personService.personExists("an3dy"));
        
        
        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        String auth1 = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "one");
        pubAuthorityService.addAuthority(auth1, "1234");
        String auth2 = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "two");
        pubAuthorityService.addAuthority(auth2, "andy");
        String auth3 = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "three");
        pubAuthorityService.addAuthority(auth3, "Novalike");
        String auth4 = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "four");
        pubAuthorityService.addAuthority(auth4, "1andy");
        String auth5 = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "five");
        pubAuthorityService.addAuthority(auth5, "andy2");
        String auth6 = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "six");
        pubAuthorityService.addAuthority(auth6, "an3dy");
        
       
        assertEquals(1, pubAuthorityService.getContainedAuthorities(null, auth1, true).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth1, true).contains("1234"));
        assertEquals(1, pubAuthorityService.getContainedAuthorities(null, auth2, true).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth2, true).contains("andy"));
        assertEquals(1, pubAuthorityService.getContainedAuthorities(null, auth3, true).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth3, true).contains("Novalike"));
        assertEquals(1, pubAuthorityService.getContainedAuthorities(null, auth4, true).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth4, true).contains("1andy"));
        assertEquals(1, pubAuthorityService.getContainedAuthorities(null, auth5, true).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth5, true).contains("andy2"));
        assertEquals(1, pubAuthorityService.getContainedAuthorities(null, auth6, true).size());
        assertTrue(pubAuthorityService.getContainedAuthorities(null, auth6, true).contains("an3dy"));
        
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, "1234", false).size());
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "1234", false).contains(auth1));
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, "andy", false).size());
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "andy", false).contains(auth2));
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, "Novalike", false).size());
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "Novalike", false).contains(auth3));
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, "1andy", false).size());
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "1andy", false).contains(auth4));
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, "andy2", false).size());
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "andy2", false).contains(auth5));
        assertEquals(1, pubAuthorityService.getContainingAuthorities(null, "an3dy", false).size());
        assertTrue(pubAuthorityService.getContainingAuthorities(null, "an3dy", false).contains(auth6));
        
    }
     
    
    public void testGroupNameTokenisation()
    {
        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());

        String auth1234 = pubAuthorityService.createAuthority(AuthorityType.GROUP, null, "1234");
        assertEquals(0, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth1234, false).size());
        String authC1 = pubAuthorityService.createAuthority(AuthorityType.GROUP, auth1234, "circle");
        assertEquals(1, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth1234, false).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, authC1, false).size());
        String authC2 = pubAuthorityService.createAuthority(AuthorityType.GROUP, authC1, "bigCircle");
        assertEquals(2, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth1234, false).size());
        assertEquals(1, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, authC1, false).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, authC2, false).size());
        String authStuff = pubAuthorityService.createAuthority(AuthorityType.GROUP, authC2, "|<>?~@:}{+_)(*&^%$£!¬`\\,./#';][=-0987654321 1234556678 '");
        assertEquals(3, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth1234, false).size());
        assertEquals(2, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, authC1, false).size());
        assertEquals(1, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, authC2, false).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, authStuff, false).size());
        String authSpace = pubAuthorityService.createAuthority(AuthorityType.GROUP, authStuff, "  Circles     ");
        assertEquals(4, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth1234, false).size());
        assertEquals(3, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, authC1, false).size());
        assertEquals(2, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, authC2, false).size());
        assertEquals(1, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, authStuff, false).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, authSpace, false).size());
        
        pubAuthorityService.deleteAuthority(authSpace);
        pubAuthorityService.deleteAuthority(authStuff);
        pubAuthorityService.deleteAuthority(authC2);
        pubAuthorityService.deleteAuthority(authC1);
        pubAuthorityService.deleteAuthority(auth1234);
        
        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
    }
    
    private Map<QName, Serializable> createDefaultProperties(String userName, String firstName, String lastName,
            String email, String orgId, NodeRef home)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        properties.put(ContentModel.PROP_HOMEFOLDER, home);
        properties.put(ContentModel.PROP_FIRSTNAME, firstName);
        properties.put(ContentModel.PROP_LASTNAME, lastName);
        properties.put(ContentModel.PROP_EMAIL, email);
        properties.put(ContentModel.PROP_ORGID, orgId);
        return properties;
    }
}
