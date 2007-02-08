/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.jcr.session;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.alfresco.jcr.test.BaseJCRTest;


/**
 * Test JCR Session
 * 
 * @author David Caruana
 */
public class SessionImplTest extends BaseJCRTest
{
    protected Session superuserSession;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        SimpleCredentials superuser = new SimpleCredentials("superuser", "".toCharArray());
        superuser.setAttribute("attr1", "superuserValue");
        superuser.setAttribute("attr2", new Integer(1));
        superuserSession = repository.login(superuser, getWorkspace());
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        superuserSession.logout();
        super.tearDown();
    }
    
    public void testRepository()
        throws RepositoryException
    {
        Repository sessionRepository = superuserSession.getRepository();
        assertNotNull(sessionRepository);
        assertEquals(repository, sessionRepository);
    }

    public void testUserId()
    {
        {
            String userId = superuserSession.getUserID();
            assertNotNull(userId);
            assertEquals("superuser", userId);
        }
    }

    public void testAttributes()
    {
        {
            String[] names = superuserSession.getAttributeNames();
            assertNotNull(names);
            assertEquals(2, names.length);
            String value1 = (String)superuserSession.getAttribute("attr1");
            assertNotNull(value1);
            assertEquals("superuserValue", value1);
            Integer value2 = (Integer)superuserSession.getAttribute("attr2");
            assertNotNull(value2);
            assertEquals(new Integer(1), value2);
            String value3 = (String)superuserSession.getAttribute("unknown");
            assertNull(value3);
        }
    }
    
    public void testLogout()
    {
        boolean isLive = superuserSession.isLive();
        assertTrue(isLive);
        superuserSession.logout();
        isLive = superuserSession.isLive();
        assertFalse(isLive);
    }

    
    public void testSessionThread()
    {
        SimpleCredentials superuser = new SimpleCredentials("superuser", "".toCharArray());
        try
        {
            Session anotherSession = repository.login(superuser, getWorkspace());
            fail("Exception not thrown when establishing two sessions on same thread");
        }
        catch(RepositoryException e)
        {
            // successful - multiple sessions on one thread caught
        }
        superuserSession.logout();
        try
        {
            Session anotherSession = repository.login(superuser, getWorkspace());
            anotherSession.logout();
        }
        catch(RepositoryException e)
        {
            fail("Exception thrown when it shouldn't of been.");
        }
    }
    
}
