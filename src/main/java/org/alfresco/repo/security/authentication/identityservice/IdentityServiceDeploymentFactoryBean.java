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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.keycloak.adapters.HttpClientBuilder;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.springframework.beans.factory.FactoryBean;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

/**
 * Creates an instance of a KeycloakDeployment object for communicating with the Identity Service.
 *
 * @author Gavin Cornwell
 */
public class IdentityServiceDeploymentFactoryBean implements FactoryBean<KeycloakDeployment>
{
    private static Log logger = LogFactory.getLog(IdentityServiceDeploymentFactoryBean.class);
    
    private IdentityServiceConfig identityServiceConfig;
    
    public void setIdentityServiceConfig(IdentityServiceConfig config)
    {
        this.identityServiceConfig = config;
    }
    
    @Override
    public KeycloakDeployment getObject() throws Exception
    {
        KeycloakDeployment deployment = KeycloakDeploymentBuilder.build(this.identityServiceConfig);
        Class myClass = deployment.getClass();
        Field realmField = myClass.getDeclaredField("realmInfoUrl");
        realmField.setAccessible(true);
        realmField.set(deployment,this.identityServiceConfig.getAuthServerUrl() + "/realms/" + this.identityServiceConfig.getRealm());

        // Set client with custom timeout values if client was created by the KeycloakDeploymentBuilder.
        // This can be removed if the future versions of Keycloak accept timeout values through the config.
        if (deployment.getClient() != null)
        {
            int connectionTimeout = identityServiceConfig.getClientConnectionTimeout();
            int socketTimeout = identityServiceConfig.getClientSocketTimeout();
            HttpClient client = new HttpClientBuilder()
                    .establishConnectionTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                    .socketTimeout(socketTimeout, TimeUnit.MILLISECONDS)
                    .build(this.identityServiceConfig);
            deployment.setClient(client);

            if (logger.isDebugEnabled())
            {
                logger.debug("Created HttpClient for Keycloak deployment with connection timeout: "+ connectionTimeout + " ms, socket timeout: "+ socketTimeout+" ms.");
            }
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("HttpClient for Keycloak deployment was not set.");
            }
        }

        if (logger.isInfoEnabled())
        {
            logger.info("Keycloak JWKS URL: " + deployment.getJwksUrl());
            logger.info("Keycloak Realm: " + deployment.getRealm());
            logger.info("Keycloak Client ID: " + deployment.getResourceName());
        }
        
        return deployment;
    }

    @Override
    public Class<KeycloakDeployment> getObjectType()
    {
        return KeycloakDeployment.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }
}
