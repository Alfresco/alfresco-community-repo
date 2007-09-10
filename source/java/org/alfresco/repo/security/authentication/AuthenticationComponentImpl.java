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

import java.io.PrintWriter;
import java.io.StringWriter;

import net.sf.acegisecurity.AuthenticationManager;
import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;

public class AuthenticationComponentImpl extends AbstractAuthenticationComponent
{
    private MutableAuthenticationDao authenticationDao;

    AuthenticationManager authenticationManager;

    public AuthenticationComponentImpl()
    {
        super();
    }

    /**
     * IOC
     * 
     * @param authenticationManager
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager)
    {
        this.authenticationManager = authenticationManager;
    }

    /**
     * IOC
     * 
     * @param authenticationDao
     */
    public void setAuthenticationDao(MutableAuthenticationDao authenticationDao)
    {
        this.authenticationDao = authenticationDao;
    }
    
    /**
     * Authenticate
     */
    protected void authenticateImpl(String userName, char[] password) throws AuthenticationException
    {
        try
        {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userName,
                    new String(password));
            authenticationManager.authenticate(authentication);
            setCurrentUser(userName);

        }
        catch (net.sf.acegisecurity.AuthenticationException ae)
        {
            // This is a bit gross, I admit, but when LDAP is 
            // configured ae, above, is non-serializable and breaks
            // remote authentication.
            StringWriter sw = new StringWriter();
            PrintWriter out = new PrintWriter(sw);
            out.println(ae.toString());
            ae.printStackTrace(out);
            out.close();
            throw new AuthenticationException(sw.toString());
        }
    }

 
    /**
     * We actually have an acegi object so override the default method.  
     */
    protected UserDetails getUserDetails(String userName)
    {
        return (UserDetails) authenticationDao.loadUserByUsername(userName);
    }

    
    /**
     * Get the password hash from the DAO
     */
    public String getMD4HashedPassword(String userName)
    {
        return authenticationDao.getMD4HashedPassword(userName);
    }

    
    /**
     * This implementation supported MD4 password hashes. 
     */
    public NTLMMode getNTLMMode()
    {
        return NTLMMode.MD4_PROVIDER;
    }

    @Override
    protected boolean implementationAllowsGuestLogin()
    {
        return true;
    }
   
    
}
