/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.security.authentication;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;
import net.sf.acegisecurity.AccountExpiredException;
import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.AuthenticationManager;
import net.sf.acegisecurity.BadCredentialsException;
import net.sf.acegisecurity.CredentialsExpiredException;
import net.sf.acegisecurity.DisabledException;
import net.sf.acegisecurity.LockedException;
import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParamsImpl;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authentication.InMemoryTicketComponentImpl.ExpiryMode;
import org.alfresco.repo.security.authentication.InMemoryTicketComponentImpl.Ticket;
import org.alfresco.repo.security.authentication.RepositoryAuthenticationDao.CacheEntry;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.GUID;

@SuppressWarnings("unchecked")
@Category(OwnJVMTestsCategory.class)
public class AuthenticationTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private NodeService nodeService;
    private AuthorityService authorityService;
    private TenantService tenantService;
    private TenantAdminService tenantAdminService;
    private CompositePasswordEncoder compositePasswordEncoder;
    private MutableAuthenticationDao dao;
    private AuthenticationManager authenticationManager;
    private TicketComponent ticketComponent;
    private SimpleCache<String, Ticket> ticketsCache;
    private SimpleCache<String, String> usernameToTicketIdCache;
    private MutableAuthenticationService authenticationService;
    private MutableAuthenticationService pubAuthenticationService;
    private AuthenticationComponent authenticationComponent;
    private AuthenticationComponent authenticationComponentImpl;
    private TransactionService transactionService;
    private PersonService pubPersonService;
    private PersonService personService;
    private SysAdminParamsImpl sysAdminParams;

    private UserTransaction userTransaction;
    private NodeRef rootNodeRef;
    private NodeRef systemNodeRef;
    private NodeRef typesNodeRef;
    private NodeRef personAndyNodeRef;

    private static char[] DONT_CARE_PASSWORD = "1 really don't care".toCharArray();

    // TODO: pending replacement
    private Dialect dialect;

    private PolicyComponent policyComponent;
    private BehaviourFilter behaviourFilter;

    private SimpleCache<String, CacheEntry> authenticationCache;    
    private SimpleCache<String, NodeRef> immutableSingletonCache;

    private static final String TEST_RUN = System.currentTimeMillis()+"";
    private static final String TEST_TENANT_DOMAIN = TEST_RUN+".my.test";
    private static final String DEFAULT_ADMIN_PW = "admin";
    private static final String TENANT_ADMIN_PW = DEFAULT_ADMIN_PW + TEST_TENANT_DOMAIN;

    public AuthenticationTest()
    {
        super();
    }

    public AuthenticationTest(String arg0)
    {
        super(arg0);
    }

    public void setUp() throws Exception
    {
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            throw new AlfrescoRuntimeException(
                    "A previous tests did not clean up transaction: " +
                    AlfrescoTransactionSupport.getTransactionId());
        }
        
        dialect = (Dialect) ctx.getBean("dialect");
        nodeService = (NodeService) ctx.getBean("nodeService");
        authorityService = (AuthorityService) ctx.getBean("authorityService");
        tenantService = (TenantService) ctx.getBean("tenantService");
        tenantAdminService = (TenantAdminService) ctx.getBean("tenantAdminService");
        compositePasswordEncoder = (CompositePasswordEncoder) ctx.getBean("compositePasswordEncoder");
        ticketComponent = (TicketComponent) ctx.getBean("ticketComponent");
        authenticationService = (MutableAuthenticationService) ctx.getBean("authenticationService");
        pubAuthenticationService = (MutableAuthenticationService) ctx.getBean("AuthenticationService");
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        authenticationComponentImpl = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        pubPersonService =  (PersonService) ctx.getBean("PersonService");
        personService =  (PersonService) ctx.getBean("personService");
        policyComponent = (PolicyComponent) ctx.getBean("policyComponent");
        behaviourFilter = (BehaviourFilter) ctx.getBean("policyBehaviourFilter");
        authenticationCache = (SimpleCache<String, CacheEntry>) ctx.getBean("authenticationCache");
        immutableSingletonCache = (SimpleCache<String, NodeRef>) ctx.getBean("immutableSingletonCache");
        // permissionServiceSPI = (PermissionServiceSPI)
        // ctx.getBean("permissionService");
        ticketsCache = (SimpleCache<String, Ticket>) ctx.getBean("ticketsCache");
        usernameToTicketIdCache = (SimpleCache<String, String>) ctx.getBean("usernameToTicketIdCache");

        ChildApplicationContextFactory sysAdminSubsystem = (ChildApplicationContextFactory) ctx.getBean("sysAdmin");
        assertNotNull("sysAdminSubsystem", sysAdminSubsystem);
        ApplicationContext sysAdminCtx  = sysAdminSubsystem.getApplicationContext();
        sysAdminParams = (SysAdminParamsImpl) sysAdminCtx.getBean("sysAdminParams");

        dao = (MutableAuthenticationDao) ctx.getBean("authenticationDao");
        
        // Let's look inside the alfresco authentication subsystem to get the DAO-wired authentication manager
        ChildApplicationContextManager authenticationChain = (ChildApplicationContextManager) ctx.getBean("Authentication");
        ApplicationContext subsystem = authenticationChain.getApplicationContext(authenticationChain.getInstanceIds().iterator().next());
        authenticationManager = (AuthenticationManager) subsystem.getBean("authenticationManager");

        transactionService = (TransactionService) ctx.getBean(ServiceRegistry.TRANSACTION_SERVICE.getLocalName());
        
        // Clean up before we start trying to create the test user
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                try
                {
                    deleteAndy();
                    return null;
                }
                finally
                {
                    authenticationComponent.clearCurrentSecurityContext();
                }
            }
        }, false, true);
        
        userTransaction = transactionService.getUserTransaction();
        userTransaction.begin();

        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);

        QName children = ContentModel.ASSOC_CHILDREN;
        QName system = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "system");
        QName container = ContentModel.TYPE_CONTAINER;
        QName types = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "people");

        systemNodeRef = nodeService.createNode(rootNodeRef, children, system, container).getChildRef();
        typesNodeRef = nodeService.createNode(systemNodeRef, children, types, container).getChildRef();
        Map<QName, Serializable> props = createPersonProperties("Andy");
        personAndyNodeRef = nodeService.createNode(typesNodeRef, children, ContentModel.TYPE_PERSON, container, props).getChildRef();
        assertNotNull(personAndyNodeRef);
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        authenticationComponent.clearCurrentSecurityContext();
    }

    private void deleteAndy()
    {
        RepositoryAuthenticationDao dao = new RepositoryAuthenticationDao();
        dao.setTransactionService(transactionService);
        dao.setAuthorityService(authorityService);
        dao.setTenantService(tenantService);
        dao.setNodeService(nodeService);
        dao.setNamespaceService(getNamespacePrefixReolsver(""));
        dao.setCompositePasswordEncoder(compositePasswordEncoder);
        dao.setPolicyComponent(policyComponent);
        dao.setAuthenticationCache(authenticationCache);
        dao.setSingletonCache(immutableSingletonCache);
        
        if (dao.userExists("andy"))
        {
            dao.deleteUser("andy");
        }
        if (dao.userExists("Andy"))
        {
            dao.deleteUser("Andy");
        }
        
        if (personService.personExists("andy"))
        {
            personService.deletePerson("andy");
        }
        if (personService.personExists("Andy"))
        {
            personService.deletePerson("Andy");
        }
    }

    @Override
    protected void tearDown() throws Exception
    {
        if ((userTransaction.getStatus() == Status.STATUS_ACTIVE) || (userTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK))
        {
            userTransaction.rollback();
        }
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
    }

    private Map<QName, Serializable> createPersonProperties(String userName)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        return properties;
    }

    public void testSystemTicket() throws Exception
    {
        assertNull(AuthenticationUtil.getFullAuthentication());
        assertNull(AuthenticationUtil.getRunAsAuthentication());
      
        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("andy", "andy".toCharArray());
        
        pubAuthenticationService.clearCurrentSecurityContext();
        
        assertNull(AuthenticationUtil.getFullAuthentication());
        assertNull(AuthenticationUtil.getRunAsAuthentication());
        
        // Authenticate
        pubAuthenticationService.authenticate("andy", "andy".toCharArray());
        
        // Get current user name
        String userName = pubAuthenticationService.getCurrentUserName();
        assertEquals("andy", userName);
        
        // Get ticket
        String ticket = pubAuthenticationService.getCurrentTicket();
        assertEquals("andy", ticketComponent.getAuthorityForTicket(ticket));
        
        // Get logged in user ...
        // Get userName
        userName = pubAuthenticationService.getCurrentUserName();
        assertEquals("andy", userName);
        // get Person
        assertTrue(pubPersonService.personExists(userName));
        
        AuthenticationUtil.runAs(new RunAsWork<Void>() {

            public Void doWork() throws Exception
            {
                // TODO Auto-generated method stub
                assertEquals("andy", ticketComponent.getAuthorityForTicket(pubAuthenticationService.getCurrentTicket()));
                return null;
            }}, AuthenticationUtil.getSystemUserName());
        
        pubPersonService.getPerson(userName);
        assertTrue(pubPersonService.personExists(userName));
        // re-getTicket
        String newticket = pubAuthenticationService.getCurrentTicket();
        assertEquals(ticket, newticket);
        assertEquals("andy", ticketComponent.getAuthorityForTicket(newticket));
        
        
        userName = pubAuthenticationService.getCurrentUserName();
        assertEquals("andy", userName);
        
        // new TX
        
        //userTransaction.commit();
        //userTransaction = transactionService.getUserTransaction();
        //userTransaction.begin();
        
        pubAuthenticationService.validate(ticket);
        userName = pubAuthenticationService.getCurrentUserName();
        assertEquals("andy", userName);
        
        pubAuthenticationService.validate(newticket);
        userName = pubAuthenticationService.getCurrentUserName();
        assertEquals("andy", userName);
        
    }
    
    public void xtestScalability()
    {
        long create = 0;

        long start;
        long end;
        authenticationComponent.authenticate(AuthenticationUtil.getAdminUserName(), "admin".toCharArray());
        for (int i = 0; i < 10000; i++)
        {
            String id = "TestUser-" + i;
            start = System.nanoTime();
            authenticationService.createAuthentication(id, id.toCharArray());
            end = System.nanoTime();
            create += (end - start);

            if ((i > 0) && (i % 100 == 0))
            {
                System.out.println("Count = " + i);
                System.out.println("Average create : " + (create / i / 1000000.0f));
                start = System.nanoTime();
                dao.userExists(id);
                end = System.nanoTime();
                System.out.println("Exists : " + ((end - start) / 1000000.0f));
            }
        }
        authenticationComponent.clearCurrentSecurityContext();
    }

    public void testNewTicketOnLogin()
    {
        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("Andy", "auth1".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();

        // authenticate with this user details
        pubAuthenticationService.authenticate("Andy", "auth1".toCharArray());
        String ticket1 = pubAuthenticationService.getCurrentTicket();
        pubAuthenticationService.authenticate("Andy", "auth1".toCharArray());
        if(ticketComponent.getUseSingleTicketPerUser())
        {
            assertTrue(ticket1.equals(pubAuthenticationService.getCurrentTicket()));
        }
        else
        {
            assertFalse(ticket1.equals(pubAuthenticationService.getCurrentTicket()));
        }
    }
    
    public void testGuest()
    {
        authenticationService.authenticate(AuthenticationUtil.getGuestUserName(), "".toCharArray());
        Set<String> guestUsers = authenticationService.getDefaultGuestUserNames();
        assertNotNull(guestUsers);
        assertTrue(guestUsers.contains(AuthenticationUtil.getGuestUserName()));
    }

    public void testCreateUsers()
    {
        authenticationService.createAuthentication(AuthenticationUtil.getGuestUserName(), DONT_CARE_PASSWORD);
        authenticationService.authenticate(AuthenticationUtil.getGuestUserName(),DONT_CARE_PASSWORD);
        // Guest is treated like any other user
        assertEquals(AuthenticationUtil.getGuestUserName(), authenticationService.getCurrentUserName());

        authenticationService.createAuthentication("Andy", DONT_CARE_PASSWORD);
        authenticationService.authenticate("Andy", DONT_CARE_PASSWORD);
        assertEquals("Andy", authenticationService.getCurrentUserName());

        if (! tenantService.isEnabled())
        {
            authenticationService.createAuthentication("Mr.Woof.Banana@chocolate.chip.cookie.com", DONT_CARE_PASSWORD);
            authenticationService.authenticate("Mr.Woof.Banana@chocolate.chip.cookie.com", DONT_CARE_PASSWORD);
            assertEquals("Mr.Woof.Banana@chocolate.chip.cookie.com", authenticationService.getCurrentUserName());
        }
        else
        {
            // TODO - could create tenant domain 'chocolate.chip.cookie.com'
        }

        try
        {
            authenticationService.createAuthentication("Andy_Woof/Domain", DONT_CARE_PASSWORD);
            authenticationService.authenticate("Andy_Woof/Domain", DONT_CARE_PASSWORD);
            fail("Tenant domain ~,./<>?\\\\| is not valid format\"");
        }
        catch (IllegalArgumentException ignored)
        {
            // Expected exception
        }
    }
    
    private RepositoryAuthenticationDao createRepositoryAuthenticationDao()
    {
        RepositoryAuthenticationDao dao = new RepositoryAuthenticationDao();
        dao.setTransactionService(transactionService);
        dao.setTenantService(tenantService);
        dao.setNodeService(nodeService);
        dao.setAuthorityService(authorityService);
        dao.setNamespaceService(getNamespacePrefixReolsver(""));
        dao.setCompositePasswordEncoder(compositePasswordEncoder);
        dao.setPolicyComponent(policyComponent);
        dao.setAuthenticationCache(authenticationCache);
        dao.setSingletonCache(immutableSingletonCache);
        return dao;
    }

    /**
     * Test for ALF-20680
     * Test of the {@link RepositoryAuthenticationDao#getUserFolderLocation(String)} in multitenancy
     */
    public void testAuthenticateMultiTenant()
    {
        // Create a tenant domain
        TenantUtil.runAsSystemTenant(new TenantUtil.TenantRunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                if (!tenantAdminService.existsTenant(TEST_TENANT_DOMAIN))
                {
                    tenantAdminService.createTenant(TEST_TENANT_DOMAIN, TENANT_ADMIN_PW.toCharArray(), null);
                }
                return null;
            }
        }, TenantService.DEFAULT_DOMAIN);

        // Use default admin
        authenticateMultiTenantWork(AuthenticationUtil.getAdminUserName(), DEFAULT_ADMIN_PW);

        // Use tenant admin
        authenticateMultiTenantWork(AuthenticationUtil.getAdminUserName() + TenantService.SEPARATOR + TEST_TENANT_DOMAIN, TENANT_ADMIN_PW);
    }

    private void authenticateMultiTenantWork(String userName, String password)
    {
        String hashedPassword = dao.getMD4HashedPassword(userName);
        assertNotNull(hashedPassword);
        assertEquals(compositePasswordEncoder.encode("md4",password, null), hashedPassword);
    }

    /**
     * Test for ACE-4909
     */
    public void testCheckUserDisabledTenant()
    {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        String domainName = "ace4909.domain";
        String userName = "ace4909" + TenantService.SEPARATOR + domainName;
        Map<QName, Serializable> props = createPersonProperties(userName);
        NodeRef userNodeRef = personService.createPerson(props);
        assertNotNull(userNodeRef);
        authenticationService.createAuthentication(userName, "passwd".toCharArray());
        tenantAdminService.createTenant(domainName, TENANT_ADMIN_PW.toCharArray(), null);
        tenantAdminService.disableTenant(domainName);
        assertTrue("The user should exist", dao.userExists(userName));
    }

    /**
     * Test for ACE-4909
     */
    public void testCheckUserDeletedTenant()
    {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        String domainName = "ace4909.domain";
        String userName = "ace4909" + TenantService.SEPARATOR + domainName;
        Map<QName, Serializable> props = createPersonProperties(userName);
        NodeRef userNodeRef = personService.createPerson(props);
        assertNotNull(userNodeRef);
        authenticationService.createAuthentication(userName, "passwd".toCharArray());
        tenantAdminService.createTenant(domainName, TENANT_ADMIN_PW.toCharArray(), null);
        tenantAdminService.deleteTenant(domainName);
        assertTrue("The user should exist", dao.userExists(userName));
    }

    public void testCreateAndyUserAndOtherCRUD() throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        RepositoryAuthenticationDao dao = createRepositoryAuthenticationDao();
        
        dao.createUser("Andy", "cabbage".toCharArray());
        assertNotNull(dao.getUserOrNull("Andy"));

        UserDetails AndyDetails = (UserDetails) dao.loadUserByUsername("Andy");
        assertNotNull(AndyDetails);
        assertEquals("Andy", AndyDetails.getUsername());
        // assertNotNull(dao.getSalt(AndyDetails));
        assertTrue(AndyDetails.isAccountNonExpired());
        assertTrue(AndyDetails.isAccountNonLocked());
        assertTrue(AndyDetails.isCredentialsNonExpired());
        assertTrue(AndyDetails.isEnabled());
        assertNotSame("cabbage", AndyDetails.getPassword());
        assertTrue(compositePasswordEncoder.matches(compositePasswordEncoder.getPreferredEncoding(),"cabbage", AndyDetails.getPassword(), null));
        assertEquals(1, AndyDetails.getAuthorities().length);

        // Object oldSalt = dao.getSalt(AndyDetails);
        dao.updateUser("Andy", "carrot".toCharArray());
        UserDetails newDetails = (UserDetails) dao.loadUserByUsername("Andy");
        assertNotNull(newDetails);
        assertEquals("Andy", newDetails.getUsername());
        // assertNotNull(dao.getSalt(newDetails));
        assertTrue(newDetails.isAccountNonExpired());
        assertTrue(newDetails.isAccountNonLocked());
        assertTrue(newDetails.isCredentialsNonExpired());
        assertTrue(newDetails.isEnabled());
        assertNotSame("carrot", newDetails.getPassword());
        assertEquals(1, newDetails.getAuthorities().length);

        assertNotSame(AndyDetails.getPassword(), newDetails.getPassword());
        RepositoryAuthenticatedUser rau = (RepositoryAuthenticatedUser) newDetails;
        assertTrue(compositePasswordEncoder.matchesPassword("carrot", newDetails.getPassword(), null, rau.getHashIndicator()));
        // assertNotSame(oldSalt, dao.getSalt(newDetails));

        //Update again
        dao.updateUser("Andy", "potato".toCharArray());
        newDetails = (UserDetails) dao.loadUserByUsername("Andy");
        assertNotNull(newDetails);
        assertEquals("Andy", newDetails.getUsername());
        rau = (RepositoryAuthenticatedUser) newDetails;
        assertTrue(compositePasswordEncoder.matchesPassword("potato", newDetails.getPassword(), null, rau.getHashIndicator()));

        dao.deleteUser("Andy");
        assertFalse("Should not be a cache entry for 'Andy'.", authenticationCache.contains("Andy"));
        assertNull("DAO should report that 'Andy' does not exist.", dao.getUserOrNull("Andy"));
    }
    
    /** 
     * <a href="https://issues.alfresco.com/jira/browse/ALF-19301">ALF-19301: Unsafe usage of transactions around authenticationCache</a>
     */
    public void testStaleAuthenticationCacheRecovery()
    {
        RepositoryAuthenticationDao dao = createRepositoryAuthenticationDao();
        
        assertFalse("Must start with no cache entry for 'Andy'.", authenticationCache.contains("Andy"));

        dao.createUser("Andy", "cabbage".toCharArray());
        NodeRef andyNodeRef = dao.getUserOrNull("Andy");
        assertNotNull(andyNodeRef);
        assertTrue("Andy's node should exist. ", nodeService.exists(andyNodeRef));

        // So the cache should be populated.  Now, remove Andy's node but without having policies fire.
        behaviourFilter.disableBehaviour(andyNodeRef);
        nodeService.deleteNode(andyNodeRef);
        
        assertTrue("Should still have an entry for 'Andy'.", authenticationCache.contains("Andy"));
        
        assertNull("Invalid node should be detected for 'Andy'.", dao.getUserOrNull("Andy"));
        assertFalse("Cache entry should have been removed for 'Andy'.", authenticationCache.contains("Andy"));
    }
    
    /**
     * Test for use without txn.
     */
    public void testRepositoryAuthenticationDaoWithoutTxn() throws Exception
    {
        RepositoryAuthenticationDao dao = createRepositoryAuthenticationDao();
        
        dao.createUser("Andy", "cabbage".toCharArray());
        authenticationCache.remove("Andy");                 // Make sure we query
        
        this.userTransaction.commit();
        
        // Now get the user out of a transaction
        dao.userExists("Andy");
        assertTrue("Should now have an entry for 'Andy'.", authenticationCache.contains("Andy"));
    }

    public void testAuthentication()
    {
        dao.createUser("GUEST", DONT_CARE_PASSWORD);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("GUEST", new String(DONT_CARE_PASSWORD));
        token.setAuthenticated(false);

        Authentication result = authenticationManager.authenticate(token);
        assertNotNull(result);

        dao.createUser("Andy", "squash".toCharArray());

        token = new UsernamePasswordAuthenticationToken("Andy", "squash");
        token.setAuthenticated(false);

        result = authenticationManager.authenticate(token);
        assertNotNull(result);

        dao.setEnabled("Andy", false);
        try
        {
            result = authenticationManager.authenticate(token);
            assertNotNull(result);
            assertNotNull(null);
        }
        catch (DisabledException e)
        {
            // Expected
        }

        dao.setEnabled("Andy", true);
        result = authenticationManager.authenticate(token);
        assertNotNull(result);

        dao.setLocked("Andy", true);
        try
        {
            result = authenticationManager.authenticate(token);
            assertNotNull(result);
            assertNotNull(null);
        }
        catch (LockedException e)
        {
            // Expected
        }

        dao.setLocked("Andy", false);
        result = authenticationManager.authenticate(token);
        assertNotNull(result);

        dao.setAccountExpires("Andy", true);
        dao.setCredentialsExpire("Andy", true);
        result = authenticationManager.authenticate(token);
        assertNotNull(result);

        dao.setAccountExpiryDate("Andy", null);
        dao.setCredentialsExpiryDate("Andy", null);
        result = authenticationManager.authenticate(token);
        assertNotNull(result);

        dao.setAccountExpiryDate("Andy", new Date(new Date().getTime() + 10000));
        dao.setCredentialsExpiryDate("Andy", new Date(new Date().getTime() + 10000));
        result = authenticationManager.authenticate(token);
        assertNotNull(result);

        dao.setAccountExpiryDate("Andy", new Date(new Date().getTime() - 10000));
        try
        {
            result = authenticationManager.authenticate(token);
            assertNotNull(result);
            assertNotNull(null);
        }
        catch (AccountExpiredException e)
        {
            // Expected
        }
        dao.setAccountExpiryDate("Andy", new Date(new Date().getTime() + 10000));
        result = authenticationManager.authenticate(token);
        assertNotNull(result);

        dao.setCredentialsExpiryDate("Andy", new Date(new Date().getTime() - 10000));
        try
        {
            result = authenticationManager.authenticate(token);
            assertNotNull(result);
            assertNotNull(null);
        }
        catch (CredentialsExpiredException e)
        {
            // Expected
        }
        dao.setCredentialsExpiryDate("Andy", new Date(new Date().getTime() + 10000));
        result = authenticationManager.authenticate(token);
        assertNotNull(result);

        dao.deleteUser("Andy");
        // assertNull(dao.getUserOrNull("Andy"));
    }

    public void testCreateAuthenticationWhileRunningAsSystem() throws Exception
    {
        userTransaction.rollback();
        RunAsWork<Object> authWorkAsMuppet = new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                RunAsWork<Object> authWorkAsSystem = new RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        RetryingTransactionCallback<Object> txnWork = new RetryingTransactionCallback<Object>()
                        {
                            public Object execute() throws Throwable
                            {
                                pubAuthenticationService.createAuthentication("blah", "pwd".toCharArray());
                                pubAuthenticationService.deleteAuthentication("blah");
                                return null;
                            }
                        };
                        return transactionService.getRetryingTransactionHelper().doInTransaction(txnWork, false, true);
                    }
                };
                return AuthenticationUtil.runAs(authWorkAsSystem, AuthenticationUtil.getSystemUserName());
            }
        };
        AuthenticationUtil.runAs(authWorkAsMuppet, "muppet");
    }
    
    public void testPushAndPopAuthentication() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser("user1");
        assertEquals("user1", AuthenticationUtil.getFullyAuthenticatedUser());
        assertEquals("user1", AuthenticationUtil.getRunAsUser());
        
        AuthenticationUtil.setRunAsUser("user2");
        assertEquals("user1", AuthenticationUtil.getFullyAuthenticatedUser());
        assertEquals("user2", AuthenticationUtil.getRunAsUser());
        
        AuthenticationUtil.pushAuthentication();

        AuthenticationUtil.setFullyAuthenticatedUser("user3");
        AuthenticationUtil.setRunAsUser("user4");
        assertEquals("user3", AuthenticationUtil.getFullyAuthenticatedUser());
        assertEquals("user4", AuthenticationUtil.getRunAsUser());

        AuthenticationUtil.popAuthentication();
        assertEquals("user1", AuthenticationUtil.getFullyAuthenticatedUser());
        assertEquals("user2", AuthenticationUtil.getRunAsUser());
    }

    public void testAuthenticationFailure()
    {
        dao.createUser("Andy", "squash".toCharArray());

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("Andy", "turnip");
        token.setAuthenticated(false);

        try
        {
            Authentication result = authenticationManager.authenticate(token);
            assertNotNull(result);
            assertNotNull(null);
        }
        catch (BadCredentialsException e)
        {
            // Expected
        }
        dao.deleteUser("Andy");
        // assertNull(dao.getUserOrNull("Andy"));
    }

    public void testTicket()
    {
        dao.createUser("Andy", "ticket".toCharArray());

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("Andy", "ticket");
        token.setAuthenticated(false);

        Authentication result = authenticationManager.authenticate(token);
        result.setAuthenticated(true);

        String ticket = ticketComponent.getNewTicket(getUserName(result));
        String user = ticketComponent.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy", true), ticket);

        user = null;
        try
        {
            user = ticketComponent.validateTicket("INVALID");
            assertNotNull(null);
        }
        catch (AuthenticationException e)
        {
            assertNull(user);
        }

        ticketComponent.invalidateTicketById(ticket);
        try
        {
            user = ticketComponent.validateTicket(ticket);
            assertEquals(ticketComponent.getCurrentTicket("Andy", true), ticket);
            assertNotNull(null);
        }
        catch (AuthenticationException e)
        {

        }

        dao.deleteUser("Andy");
        // assertNull(dao.getUserOrNull("Andy"));

    }

    public void testTicketRepeat()
    {
        InMemoryTicketComponentImpl tc = new InMemoryTicketComponentImpl();
        tc.setOneOff(false);
        tc.setTicketsExpire(false);
        tc.setValidDuration("P0D");
        tc.setTicketsCache(ticketsCache);
        tc.setUsernameToTicketIdCache(usernameToTicketIdCache);

        dao.createUser("Andy", "ticket".toCharArray());

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("Andy", "ticket");
        token.setAuthenticated(false);

        Authentication result = authenticationManager.authenticate(token);
        result.setAuthenticated(true);

        String ticket = tc.getNewTicket(getUserName(result));
        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy", true), ticket);
        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy", true), ticket);

        dao.deleteUser("Andy");
        // assertNull(dao.getUserOrNull("Andy"));
    }

    public void testTicketOneOff()
    {
        InMemoryTicketComponentImpl tc = new InMemoryTicketComponentImpl();
        tc.setOneOff(true);
        tc.setTicketsExpire(false);
        tc.setValidDuration("P0D");
        tc.setTicketsCache(ticketsCache);
        tc.setUsernameToTicketIdCache(usernameToTicketIdCache);

        dao.createUser("Andy", "ticket".toCharArray());

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("Andy", "ticket");
        token.setAuthenticated(false);

        Authentication result = authenticationManager.authenticate(token);
        result.setAuthenticated(true);

        String ticket = tc.getNewTicket(getUserName(result));
        tc.validateTicket(ticket);
        assertTrue(!ticketComponent.getCurrentTicket("Andy", true).equals(ticket));
        try
        {
            tc.validateTicket(ticket);
            assertNotNull(null);
        }
        catch (AuthenticationException e)
        {

        }

        dao.deleteUser("Andy");
        // assertNull(dao.getUserOrNull("Andy"));
    }

    public void testTicketExpiryMode()
    {   
        ticketsCache.clear();
        usernameToTicketIdCache.clear();
        
        InMemoryTicketComponentImpl tc = new InMemoryTicketComponentImpl();
        tc.setOneOff(false);
        tc.setTicketsExpire(true);
        tc.setValidDuration("P5S");
        tc.setTicketsCache(ticketsCache);
        tc.setUsernameToTicketIdCache(usernameToTicketIdCache);
        tc.setExpiryMode(ExpiryMode.AFTER_FIXED_TIME.toString());

        dao.createUser("Andy", "ticket".toCharArray());

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("Andy", "ticket");
        token.setAuthenticated(false);

        Authentication result = authenticationManager.authenticate(token);
        result.setAuthenticated(true);

        String ticket = tc.getNewTicket(getUserName(result));
        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy", true), ticket);
        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy", true), ticket);
        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy", true), ticket);

        synchronized (this)
        {
            try
            {
                wait(10000);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            tc.validateTicket(ticket);
            assertNotNull(null);
        }
        catch (AuthenticationException e)
        {

        }

        try
        {
            tc.validateTicket(ticket);
            assertNotNull(null);
        }
        catch (AuthenticationException e)
        {

        }

        try
        {
            tc.validateTicket(ticket);
            assertNotNull(null);
        }
        catch (AuthenticationException e)
        {

        }

        synchronized (this)
        {
            try
            {
                wait(10000);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try
        {
            tc.validateTicket(ticket);
            assertNotNull(null);
        }
        catch (AuthenticationException e)
        {

        }

        tc.setExpiryMode(ExpiryMode.AFTER_INACTIVITY.toString());
        ticket = tc.getNewTicket(getUserName(result));

        for (int i = 0; i < 50; i++)
        {
            synchronized (this)
            {

                try
                {
                    wait(100);
                }
                catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                tc.validateTicket(ticket);

            }
        }
        
        synchronized (this)
        {
            try
            {
                wait(10000);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try
        {
            tc.validateTicket(ticket);
            assertNotNull(null);
        }
        catch (AuthenticationException e)
        {

        }
        

        dao.deleteUser("Andy");
        // assertNull(dao.getUserOrNull("Andy"));
    }

    public void testTicketExpires()
    {
        ticketsCache.clear();
        usernameToTicketIdCache.clear();
        InMemoryTicketComponentImpl tc = new InMemoryTicketComponentImpl();
        tc.setOneOff(false);
        tc.setTicketsExpire(true);
        tc.setValidDuration("P5S");
        tc.setTicketsCache(ticketsCache);
        tc.setUsernameToTicketIdCache(usernameToTicketIdCache);

        dao.createUser("Andy", "ticket".toCharArray());

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("Andy", "ticket");
        token.setAuthenticated(false);

        Authentication result = authenticationManager.authenticate(token);
        result.setAuthenticated(true);

        String ticket = tc.getNewTicket(getUserName(result));
        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy", true), ticket);
        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy", true), ticket);
        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy", true), ticket);

        synchronized (this)
        {
            try
            {
                wait(10000);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            tc.validateTicket(ticket);
            assertNotNull(null);
        }
        catch (AuthenticationException e)
        {

        }

        try
        {
            tc.validateTicket(ticket);
            assertNotNull(null);
        }
        catch (AuthenticationException e)
        {

        }

        try
        {
            tc.validateTicket(ticket);
            assertNotNull(null);
        }
        catch (AuthenticationException e)
        {

        }

        synchronized (this)
        {
            try
            {
                wait(10000);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try
        {
            tc.validateTicket(ticket);
            assertNotNull(null);
        }
        catch (AuthenticationException e)
        {

        }

        dao.deleteUser("Andy");
        // assertNull(dao.getUserOrNull("Andy"));
    }

    public void testTicketDoesNotExpire()
    {
        InMemoryTicketComponentImpl tc = new InMemoryTicketComponentImpl();
        tc.setOneOff(false);
        tc.setTicketsExpire(true);
        tc.setValidDuration("P1D");
        tc.setTicketsCache(ticketsCache);
        tc.setUsernameToTicketIdCache(usernameToTicketIdCache);

        dao.createUser("Andy", "ticket".toCharArray());

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("Andy", "ticket");
        token.setAuthenticated(false);

        Authentication result = authenticationManager.authenticate(token);
        result.setAuthenticated(true);

        String ticket = tc.getNewTicket(getUserName(result));
        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy", true), ticket);
        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy", true), ticket);
        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy", true), ticket);
        synchronized (this)
        {
            try
            {
                wait(10000);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy", true), ticket);

        dao.deleteUser("Andy");
        // assertNull(dao.getUserOrNull("Andy"));

    }

    public void testAuthenticationServiceGetNewTicket()
    {
        authenticationService.createAuthentication("GUEST", DONT_CARE_PASSWORD);
        authenticationService.authenticate("GUEST", DONT_CARE_PASSWORD);

        // create an authentication object e.g. the user
        authenticationService.createAuthentication("Andy", "auth1".toCharArray());

        // authenticate with this user details
        authenticationService.authenticate("Andy", "auth1".toCharArray());

        // assert the user is authenticated
        assertEquals("Andy", authenticationService.getCurrentUserName());

        String ticket1 = authenticationService.getCurrentTicket();

        authenticationService.authenticate("Andy", "auth1".toCharArray());

        // assert the user is authenticated
        assertEquals("Andy", authenticationService.getCurrentUserName());

        String ticket2 = authenticationService.getCurrentTicket();

        if(ticketComponent.getUseSingleTicketPerUser())
        {
            assertTrue(ticket1.equals(ticket2));
        }
        else
        {
            assertFalse(ticket1.equals(ticket2));
        }
    }

    public void testAuthenticationService1()
    {
        authenticationService.createAuthentication("GUEST", DONT_CARE_PASSWORD);
        authenticationService.authenticate("GUEST", DONT_CARE_PASSWORD);

        // create an authentication object e.g. the user
        authenticationService.createAuthentication("Andy", "auth1".toCharArray());

        // authenticate with this user details
        authenticationService.authenticate("Andy", "auth1".toCharArray());

        // assert the user is authenticated
        assertEquals("Andy", authenticationService.getCurrentUserName());
        // delete the user authentication object

        authenticationService.clearCurrentSecurityContext();
        authenticationService.deleteAuthentication("Andy");

        // create a new authentication user object
        authenticationService.createAuthentication("Andy", "auth2".toCharArray());
        // change the password
        authenticationService.setAuthentication("Andy", "auth3".toCharArray());
        // authenticate again to assert password changed
        authenticationService.authenticate("Andy", "auth3".toCharArray());

        try
        {
            authenticationService.authenticate("Andy", "auth1".toCharArray());
            fail("Authentication should have been rejected");
        }
        catch (AuthenticationException e)
        {

        }
    }

    public void testAuthenticationService2()
    {
        authenticationService.createAuthentication("GUEST", DONT_CARE_PASSWORD);
        authenticationService.authenticate("GUEST", DONT_CARE_PASSWORD);

        // create an authentication object e.g. the user
        authenticationService.createAuthentication("Andy", "auth1".toCharArray());

        // authenticate with this user details
        authenticationService.authenticate("Andy", "auth1".toCharArray());

        // assert the user is authenticated
        assertEquals("Andy", authenticationService.getCurrentUserName());
        // delete the user authentication object

        authenticationService.clearCurrentSecurityContext();
        authenticationService.deleteAuthentication("Andy");

        // create a new authentication user object
        authenticationService.createAuthentication("Andy", "auth2".toCharArray());
        // change the password
        authenticationService.setAuthentication("Andy", "auth3".toCharArray());
        // authenticate again to assert password changed
        authenticationService.authenticate("Andy", "auth3".toCharArray());

        try
        {
            authenticationService.authenticate("Andy", "auth2".toCharArray());
            fail("Authentication should have been rejected");
        }
        catch (AuthenticationException e)
        {

        }
    }

    public void testAuthenticationService3()
    {
        authenticationService.createAuthentication("GUEST", DONT_CARE_PASSWORD);
        authenticationService.authenticate("GUEST", DONT_CARE_PASSWORD);

        // create an authentication object e.g. the user
        authenticationService.createAuthentication("Andy", "auth1".toCharArray());

        // authenticate with this user details
        authenticationService.authenticate("Andy", "auth1".toCharArray());

        // assert the user is authenticated
        assertEquals("Andy", authenticationService.getCurrentUserName());
        // delete the user authentication object

        authenticationService.clearCurrentSecurityContext();
        authenticationService.deleteAuthentication("Andy");

        // create a new authentication user object
        authenticationService.createAuthentication("Andy", "auth2".toCharArray());
        // change the password
        authenticationService.setAuthentication("Andy", "auth3".toCharArray());
        // authenticate again to assert password changed
        authenticationService.authenticate("Andy", "auth3".toCharArray());

        authenticationService.authenticate("Andy", "auth3".toCharArray());
        // get the ticket that represents the current user authentication
        // instance
        String ticket = authenticationService.getCurrentTicket();
        // validate our ticket is still valid
        authenticationService.validate(ticket);
        assertEquals(ticket, authenticationService.getCurrentTicket());

        // destroy the ticket instance
        authenticationService.invalidateTicket(ticket);
        try
        {
            authenticationService.validate(ticket);
            fail("Invalid taicket should have been rejected");
        }
        catch (AuthenticationException e)
        {

        }

    }

    public void testAuthenticationService4()
    {
        authenticationService.createAuthentication("GUEST", DONT_CARE_PASSWORD);
        authenticationService.authenticate("GUEST", DONT_CARE_PASSWORD);

        // create an authentication object e.g. the user
        authenticationService.createAuthentication("Andy", "auth1".toCharArray());

        // authenticate with this user details
        authenticationService.authenticate("Andy", "auth1".toCharArray());

        // assert the user is authenticated
        assertEquals("Andy", authenticationService.getCurrentUserName());
        // delete the user authentication object

        authenticationService.clearCurrentSecurityContext();
        authenticationService.deleteAuthentication("Andy");

        // create a new authentication user object
        authenticationService.createAuthentication("Andy", "auth2".toCharArray());
        // change the password
        authenticationService.setAuthentication("Andy", "auth3".toCharArray());
        // authenticate again to assert password changed
        authenticationService.authenticate("Andy", "auth3".toCharArray());

        authenticationService.authenticate("Andy", "auth3".toCharArray());
        // get the ticket that represents the current user authentication
        // instance
        String ticket = authenticationService.getCurrentTicket();
        // validate our ticket is still valid

        authenticationService.clearCurrentSecurityContext();
        authenticationService.validate(ticket);
        assertEquals(ticket, authenticationService.getCurrentTicket());

        // destroy the ticket instance
        authenticationService.invalidateTicket(ticket);

        Authentication current = authenticationComponent.getCurrentAuthentication();
        if (current != null)
        {
            // Still authentication
            assertTrue(current.isAuthenticated());
        }

        try
        {
            authenticationService.validate(ticket);
            fail("Invalid ticket should have been rejected");
        }
        catch (AuthenticationException e)
        {
            assertNull(authenticationComponentImpl.getCurrentAuthentication());
        }

    }

    public void testAuthenticationService()
    {
        authenticationService.createAuthentication("GUEST", DONT_CARE_PASSWORD);
        authenticationService.authenticate("GUEST", DONT_CARE_PASSWORD);

        // create an authentication object e.g. the user
        authenticationService.createAuthentication("Andy", "auth1".toCharArray());

        // authenticate with this user details
        authenticationService.authenticate("Andy", "auth1".toCharArray());

        // assert the user is authenticated
        assertEquals("Andy", authenticationService.getCurrentUserName());
        // delete the user authentication object

        authenticationService.clearCurrentSecurityContext();
        authenticationService.deleteAuthentication("Andy");

        // create a new authentication user object
        authenticationService.createAuthentication("Andy", "auth2".toCharArray());
        // change the password
        authenticationService.setAuthentication("Andy", "auth3".toCharArray());
        // authenticate again to assert password changed
        authenticationService.authenticate("Andy", "auth3".toCharArray());

        // update the authentication
        authenticationService.updateAuthentication("Andy", "auth3".toCharArray(), "auth4".toCharArray());
        authenticationService.authenticate("Andy", "auth4".toCharArray());

        authenticationService.authenticate("Andy", "auth4".toCharArray());
        // get the ticket that represents the current user authentication
        // instance
        String ticket = authenticationService.getCurrentTicket();
        // validate our ticket is still valid
        authenticationService.validate(ticket);
        assertEquals(ticket, authenticationService.getCurrentTicket());

        // destroy the ticket instance
        authenticationService.invalidateTicket(ticket);

        Authentication current = authenticationComponent.getCurrentAuthentication();
        if (current != null)
        {
            assertTrue(current.isAuthenticated());
        }

        authenticationService.authenticate("Andy", "auth4".toCharArray());

        authenticationService.updateAuthentication("Andy", "auth4".toCharArray(), "auth5".toCharArray());

        authenticationService.authenticate("Andy", "auth5".toCharArray());

        // clear any context and check we are no longer authenticated
        authenticationService.clearCurrentSecurityContext();
        assertNull(authenticationService.getCurrentUserName());

        dao.deleteUser("Andy");
        // assertNull(dao.getUserOrNull("Andy"));
    }

    public void testAuthenticationService0()
    {
        authenticationService.createAuthentication("GUEST", DONT_CARE_PASSWORD);
        authenticationService.authenticate("GUEST", DONT_CARE_PASSWORD);

        // create an authentication object e.g. the user
        authenticationService.createAuthentication("Andy", "auth1".toCharArray());

        // authenticate with this user details
        authenticationService.authenticate("Andy", "auth1".toCharArray());

        // assert the user is authenticated
        assertEquals("Andy", authenticationService.getCurrentUserName());
        // delete the user authentication object

        authenticationService.clearCurrentSecurityContext();
        authenticationService.deleteAuthentication("Andy");

        // create a new authentication user object
        authenticationService.createAuthentication("Andy", "auth2".toCharArray());
        // change the password
        authenticationService.setAuthentication("Andy", "auth3".toCharArray());
        // authenticate again to assert password changed
        authenticationService.authenticate("Andy", "auth3".toCharArray());

        // update the authentication
        authenticationService.updateAuthentication("Andy", "auth3".toCharArray(), "auth4".toCharArray());
        authenticationService.authenticate("Andy", "auth4".toCharArray());

        authenticationService.authenticate("Andy", "auth4".toCharArray());
        // get the ticket that represents the current user authentication
        // instance
        String ticket = authenticationService.getCurrentTicket();
        // validate our ticket is still valid
        authenticationService.validate(ticket);
        assertEquals(ticket, authenticationService.getCurrentTicket());

        // destroy the ticket instance
        authenticationService.invalidateTicket(ticket);

        Authentication current = authenticationComponent.getCurrentAuthentication();
        if (current != null)
        {
            assertTrue(current.isAuthenticated());
        }

        authenticationService.authenticate("Andy", "auth4".toCharArray());

        // clear any context and check we are no longer authenticated
        authenticationService.clearCurrentSecurityContext();
        assertNull(authenticationService.getCurrentUserName());

        dao.deleteUser("Andy");
        // assertNull(dao.getUserOrNull("Andy"));
    }

    public void testPubAuthenticationService1()
    {
        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("GUEST", DONT_CARE_PASSWORD);
        authenticationComponent.clearCurrentSecurityContext();

        pubAuthenticationService.authenticate("GUEST", DONT_CARE_PASSWORD);

        // create an authentication object e.g. the user

        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("Andy", "auth1".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();

        // authenticate with this user details
        pubAuthenticationService.authenticate("Andy", "auth1".toCharArray());

        // assert the user is authenticated
        assertEquals("Andy", authenticationService.getCurrentUserName());
        // delete the user authentication object

        pubAuthenticationService.clearCurrentSecurityContext();

        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.deleteAuthentication("Andy");
        authenticationComponent.clearCurrentSecurityContext();

        // create a new authentication user object
        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("Andy", "auth2".toCharArray());
        // change the password
        pubAuthenticationService.setAuthentication("Andy", "auth3".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();
        // authenticate again to assert password changed
        pubAuthenticationService.authenticate("Andy", "auth3".toCharArray());

        try
        {
            pubAuthenticationService.authenticate("Andy", "auth1".toCharArray());
            fail("Authentication should fail");
        }
        catch (AuthenticationException e)
        {

        }

    }

    public void testPubAuthenticationService2()
    {
        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("GUEST", DONT_CARE_PASSWORD);
        authenticationComponent.clearCurrentSecurityContext();

        pubAuthenticationService.authenticate("GUEST", DONT_CARE_PASSWORD);

        // create an authentication object e.g. the user

        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("Andy", "auth1".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();

        // authenticate with this user details
        pubAuthenticationService.authenticate("Andy", "auth1".toCharArray());

        // assert the user is authenticated
        assertEquals("Andy", authenticationService.getCurrentUserName());
        // delete the user authentication object

        pubAuthenticationService.clearCurrentSecurityContext();

        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.deleteAuthentication("Andy");
        authenticationComponent.clearCurrentSecurityContext();

        // create a new authentication user object
        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("Andy", "auth2".toCharArray());
        // change the password
        pubAuthenticationService.setAuthentication("Andy", "auth3".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();
        // authenticate again to assert password changed
        pubAuthenticationService.authenticate("Andy", "auth3".toCharArray());

        try
        {
            pubAuthenticationService.authenticate("Andy", "auth2".toCharArray());
            fail("Authentication should fail");
        }
        catch (AuthenticationException e)
        {

        }
    }

    public void testPubAuthenticationService3()
    {
        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("GUEST", DONT_CARE_PASSWORD);
        authenticationComponent.clearCurrentSecurityContext();

        pubAuthenticationService.authenticate("GUEST", DONT_CARE_PASSWORD);

        // create an authentication object e.g. the user

        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("Andy", "auth1".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();

        // authenticate with this user details
        pubAuthenticationService.authenticate("Andy", "auth1".toCharArray());

        // assert the user is authenticated
        assertEquals("Andy", authenticationService.getCurrentUserName());
        // delete the user authentication object

        pubAuthenticationService.clearCurrentSecurityContext();

        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.deleteAuthentication("Andy");
        authenticationComponent.clearCurrentSecurityContext();

        // create a new authentication user object
        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("Andy", "auth2".toCharArray());
        // change the password
        pubAuthenticationService.setAuthentication("Andy", "auth3".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();
        assertNull(authenticationComponent.getCurrentAuthentication());
        // authenticate again to assert password changed
        pubAuthenticationService.authenticate("Andy", "auth3".toCharArray());

        pubAuthenticationService.authenticate("Andy", "auth3".toCharArray());
        // get the ticket that represents the current user authentication
        // instance
        String ticket = pubAuthenticationService.getCurrentTicket();
        authenticationComponent.clearCurrentSecurityContext();
        assertNull(authenticationComponent.getCurrentAuthentication());

        // validate our ticket is still valid
        pubAuthenticationService.validate(ticket);
        assertEquals(ticket, authenticationService.getCurrentTicket());

        // destroy the ticket instance
        pubAuthenticationService.invalidateTicket(ticket);
        try
        {
            pubAuthenticationService.validate(ticket);
            fail("Ticket should not validate");
        }
        catch (AuthenticationException e)
        {

        }
    }

    public void testAlwaysGetNewTicket()
    {
        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("Andy", "auth1".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();

        // authenticate with this user details
        pubAuthenticationService.authenticate("Andy", "auth1".toCharArray());

        // assert the user is authenticated
        assertEquals("Andy", authenticationService.getCurrentUserName());

        // get the ticket that represents the current user authentication instance
        String ticket = pubAuthenticationService.getCurrentTicket();

        // validate our ticket is still valid
        pubAuthenticationService.validate(ticket);
        assertEquals(ticket, authenticationService.getCurrentTicket());

        // destroy the ticket instance, but keep the current security context
        pubAuthenticationService.invalidateTicket(ticket);

        // we should be able to get a ticket now, because if we got past the authentication filters, meaning
        // we have a valid security context,
        // it means that we should be able to use the application (for external SSO case, for example)
        String ticketRenew = pubAuthenticationService.getCurrentTicket();
        // validate our ticket is still valid
        pubAuthenticationService.validate(ticketRenew);
        assertEquals(ticketRenew, authenticationService.getCurrentTicket());

        pubAuthenticationService.invalidateTicket(ticketRenew);
        try
        {
            pubAuthenticationService.validate(ticketRenew);
            fail("Ticket should not validate");
        }
        catch (AuthenticationException e)
        {
            // intentionally left blank
        }
        try
        {
            String ticketRenewAfterValidate = pubAuthenticationService.getCurrentTicket();
            fail(" Previous call to validate should have cleared the context, so no new tickets should be issued");
        }
        catch (Exception e)
        {
            // we expect this here
        }
    }

    public void testPubAuthenticationService()
    {
        // pubAuthenticationService.authenticateAsGuest();
        // authenticationComponent.clearCurrentSecurityContext();

        assertNull(authenticationComponent.getCurrentAuthentication());
        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("GUEST", DONT_CARE_PASSWORD);
        authenticationComponent.clearCurrentSecurityContext();

        assertNull(authenticationComponent.getCurrentAuthentication());
        pubAuthenticationService.authenticate("GUEST", DONT_CARE_PASSWORD);
        pubAuthenticationService.authenticate("GUEST", DONT_CARE_PASSWORD);
        authenticationComponent.clearCurrentSecurityContext();
        assertNull(authenticationComponent.getCurrentAuthentication());

        pubAuthenticationService.authenticateAsGuest();
        authenticationComponent.clearCurrentSecurityContext();
        assertNull(authenticationComponent.getCurrentAuthentication());

        // create an authentication object e.g. the user

        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("Andy", "auth1".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();

        // authenticate with this user details
        pubAuthenticationService.authenticate("Andy", "auth1".toCharArray());

        // assert the user is authenticated
        assertEquals("Andy", authenticationService.getCurrentUserName());
        // delete the user authentication object

        pubAuthenticationService.clearCurrentSecurityContext();

        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.deleteAuthentication("Andy");
        authenticationComponent.clearCurrentSecurityContext();

        // create a new authentication user object
        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("Andy", "auth2".toCharArray());
        // change the password
        pubAuthenticationService.setAuthentication("Andy", "auth3".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();
        // authenticate again to assert password changed
        pubAuthenticationService.authenticate("Andy", "auth3".toCharArray());

        pubAuthenticationService.authenticate("Andy", "auth3".toCharArray());
        // get the ticket that represents the current user authentication
        // instance
        String ticket = pubAuthenticationService.getCurrentTicket();
        // validate our ticket is still valid
        pubAuthenticationService.validate(ticket);
        assertEquals(ticket, authenticationService.getCurrentTicket());

        // destroy the ticket instance
        pubAuthenticationService.invalidateTicket(ticket);

    }

    public void testPubAuthenticationService0()
    {
        // pubAuthenticationService.authenticateAsGuest();
        // authenticationComponent.clearCurrentSecurityContext();

        assertNull(authenticationComponent.getCurrentAuthentication());
        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("GUEST", DONT_CARE_PASSWORD);
        authenticationComponent.clearCurrentSecurityContext();

        assertNull(authenticationComponent.getCurrentAuthentication());
        pubAuthenticationService.authenticate("GUEST", DONT_CARE_PASSWORD);
        pubAuthenticationService.authenticate("GUEST", DONT_CARE_PASSWORD);
        authenticationComponent.clearCurrentSecurityContext();
        assertNull(authenticationComponent.getCurrentAuthentication());

        pubAuthenticationService.authenticateAsGuest();
        authenticationComponent.clearCurrentSecurityContext();
        assertNull(authenticationComponent.getCurrentAuthentication());

        // create an authentication object e.g. the user

        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("Andy", "auth1".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();

        // authenticate with this user details
        pubAuthenticationService.authenticate("Andy", "auth1".toCharArray());

        // assert the user is authenticated
        assertEquals("Andy", authenticationService.getCurrentUserName());
        // delete the user authentication object

        pubAuthenticationService.clearCurrentSecurityContext();

        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.deleteAuthentication("Andy");
        authenticationComponent.clearCurrentSecurityContext();

        // create a new authentication user object
        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("Andy", "auth2".toCharArray());
        // change the password
        pubAuthenticationService.setAuthentication("Andy", "auth3".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();
        // authenticate again to assert password changed
        pubAuthenticationService.authenticate("Andy", "auth3".toCharArray());

        pubAuthenticationService.authenticate("Andy", "auth3".toCharArray());
        // get the ticket that represents the current user authentication
        // instance
        String ticket = pubAuthenticationService.getCurrentTicket();
        // validate our ticket is still valid
        pubAuthenticationService.validate(ticket);
        assertEquals(ticket, authenticationService.getCurrentTicket());

        // destroy the ticket instance
        pubAuthenticationService.invalidateTicket(ticket);

        authenticationComponent.clearCurrentSecurityContext();

        pubAuthenticationService.authenticate("Andy", "auth3".toCharArray());
        pubAuthenticationService.updateAuthentication("Andy", "auth3".toCharArray(), "auth4".toCharArray());
        pubAuthenticationService.authenticate("Andy", "auth4".toCharArray());

        try
        {
            pubAuthenticationService.updateAuthentication("Andy", "auth3".toCharArray(), "auth4".toCharArray());
            fail("Should not be able to update");
        }
        catch (AuthenticationException ae)
        {

        }

    }

    public void testAbstractAuthenticationComponentGuestUserSupport()
    {
        authenticationComponent.setGuestUserAsCurrentUser();
        assertEquals(authenticationComponent.getCurrentUserName(), authenticationComponent.getGuestUserName());
    }

    public void testPassThroughLogin()
    {
        authenticationService.createAuthentication("Andy", "auth1".toCharArray());

        authenticationComponent.setCurrentUser("Andy");
        assertEquals("Andy", authenticationService.getCurrentUserName());

        // authenticationService.deleteAuthentication("andy");
    }
    public void testAuthenticationServiceImpl()
    {
        Set<String> domains = authenticationService.getDomains();
        assertNotNull(domains);
        domains = authenticationService.getDomainsThatAllowUserCreation();
        assertNotNull(domains);
        domains = authenticationService.getDomiansThatAllowUserPasswordChanges();
        assertNotNull(domains);
        domains = authenticationService.getDomainsThatAllowUserDeletion();
        assertNotNull(domains);

        List<AuthenticationService> services = ((AbstractChainingAuthenticationService) authenticationService).getUsableAuthenticationServices();
        for (AuthenticationService service : services)
        {
            if (service instanceof AuthenticationServiceImpl)
            {
                AuthenticationServiceImpl impl = (AuthenticationServiceImpl) service;

                assertFalse("Not just anyone", impl.authenticationExists("anyone"));
                assertFalse("Hardcoded to true", impl.getAuthenticationEnabled("anyone"));
                authenticationService.invalidateUserSession("anyone");

                impl.setDomain("mydomain");
                String domain = impl.getDomain();
                assertEquals("mydomain", domain);
                Set<TicketComponent> ticketComponents = impl.getTicketComponents();
                assertNotNull(ticketComponents);

                boolean allows = impl.getAllowsUserPasswordChange();
                impl.setAllowsUserPasswordChange(allows);
                assertEquals(allows, impl.getAllowsUserPasswordChange());

                allows = impl.getAllowsUserDeletion();
                impl.setAllowsUserDeletion(allows);
                assertEquals(allows, impl.getAllowsUserDeletion());

                allows = impl.getAllowsUserCreation();
                impl.setAllowsUserCreation(allows);
                assertEquals(allows, impl.getAllowsUserCreation());

                assertFalse(impl.isCurrentUserTheSystemUser());

                Set<String> users = impl.getUsersWithTickets(true);
                assertNotNull(users);
                int tickets = impl.countTickets(true);
                assertFalse(tickets < users.size());

                tickets = impl.invalidateTickets(true);

                assertTrue(impl.guestUserAuthenticationAllowed());

                break;
            }
        }
    }

    public void testLoginNotExistingTenant()
    {
        boolean wasEnabled = AuthenticationUtil.isMtEnabled();
        
        try
        {
            tenantAdminService.createTenant(GUID.generate() + "test1.test", "admin".toCharArray());
            
            String notExistingTenant = GUID.generate() + "tenant.test";
            String userName = "user@" + notExistingTenant;
            
            assertFalse(tenantAdminService.existsTenant(notExistingTenant));
            
            try
            {
                pubAuthenticationService.authenticate(userName, GUID.generate().toCharArray());
                fail();
            }
            catch (AuthenticationException e)
            {
                // it is expected exception
            }
        }
        finally
        {
            AuthenticationUtil.setMtEnabled(wasEnabled);
        }
    }

    /**
     * ACE-3542: test that "server.maxusers" setting limits the number of unique logins to that number.
     */
    public void testMaxUsers()
    {
        final String user1 = GUID.generate();
        final String user2 = GUID.generate();

        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                authenticationService.createAuthentication(user1, "password".toCharArray());
                authenticationService.createAuthentication(user2, "password".toCharArray());
                ticketComponent.invalidateTickets(false);
                return null;
            }
        });

        int maxUsers = sysAdminParams.getMaxUsers();

        try
        {
            sysAdminParams.setMaxUsers(1);

            authenticationService.authenticate(user1, "password".toCharArray());

            try
            {
                authenticationService.authenticate(user2, "password".toCharArray());
                fail("Number of logins should not exceed maxUsers setting");
            }
            catch (AuthenticationException e)
            {
                // it is expected exception
            }
        }
        finally
        {
            sysAdminParams.setMaxUsers(maxUsers);
        }
    }

    
    /**
     * Tests the scenario where a user logs in after the system has been upgraded.
     * Their password should get re-hashed using the preferred encoding.
     */
    public void testRehashedPasswordOnAuthentication() throws Exception
    {
        // create the Andy authentication
        assertNull(authenticationComponent.getCurrentAuthentication());
        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("Andy", "auth1".toCharArray());
        
        // find the node representing the Andy user and it's properties
        NodeRef andyUserNodeRef = getRepositoryAuthenticationDao(). getUserOrNull("Andy");
        assertNotNull(andyUserNodeRef);
        
        // ensure the properties are in the state we're expecting
        Map<QName, Serializable> userProps = nodeService.getProperties(andyUserNodeRef);
        String passwordProp = (String)userProps.get(ContentModel.PROP_PASSWORD);
        assertNull("Expected the password property to be null", passwordProp);
        String password2Prop = (String)userProps.get(ContentModel.PROP_PASSWORD_SHA256);
        assertNull("Expected the password2 property to be null", password2Prop);
        String passwordHashProp = (String)userProps.get(ContentModel.PROP_PASSWORD_HASH);
        assertNotNull("Expected the passwordHash property to be populated", passwordHashProp);
        List<String> hashIndicatorProp = (List<String>)userProps.get(ContentModel.PROP_HASH_INDICATOR);
        assertNotNull("Expected the hashIndicator property to be populated", hashIndicatorProp);
     
        // re-generate an md4 hashed password
        MD4PasswordEncoderImpl md4PasswordEncoder = new MD4PasswordEncoderImpl();
        String md4Password = md4PasswordEncoder.encodePassword("auth1", null);
        
        // re-generate a sha256 hashed password
        String salt = (String)userProps.get(ContentModel.PROP_SALT);
        ShaPasswordEncoderImpl sha256PasswordEncoder = new ShaPasswordEncoderImpl(256);
        String sha256Password = sha256PasswordEncoder.encodePassword("auth1", salt);
        
        // change the underlying user object to represent state in previous release
        userProps.put(ContentModel.PROP_PASSWORD, md4Password);
        userProps.put(ContentModel.PROP_PASSWORD_SHA256, sha256Password);
        userProps.remove(ContentModel.PROP_PASSWORD_HASH);
        userProps.remove(ContentModel.PROP_HASH_INDICATOR);
        nodeService.setProperties(andyUserNodeRef, userProps);
        
        // make sure the changes took effect
        Map<QName, Serializable> updatedProps = nodeService.getProperties(andyUserNodeRef);
        String usernameProp = (String)updatedProps.get(ContentModel.PROP_USER_USERNAME);
        assertEquals("Expected the username property to be 'Andy'", "Andy", usernameProp);
        passwordProp = (String)updatedProps.get(ContentModel.PROP_PASSWORD);
        assertNotNull("Expected the password property to be populated", passwordProp);
        password2Prop = (String)updatedProps.get(ContentModel.PROP_PASSWORD_SHA256);
        assertNotNull("Expected the password2 property to be populated", password2Prop);
        passwordHashProp = (String)updatedProps.get(ContentModel.PROP_PASSWORD_HASH);
        assertNull("Expected the passwordHash property to be null", passwordHashProp);
        hashIndicatorProp = (List<String>)updatedProps.get(ContentModel.PROP_HASH_INDICATOR);
        assertNull("Expected the hashIndicator property to be null", hashIndicatorProp);
        
        // authenticate the user
        authenticationComponent.clearCurrentSecurityContext();
        pubAuthenticationService.authenticate("Andy", "auth1".toCharArray());
        assertEquals("Andy", authenticationService.getCurrentUserName());
        
        // commit the transaction to invoke the password hashing of the user
        userTransaction.commit();
        
        // start another transaction and change to system user
        userTransaction = transactionService.getUserTransaction();
        userTransaction.begin();
        authenticationComponent.setSystemUserAsCurrentUser();
        
        // verify that the new properties are populated and the old ones are cleaned up
        Map<QName, Serializable> upgradedProps = nodeService.getProperties(andyUserNodeRef);
        passwordProp = (String)upgradedProps.get(ContentModel.PROP_PASSWORD);
        assertNull("Expected the password property to be null", passwordProp);
        password2Prop = (String)upgradedProps.get(ContentModel.PROP_PASSWORD_SHA256);
        assertNull("Expected the password2 property to be null", password2Prop);
        passwordHashProp = (String)upgradedProps.get(ContentModel.PROP_PASSWORD_HASH);
        assertNotNull("Expected the passwordHash property to be populated", passwordHashProp);
        hashIndicatorProp = (List<String>)upgradedProps.get(ContentModel.PROP_HASH_INDICATOR);
        assertNotNull("Expected the hashIndicator property to be populated", hashIndicatorProp);
        assertTrue("Expected there to be a single hash indicator entry", (hashIndicatorProp.size() == 1));
        String preferredEncoding = compositePasswordEncoder.getPreferredEncoding();
        String hashEncoding = (String)hashIndicatorProp.get(0);
        assertEquals("Expected hash indicator to be '" + preferredEncoding + "' but it was: " + hashEncoding, 
                    preferredEncoding, hashEncoding);
        
        // delete the user and clear the security context
        this.deleteAndy();
        authenticationComponent.clearCurrentSecurityContext();
    }

    /**
     * For on premise the default is MD4, for cloud BCRYPT10
     *
     * @throws Exception
     */
    public void testDefaultEncodingIsMD4() throws Exception
    {
        assertNotNull(compositePasswordEncoder);
        assertEquals("md4", compositePasswordEncoder.getPreferredEncoding());
    }

    /**
     * For on premise the default is MD4, get it
     *
     * @throws Exception
     */
    public void testGetsMD4Password() throws Exception
    {
        String user = "mduzer";
        String rawPass = "roarPazzw0rd";
        assertEquals("md4", compositePasswordEncoder.getPreferredEncoding());
        dao.createUser(user, null, rawPass.toCharArray());
        NodeRef userNodeRef = getRepositoryAuthenticationDao().getUserOrNull(user);
        assertNotNull(userNodeRef);
        String pass = dao.getMD4HashedPassword(user);
        assertNotNull(pass);
        assertTrue(compositePasswordEncoder.matches("md4", rawPass, pass, null));

        Map<QName, Serializable> properties = nodeService.getProperties(userNodeRef);
        properties.remove(ContentModel.PROP_PASSWORD_HASH);
        properties.remove(ContentModel.PROP_HASH_INDICATOR);
        properties.remove(ContentModel.PROP_PASSWORD);
        properties.remove(ContentModel.PROP_PASSWORD_SHA256);
        String encoded =  compositePasswordEncoder.encode("md4",new String(rawPass), null);
        properties.put(ContentModel.PROP_PASSWORD, encoded);
        nodeService.setProperties(userNodeRef, properties);
        pass = dao.getMD4HashedPassword(user);
        assertNotNull(pass);
        assertEquals(encoded, pass);
        dao.deleteUser(user);
    }

    /**
     * Tests creating a user with a Hashed password
     */
    public void testCreateUserWithHashedPassword() throws Exception
    {
        String SOME_PASSWORD = "1 passw0rd";
        String defaultencoding = compositePasswordEncoder.getPreferredEncoding();
        String user1 = "uzer"+GUID.generate();
        String user2 = "uzer"+GUID.generate();
        List<String> encs = Arrays.asList("bcrypt10", "md4");

        final String myTestDomain = TEST_TENANT_DOMAIN+"my.test";

        TenantUtil.runAsSystemTenant(new TenantUtil.TenantRunAsWork<Object>() {
            public Object doWork() throws Exception {
                if (!tenantAdminService.existsTenant(myTestDomain)) {
                    tenantAdminService.createTenant(myTestDomain, TENANT_ADMIN_PW.toCharArray(), null);
                }
                return null;
            }
        }, TenantService.DEFAULT_DOMAIN);

        for (String enc : encs)
        {
            compositePasswordEncoder.setPreferredEncoding(enc);
            String hash = compositePasswordEncoder.encodePreferred(SOME_PASSWORD,null);
            assertCreateHashed(SOME_PASSWORD, hash, null, user1+ TenantService.SEPARATOR + myTestDomain);
            assertCreateHashed(SOME_PASSWORD, null, SOME_PASSWORD.toCharArray(), user2+ TenantService.SEPARATOR + myTestDomain);
        }
        compositePasswordEncoder.setPreferredEncoding(defaultencoding);
    }

    private void assertCreateHashed(String rawString, String hash, char[] rawPassword, String user)
    {
        dao.createUser(user, hash, rawPassword);
        UserDetails userDetails = (UserDetails) dao.loadUserByUsername(user);
        assertNotNull(userDetails);
        assertNotNull(userDetails.getPassword());
        assertTrue(compositePasswordEncoder.matches(compositePasswordEncoder.getPreferredEncoding(), rawString, userDetails.getPassword(), null));
        dao.deleteUser(user);
    }

    private RepositoryAuthenticationDao getRepositoryAuthenticationDao()
    {
        RepositoryAuthenticationDao dao = new RepositoryAuthenticationDao();
        dao.setTransactionService(transactionService);
        dao.setAuthorityService(authorityService);
        dao.setTenantService(tenantService);
        dao.setNodeService(nodeService);
        dao.setNamespaceService(getNamespacePrefixReolsver(""));
        dao.setCompositePasswordEncoder(compositePasswordEncoder);
        dao.setPolicyComponent(policyComponent);
        dao.setAuthenticationCache(authenticationCache);
        dao.setSingletonCache(immutableSingletonCache);
        return dao;
    }

    private String getUserName(Authentication authentication)
    {
        String username = authentication.getPrincipal().toString();

        if (authentication.getPrincipal() instanceof UserDetails)
        {
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return username;
    }

    private NamespacePrefixResolver getNamespacePrefixReolsver(String defaultURI)
    {
        DynamicNamespacePrefixResolver nspr = new DynamicNamespacePrefixResolver(null);
        nspr.registerNamespace(NamespaceService.SYSTEM_MODEL_PREFIX, NamespaceService.SYSTEM_MODEL_1_0_URI);
        nspr.registerNamespace(NamespaceService.CONTENT_MODEL_PREFIX, NamespaceService.CONTENT_MODEL_1_0_URI);
        nspr.registerNamespace(ContentModel.USER_MODEL_PREFIX, ContentModel.USER_MODEL_URI);
        nspr.registerNamespace("namespace", "namespace");
        nspr.registerNamespace(NamespaceService.DEFAULT_PREFIX, defaultURI);
        return nspr;
    }

    public void testCreatingUserWithEmptyPassword() throws Exception
    {
        String previousAuthenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
        String userName = GUID.generate();
        String rawPass = "";
        try
        {
            dao.createUser(userName, null, rawPass.toCharArray());
            NodeRef userNodeRed = getRepositoryAuthenticationDao().getUserOrNull(userName);
            assertNotNull(userNodeRed);

            Map<QName, Serializable> properties = nodeService.getProperties(userNodeRed);
            assertEquals(properties.get(ContentModel.PROP_ENABLED), false);

            properties.remove(ContentModel.PROP_ENABLED);
            properties.put(ContentModel.PROP_ENABLED, true);
            nodeService.setProperties(userNodeRed, properties);
            assertEquals(properties.get(ContentModel.PROP_ENABLED), true);

            try
            {
                authenticationService.authenticate(userName, rawPass.toCharArray());
                fail("Authentication should have been rejected");
            }
            catch (IllegalArgumentException e)
            {
                assertEquals(e.getMessage(), "rawPassword is a mandatory parameter");
            }

            rawPass = "newPassword";
            dao.updateUser(userName, rawPass.toCharArray());
            try
            {
                authenticationService.authenticate(userName, rawPass.toCharArray());
            }
            catch (AuthenticationException e)
            {
                fail("Authentication should have passed.");
            }
            assertEquals(authenticationService.getCurrentUserName(), userName);
        }
        finally
        {
            if (previousAuthenticatedUser != null)
            {
                AuthenticationUtil.setFullyAuthenticatedUser(previousAuthenticatedUser);
            }
            try
            {
                dao.deleteUser(userName);
            }
            catch (Exception e)
            {
                // Nothing to do here.
            }
        }
    }
}
