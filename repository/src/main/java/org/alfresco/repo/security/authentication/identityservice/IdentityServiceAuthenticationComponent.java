/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.security.authentication.identityservice;

import java.net.ConnectException;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Authenticates a user against Identity Service (Keycloak).
 * {@link OAuth2Client} is used to verify provided user credentials. User is set as the current user if the user
 * credentials are valid.
 * <br>
 * The {@link IdentityServiceAuthenticationComponent#oAuth2Client} can be null in which case this authenticator will
 * just fall through to the next one in the chain.
 *
 */
public class IdentityServiceAuthenticationComponent extends AbstractAuthenticationComponent implements ActivateableBean
{
    private final Log logger = LogFactory.getLog(IdentityServiceAuthenticationComponent.class);
    /** client used to authenticate user credentials against Authorization Server **/
    private OAuth2Client oAuth2Client;
    /** enabled flag for the identity service subsystem**/
    private boolean active;
    private boolean allowGuestLogin;

    public void setOAuth2Client(OAuth2Client oAuth2Client)
    {
        this.oAuth2Client = oAuth2Client;
    }

    public void setAllowGuestLogin(boolean allowGuestLogin)
    {
        this.allowGuestLogin = allowGuestLogin;
    }

    public void authenticateImpl(String userName, char[] password) throws AuthenticationException
    {

        if (oAuth2Client == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("AuthzClient was not set, possibly due to the 'identity-service.authentication.enable-username-password-authentication=false' property. ");
            }

            throw new AuthenticationException("User not authenticated because AuthzClient was not set.");
        }

        try
        {
            // Attempt to get an access token using the user credentials
            oAuth2Client.verifyCredentialsUsingResourceOwnerPasswordCredentialsFlow(userName, new String(password));

            // Successfully obtained access token so treat as authenticated user
            setCurrentUser(userName);
        }
//        catch (HttpResponseException e)
//        {
//            if (logger.isDebugEnabled())
//            {
//                logger.debug("Failed to authenticate user against Keycloak. Status: " + e.getStatusCode() + " Reason: "+ e.getReasonPhrase());
//            }
//
//            throw new AuthenticationException("Failed to authenticate user against Keycloak.", e);
//        }
        catch (RuntimeException e)
        {
            Throwable cause = ExceptionStackUtil.getCause(e, ConnectException.class);
            if (cause != null)
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("Couldn't connect to Keycloak server to authenticate user. Reason: " + cause.getMessage());
                }
                throw new AuthenticationException("Couldn't connect to Keycloak server to authenticate user.", cause);
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("Error occurred while authenticating user against Keycloak. Reason: " + e.getMessage());
            }
            throw new AuthenticationException("Error occurred while authenticating user against Keycloak.", e);
        }
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    @Override
    public boolean isActive()
    {
        return active;
    }

    @Override
    protected boolean implementationAllowsGuestLogin()
    {
        return allowGuestLogin;
    }

    interface OAuth2Client
    {
        void verifyCredentialsUsingResourceOwnerPasswordCredentialsFlow(String userName, String password);
    }
}
