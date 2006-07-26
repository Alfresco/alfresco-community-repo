/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.security.authority;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
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
        authenticationComponentImpl = (AuthenticationComponent) ctx.getBean("authenticationComponentImpl");
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
}
