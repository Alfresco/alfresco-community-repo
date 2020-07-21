/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.dictionary;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;


/**
 * Bootstrap Dictionary DAO with pre-defined models & message resources (from classpath)
 * 
 * @author David Caruana, janv
 *
 */
public class DictionaryBootstrap implements DictionaryListener
{
    // The list of models to bootstrap with
    private List<String> models = new ArrayList<String>();

    // The list of model resource bundles to bootstrap with
    private List<String> resourceBundles = new ArrayList<String>();

    // Dictionary DAO
    private DictionaryDAO dictionaryDAO = null;
    
    // Tenant Service
    private TenantService tenantService;

    private Properties globalProperties;

    // Logger
    private static Log logger = LogFactory.getLog(DictionaryBootstrap.class);
    
    
    /**
     * Sets the Dictionary DAO
     * 
     * @param dictionaryDAO DictionaryDAO
     */
    public void setDictionaryDAO(DictionaryDAO dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }
    
    /**
     * Sets the Tenant Service
     * 
     * @param tenantService TenantService
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /**
     * Sets the global properties
     * 
     * @param globalProperties
     */
    public void setGlobalProperties(Properties globalProperties)
    {
        this.globalProperties = globalProperties;
    }
    
    /**
     * Sets the initial list of models to bootstrap with
     * 
     * @param modelResources the model names
     */
    public void setModels(List<String> modelResources)
    {
        this.models = modelResources;
    }
    
    /**
     * Sets the initial list of models to bootstrap with
     * 
     * @param labels the labels
     */
    public void setLabels(List<String> labels)
    {
        this.resourceBundles = labels;
    }
    
    /**
     * Bootstrap the Dictionary - register and populate
     * 
     */
    public void bootstrap()
    {
        onDictionaryInit();
        initStaticMessages();
        
        register();
    }
    
    /**
     * Register with the Dictionary
     */
    private void register()
    {
        dictionaryDAO.registerListener(this);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryListener#onInit()
     */
    public void onDictionaryInit()
    {
        long startTime = System.currentTimeMillis();
        
        if (logger.isTraceEnabled())
        {
            logger.trace("onDictionaryInit: ["+Thread.currentThread()+"]");
        }

        // note: on first bootstrap will init empty dictionary
        Collection<QName> modelsBefore = dictionaryDAO.getModels(true); // note: on first bootstrap will init empty dictionary
        int modelsBeforeCnt = (modelsBefore != null ? modelsBefore.size() : 0);
        
        if ((tenantService == null) || (! tenantService.isTenantUser()))
        {
            // register models
            for (String bootstrapModel : models)
            {
                InputStream modelStream = getClass().getClassLoader().getResourceAsStream(bootstrapModel);
                if (modelStream == null)
                {
                    throw new DictionaryException("d_dictionary.bootstrap.model_not_found", bootstrapModel);
                }
                try
                {
                    M2Model model = M2Model.createModel(modelStream);
                    model.setConfigProperties(globalProperties);
                    
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Loading model: "+model.getName()+" (from "+bootstrapModel+")");
                    }

                    dictionaryDAO.putModel(model);
                }
                catch(DictionaryException e)
                {
                    throw new DictionaryException("d_dictionary.bootstrap.model_not_imported", e, bootstrapModel);
                }
                finally
                {
                    try
                    {
                        modelStream.close();
                    } 
                    catch (IOException ioe)
                    {
                        logger.warn("Failed to close model input stream for '"+bootstrapModel+"': "+ioe);
                    }
                }
            }
            
            Collection<QName> modelsAfter = dictionaryDAO.getModels(true);
            int modelsAfterCnt = (modelsAfter != null ? modelsAfter.size() : 0);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Model count: before="+modelsBeforeCnt+", load="+models.size()+", after="+modelsAfterCnt+" in "+(System.currentTimeMillis()-startTime)+" msecs ["+Thread.currentThread()+"]");
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryListener#afterInit()
     */
    public void afterDictionaryInit()
    {
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryListener#onDictionaryDestroy()
     */
    public void afterDictionaryDestroy()
    {
    }
    
    /**
     * Register the static resource bundles
     */
    private void initStaticMessages()
    {
        // register messages
        for (String resourceBundle : resourceBundles)
        {
            I18NUtil.registerResourceBundle(resourceBundle);
        }
    }
}
