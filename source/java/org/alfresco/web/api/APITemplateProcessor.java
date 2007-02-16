/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.web.api;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.template.FreeMarkerProcessor;
import org.alfresco.repo.template.QNameAwareObjectWrapper;
import org.alfresco.util.AbstractLifecycleBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import freemarker.cache.MruCacheStorage;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;


/**
 * FreeMarker Processor for use in Web API
 * 
 * Adds the ability to:
 * - specify template loaders
 * - caching of templates
 * 
 * @author davidc
 */
public class APITemplateProcessor extends FreeMarkerProcessor implements ApplicationContextAware, ApplicationListener
{
    private ProcessorLifecycle lifecycle = new ProcessorLifecycle();
    private Set<TemplateLoader> templateLoaders = new HashSet<TemplateLoader>();
    private String defaultEncoding;
    private Configuration templateConfig;


    /* (non-Javadoc)
     * @see org.alfresco.repo.template.FreeMarkerProcessor#setDefaultEncoding(java.lang.String)
     */
    public void setDefaultEncoding(String defaultEncoding)
    {
        this.defaultEncoding = defaultEncoding;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.template.FreeMarkerProcessor#getConfig()
     */
    @Override
    protected Configuration getConfig()
    {
        return templateConfig;
    }

    /**
     * Add a Template Loader
     * 
     * @param templateLoader  template loader
     */
    public void addTemplateLoader(TemplateLoader templateLoader)
    {
        templateLoaders.add(templateLoader);
    }

    /**
     * Initialise FreeMarker Configuration
     */
    protected void initConfig()
    {
        Configuration config = new Configuration();
        
        // setup template cache
        config.setCacheStorage(new MruCacheStorage(20, 100));

        // setup template loaders
        for (TemplateLoader templateLoader : templateLoaders)
        {
            config.setTemplateLoader(templateLoader);
        }
        
        // use our custom object wrapper that can deal with QNameMap objects directly
        config.setObjectWrapper(new QNameAwareObjectWrapper());
        
        // rethrow any exception so we can deal with them
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        
        // set template encoding
        if (defaultEncoding != null)
        {
            config.setDefaultEncoding(defaultEncoding);
        }
        
        // set output encoding
        config.setOutputEncoding("UTF-8");
        
        templateConfig = config;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        lifecycle.setApplicationContext(applicationContext);
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event)
    {
        lifecycle.onApplicationEvent(event);
    }
    
    /**
     * Hooks into Spring Application Lifecycle
     */
    private class ProcessorLifecycle extends AbstractLifecycleBean
    {
        @Override
        protected void onBootstrap(ApplicationEvent event)
        {
            initConfig();
        }
    
        @Override
        protected void onShutdown(ApplicationEvent event)
        {
        }
    }

}
