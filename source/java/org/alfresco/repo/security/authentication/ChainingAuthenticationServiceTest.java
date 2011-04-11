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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.security.AuthenticationService;

public class ChainingAuthenticationServiceTest extends TestCase
{
    private static final String EMPTY = "Empty";

    private static final String FIVE_AND_MORE = "FiveAndMore";

    private static final String FIVE = "Five";

    private static final String LONELY_DISABLE = "LonelyDisable";

    private static final String LONELY_ENABLED = "LonelyEnabled";

    private static final String ALFRESCO = "Alfresco";

    TestAuthenticationServiceImpl service1;

    TestAuthenticationServiceImpl service2;

    TestAuthenticationServiceImpl service3;

    TestAuthenticationServiceImpl service4;

    TestAuthenticationServiceImpl service5;

    private TestAuthenticationServiceImpl service6;

    public ChainingAuthenticationServiceTest()
    {
        super();
    }

    public ChainingAuthenticationServiceTest(String arg0)
    {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception
    {
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            throw new AlfrescoRuntimeException(
                    "A previous tests did not clean up transaction: " +
                    AlfrescoTransactionSupport.getTransactionId());
        }
        
        AuthenticationUtil authUtil = new AuthenticationUtil();
        authUtil.setDefaultAdminUserName("admin");
        authUtil.setDefaultGuestUserName("guest");
        authUtil.afterPropertiesSet();
        
        service1 = new TestAuthenticationServiceImpl(ALFRESCO, true, true, true, false);
        service1.createAuthentication("andy", "andy".toCharArray());

        HashMap<String, String> up = new HashMap<String, String>();
        HashSet<String> disabled = new HashSet<String>();
        up.put("lone", "lone");
        service2 = new TestAuthenticationServiceImpl(LONELY_ENABLED, false, false, false, true, up, disabled);

        up.clear();
        disabled.clear();

        up.put("ranger", "ranger");
        disabled.add("ranger");

        service3 = new TestAuthenticationServiceImpl(LONELY_DISABLE, false, false, false, false, up, disabled);

        service4 = new TestAuthenticationServiceImpl(EMPTY, true, true, true, false);

        up.clear();
        disabled.clear();

        up.put("A", "A");
        up.put("B", "B");
        up.put("C", "C");
        up.put("D", "D");
        up.put("E", "E");
        service5 = new TestAuthenticationServiceImpl(FIVE, false, false, false, false, up, disabled);

        up.clear();
        disabled.clear();

        up.put("A", "a");
        up.put("B", "b");
        up.put("C", "c");
        up.put("D", "d");
        up.put("E", "e");
        up.put("F", "f");
        up.put("G", "g");
        up.put("H", "h");
        up.put("I", "i");
        up.put("J", "j");
        up.put("K", "k");
        service6 = new TestAuthenticationServiceImpl(FIVE_AND_MORE, false, false, false, false, up, disabled);
    }

    //
    // Single service test
    //

    public void testServiceOne_Auth()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        ArrayList<AuthenticationService> ases = new ArrayList<AuthenticationService>();
        ases.add(service1);
        as.setAuthenticationServices(ases);
        as.authenticate("andy", "andy".toCharArray());
        assertEquals(as.getCurrentUserName(), "andy");
    }

    public void testServiceOne_AuthFail()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        ArrayList<AuthenticationService> ases = new ArrayList<AuthenticationService>();
        ases.add(service1);
        as.setAuthenticationServices(ases);
        try
        {
            as.authenticate("andy", "woof".toCharArray());
            fail();
        }
        catch (AuthenticationException e)
        {

        }
    }

    public void testServiceOne_GuestDenied()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        ArrayList<AuthenticationService> ases = new ArrayList<AuthenticationService>();
        ases.add(service1);
        as.setAuthenticationServices(ases);
        try
        {
            as.authenticateAsGuest();
            fail();
        }
        catch (AuthenticationException e)
        {

        }

    }

    public void testServiceTwo_GuestAllowed()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        ArrayList<AuthenticationService> ases = new ArrayList<AuthenticationService>();
        ases.add(service2);
        as.setAuthenticationServices(ases);
        as.authenticateAsGuest();
        assertEquals(as.getCurrentUserName(), AuthenticationUtil.getGuestUserName());
        as.clearCurrentSecurityContext();
        assertNull(as.getCurrentUserName());
    }

    public void testServiceOne_CRUD_Fails()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        ArrayList<AuthenticationService> ases = new ArrayList<AuthenticationService>();
        ases.add(service1);
        as.setAuthenticationServices(ases);
        try
        {
            as.authenticate("bob", "bob".toCharArray());
            fail();
        }
        catch (AuthenticationException e)
        {

        }
        try
        {
            as.createAuthentication("bob", "bob".toCharArray());
            fail();
        }
        catch (AuthenticationException e)
        {

        }
    }

    public void testServiceOne_CRUD()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        as.setMutableAuthenticationService(service1);
        try
        {
            as.authenticate("bob", "bob".toCharArray());
            fail();
        }
        catch (AuthenticationException e)
        {

        }
        as.createAuthentication("bob", "bob".toCharArray());
        as.authenticate("bob", "bob".toCharArray());
        as.updateAuthentication("bob", "bob".toCharArray(), "carol".toCharArray());
        try
        {
            as.authenticate("bob", "bob".toCharArray());
            fail();
        }
        catch (AuthenticationException e)
        {

        }
        as.authenticate("bob", "carol".toCharArray());
        as.deleteAuthentication("bob");
        try
        {
            as.authenticate("bob", "carol".toCharArray());
            fail();
        }
        catch (AuthenticationException e)
        {

        }
    }

    public void testServiceOne_Enabled()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        as.setMutableAuthenticationService(service1);

        assertTrue(as.getAuthenticationEnabled("andy"));

        as.setAuthenticationEnabled("andy", false);
        assertFalse(as.getAuthenticationEnabled("andy"));

        as.setAuthenticationEnabled("andy", true);
        assertTrue(as.getAuthenticationEnabled("andy"));
        as.authenticate("andy", "andy".toCharArray());

        as.setAuthenticationEnabled("andy", false);
        assertFalse(as.getAuthenticationEnabled("andy"));

        try
        {
            as.authenticate("andy", "andy".toCharArray());
            fail();
        }
        catch (AuthenticationException e)
        {

        }
    }

    public void testServiceOneDomains()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        as.setMutableAuthenticationService(service1);

        HashSet<String> testDomains = new HashSet<String>();
        testDomains.add(ALFRESCO);

        assertTrue(as.getDomains().equals(testDomains));
        assertTrue(as.getDomainsThatAllowUserCreation().equals(testDomains));
        assertTrue(as.getDomainsThatAllowUserDeletion().equals(testDomains));
        assertTrue(as.getDomiansThatAllowUserPasswordChanges().equals(testDomains));
        assertTrue(as.getDomains().equals(service1.getDomains()));
        assertTrue(as.getDomainsThatAllowUserCreation().equals(service1.getDomainsThatAllowUserCreation()));
        assertTrue(as.getDomainsThatAllowUserDeletion().equals(service1.getDomainsThatAllowUserDeletion()));
        assertTrue(as.getDomiansThatAllowUserPasswordChanges()
                .equals(service1.getDomiansThatAllowUserPasswordChanges()));

    }

    public void testServiceOneTickets()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        as.setMutableAuthenticationService(service1);
        as.authenticate("andy", "andy".toCharArray());

        String ticket = as.getCurrentTicket();
        assertTrue(ticket == as.getCurrentTicket());

        as.validate(ticket);
        as.invalidateTicket(ticket);
        try
        {
            as.validate(ticket);
            fail();
        }
        catch (AuthenticationException e)
        {

        }

        ticket = as.getCurrentTicket();
        as.validate(ticket);
        as.invalidateUserSession("andy");
        try
        {
            as.validate(ticket);
            fail();
        }
        catch (AuthenticationException e)
        {

        }
    }

    //
    // Multi service tests
    //

    public void testAll_Auth()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        ArrayList<AuthenticationService> ases = new ArrayList<AuthenticationService>();
        ases.add(service2);
        ases.add(service3);
        ases.add(service4);
        ases.add(service5);
        ases.add(service6);
        as.setAuthenticationServices(ases);
        as.setMutableAuthenticationService(service1);

        as.authenticate("andy", "andy".toCharArray());
        assertEquals(as.getCurrentUserName(), "andy");
        as.authenticate("lone", "lone".toCharArray());
        try
        {
            as.authenticate("ranger", "ranger".toCharArray());
            fail();
        }
        catch (AuthenticationException e)
        {

        }
        as.authenticate("A", "A".toCharArray());
        as.authenticate("B", "B".toCharArray());
        as.authenticate("C", "C".toCharArray());
        as.authenticate("D", "D".toCharArray());
        as.authenticate("E", "E".toCharArray());
        as.authenticate("A", "a".toCharArray());
        as.authenticate("B", "b".toCharArray());
        as.authenticate("C", "c".toCharArray());
        as.authenticate("D", "d".toCharArray());
        as.authenticate("E", "e".toCharArray());
        as.authenticate("F", "f".toCharArray());
        as.authenticate("G", "g".toCharArray());
        as.authenticate("H", "h".toCharArray());
        as.authenticate("I", "i".toCharArray());
        as.authenticate("J", "j".toCharArray());
        as.authenticate("K", "k".toCharArray());
    }

    public void testAll_AuthOverLapReversed()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        ArrayList<AuthenticationService> ases = new ArrayList<AuthenticationService>();
        ases.add(service2);
        ases.add(service3);
        ases.add(service4);
        ases.add(service6);
        ases.add(service5);
        as.setAuthenticationServices(ases);
        as.setMutableAuthenticationService(service1);

        as.authenticate("andy", "andy".toCharArray());
        assertEquals(as.getCurrentUserName(), "andy");
        as.authenticate("lone", "lone".toCharArray());
        try
        {
            as.authenticate("ranger", "ranger".toCharArray());
            fail();
        }
        catch (AuthenticationException e)
        {

        }

        try
        {
            as.authenticate("A", "B".toCharArray());
            fail();
        }
        catch (AuthenticationException e)
        {

        }
        as.authenticate("A", "A".toCharArray());
        as.authenticate("B", "B".toCharArray());
        as.authenticate("C", "C".toCharArray());
        as.authenticate("D", "D".toCharArray());
        as.authenticate("E", "E".toCharArray());
        as.authenticate("A", "a".toCharArray());
        as.authenticate("B", "b".toCharArray());
        as.authenticate("C", "c".toCharArray());
        as.authenticate("D", "d".toCharArray());
        as.authenticate("E", "e".toCharArray());
        as.authenticate("F", "f".toCharArray());
        as.authenticate("G", "g".toCharArray());
        as.authenticate("H", "h".toCharArray());
        as.authenticate("I", "i".toCharArray());
        as.authenticate("J", "j".toCharArray());
        as.authenticate("K", "k".toCharArray());
    }

    public void testAll_MutAuth()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        ArrayList<AuthenticationService> ases = new ArrayList<AuthenticationService>();
        ases.add(service2);
        ases.add(service3);
        ases.add(service4);
        ases.add(service5);
        ases.add(service6);
        as.setAuthenticationServices(ases);
        as.setMutableAuthenticationService(service1);

        as.authenticate("andy", "andy".toCharArray());
        assertEquals(as.getCurrentUserName(), "andy");
        as.authenticate("lone", "lone".toCharArray());
        try
        {
            as.authenticate("ranger", "ranger".toCharArray());
            fail();
        }
        catch (AuthenticationException e)
        {

        }
        as.authenticate("A", "A".toCharArray());
        as.authenticate("B", "B".toCharArray());
        as.authenticate("C", "C".toCharArray());
        as.authenticate("D", "D".toCharArray());
        as.authenticate("E", "E".toCharArray());
        as.authenticate("A", "a".toCharArray());
        as.authenticate("B", "b".toCharArray());
        as.authenticate("C", "c".toCharArray());
        as.authenticate("D", "d".toCharArray());
        as.authenticate("E", "e".toCharArray());
        as.authenticate("F", "f".toCharArray());
        as.authenticate("G", "g".toCharArray());
        as.authenticate("H", "h".toCharArray());
        as.authenticate("I", "i".toCharArray());
        as.authenticate("J", "j".toCharArray());
        as.authenticate("K", "k".toCharArray());

        as.createAuthentication("A", "woof".toCharArray());
        as.authenticate("A", "woof".toCharArray());
        as.updateAuthentication("A", "woof".toCharArray(), "bark".toCharArray());
        as.authenticate("A", "bark".toCharArray());
        as.setAuthentication("A", "tree".toCharArray());
        as.authenticate("A", "tree".toCharArray());
        as.deleteAuthentication("A");
        as.authenticate("A", "A".toCharArray());
        as.authenticate("A", "a".toCharArray());
        try
        {
            as.authenticate("A", "woof".toCharArray());
            fail();
        }
        catch (AuthenticationException e)
        {

        }
        try
        {
            as.authenticate("A", "bark".toCharArray());
            fail();
        }
        catch (AuthenticationException e)
        {

        }
        try
        {
            as.authenticate("A", "tree".toCharArray());
            fail();
        }
        catch (AuthenticationException e)
        {

        }
    }

    public void testAll_AuthEnabled()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        ArrayList<AuthenticationService> ases = new ArrayList<AuthenticationService>();
        ases.add(service2);
        ases.add(service3);
        ases.add(service4);
        ases.add(service5);
        ases.add(service6);
        as.setAuthenticationServices(ases);
        as.setMutableAuthenticationService(service1);

        assertTrue(as.getAuthenticationEnabled("andy"));
        assertTrue(as.getAuthenticationEnabled("lone"));
        assertFalse(as.getAuthenticationEnabled("ranger"));
        assertTrue(as.getAuthenticationEnabled("A"));
        assertTrue(as.getAuthenticationEnabled("B"));
        assertTrue(as.getAuthenticationEnabled("C"));
        assertTrue(as.getAuthenticationEnabled("D"));
        assertTrue(as.getAuthenticationEnabled("E"));
        assertTrue(as.getAuthenticationEnabled("F"));
        assertTrue(as.getAuthenticationEnabled("G"));
        assertTrue(as.getAuthenticationEnabled("H"));
        assertTrue(as.getAuthenticationEnabled("I"));
        assertTrue(as.getAuthenticationEnabled("J"));
        assertTrue(as.getAuthenticationEnabled("K"));

        as.setAuthenticationEnabled("andy", false);
        assertFalse(as.getAuthenticationEnabled("andy"));
        as.setAuthenticationEnabled("andy", true);
        assertTrue(as.getAuthenticationEnabled("andy"));
        as.setAuthenticationEnabled("andy", false);

        try
        {
            as.authenticate("andy", "andy".toCharArray());
            fail();
        }
        catch (AuthenticationException e)
        {

        }
    }

    public void testService_GuestDenied()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        ArrayList<AuthenticationService> ases = new ArrayList<AuthenticationService>();
        ases.add(service1);
        ases.add(service3);
        ases.add(service4);
        ases.add(service5);
        ases.add(service6);
        as.setAuthenticationServices(ases);
        try
        {
            as.authenticateAsGuest();
            fail();
        }
        catch (AuthenticationException e)
        {

        }

    }

    public void testService_GuestAllowed()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        ArrayList<AuthenticationService> ases = new ArrayList<AuthenticationService>();
        ases.add(service1);
        ases.add(service2);
        ases.add(service3);
        ases.add(service4);
        ases.add(service5);
        ases.add(service6);
        as.setAuthenticationServices(ases);
        as.authenticateAsGuest();
        assertEquals(as.getCurrentUserName(), AuthenticationUtil.getGuestUserName());
        as.clearCurrentSecurityContext();
        assertNull(as.getCurrentUserName());
    }
    
    public void testService_NoGuestConfigured() throws Exception
    {
        
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        ArrayList<AuthenticationService> ases = new ArrayList<AuthenticationService>();
        ases.add(service2);
        as.setAuthenticationServices(ases);
        
        assertNotNull(AuthenticationUtil.getGuestUserName());
        as.authenticateAsGuest();
        assertEquals(as.getCurrentUserName(), AuthenticationUtil.getGuestUserName());
        as.clearCurrentSecurityContext();
        assertNull(as.getCurrentUserName());
        
        AuthenticationUtil authUtil = new AuthenticationUtil();
        authUtil.setDefaultAdminUserName("admin");
        authUtil.setDefaultGuestUserName(null);
        authUtil.afterPropertiesSet();
        
        try
        {
            as.authenticateAsGuest();
            fail("Guest authentication should not be supported");
        }
        catch (AuthenticationException ae)
        {
            // expected
            assertTrue(ae.getMessage().contains("Guest authentication not supported"));
        }
        assertNull(as.getCurrentUserName());
    }
    
    public void testService_Domains()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        ArrayList<AuthenticationService> ases = new ArrayList<AuthenticationService>();
        ases.add(service2);
        ases.add(service3);
        ases.add(service4);
        ases.add(service5);
        ases.add(service6);
        as.setAuthenticationServices(ases);
        as.setMutableAuthenticationService(service1);

        HashSet<String> testDomains = new HashSet<String>();
        testDomains.add(ALFRESCO);
        testDomains.add(LONELY_ENABLED);
        testDomains.add(LONELY_DISABLE);
        testDomains.add(EMPTY);
        testDomains.add(FIVE);
        testDomains.add(FIVE_AND_MORE);

        HashSet<String> onlyAlfDomain = new HashSet<String>();
        onlyAlfDomain.add(ALFRESCO);

        assertTrue(as.getDomains().equals(testDomains));
        assertTrue(as.getDomainsThatAllowUserCreation().equals(onlyAlfDomain));
        assertTrue(as.getDomainsThatAllowUserDeletion().equals(onlyAlfDomain));
        assertTrue(as.getDomiansThatAllowUserPasswordChanges().equals(onlyAlfDomain));

    }

    public void testServiceTickets()
    {
        ChainingAuthenticationServiceImpl as = new ChainingAuthenticationServiceImpl();
        ArrayList<AuthenticationService> ases = new ArrayList<AuthenticationService>();
        ases.add(service2);
        ases.add(service3);
        ases.add(service4);
        ases.add(service5);
        ases.add(service6);
        as.setAuthenticationServices(ases);
        as.setMutableAuthenticationService(service1);

        as.authenticate("andy", "andy".toCharArray());

        String ticket = as.getCurrentTicket();
        assertTrue(ticket == as.getCurrentTicket());

        as.validate(ticket);
        as.invalidateTicket(ticket);
        try
        {
            as.validate(ticket);
            fail();
        }
        catch (AuthenticationException e)
        {

        }

        ticket = as.getCurrentTicket();
        as.validate(ticket);
        as.invalidateUserSession("andy");
        try
        {
            as.validate(ticket);
            fail();
        }
        catch (AuthenticationException e)
        {

        }
    }

}
