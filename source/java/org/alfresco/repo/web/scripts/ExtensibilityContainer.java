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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.springframework.extensions.config.ConfigImpl;
import org.springframework.extensions.config.ConfigSection;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.config.evaluator.Evaluator;
import org.springframework.extensions.config.xml.XMLConfigService;
import org.springframework.extensions.config.xml.elementreader.ConfigElementReader;
import org.springframework.extensions.surf.extensibility.BasicExtensionModule;
import org.springframework.extensions.surf.extensibility.ExtensibilityModel;
import org.springframework.extensions.surf.extensibility.HandlesExtensibility;
import org.springframework.extensions.surf.extensibility.WebScriptExtensibilityModuleHandler;
import org.springframework.extensions.surf.extensibility.impl.ExtensibilityModelImpl;
import org.springframework.extensions.surf.extensibility.impl.MarkupDirective;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.ExtendedScriptConfigModel;
import org.springframework.extensions.webscripts.ExtendedTemplateConfigModel;
import org.springframework.extensions.webscripts.ScriptConfigModel;
import org.springframework.extensions.webscripts.TemplateConfigModel;
import org.springframework.extensions.webscripts.WebScriptPropertyResourceBundle;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * <p>A simple extensibility {@link Container} for processing WebScripts. This extends the {@link RepositoryContainer} and
 * implements the {@link HandlesExtensibility} interface to provide extensibility capabilities.</p>
 * 
 * @author David Draper
 */
public class ExtensibilityContainer extends RepositoryContainer implements HandlesExtensibility
{
    private static final Log logger = LogFactory.getLog(ExtensibilityContainer.class);
    
    /**
     * <p>Opens a new {@link ExtensibilityModel}, defers execution to the extended {@link RepositoryContainer} and
     * then closes the {@link ExtensibilityModel}.</p>
     */
    @Override
    public void executeScript(WebScriptRequest scriptReq, 
                              WebScriptResponse scriptRes, 
                              Authenticator auth) throws IOException
    {
        ExtensibilityModel extModel = this.openExtensibilityModel();
        try
        {
            super.executeScript(scriptReq, scriptRes, auth);
        }
        finally
        {
            // It's only necessary to close the model if it's actually been used. Not all WebScripts will make use of the
            // model. An example of this would be the StreamContent WebScript. It is important not to attempt to close
            // an unused model since the WebScript executed may have already flushed the response if it has overridden
            // the default .execute() method.
            if (this.modelUsed.get())
            {
                try
                {
                    this.closeExtensibilityModel(extModel, scriptRes.getWriter());
                }
                catch (IOException e)
                {
                    logger.error("An error occurred getting the Writer when closing an ExtensibilityModel", e);
                }
            }
        }
    }

    /**
     * <p>This keeps track of whether or not the {@link ExtensibilityModel} for the current thread has been used. The
     * thread local value will only be set to <code>true</code> if the <code>getCurrentExtensibilityModel</code> method
     * is called.</p>
     */
    private ThreadLocal<Boolean> modelUsed = new ThreadLocal<Boolean>();
    
    /**
     * <p>A {@link WebScriptExtensibilityModuleHandler} is required for retrieving information on what
     * {@link BasicExtensionModule} instances have been configured and the extension files that need 
     * to be processed. This variable should be set thorugh the Spring application context configuration.</p>
     */
    private WebScriptExtensibilityModuleHandler extensibilityModuleHandler = null;
    
    /**
     * <p>Sets the {@link WebScriptExtensibilityModuleHandler} for this {@link Container}.</p>
     * @param extensibilityModuleHandler
     */
    public void setExtensibilityModuleHandler(WebScriptExtensibilityModuleHandler extensibilityModuleHandler)
    {
        this.extensibilityModuleHandler = extensibilityModuleHandler;
    }

    /**
     * <p>Maintains a list of all the {@link ExtensibilityModel} instances being used across all the 
     * available threads.</p>
     */
    private ThreadLocal<ExtensibilityModel> extensibilityModel = new ThreadLocal<ExtensibilityModel>();
    
    /**
     * <p>Creates a new {@link ExtensibilityModel} and sets it on the current thread
     */
    public ExtensibilityModel openExtensibilityModel()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Opening for thread: " + Thread.currentThread().getName());
        }
        this.extendedBundleCache.set(new HashMap<String, WebScriptPropertyResourceBundle>());
        this.evaluatedModules.set(null);
        this.fileBeingProcessed.set(null);
        this.globalConfig.set(null);
        this.sections.set(null);
        this.sectionsByArea.set(null);
        
        ExtensibilityModel model = new ExtensibilityModelImpl(null, this);
        this.extensibilityModel.set(model); 
        this.modelUsed.set(Boolean.FALSE);
        return model;
    }

    /**
     * <p>Flushes the {@link ExtensibilityModel} provided and sets its parent as the current {@link ExtensibilityModel}
     * for the current thread.<p>
     */
    public void closeExtensibilityModel(ExtensibilityModel model, Writer out)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Closing for thread: " + Thread.currentThread().getName());
        }
        
        model.flushModel(out);
        this.modelUsed.set(Boolean.FALSE);
        this.extensibilityModel.set(null);
    }

    /**
     * <p>Returns the {@link ExtensibilityModel} for the current thread.</p>
     */
    public ExtensibilityModel getCurrentExtensibilityModel()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Getting current for thread: " + Thread.currentThread().getName());
        }
        this.modelUsed.set(Boolean.TRUE);
        return this.extensibilityModel.get();
    }

    /**
     * <p>This method is implemented to perform no action as it is not necessary for a standalone WebScript
     * container to add dependencies for processing.</p>
     */
    public void updateExtendingModuleDependencies(String pathBeingProcessed, Map<String, Object> model)
    {
        // NOT REQUIRED FOR STANDALONE WEBSCRIPT CONTAINER
    }

    /**
     * <p>A thread-safe cache of extended {@link ResourceBundle} instances for the current request.</p>
     */
    private ThreadLocal<Map<String, WebScriptPropertyResourceBundle>> extendedBundleCache = new ThreadLocal<Map<String, WebScriptPropertyResourceBundle>>(); 
    
    /**
     * <p>Checks the cache to see if it has cached an extended bundle (that is a basic {@link ResourceBundle} that
     * has had extension modules applied to it. Extended bundles can only be safely cached once per request as the modules
     * applied can vary for each request.</p>
     * 
     * @param webScriptId The id of the WebScript to retrieve the extended bundle for.
     * @return A cached bundle or <code>null</code> if the bundle has not previously been cached.
     */
    public ResourceBundle getCachedExtendedBundle(String webScriptId)
    {
        ResourceBundle cachedExtendedBundle = null;
        Map<String, WebScriptPropertyResourceBundle> threadLocal = this.extendedBundleCache.get();
        if (threadLocal != null)
        {
            cachedExtendedBundle = this.extendedBundleCache.get().get(webScriptId);
        }
        return cachedExtendedBundle;
    }
    
    /**
     * <p>Adds a new extended bundle to the cache. An extended bundle is a WebScript {@link ResourceBundle} that has had 
     * {@link ResourceBundle} instances merged into it from extension modules that have been applied. These can only be cached 
     * for the lifetime of the request as different modules may be applied to the same WebScript for different requests.</p>
     * 
     * @param webScriptId The id of the WebScript to cache the extended bundle against.
     * @param extensionBUndle The extended bundle to cache.
     */
    public void addExtensionBundleToCache(String webScriptId, WebScriptPropertyResourceBundle extensionBundle)
    {
        Map<String, WebScriptPropertyResourceBundle> threadLocal = this.extendedBundleCache.get();
        if (threadLocal == null)
        {
            // This should never be the case because when a new model is opened this value should be reset
            // but we will double-check to avoid the potential of NPEs...
            threadLocal = new HashMap<String, WebScriptPropertyResourceBundle>();
            this.extendedBundleCache.set(threadLocal);
        }
        threadLocal.put(webScriptId, extensionBundle);
    }
    
    /**
     * <p>A {@link ThreadLocal} reference to the file currently being processed in the model.
     */
    private ThreadLocal<String> fileBeingProcessed = new ThreadLocal<String>();
    
    /**
     * <p>Returns the path of the file currently being processed in the model by the current thread. 
     * This information is primarily provided for the purposes of generating debug information.</p>
     * 
     * @return The path of the file currently being processed.
     */
    public String getFileBeingProcessed()
    {
        return this.fileBeingProcessed.get();
    }

    /**
     * <p>Sets the path of the file currently being processed in the model by the current thread. 
     * This information should be collected to assist with providing debug information.</p>
     * @param file The path of the file currently being processed.
     */
    public void setFileBeingProcessed(String file)
    {
        this.fileBeingProcessed.set(file);
    }
    
    /**
     * <p>Retrieves an files for the evaluated modules that are extending the WebScript files being processed.</p>
     */
    public List<String> getExtendingModuleFiles(String pathBeingProcessed)
    {
        List<String> extendingModuleFiles = new ArrayList<String>();
        for (BasicExtensionModule module: this.getEvaluatedModules())
        {
            extendingModuleFiles.addAll(this.extensibilityModuleHandler.getExtendingModuleFiles(module, pathBeingProcessed));
        }
        return extendingModuleFiles;
    }

    /**
     * <p>The list of {@link ExtensionModule} instances that have been evaluated as applicable to
     * this RequestContext. This is set to <code>null</code> when during instantiation and is only
     * properly set the first time the <code>getEvaluatedModules</code> method is invoked. This ensures
     * that module evaluation only occurs once per request.</p>
     */
    private ThreadLocal<List<BasicExtensionModule>> evaluatedModules = new ThreadLocal<List<BasicExtensionModule>>();
    
    /**
     * <p>Retrieve the list of {@link ExtensionModule} instances that have been evaluated as applicable
     * for the current request. If this list has not yet been populated then use the {@link ExtensibilityModuleHandler}
     * configured in the Spring application context to evaluate them.</p>
     * 
     * @return A list of {@link ExtensionModule} instances that are applicable to the current request.
     */
    public List<BasicExtensionModule> getEvaluatedModules()
    {
        List<BasicExtensionModule> evaluatedModules = this.evaluatedModules.get();
        if (evaluatedModules == null)
        {
            if (this.extensibilityModuleHandler == null)
            {
                if (logger.isErrorEnabled())
                {
                    logger.error("No 'extensibilityModuleHandler' has been configured for this request context. Extensions cannot be processed");
                }
                evaluatedModules = new ArrayList<BasicExtensionModule>();
                this.evaluatedModules.set(evaluatedModules);
            }
            else
            {
                evaluatedModules = this.extensibilityModuleHandler.getExtensionModules();
                this.evaluatedModules.set(evaluatedModules);
            }
        }
        return evaluatedModules;
    }
    
    /**
     * <p>This is a local {@link ConfigImpl} instance that will only be used when extension modules are employed. It will
     * initially be populated with the default "static" global configuration taken from the {@link ConfigService} associated
     * with this {@link RequestContext} but then updated to include global configuration provided by extension modules that
     * have been evaluated to be applied to the current request.</p>
     */
    private ThreadLocal<ConfigImpl> globalConfig = new ThreadLocal<ConfigImpl>();
    
    /**
     * <p>This map represents {@link ConfigSection} instances mapped by area. It  will only be used when extension modules are 
     * employed. It will initially be populated with the default "static" configuration taken from the {@link ConfigService} associated
     * with this {@link RequestContext} but then updated to include configuration provided by extension modules that have been evaluated 
     * to be applied to the current request.</p>
     */
    private ThreadLocal<Map<String, List<ConfigSection>>> sectionsByArea = new ThreadLocal<Map<String,List<ConfigSection>>>();
    
    /**
     * <p>A list of {@link ConfigSection} instances that are only applicable to the current request. It  will only be used when extension modules are 
     * employed. It will initially be populated with the default "static" configuration taken from the {@link ConfigService} associated
     * with this {@link RequestContext} but then updated to include configuration provided by extension modules that have been evaluated 
     * to be applied to the current request.</p>
     */
    private ThreadLocal<List<ConfigSection>> sections = new ThreadLocal<List<ConfigSection>>();
    
    /**
     * <p>Creates a new {@link ExtendedScriptConfigModel} instance using the local configuration generated for this request.
     * If configuration for the request will be generated if it does not yet exist. It is likely that this method will be
     * called multiple times within the context of a single request and although the configuration containers will always
     * be the same a new {@link ExtendedScriptConfigModel} instance will always be created as the the supplied <code>xmlConfig</code>
     * string could be different for each call (because each WebScript invoked in the request will supply different
     * configuration.</p>
     */
    public ScriptConfigModel getExtendedScriptConfigModel(String xmlConfig)
    {
        if (this.globalConfig.get() == null && this.sectionsByArea.get() == null && this.sections.get() == null)
        {
            this.getConfigExtensions();
        }
        return new ExtendedScriptConfigModel(getConfigService(), xmlConfig, this.globalConfig.get(), this.sectionsByArea.get(), this.sections.get());
    }
    
    /**
     * <p>Creates a new {@link TemplateConfigModel} instance using the local configuration generated for this request.
     * If configuration for the request will be generated if it does not yet exist. It is likely that this method will be
     * called multiple times within the context of a single request and although the configuration containers will always
     * be the same a new {@link TemplateConfigModel} instance will always be created as the the supplied <code>xmlConfig</code>
     * string could be different for each call (because each WebScript invoked in the request will supply different
     * configuration.</p>
     */
    public TemplateConfigModel getExtendedTemplateConfigModel(String xmlConfig)
    {
        if (this.globalConfig.get() == null && this.sectionsByArea.get() == null && this.sections.get() == null)
        {
            this.getConfigExtensions();
        }
        return new ExtendedTemplateConfigModel(getConfigService(), xmlConfig, this.globalConfig.get(), this.sectionsByArea.get(), this.sections.get());
    }
    
    /**
     * <p>Creates and populates the request specific configuration container objects (<code>globalConfig</code>, <code>sectionsByArea</code> & 
     * <code>sections</code> with a combination of the default static configuration (taken from files accessed by the {@link ConfigService}) and
     * dynamic configuration taken from extension modules evaluated for the current request. </p>  
     */
    private void getConfigExtensions()
    {
        // Extended configuration is only possible if config service is an XMLConfigService...
        // 
        // ...also, it's only necessary to populate the configuration containers if they have not already been populated. This test should also
        // be carried out by the two methods ("getExtendedTemplateConfigModel" & "getExtendedTemplateConfigModel") to prevent duplication
        // of effort... but in case other methods attempt to access it we will make these additional tests. 
        if (getConfigService() instanceof XMLConfigService && this.globalConfig == null && this.sectionsByArea == null && this.sections == null)
        {
            // Cast the config service for ease of access
            XMLConfigService xmlConfigService = (XMLConfigService) getConfigService();
            
            // Get the current configuration from the ConfigService - we don't want to permanently pollute
            // the standard configuration with additions from the modules...
            this.globalConfig.set(new ConfigImpl((ConfigImpl)xmlConfigService.getGlobalConfig())); // Make a copy of the current global config
            
            // Initialise these with the config service values...
            this.sectionsByArea.set(new HashMap<String, List<ConfigSection>>(xmlConfigService.getSectionsByArea())); 
            this.sections.set(new ArrayList<ConfigSection>(xmlConfigService.getSections()));
            
            // Check to see if there are any modules that we need to apply...
            List<BasicExtensionModule> evaluatedModules = this.getEvaluatedModules();
            if (evaluatedModules != null && !evaluatedModules.isEmpty())
            {
                for (BasicExtensionModule currModule: evaluatedModules)
                {
                    for (Element currentConfigElement: currModule.getConfigurations())
                    {
                        // Set up containers for our request specific configuration - this will contain data taken from the evaluated modules...
                        Map<String, ConfigElementReader> parsedElementReaders = new HashMap<String, ConfigElementReader>();
                        Map<String, Evaluator> parsedEvaluators = new HashMap<String, Evaluator>();
                        List<ConfigSection> parsedConfigSections = new ArrayList<ConfigSection>();
                        
                        // Parse and process the parses configuration...
                        String currentArea = xmlConfigService.parseFragment(currentConfigElement, parsedElementReaders, parsedEvaluators, parsedConfigSections);
                        for (Map.Entry<String, Evaluator> entry : parsedEvaluators.entrySet())
                        {
                            // add the evaluators to the config service
                            parsedEvaluators.put(entry.getKey(), entry.getValue());
                        }
                        for (Map.Entry<String, ConfigElementReader> entry : parsedElementReaders.entrySet())
                        {
                            // add the element readers to the config service
                            parsedElementReaders.put(entry.getKey(), entry.getValue());
                        }
                        for (ConfigSection section : parsedConfigSections)
                        {
                            // Update local configuration with our updated data...
                            xmlConfigService.addConfigSection(section, currentArea, this.globalConfig.get(), this.sectionsByArea.get(), this.sections.get());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * <p>Adds the <{@code}@markup> directive to the container which allows FreeMarker templates to be extended.</p>
     */
    public void addExtensibilityDirectives(Map<String, Object> freeMarkerModel, ExtensibilityModel extModel)
    {
        MarkupDirective mud = new MarkupDirective("markup", extModel);
        freeMarkerModel.put("markup", mud);
    }
}
