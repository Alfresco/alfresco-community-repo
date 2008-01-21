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

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.service.cmr.repository.ScriptException;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.web.scripts.MultiScriptLoader;
import org.alfresco.web.scripts.ScriptContent;
import org.alfresco.web.scripts.ScriptLoader;
import org.alfresco.web.scripts.ScriptProcessor;
import org.alfresco.web.scripts.SearchPath;
import org.alfresco.web.scripts.Store;
import org.alfresco.web.scripts.WebScriptException;


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
    private ValueConverter valueConverter = new ValueConverter();

    
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
        return (value instanceof Serializable) ? valueConverter.convertValueForRepo((Serializable)value) : value;
	}

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ScriptProcessor#reset()
     */
    public void reset()
    {
        init();
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
        
        @Override
        public String toString()
        {
        	return content.getPathDescription();
        }
    }

}
