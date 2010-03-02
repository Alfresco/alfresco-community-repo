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
package org.alfresco.repo.web.scripts.facebook;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Container;
import org.springframework.extensions.webscripts.ScriptContent;
import org.springframework.extensions.webscripts.ScriptProcessor;
import org.springframework.extensions.webscripts.WebScriptException;


/**
 * Facebook Service
 * 
 * @author davidc
 */
public class FacebookService
{
    // Logger
    private static final Log logger = LogFactory.getLog(FacebookService.class);
    
    // Facebook Application Cache
    private Map<String, FacebookAppModel> apps = new HashMap<String, FacebookAppModel>();    
    private ReentrantReadWriteLock appsLock = new ReentrantReadWriteLock(); 

    // Component dependencies
    private Container container;
    

    /**
     * @param registry  Web Script Registry
     */
    public void setContainer(Container container)
    {
        this.container = container;
    }

    /**
     * Gets the application model for the given application api key
     * 
     * @param apiKey  api key
     * @return  application model
     */
    FacebookAppModel getAppModel(String apiKey)
    {
        FacebookAppModel facebookApp = null;
        appsLock.readLock().lock();

        try
        {
            facebookApp = apps.get(apiKey);
            if (facebookApp == null)
            {
                // Upgrade read lock to write lock
                appsLock.readLock().unlock();
                appsLock.writeLock().lock();

                try
                {
                    // Check again
                    facebookApp = apps.get(apiKey);
                    if (facebookApp == null)
                    {
                        if (logger.isDebugEnabled())
                            logger.debug("Initialising Facebook Application '" + apiKey + "'");
                        
                        // Locate app initialisation script in web script store
                        String appPath = "com/facebook/_apps/app." + apiKey + ".js";
                        String validScriptPath = container.getScriptProcessorRegistry().findValidScriptPath(appPath);
                        if (validScriptPath == null)
                        {
                            throw new WebScriptException("Unable to locate application initialisation script '" + appPath + "'");
                        }
                        ScriptProcessor scriptProcessor = container.getScriptProcessorRegistry().getScriptProcessor(validScriptPath);
                        ScriptContent appScript = scriptProcessor.findScript(validScriptPath);
                        if (appScript == null)
                        {
                            throw new WebScriptException("Unable to locate application initialisation script '" + appPath + "'");
                        }
                        
                        // Execute app initialisation script
                        Map<String, Object> model = new HashMap<String, Object>();
                        FacebookAppModel app = new FacebookAppModel(apiKey);
                        model.put("app", app);
                        scriptProcessor.executeScript(appScript, model);
                        
                        // Validate initialisation
                        if (app.getSecret() == null)
                        {
                            throw new WebScriptException("Secret key for application '" + apiKey + "' has not been specified.");
                        }
                        if (app.getApiKey() == null)
                        {
                            throw new WebScriptException("Application Id for application '" + apiKey + "' has not been specified.");
                        }

                        apps.put(apiKey, app);
                        facebookApp = app;
                    }
                }
                finally
                {
                    // Downgrade lock to read
                    appsLock.readLock().lock();
                    appsLock.writeLock().unlock();
                }
            }
            return facebookApp;
        }
        finally
        {
            appsLock.readLock().unlock();
        }
    }

    /**
     * Gets currently known Facebook Applications
     * 
     * @return  map (name, application) of known applications
     */
    public Map<String, FacebookAppModel> getAppModels()
    {
        return apps;
    }

    /**
     * Reset Facebook Service
     */
    public void reset()
    {
        appsLock.writeLock().lock();
        try
        {
            apps.clear();
        }
        finally
        {
            appsLock.writeLock().unlock();
        }
    }

}
