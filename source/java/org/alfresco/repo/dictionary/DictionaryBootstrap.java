/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.dictionary;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Bootstrap Dictionary DAO with pre-defined models
 * 
 * @author David Caruana
 *
 */
public class DictionaryBootstrap
{
    // The list of models to bootstrap with
    private List<String> models = new ArrayList<String>();

    // The list of model resource bundles to bootstrap with
    private List<String> resourceBundles = new ArrayList<String>();

    // Dictionary DAO
    private DictionaryDAO dictionaryDAO = null;

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
     * Bootstrap the Dictionary
     */
    public void bootstrap()
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
        
        // register models
        for (String resourceBundle : resourceBundles)
        {
            I18NUtil.registerResourceBundle(resourceBundle);
        }
    }

}
