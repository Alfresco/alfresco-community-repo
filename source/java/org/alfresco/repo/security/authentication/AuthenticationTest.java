/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.security.authentication;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authentication.InMemoryTicketComponentImpl.ExpiryMode;
import org.alfresco.repo.security.authentication.InMemoryTicketComponentImpl.Ticket;
import org.alfresco.repo.security.person.UserNameMatcher;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.springframework.context.ApplicationContext;

@SuppressWarnings("unchecked")
public class AuthenticationTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private NodeService nodeService;

    private TenantService tenantService;

    private NodeRef rootNodeRef;

    private NodeRef systemNodeRef;

    private NodeRef typesNodeRef;

    private NodeRef personAndyNodeRef;

    private MD4PasswordEncoder passwordEncoder;

    private MutableAuthenticationDao dao;

    private AuthenticationManager authenticationManager;

    private TicketComponent ticketComponent;

    private SimpleCache<String, Ticket> ticketsCache;

    private MutableAuthenticationService authenticationService;

    private MutableAuthenticationService pubAuthenticationService;

    private AuthenticationComponent authenticationComponent;

    private UserTransaction userTransaction;

    private AuthenticationComponent authenticationComponentImpl;

    private TransactionService transactionService;

    private PersonService pubPersonService;

    private PersonService personService;

    private UserNameMatcher userNameMatcher;
    
    // TODO: pending replacement
    private Dialect dialect;

    private PolicyComponent policyComponent;

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
        dialect = (Dialect) ctx.getBean("dialect");
        
        nodeService = (NodeService) ctx.getBean("nodeService");
        tenantService = (TenantService) ctx.getBean("tenantService");
        passwordEncoder = (MD4PasswordEncoder) ctx.getBean("passwordEncoder");
        ticketComponent = (TicketComponent) ctx.getBean("ticketComponent");
        authenticationService = (MutableAuthenticationService) ctx.getBean("authenticationService");
        pubAuthenticationService = (MutableAuthenticationService) ctx.getBean("AuthenticationService");
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        authenticationComponentImpl = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        pubPersonService =  (PersonService) ctx.getBean("PersonService");
        personService =  (PersonService) ctx.getBean("personService");
        userNameMatcher = (UserNameMatcher) ctx.getBean("userNameMatcher");
        policyComponent = (PolicyComponent) ctx.getBean("policyComponent");
        // permissionServiceSPI = (PermissionServiceSPI)
        // ctx.getBean("permissionService");
        ticketsCache = (SimpleCache<String, Ticket>) ctx.getBean("ticketsCache");

        dao = (MutableAuthenticationDao) ctx.getBean("authenticationDao");
        
        // Let's look inside the alfresco authentication subsystem to get the DAO-wired authentication manager
        ChildApplicationContextManager authenticationChain = (ChildApplicationContextManager) ctx.getBean("Authentication");
        ApplicationContext subsystem = authenticationChain.getApplicationContext(authenticationChain.getInstanceIds().iterator().next());
        authenticationManager = (AuthenticationManager) subsystem.getBean("authenticationManager");

        transactionService = (TransactionService) ctx.getBean(ServiceRegistry.TRANSACTION_SERVICE.getLocalName());
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

        deleteAndy();
        authenticationComponent.clearCurrentSecurityContext();
    }

    private void deleteAndy()
    {
        RepositoryAuthenticationDao dao = new RepositoryAuthenticationDao();
        dao.setTenantService(tenantService);
        dao.setNodeService(nodeService);
        dao.setNamespaceService(getNamespacePrefixReolsver(""));
        dao.setPasswordEncoder(passwordEncoder);
        dao.setUserNameMatcher(userNameMatcher);
        dao.setPolicyComponent(policyComponent);

        if (dao.getUserOrNull("andy") != null)
        {
            dao.deleteUser("andy");
        }
        
        if(personService.personExists("andy"))
        {
            personService.deletePerson("andy");
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
        properties.put(ContentModel.PROP_USERNAME, "Andy");
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
        
        AuthenticationUtil.runAs(new RunAsWork() {

            public Object doWork() throws Exception
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

    public void c()
    {
        try
        {
            authenticationService.authenticate("", "".toCharArray());
        }
        catch (AuthenticationException e)
        {
            // Expected
        }
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
        assertFalse(ticket1.equals(pubAuthenticationService.getCurrentTicket()));
        
    }
    
    public void testGuest()
    {
        authenticationService.authenticate(AuthenticationUtil.getGuestUserName(), "".toCharArray());
    }

    public void testCreateUsers()
    {
        authenticationService.createAuthentication(AuthenticationUtil.getGuestUserName(), "".toCharArray());
        authenticationService.authenticate(AuthenticationUtil.getGuestUserName(), "".toCharArray());
        // Guest is treated like any other user
        assertEquals(AuthenticationUtil.getGuestUserName(), authenticationService.getCurrentUserName());

        authenticationService.createAuthentication("Andy", "".toCharArray());
        authenticationService.authenticate("Andy", "".toCharArray());
        assertEquals("Andy", authenticationService.getCurrentUserName());

        if (! tenantService.isEnabled())
        {
            authenticationService.createAuthentication("Mr.Woof.Banana@chocolate.chip.cookie.com", "".toCharArray());
            authenticationService.authenticate("Mr.Woof.Banana@chocolate.chip.cookie.com", "".toCharArray());
            assertEquals("Mr.Woof.Banana@chocolate.chip.cookie.com", authenticationService.getCurrentUserName());
        }
        else
        {
            // TODO - could create tenant domain 'chocolate.chip.cookie.com'
        }

        authenticationService.createAuthentication("Andy_Woof/Domain", "".toCharArray());
        authenticationService.authenticate("Andy_Woof/Domain", "".toCharArray());
        assertEquals("Andy_Woof/Domain", authenticationService.getCurrentUserName());

        authenticationService.createAuthentication("Andy_ Woof/Domain", "".toCharArray());
        authenticationService.authenticate("Andy_ Woof/Domain", "".toCharArray());
        assertEquals("Andy_ Woof/Domain", authenticationService.getCurrentUserName());

        if (! tenantService.isEnabled())
        {
            String un = "Andy `\u00ac\u00a6!\u00a3$%^&*()-_=+\t\n\u0000[]{};'#:@~,./<>?|";
            if (dialect instanceof PostgreSQLDialect)
            {
                // Note: PostgreSQL does not support \u0000 char embedded in a string
                // http://archives.postgresql.org/pgsql-jdbc/2007-02/msg00115.php
                un = "Andy `\u00ac\u00a6!\u00a3$%^&*()-_=+\t\n[]{};'#:@~,./<>?|";
            }
            
            authenticationService.createAuthentication(un, "".toCharArray());
            authenticationService.authenticate(un, "".toCharArray());
            assertEquals(un, authenticationService.getCurrentUserName());
        }
        else
        {
            // tenant domain ~,./<>?\\| is not valid format"
        }
    }

    public void testCreateAndyUserAndOtherCRUD() throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        RepositoryAuthenticationDao dao = new RepositoryAuthenticationDao();
        dao.setTenantService(tenantService);
        dao.setNodeService(nodeService);
        dao.setNamespaceService(getNamespacePrefixReolsver(""));
        dao.setPasswordEncoder(passwordEncoder);
        dao.setUserNameMatcher(userNameMatcher);
        dao.setPolicyComponent(policyComponent);
        dao.createUser("Andy", "cabbage".toCharArray());
        assertNotNull(dao.getUserOrNull("Andy"));

        byte[] decodedHash = passwordEncoder.decodeHash(dao.getMD4HashedPassword("Andy"));
        byte[] testHash = MessageDigest.getInstance("MD4").digest("cabbage".getBytes("UnicodeLittleUnmarked"));
        assertEquals(new String(decodedHash), new String(testHash));

        UserDetails AndyDetails = (UserDetails) dao.loadUserByUsername("Andy");
        assertNotNull(AndyDetails);
        assertEquals("Andy", AndyDetails.getUsername());
        // assertNotNull(dao.getSalt(AndyDetails));
        assertTrue(AndyDetails.isAccountNonExpired());
        assertTrue(AndyDetails.isAccountNonLocked());
        assertTrue(AndyDetails.isCredentialsNonExpired());
        assertTrue(AndyDetails.isEnabled());
        assertNotSame("cabbage", AndyDetails.getPassword());
        assertEquals(AndyDetails.getPassword(), passwordEncoder.encodePassword("cabbage", dao.getSalt(AndyDetails)));
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
        // assertNotSame(oldSalt, dao.getSalt(newDetails));

        dao.deleteUser("Andy");
        assertNull(dao.getUserOrNull("Andy"));

        MessageDigest digester;
        try
        {
            digester = MessageDigest.getInstance("MD4");
            System.out.println("Digester from " + digester.getProvider());
        }
        catch (NoSuchAlgorithmException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("No digester");
        }

    }

    public void testAuthentication()
    {
        dao.createUser("GUEST", "".toCharArray());

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("GUEST", "");
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
        InMemoryTicketComponentImpl tc = new InMemoryTicketComponentImpl();
        tc.setOneOff(false);
        tc.setTicketsExpire(true);
        tc.setValidDuration("P5S");
        tc.setTicketsCache(ticketsCache);
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
        InMemoryTicketComponentImpl tc = new InMemoryTicketComponentImpl();
        tc.setOneOff(false);
        tc.setTicketsExpire(true);
        tc.setValidDuration("P5S");
        tc.setTicketsCache(ticketsCache);

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
        authenticationService.createAuthentication("GUEST", "".toCharArray());
        authenticationService.authenticate("GUEST", "".toCharArray());

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

        assertFalse(ticket1.equals(ticket2));
    }

    public void testAuthenticationService1()
    {
        authenticationService.createAuthentication("GUEST", "".toCharArray());
        authenticationService.authenticate("GUEST", "".toCharArray());

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
        authenticationService.createAuthentication("GUEST", "".toCharArray());
        authenticationService.authenticate("GUEST", "".toCharArray());

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
        authenticationService.createAuthentication("GUEST", "".toCharArray());
        authenticationService.authenticate("GUEST", "".toCharArray());

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
        authenticationService.createAuthentication("GUEST", "".toCharArray());
        authenticationService.authenticate("GUEST", "".toCharArray());

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
        authenticationService.createAuthentication("GUEST", "".toCharArray());
        authenticationService.authenticate("GUEST", "".toCharArray());

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
        authenticationService.createAuthentication("GUEST", "".toCharArray());
        authenticationService.authenticate("GUEST", "".toCharArray());

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
        pubAuthenticationService.createAuthentication("GUEST", "".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();

        pubAuthenticationService.authenticate("GUEST", "".toCharArray());

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
        pubAuthenticationService.createAuthentication("GUEST", "".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();

        pubAuthenticationService.authenticate("GUEST", "".toCharArray());

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
        pubAuthenticationService.createAuthentication("GUEST", "".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();

        pubAuthenticationService.authenticate("GUEST", "".toCharArray());

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

    public void testPubAuthenticationService()
    {
        // pubAuthenticationService.authenticateAsGuest();
        // authenticationComponent.clearCurrentSecurityContext();

        assertNull(authenticationComponent.getCurrentAuthentication());
        authenticationComponent.setSystemUserAsCurrentUser();
        pubAuthenticationService.createAuthentication("GUEST", "".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();

        assertNull(authenticationComponent.getCurrentAuthentication());
        pubAuthenticationService.authenticate("GUEST", "".toCharArray());
        pubAuthenticationService.authenticate("GUEST", "".toCharArray());
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
        pubAuthenticationService.createAuthentication("GUEST", "".toCharArray());
        authenticationComponent.clearCurrentSecurityContext();

        assertNull(authenticationComponent.getCurrentAuthentication());
        pubAuthenticationService.authenticate("GUEST", "".toCharArray());
        pubAuthenticationService.authenticate("GUEST", "".toCharArray());
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
}
