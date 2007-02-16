/*
 * Copyright (C) 2005 Alfresco, Inc.
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

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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

public class SimpleAuthorityServiceTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private AuthenticationComponent authenticationComponent;

    private AuthenticationService authenticationService;

    private AuthorityService authorityService;

    private AuthorityService pubAuthorityService;
    
    private MutableAuthenticationDao authenticationDAO;

    private PersonService personService;

    private UserTransaction tx;

    public SimpleAuthorityServiceTest()
    {
        super();

    }

    public void setUp() throws Exception
    {
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        authenticationService = (AuthenticationService) ctx.getBean("authenticationService");
        authorityService = (AuthorityService) ctx.getBean("authorityService");
        pubAuthorityService = (AuthorityService) ctx.getBean("AuthorityService");
        personService = (PersonService) ctx.getBean("personService");
        authenticationDAO = (MutableAuthenticationDao) ctx.getBean("alfDaoImpl");

        this.authenticationComponent.setSystemUserAsCurrentUser();

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
        AuthenticationUtil.clearCurrentSecurityContext();
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
        assertFalse(authorityService.authorityExists("woof"));
        
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
        assertFalse(pubAuthorityService.authorityExists("woof"));
        assertEquals(1, pubAuthorityService.getAllAuthorities(AuthorityType.ADMIN).size());
        assertTrue(pubAuthorityService.getAllAuthorities(AuthorityType.ADMIN).contains(
                PermissionService.ADMINISTRATOR_AUTHORITY));
        assertEquals(1, pubAuthorityService.getAllAuthorities(AuthorityType.EVERYONE).size());
        assertTrue(pubAuthorityService.getAllAuthorities(AuthorityType.EVERYONE).contains(
                PermissionService.ALL_AUTHORITIES));
        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(1, pubAuthorityService.getAllAuthorities(AuthorityType.GUEST).size());
        assertTrue(pubAuthorityService.getAllAuthorities(AuthorityType.GUEST).contains(PermissionService.GUEST_AUTHORITY));
        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.OWNER).size());
        assertEquals(0, pubAuthorityService.getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(2, pubAuthorityService.getAllAuthorities(AuthorityType.USER).size());
        assertEquals(personService.getAllPeople().size(), pubAuthorityService.getAllAuthorities(AuthorityType.USER)
                .size());

    }

}
