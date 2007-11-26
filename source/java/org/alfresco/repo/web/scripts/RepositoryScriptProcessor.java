package org.alfresco.repo.web.scripts;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.springframework.beans.factory.InitializingBean;


public class RepositoryScriptProcessor implements ScriptProcessor, InitializingBean
{
    // dependencies
    protected ScriptService scriptService;
    protected ScriptLoader scriptLoader;
    protected SearchPath searchPath;

    
    /**
     * Sets the script service
     * 
     * @param scriptService
     */
    public void setScriptService(ScriptService scriptService)
    {
        this.scriptService = scriptService;
    }

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
     * @see org.alfresco.web.scripts.ScriptProcessor#reset()
     */
    public void reset()
    {
        // NOOP
    }
    
    
    private static class RepositoryScriptLocation implements ScriptLocation
    {
        private ScriptContent content;
        
        private RepositoryScriptLocation(ScriptContent content)
        {
            this.content = content;
        }
        
        public InputStream getInputStream()
        {
            return content.getInputStream();
        }

        public Reader getReader()
        {
            return content.getReader();
        }
    }
    
    /**
     * Register script loader from each Web Script Store with Script Processor
     */
    public void afterPropertiesSet()
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

}
