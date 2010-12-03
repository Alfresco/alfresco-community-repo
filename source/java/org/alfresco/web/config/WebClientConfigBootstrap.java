/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.web.config;

import java.util.List;

import org.springframework.extensions.config.ConfigDeployer;
import org.springframework.extensions.config.ConfigDeployment;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.config.source.UrlConfigSource;
import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Web client config bootstrap
 * 
 * @author Roy Wetherall
 */
public class WebClientConfigBootstrap implements ApplicationContextAware, BeanNameAware, ConfigDeployer
{
    
    /** The bean name. */
    private String beanName;

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

    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        this.beanName = name;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;        
    }
    
    public void setConfigService(ConfigService configService)
    {
    	this.configService = configService;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.config.ConfigDeployer#getSortKey()
     */
    public String getSortKey()
    {
        return this.beanName;
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
