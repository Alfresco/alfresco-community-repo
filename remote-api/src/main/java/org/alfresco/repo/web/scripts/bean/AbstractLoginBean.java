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
package org.alfresco.repo.web.scripts.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.sync.events.types.Event;
import org.alfresco.sync.events.types.RepositoryEventImpl;
import org.alfresco.sync.repo.events.EventPreparator;
import org.alfresco.sync.repo.events.EventPublisher;

/**
 * Common code between Get based login and POST based login
 */
/* package scope */ abstract class AbstractLoginBean extends DeclarativeWebScript
{
    // dependencies
    private AuthenticationService authenticationService;
    protected EventPublisher eventPublisher;

    /**
     * @param authenticationService
     *            AuthenticationService
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * @param eventPublisher
     *            EventPublisher
     */
    public void setEventPublisher(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse) */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        return null;
    }

    protected Map<String, Object> login(final String username, String password)
    {
        try
        {
            // get ticket
            authenticationService.authenticate(username, password.toCharArray());

            eventPublisher.publishEvent(new EventPreparator() {
                @Override
                public Event prepareEvent(String user, String networkId, String transactionId)
                {
                    // TODO need to fix up to pass correct seqNo and alfrescoClientId
                    return new RepositoryEventImpl(-1l, "login", transactionId, networkId, new Date().getTime(),
                            username, null);
                }
            });

            // add ticket to model for javascript and template access
            Map<String, Object> model = new HashMap<String, Object>(7, 1.0f);
            model.put("username", username);
            model.put("ticket", authenticationService.getCurrentTicket());

            return model;
        }
        catch (AuthenticationException e)
        {
            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "Login failed");
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }

}
