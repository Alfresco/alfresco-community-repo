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
package org.alfresco.repo.dictionary;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


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

    // Logger
    private static Log logger = LogFactory.getLog(DictionaryBootstrap.class);
    
    
    /**
     * Sets the Dictionary DAO
     * 
     * @param dictionaryDAO
     */
    public void setDictionaryDAO(DictionaryDAO dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }
    
    /**
     * Sets the Tenant Service
     * 
     * @param tenantService
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
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
     * @param modelResources the model names
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
    public void register()
    {
        dictionaryDAO.register(this);
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
        
        Collection<QName> modelsBefore = dictionaryDAO.getModels(); // note: on first bootstrap will init empty dictionary
        int modelsBeforeCnt = (modelsBefore != null ? modelsBefore.size() : 0);
        
        if ((tenantService == null) || (! tenantService.isTenantUser()))
        {
            // register models
            for (String bootstrapModel : models)
            {
                InputStream modelStream = getClass().getClassLoader().getResourceAsStream(bootstrapModel);
                if (modelStream == null)
                {
                    throw new DictionaryException("Could not find bootstrap model " + bootstrapModel);
                }
                try
                {
                    M2Model model = M2Model.createModel(modelStream);
                    
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Loading model: "+model.getName()+" (from "+bootstrapModel+")");
                    }
                    
                    dictionaryDAO.putModel(model);
                }
                catch(DictionaryException e)
                {
                    throw new DictionaryException("Could not import bootstrap model " + bootstrapModel, e);
                }
            }
            
            Collection<QName> modelsAfter = dictionaryDAO.getModels();
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
