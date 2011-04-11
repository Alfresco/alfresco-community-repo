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
package org.alfresco.repo.web.scripts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.processor.ProcessorExtension;
import org.alfresco.repo.template.FreeMarkerProcessor;
import org.alfresco.repo.template.QNameAwareObjectWrapper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.extensions.webscripts.SearchPath;
import org.springframework.extensions.webscripts.Store;
import org.springframework.extensions.webscripts.TemplateProcessor;
import org.springframework.extensions.webscripts.WebScriptException;

import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StrongCacheStorage;
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
    private int updateDelay = 1;


    /* (non-Javadoc)
     * @see org.alfresco.repo.template.FreeMarkerProcessor#setDefaultEncoding(java.lang.String)
     */
    @Override
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
     * @param updateDelay the time in seconds between checks on the modified date for cached templates
     */
    public void setUpdateDelay(int updateDelay)
    {
        this.updateDelay = updateDelay;
    }
    
    /**
     * @deprecated
     * @param cacheSize not used anymore
     */
    @Deprecated
    public void setCacheSize(int cacheSize)
    {
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
        config.setCacheStorage(new StrongCacheStorage());
        config.setTemplateUpdateDelay(updateDelay);
        
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
