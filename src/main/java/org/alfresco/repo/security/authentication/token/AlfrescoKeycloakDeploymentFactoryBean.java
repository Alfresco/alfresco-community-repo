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
package org.alfresco.repo.security.authentication.token;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.beans.factory.FactoryBean;

/**
 * Creates an instance of a KeycloakDeployment object.
 *
 * @author Gavin Cornwell
 */
public class AlfrescoKeycloakDeploymentFactoryBean implements FactoryBean<KeycloakDeployment>
{
    private static Log logger = LogFactory.getLog(AlfrescoKeycloakDeploymentFactoryBean.class);
    
    private AdapterConfig keycloakAdapterConfig;
    
    public void setAdapterConfig(AdapterConfig adapterConfig)
    {
        this.keycloakAdapterConfig = adapterConfig;
    }
    
    @Override
    public KeycloakDeployment getObject() throws Exception
    {
        KeycloakDeployment deployment = KeycloakDeploymentBuilder.build(this.keycloakAdapterConfig);
        
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
