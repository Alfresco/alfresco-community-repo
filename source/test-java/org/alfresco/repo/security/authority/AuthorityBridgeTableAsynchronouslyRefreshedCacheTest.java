/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;

import org.junit.experimental.categories.Category;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;

@Category(OwnJVMTestsCategory.class)
public class AuthorityBridgeTableAsynchronouslyRefreshedCacheTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private AuthorityService authorityService;
    private TenantAdminService tenantAdminService;
    private TransactionService transactionService;
    private PersonService personService;
    private TenantService tenantService;
    private AuthorityBridgeTableAsynchronouslyRefreshedCache authorityBridgeTableCache;

    private static final String TENANT_DOMAIN = GUID.generate() + ".com";
    private static final String TENANT_ADMIN_USER = AuthenticationUtil.getAdminUserName() + "@" + TENANT_DOMAIN;

    public AuthorityBridgeTableAsynchronouslyRefreshedCacheTest()
    {
        super();
    }

    @Override
    public void setUp() throws Exception
    {
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            throw new AlfrescoRuntimeException(
                    "A previous tests did not clean up transaction: " +
                    AlfrescoTransactionSupport.getTransactionId());
        }
        
        transactionService = (TransactionService) ctx.getBean(ServiceRegistry.TRANSACTION_SERVICE.getLocalName());
        authorityService = (AuthorityService) ctx.getBean(ServiceRegistry.AUTHORITY_SERVICE.getLocalName());
        tenantAdminService = ctx.getBean("tenantAdminService", TenantAdminService.class);
        personService = (PersonService) ctx.getBean(ServiceRegistry.PERSON_SERVICE.getLocalName());
        tenantService = (TenantService) ctx.getBean("tenantService");
        authorityBridgeTableCache = (AuthorityBridgeTableAsynchronouslyRefreshedCache) ctx.getBean("authorityBridgeTableCache");
    }

    @Override
    protected void tearDown()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    /**
     * See MNT-9375
     */
    public void testAuthorityBridgeTableCacheForTenants() throws Exception
    {
        final String tenantPersonName = GUID.generate() + "@" + TENANT_DOMAIN;
        final String childGroupName = "tenantChildGroup" + GUID.generate();
        final String parentGroupName = "tenantParentGroup" + GUID.generate();

        createTenant(TENANT_DOMAIN);

        // Create a group and place a user in it
        AuthenticationUtil.setFullyAuthenticatedUser(TENANT_ADMIN_USER);
        assertEquals(TENANT_DOMAIN, tenantService.getCurrentUserDomain());
        final String tenantChildGroup = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<String>()
        {
            @Override
            public String execute() throws Throwable
            {
                personService.getPerson(tenantPersonName, true);
                assertTrue(personService.personExists(tenantPersonName));
                String tenantChildGroup = authorityService.createAuthority(AuthorityType.GROUP, childGroupName);
                assertNotNull(authorityService.getAuthorityNodeRef(tenantChildGroup));
                authorityService.addAuthority(tenantChildGroup, tenantPersonName);
                authorityBridgeTableCache.forceInChangesForThisUncommittedTransaction();
                return tenantChildGroup;
            }
        }, false, true);

        // Create another group and place in it an existing group with a user
        // The transaction is required, because the AuthorityBridgeTableAsynchronouslyRefreshedCache is cleared in the end of transaction asynchronously using FutureTask.
        final String tenantParentGroup = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<String>()
        {
            @Override
            public String execute() throws Throwable
            {
                String tenantParentGroup = authorityService.createAuthority(AuthorityType.GROUP, parentGroupName);
                assertNotNull(authorityService.getAuthorityNodeRef(tenantParentGroup));
                authorityService.addAuthority(tenantParentGroup, tenantChildGroup);
                authorityBridgeTableCache.forceInChangesForThisUncommittedTransaction();
                return tenantParentGroup;
            }
        }, false, true);

        Set<String> authorities = authorityService.getContainingAuthorities(null, tenantPersonName, false);
        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(tenantParentGroup));
        assertTrue(authorities.contains(tenantChildGroup));
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    }

    private void createTenant(final String tenantDomain)
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                if (!tenantAdminService.existsTenant(tenantDomain))
                {
                    tenantAdminService.createTenant(tenantDomain, "password".toCharArray());
                }
                else
                {
                    throw new IllegalStateException("Tenant exists!");
                }
                return null;
            }
        }, false, true);
    }
    
    /**
     * See MNT-12473
     */
    public void testCyclicGroups()
    {
        List<AuthorityBridgeLink> cyclicLinks = new LinkedList<AuthorityBridgeLink>();
        // no cycle
        cyclicLinks.add(createAuthorityBridgeLink("a1", "a2"));
        
        cyclicLinks.add(createAuthorityBridgeLink("g1", "g2"));
        cyclicLinks.add(createAuthorityBridgeLink("g2", "g3"));
        cyclicLinks.add(createAuthorityBridgeLink("g3", "g4"));
        // 1st cycle
        cyclicLinks.add(createAuthorityBridgeLink("g4", "g1"));
        
        cyclicLinks.add(createAuthorityBridgeLink("b1", "b2"));
        // child with no cycle
        cyclicLinks.add(createAuthorityBridgeLink("b2", "a1"));
        cyclicLinks.add(createAuthorityBridgeLink("b2", "b3"));
        // 2nd cycle
        cyclicLinks.add(createAuthorityBridgeLink("b3", "b1"));
        
        cyclicLinks.add(createAuthorityBridgeLink("d1", "d2"));
        cyclicLinks.add(createAuthorityBridgeLink("d2", "d3"));
        // 3rd cycle
        cyclicLinks.add(createAuthorityBridgeLink("d3", "d1"));
        cyclicLinks.add(createAuthorityBridgeLink("d2", "d4"));
        // 4th cycle
        cyclicLinks.add(createAuthorityBridgeLink("d4", "d1"));
        cyclicLinks.add(createAuthorityBridgeLink("d3", "d5"));
        // 5th cycle
        cyclicLinks.add(createAuthorityBridgeLink("d5", "d1"));
        
        AuthorityBridgeDAO authorityBridgeDAOMock = mock(AuthorityBridgeDAO.class);
        when(authorityBridgeDAOMock.getAuthorityBridgeLinks()).thenReturn(cyclicLinks);
        
        AuthorityDAO authorityDAOMock = mock(AuthorityDAO.class);
        class Counter
        {
            private int removed = 0;
            public int getRemoved()
            {
                return removed;
            }
            public void increase()
            {
                this.removed++;
            }
        }
        ;
        final Counter cycles = new Counter();
        doAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocation)
            {
                cycles.increase();
                return null;
            }
        }).when(authorityDAOMock).removeAuthority(anyString(), anyString(), anyBoolean());
        
        AuthorityBridgeTableAsynchronouslyRefreshedCache cache = new AuthorityBridgeTableAsynchronouslyRefreshedCache();
        cache.setAuthorityBridgeDAO(authorityBridgeDAOMock);
        cache.setAuthorityDAO(authorityDAOMock);
        cache.setTenantAdminService(tenantAdminService);
        cache.setRetryingTransactionHelper(transactionService.getRetryingTransactionHelper());
        
        try
        {
            cache.buildCache(tenantAdminService.getCurrentUserDomain());
        }
        catch (AlfrescoRuntimeException e1)
        {
            assertTrue(e1.getMessage().contains("Cyclic links were detected"));
            assertEquals(5, cycles.getRemoved());
        }
        catch (ConcurrentModificationException e2)
        {
            fail("Cyclic links were NOT detected and processed");
        }
    }
    
    private AuthorityBridgeLink createAuthorityBridgeLink(String parentName, String childName)
    {
        AuthorityBridgeLink link = new AuthorityBridgeLink();
        link.setParentName(parentName);
        link.setChildName(childName);
        return link;
    }

}
