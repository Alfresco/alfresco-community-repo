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
package org.alfresco.web.scripts.facebook;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.web.scripts.WebScriptContext;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


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
    private WebScriptRegistry registry;
    private WebScriptContext context;
    

    /**
     * @param registry  Web Script Registry
     */
    public void setRegistry(WebScriptRegistry registry)
    {
        this.registry = registry;
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
                        String appPath = "/com/facebook/_apps/app." + apiKey + ".js";
                        ScriptLocation appScript = registry.getScriptProcessor().findScript(appPath);
                        if (appScript == null)
                        {
                            throw new WebScriptException("Unable to locate application initialisation script '" + appPath + "'");
                        }
                        
                        // Execute app initialisation script
                        Map<String, Object> model = new HashMap<String, Object>();
                        FacebookAppModel app = new FacebookAppModel(apiKey);
                        model.put("app", app);
                        registry.getScriptProcessor().executeScript(appScript, model);

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
