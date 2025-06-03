/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.security.authentication.identityservice.authentication.admin;

import java.util.Set;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.external.ExternalUserAuthenticator;
import org.alfresco.repo.security.authentication.identityservice.authentication.AbstractIdentityServiceAuthenticator;

/**
 * An {@link ExternalUserAuthenticator} implementation to extract an externally authenticated user ID or to initiate the OIDC authorization code flow.
 */
public class IdentityServiceAdminConsoleAuthenticator extends AbstractIdentityServiceAuthenticator
        implements ExternalUserAuthenticator, ActivateableBean
{
    private boolean isEnabled;

    @Override
    protected Set<String> getConfiguredScopes()
    {
        return identityServiceConfig.getAdminConsoleScopes();
    }

    @Override
    protected String getConfiguredRedirectPath()
    {
        return identityServiceConfig.getAdminConsoleRedirectPath();
    }

    @Override
    public boolean isActive()
    {
        return isEnabled;
    }

    public void setActive(boolean isEnabled)
    {
        this.isEnabled = isEnabled;
    }
}
