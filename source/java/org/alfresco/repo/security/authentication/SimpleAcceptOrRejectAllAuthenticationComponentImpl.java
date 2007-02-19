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


/**
 * This implementation of an AuthenticationComponent can be configured to accept or reject all attempts to login.
 * 
 * This only affects attempts to login using a user name and password.
 * Authentication filters etc. could still support authentication but not via user names and passwords.
 * For example, where they set the current user using the authentication component.
 * Then the current user is set in the security context and asserted to be authenticated.
 * 
 * By default, the implementation rejects all authentication attempts.
 *  
 * @author Andy Hind
 */
public class SimpleAcceptOrRejectAllAuthenticationComponentImpl extends AbstractAuthenticationComponent
{
    private boolean accept = false;

    public SimpleAcceptOrRejectAllAuthenticationComponentImpl()
    {
        super();
    }

    public void setAccept(boolean accept)
    {
        this.accept = accept;
    }
    
    public void authenticate(String userName, char[] password) throws AuthenticationException
    {
        if(accept)
        {
            setCurrentUser(userName);
        }
        else
        {
            throw new AuthenticationException("Access Denied");
        }

    }

    @Override
    protected boolean implementationAllowsGuestLogin()
    {
       return accept;
    }
    
    
}
