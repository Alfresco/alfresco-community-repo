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
package org.alfresco.jcr.repository;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.alfresco.jcr.test.BaseJCRTest;


/**
 * Test JCR Repository Implementation
 * 
 * @author David Caruana
 */
public class RepositoryImplTest extends BaseJCRTest
{

    public void testDescriptors()
    {
        String[] keys = repository.getDescriptorKeys();
        assertEquals(11, keys.length);
        for (String key : keys)
        {
            String value = repository.getDescriptor(key);
            assertNotNull(value);
        }
        
        assertNotNull(repository.getDescriptor(Repository.REP_NAME_DESC));
        System.out.println(repository.getDescriptor(Repository.REP_NAME_DESC));
        assertNotNull(repository.getDescriptor(Repository.REP_VENDOR_DESC));
        assertNotNull(repository.getDescriptor(Repository.REP_VENDOR_URL_DESC));
        assertNotNull(repository.getDescriptor(Repository.REP_VERSION_DESC));
        System.out.println(repository.getDescriptor(Repository.REP_VERSION_DESC));
        assertNotNull(repository.getDescriptor(Repository.SPEC_NAME_DESC));
        assertNotNull(repository.getDescriptor(Repository.SPEC_VERSION_DESC));
        assertEquals("true",  repository.getDescriptor(Repository.LEVEL_1_SUPPORTED));
        assertEquals("true", repository.getDescriptor(Repository.LEVEL_2_SUPPORTED));
        assertEquals("true", repository.getDescriptor(Repository.OPTION_TRANSACTIONS_SUPPORTED));
        assertEquals("true", repository.getDescriptor(Repository.QUERY_XPATH_DOC_ORDER));
        assertEquals("true", repository.getDescriptor(Repository.QUERY_XPATH_POS_INDEX));
    }
    
    public void testBadUsernameLogin() throws Exception
    {
        SimpleCredentials badUser = new SimpleCredentials("baduser", "".toCharArray());
        try
        {
            repository.login(badUser);
            fail("Failed to catch bad username - username should not exist.");
        }
        catch (LoginException e)
        {
        }
    }
    
    public void testBadPwdLogin() throws Exception
    {
        SimpleCredentials badPwd = new SimpleCredentials("superuser", "badpwd".toCharArray());
        try
        {
            repository.login(badPwd);
            fail("Failed to catch bad password - password is invalid.");
        }
        catch (LoginException e)
        {
        }
    }
    
    public void testNoCredentialsLogin() throws Exception
    {
        try
        {
            repository.login();
            fail("Failed to catch no credentials.");
        }
        catch (LoginException e)
        {
        }
    }

    public void testLogin()
        throws RepositoryException
    {
        SimpleCredentials good = new SimpleCredentials("superuser", "".toCharArray());
        try
        {
            Session session = repository.login(good, getWorkspace());
            assertNotNull(session);
            session.logout();
        }
        catch (LoginException e)
        {
            fail("Failed to login.");
        }

        try
        {
            Session session = repository.login(good, null);
            session.logout();
        }
        catch (NoSuchWorkspaceException e)
        {
            fail("Failed to detect default workspace");
        }
    }
    
}

