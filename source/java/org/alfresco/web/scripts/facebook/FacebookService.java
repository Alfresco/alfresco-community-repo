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


public class FacebookService
{
    // Logger
    private static final Log logger = LogFactory.getLog(FacebookService.class);
    
    // Facebook Application Cache
    private Map<String, FacebookAppModel> apps = new HashMap<String, FacebookAppModel>();    
    private ReentrantReadWriteLock appsLock = new ReentrantReadWriteLock(); 

    private WebScriptRegistry registry;
    private WebScriptContext context;
    
    
    public void setRegistry(WebScriptRegistry registry)
    {
        this.registry = registry;
    }



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

    public Map<String, FacebookAppModel> getAppModels()
    {
        return apps;
    }
 
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
