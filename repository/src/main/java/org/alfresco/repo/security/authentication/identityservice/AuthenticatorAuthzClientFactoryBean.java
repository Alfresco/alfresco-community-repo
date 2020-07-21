/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.keycloak.adapters.HttpClientBuilder;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.springframework.beans.factory.FactoryBean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * Creates an instance of {@link AuthzClient}. <br>
 * The creation of {@link AuthzClient} requires connection to a Keycloak server, disable this factory if Keycloak cannot be reached. <br>
 * This factory can return a null if it is disabled.
 *
 */
public class AuthenticatorAuthzClientFactoryBean implements FactoryBean<AuthzClient>
{

    private static Log logger = LogFactory.getLog(AuthenticatorAuthzClientFactoryBean.class);
    private IdentityServiceConfig identityServiceConfig;
    private boolean enabled;

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void setIdentityServiceConfig(IdentityServiceConfig identityServiceConfig)
    {
        this.identityServiceConfig = identityServiceConfig;
    }

    @Override
    public AuthzClient getObject() throws Exception
    {
        // The creation of the client can be disabled for testing or when the username/password authentication is not required,
        // for instance when Keycloak is configured for 'bearer only' authentication or Direct Access Grants are disabled.
        if (!enabled)
        {
            return null;
        }

        // Build default http client using the keycloak client builder.
        int conTimeout = identityServiceConfig.getClientConnectionTimeout();
        int socTimeout = identityServiceConfig.getClientSocketTimeout();
        HttpClient client = new HttpClientBuilder()
                .establishConnectionTimeout(conTimeout, TimeUnit.MILLISECONDS)
                .socketTimeout(socTimeout, TimeUnit.MILLISECONDS)
                .build(this.identityServiceConfig);

        // Add secret to credentials if needed.
        // AuthzClient configuration needs credentials with a secret even if the client in Keycloak is configured as public.
        Map<String, Object> credentials = identityServiceConfig.getCredentials();
        if (credentials == null || !credentials.containsKey("secret"))
        {
            credentials = credentials == null ? new HashMap<>() : new HashMap<>(credentials);
            credentials.put("secret", "");
        }

        // Create default AuthzClient for authenticating users against keycloak
        String authServerUrl = identityServiceConfig.getAuthServerUrl();
        String realm = identityServiceConfig.getRealm();
        String resource = identityServiceConfig.getResource();
        Configuration authzConfig = new Configuration(authServerUrl, realm, resource, credentials, client);
        AuthzClient authzClient = AuthzClient.create(authzConfig);

        if (logger.isDebugEnabled())
        {
            logger.debug(" Created Keycloak AuthzClient");
            logger.debug(" Keycloak AuthzClient server URL: " + authzClient.getConfiguration().getAuthServerUrl());
            logger.debug(" Keycloak AuthzClient realm: " + authzClient.getConfiguration().getRealm());
            logger.debug(" Keycloak AuthzClient resource: " + authzClient.getConfiguration().getResource());
        }
        return authzClient;
    }

    @Override
    public Class<?> getObjectType()
    {
        return AuthenticatorAuthzClientFactoryBean.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }
}
