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
package org.alfresco.repo.security.authentication;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
import net.sf.acegisecurity.providers.dao.SaltSource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.authentication.InMemoryTicketComponentImpl.Ticket;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.simple.permission.AuthorityCapabilityRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

@SuppressWarnings("unchecked")
public class AuthenticationTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private NodeService nodeService;
    
    private TenantService tenantService;

    private SearchService searchService;

    private NodeRef rootNodeRef;

    private NodeRef systemNodeRef;

    private NodeRef typesNodeRef;

    private NodeRef personAndyNodeRef;

    private DictionaryService dictionaryService;

    private MD4PasswordEncoder passwordEncoder;

    private MutableAuthenticationDao dao;

    private AuthenticationManager authenticationManager;

    private SaltSource saltSource;

    private TicketComponent ticketComponent;
    
    private SimpleCache<String, Ticket> ticketsCache;

    private AuthenticationService authenticationService;

    private AuthenticationService pubAuthenticationService;

    private AuthenticationComponent authenticationComponent;

    private UserTransaction userTransaction;

    private AuthenticationComponent authenticationComponentImpl;

    private AuthorityCapabilityRegistry authorityCapabilityRegistry;
    
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

        nodeService = (NodeService) ctx.getBean("nodeService");
        tenantService = (TenantService) ctx.getBean("tenantService");
        searchService = (SearchService) ctx.getBean("searchService");
        dictionaryService = (DictionaryService) ctx.getBean("dictionaryService");
        passwordEncoder = (MD4PasswordEncoder) ctx.getBean("passwordEncoder");
        ticketComponent = (TicketComponent) ctx.getBean("ticketComponent");
        authenticationService = (AuthenticationService) ctx.getBean("authenticationService");
        pubAuthenticationService = (AuthenticationService) ctx.getBean("AuthenticationService");
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        authenticationComponentImpl = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        authorityCapabilityRegistry = (AuthorityCapabilityRegistry) ctx.getBean("authorityCapabilityRegistry");
        // permissionServiceSPI = (PermissionServiceSPI)
        // ctx.getBean("permissionService");
        ticketsCache = (SimpleCache<String, Ticket>) ctx.getBean("ticketsCache");

        dao = (MutableAuthenticationDao) ctx.getBean("authenticationDao");
        authenticationManager = (AuthenticationManager) ctx.getBean("authenticationManager");
        saltSource = (SaltSource) ctx.getBean("saltSource");

        TransactionService transactionService = (TransactionService) ctx.getBean(ServiceRegistry.TRANSACTION_SERVICE
                .getLocalName());
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
        personAndyNodeRef = nodeService.createNode(typesNodeRef, children, ContentModel.TYPE_PERSON, container, props)
                .getChildRef();
        assertNotNull(personAndyNodeRef);

        deleteAndy();
        authenticationComponent.clearCurrentSecurityContext();
    }

    private void deleteAndy()
    {
        RepositoryAuthenticationDao dao = new RepositoryAuthenticationDao();
        dao.setTenantService(tenantService);
        dao.setNodeService(nodeService);
        dao.setSearchService(searchService);
        dao.setDictionaryService(dictionaryService);
        dao.setNamespaceService(getNamespacePrefixReolsver(""));
        dao.setPasswordEncoder(passwordEncoder);

        if (dao.getUserOrNull("andy") != null)
        {
            dao.deleteUser("andy");
        }
    }

    @Override
    protected void tearDown() throws Exception
    {
        authenticationComponentImpl.clearCurrentSecurityContext();
        userTransaction.rollback();
        super.tearDown();
    }

    private Map<QName, Serializable> createPersonProperties(String userName)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, "Andy");
        return properties;
    }

    public void xtestScalability()
    {
        long create = 0;

        long start;
        long end;
        authenticationComponent.authenticate("admin", "admin".toCharArray());
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

    public void testGuest()
    {
        authenticationService.authenticate("GUEST", "".toCharArray());
    }
    
    public void testCreateUsers()
    {
        authenticationService.createAuthentication("GUEST", "".toCharArray());
        authenticationService.authenticate("GUEST", "".toCharArray());
        // Guest is reported as lower case and the authentication basically
        // ignored at the moment
        assertEquals("guest", authenticationService.getCurrentUserName());

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
            authenticationService.createAuthentication("Andy `\u00ac\u00a6!\u00a3$%^&*()-_=+\t\n\u0000[]{};'#:@~,./<>?\\|", "".toCharArray());
            authenticationService.authenticate("Andy `\u00ac\u00a6!\u00a3$%^&*()-_=+\t\n\u0000[]{};'#:@~,./<>?\\|", "".toCharArray());
            assertEquals("Andy `\u00ac\u00a6!\u00a3$%^&*()-_=+\t\n\u0000[]{};'#:@~,./<>?\\|", authenticationService.getCurrentUserName());
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
        dao.setSearchService(searchService);
        dao.setDictionaryService(dictionaryService);
        dao.setNamespaceService(getNamespacePrefixReolsver(""));
        dao.setPasswordEncoder(passwordEncoder);
        dao.setAuthorityCapabilityRegistry(authorityCapabilityRegistry);
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
        assertEquals(AndyDetails.getPassword(), passwordEncoder.encodePassword("cabbage", saltSource
                .getSalt(AndyDetails)));
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
        assertEquals(ticketComponent.getCurrentTicket("Andy"), ticket);

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
            assertEquals(ticketComponent.getCurrentTicket("Andy"), ticket);
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
        assertEquals(ticketComponent.getCurrentTicket("Andy"), ticket);
        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy"), ticket);

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
        assertTrue(!ticketComponent.getCurrentTicket("Andy").equals(ticket));
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
        assertEquals(ticketComponent.getCurrentTicket("Andy"), ticket);
        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy"), ticket);
        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy"), ticket);

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
        assertEquals(ticketComponent.getCurrentTicket("Andy"), ticket);
        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy"), ticket);
        tc.validateTicket(ticket);
        assertEquals(ticketComponent.getCurrentTicket("Andy"), ticket);
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
        assertEquals(ticketComponent.getCurrentTicket("Andy"), ticket);
        
        dao.deleteUser("Andy");
        // assertNull(dao.getUserOrNull("Andy"));

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
