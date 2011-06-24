/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.security.authority;

import java.util.List;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

public class SimpleAuthorityServiceTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private AuthenticationComponent authenticationComponent;

    private MutableAuthenticationService authenticationService;

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
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            throw new AlfrescoRuntimeException(
                    "A previous tests did not clean up transaction: " +
                    AlfrescoTransactionSupport.getTransactionId());
        }
        
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        authenticationService = (MutableAuthenticationService) ctx.getBean("authenticationService");
        authorityService = (AuthorityService) ctx.getBean("authorityService");
        pubAuthorityService = (AuthorityService) ctx.getBean("AuthorityService");
        personService = (PersonService) ctx.getBean("personService");
        authenticationDAO = (MutableAuthenticationDao) ctx.getBean("authenticationDao");

        this.authenticationComponent.setSystemUserAsCurrentUser();

        TransactionService transactionService = (TransactionService) ctx.getBean(ServiceRegistry.TRANSACTION_SERVICE
                .getLocalName());
        tx = transactionService.getUserTransaction();
        tx.begin();

        if (!authenticationDAO.userExists("andy"))
        {
            authenticationService.createAuthentication("andy", "andy".toCharArray());
        }

        if (!authenticationDAO.userExists(AuthenticationUtil.getAdminUserName()))
        {
            authenticationService.createAuthentication(AuthenticationUtil.getAdminUserName(), "admin".toCharArray());
        }

        if (!authenticationDAO.userExists("administrator"))
        {
            authenticationService.createAuthentication("administrator", "administrator".toCharArray());
        }
    }

    @Override
    protected void tearDown() throws Exception
    {
        if ((tx.getStatus() == Status.STATUS_ACTIVE) || (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK))
        {
            tx.rollback();
        }
        AuthenticationUtil.clearCurrentSecurityContext();
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
        
        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        assertTrue(authorityService.hasAdminAuthority());
        assertTrue(pubAuthorityService.hasAdminAuthority());
        assertEquals(6, authorityService.getAuthorities().size());
    }

    public void testAuthorities()
    {
        assertFalse(pubAuthorityService.authorityExists("woof"));
        assertEquals(1, getAllAuthorities(AuthorityType.ADMIN).size());
        assertTrue(getAllAuthorities(AuthorityType.ADMIN).contains(PermissionService.ADMINISTRATOR_AUTHORITY));
        assertEquals(1, getAllAuthorities(AuthorityType.EVERYONE).size());
        assertTrue(getAllAuthorities(AuthorityType.EVERYONE).contains(PermissionService.ALL_AUTHORITIES));
        assertEquals(7, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(1, getAllAuthorities(AuthorityType.GUEST).size());
        assertTrue(getAllAuthorities(AuthorityType.GUEST).contains(PermissionService.GUEST_AUTHORITY));
        assertEquals(0, getAllAuthorities(AuthorityType.OWNER).size());
        assertEquals(0, getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(2, getAllAuthorities(AuthorityType.USER).size());
        
        int peopleCnt = personService.getPeople(null, true, null, new PagingRequest(0, Integer.MAX_VALUE, null)).getPage().size();
        assertEquals(peopleCnt, getAllAuthorities(AuthorityType.USER).size());
    }
    
    private List<String> getAllAuthorities(AuthorityType type)
    {
        return pubAuthorityService.getAuthorities(type, null, null, false, true, new PagingRequest(0, Integer.MAX_VALUE, null)).getPage();
    }

}
