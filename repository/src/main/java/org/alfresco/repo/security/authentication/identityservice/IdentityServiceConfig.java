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

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.beans.factory.InitializingBean;

/**
 * Class to hold configuration for the Identity Service.
 *
 * @author Gavin Cornwell
 */
public class IdentityServiceConfig extends AdapterConfig implements InitializingBean
{
    private static Log logger = LogFactory.getLog(IdentityServiceConfig.class);
    
    private static final String CREDENTIALS_SECRET = "identity-service.credentials.secret";
    private static final String CREDENTIALS_PROVIDER = "identity-service.credentials.provider";
    
    private Properties globalProperties;
    
    public void setGlobalProperties(Properties globalProperties)
    {
        this.globalProperties = globalProperties;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        // programatically build the more complex objects i.e. credentials
        Map<String, Object> credentials = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        
        String secret = this.globalProperties.getProperty(CREDENTIALS_SECRET);
        if (secret != null && !secret.isEmpty())
        {
            credentials.put("secret", secret);
        }
        
        String provider = this.globalProperties.getProperty(CREDENTIALS_PROVIDER);
        if (provider != null && !provider.isEmpty())
        {
            credentials.put("provider", provider);
        }
        
        // TODO: add support for redirect-rewrite-rules and policy-enforcer if and when we need to support it
        
        if (!credentials.isEmpty())
        {
            this.setCredentials(credentials);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Created credentials map from config: " + credentials);
            }
        }
    }
}
