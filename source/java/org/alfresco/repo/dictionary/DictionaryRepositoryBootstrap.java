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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;


/**
 * Bootstrap the dictionary from specified locations within the repository
 * 
 * @author Roy Wetherall
 */
public class DictionaryRepositoryBootstrap
{   
    /** Loactions in the respository fro which models should be loaded */
    private List<RepositoryLocation> repositoryLocations = new ArrayList<RepositoryLocation>();

    /** Dictionary DAO */
    private DictionaryDAO dictionaryDAO = null;
    
    /** Search service */
    private SearchService searchService;
    
    /** The content service */
    private ContentService contentService;
    
    /** The transaction service */
    private TransactionService transactionService;
    
    /** The authentication component */
    private AuthenticationComponent authenticationComponent;
      
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
     * Set the search search service
     * 
     * @param searchService     the search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /** 
     * Set the content service
     * 
     * @param contentService    the content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * Set the transaction service
     * 
     * @param transactionService    the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    /**
     * Set the authentication service
     * 
     * @param authenticationComponent   the authentication component
     */
    public void setAuthenticationComponent(
            AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }
        
    /**
     * Set the respository locations
     * 
     * @param repositoryLocations   list of the repository locaitons
     */
    public void setRepositoryLocations(
            List<RepositoryLocation> repositoryLocations)
    {
        this.repositoryLocations = repositoryLocations;
    }
    
    @SuppressWarnings("unchecked")
    public void bootstrap()
    {
        TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionUtil.TransactionWork()
        {
            public Object doWork() throws Exception
            {
                DictionaryRepositoryBootstrap.this.authenticationComponent.setCurrentUser(
                        DictionaryRepositoryBootstrap.this.authenticationComponent.getSystemUserName());
                try
                {
                    bootstrapImpl();
                }
                finally
                {
                    DictionaryRepositoryBootstrap.this.authenticationComponent.clearCurrentSecurityContext();
                }
                return null;
            }
        });
    }
    
    /**
     * Bootstrap the Dictionary
     */
    public void bootstrapImpl()
    {
        Map<String, M2Model> modelMap = new HashMap<String, M2Model>();
        
        // Register the models found in the respository
        for (RepositoryLocation repositoryLocation : this.repositoryLocations)
        {
            ResultSet resultSet = this.searchService.query(repositoryLocation.getStoreRef(), SearchService.LANGUAGE_LUCENE, repositoryLocation.getQueryStatement());
            for (NodeRef dictionaryModel : resultSet.getNodeRefs())
            {
                M2Model model = createM2Model(dictionaryModel);
                if (model != null)
                {
                    for (M2Namespace namespace : model.getNamespaces())
                    {
                        modelMap.put(namespace.getUri(), model);
                    } 
                }
            }
        }
        
        // Load the models ensuring that they are loaded in the correct order
        List<String> loadedModels = new ArrayList<String>();
        for (Map.Entry<String, M2Model> entry : modelMap.entrySet())
        {
            loadModel(modelMap, loadedModels, entry.getValue());
        }
    }
    
    /**
     * Loads a model (and it dependants) if it does not exist in the list of loaded models.
     * 
     * @param modelMap          a map of the models to be loaded
     * @param loadedModels      the list of models already loaded
     * @param model             the model to try and load
     */
    private void loadModel(Map<String, M2Model> modelMap, List<String> loadedModels, M2Model model)
    {
        String modelName = model.getName();
        if (loadedModels.contains(modelName) == false)
        {
            for (M2Namespace importNamespace : model.getImports())
            {
                M2Model importedModel = modelMap.get(importNamespace.getUri());
                if (importedModel != null)
                {
                    // Ensure that the imported model is loaded first
                    loadModel(modelMap, loadedModels, importedModel);
                }
                // else we can assume that the imported model is already loaded, if this not the case then
                //      an error will be raised during compilation
            }
            
            dictionaryDAO.putModel(model);
            loadedModels.add(modelName);
        }        
    }

    /**
     * Create a M2Model from a dictionary model node
     * 
     * @param nodeRef   the dictionary model node reference
     * @return          the M2Model
     */
    public M2Model createM2Model(NodeRef nodeRef)
    {
        M2Model model = null;
        ContentReader contentReader = this.contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        if (contentReader != null)
        {
            model = M2Model.createModel(contentReader.getContentInputStream());
        }
        // TODO should we inactivate the model node and put the error somewhere??
        return model;
    }

    /**
     * Repositotry location object, defines a location in the repository from within which dictionary models should be loaded
     * for inclusion in the data dictionary.
     * 
     * @author Roy Wetherall
     */
    public class RepositoryLocation
    {
        /** Store protocol */
        private String storeProtocol; 
        
        /** Store identifier */
        private String storeId;
        
        /** Path */
        private String path;
        
        /** 
         * Set the store protocol
         * 
         * @param storeProtocol     the store protocol
         */
        public void setStoreProtocol(String storeProtocol)
        {
            this.storeProtocol = storeProtocol;
        }
        
        /**
         * Set the store identifier
         * 
         * @param storeId       the store identifier
         */
        public void setStoreId(String storeId)
        {
            this.storeId = storeId;
        }
        
        /**
         * Set the path
         * 
         * @param path  the path
         */
        public void setPath(String path)
        {
            this.path = path;
        }
        
        /**
         * Get the store reference
         * 
         * @return  the store reference
         */
        public StoreRef getStoreRef()
        {
            return new StoreRef(this.storeProtocol, this.storeId);
        }
        
        /**
         * Get the query statement, based on the path
         * 
         * @return  the query statement
         */
        public String getQueryStatement()
        {
            String result = "+TYPE:\"" + ContentModel.TYPE_DICTIONARY_MODEL.toString() + "\"";
            if (this.path != null)
            {
                result += " +PATH:\"" + this.path + "\"";
            }
            return result;
        }
    }
}
