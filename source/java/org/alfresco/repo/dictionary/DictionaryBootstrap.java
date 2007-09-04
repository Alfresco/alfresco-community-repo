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
package org.alfresco.repo.dictionary;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Bootstrap Dictionary DAO with pre-defined models & message resources (from classpath)
 * 
 * @author David Caruana
 *
 */
public class DictionaryBootstrap implements DictionaryDeployer
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
    private static Log logger = LogFactory.getLog(DictionaryDAO.class);
    
    
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
        initDictionary();
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

    /**
     * Populate the Dictionary
     */
    public void initDictionary()
    {
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
                    if (logger.isInfoEnabled())
                        logger.info("Loading model from " + bootstrapModel);
                    
                    M2Model model = M2Model.createModel(modelStream);
                    dictionaryDAO.putModel(model);
                }
                catch(DictionaryException e)
                {
                    throw new DictionaryException("Could not import bootstrap model " + bootstrapModel, e);
                }
            }
        }
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
