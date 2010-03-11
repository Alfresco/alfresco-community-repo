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

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.scripts.ScriptException;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.springframework.extensions.webscripts.MultiScriptLoader;
import org.springframework.extensions.webscripts.ScriptContent;
import org.springframework.extensions.webscripts.ScriptLoader;
import org.springframework.extensions.webscripts.ScriptProcessor;
import org.springframework.extensions.webscripts.SearchPath;
import org.springframework.extensions.webscripts.Store;
import org.springframework.extensions.webscripts.WebScriptException;


/**
 * Repository (server-tier) Web Script Processor
 * 
 * @author davidc
 */
public class RepositoryScriptProcessor implements ScriptProcessor
{
    // dependencies
    protected ScriptService scriptService;
    protected ScriptLoader scriptLoader;
    protected SearchPath searchPath;

    // Javascript Converter
    private final ValueConverter valueConverter = new ValueConverter();

    
    /**
     * @param scriptService
     */
    public void setScriptService(ScriptService scriptService)
    {
        this.scriptService = scriptService;
    }

    /**
     * @param searchPath
     */
    public void setSearchPath(SearchPath searchPath)
    {
        this.searchPath = searchPath;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ScriptProcessor#findScript(java.lang.String)
     */
    public ScriptContent findScript(String path)
    {
        return scriptLoader.getScript(path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ScriptProcessor#executeScript(java.lang.String, java.util.Map)
     */
    public Object executeScript(String path, Map<String, Object> model)
        throws ScriptException
    {
        // locate script within web script stores
        ScriptContent scriptContent = findScript(path);
        if (scriptContent == null)
        {
            throw new WebScriptException("Unable to locate script " + path);
        }
        // execute script
        return executeScript(scriptContent, model);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ScriptProcessor#executeScript(org.alfresco.web.scripts.ScriptContent, java.util.Map)
     */
    public Object executeScript(ScriptContent content, Map<String, Object> model)
    {
        return scriptService.executeScript("javascript", new RepositoryScriptLocation(content), model);        
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ScriptProcessor#unwrapValue(java.lang.Object)
     */
    public Object unwrapValue(Object value)
    {
        return valueConverter.convertValueForJava(value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ScriptProcessor#reset()
     */
    public void reset()
    {
        init();
        this.scriptService.resetScriptProcessors();
    }
    
    /**
     * Register script loader from each Web Script Store with Script Processor
     */
    private void init()
    {
        List<ScriptLoader> loaders = new ArrayList<ScriptLoader>();
        for (Store apiStore : searchPath.getStores())
        {
            ScriptLoader loader = apiStore.getScriptLoader();
            if (loader == null)
            {
                throw new WebScriptException("Unable to retrieve script loader for Web Script store " + apiStore.getBasePath());
            }
            loaders.add(loader);
        }
        scriptLoader = new MultiScriptLoader(loaders.toArray(new ScriptLoader[loaders.size()]));
    }

    
    /**
     * Script Location Facade
     */
    private static class RepositoryScriptLocation implements ScriptLocation
    {
        private ScriptContent content;
        
        private RepositoryScriptLocation(ScriptContent content)
        {
            this.content = content;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.repository.ScriptLocation#getInputStream()
         */
        public InputStream getInputStream()
        {
            return content.getInputStream();
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.repository.ScriptLocation#getReader()
         */
        public Reader getReader()
        {
            return content.getReader();
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.repository.ScriptLocation#isCachable()
         */
        public boolean isCachable()
        {
            return content.isCachable();
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.repository.ScriptLocation#isSecure()
         */
        public boolean isSecure()
        {
            return content.isSecure();
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.repository.ScriptLocation#getPath()
         */
        public String getPath()
        {
            return content.getPath();
        }
        
        @Override
        public String toString()
        {
        	return content.getPathDescription();
        }
    }
}