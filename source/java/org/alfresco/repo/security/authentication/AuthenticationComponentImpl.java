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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Set;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.AuthenticationManager;
import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.context.ContextHolder;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authentication.ntlm.NLTMAuthenticator;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;

public class AuthenticationComponentImpl extends AbstractAuthenticationComponent implements NLTMAuthenticator
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
    @Override
    protected void authenticateImpl(final String userName, final char[] password) throws AuthenticationException
    {
        try
        {
            String normalized = getTransactionService().getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionCallback<String>()
                    {
                        public String execute() throws Throwable
                        {
                            return AuthenticationUtil.runAs(new RunAsWork<String>()
                            {
                                public String doWork() throws Exception
                                {
                                    String normalized = getPersonService().getUserIdentifier(userName);
                                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                            normalized == null ? userName : normalized, new String(password));
                                    authenticationManager.authenticate(authentication);
                                    return normalized;
                                }
                            }, getSystemUserName(getUserDomain(userName)));
                        }
                    }, true); 
            if (normalized == null)
            {
                setCurrentUser(userName, UserNameValidationMode.CHECK_AND_FIX);
            }
            else
            {
                setCurrentUser(normalized, UserNameValidationMode.NONE);                
            }
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
    @Override
    protected UserDetails getUserDetails(String userName)
    {
        if (AuthenticationUtil.isMtEnabled())
        {
            // ALF-9403 - "manual" runAs to avoid clearing ticket, eg. when called via "validate" (->setCurrentUser->CheckCurrentUser)
            Authentication originalFullAuthentication = AuthenticationUtil.getFullAuthentication();
            try
            {
                if (originalFullAuthentication == null)
                {
                    AuthenticationUtil.setFullyAuthenticatedUser(getSystemUserName(getUserDomain(userName)));
                }
                return authenticationDao.loadUserByUsername(userName);
            }
            finally
            {
                if (originalFullAuthentication == null)
                {
                    ContextHolder.setContext(null); // note: does not clear ticket (unlike AuthenticationUtil.clearCurrentSecurityContext())
                }
            }
        }
        else
        {
            return authenticationDao.loadUserByUsername(userName);
        }
    }
    
    /**
     * Get the password hash from the DAO
     */
    public String getMD4HashedPassword(String userName)
    {
        return this.authenticationDao.getMD4HashedPassword(userName);
    }

    /**
     * The default is not to support Authentication token base authentication
     */
    public Authentication authenticate(Authentication token) throws AuthenticationException
    {
        throw new AlfrescoRuntimeException("Authentication via token not supported");
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

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.authentication.AuthenticationComponent#getDefaultAdministratorUserNames()
     */
    @Override
    public Set<String> getDefaultAdministratorUserNames()
    {
        return Collections.singleton(AuthenticationUtil.getAdminUserName());
    }
}
