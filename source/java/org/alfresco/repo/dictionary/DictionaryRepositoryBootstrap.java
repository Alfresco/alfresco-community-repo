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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageDeployer;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.tenant.TenantDeployerService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

/**
 * Bootstrap the dictionary from specified locations within the repository
 * 
 * @author Roy Wetherall, JanV
 */
public class DictionaryRepositoryBootstrap extends AbstractLifecycleBean implements TenantDeployer, DictionaryDeployer, MessageDeployer
{
    // Logging support
    private static Log logger = LogFactory
            .getLog("org.alfresco.repo.dictionary.DictionaryRepositoryBootstrap");

    /** Locations in the repository from which models should be loaded */
    private List<RepositoryLocation> repositoryModelsLocations = new ArrayList<RepositoryLocation>();

    /** Locations in the repository from which messages should be loaded */
    private List<RepositoryLocation> repositoryMessagesLocations = new ArrayList<RepositoryLocation>();

    /** Dictionary DAO */
    private DictionaryDAO dictionaryDAO = null;
    
    /** Search service */
    private SearchService searchService;
    
    /** The content service */
    private ContentService contentService;

    /** The node service */
    private NodeService nodeService;

    /** The tenant service */
    private TenantService tenantService;
    
    /** The tenant deployer service */
    private TenantDeployerService tenantDeployerService;

    /** The namespace service */
    private NamespaceService namespaceService;

    /** The message service */
    private MessageService messageService;

    /** The transaction service */
    private TransactionService transactionService;
      
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
     * Set the node service
     * 
     * @param nodeService   the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the tenant service
     * 
     * @param tenantService     the tenant service
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /**
     * Set the tenant admin service
     * 
     * @param tenantAdminService    the tenant admin service
     */
    public void setTenantDeployerService(TenantDeployerService tenantDeployerService)
    {
        this.tenantDeployerService = tenantDeployerService;
    }

    /**
     * Set the namespace service
     * 
     * @param namespaceService the namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * Set the message service
     * 
     * @param messageService    the message service
     */
    public void setMessageService(MessageService messageService)
    {
        this.messageService = messageService;
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
     * Set the repository models locations
     * 
     * @param repositoryModelsLocations   list of the repository models locations
     */    public void setRepositoryModelsLocations(
            List<RepositoryLocation> repositoryLocations)
    {
        this.repositoryModelsLocations = repositoryLocations;
    }
        
    /**
     * Set the repository messages (resource bundle) locations
     * 
     * @param repositoryLocations
     *            list of the repository messages locations
     */
    public void setRepositoryMessagesLocations(
            List<RepositoryLocation> repositoryLocations)
    {
        this.repositoryMessagesLocations = repositoryLocations;
    }


    /**
     * Initialise - after bootstrap of schema and tenant admin service
     */
    public void init()
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                initDictionary();
                initMessages();
                
                return (Object)null;
            }
        });
    }
    
    public void destroy()
    {    
        // NOOP - will be destroyed directly via DictionaryComponent
    }
    
    public void initDictionary()
    {
        if (this.repositoryModelsLocations != null)
        {            
            Map<String, M2Model> modelMap = new HashMap<String, M2Model>();
    
            // Register the models found in the repository
    
            for (RepositoryLocation repositoryLocation : this.repositoryModelsLocations)
            {
                StoreRef storeRef = repositoryLocation.getStoreRef();
                
                if (! nodeService.exists(storeRef))
                {
                    logger.warn("StoreRef '"+ storeRef+"' does not exist");
                    continue; // skip this location
                }          
    
                if (repositoryLocation.getQueryLanguage().equals(
                        SearchService.LANGUAGE_XPATH))
                {
                    NodeRef rootNode = nodeService.getRootNode(storeRef);
    
                    List<NodeRef> nodeRefs = searchService.selectNodes(rootNode,
                                                                       repositoryLocation.getXPathQueryStatement(ContentModel.TYPE_DICTIONARY_MODEL.getPrefixedQName(namespaceService)),
                                                                       null, 
                                                                       namespaceService, 
                                                                       false);
    
                    for (NodeRef dictionaryModel : nodeRefs)
                    {                    	
                        // Ignore if the node is a working copy or if its inactive
                        if (nodeService.hasAspect(dictionaryModel, ContentModel.ASPECT_WORKING_COPY) == false)
                        {
                            Boolean isActive = (Boolean)nodeService.getProperty(dictionaryModel, ContentModel.PROP_MODEL_ACTIVE);
           
                            if ((isActive != null) && (isActive.booleanValue() == true))
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
    }
    
    public void initMessages()
    {
        if (this.repositoryMessagesLocations != null)
        {
            // Register the messages found in the repository
            for (RepositoryLocation repositoryLocation : this.repositoryMessagesLocations)
            {                
                StoreRef storeRef = repositoryLocation.getStoreRef();
                String path = repositoryLocation.getPath();
                
                if (! nodeService.exists(storeRef))
                {
                    logger.warn("StoreRef '"+ storeRef+"' does not exist");
                    continue; // skip this location
                } 
  
                if (repositoryLocation.getQueryLanguage().equals(
                        SearchService.LANGUAGE_XPATH))
                {
                    NodeRef rootNode = nodeService.getRootNode(storeRef);
    
                    List<NodeRef> nodeRefs = searchService.selectNodes(rootNode, 
                                                                       repositoryLocation.getXPathQueryStatement(ContentModel.TYPE_CONTENT.getPrefixedQName(namespaceService)), 
                                                                       null, 
                                                                       namespaceService, 
                                                                       false);
     
                    List<String> resourceBundleBaseNames = new ArrayList<String>();
    
                    for (NodeRef messageResource : nodeRefs)
                    {
                        String resourceName = (String) nodeService.getProperty(
                                messageResource, ContentModel.PROP_NAME);
                        
                        String bundleBaseName = messageService.getBaseBundleName(resourceName);
                        
                        if (!resourceBundleBaseNames.contains(bundleBaseName))
                        {
                            resourceBundleBaseNames.add(bundleBaseName);
                        }
                    }
    
                    // Only need to register resource bundle names
                    for (String resourceBundleBaseName : resourceBundleBaseNames)
                    {
                        logger.info("Register bundle: " + resourceBundleBaseName);
    
                        messageService.registerResourceBundle(storeRef.toString() + path + "/cm:" + resourceBundleBaseName);
    
                    }
                } 
            }
        }
    }
    
    /**
     * Loads a model (and its dependents) if it does not exist in the list of loaded models.
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
            
            try
            {
                dictionaryDAO.putModel(model);
                loadedModels.add(modelName);
            }
            catch (AlfrescoRuntimeException e)
            {
                // note: skip with warning - to allow server to start, and hence allow the possibility of fixing the broken model(s)
                logger.warn("Failed to load model '" + modelName + "' : " + e);
            }
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
    
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // run as System on bootstrap
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {            
                init();
                return null;
            }                               
        }, AuthenticationUtil.getSystemUserName());
               
        if (tenantService.isEnabled())
        {
            tenantDeployerService.deployTenants(this, logger);
        }
        
    	register();
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        unregister();
        
        // run as System on shutdown
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {            
                destroy();
                return null;
            }                               
        }, AuthenticationUtil.getSystemUserName());
        
        if (tenantService.isEnabled())
        {
            tenantDeployerService.undeployTenants(this, logger);
        }
    }
    
    public void onEnableTenant()
    {
        init(); // will be called in context of tenant
    }
    
    public void onDisableTenant()
    {
        destroy(); // will be called in context of tenant
    }
    
    /**
     * Register
     */
    public void register()
    {
    	// register with Dictionary Service to allow (re-)init
    	dictionaryDAO.register(this);
    	
        // register with Message Service to allow (re-)init
        messageService.register(this);
        
        if (tenantService.isEnabled())
        {
            // register dictionary repository bootstrap
            tenantDeployerService.register(this);
            
            // register repository message (I18N) service
            tenantDeployerService.register(messageService);
        }
    }
    
    /**
     * Unregister
     */
    protected void unregister()
    {
        if (tenantService.isEnabled())
        {
            // register dictionary repository bootstrap
            tenantDeployerService.unregister(this);
            
            // register repository message (I18N) service
            tenantDeployerService.unregister(messageService);
        }
    }
}
