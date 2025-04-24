/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail. Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.security.authentication.identityservice.webscript;

import java.io.IOException;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.nimbusds.oauth2.sdk.id.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.external.WebScriptHomeAuthenticator;

/**
 * A {@link WebScriptHomeAuthenticator} implementation to extract an externally authenticated user ID or to initiate the OIDC authorization code flow for WebScript Home UI.
 */
public class IdentityServiceWebScriptHomeAuthenticator extends AbstractIdentityServiceAuthenticator
        implements WebScriptHomeAuthenticator, ActivateableBean
{
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityServiceWebScriptHomeAuthenticator.class);

    private boolean isEnabled;

    @Override
    public String getWebScriptHomeUser(HttpServletRequest request, HttpServletResponse response)
    {
        return resolveUser(request, response);
    }

    @Override
    public void requestAuthentication(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Responding with the authentication challenge");
            }
            response.sendRedirect(getAuthenticationRequest(request));
        }
        catch (IOException e)
        {
            LOGGER.error("WebScript Home Auth challenge failed: {}", e.getMessage(), e);
            throw new AuthenticationException(e.getMessage(), e);
        }
    }

    @Override
    protected boolean isWebScriptHome()
    {
        return true;
    }

    @Override
    protected HttpServletRequest newRequestWrapper(Map<String, String> headers, HttpServletRequest request)
    {
        return new WebScriptHomeHttpServletRequestWrapper(headers, request);
    }

    @Override
    protected String buildAuthRequestUrl(HttpServletRequest request)
    {
        return getAuthenticationRequest(request);
    }

    @Override
    protected boolean hasWebScriptHomeScope(Identifier scope)
    {
        return identityServiceConfig.getWebScriptHomeScopes().contains(scope.getValue());
    }

    @Override
    protected boolean hasAdminConsoleScope(Identifier scope)
    {
        return false;
    }

    protected String getRedirectUri(String requestURL)
    {
        return getWebScriptHomeRedirectUri(requestURL);
    }

    @Override
    public boolean isActive()
    {
        return this.isEnabled;
    }

    public void setActive(boolean isEnabled)
    {
        this.isEnabled = isEnabled;
    }
}
