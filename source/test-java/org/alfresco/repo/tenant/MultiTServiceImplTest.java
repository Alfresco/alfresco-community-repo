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
package org.alfresco.repo.tenant;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * A test for MultiTServiceImpl class.
 * 
 * @author alex.mukha
 * @since 4.2.3
 */
public class MultiTServiceImplTest
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    private MultiTServiceImpl multiTServiceImpl;
    private TenantAdminService tenantAdminService;
    private PersonService personService;
    private TenantService tenantService;
    private MutableAuthenticationService authenticationService;
    private TransactionService transactionService;
    private NodeService nodeService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    
    private UserTransaction txn;
    
    private boolean mtEnabled;
    
    // Test variables 
    private static final String DEFAULT_ADMIN_PW = "admin";
    private static final String PASS = "password";
    private static final String DOMAIN = MultiTServiceImplTest.class.getName().toLowerCase();
    private static final String USER1 = "USER1";
    private static final String USER2 = "USER2";
    private static final String USER3 = "USER3";
    private static final String USER2_WITH_DOMAIN = USER2 + TenantService.SEPARATOR + DOMAIN;
    private static final String STRING = "stringwithouttenant";
    private static final String TENANT_STRING = addDomainToId(STRING, DOMAIN);
    private static final String STRING_WITH_EXISTENT_DOMAIN = TenantService.SEPARATOR + DOMAIN + TenantService.SEPARATOR;
    private static final String STRING_WITH_NONEXITENT_DOMAIN = TenantService.SEPARATOR + STRING + TenantService.SEPARATOR;
    private static final String PROTOCOL = "testprotocol";
    private static final String IDENTIFIER = "testidentifier";
    private static final String ID = "id";
    private static final String ROOT_PATH = "/";
    private static final StoreRef DEFAULT_STORE = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
    private static final StoreRef TENANT_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, addDomainToId("SpacesStore", DOMAIN));
    private static final NodeRef NODE_REF = new NodeRef(PROTOCOL, IDENTIFIER, ID);
    private static final NodeRef TENANT_NODE_REF = new NodeRef(PROTOCOL, addDomainToId(IDENTIFIER, DOMAIN), ID);
    private static final StoreRef STORE_REF = new StoreRef(PROTOCOL, IDENTIFIER);
    private static final StoreRef TENANT_STORE_REF = new StoreRef(PROTOCOL, addDomainToId(IDENTIFIER, DOMAIN));
    private static final String NAMESPACE_URI = "testassoctypenamespace";
    private static final String LOCAL_NAME = "testassoctypelocalname";
    private static final QName QNAME = QName.createQName(NAMESPACE_URI, LOCAL_NAME);
    private static final QName TENANT_QNAME = QName.createQName(addDomainToId(NAMESPACE_URI, DOMAIN), LOCAL_NAME);
    private static final AssociationRef assocRef = new AssociationRef(NODE_REF, QNAME, NODE_REF);
    private static final AssociationRef tenantAssocRef = new AssociationRef(TENANT_NODE_REF, QNAME, TENANT_NODE_REF);
    private static final ChildAssociationRef childAssocRef = new ChildAssociationRef(QNAME, NODE_REF, QNAME, NODE_REF);
    private static final ChildAssociationRef tenantChildAssocRef = new ChildAssociationRef(QNAME, TENANT_NODE_REF, QNAME, TENANT_NODE_REF);
    
    @Before
    public void setUp() throws Exception
    {
        multiTServiceImpl = ctx.getBean("tenantService", MultiTServiceImpl.class);
        tenantAdminService = ctx.getBean("tenantAdminService", TenantAdminService.class);
        personService = ctx.getBean("PersonService", PersonService.class);
        tenantService = ctx.getBean("tenantService", TenantService.class);
        authenticationService = ctx.getBean("AuthenticationService", MutableAuthenticationService.class);
        transactionService = ctx.getBean("TransactionService", TransactionService.class);
        nodeService = ctx.getBean("NodeService", NodeService.class);
        searchService = ctx.getBean("SearchService", SearchService.class);
        namespaceService = ctx.getBean("NamespaceService", NamespaceService.class);
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        mtEnabled = AuthenticationUtil.isMtEnabled();
        txn = transactionService.getUserTransaction();
        txn.begin();
        AuthenticationUtil.setMtEnabled(false);
    }
    
    @After
    public void tearDown() throws Exception
    {
        txn.rollback();
        AuthenticationUtil.setMtEnabled(mtEnabled);
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    @Test
    public void testIsTenantUser()
    {
        // Create a user with a plain user name without a domain
        NodeRef userNodeRef = createUser(USER1, TenantService.DEFAULT_DOMAIN, PASS);
        assertNotNull("The user was not created.", userNodeRef);
        assertFalse("The user is not from a tenant, but was reported otherwise.", multiTServiceImpl.isTenantUser(USER1));
        
        // Create a user with a name as an email, but not from tenant
        userNodeRef =  createUser(USER2_WITH_DOMAIN, TenantService.DEFAULT_DOMAIN, PASS);
        assertNotNull("The user was not created.", userNodeRef);
        assertFalse("The user is not from a tenant, but was reported otherwise.", multiTServiceImpl.isTenantUser(USER2_WITH_DOMAIN));
        
        // Create a tenant and a user in it
        createTenant(DOMAIN);
        userNodeRef = createUser(USER3, DOMAIN, PASS);
        assertNotNull("The user was not created.", userNodeRef);
        assertTrue("The user is from a tenant, but was reported otherwise.", multiTServiceImpl.isTenantUser(USER3 + MultiTServiceImpl.SEPARATOR + DOMAIN));
    }
    
    @Test
    public void testGetCurrentUserDomain()
    {
        // Test a tenant user
        createTenant(DOMAIN);
        NodeRef userNodeRef = createUser(USER1, DOMAIN, PASS);
        assertNotNull("The user was not created.", userNodeRef);
        
        TenantRunAsWork<String> work = new TenantRunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                return tenantService.getCurrentUserDomain();
            }
        };
        
        String result = TenantUtil.runAsUserTenant(work, USER1, DOMAIN);
        assertEquals("The domains do not match.", DOMAIN, result);
        
        // Test a default user
        userNodeRef = createUser(USER2, TenantService.DEFAULT_DOMAIN, PASS);
        assertNotNull("The user was not created.", userNodeRef);
        
        work = new TenantRunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                return tenantService.getCurrentUserDomain();
            }
        };
        
        result = TenantUtil.runAsUserTenant(work, USER2, TenantService.DEFAULT_DOMAIN);
        assertEquals("The domains do not match.", TenantService.DEFAULT_DOMAIN, result);
    }
    
    @Test
    public void testGetName()
    {
        NodeRef userNodeRef = createUser(USER1, TenantService.DEFAULT_DOMAIN, PASS);
        assertNotNull("The user was not created.", userNodeRef);
        TenantRunAsWork<NodeRef> work1 = new TenantRunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork() throws Exception
            {
                return tenantService.getName(NODE_REF);
            }
        };
        NodeRef result = TenantUtil.runAsUserTenant(work1, USER1, TenantService.DEFAULT_DOMAIN);
        assertEquals("The NodeRef should contain domain.", NODE_REF, result);
        
        createTenant(DOMAIN);
        userNodeRef = createUser(USER2, DOMAIN, PASS);
        assertNotNull("The user was not created.", userNodeRef);
        work1 = new TenantRunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork() throws Exception
            {
                return tenantService.getName(NODE_REF);
            }
        };
        result = TenantUtil.runAsUserTenant(work1, USER2, DOMAIN);
        assertEquals("The NodeRef should contain domain.", TENANT_NODE_REF, result);

        work1 = new TenantRunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork() throws Exception
            {
                return tenantService.getName(TENANT_NODE_REF, NODE_REF);
            }
        };
        result = TenantUtil.runAsUserTenant(work1, USER2, DOMAIN);
        assertEquals("The NodeRef should contain domain.", TENANT_NODE_REF, result);
        
        TenantRunAsWork<StoreRef> work2 = new TenantRunAsWork<StoreRef>()
        {
            @Override
            public StoreRef doWork() throws Exception
            {
                return tenantService.getName(STORE_REF);
            }
        };
        StoreRef result2 = TenantUtil.runAsUserTenant(work2, USER2, DOMAIN);
        assertEquals("The StoreRef should contain domain.", TENANT_STORE_REF, result2);
        
        TenantRunAsWork<ChildAssociationRef> work3 = new TenantRunAsWork<ChildAssociationRef>()
        {
            @Override
            public ChildAssociationRef doWork() throws Exception
            {
                return tenantService.getName(childAssocRef);
            }
        };
        ChildAssociationRef result3 = TenantUtil.runAsUserTenant(work3, USER2, DOMAIN);
        assertEquals("The ChildAssociationRef should contain domain.", tenantChildAssocRef, result3);
        
        TenantRunAsWork<AssociationRef> work4 = new TenantRunAsWork<AssociationRef>()
        {
            @Override
            public AssociationRef doWork() throws Exception
            {
                return tenantService.getName(assocRef);
            }
        };
        AssociationRef result4 = TenantUtil.runAsUserTenant(work4, USER2, DOMAIN);
        assertEquals("The AssociationRef should contain domain.", tenantAssocRef, result4);
        
        TenantRunAsWork<StoreRef> work5 = new TenantRunAsWork<StoreRef>()
        {
            @Override
            public StoreRef doWork() throws Exception
            {
                return tenantService.getName(USER2_WITH_DOMAIN, STORE_REF);
            }
        };
        StoreRef result5 = TenantUtil.runAsUserTenant(work5, USER2, DOMAIN);
        assertEquals("The StoreRef should contain domain.", TENANT_STORE_REF, result5);
        
        TenantRunAsWork<QName> work6 = new TenantRunAsWork<QName>()
        {
            @Override
            public QName doWork() throws Exception
            {
                return tenantService.getName(QNAME);
            }
        };
        QName result6 = TenantUtil.runAsUserTenant(work6, USER2, DOMAIN);
        assertEquals("The QName should contain domain.", TENANT_QNAME, result6);
        
        TenantRunAsWork<QName> work7 = new TenantRunAsWork<QName>()
        {
            @Override
            public QName doWork() throws Exception
            {
                return tenantService.getName(TENANT_NODE_REF ,QNAME);
            }
        };
        QName result7 = TenantUtil.runAsUserTenant(work7, USER2, DOMAIN);
        assertEquals("The QName should contain domain.", TENANT_QNAME, result7);
        
        TenantRunAsWork<String> work8 = new TenantRunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                return tenantService.getName(STRING);
            }
        };
        String result8 = TenantUtil.runAsUserTenant(work8, USER2, DOMAIN);
        assertEquals("The String should contain domain.", TENANT_STRING, result8);
    }
    
    @Test
    public void testGetBaseName()
    {
        NodeRef userNodeRef = createUser(USER1, TenantService.DEFAULT_DOMAIN, PASS);
        assertNotNull("The user was not created.", userNodeRef);
        TenantRunAsWork<NodeRef> work1 = new TenantRunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork() throws Exception
            {
                return tenantService.getBaseName(NODE_REF);
            }
        };
        NodeRef result = TenantUtil.runAsUserTenant(work1, USER1, TenantService.DEFAULT_DOMAIN);
        assertEquals("The NodeRef should not contain domain.", NODE_REF, result);
        
        createTenant(DOMAIN);
        userNodeRef = createUser(USER2, DOMAIN, PASS);
        assertNotNull("The user was not created.", userNodeRef);
        work1 = new TenantRunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork() throws Exception
            {
                return tenantService.getBaseName(TENANT_NODE_REF);
            }
        };
        result = TenantUtil.runAsUserTenant(work1, USER2, DOMAIN);
        assertEquals("The NodeRef should not contain domain.", NODE_REF, result);

        work1 = new TenantRunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork() throws Exception
            {
                return tenantService.getBaseName(TENANT_NODE_REF, true);
            }
        };
        result = TenantUtil.runAsUserTenant(work1, USER1, TenantService.DEFAULT_DOMAIN);
        assertEquals("The NodeRef should not contain domain.", NODE_REF, result);
        
        work1 = new TenantRunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork() throws Exception
            {
                return tenantService.getBaseName(TENANT_NODE_REF, false);
            }
        };
        result = TenantUtil.runAsUserTenant(work1, USER1, TenantService.DEFAULT_DOMAIN);
        assertEquals("The NodeRef should contain domain.", TENANT_NODE_REF, result);
        
        TenantRunAsWork<StoreRef> work2 = new TenantRunAsWork<StoreRef>()
        {
            @Override
            public StoreRef doWork() throws Exception
            {
                return tenantService.getBaseName(TENANT_STORE_REF);
            }
        };
        StoreRef result2 = TenantUtil.runAsUserTenant(work2, USER2, DOMAIN);
        assertEquals("The StoreRef should not contain domain.", STORE_REF, result2);
        
        TenantRunAsWork<ChildAssociationRef> work3 = new TenantRunAsWork<ChildAssociationRef>()
        {
            @Override
            public ChildAssociationRef doWork() throws Exception
            {
                return tenantService.getBaseName(tenantChildAssocRef);
            }
        };
        ChildAssociationRef result3 = TenantUtil.runAsUserTenant(work3, USER2, DOMAIN);
        assertEquals("The ChildAssociationRef not should contain domain.", childAssocRef, result3);
        
        TenantRunAsWork<AssociationRef> work4 = new TenantRunAsWork<AssociationRef>()
        {
            @Override
            public AssociationRef doWork() throws Exception
            {
                return tenantService.getBaseName(tenantAssocRef);
            }
        };
        AssociationRef result4 = TenantUtil.runAsUserTenant(work4, USER2, DOMAIN);
        assertEquals("The AssociationRef should not contain domain.", assocRef, result4);
        
        TenantRunAsWork<QName> work5 = new TenantRunAsWork<QName>()
        {
            @Override
            public QName doWork() throws Exception
            {
                return tenantService.getBaseName(TENANT_QNAME, false);
            }
        };
        QName result5 = TenantUtil.runAsUserTenant(work5, USER2, DOMAIN);
        assertEquals("The QName should not contain domain.", QNAME, result5);
        
        TenantRunAsWork<String> work6 = new TenantRunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                return tenantService.getBaseName(TENANT_STRING);
            }
        };
        String result6 = TenantUtil.runAsUserTenant(work6, USER2, DOMAIN);
        assertEquals("The String should not contain domain.", STRING, result6);
    }
    
    @Test
    public void testCheckDomainUser()
    {
        String nonExistentDomain = "nonExistentDomain";
        createUser(USER1, TenantService.DEFAULT_DOMAIN, PASS);
        createTenant(DOMAIN);
        createUser(USER2, DOMAIN, PASS);
        createUser(USER3, nonExistentDomain, PASS);
        String username3WithDomain = USER3 + TenantService.SEPARATOR + nonExistentDomain;
        try
        {
            checkDomainUserWork(USER1, TenantService.DEFAULT_DOMAIN, USER1);
        }
        catch (Exception e)
        {
            fail("The user is not from domain and is not a tenant.");
        }

        try
        {
            checkDomainUserWork(USER2_WITH_DOMAIN, DOMAIN, USER2);
        }
        catch (Exception e)
        {
            fail("The user is from domain and is a tenant.");
        }
        
        try
        {
            checkDomainUserWork(username3WithDomain, DOMAIN, USER2);
            fail("The user is not from this domain.");
        }
        catch (Exception e)
        {
            // Expected
        }
    }
    
    @Test
    public void testCheckDomain()
    {
        createUser(USER1, TenantService.DEFAULT_DOMAIN, PASS);
        createTenant(DOMAIN);
        createUser(USER2, DOMAIN, PASS);
        createUser(USER3, STRING, PASS);
        try
        {
            checkDomainWork(STRING_WITH_EXISTENT_DOMAIN, TenantService.DEFAULT_DOMAIN, USER1);
            fail("The string has a domain, which should not match the default one.");
        }
        catch (Exception e)
        {
            // Expected
        }

        try
        {
            checkDomainWork(STRING_WITH_EXISTENT_DOMAIN, DOMAIN, USER2);
        }
        catch (Exception e)
        {
            fail("The string has a tenant domain and should match the execution context tenant.");
        }
        
        try
        {
            checkDomainWork(STRING_WITH_NONEXITENT_DOMAIN, DOMAIN, USER2);
            fail("The string has a domain, which should not match the execution context tenant.");
        }
        catch (Exception e)
        {
            // Expected
        }
    }
    
    @Test
    public void testGetRootNode()
    {
        NodeRef rootNodeRefDefault = nodeService.getRootNode(DEFAULT_STORE);
        NodeRef rootNodeRef = new NodeRef(DEFAULT_STORE, IDENTIFIER);
        NodeRef nodeRef = tenantService.getRootNode(nodeService, searchService, namespaceService, ROOT_PATH, rootNodeRef);
        assertEquals("The reported rootNodeRef for the default domain is not correct.", rootNodeRefDefault, nodeRef);
        
        createTenant(DOMAIN);
        rootNodeRefDefault = nodeService.getRootNode(TENANT_STORE);
        rootNodeRef = new NodeRef(TENANT_STORE, IDENTIFIER);
        nodeRef = tenantService.getRootNode(nodeService, searchService, namespaceService, ROOT_PATH, rootNodeRef);
        assertEquals("The reported rootNodeRef for the tenant domain is not correct.", rootNodeRefDefault, nodeRef);
    }
    
    /**
     * Format of a valid domain string is "@domain@" 
     */
    @Test
    public void testIsTenantName()
    {
        boolean result = tenantService.isTenantName(STRING);
        assertFalse("The string was reported as domain, but it is not", result);
        
        result = tenantService.isTenantName(STRING_WITH_EXISTENT_DOMAIN);
        assertTrue("The string was not reported as domain.", result);
    }
    
    @Test
    public void testGetPrimaryDomain()
    {
        String result = tenantService.getPrimaryDomain(USER1);
        assertNull("The primary domain should be null for a non tenant user without a tenant in name.", result);
        
        result = tenantService.getPrimaryDomain(USER2_WITH_DOMAIN);
        assertNull("The primary domain should be null for a tenant user if multi tenancy is not enabled.", result);
        
        createTenant(DOMAIN);
        result = tenantService.getPrimaryDomain(USER2_WITH_DOMAIN);
        assertEquals("The primary domain should be of the USER2 is " + DOMAIN + ", but was reported as " + result, DOMAIN, result);
        
        result = tenantService.getPrimaryDomain(USER1);
        assertTrue("The primary domain should be the default one (empty string) for a non tenant user without a tenant in name.", result.equals(TenantService.DEFAULT_DOMAIN));
    }
    
    @Test
    public void testGetDomain() throws Exception
    {
        createUser(USER1, TenantService.DEFAULT_DOMAIN, PASS);
        String result = getDomainWork(STRING, TenantService.DEFAULT_DOMAIN, USER1, false);
        assertEquals("The domain should be reported as default.", TenantService.DEFAULT_DOMAIN, result);

        createUser(USER2, DOMAIN, PASS);
        result = getDomainWork(STRING, TenantService.DEFAULT_DOMAIN, USER2, false);
        assertEquals("The domain should be reported as default as the tenant was not created yet.", TenantService.DEFAULT_DOMAIN, result);

        createTenant(DOMAIN);
        result = getDomainWork(STRING_WITH_EXISTENT_DOMAIN, DOMAIN, USER2, false);
        assertEquals("The USER2 domain should be reported as " + DOMAIN, DOMAIN, result);

        try
        {
            result = getDomainWork(STRING_WITH_EXISTENT_DOMAIN, TenantService.DEFAULT_DOMAIN, USER1, true);
            assertEquals("The domain should be reported as " + DOMAIN, DOMAIN, result);
        }
        catch (Exception e)
        {
            fail("An exception should not be thrown.");
        }
        
        try
        {
            result = getDomainWork(STRING_WITH_NONEXITENT_DOMAIN, DOMAIN, USER2, true);
            fail("An exception should be thrown as the domains do not match.");
        }
        catch (Exception e)
        {
            // Expected
        }
    }
    
    @Test
    public void testGetTenant()
    {
        Tenant tenant = tenantService.getTenant(DOMAIN);
        assertNull("The tenant should not exist.", tenant);
        
        createTenant(DOMAIN);
        tenant = tenantService.getTenant(DOMAIN);
        assertNotNull("The tenant should exist.", tenant);
        assertTrue("The tenant should have the correct domain.", DOMAIN.equals(tenant.getTenantDomain()));
        assertTrue("The tenant should be enabled.", tenant.isEnabled());
    }
    
    @Test
    public void testGetUserDomain()
    {
        String result = tenantService.getUserDomain(USER1);
        assertEquals("The user domain should be the default one for a non tenant user without a tenant in name.", TenantService.DEFAULT_DOMAIN, result);
        
        result = tenantService.getUserDomain(USER2_WITH_DOMAIN);
        assertEquals("The user domain should be the default one for a user with email like name if multi tenancy is not enabled.", TenantService.DEFAULT_DOMAIN, result);
        
        createTenant(DOMAIN);
        result = tenantService.getUserDomain(USER2_WITH_DOMAIN);
        assertEquals("The user domain should be of the USER2 is " + DOMAIN + ", but was reported as " + result, DOMAIN, result);
        
        result = tenantService.getUserDomain(USER1);
        assertTrue("The user domain should be the default one (empty string) for a non tenant user without a tenant in name.", result.equals(TenantService.DEFAULT_DOMAIN));
    }
    
    /**
     * Runs {@link TenantService#getDomain(String)} as a specified tenant.
     * @param user The input parameter to the {@link TenantService#getDomain(String)}
     * @param domain
     * @param runAsUsername
     * @param checkCurrentDomain
     * @return domain
     * @throws Exception
     */
    private String getDomainWork(final String user, String domain, String runAsUsername, final boolean checkCurrentDomain) throws Exception
    {
        TenantRunAsWork<String> work = new TenantRunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                return tenantService.getDomain(user, checkCurrentDomain);
            }
        };
        return TenantUtil.runAsUserTenant(work, runAsUsername, domain);
    }
    
    /**
     * Runs {@link TenantService#checkDomain(String)} as a specified tenant.
     * @param string The input parameter to the {@link TenantService#checkDomain(String)}
     * @param domain
     * @param runAsUsername
     * @throws Exception
     */
    private void checkDomainWork(final String string, String domain, String runAsUsername) throws Exception
    {
        TenantRunAsWork<Void> work = new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                tenantService.checkDomain(string);
                return null;
            }
        };
        TenantUtil.runAsUserTenant(work, runAsUsername, domain);
    }
    
    /**
     * Runs {@link TenantService#checkDomainUser(String)} as a specified tenant.
     * @param username The input parameter to the {@link TenantService#checkDomainUser(String)}
     * @param domain
     * @param runAsUsername
     * @throws Exception
     */
    private void checkDomainUserWork(final String username, String domain, String runAsUsername) throws Exception
    {
        TenantRunAsWork<Void> work = new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                tenantService.checkDomainUser(username);
                return null;
            }
        };
        TenantUtil.runAsUserTenant(work, runAsUsername, domain);
    }
    
    /**
     * Create a tenant domain, if not already created
     * 
     * @param tenantDomain
     */
    private void createTenant(final String tenantDomain)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                if (!tenantAdminService.existsTenant(tenantDomain))
                {
                    tenantAdminService.createTenant(tenantDomain, (DEFAULT_ADMIN_PW + " " + tenantDomain).toCharArray(), null);
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * Crate a user and authentication
     * 
     * @param baseUserName
     * @param tenantDomain
     * @param password
     * @return the new user NideRef
     */
    private NodeRef createUser(String baseUserName, String tenantDomain, String password)
    {
        String userName = tenantService.getDomainUser(baseUserName, tenantDomain);

        NodeRef personNodeRef = null;

        if (!this.personService.personExists(userName))
        {
            // Create the authentication
            this.authenticationService.createAuthentication(userName, password.toCharArray());

            // Create the person
            Map<QName, Serializable> personProperties = new HashMap<QName, Serializable>();
            personProperties.put(ContentModel.PROP_USERNAME, userName);
            personProperties.put(ContentModel.PROP_FIRSTNAME, baseUserName);
            personProperties.put(ContentModel.PROP_LASTNAME, baseUserName + "-" + tenantDomain);
            personProperties.put(ContentModel.PROP_EMAIL, userName);

            personNodeRef = this.personService.createPerson(personProperties);
        }
        else
        {
            personNodeRef = personService.getPerson(userName);
        }

        return personNodeRef;
    }
    
    /**
     * Utility method to add a domain to an string id
     * @param id
     * @param domain
     * @return a string in format "@domain@id"
     */
    private static String addDomainToId(String id, String domain)
    {
        return TenantService.SEPARATOR + domain + TenantService.SEPARATOR + id;
    }
}
