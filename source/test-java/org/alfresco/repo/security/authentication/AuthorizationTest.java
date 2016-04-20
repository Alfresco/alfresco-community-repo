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
            new Authorization("");
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
