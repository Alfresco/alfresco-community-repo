/*
 * #%L
 * Alfresco Repository
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.EmptyContentReader;
import org.alfresco.repo.dictionary.DynamicModelPolicies.OnLoadDynamicModel;
import org.alfresco.repo.i18n.MessageDeployer;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Bootstrap the dictionary from specified locations within the repository
 * 
 * @author Roy Wetherall, JanV, sglover
 */
public class DictionaryRepositoryBootstrap extends AbstractLifecycleBean
implements TenantDeployer, DictionaryListener, /*TenantDictionaryListener, */MessageDeployer
{
    // Logging support
    private static Log logger = LogFactory.getLog(DictionaryRepositoryBootstrap.class);

    /** Locations in the repository from which models should be loaded */
    private List<RepositoryLocation> repositoryModelsLocations = new ArrayList<RepositoryLocation>();

    /** Locations in the repository from which messages should be loaded */
    private List<RepositoryLocation> repositoryMessagesLocations = new ArrayList<RepositoryLocation>();

    /** Dictionary DAO */
    private DictionaryDAO dictionaryDAO = null;
    
    /** The content service */
    private ContentService contentService;

    /** The node service */
    private NodeService nodeService;
    
    /** The tenant admin service */
    private TenantAdminService tenantAdminService;

    /** The namespace service */
    private NamespaceService namespaceService;

    /** The message service */
    private MessageService messageService;

    /** The transaction service */
    private TransactionService transactionService;
    
    /** The policy component */
    private PolicyComponent policyComponent;
    
      
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

    public PolicyComponent getPolicyComponent()
    {
        return policyComponent;
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Set the tenant admin service
     * 
     * @param tenantAdminService    the tenant admin service
     */
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
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
     * @param repositoryLocations   list of the repository models locations
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

    private ClassPolicyDelegate<OnLoadDynamicModel> onLoadDynamicModelDelegate;
    
    /**
     * Initialise - after bootstrap of schema and tenant admin service
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "dictionaryDAO", dictionaryDAO);
        PropertyCheck.mandatory(this, "contentService", contentService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "tenantAdminService", tenantAdminService);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "messageService", messageService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        
        if(onLoadDynamicModelDelegate == null)
        {
            onLoadDynamicModelDelegate = policyComponent.registerClassPolicy(DynamicModelPolicies.OnLoadDynamicModel.class);
        }
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                onDictionaryInit();
                initMessages();
                
                return (Object)null;
            }
        }, transactionService.isReadOnly(), false);
    }
    
    public void destroy()
    {    
        // NOOP - will be destroyed directly via DictionaryComponent
    }

    /**
     * Perform the actual repository access, checking for the existence of a valid transaction
     */
    private void onDictionaryInitInTxn()
    {
        if (AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_NONE)
        {
            throw new IllegalStateException("The Repository-based dictionary initialization has to be done in the context of a transaction.");
        }
        
        long startTime = System.currentTimeMillis();
        
        if (logger.isTraceEnabled())
        {
            String tenantDomain = tenantAdminService.getCurrentUserDomain();
            logger.trace("onDictionaryInit: ["+Thread.currentThread()+"]"+(tenantDomain.equals(TenantService.DEFAULT_DOMAIN) ? "" : " (Tenant: "+tenantDomain+")"));
        }
        
        Collection<QName> modelsBefore = dictionaryDAO.getModels(true); // note: re-entrant
        int modelsBeforeCnt = (modelsBefore != null ? modelsBefore.size() : 0);
        
        List<String> loadedModels = new ArrayList<String>();
        
        if (this.repositoryModelsLocations != null)
        {
            // URI to model map
            Map<String, DynamicModelInfo> modelMap = new HashMap<String, DynamicModelInfo>();
            
            if (logger.isTraceEnabled())
            {
                logger.trace("onDictionaryInit: locations="+this.repositoryModelsLocations);
            }
            
            // Register the models found in the repository
            
            for (RepositoryLocation repositoryLocation : this.repositoryModelsLocations)
            {
                StoreRef storeRef = repositoryLocation.getStoreRef();
                
                if (! nodeService.exists(storeRef))
                {
                    logger.info("StoreRef '"+ storeRef+"' does not exist");
                    continue; // skip this location
                }
                
                List<NodeRef> nodeRefs = null;
                
                if (repositoryLocation.getQueryLanguage().equals(RepositoryLocation.LANGUAGE_PATH))
                {
                    nodeRefs = getNodes(storeRef, repositoryLocation, ContentModel.TYPE_DICTIONARY_MODEL);
                    
                    if (nodeRefs.size() > 0)
                    {
                        for (NodeRef dictionaryModel : nodeRefs)
                        {
                            try
                            {
                                // Ignore if the node is a working copy or archived, or if its inactive
                                if (! (nodeService.hasAspect(dictionaryModel, ContentModel.ASPECT_WORKING_COPY) ||
                                        nodeService.hasAspect(dictionaryModel, ContentModel.ASPECT_ARCHIVED))) 
                                {
                                    Boolean isActive = (Boolean)nodeService.getProperty(dictionaryModel, ContentModel.PROP_MODEL_ACTIVE);
                                    
                                    if ((isActive != null) && (isActive.booleanValue() == true))
                                    {
                                        M2Model model = createM2Model(dictionaryModel);
                                        if (model != null)
                                        {
                                            if (logger.isTraceEnabled())
                                            {
                                                logger.trace("onDictionaryInit: "+model.getName()+" ("+dictionaryModel+")");
                                            }
                                            
                                            for (M2Namespace namespace : model.getNamespaces())
                                            {
                                                modelMap.put(namespace.getUri(), new DynamicModelInfo(repositoryLocation, model, dictionaryModel));
                                            }
                                        }
                                    }
                                }
                            }
                            catch (InvalidNodeRefException inre)
                            {
                                // ignore - model no longer exists
                                if (logger.isDebugEnabled())
                                {
                                    logger.debug("onDictionaryInit: "+inre+" (assume concurrently deleted)");
                                }
                                
                                continue;
                            }
                        }
                    }
                }
                else
                {
                    logger.error("Unsupported query language for models location: " + repositoryLocation.getQueryLanguage());
                }
            }
            
            // Load the models ensuring that they are loaded in the correct order
            for (Map.Entry<String, DynamicModelInfo> entry : modelMap.entrySet())
            {
                RepositoryLocation importedLocation = entry.getValue().location;
                M2Model importedModel = entry.getValue().model;
                loadModel(modelMap, loadedModels, importedModel, importedLocation);
                notifyDynamicModelLoaded(entry.getValue());
            }
        }
        
        Collection<QName> modelsAfter = dictionaryDAO.getModels(true);
        int modelsAfterCnt = (modelsAfter != null ? modelsAfter.size() : 0);
        
        if (logger.isDebugEnabled())
        {
            String tenantDomain = tenantAdminService.getCurrentUserDomain();
            logger.debug("Model count: before="+modelsBeforeCnt+", load/update="+loadedModels.size()+", after="+modelsAfterCnt+" in "+(System.currentTimeMillis()-startTime)+" msecs ["+Thread.currentThread()+"] "+(tenantDomain.equals(TenantService.DEFAULT_DOMAIN) ? "" : " (Tenant: "+tenantDomain+")"));
        }
    }

    public void notifyDynamicModelLoaded(DynamicModelInfo entry)
    {
        if (onLoadDynamicModelDelegate == null)
        {
            onLoadDynamicModelDelegate = policyComponent.registerClassPolicy(DynamicModelPolicies.OnLoadDynamicModel.class);
        }

        DynamicModelPolicies.OnLoadDynamicModel policy = onLoadDynamicModelDelegate.get(ContentModel.TYPE_CONTENT);
        try
        {
            policy.onLoadDynamicModel(entry.model, entry.nodeRef);
        }
        catch (RuntimeException e)
        {
            // similar to "loadModel" method
            // note: skip with warning - to allow server to start, and hence allow the possibility of fixing the broken model(s)
            // do print the stacktrace so there is some information about what went wrong, in order to fix it
            logger.warn("Failed to notify dynamic model loaded'" + (entry.model != null ? entry.model.getName() : "null") + "' : " + e, e);
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
                
                if (! nodeService.exists(storeRef))
                {
                    logger.info("StoreRef '"+ storeRef+"' does not exist");
                    continue; // skip this location
                }
                
                if (repositoryLocation.getQueryLanguage().equals(RepositoryLocation.LANGUAGE_PATH))
                {
                    List<NodeRef> nodeRefs = getNodes(storeRef, repositoryLocation, ContentModel.TYPE_CONTENT);
                    
                    if (nodeRefs.size() > 0)
                    {
                        List<String> resourceBundleBaseNames = new ArrayList<String>();
                        
                        for (NodeRef messageResource : nodeRefs)
                        {
                            String messageResourcePath = nodeService.getPath(messageResource).toPrefixString(namespaceService);
                            String bundleBasePrefixedName = messageResourcePath.substring(messageResourcePath.lastIndexOf("/"));
                            String bundleBaseName = messageService.getBaseBundleName(bundleBasePrefixedName); 
                            
                            if (!resourceBundleBaseNames.contains(bundleBaseName))
                            {
                                messageService.registerResourceBundle(storeRef.toString() + repositoryLocation.getPath() + bundleBaseName);
                                resourceBundleBaseNames.add(bundleBaseName);
                            }
                        }
                    }
                }
                else
                {
                    logger.error("Unsupported query language for messages location: " + repositoryLocation.getQueryLanguage());
                }
            }
        }
    }
    
    // note: active or inactive
    public List<NodeRef> getModelRefs()
    {
        List<NodeRef> modelRefs = new ArrayList<NodeRef>();
        
        for (RepositoryLocation repositoryLocation : this.repositoryModelsLocations)
        {
            StoreRef storeRef = repositoryLocation.getStoreRef();
            
            if (! nodeService.exists(storeRef))
            {
                logger.info("StoreRef '"+ storeRef+"' does not exist");
                continue; // skip this location
            }
            
            if (repositoryLocation.getQueryLanguage().equals(RepositoryLocation.LANGUAGE_PATH))
            {
                List<NodeRef> nodeRefs = getNodes(storeRef, repositoryLocation, ContentModel.TYPE_DICTIONARY_MODEL);
                
                if (nodeRefs.size() > 0)
                {
                    for (NodeRef dictionaryModel : nodeRefs)
                    {
                        try
                        {
                            // Ignore if the node is a working copy or archived
                            if (! (nodeService.hasAspect(dictionaryModel, ContentModel.ASPECT_WORKING_COPY) || nodeService.hasAspect(dictionaryModel, ContentModel.ASPECT_ARCHIVED))) 
                            {
                                modelRefs.add(dictionaryModel);
                            }
                        }
                        catch (InvalidNodeRefException inre)
                        {
                            // ignore - model no longer exists
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("getModelRefs: "+inre+" (assume concurrently deleted)");
                            }
                            
                            continue;
                        }
                    }
                }
            }
            else
            {
                logger.error("Unsupported query language for models location: " + repositoryLocation.getQueryLanguage());
            }
        }
        
        return modelRefs;
    }

    protected List<NodeRef> getNodes(StoreRef storeRef, RepositoryLocation repositoryLocation, QName nodeType)
    {
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>();

        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
        if(nodeService.exists(rootNodeRef) == false)
        {
            //Tenant is deleted. But cache refresh was called to inform another cluster nodes
            //Should be reworked when MNT-11638 will be implemented
            return nodeRefs;
        }
        
        if(repositoryLocation instanceof DynamicCreateRepositoryLocation)
        {
            ((DynamicCreateRepositoryLocation)repositoryLocation).checkAndCreate(rootNodeRef);
        }

        String[] pathElements = repositoryLocation.getPathElements();

        NodeRef folderNodeRef = rootNodeRef;
        if (pathElements.length > 0)
        {
            folderNodeRef = resolveQNamePath(rootNodeRef, pathElements);
        }
        
        if (folderNodeRef != null)
        {
            Set<QName> types = new HashSet<QName>(1);
            types.add(nodeType);
            List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(folderNodeRef, types);
            
            if (childAssocRefs.size() > 0)
            {
                nodeRefs = new ArrayList<NodeRef>(childAssocRefs.size());
                for (ChildAssociationRef childAssocRef : childAssocRefs)
                {
                    nodeRefs.add(childAssocRef.getChildRef());
                }
            }
        }
        
        return nodeRefs;
    }
    
    private class DynamicModelInfo
    {
        RepositoryLocation location;
        M2Model model;
        NodeRef nodeRef;
        
        
        DynamicModelInfo(RepositoryLocation location, M2Model model, NodeRef nodeRef)
        {
            this.location = location;
            this.model = model;
            this.nodeRef = nodeRef;
        }
    }
    
    /**
     * Loads a model (and its dependents) if it does not exist in the list of loaded models.
     * 
     * @param modelMap          a map of the models to be loaded
     * @param loadedModels      the list of models already loaded
     * @param model             the model to try and load
     */
    private void loadModel(Map<String, DynamicModelInfo> modelMap, List<String> loadedModels, M2Model model, RepositoryLocation modelLocation)
    {
        String modelName = model.getName();
        if (loadedModels.contains(modelName) == false)
        {
            for (M2Namespace importNamespace : model.getImports())
            {
                DynamicModelInfo entry = modelMap.get(importNamespace.getUri());
                if (entry != null)
                {
                    RepositoryLocation importedLocation = entry.location;
                    M2Model importedModel = entry.model;
                    
                    // Ensure that the imported model is loaded first
                    loadModel(modelMap, loadedModels, importedModel, importedLocation);
                }
                // else we can assume that the imported model is already loaded, if this not the case then
                //      an error will be raised during compilation
            }
            
            try
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Loading model: " + modelName
                            + " (from ["+ modelLocation.getStoreRef() + "]"+ modelLocation.getPath() + ")");
                }

                dictionaryDAO.putModel(model);

                loadedModels.add(modelName);
            }
            catch (AlfrescoRuntimeException e)
            {
                // note: skip with warning - to allow server to start, and hence allow the possibility of fixing the broken model(s)
                // do print the stacktrace so there is some information about what went wrong, in order to fix it
                logger.warn("Failed to load model '" + modelName + "' : " + e, e);
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
            if (contentReader instanceof EmptyContentReader)
            {
                // belts-and-braces
                logger.error("Failed to create model (due to EmptyContentReader): "+nodeRef);
            }
            else
            {
                InputStream is = null;
                try
                {
                    is = contentReader.getContentInputStream();
                    model = M2Model.createModel(is);
                }
                finally
                {
                    if (is != null)
                    {
                        try
                        {
                            is.close();
                        }
                        catch (IOException e)
                        {
                            logger.error("Failed to close input stream for " + nodeRef);
                        }
                    }
                }
            }
        }
        // TODO should we inactivate the model node and put the error somewhere??
        return model;
    }
    
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // Reset the dictionary (destroy and reload) in order to ensure that we have a basic version of
        // the dictionary (static models) loaded at least
        dictionaryDAO.reset();

        // Register listeners, which will be called when the dictionary is next reloaded
        register();
        
        // Trigger a reload.  The callbacks will occur immediately on the current thread, however,
        // the model created in reset() will still be available for the basic necessities
        dictionaryDAO.init();
        
        // The listeners can now know about this
        // However, the listeners will be needing to access the dictionary themselves, hence the earlier 'reset'
        // to ensure that there is no deadlock waiting for a new dictionary
        ((ApplicationContext) event.getSource()).publishEvent(new DictionaryRepositoryBootstrappedEvent(this));
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
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
     * Register listeners
     */
    public void register()
    {
        // register with Dictionary Service to allow (re-)init
        dictionaryDAO.registerListener(this);

        // register with Message Service to allow (re-)init
        messageService.register(this);
        
        if (tenantAdminService.isEnabled())
        {
            // register dictionary repository bootstrap
            tenantAdminService.register(this);
            
            // register repository message (I18N) service
            tenantAdminService.register(messageService);
        }
    }
    
    /**
     * Unregister
     */
    protected void unregister()
    {
        if (tenantAdminService.isEnabled())
        {
            // register dictionary repository bootstrap
            tenantAdminService.unregister(this);
            
            // register repository message (I18N) service
            tenantAdminService.unregister(messageService);
        }
    }

    // TODO refactor (see also MessageServiceImpl)
    protected NodeRef resolveQNamePath(NodeRef rootNodeRef, String[] pathPrefixQNameStrings)
    {
        if (pathPrefixQNameStrings.length == 0)
        {
            throw new IllegalArgumentException("Path array is empty");
        }
        // walk the path
        NodeRef parentNodeRef = rootNodeRef;
        for (int i = 0; i < pathPrefixQNameStrings.length; i++)
        {
            String pathPrefixQNameString = pathPrefixQNameStrings[i];
            
            QName pathQName = null;
            if (tenantAdminService.isEnabled())
            {
                String[] parts = QName.splitPrefixedQName(pathPrefixQNameString);
                if ((parts.length == 2) && (parts[0].equals(NamespaceService.APP_MODEL_PREFIX)))
                {
                    String pathUriQNameString = new StringBuilder(64).
                        append(QName.NAMESPACE_BEGIN).
                        append(NamespaceService.APP_MODEL_1_0_URI).
                        append(QName.NAMESPACE_END).
                        append(parts[1]).toString();
                    
                    pathQName = QName.createQName(pathUriQNameString);
                }
                else
                {
                    pathQName = QName.createQName(pathPrefixQNameString, namespaceService);
                }
            }
            else
            {
                pathQName = QName.createQName(pathPrefixQNameString, namespaceService);
            }
            
            List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(parentNodeRef, RegexQNamePattern.MATCH_ALL, pathQName);
            if (childAssocRefs.size() != 1)
            {
                return null;
            }
            parentNodeRef = childAssocRefs.get(0).getChildRef();
        }
        return parentNodeRef;
    }

    /**
     * Initialise the dictionary, ensuring that a transaction is available
     */
    @Override
    public void onDictionaryInit()
    {
        if(onLoadDynamicModelDelegate == null)
        {
            onLoadDynamicModelDelegate = policyComponent.registerClassPolicy(DynamicModelPolicies.OnLoadDynamicModel.class);
        }
        RetryingTransactionCallback<Void> initCallback = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                onDictionaryInitInTxn();
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(initCallback, true, false);
    }

    @Override
    public void afterDictionaryDestroy()
    {
    }

    @Override
    public void afterDictionaryInit()
    {
    }
}
