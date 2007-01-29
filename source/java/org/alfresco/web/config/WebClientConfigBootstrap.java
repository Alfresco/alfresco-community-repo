/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.config;

import java.util.List;

import org.alfresco.config.ConfigService;
import org.alfresco.config.ConfigSource;
import org.alfresco.config.source.UrlConfigSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Web client config bootstrap
 * 
 * @author Roy Wetherall
 */
public class WebClientConfigBootstrap implements ApplicationContextAware
{
    /** The application context */
    private ApplicationContext applicationContext;
    
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
     * Initialisation method
     */
    public void init()
    {
        if (this.applicationContext.containsBean("webClientConfigService") == true)
        {    
            ConfigService configService = (ConfigService)this.applicationContext.getBean("webClientConfigService"); 
            if (configService != null && this.configs != null && this.configs.size() != 0)
            {
                UrlConfigSource configSource = new UrlConfigSource(this.configs);
                configService.appendConfig(configSource);
            }
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;        
    }
}
