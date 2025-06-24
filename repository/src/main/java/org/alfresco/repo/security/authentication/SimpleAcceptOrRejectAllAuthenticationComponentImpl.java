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

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.context.ContextHolder;
import net.sf.acegisecurity.providers.dao.AuthenticationDao;
import net.sf.acegisecurity.providers.dao.UsernameNotFoundException;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * This implementation of an AuthenticationComponent can be configured to accept or reject all attempts to login.
 * 
 * This only affects attempts to login using a user name and password. Authentication filters etc. could still support authentication but not via user names and passwords. For example, where they set the current user using the authentication component. Then the current user is set in the security context and asserted to be authenticated.
 * 
 * By default, the implementation rejects all authentication attempts.
 * 
 * @author Andy Hind
 */
public class SimpleAcceptOrRejectAllAuthenticationComponentImpl extends AbstractAuthenticationComponent
{
    private boolean accept = false;

    private AuthenticationDao authenticationDao;

    public SimpleAcceptOrRejectAllAuthenticationComponentImpl()
    {
        super();
    }

    public void setAuthenticationDao(AuthenticationDao authenticationDao)
    {
        this.authenticationDao = authenticationDao;
    }

    public void setAccept(boolean accept)
    {
        this.accept = accept;
    }

    public void authenticateImpl(String userName, char[] password) throws AuthenticationException
    {
        if (accept)
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
        if (accept)
        {
            return "0cb6948805f797bf2a82807973b89537";
        }
        else
        {
            throw new AuthenticationException("Access Denied");
        }
    }

    /**
     * The default is not to support Authentication token base authentication
     */
    public Authentication authenticate(Authentication token) throws AuthenticationException
    {
        throw new AlfrescoRuntimeException("Authentication via token not supported");
    }

    /**
     * We actually have an acegi object so override the default method.
     */
    @Override
    protected UserDetails getUserDetails(String userName)
    {
        UserDetails userDetails = null;
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
                userDetails = authenticationDao.loadUserByUsername(userName);
            }
            catch (UsernameNotFoundException unfe)
            {
                // the user was not created beforehand
                logEvent(userName);
                userDetails = super.getUserDetails(userName);
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
            try
            {
                userDetails = authenticationDao.loadUserByUsername(userName);
            }
            catch (UsernameNotFoundException unfe)
            {
                // the user was not created beforehand
                logEvent(userName);
                userDetails = super.getUserDetails(userName);
            }
        }
        return userDetails;
    }

    private void logEvent(String userName)
    {
        if (logger.isTraceEnabled())
        {
            // we log this as trace because we expect sometimes this to happen and it is ok
            logger.trace("The user was not created beforehand: " + AuthenticationUtil.maskUsername(userName)
                    + " . This is not a problem, we expect this to happen in some cases");
        }
    }
}
