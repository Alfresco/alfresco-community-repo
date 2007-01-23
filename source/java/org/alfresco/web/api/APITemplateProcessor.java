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
