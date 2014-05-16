/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.webdav.auth;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Basic HTTP web authentication implementation. Main purpose to use as fallback authentication with SSO filters.
 * </p>
 * 
 * @author pavel.yurkevich
 */
public class SSOFallbackBasicAuthenticationDriver implements AuthenticationDriver
{
    public static final String AUTHENTICATION_USER = "_alfAuthTicket";
    
    private Log logger = LogFactory.getLog(SSOFallbackBasicAuthenticationDriver.class);
    
    private AuthenticationService authenticationService;
    private PersonService personService;
    private NodeService nodeService;
    private TransactionService transactionService;
    
    private String userAttributeName = AUTHENTICATION_USER;

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setUserAttributeName(String userAttributeName)
    {
        this.userAttributeName = userAttributeName;
    }
    
    @Override
    public boolean authenticateRequest(ServletContext context, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException
    {
        String authHdr = request.getHeader("Authorization");
        HttpSession session = request.getSession(false);
        SessionUser user = session == null ? null : (SessionUser) session.getAttribute(userAttributeName);
        if (user == null)
        {
            if (authHdr != null && authHdr.length() > 5 && authHdr.substring(0, 5).equalsIgnoreCase("Basic"))
            {
                String basicAuth = new String(Base64.decodeBase64(authHdr.substring(5).getBytes()));
                String username = null;
                String password = null;
    
                int pos = basicAuth.indexOf(":");
                if (pos != -1)
                {
                    username = basicAuth.substring(0, pos);
                    password = basicAuth.substring(pos + 1);
                }
                else
                {
                    username = basicAuth;
                    password = "";
                }
    
                try
                {
                    if (logger.isDebugEnabled())
                        logger.debug("Authenticating user '" + username + "'");
    
                    authenticationService.authenticate(username, password.toCharArray());

                    final RetryingTransactionCallback<SessionUser> callback = new RetryingTransactionCallback<SessionUser>()
                    {
                        @Override
                        public SessionUser execute() throws Throwable
                        {
                            NodeRef personNodeRef = personService.getPerson(authenticationService.getCurrentUserName());
                            String username = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME);
                            NodeRef homeSpaceRef = (NodeRef) nodeService.getProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER);
                            
                            return new WebDAVUser(username, authenticationService.getCurrentTicket(), homeSpaceRef);
                        }
                    };
                    
                    user = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<SessionUser>()
                    {
                        public SessionUser doWork() throws Exception
                        {
                            return transactionService.getRetryingTransactionHelper().doInTransaction(callback, true);
                        }
                    }, AuthenticationUtil.SYSTEM_USER_NAME);
                    
                    if (logger.isDebugEnabled())
                        logger.debug("Authenticated user '" + username + "'");
                    
                    request.getSession().setAttribute(userAttributeName, user);
                    return true;
                }
                catch (AuthenticationException ex)
                {
                    // Do nothing, user object will be null
                }
            }
        }
        else
        {
            try
            {
                authenticationService.validate(user.getTicket());
                return true;
            }
            catch (AuthenticationException ex)
            {
                session.invalidate();
            }
        }

        return false;
    }

    @Override
    public void restartLoginChallenge(ServletContext context, HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Including Basic HTTP authentication into response headers...");
        }

        response.addHeader("WWW-Authenticate", "Basic realm=\"Alfresco Server\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

}
