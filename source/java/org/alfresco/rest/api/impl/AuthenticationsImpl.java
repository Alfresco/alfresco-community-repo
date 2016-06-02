/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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

package org.alfresco.rest.api.impl;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.Authorization;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.rest.api.Authentications;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.model.LoginTicket;
import org.alfresco.rest.api.model.LoginTicketResponse;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.Base64;
import org.springframework.extensions.webscripts.Status;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class AuthenticationsImpl implements Authentications
{
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String PARAM_ALF_TICKET = "alf_ticket";

    private AuthenticationService authenticationService;
    private TicketComponent ticketComponent;

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setTicketComponent(TicketComponent ticketComponent)
    {
        this.ticketComponent = ticketComponent;
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "authenticationService", authenticationService);
        PropertyCheck.mandatory(this, "ticketComponent", ticketComponent);
    }

    @Override
    public LoginTicketResponse createTicket(LoginTicket loginRequest, Parameters parameters)
    {
        validateLoginRequest(loginRequest);
        try
        {
            // get ticket
            authenticationService.authenticate(loginRequest.getUserId(), loginRequest.getPassword().toCharArray());

            LoginTicketResponse response = new LoginTicketResponse();
            response.setUserId(loginRequest.getUserId());
            response.setId(authenticationService.getCurrentTicket());

            return response;
        }
        catch (AuthenticationException e)
        {
            throw new PermissionDeniedException("Login failed");
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }

    @Override
    public LoginTicketResponse validateTicket(String me, Parameters parameters, WithResponse withResponse)
    {
        if (!People.DEFAULT_USER.equals(me))
        {
            throw new InvalidArgumentException("Invalid parameter: " + me);
        }

        final String ticket = getTicket(parameters);
        try
        {
            final String ticketUser = ticketComponent.validateTicket(ticket);

            final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
            // do not go any further if tickets are different
            // or the user is not fully authenticated
            if (currentUser == null || !currentUser.equals(ticketUser))
            {
                withResponse.setStatus(Status.STATUS_NOT_FOUND);
            }
        }
        catch (AuthenticationException e)
        {
            withResponse.setStatus(Status.STATUS_NOT_FOUND);
        }
        LoginTicketResponse response = new LoginTicketResponse();
        response.setId(ticket);
        return response;
    }

    @Override
    public void deleteTicket(String me, Parameters parameters, WithResponse withResponse)
    {
        if (!People.DEFAULT_USER.equals(me))
        {
            throw new InvalidArgumentException("Invalid parameter: " + me);
        }

        final String ticket = getTicket(parameters);
        try
        {
            final String ticketUser = ticketComponent.validateTicket(ticket);

            final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
            // do not go any further if tickets are different
            // or the user is not fully authenticated
            if (currentUser == null || !currentUser.equals(ticketUser))
            {
                withResponse.setStatus(Status.STATUS_NOT_FOUND);
            }
            else
            {
                // delete the ticket
                authenticationService.invalidateTicket(ticket);
            }
        }
        catch (AuthenticationException e)
        {
            withResponse.setStatus(Status.STATUS_NOT_FOUND);
        }
    }

    protected void validateLoginRequest(LoginTicket loginTicket)
    {
        if (loginTicket == null || loginTicket.getUserId() == null || loginTicket.getPassword() == null)
        {
            throw new InvalidArgumentException("Invalid login details.");
        }
    }

    protected String getTicket(Parameters parameters)
    {
        // First check the alf_ticket in the URL
        final String alfTicket = parameters.getParameter(PARAM_ALF_TICKET);
        if (StringUtils.isNotEmpty(alfTicket))
        {
            return alfTicket;
        }

        // Check the Authorization header
        final String authorization = parameters.getRequest().getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isEmpty(authorization))
        {
            throw new InvalidArgumentException("Authorization header is required.");
        }

        final String[] authorizationParts = authorization.split(" ");
        if (!authorizationParts[0].equalsIgnoreCase("basic"))
        {
            throw new InvalidArgumentException("Authorization '" + authorizationParts[0] + "' not supported.");
        }

        final String decodedAuthorisation = new String(Base64.decode(authorizationParts[1]));
        Authorization authObj = new Authorization(decodedAuthorisation);
        if (!authObj.isTicket())
        {
            throw new InvalidArgumentException("Ticket base authentication required.");
        }
        return authObj.getTicket();
    }
}
