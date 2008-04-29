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
package org.alfresco.repo.web.scripts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.template.FreeMarkerProcessor;
import org.alfresco.repo.template.QNameAwareObjectWrapper;
import org.alfresco.service.cmr.repository.ProcessorExtension;
import org.alfresco.util.AbstractLifecycleBean;
import org.alfresco.web.scripts.SearchPath;
import org.alfresco.web.scripts.Store;
import org.alfresco.web.scripts.TemplateProcessor;
import org.alfresco.web.scripts.WebScriptException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import freemarker.cache.MruCacheStorage;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;


/**
 * Repository (server-tier) Web Script Template Processor
 * 
 * @author davidc
 */
public class RepositoryTemplateProcessor extends FreeMarkerProcessor
    implements TemplateProcessor, ApplicationContextAware, ApplicationListener
{
    private ProcessorLifecycle lifecycle = new ProcessorLifecycle();
    protected SearchPath searchPath;
    protected String defaultEncoding;
    protected Configuration templateConfig;
    protected FreeMarkerProcessor freeMarkerProcessor;


    /* (non-Javadoc)
     * @see org.alfresco.repo.template.FreeMarkerProcessor#setDefaultEncoding(java.lang.String)
     */
    public void setDefaultEncoding(String defaultEncoding)
    {
        this.defaultEncoding = defaultEncoding;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.TemplateProcessor#getDefaultEncoding()
     */
    public String getDefaultEncoding()
    {
        return this.defaultEncoding;
    }

    /**
     * @param searchPath
     */
    public void setSearchPath(SearchPath searchPath)
    {
        this.searchPath = searchPath;
    }
    
    /**
     * Set the freemarker processor
     * 
     * @param freeMarkerProcessor   the free marker processor
     */
    public void setFreeMarkerProcessor(FreeMarkerProcessor freeMarkerProcessor)
    {
        this.freeMarkerProcessor = freeMarkerProcessor;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.template.FreeMarkerProcessor#getConfig()
     */
    @Override
    protected Configuration getConfig()
    {
        return templateConfig;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.TemplateProcessor#reset()
     */
    public void reset()
    {
        if (templateConfig != null)
        {
            templateConfig.clearTemplateCache();
        }
        initConfig();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.TemplateProcessor#hasTemplate(java.lang.String)
     */
    public boolean hasTemplate(String templatePath)
    {
        boolean hasTemplate = false;
        try
        {
            Template template = templateConfig.getTemplate(templatePath);
            hasTemplate = (template != null);
        }
        catch(FileNotFoundException e)
        {
            // NOTE: return false as template is not found
        }
        catch(IOException e)
        {
            throw new WebScriptException("Failed to retrieve template " + templatePath, e);
        }
        return hasTemplate;
    }
    
    /**
     * Initialise FreeMarker Configuration
     */
    protected void initConfig()
    {
        Configuration config = new Configuration();
        
        // setup template cache
        config.setCacheStorage(new MruCacheStorage(20, 100));
        config.setTemplateUpdateDelay(0);

        // setup template loaders
        List<TemplateLoader> loaders = new ArrayList<TemplateLoader>();
        for (Store apiStore : searchPath.getStores())
        {
            TemplateLoader loader = apiStore.getTemplateLoader();
            if (loader == null)
            {
                throw new WebScriptException("Unable to retrieve template loader for Web Script store " + apiStore.getBasePath());
            }
            loaders.add(loader);
        }
        MultiTemplateLoader loader = new MultiTemplateLoader(loaders.toArray(new TemplateLoader[loaders.size()]));
        config.setTemplateLoader(loader);
        
        // use our custom object wrapper that can deal with QNameMap objects directly
        config.setObjectWrapper(new QNameAwareObjectWrapper());
        
        // rethrow any exception so we can deal with them
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        
        // turn off locale sensitive lookup - to save numerous wasted calls to nodeservice.exists()
        config.setLocalizedLookup(false);
        
        // set template encoding
        if (defaultEncoding != null)
        {
            config.setDefaultEncoding(defaultEncoding);
        }
        
        // set output encoding
        config.setOutputEncoding("UTF-8");
        
        templateConfig = config;
    }
    
    /**
     * Tempory fix to initialise this template processor with the freeMarker extensions expected by
     * the templates.    
     */
    private void initProcessorExtensions()
    {
        for (ProcessorExtension processorExtension : this.freeMarkerProcessor.getProcessorExtensions())
        {
            this.registerProcessorExtension(processorExtension);
        }
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
            initProcessorExtensions();
        }

        @Override
        protected void onShutdown(ApplicationEvent event)
        {
        }
    }

}
