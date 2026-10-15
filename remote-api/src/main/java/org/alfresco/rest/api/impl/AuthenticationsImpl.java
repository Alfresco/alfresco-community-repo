/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

package org.alfresco.rest.api.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.Base64;

import org.alfresco.repo.SessionUser;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.Authorization;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.repo.security.authentication.external.RemoteUserMapper;
import org.alfresco.repo.web.scripts.BufferedRequest;
import org.alfresco.repo.webdav.auth.AuthenticationDriver;
import org.alfresco.repo.webdav.auth.BaseAuthenticationFilter;
import org.alfresco.rest.api.Authentications;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.PublicApiTenantWebScriptServletRequest;
import org.alfresco.rest.api.model.LoginTicket;
import org.alfresco.rest.api.model.LoginTicketResponse;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.PropertyCheck;
import org.alfresco.web.app.servlet.KerberosAuthenticationFilter;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class AuthenticationsImpl implements Authentications
{
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String PARAM_ALF_TICKET = "alf_ticket";
    private static final String PARAM_TICKET = "ticket";
    private static final String AUTHORIZATION_NEGOTIATE = "negotiate";

    private AuthenticationService authenticationService;
    private TicketComponent ticketComponent;
    private RemoteUserMapper remoteUserMapper;

    private ChildApplicationContextManager authenticationContextManager;
    private static final String GLOBAL_AUTHENTICATION_FILTER_BEAN = "globalAuthenticationFilter";

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setTicketComponent(TicketComponent ticketComponent)
    {
        this.ticketComponent = ticketComponent;
    }

    public void setRemoteUserMapper(RemoteUserMapper remoteUserMapper)
    {
        this.remoteUserMapper = remoteUserMapper;
    }

    public void setAuthenticationContextManager(ChildApplicationContextManager authenticationContextManager)
    {
        this.authenticationContextManager = authenticationContextManager;
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

        // Return the ticket of an user already authenticated by an SSO filter (e.g. Kerberos)
        final String ssoTicket = getTicketForSsoAuthenticatedUser(parameters);
        if (ssoTicket != null)
        {
            LoginTicketResponse ssoResponse = new LoginTicketResponse();
            ssoResponse.setId(ssoTicket);
            return ssoResponse;
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
                throw new NotFoundException(NotFoundException.DEFAULT_MESSAGE_ID, new String[]{ticket});
            }
        }
        catch (AuthenticationException e)
        {
            throw new NotFoundException(NotFoundException.DEFAULT_MESSAGE_ID, new String[]{ticket});
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
                throw new NotFoundException(NotFoundException.DEFAULT_MESSAGE_ID, new String[]{ticket});
            }
            else
            {
                // delete the ticket
                authenticationService.invalidateTicket(ticket);
            }
        }
        catch (AuthenticationException e)
        {
            throw new NotFoundException(NotFoundException.DEFAULT_MESSAGE_ID, new String[]{ticket});
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
        if (authorizationParts[0].equalsIgnoreCase("basic"))
        {
            final String decodedAuthorisation = new String(Base64.decode(authorizationParts[1]));
            Authorization authObj = new Authorization(decodedAuthorisation);
            if (!authObj.isTicket())
            {
                throw new InvalidArgumentException("Ticket base authentication required.");
            }
            return authObj.getTicket();
        }
        else if (authorizationParts[0].equalsIgnoreCase("bearer"))
        {
            return getTicketFromRemoteUserMapperUserId(parameters);
        }

        throw new InvalidArgumentException("Authorization '" + authorizationParts[0] + "' not supported.");
    }

    /**
     * Returns the ticket held in the HTTP session of an SSO authenticated user.
     * <p>
     * Applies only when the request carries no ticket (no alf_ticket/ticket parameter and either no Authorization header or a "Negotiate" one) and:
     * <ul>
     * <li>Kerberos SSO is the active global authentication filter ({@link #isKerberosSsoActive()});</li>
     * <li>The session is flagged as externally authenticated ({@link BaseAuthenticationFilter#LOGIN_EXTERNAL_AUTH}), set by the SSO filters on every request they authenticate;</li>
     * <li>The session holds a {@link SessionUser} ({@link AuthenticationDriver#AUTHENTICATION_USER}) for the fully authenticated user;</li>
     * <li>The session user's ticket is valid and belongs to that same user.</li>
     * </ul>
     * </p>
     * <p>
     * No new ticket is created: the existing session ticket is returned.
     * </p>
     *
     * @return the session ticket, or null if the request does not match this case
     */
    private String getTicketForSsoAuthenticatedUser(Parameters parameters)
    {
        // An explicit ticket was provided: keep the standard validation behaviour
        if (StringUtils.isNotEmpty(parameters.getParameter(PARAM_ALF_TICKET))
                || StringUtils.isNotEmpty(parameters.getParameter(PARAM_TICKET)))
        {
            return null;
        }

        final String authorization = parameters.getRequest().getHeader(AUTHORIZATION_HEADER);
        final boolean noAuthorizationHeader = StringUtils.isEmpty(authorization);
        final boolean negotiate = !noAuthorizationHeader && authorization.split(" ")[0].equalsIgnoreCase(AUTHORIZATION_NEGOTIATE);
        if (!noAuthorizationHeader && !negotiate)
        {
            return null;
        }

        final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        if (currentUser == null || AuthenticationUtil.isRunAsUserTheSystemUser())
        {
            return null;
        }

        final HttpServletRequest httpServletRequest = extractHttpServletRequestFromParameters(parameters);
        if (httpServletRequest == null)
        {
            return null;
        }

        final HttpSession session = httpServletRequest.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute(BaseAuthenticationFilter.LOGIN_EXTERNAL_AUTH)))
        {
            return null;
        }

        final Object sessionAttribute = session.getAttribute(AuthenticationDriver.AUTHENTICATION_USER);
        if (!(sessionAttribute instanceof SessionUser))
        {
            return null;
        }

        final SessionUser sessionUser = (SessionUser) sessionAttribute;
        final String sessionTicket = sessionUser.getTicket();
        if (StringUtils.isEmpty(sessionTicket) || !currentUser.equals(sessionUser.getUserName()))
        {
            return null;
        }

        // Checked last (walks the authentication chain): only when Kerberos SSO is the active global authentication filter
        if (!isKerberosSsoActive())
        {
            return null;
        }

        try
        {
            final String ticketUser = ticketComponent.validateTicket(sessionTicket);
            return currentUser.equals(ticketUser) ? sessionTicket : null;
        }
        catch (AuthenticationException e)
        {
            return null;
        }
    }

    /**
     * Checks whether the global authentication filter that is actually routed to at runtime is the Kerberos SSO filter. Mirrors the routing done by the "GlobalAuthenticationFilter" ChainingSubsystemProxyFactory: the first active "globalAuthenticationFilter" bean in the authentication chain order wins.
     * <p>
     * This reflects the real state (chain, kerberos.authentication.sso.enabled, values changed via JMX/Admin Console) rather than a raw property value.
     * </p>
     */
    private boolean isKerberosSsoActive()
    {
        if (authenticationContextManager == null)
        {
            return false;
        }

        for (String instanceId : authenticationContextManager.getInstanceIds())
        {
            final ApplicationContext context;
            try
            {
                context = authenticationContextManager.getApplicationContext(instanceId);
            }
            catch (RuntimeException e)
            {
                // Subsystem failed to start, ignore (same behaviour as ChainingSubsystemProxyFactory)
                continue;
            }

            try
            {
                final Object filter = context.getBean(GLOBAL_AUTHENTICATION_FILTER_BEAN);
                if (!(filter instanceof ActivateableBean) || ((ActivateableBean) filter).isActive())
                {
                    return filter instanceof KerberosAuthenticationFilter;
                }
            }
            catch (NoSuchBeanDefinitionException e)
            {
                // No global filter in this subsystem, continue
            }
        }
        return false;
    }

    private String getTicketFromRemoteUserMapperUserId(Parameters parameters)
    {
        // If we got to execute any of the code in this class, it means that, somehow, the user is authenticated;
        // If we got this far, in this method, it means that:
        // * there is no alf_ticket in the URL;
        // * there is an "Authorization" header that says it is "bearer";
        // * the authorization type in the header is not "basic"; therefore the user was not authenticated with basic auth

        // We could end up here authenticated with some other mechanism (kerberos (SSO) or other custom authenticators)
        // We need to validate the bearer token so as not to open an exploit where we return the alf_ticket even if
        // the value of the bearer access token is not valid;

        // Validate the bearer access token again and
        // confirm that the current authenticated user is the same user specified in the bearer token
        HttpServletRequest httpServletRequest = extractHttpServletRequestFromParameters(parameters);
        if (httpServletRequest != null && isRemoteUserMapperActive())
        {
            String remoteUser = remoteUserMapper.getRemoteUser(httpServletRequest);
            // We accept that the remoteUserMapper may have not been the IdentityServiceRemoteUserMapper,
            // and could have been DefaultRemoteUserMapper (using External authentication), but that is ok
            // because the business logic is similar.
            if (remoteUser != null)
            {
                return ticketComponent.getCurrentTicket(remoteUser, false);
            }
        }
        throw new InvalidArgumentException("Can't use Alfresco Identity Services to validate the user in the bearer access token");
    }

    private HttpServletRequest extractHttpServletRequestFromParameters(Parameters parameters)
    {
        // An alternative solution would be to create some sort of ServletHttpFacade object based on the information present
        // in the parameters object. But for that we need to write a lot of code and check that we pass all the data required
        // by the keycloak library;

        // Parameters object is clearly not designed to give us access to the HttpServletRequest object,
        // but we know that remoteUserMapper.getRemoteUser will use this in a safe way
        if (parameters.getRequest() instanceof BufferedRequest &&
                ((BufferedRequest) parameters.getRequest()).getNext() instanceof PublicApiTenantWebScriptServletRequest)
        {
            return ((PublicApiTenantWebScriptServletRequest) ((BufferedRequest) parameters.getRequest()).getNext()).getHttpServletRequest();
        }
        return null;
    }

    private boolean isRemoteUserMapperActive()
    {
        return remoteUserMapper instanceof ActivateableBean && ((ActivateableBean) remoteUserMapper).isActive();
    }

}
