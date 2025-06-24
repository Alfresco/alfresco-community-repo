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

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.security.PersonService;

/**
 * Mocked test for {@link AuthenticationServiceImpl}
 *
 * @author amukha
 * @since 5.2
 */
public class AuthenticationServiceImplTest
{
    private AuthenticationComponent authenticationComponent = mock(AuthenticationComponent.class);
    private SimpleCache<String, ProtectedUser> cache;
    private TicketComponent ticketComponent = mock(TicketComponent.class);
    private AuthenticationServiceImpl authService;
    private AuthenticationServiceImpl authService2;
    private PersonService personService = mock(PersonService.class);

    private static final String USERNAME = "username";
    private static final String USERNAME_CASE_SENSITIVE = "uSeRnAmE";
    private static final char[] PASSWORD = "password".toCharArray();

    @Before
    public void beforeTest()
    {
        authService = new AuthenticationServiceImpl();
        authService.setAuthenticationComponent(authenticationComponent);
        authService.setTicketComponent(ticketComponent);
        cache = new MockCache<>();
        authService.setProtectedUsersCache(cache);
        authService.setPersonService(personService);

        authService2 = new AuthenticationServiceImpl();
        authService2.setAuthenticationComponent(authenticationComponent);
        authService2.setTicketComponent(ticketComponent);
        authService2.setProtectedUsersCache(cache);
    }

    @Test
    public void testProtectedUserBadPassword()
    {
        int limit = 3;
        int attempts = limit + 3;
        authService.setProtectionPeriodSeconds(99999);
        authService.setProtectionLimit(limit);
        authService.setProtectionEnabled(true);

        Exception spoofedAE = new AuthenticationException("Bad password");
        doThrow(spoofedAE).when(authenticationComponent).authenticate(USERNAME, PASSWORD);
        for (int i = 0; i < attempts; i++)
        {
            try
            {
                authService.authenticate(USERNAME, PASSWORD);
                fail("The " + AuthenticationException.class.getName() + " should have been thrown.");
            }
            catch (AuthenticationException ae)
            {
                // normal
                if (i < limit)
                {
                    assertTrue("Expected failure from AuthenticationComponent", ae == spoofedAE);
                }
                else
                {
                    assertFalse("Expected failure from protection code", ae == spoofedAE);
                }
            }
        }
        verify(authenticationComponent, times(limit)).authenticate(USERNAME, PASSWORD);
        assertTrue("The user should be protected.", authService.isUserProtected(USERNAME));

        final String protectedUserKey = authService.getProtectedUserKey(USERNAME);
        assertEquals("The number of recorded logins did not match.", attempts, cache.get(protectedUserKey).getNumLogins());

        // test that the protection is still in place even if the password is correct
        doNothing().when(authenticationComponent).authenticate(USERNAME, PASSWORD);
        try
        {
            authService.authenticate(USERNAME, PASSWORD);
            fail("The " + AuthenticationException.class.getName() + " should have been thrown.");
        }
        catch (AuthenticationException ae)
        {
            // normal
        }
        verify(authenticationComponent, times(limit)).authenticate(USERNAME, PASSWORD);
        assertEquals("The number of recorded logins did not match.", attempts + 1, cache.get(protectedUserKey).getNumLogins());
    }

    @Test
    public void testProtectedUserIfUserNamesAreNotCaseSensitive()
    {
        int limit = 3;
        int attempts = limit + 1;
        authService.setProtectionPeriodSeconds(999);
        authService.setProtectionLimit(limit);
        authService.setProtectionEnabled(true);

        Exception spoofedAE = new AuthenticationException("Bad password");
        doThrow(spoofedAE).when(authenticationComponent).authenticate(USERNAME_CASE_SENSITIVE, PASSWORD);
        for (int i = 0; i < attempts; i++)
        {
            try
            {
                authService.authenticate(USERNAME_CASE_SENSITIVE, PASSWORD);
                fail("The " + AuthenticationException.class.getName() + " should have been thrown.");
            }
            catch (AuthenticationException ae)
            {
                // normal
                if (i < limit)
                {
                    assertTrue("Expected failure from AuthenticationComponent", ae == spoofedAE);
                }
                else
                {
                    assertFalse("Expected failure from protection code", ae == spoofedAE);
                }
            }
        }
        verify(authenticationComponent, times(0)).authenticate(USERNAME, PASSWORD);
        assertTrue("The user should be protected.", authService.isUserProtected(USERNAME));
        verify(authenticationComponent, times(limit)).authenticate(USERNAME_CASE_SENSITIVE, PASSWORD);
        assertTrue("The user should be protected.", authService.isUserProtected(USERNAME_CASE_SENSITIVE));
    }

    @Test
    public void testProtectedUserIfUserNamesAreCaseSensitive()
    {
        int limit = 3;
        int attempts = limit + 1;
        authService.setProtectionPeriodSeconds(999);
        authService.setProtectionLimit(limit);
        authService.setProtectionEnabled(true);

        when(personService.getUserNamesAreCaseSensitive()).thenReturn(true);

        Exception spoofedAE = new AuthenticationException("Bad password");
        doThrow(spoofedAE).when(authenticationComponent).authenticate(USERNAME, PASSWORD);
        for (int i = 0; i < attempts; i++)
        {
            try
            {
                authService.authenticate(USERNAME, PASSWORD);
                fail("The " + AuthenticationException.class.getName() + " should have been thrown.");
            }
            catch (AuthenticationException ae)
            {
                // normal
                if (i < limit)
                {
                    assertTrue("Expected failure from AuthenticationComponent", ae == spoofedAE);
                }
                else
                {
                    assertFalse("Expected failure from protection code", ae == spoofedAE);
                }
            }
        }
        verify(authenticationComponent, times(limit)).authenticate(USERNAME, PASSWORD);
        assertTrue("The user should be protected.", authService.isUserProtected(USERNAME));
        verify(authenticationComponent, times(0)).authenticate(USERNAME_CASE_SENSITIVE, PASSWORD);
        assertFalse("The user should not be protected.", authService.isUserProtected(USERNAME_CASE_SENSITIVE));
    }

    @Test
    public void testProtectedUserCanLoginAfterProtection() throws Exception
    {
        int timeLimit = 1;
        int attempts = 2;
        authService.setProtectionPeriodSeconds(timeLimit);
        authService.setProtectionLimit(attempts);
        authService.setProtectionEnabled(true);

        doThrow(new AuthenticationException("Bad password"))
                .when(authenticationComponent).authenticate(USERNAME, PASSWORD);
        for (int i = 0; i < attempts; i++)
        {
            try
            {
                authService.authenticate(USERNAME, PASSWORD);
                fail("An " + AuthenticationException.class.getName() + " should be thrown.");
            }
            catch (AuthenticationException ae)
            {
                // normal
            }
        }
        assertTrue("The user should be protected.", authService.isUserProtected(USERNAME));

        final String protectedUserKey = authService.getProtectedUserKey(USERNAME);
        assertEquals("The number of recorded logins did not match.", attempts, cache.get(protectedUserKey).getNumLogins());
        Thread.sleep(timeLimit * 1000 + 1);
        assertFalse("The user should not be protected any more.", authService.isUserProtected(USERNAME));
        assertEquals("The number of recorded logins should stay the same after protection period ends.",
                attempts, cache.get(protectedUserKey).getNumLogins());

        doNothing().when(authenticationComponent).authenticate(USERNAME, PASSWORD);
        try
        {
            authService.authenticate(USERNAME, PASSWORD);
        }
        catch (AuthenticationException ae)
        {
            fail("An " + AuthenticationException.class.getName() + " should not be thrown.");
        }
        assertNull("The user should be removed from the cache after successful login.",
                cache.get(protectedUserKey));
    }

    @Test
    public void testAuthChainWorksIfFirstAuthFails() throws Exception
    {
        int timeLimit = 1;
        int attempts = 2;
        authService.setProtectionPeriodSeconds(timeLimit);
        authService.setProtectionLimit(attempts);
        authService.setProtectionEnabled(true);

        authService2.setProtectionPeriodSeconds(timeLimit);
        authService2.setProtectionLimit(attempts);
        authService2.setProtectionEnabled(true);

        AuthenticationServiceImpl[] authenticationChain = {authService, authService2};

        doThrow(new AuthenticationException("Bad password"))
                .when(authenticationComponent).authenticate(USERNAME, PASSWORD);

        // Authentication fails on first run.
        for (int i = 0; i < attempts; i++)
        {
            for (AuthenticationServiceImpl authentication : authenticationChain)
            {
                try
                {
                    authentication.authenticate(USERNAME, PASSWORD);
                    fail("An " + AuthenticationException.class.getName() + " should be thrown.");
                }
                catch (AuthenticationException ae)
                {
                    // normal
                }
            }
        }

        for (AuthenticationServiceImpl authentication : authenticationChain)
        {
            assertTrue("The user should be protected.", authentication.isUserProtected(USERNAME));
        }

        Thread.sleep(timeLimit * 1000 + 1);

        for (AuthenticationServiceImpl authentication : authenticationChain)
        {
            assertFalse("The user should not be protected any more.", authentication.isUserProtected(USERNAME));
        }

        // Authentication always fails on first authentication service in the chain.
        try
        {
            authenticationChain[0].authenticate(USERNAME, PASSWORD);
            fail("An " + AuthenticationException.class.getName() + " should be thrown.");
        }
        catch (AuthenticationException ae)
        {
            // normal
        }

        // Authentication should succeed on second authentication service in the chain.
        doNothing().when(authenticationComponent).authenticate(USERNAME, PASSWORD);
        try
        {
            authenticationChain[1].authenticate(USERNAME, PASSWORD);
        }
        catch (AuthenticationException ae)
        {
            fail("An " + AuthenticationException.class.getName() + " should not be thrown.");
        }

        assertNotNull("The user should not be removed from the cache for the corresponding authorization service after a failed login.",
                cache.get(authenticationChain[0].getProtectedUserKey(USERNAME)));
        assertNull("The user should be removed from the cache for the corresponding authorization service after successful login.",
                cache.get(authenticationChain[1].getProtectedUserKey(USERNAME)));
    }

    @Test
    public void testProtectionDisabledBadPassword()
    {
        int limit = 3;
        int attempts = limit + 2;
        authService.setProtectionPeriodSeconds(99999);
        authService.setProtectionLimit(limit);
        authService.setProtectionEnabled(false);

        Exception spoofedAE = new AuthenticationException("Bad password");
        doThrow(spoofedAE).when(authenticationComponent).authenticate(USERNAME, PASSWORD);
        for (int i = 0; i < attempts; i++)
        {
            try
            {
                authService.authenticate(USERNAME, PASSWORD);
                fail("The " + AuthenticationException.class.getName() + " should have been thrown.");
            }
            catch (AuthenticationException ae)
            {
                assertTrue("Expected failure from AuthenticationComponent", ae == spoofedAE);
            }
        }
        verify(authenticationComponent, times(attempts)).authenticate(USERNAME, PASSWORD);
        assertNull("The user should not be in the cache.", cache.get(USERNAME));
    }

    private class MockCache<K extends Serializable, V> implements SimpleCache<K, V>
    {
        private Map<K, V> internalCache;

        MockCache()
        {
            internalCache = new HashMap<>();
        }

        @Override
        public boolean contains(K key)
        {
            return internalCache.containsKey(key);
        }

        @Override
        public Collection<K> getKeys()
        {
            return internalCache.keySet();
        }

        @Override
        public V get(K key)
        {
            return internalCache.get(key);
        }

        @Override
        public void put(K key, V value)
        {
            internalCache.put(key, value);
        }

        @Override
        public void remove(K key)
        {
            internalCache.remove(key);
        }

        @Override
        public void clear()
        {
            internalCache.clear();
        }
    }
}
