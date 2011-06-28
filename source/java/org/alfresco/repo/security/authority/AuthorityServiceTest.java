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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

public class AuthorityServiceTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private AuthenticationComponent authenticationComponent;
    private MutableAuthenticationService authenticationService;
    private MutableAuthenticationDao authenticationDAO;
    private AuthorityService authorityService;
    private AuthorityService pubAuthorityService;
    private PersonService personService;
    private UserTransaction tx;
    private AclDAO aclDaoComponent;
    private NodeService nodeService;
    
    public AuthorityServiceTest()
    {
        super();
    }
    
    private static final int DEFAULT_SITE_GRP_CNT = 5;      // default number of groups per site
    private static final int DEFAULT_SITE_ROOT_GRP_CNT = 1; // default number of root groups per site
    private static final int DEFAULT_GRP_CNT = 2;           // default (non-site) bootstrap groups - eg. GROUP_ALFRESCO_ADMINISTRATORS, GROUP_EMAIL_CONTRIBUTORS
    
    private int SITE_CNT = 0;
    private int GRP_CNT = 0;
    private int ROOT_GRP_CNT = 0;
    
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
        aclDaoComponent = (AclDAO) ctx.getBean("aclDAO");
        nodeService = (NodeService) ctx.getBean("nodeService");
        
        String defaultAdminUser = AuthenticationUtil.getAdminUserName();
        AuthenticationUtil.setFullyAuthenticatedUser(defaultAdminUser);
        
        // note: currently depends on any existing (and/or bootstrap) group data - eg. default site "swsdp" (Sample Web Site Design Project)
        SiteService siteService = (SiteService) ctx.getBean("SiteService");
        SITE_CNT = siteService.listSites(defaultAdminUser).size();
        GRP_CNT = DEFAULT_GRP_CNT + (DEFAULT_SITE_GRP_CNT * SITE_CNT);
        ROOT_GRP_CNT = DEFAULT_GRP_CNT + (DEFAULT_SITE_ROOT_GRP_CNT * SITE_CNT);
        
        TransactionService transactionService = (TransactionService) ctx.getBean(ServiceRegistry.TRANSACTION_SERVICE.getLocalName());
        tx = transactionService.getUserTransaction();
        tx.begin();
        for (String user : getAllAuthorities(AuthorityType.USER))
        {
            if (user.equals(AuthenticationUtil.getGuestUserName()))
            {
                continue;
            }
            else if (user.equals(AuthenticationUtil.getAdminUserName()))
            {
                continue;
            }
            else
            {
                if (personService.personExists(user))
                {
                    NodeRef person = personService.getPerson(user);
                    NodeRef hf = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER));
                    if (hf != null)
                    {
                        nodeService.deleteNode(hf);
                    }
                    aclDaoComponent.deleteAccessControlEntries(user);
                    personService.deletePerson(user);
                }
                if (authenticationDAO.userExists(user))
                {
                    authenticationDAO.deleteUser(user);
                }
            }

        }
        tx.commit();

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

    public void testZones()
    {
        assertNull(pubAuthorityService.getAuthorityZones("GROUP_DEFAULT"));
        assertNull(pubAuthorityService.getAuthorityZones("GROUP_NULL"));
        assertNull(pubAuthorityService.getAuthorityZones("GROUP_EMPTY"));
        assertNull(pubAuthorityService.getAuthorityZones("GROUP_1"));
        assertNull(pubAuthorityService.getAuthorityZones("GROUP_2"));
        assertNull(pubAuthorityService.getAuthorityZones("GROUP_3"));

        pubAuthorityService.createAuthority(AuthorityType.GROUP, "DEFAULT");
        Set<String> zones = pubAuthorityService.getAuthorityZones("GROUP_DEFAULT");
        assertEquals(2, zones.size());
        pubAuthorityService.removeAuthorityFromZones("GROUP_DEFAULT", zones);
        assertEquals(0, pubAuthorityService.getAuthorityZones("GROUP_DEFAULT").size());
        pubAuthorityService.addAuthorityToZones("GROUP_DEFAULT", zones);
        assertEquals(2, pubAuthorityService.getAuthorityZones("GROUP_DEFAULT").size());

        HashSet<String> newZones = null;
        pubAuthorityService.createAuthority(AuthorityType.GROUP, "NULL", "NULL", newZones);
        assertEquals(0, pubAuthorityService.getAuthorityZones("GROUP_NULL").size());

        newZones = new HashSet<String>();
        pubAuthorityService.createAuthority(AuthorityType.GROUP, "EMPTY", "EMPTY", newZones);
        assertEquals(0, pubAuthorityService.getAuthorityZones("GROUP_EMPTY").size());

        newZones.add("One");
        pubAuthorityService.createAuthority(AuthorityType.GROUP, "1", "1", newZones);
        assertEquals(1, pubAuthorityService.getAuthorityZones("GROUP_1").size());

        newZones.add("Two");
        pubAuthorityService.createAuthority(AuthorityType.GROUP, "2", "2", newZones);
        assertEquals(2, pubAuthorityService.getAuthorityZones("GROUP_2").size());

        newZones.add("Three");
        pubAuthorityService.createAuthority(AuthorityType.GROUP, "3", "3", newZones);
        assertEquals(3, pubAuthorityService.getAuthorityZones("GROUP_3").size());

        HashSet<String> toRemove = null;
        pubAuthorityService.removeAuthorityFromZones("GROUP_3", toRemove);
        assertEquals(3, pubAuthorityService.getAuthorityZones("GROUP_3").size());

        toRemove = new HashSet<String>();
        pubAuthorityService.removeAuthorityFromZones("GROUP_3", toRemove);
        assertEquals(3, pubAuthorityService.getAuthorityZones("GROUP_3").size());

        toRemove.add("Three");
        pubAuthorityService.removeAuthorityFromZones("GROUP_3", toRemove);
        assertEquals(2, pubAuthorityService.getAuthorityZones("GROUP_3").size());

        toRemove.add("Two");
        pubAuthorityService.removeAuthorityFromZones("GROUP_3", toRemove);
        assertEquals(1, pubAuthorityService.getAuthorityZones("GROUP_3").size());

        toRemove.add("One");
        pubAuthorityService.removeAuthorityFromZones("GROUP_3", toRemove);
        assertEquals(0, pubAuthorityService.getAuthorityZones("GROUP_3").size());

        pubAuthorityService.addAuthorityToZones("GROUP_3", newZones);
        assertEquals(3, pubAuthorityService.getAuthorityZones("GROUP_3").size());
        assertEquals(3, pubAuthorityService.getAllAuthoritiesInZone("One", null).size());
        assertEquals(2, pubAuthorityService.getAllAuthoritiesInZone("Two", null).size());
        assertEquals(1, pubAuthorityService.getAllAuthoritiesInZone("Three", null).size());
        assertEquals(3, pubAuthorityService.getAllAuthoritiesInZone("One", AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllAuthoritiesInZone("Two", AuthorityType.GROUP).size());
        assertEquals(1, pubAuthorityService.getAllAuthoritiesInZone("Three", AuthorityType.GROUP).size());

        assertEquals(3, pubAuthorityService.getAllRootAuthoritiesInZone("One", null).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthoritiesInZone("Two", null).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthoritiesInZone("Three", null).size());
        assertEquals(3, pubAuthorityService.getAllRootAuthoritiesInZone("One", AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthoritiesInZone("Two", AuthorityType.GROUP).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthoritiesInZone("Three", AuthorityType.GROUP).size());

        pubAuthorityService.addAuthority("GROUP_1", "GROUP_2");
        pubAuthorityService.addAuthority("GROUP_1", "GROUP_3");

        assertEquals(1, pubAuthorityService.getAllRootAuthoritiesInZone("One", null).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthoritiesInZone("Two", null).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthoritiesInZone("Three", null).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthoritiesInZone("One", AuthorityType.GROUP).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthoritiesInZone("Two", AuthorityType.GROUP).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthoritiesInZone("Three", AuthorityType.GROUP).size());
    }

    public void test_ETWOTWO_400()
    {
        pubAuthorityService.createAuthority(AuthorityType.GROUP, "wo\"of");
        Set<String> authorities = pubAuthorityService.findAuthorities(AuthorityType.GROUP, null, true, "wo\"of*", AuthorityService.ZONE_APP_DEFAULT);
        assertEquals(1, authorities.size());
    }
    
    public void testGroupWildcards()
    {
        long before, after;
        char end = 'd';
        String[] zones = new String[] { null, "ONE", "TWO", "THREE" };
        for (String zone : zones)
        {
            for (char i = 'a'; i <= end; i++)
            {
                for (char j = 'a'; j <= end; j++)
                {
                    for (char k = 'a'; k <= end; k++)
                    {
                        StringBuilder name = new StringBuilder();
                        name.append("__").append(zone).append("__").append(i).append(j).append(k);
                        if (zone == null)
                        {
                            pubAuthorityService.createAuthority(AuthorityType.GROUP, name.toString());
                        }
                        else
                        {
                            pubAuthorityService.createAuthority(AuthorityType.GROUP, name.toString(), name.toString(), Collections.singleton(zone));
                        }
                    }
                }
            }
        }
        int size = end - 'a' + 1;
        before = System.nanoTime();
        Set<String> matches = pubAuthorityService.findAuthorities(AuthorityType.GROUP, null, false, "__*__a*", null);
        after = System.nanoTime();
        System.out.println("GROUP___a* in " + ((after - before) / 1000000000.0f));
        assertEquals(size * size * zones.length, matches.size());

        before = System.nanoTime();
        matches = pubAuthorityService.findAuthorities(AuthorityType.GROUP, null, false, "__*__aa*", null);
        after = System.nanoTime();
        System.out.println("GROUP___aa* in " + ((after - before) / 1000000000.0f));
        assertEquals(size * zones.length, matches.size());

        before = System.nanoTime();
        matches = pubAuthorityService.findAuthorities(AuthorityType.GROUP, null, false, "__*__*aa", null);
        after = System.nanoTime();
        System.out.println("GROUP___*aa in " + ((after - before) / 1000000000.0f));
        assertEquals(size * zones.length, matches.size());
        before = System.nanoTime();

        matches = pubAuthorityService.findAuthorities(AuthorityType.GROUP, null, false, "__*__*a", null);
        after = System.nanoTime();
        System.out.println("GROUP___*a in " + ((after - before) / 1000000000.0f));
        assertEquals(size * size * zones.length, matches.size());

        // Zone specific

        for (String zone : zones)
        {
            if (zone != null)
            {
                before = System.nanoTime();
                matches = pubAuthorityService.findAuthorities(AuthorityType.GROUP, null, false, "__*__a*", zone);
                after = System.nanoTime();
                System.out.println("GROUP___a* in " + ((after - before) / 1000000000.0f));
                assertEquals(size * size, matches.size());

                before = System.nanoTime();
                matches = pubAuthorityService.findAuthorities(AuthorityType.GROUP, null, false, "__*__aa*", zone);
                after = System.nanoTime();
                System.out.println("GROUP___aa* in " + ((after - before) / 1000000000.0f));
                assertEquals(size, matches.size());

                before = System.nanoTime();
                matches = pubAuthorityService.findAuthorities(AuthorityType.GROUP, null, false, "__*__*aa", zone);
                after = System.nanoTime();
                System.out.println("GROUP___*aa in " + ((after - before) / 1000000000.0f));
                assertEquals(size, matches.size());
                before = System.nanoTime();

                matches = pubAuthorityService.findAuthorities(AuthorityType.GROUP, null, false, "__*__*a", zone);
                after = System.nanoTime();
                System.out.println("GROUP___*a in " + ((after - before) / 1000000000.0f));
                assertEquals(size * size, matches.size());
            }
        }

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
        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        assertTrue(authorityService.hasAdminAuthority());
        assertTrue(pubAuthorityService.hasAdminAuthority());
        Set<String> authorities = authorityService.getAuthorities();
        assertEquals("Unexpected result: " + authorities, 4 + (SITE_CNT*2), authorityService.getAuthorities().size());
    }
    
    public void testAuthorities()
    {
        assertEquals(1, getAllAuthorities(AuthorityType.ADMIN).size());
        assertTrue(getAllAuthorities(AuthorityType.ADMIN).contains(PermissionService.ADMINISTRATOR_AUTHORITY));
        assertEquals(1, getAllAuthorities(AuthorityType.EVERYONE).size());
        assertTrue(getAllAuthorities(AuthorityType.EVERYONE).contains(PermissionService.ALL_AUTHORITIES));
        // groups added for email and admin
        assertEquals(GRP_CNT, getAllAuthorities(AuthorityType.GROUP).size());
        assertFalse(getAllAuthorities(AuthorityType.GROUP).contains(PermissionService.ALL_AUTHORITIES));
        assertEquals(1, getAllAuthorities(AuthorityType.GUEST).size());
        assertTrue(getAllAuthorities(AuthorityType.GUEST).contains(PermissionService.GUEST_AUTHORITY));
        assertEquals(0, getAllAuthorities(AuthorityType.OWNER).size());
        assertEquals(0, getAllAuthorities(AuthorityType.ROLE).size());
        
        int peopleCnt = personService.getPeople(null, true, null, new PagingRequest(0, Integer.MAX_VALUE, null)).getPage().size();
        assertEquals(peopleCnt, getAllAuthorities(AuthorityType.USER).size());
    }

    public void testCreateAdminAuth()
    {
        try
        {
            pubAuthorityService.createAuthority(AuthorityType.ADMIN, "woof");
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
            pubAuthorityService.createAuthority(AuthorityType.EVERYONE, "woof");
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
            pubAuthorityService.createAuthority(AuthorityType.GUEST, "woof");
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
            pubAuthorityService.createAuthority(AuthorityType.OWNER, "woof");
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
            pubAuthorityService.createAuthority(AuthorityType.USER, "woof");
            fail("Should not be able to create an user authority");
        }
        catch (AuthorityException ae)
        {

        }
    }

    public void testCreateRootAuth()
    {
        String auth;

        assertEquals(GRP_CNT, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth = pubAuthorityService.createAuthority(AuthorityType.GROUP, "woof");
        assertEquals(GRP_CNT+1, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+1, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        pubAuthorityService.deleteAuthority(auth);
        assertEquals(GRP_CNT, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());

        assertEquals(0, getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        auth = pubAuthorityService.createAuthority(AuthorityType.ROLE, "woof");
        assertEquals(1, getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        pubAuthorityService.deleteAuthority(auth);
        assertEquals(0, getAllAuthorities(AuthorityType.ROLE).size());
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
        assertEquals(GRP_CNT, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth1 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "one");
        assertTrue(pubAuthorityService.authorityExists(auth1));
        assertEquals(GRP_CNT+1, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+1, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth2 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "two");
        assertEquals(GRP_CNT+2, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth3 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "three");
        pubAuthorityService.addAuthority(auth1, auth3);
        assertEquals(GRP_CNT+3, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth4 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "four");
        pubAuthorityService.addAuthority(auth1, auth4);
        assertEquals(GRP_CNT+4, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth5 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "five");
        pubAuthorityService.addAuthority(auth2, auth5);
        assertEquals(GRP_CNT+5, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        
        pubAuthorityService.deleteAuthority(auth5);
        assertEquals(GRP_CNT+4, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        pubAuthorityService.deleteAuthority(auth4);
        assertEquals(GRP_CNT+3, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        pubAuthorityService.deleteAuthority(auth3);
        assertEquals(GRP_CNT+2, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        pubAuthorityService.deleteAuthority(auth2);
        assertEquals(GRP_CNT+1, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+1, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        pubAuthorityService.deleteAuthority(auth1);
        assertEquals(GRP_CNT, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        
        assertEquals(0, getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        auth1 = pubAuthorityService.createAuthority(AuthorityType.ROLE, "one");
        assertEquals(1, getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        auth2 = pubAuthorityService.createAuthority(AuthorityType.ROLE, "two");
        assertEquals(2, getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        auth3 = pubAuthorityService.createAuthority(AuthorityType.ROLE, "three");
        pubAuthorityService.addAuthority(auth1, auth3);
        assertEquals(3, getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        auth4 = pubAuthorityService.createAuthority(AuthorityType.ROLE, "four");
        pubAuthorityService.addAuthority(auth1, auth4);
        assertEquals(4, getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        auth5 = pubAuthorityService.createAuthority(AuthorityType.ROLE, "five");
        pubAuthorityService.addAuthority(auth2, auth5);
        assertEquals(5, getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        
        pubAuthorityService.deleteAuthority(auth5);
        assertEquals(4, getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        pubAuthorityService.deleteAuthority(auth4);
        assertEquals(3, getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        pubAuthorityService.deleteAuthority(auth3);
        assertEquals(2, getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(2, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        pubAuthorityService.deleteAuthority(auth2);
        assertEquals(1, getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(1, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
        pubAuthorityService.deleteAuthority(auth1);
        assertEquals(0, getAllAuthorities(AuthorityType.ROLE).size());
        assertEquals(0, pubAuthorityService.getAllRootAuthorities(AuthorityType.ROLE).size());
    }

    private void checkAuthorityCollectionSize(int expected, List<String> actual, AuthorityType type)
    {
        if (actual.size() != expected)
        {
            String msg = "Incorrect number of authorities.\n"
                    + "   Type:           " + type + "\n" + "   Expected Count: " + expected + "\n" + "   Actual Count:   " + actual.size() + "\n" + "   Authorities:    " + actual;
            fail(msg);
        }
    }

    public void testCreateAuthTree()
    {
        personService.getPerson("andy");

        String auth1;
        String auth2;
        String auth3;
        String auth4;
        String auth5;
        
        assertEquals(GRP_CNT, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth1 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "one");
        assertEquals("GROUP_one", auth1);
        assertEquals(GRP_CNT+1, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+1, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth2 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "two");
        assertEquals("GROUP_two", auth2);
        assertEquals(GRP_CNT+2, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth3 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "three");
        pubAuthorityService.addAuthority(auth1, auth3);
        assertEquals("GROUP_three", auth3);
        assertEquals(GRP_CNT+3, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth4 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "four");
        pubAuthorityService.addAuthority(auth1, auth4);
        assertEquals("GROUP_four", auth4);
        assertEquals(GRP_CNT+4, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth5 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "five");
        pubAuthorityService.addAuthority(auth2, auth5);
        assertEquals("GROUP_five", auth5);
        assertEquals(GRP_CNT+5, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        
        //System.out.println("Users: "+ getAllAuthorities(AuthorityType.USER));
        checkAuthorityCollectionSize(3, getAllAuthorities(AuthorityType.USER), AuthorityType.USER);
        pubAuthorityService.addAuthority(auth5, "andy");
        assertEquals(GRP_CNT+5, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        // The next call looks for people not users :-)
        checkAuthorityCollectionSize(3, getAllAuthorities(AuthorityType.USER), AuthorityType.USER);
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
        assertEquals(GRP_CNT+5, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        // The next call looks for people not users :-)
        checkAuthorityCollectionSize(3, getAllAuthorities(AuthorityType.USER), AuthorityType.USER);
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
        
        assertEquals(GRP_CNT, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth1 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "one");
        assertEquals(GRP_CNT+1, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+1, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth2 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "two");
        assertEquals(GRP_CNT+2, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth3 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "three");
        pubAuthorityService.addAuthority(auth1, auth3);
        assertEquals(GRP_CNT+3, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth4 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "four");
        pubAuthorityService.addAuthority(auth1, auth4);
        assertEquals(GRP_CNT+4, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth5 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "five");
        pubAuthorityService.addAuthority(auth2, auth5);
        assertEquals(GRP_CNT+5, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        
        assertEquals(3, getAllAuthorities(AuthorityType.USER).size());
        pubAuthorityService.addAuthority(auth5, "andy");
        pubAuthorityService.addAuthority(auth1, "andy");
        
        assertEquals(GRP_CNT+5, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        // The next call looks for people not users :-)
        checkAuthorityCollectionSize(3, getAllAuthorities(AuthorityType.USER), AuthorityType.USER);
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
        
        assertEquals(GRP_CNT+5, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        // The next call looks for people not users :-)
        checkAuthorityCollectionSize(3, getAllAuthorities(AuthorityType.USER), AuthorityType.USER);
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
        
        assertEquals(GRP_CNT, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth1 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "one");
        assertEquals(GRP_CNT+1, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+1, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth2 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "two");
        assertEquals(GRP_CNT+2, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth3 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "three");
        pubAuthorityService.addAuthority(auth1, auth3);
        assertEquals(GRP_CNT+3, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth4 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "four");
        pubAuthorityService.addAuthority(auth1, auth4);
        assertEquals(GRP_CNT+4, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        auth5 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "five");
        pubAuthorityService.addAuthority(auth2, auth5);
        assertEquals(GRP_CNT+5, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        
        checkAuthorityCollectionSize(3, getAllAuthorities(AuthorityType.USER), AuthorityType.USER);
        pubAuthorityService.addAuthority(auth5, "andy");
        pubAuthorityService.addAuthority(auth1, "andy");
        
        assertEquals(GRP_CNT+5, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT+2, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        // The next call looks for people not users :-)
        checkAuthorityCollectionSize(3, getAllAuthorities(AuthorityType.USER), AuthorityType.USER);
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
        
        assertEquals(GRP_CNT+5, getAllAuthorities(AuthorityType.GROUP).size());
        
        // Number of root authorities has been reduced since auth2 is no longer an orphan
        assertEquals(ROOT_GRP_CNT+1, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        // The next call looks for people not users :-)
        checkAuthorityCollectionSize(3, getAllAuthorities(AuthorityType.USER), AuthorityType.USER);
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

    public void testGetAuthorityNodeRef()
    {
        String ADMIN_GROUP = "GROUP_ALFRESCO_ADMINISTRATORS";
        String NEW_GROUP = "GROUP_NEWLY_ADDED";
        
        int rootCount = pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size();
        int allCount = getAllAuthorities(AuthorityType.GROUP).size();
        
        // Should have a root group "GROUP_ALFRESCO_ADMINISTRATORS"
        Set<String> root = pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP);
        assertTrue(
                ADMIN_GROUP + " not found in " + root,
                root.contains(ADMIN_GROUP)
        );
        NodeRef rootNode = pubAuthorityService.getAuthorityNodeRef(ADMIN_GROUP);
        
        // Check it makes sense
        assertEquals(
                ADMIN_GROUP,
                nodeService.getProperty(rootNode, ContentModel.PROP_AUTHORITY_NAME)
        );
        
        
        // Now add a child
        pubAuthorityService.createAuthority(AuthorityType.GROUP, NEW_GROUP.replace("GROUP_", ""));
        pubAuthorityService.setAuthorityDisplayName(NEW_GROUP, NEW_GROUP);
        pubAuthorityService.addAuthority(ADMIN_GROUP, NEW_GROUP);
        assertEquals(rootCount, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        assertEquals(allCount+1, getAllAuthorities(AuthorityType.GROUP).size());
        
        // Check that
        NodeRef newNode = pubAuthorityService.getAuthorityNodeRef(NEW_GROUP);
        assertEquals(
                NEW_GROUP,
                nodeService.getProperty(newNode, ContentModel.PROP_AUTHORITY_NAME)
        );
        
        // Should be siblings
        assertEquals(
                nodeService.getPrimaryParent(rootNode).getParentRef(),
                nodeService.getPrimaryParent(newNode).getParentRef()
        );
        
        // With an association between them
        List<ChildAssociationRef> members = nodeService.getChildAssocs(
                rootNode, ContentModel.ASSOC_MEMBER,
                RegexQNamePattern.MATCH_ALL
        );
        boolean found = false;
        for(ChildAssociationRef assoc : members)
        {
            if(assoc.getChildRef().equals(newNode)) found = true;
        }
        assertTrue(
                "Association from child to parent group not found",
                found
        );
    }
    
    public void test_AR_1510()
    {
        personService.getPerson("andy1");
        personService.getPerson("andy2");
        personService.getPerson("andy3");
        personService.getPerson("andy4");
        personService.getPerson("andy5");
        personService.getPerson("andy6");
        assertEquals(GRP_CNT, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        String auth1 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "one");
        pubAuthorityService.addAuthority(auth1, "andy1");
        String auth2 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "two");
        pubAuthorityService.addAuthority(auth1, auth2);
        pubAuthorityService.addAuthority(auth2, "andy1");
        pubAuthorityService.addAuthority(auth2, "andy2");
        String auth3 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "three");
        pubAuthorityService.addAuthority(auth2, auth3);
        pubAuthorityService.addAuthority(auth3, "andy3");
        String auth4 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "four");
        pubAuthorityService.addAuthority(auth3, auth4);
        pubAuthorityService.addAuthority(auth4, "andy1");
        pubAuthorityService.addAuthority(auth4, "andy4");
        String auth5 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "five");
        pubAuthorityService.addAuthority(auth4, auth5);
        pubAuthorityService.addAuthority(auth5, "andy1");
        pubAuthorityService.addAuthority(auth5, "andy5");
        String auth6 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "six");
        pubAuthorityService.addAuthority(auth3, auth6);
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
        
        assertEquals(GRP_CNT, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        String auth1 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "one");
        pubAuthorityService.addAuthority(auth1, "1234");
        String auth2 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "two");
        pubAuthorityService.addAuthority(auth2, "andy");
        String auth3 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "three");
        pubAuthorityService.addAuthority(auth3, "Novalike");
        String auth4 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "four");
        pubAuthorityService.addAuthority(auth4, "1andy");
        String auth5 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "five");
        pubAuthorityService.addAuthority(auth5, "andy2");
        String auth6 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "six");
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
        assertEquals(GRP_CNT, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
        
        String auth1234 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "1234");
        assertEquals(0, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth1234, false).size());
        String authC1 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "circle");
        pubAuthorityService.addAuthority(auth1234, authC1);
        assertEquals(1, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth1234, false).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, authC1, false).size());
        String authC2 = pubAuthorityService.createAuthority(AuthorityType.GROUP, "bigCircle");
        pubAuthorityService.addAuthority(authC1, authC2);
        assertEquals(2, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth1234, false).size());
        assertEquals(1, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, authC1, false).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, authC2, false).size());
        String authStuff = pubAuthorityService.createAuthority(AuthorityType.GROUP, "|<>?~@:}{+_)(*&^%$£!¬`,./#';][=-0987654321 1234556678 '");
        pubAuthorityService.addAuthority(authC2, authStuff);
        assertEquals(3, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, auth1234, false).size());
        assertEquals(2, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, authC1, false).size());
        assertEquals(1, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, authC2, false).size());
        assertEquals(0, pubAuthorityService.getContainedAuthorities(AuthorityType.GROUP, authStuff, false).size());
        String authSpace = pubAuthorityService.createAuthority(AuthorityType.GROUP, "  Circles     ");
        pubAuthorityService.addAuthority(authStuff, authSpace);
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
        
        assertEquals(GRP_CNT, getAllAuthorities(AuthorityType.GROUP).size());
        assertEquals(ROOT_GRP_CNT, pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size());
    }

    public void testAdminGroup()
    {
        personService.getPerson("andy");
        String adminGroup = pubAuthorityService.getName(AuthorityType.GROUP, "ALFRESCO_ADMINISTRATORS");
        pubAuthorityService.removeAuthority(adminGroup, "andy");
        assertFalse(pubAuthorityService.isAdminAuthority("andy"));
        pubAuthorityService.addAuthority(adminGroup, "andy");
        assertTrue(pubAuthorityService.isAdminAuthority("andy"));
        pubAuthorityService.removeAuthority(adminGroup, "andy");
        assertFalse(pubAuthorityService.isAdminAuthority("andy"));
    }

    private Map<QName, Serializable> createDefaultProperties(String userName, String firstName, String lastName, String email, String orgId, NodeRef home)
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

    public void testAuthorityDisplayNames()
    {
        String authOne = pubAuthorityService.createAuthority(AuthorityType.GROUP, "One");
        assertEquals(pubAuthorityService.getAuthorityDisplayName(authOne), "One");
        pubAuthorityService.setAuthorityDisplayName(authOne, "Selfish Crocodile");
        assertEquals(pubAuthorityService.getAuthorityDisplayName(authOne), "Selfish Crocodile");

        String authTwo = pubAuthorityService.createAuthority(AuthorityType.GROUP, "Two", "Lamp posts", authorityService.getDefaultZones());
        assertEquals(pubAuthorityService.getAuthorityDisplayName(authTwo), "Lamp posts");
        pubAuthorityService.setAuthorityDisplayName(authTwo, "Happy Hippos");
        assertEquals(pubAuthorityService.getAuthorityDisplayName(authTwo), "Happy Hippos");

        assertEquals(pubAuthorityService.getAuthorityDisplayName("GROUP_Loon"), "Loon");
        assertEquals(pubAuthorityService.getAuthorityDisplayName("ROLE_Gibbon"), "Gibbon");
        assertEquals(pubAuthorityService.getAuthorityDisplayName("Monkey"), "Monkey");

        authenticationComponent.setCurrentUser("andy");
        assertEquals(pubAuthorityService.getAuthorityDisplayName(authOne), "Selfish Crocodile");
        assertEquals(pubAuthorityService.getAuthorityDisplayName(authTwo), "Happy Hippos");
        assertEquals(pubAuthorityService.getAuthorityDisplayName("GROUP_Loon"), "Loon");
        assertEquals(pubAuthorityService.getAuthorityDisplayName("GROUP_Loon"), "Loon");
        assertEquals(pubAuthorityService.getAuthorityDisplayName("ROLE_Gibbon"), "Gibbon");
        assertEquals(pubAuthorityService.getAuthorityDisplayName("Monkey"), "Monkey");
    }
    
    public void testGetAuthoritiesFilteringSorting()
    {
        String TEST_RUN = System.currentTimeMillis()+"-";
        
        PagingRequest pr = new PagingRequest(100, null);
        
        List<String> result = null;
        
        // -ve test
        try
        {
            result = pubAuthorityService.getAuthorities(null, null, null, false, true, pr).getPage();
            fail("Either type and/or zoneName must be supplied");
        }
        catch (IllegalArgumentException iae)
        {
            // ignore - expected
        }
        
        int origGroupCnt = pubAuthorityService.getAuthorities(AuthorityType.GROUP, null, null, false, true, pr).getPage().size();
        int origRoleCnt = pubAuthorityService.getAuthorities(AuthorityType.ROLE, null, null, false, true, pr).getPage().size();
        int origZoneCnt = pubAuthorityService.getAuthorities(null, AuthorityService.ZONE_APP_DEFAULT, null, false, true, pr).getPage().size();
        
        String G1 = pubAuthorityService.createAuthority(AuthorityType.GROUP, TEST_RUN+"aabbg");
        String G2 = pubAuthorityService.createAuthority(AuthorityType.GROUP, TEST_RUN+"abg");
        String G3 = pubAuthorityService.createAuthority(AuthorityType.GROUP, TEST_RUN+"bcg");
        String G4 = pubAuthorityService.createAuthority(AuthorityType.GROUP, TEST_RUN+"bbccg");
        List<String> groups = Arrays.asList(G1, G2, G3, G4);
        
        pubAuthorityService.setAuthorityDisplayName(G1, TEST_RUN+"z");
        
        String R1 = pubAuthorityService.createAuthority(AuthorityType.ROLE, TEST_RUN+"aabbr");
        String R2 = pubAuthorityService.createAuthority(AuthorityType.ROLE, TEST_RUN+"abr");
        String R3 = pubAuthorityService.createAuthority(AuthorityType.ROLE, TEST_RUN+"bcr");
        String R4 = pubAuthorityService.createAuthority(AuthorityType.ROLE, TEST_RUN+"bbccr");
        List<String> roles = Arrays.asList(R1, R2, R3, R4);
        
        // filter by type - but no additional name filtering ("*" is equivalent to null)
        
        result = pubAuthorityService.getAuthorities(AuthorityType.GROUP, null, "*", false, true, pr).getPage();
        assertEquals(origGroupCnt + groups.size(), result.size());
        assertContains(result, groups, true);
        assertContains(result, roles, false);
        
        result = pubAuthorityService.getAuthorities(AuthorityType.ROLE, null, "*", false, true, pr).getPage();
        assertEquals(origRoleCnt + roles.size(), result.size());
        assertContains(result, roles, true);
        assertContains(result, groups, false);
        
        // filter by type and name
        
        result = pubAuthorityService.getAuthorities(AuthorityType.GROUP, null, TEST_RUN+"a*", false, true, pr).getPage();
        assertEquals(2, result.size());
        assertContains(result, Arrays.asList(G1, G2), true);
        
        result = pubAuthorityService.getAuthorities(AuthorityType.ROLE, null, TEST_RUN+"b*", false, true, pr).getPage();
        assertEquals(2, result.size());
        assertContains(result, Arrays.asList(R3, R4), true);
        
        // filter by zone - but no additional name filtering ("*" is equivalent to null)
        
        result = pubAuthorityService.getAuthorities(null, AuthorityService.ZONE_APP_DEFAULT, null, false, true, pr).getPage();
        assertEquals(origZoneCnt + groups.size() + roles.size(), result.size());
        assertContains(result, groups, true);
        assertContains(result, roles, true);
        
        // filter by zone and name
        
        result = pubAuthorityService.getAuthorities(null, AuthorityService.ZONE_APP_DEFAULT, TEST_RUN+"a*", false, true, pr).getPage();
        assertEquals(4, result.size());
        assertContains(result, Arrays.asList(G1, G2, R1, R2), true);
        
        // sorting - by display name (ascending)
        result = pubAuthorityService.getAuthorities(AuthorityType.GROUP, null, TEST_RUN, true, true, pr).getPage();
        assertEquals(4, result.size());
        assertSameOrder(result, Arrays.asList(G2, G4, G3, G1));
        
        // sorting - by display name (descending)
        result = pubAuthorityService.getAuthorities(AuthorityType.GROUP, null, TEST_RUN, true, false, pr).getPage();
        assertEquals(4, result.size());
        assertSameOrder(result, Arrays.asList(G1, G3, G4, G2));
    }
    
    private void assertContains(List<String> results, List<String> checklist, boolean included)
    {
        for (String check : checklist)
        {
            assertContains(results, check, included);
        }
    }
    
    private void assertContains(List<String> results, String check, boolean included)
    {
        if (results.contains(check) != included)
        {
            fail("Unexpected: "+check+" [result="+results+",included="+included+"]");
        }
    }
    
    private void assertSameOrder(List<String> results, List<String> checklist)
    {
        assertEquals(results.size(), checklist.size());
        for (int i = 0; i < results.size(); i++)
        {
            assertEquals("Unexpected", results.get(i), checklist.get(i));
        }
    }
    
    private List<String> getAllAuthorities(AuthorityType type)
    {
        return pubAuthorityService.getAuthorities(type, null, null, false, true, new PagingRequest(0, Integer.MAX_VALUE, null)).getPage();
    }
}
