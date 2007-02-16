/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
package org.alfresco.repo.security.authentication.ldap;

import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;

/**
 * Currently expects the cn name of the user which is in a fixed location.
 * 
 * @author Andy Hind
 */
public class LDAPAuthenticationComponentImpl extends AbstractAuthenticationComponent
{
    
    private String userNameFormat;
    
    private LDAPInitialDirContextFactory ldapInitialContextFactory;
    
    public LDAPAuthenticationComponentImpl()
    {
        super();
    }

    
    public void setLDAPInitialDirContextFactory(LDAPInitialDirContextFactory ldapInitialDirContextFactory)
    {
        this.ldapInitialContextFactory = ldapInitialDirContextFactory;
    }
    
    
    public void setUserNameFormat(String userNameFormat)
    {
        this.userNameFormat = userNameFormat;
    }
    
    /**
     * Implement the authentication method
     */
    public void authenticate(String userName, char[] password) throws AuthenticationException
    {
        InitialDirContext ctx = null;
        try
        {
            ctx = ldapInitialContextFactory.getInitialDirContext(String.format(userNameFormat, new Object[]{userName}), new String(password));
            
            // Authentication has been successful.
            // Set the current user, they are now authenticated.
            setCurrentUser(userName);         
            
        }
        finally
        {
            if(ctx != null)
            {
                try
                {
                    ctx.close();
                }
                catch (NamingException e)
                {
                    clearCurrentSecurityContext();
                    throw new AuthenticationException("Failed to close connection", e);
                }
            }
        }
    }


    @Override
    protected boolean implementationAllowsGuestLogin()
    {
        InitialDirContext ctx = null;
        try
        {
            ctx = ldapInitialContextFactory.getDefaultIntialDirContext();
            return true;       
            
        }
        catch(Exception e)
        {
            return false;
        }
        finally
        {
            if(ctx != null)
            {
                try
                {
                    ctx.close();
                }
                catch (NamingException e)
                {
                    throw new AuthenticationException("Failed to close connection", e);
                }
            }
        }
    }
    
    
}
