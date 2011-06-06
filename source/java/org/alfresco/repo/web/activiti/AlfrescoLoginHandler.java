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

package org.alfresco.repo.web.activiti;

import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.identity.LoggedInUser;
import org.activiti.explorer.ui.login.LoginHandler;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;

/**
 * Handler for logging in into the Activiti administration UI, authenticates
 * against Alfresco {@link AuthenticationService} and
 * {@link AuthenticationService}.
 * 
 * @author Frederik Heremans
 */
public class AlfrescoLoginHandler implements LoginHandler
{
    protected AuthenticationService authenticationService;
    protected PersonService personService;
    protected NodeService nodeService;
    protected AuthorityService authorityService;

    @Override
    public LoggedInUser authenticate(String userName, String password)
    {
        LoggedInUser loggedInUser = null;

        if (checkCredentials(userName, password))
        {
            // Check if the user has the rights to use administrative
            // capabilities
            if (authorityService.isAdminAuthority(userName))
            {
                loggedInUser = createLoggedInUser(userName);
            }
        }
        return loggedInUser;
    }

    @Override
    public LoggedInUser authenticate()
    {
        LoggedInUser loggedInUser = null;
        try
        {
            String authenticatedUser = authenticationService.getCurrentUserName();
            if (authenticatedUser != null && authorityService.isAdminAuthority(authenticatedUser))
            {
                loggedInUser = createLoggedInUser(authenticatedUser);
            }
        }
        catch (AuthenticationException ae)
        {
            // Ignore, no user in current security-context
        }
        catch(net.sf.acegisecurity.AuthenticationException ae2) 
        {
            // Ignore, no user in current security-context
        }
        return loggedInUser;
    }

    @Override
    public void logout(LoggedInUser loggedInUser)
    {
        // Clear context
        authenticationService.clearCurrentSecurityContext();
    }

    protected LoggedInUser createLoggedInUser(String userName)
    {
        final NodeRef personNode = personService.getPerson(userName);
        final Map<QName, Serializable> allProperties = nodeService.getProperties(personNode);

        // Create user based on node properties
        final ActivitiLoggedInUser loggedInUser = new ActivitiLoggedInUser(userName);
        loggedInUser.setFirstName((String) allProperties.get(ContentModel.PROP_FIRSTNAME));
        loggedInUser.setLastName((String) allProperties.get(ContentModel.PROP_LASTNAME));

        // Indicate user can use and administer the app
        loggedInUser.setUser(true);
        loggedInUser.setAdmin(true);

        return loggedInUser;
    }

    protected boolean checkCredentials(String userName, String password)
    {
        try
        {
            authenticationService.authenticate(userName, password.toCharArray());
            return true;
        }
        catch (AuthenticationException ae)
        {
            return false;
        }
    }

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

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    @Override
    public void onRequestEnd(HttpServletRequest req, HttpServletResponse res)
    {
        // Nothing to do here
    }

    @Override
    public void onRequestStart(HttpServletRequest req, HttpServletResponse res)
    {
        if(ExplorerApp.get().getLoggedInUser() != null) {
            // Revalidate the ticket, if any the user is logged in to make sure all
            // calls to alfresco from activiti happen in right security context
            try 
            {
                authenticationService.validate(authenticationService.getCurrentTicket());
            }
            catch (AuthenticationException ae)
            {
                ticketExpired();
            }
            catch(net.sf.acegisecurity.AuthenticationException ae2) 
            {
                ticketExpired();
            }
        }
    }

    private void ticketExpired()
    {
        ExplorerApp.get().close();
    }

}
