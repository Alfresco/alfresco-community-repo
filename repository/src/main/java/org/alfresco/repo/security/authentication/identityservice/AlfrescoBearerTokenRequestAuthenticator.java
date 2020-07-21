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
package org.alfresco.repo.security.authentication.identityservice;

import org.keycloak.adapters.BearerTokenRequestAuthenticator;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.OIDCAuthenticationError.Reason;
import org.keycloak.adapters.spi.AuthChallenge;
import org.keycloak.adapters.spi.HttpFacade;

/**
 * Extends the Keycloak BearerTokenRequestAuthenticator class to capture the error description
 * when token valiation fails.
 *
 * @author Gavin Cornwell
 */
public class AlfrescoBearerTokenRequestAuthenticator extends BearerTokenRequestAuthenticator
{
    private String validationFailureDescription;

    public AlfrescoBearerTokenRequestAuthenticator(KeycloakDeployment deployment)
    {
        super(deployment);
    }
    
    public String getValidationFailureDescription()
    {
        return this.validationFailureDescription;
    }

    @Override
    protected AuthChallenge challengeResponse(HttpFacade facade, Reason reason, String error, String description)
    {
        this.validationFailureDescription = description;
        
        return super.challengeResponse(facade, reason, error, description);
    }
}
