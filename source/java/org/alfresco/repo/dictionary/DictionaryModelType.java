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

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Dictionary model type behaviour.
 * 
 * @author Roy Wetherall
 */
public class DictionaryModelType implements ContentServicePolicies.OnContentUpdatePolicy,
                                            NodeServicePolicies.OnUpdatePropertiesPolicy,
                                            NodeServicePolicies.BeforeDeleteNodePolicy
{
    // Logger
    private static Log logger = LogFactory.getLog(DictionaryModelType.class);
    
    /** Key to the pending models */
    private static final String KEY_PENDING_MODELS = "dictionaryModelType.pendingModels";
    
    /** The dictionary DAO */
    private DictionaryDAO dictionaryDAO;
    
    /** The namespace DAO */
    private NamespaceDAO namespaceDAO;
    
    /** The node service */
    private NodeService nodeService;
    
    /** The content service */
    private ContentService contentService;
    
    /** The policy component */
    private PolicyComponent policyComponent;
    
    /** The workflow service */
    private WorkflowService workflowService;
    
    /** The search service */
    private SearchService searchService;
    
    /** The namespace service */
    private NamespaceService namespaceService;
    
    /** The tenant service */
    private TenantService tenantService;
    
    /** Transaction listener */
    private DictionaryModelTypeTransactionListener transactionListener;
        
    private List<String> storeUrls; // stores against which model deletes should be validated

    
    /**
     * Set the dictionary DAO
     * 
     * @param dictionaryDAO     the dictionary DAO
     */
    public void setDictionaryDAO(DictionaryDAO dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }
    
    /**
     * Set the namespace DOA
     * 
     * @param namespaceDAO      the namespace DAO
     */
    public void setNamespaceDAO(NamespaceDAO namespaceDAO)
    {
        this.namespaceDAO = namespaceDAO;
    }
    
    /**
     * Set the node service
     * 
     * @param nodeService       the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
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
     * Set the policy component
     * 
     * @param policyComponent   the policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Set the workflow service
     *
     * @param workflowService   the workflow service
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
    /**
     * Set the search service
     *
     * @param searchService   the search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * Set the namespace service
     *
     * @param namespaceService   the namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * Set the tenant service
     *
     * @param tenantService   the tenant service
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    
    public void setStoreUrls(List<String> storeUrls)
    {
        this.storeUrls = storeUrls;
    }
    
    
    /**
     * The initialise method     
     */
    public void init()
    {
        // Register interest in the onContentUpdate policy for the dictionary model type
        policyComponent.bindClassBehaviour(
                ContentServicePolicies.ON_CONTENT_UPDATE, 
                ContentModel.TYPE_DICTIONARY_MODEL, 
                new JavaBehaviour(this, "onContentUpdate"));
        
        // Register interest in the onPropertyUpdate policy for the dictionary model type
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), 
                ContentModel.TYPE_DICTIONARY_MODEL, 
                new JavaBehaviour(this, "onUpdateProperties"));
        
        // Register interest in the node delete policy
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"), 
                ContentModel.TYPE_DICTIONARY_MODEL, 
                new JavaBehaviour(this, "beforeDeleteNode"));
        
        // Create the transaction listener
        this.transactionListener = new DictionaryModelTypeTransactionListener(this.nodeService, this.contentService);
    }
    
    /**
     * On content update behaviour implementation
     * 
     * @param nodeRef   the node reference whose content has been updated
     */
    public void onContentUpdate(NodeRef nodeRef, boolean newContent)
    {
        queueModel(nodeRef);
    }
    
    @SuppressWarnings("unchecked")
    private void queueModel(NodeRef nodeRef)
    {
        Set<NodeRef> pendingModelUpdates = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_PENDING_MODELS);
        if (pendingModelUpdates == null)
        {
            pendingModelUpdates = new HashSet<NodeRef>();
            AlfrescoTransactionSupport.bindResource(KEY_PENDING_MODELS, pendingModelUpdates);
        }
        pendingModelUpdates.add(nodeRef);
        
        AlfrescoTransactionSupport.bindListener(this.transactionListener);
    }
    
    /**
     * On update properties behaviour implementation
     * 
     * @param nodeRef   the node reference
     * @param before    the values of the properties before update
     * @param after     the values of the properties after the update
     */
    public void onUpdateProperties(
            NodeRef nodeRef,
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        Boolean beforeValue = (Boolean)before.get(ContentModel.PROP_MODEL_ACTIVE);
        Boolean afterValue = (Boolean)after.get(ContentModel.PROP_MODEL_ACTIVE);
        
        if (beforeValue == null && afterValue != null)
        {
            queueModel(nodeRef);
        }
        else if (afterValue == null && beforeValue != null)
        {
            // Remove the model since the value has been cleared
            queueModel(nodeRef);
        }
        else if (beforeValue != null && afterValue != null && beforeValue.equals(afterValue) == false)
        {
            queueModel(nodeRef);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        // Ignore if the node is a working copy 
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) == false)
        {
            QName modelName = (QName)this.nodeService.getProperty(nodeRef, ContentModel.PROP_MODEL_NAME);
            if (modelName != null)
            {
            	// Validate model delete against usages - content and/or workflows
            	validateModelDelete(modelName);
            	
                // Remove the model from the dictionary
                dictionaryDAO.removeModel(modelName);
            }
        }
    }
    
    /**
     * Dictionary model type transaction listener class.
     */
    public class DictionaryModelTypeTransactionListener implements TransactionListener
    {
        /**
         * Id used in equals and hash
         */
        private String id = GUID.generate();
        
        private NodeService nodeService;
        private ContentService contentService;
        
        public DictionaryModelTypeTransactionListener(NodeService nodeService, ContentService contentService)
        {
            this.nodeService = nodeService;
            this.contentService = contentService;
        }
        
        /**
         * @see org.alfresco.repo.transaction.TransactionListener#flush()
         */
        public void flush()
        {
        }
        
        /**
         * @see org.alfresco.repo.transaction.TransactionListener#beforeCommit(boolean)
         */
        @SuppressWarnings("unchecked")
        public void beforeCommit(boolean readOnly)
        { 
            Set<NodeRef> pendingModels = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_PENDING_MODELS);
            
            if (pendingModels != null)
            {
                for (final NodeRef nodeRef : pendingModels)
                {
                    String tenantDomain = tenantService.getDomain(nodeRef.getStoreRef().getIdentifier());
                    String tenantAdminUserName = tenantService.getDomainUser(TenantService.ADMIN_BASENAME, tenantDomain);
                    
                    AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork()
                        {            
                            // Find out whether the model is active
                            boolean isActive = false;
                            Boolean value = (Boolean)nodeService.getProperty(nodeRef, ContentModel.PROP_MODEL_ACTIVE);
                            if (value != null)
                            {
                                isActive = value.booleanValue();
                            }
                            
                            // Ignore if the node is a working copy or if its inactive
                            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) == false)
                            {
                                if (isActive == true)
                                {
                                    // 1. Compile the model and update the details on the node            
                                    // 2. Re-put the model
                                    
                                    ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                                    if (contentReader != null)
                                    {
                                        // Create a model from the current content
                                        M2Model m2Model = M2Model.createModel(contentReader.getContentInputStream());
                                        
                                        // Try and compile the model
                                        ModelDefinition modelDefintion = m2Model.compile(dictionaryDAO, namespaceDAO).getModelDefinition();
                                        
                                        // Update the meta data for the model
                                        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
                                        props.put(ContentModel.PROP_MODEL_NAME, modelDefintion.getName());
                                        props.put(ContentModel.PROP_MODEL_DESCRIPTION, modelDefintion.getDescription());
                                        props.put(ContentModel.PROP_MODEL_AUTHOR, modelDefintion.getAuthor());
                                        props.put(ContentModel.PROP_MODEL_PUBLISHED_DATE, modelDefintion.getPublishedDate());
                                        props.put(ContentModel.PROP_MODEL_VERSION, modelDefintion.getVersion());
                                        nodeService.setProperties(nodeRef, props);
                                        
                                        // Validate model against dictionary - could be new, unchanged or updated
                                        dictionaryDAO.validateModel(m2Model);
                                        
                                        // Put the model
                                        dictionaryDAO.putModel(m2Model);
                                    }
                                }
                                else
                                {
                                    QName modelName = (QName)nodeService.getProperty(nodeRef, ContentModel.PROP_MODEL_NAME);
                                    if (modelName != null)
                                    {
                                        // Validate model delete against usages - content and/or workflows
                                        validateModelDelete(modelName);
                                        
                                        // Remove the model from the dictionary
                                        dictionaryDAO.removeModel(modelName);
                                    }
                                }
                            }

                            return null; 
                        }
                    }, tenantAdminUserName);
                }
            }
        }

        /**
         * @see org.alfresco.repo.transaction.TransactionListener#beforeCompletion()
         */
        public void beforeCompletion()
        {
        }

        /**
         * @see org.alfresco.repo.transaction.TransactionListener#afterCommit()
         */
        public void afterCommit()
        {
        }
        
        /**
         * @see org.alfresco.repo.transaction.TransactionListener#afterRollback()
         */
        @SuppressWarnings("unchecked")
        public void afterRollback()
        {
        }
        
        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj instanceof DictionaryModelTypeTransactionListener)
            {
                DictionaryModelTypeTransactionListener that = (DictionaryModelTypeTransactionListener) obj;
                return (this.id.equals(that.id));
            }
            else
            {
                return false;
            }
        }
    }
    
    /**
     * validate against repository contents / workflows (e.g. when deleting an existing model)
     * 
     * @param modelName
     */
    private void validateModelDelete(QName modelName)
    {
        // TODO add model locking during delete (would need to be tenant-aware & cluster-aware) to avoid potential 
    	//      for concurrent addition of new content/workflow as model is being deleted
        
        try
        {
        	dictionaryDAO.getModel(modelName); // ignore returned model definition
        }
        catch (DictionaryException e)
        {
        	logger.warn("Model ' + modelName + ' does not exist ... skip delete validation : " + e);
        	return;
        }
    
        // check workflow namespace usage
        for (WorkflowDefinition workflowDef : workflowService.getDefinitions())
        {
            String workflowDefName = workflowDef.getName();
            String workflowNamespaceURI = QName.createQName(BPMEngineRegistry.getLocalId(workflowDefName), namespaceService).getNamespaceURI();
            for (NamespaceDefinition namespace : dictionaryDAO.getNamespaces(modelName))
            {
                if (workflowNamespaceURI.equals(namespace.getUri()))
                {
                    throw new AlfrescoRuntimeException("Failed to validate model delete - found workflow process definition " + workflowDefName + " using model namespace '" + namespace.getUri() + "'");
                }
            }
        }
 
        // check for type usages
        for (TypeDefinition type : dictionaryDAO.getTypes(modelName))
        {
        	validateClass(type);
        }

        // check for aspect usages
        for (AspectDefinition aspect : dictionaryDAO.getAspects(modelName))
        {
        	validateClass(aspect);
        }
    }
    
    private void validateClass(ClassDefinition classDef)
    {
    	QName className = classDef.getName();
        
        String classType = "TYPE";
        if (classDef instanceof AspectDefinition)
        {
        	classType = "ASPECT";
        }
        
        for (String storeUrl : this.storeUrls)
        {
            StoreRef store = new StoreRef(storeUrl);
            
            // search for TYPE or ASPECT - TODO - alternative would be to extract QName and search by namespace ...
            ResultSet rs = searchService.query(store, SearchService.LANGUAGE_LUCENE, classType+":\""+className+"\"");
            if (rs.length() > 0)
            {
                throw new AlfrescoRuntimeException("Failed to validate model delete - found " + rs.length() + " nodes in store " + store + " with " + classType + " '" + className + "'");
            }
        }
        
        // check against workflow task usage
        for (WorkflowDefinition workflowDef : workflowService.getDefinitions())
        {
            for (WorkflowTaskDefinition workflowTaskDef : workflowService.getTaskDefinitions(workflowDef.getId()))
            {
                TypeDefinition workflowTypeDef = workflowTaskDef.metadata;
                if (workflowTypeDef.getName().toString().equals(className))
                {
                    throw new AlfrescoRuntimeException("Failed to validate model delete - found task definition in workflow " + workflowDef.getName() + " with " + classType + " '" + className + "'");
                }
            }
        }
    }
}
