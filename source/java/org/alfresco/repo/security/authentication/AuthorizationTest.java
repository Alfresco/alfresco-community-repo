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

import junit.framework.TestCase;


/**
 * Test Authorization
 */
public class AuthorizationTest extends TestCase
{
    private static String USER = "user";
    private static String PASSWORD = "pass";
    
    public void testInvalidAuthorization()
    {
        try
        {
            new Authorization(null);
            fail();
        }
        catch(IllegalArgumentException e)
        {
        }
        try
        {
            new Authorization("username:password:invalid");
            fail();
        }
        catch(IllegalArgumentException e)
        {
        }
    }
    
    public void testAuthorization()
    {
        Authorization auth1 = new Authorization(USER, PASSWORD);
        assertUserPass(USER, PASSWORD, auth1);
        Authorization auth2 = new Authorization("", PASSWORD);
        assertTicket("", PASSWORD, auth2);
        Authorization auth3 = new Authorization(null, PASSWORD);
        assertTicket(null, PASSWORD, auth3);
        Authorization auth4 = new Authorization(Authorization.TICKET_USERID, PASSWORD);
        assertTicket(Authorization.TICKET_USERID, PASSWORD, auth4);
        Authorization auth5 = new Authorization(Authorization.TICKET_USERID.toLowerCase(), PASSWORD);
        assertTicket(Authorization.TICKET_USERID.toLowerCase(), PASSWORD, auth5);
    }

    public void testUserPass()
    {
        Authorization auth1 = new Authorization(USER + ":" + PASSWORD);
        assertUserPass(USER, PASSWORD, auth1);
        Authorization auth2 = new Authorization(":" + PASSWORD);
        assertTicket("", PASSWORD, auth2);
        Authorization auth3 = new Authorization(PASSWORD);
        assertTicket(null, PASSWORD, auth3);
        Authorization auth4 = new Authorization(Authorization.TICKET_USERID + ":" + PASSWORD);
        assertTicket(Authorization.TICKET_USERID, PASSWORD, auth4);
        Authorization auth5 = new Authorization(Authorization.TICKET_USERID.toLowerCase() + ":" + PASSWORD);
        assertTicket(Authorization.TICKET_USERID.toLowerCase(), PASSWORD, auth5);
    }

    private void assertUserPass(String user, String pass, Authorization auth)
    {
        assertEquals(user, auth.getUserName());
        assertEquals(pass, auth.getPassword());
        assertFalse(auth.isTicket());
        assertNull(auth.getTicket());
    }

    private void assertTicket(String user, String pass, Authorization auth)
    {
        assertEquals(user, auth.getUserName());
        assertEquals(pass, auth.getPassword());
        assertTrue(auth.isTicket());
        assertEquals(pass, auth.getTicket());
    }

}
