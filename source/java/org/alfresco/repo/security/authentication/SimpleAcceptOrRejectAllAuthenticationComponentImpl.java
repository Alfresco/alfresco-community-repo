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

import net.sf.acegisecurity.Authentication;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.ntlm.NLTMAuthenticator;


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
public class SimpleAcceptOrRejectAllAuthenticationComponentImpl extends AbstractAuthenticationComponent implements NLTMAuthenticator
{
    private boolean accept = false;
    private boolean supportNtlm = false;

    public SimpleAcceptOrRejectAllAuthenticationComponentImpl()
    {
        super();
    }

    public void setAccept(boolean accept)
    {
        this.accept = accept;
    }
            
    public void setSupportNtlm(boolean supportNtlm)
    {
        this.supportNtlm = supportNtlm;
    }

   public void authenticateImpl(String userName, char[] password) throws AuthenticationException
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

    public String getMD4HashedPassword(String userName)
    {
        if(accept)
        {
            return "0cb6948805f797bf2a82807973b89537";
        }
        else
        {
            throw new AuthenticationException("Access Denied");
        }
    }

    public NTLMMode getNTLMMode()
    {
        return supportNtlm ? NTLMMode.MD4_PROVIDER : NTLMMode.NONE;
    }
    
    /**
     * The default is not to support Authentication token base authentication
     */
    public Authentication authenticate(Authentication token) throws AuthenticationException
    {
        throw new AlfrescoRuntimeException("Authentication via token not supported");
    }
}
