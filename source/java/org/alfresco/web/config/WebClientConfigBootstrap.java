/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.config;

import java.util.List;

import org.alfresco.config.ConfigDeployer;
import org.alfresco.config.ConfigDeployment;
import org.alfresco.config.ConfigService;
import org.alfresco.config.source.UrlConfigSource;
import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Web client config bootstrap
 * 
 * @author Roy Wetherall
 */
public class WebClientConfigBootstrap implements ApplicationContextAware, ConfigDeployer
{
    /** The application context */
    private ApplicationContext applicationContext;
    
    /** Dependency */
    private ConfigService configService;
    
    /** List of configs */
    private List<String> configs;
    
    /**
     * Set the configs
     * 
     * @param configs   the configs
     */
    public void setConfigs(List<String> configs)
    {
        this.configs = configs;
    }
    
    /**
     *
     * @deprecated
     */
    public void init()
    {
    	// TODO - see JIRA Task AR-1715 - refactor calling modules to inject webClientConfigService, and use init-method="register" directly 
    	// (instead of init-method="init"). Can then remove applicationContext and no longer implement ApplicationContextAware
        
        if (this.applicationContext.containsBean("webClientConfigService") == true)
        {    
            ConfigService configService = (ConfigService)this.applicationContext.getBean("webClientConfigService"); 
            if (configService != null)
            {
                setConfigService(configService);
                register();
            }
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;        
    }
    
    public void setConfigService(ConfigService configService)
    {
    	this.configService = configService;
    }
    
    public void register()
    {
    	if (configService == null)
        {
    		throw new AlfrescoRuntimeException("Config service must be provided");
        }
    	
    	configService.addDeployer(this);
    }
    
    /**
     * Initialisation method
     */
    public List<ConfigDeployment> initConfig()
    {
    	if (configService != null && this.configs != null && this.configs.size() != 0)
        {
            UrlConfigSource configSource = new UrlConfigSource(this.configs);
            return configService.appendConfig(configSource);
        }
    	
    	return null;
    }
}
