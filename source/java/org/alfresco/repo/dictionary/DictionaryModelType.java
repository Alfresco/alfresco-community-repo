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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
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
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * Dictionary model type behaviour.
 * 
 * @author Roy Wetherall, janv
 */
public class DictionaryModelType implements ContentServicePolicies.OnContentUpdatePolicy,
                                            NodeServicePolicies.OnUpdatePropertiesPolicy,
                                            NodeServicePolicies.BeforeDeleteNodePolicy,
                                            NodeServicePolicies.OnDeleteNodePolicy,
                                            NodeServicePolicies.OnCreateNodePolicy,
                                            NodeServicePolicies.OnRemoveAspectPolicy
{
    // Logger
    private static Log logger = LogFactory.getLog(DictionaryModelType.class);
    
    /** Key to the pending models */
    private static final String KEY_PENDING_MODELS = "dictionaryModelType.pendingModels";
    
    /** Key to the pending deleted models */
    private static final String KEY_PENDING_DELETE_MODELS = "dictionaryModelType.pendingDeleteModels";
    
    /** Key to the removed "workingcopy" aspect */
    private static final String KEY_WORKING_COPY = "dictionaryModelType.workingCopy";
    
    /** The name of the lock used to ensure that DictionaryModelType updates do not run on more than one thread/node at the same time. */
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "DictionaryModelType");
    
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
    
    /** The tenant deployer service */
    private TenantAdminService tenantAdminService;
    
    private TransactionService transactionService;
    
    private JobLockService jobLockService;
    
    /** Transaction listener */
    private DictionaryModelTypeTransactionListener transactionListener;
        
    private List<String> storeUrls; // stores against which model deletes should be validated
    
    /** Validation marker */
    private boolean doValidation = true;
    
    /**
     * Set the dictionary DAO
     */
    public void setDictionaryDAO(DictionaryDAO dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }
    
    /**
     * Set the namespace DOA
     */
    public void setNamespaceDAO(NamespaceDAO namespaceDAO)
    {
        this.namespaceDAO = namespaceDAO;
    }
    
    /**
     * Set the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * Set the policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Set the workflow service
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
    /**
     * Set the search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * Set the namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * Set the tenant service
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /**
     * Set the tenant admin service
     */
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
    
    /**
     * Set the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }
    
    public void setStoreUrls(List<String> storeUrls)
    {
        this.storeUrls = storeUrls;
    }
    
    public void setDoValidation(boolean doValidation)
    {
    	this.doValidation = doValidation;
    }    
    
    /**
     * The initialise method
     */
    public void init()
    {
        // Register interest in the onContentUpdate policy for the dictionary model type
        policyComponent.bindClassBehaviour(
                ContentServicePolicies.OnContentUpdatePolicy.QNAME,
                ContentModel.TYPE_DICTIONARY_MODEL,
                new JavaBehaviour(this, "onContentUpdate"));
        
        // Register interest in the onUpdateProperties policy for the dictionary model type
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                ContentModel.TYPE_DICTIONARY_MODEL,
                new JavaBehaviour(this, "onUpdateProperties"));
        
        // Register interest in the beforeDeleteNode policy for the dictionary model type
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                ContentModel.TYPE_DICTIONARY_MODEL,
                new JavaBehaviour(this, "beforeDeleteNode"));
        
        // Register interest in the onDeleteNode policy for the dictionary model type
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"),
                ContentModel.TYPE_DICTIONARY_MODEL,
                new JavaBehaviour(this, "onDeleteNode"));
        
        // Register interest in the onRemoveAspect policy
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveAspect"),
                ContentModel.TYPE_DICTIONARY_MODEL,
                new JavaBehaviour(this, "onRemoveAspect"));
        
        // Register interest in the onCreateNode policy
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
                ContentModel.TYPE_DICTIONARY_MODEL,
                new JavaBehaviour(this, "onCreateNode"));
        
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
        if (logger.isTraceEnabled())
        {
            logger.trace("onContentUpdate: nodeRef="+nodeRef+ " ["+AlfrescoTransactionSupport.getTransactionId()+"]");
        }
        
        queueModel(nodeRef);
    }
    
    @SuppressWarnings("unchecked")
    private void queueModel(NodeRef nodeRef)
    {
        Set<NodeRef> pendingModels = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_PENDING_MODELS);
        if (pendingModels == null)
        {
            //pendingModels = Collections.newSetFromMap(new ConcurrentHashMap()); // Java 6
            pendingModels = new CopyOnWriteArraySet<NodeRef>();
            AlfrescoTransactionSupport.bindResource(KEY_PENDING_MODELS, pendingModels);
        }
        pendingModels.add(tenantService.getName(nodeRef));
        
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
        if (logger.isTraceEnabled())
        {
            logger.trace("onUpdateProperties: nodeRef="+nodeRef+ " ["+AlfrescoTransactionSupport.getTransactionId()+"]");
        }
        
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
    
    public void onRemoveAspect(NodeRef nodeRef, QName aspect)
    {
        if (logger.isTraceEnabled())
        {
            logger.trace("onRemoveAspect: nodeRef="+nodeRef+ " ["+AlfrescoTransactionSupport.getTransactionId()+"]");
        }
        
        // undo/cancel checkout removes the "workingcopy" aspect prior to deleting the node - hence need to track here
        if (aspect.equals(ContentModel.ASPECT_WORKING_COPY))
        {
            AlfrescoTransactionSupport.bindResource(KEY_WORKING_COPY, nodeRef);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        boolean workingCopy = nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY);
        NodeRef wcNodeRef = (NodeRef)AlfrescoTransactionSupport.getResource(KEY_WORKING_COPY);
        if ((wcNodeRef != null) && (wcNodeRef.equals(nodeRef)))
        {
            workingCopy = true;
        }
        
        boolean isVersionNode = nodeRef.getStoreRef().getIdentifier().equals(Version2Model.STORE_ID);
        
        // Ignore if the node is a working copy or version node
        if (! (workingCopy || isVersionNode))
        {
            QName modelName = (QName)this.nodeService.getProperty(nodeRef, ContentModel.PROP_MODEL_NAME);
            
            if (logger.isTraceEnabled())
            {
                logger.trace("beforeDeleteNode: nodeRef="+nodeRef+" validate model delete (modelName="+modelName+")");
            }
            
            if (modelName != null)
            {
                // Validate model delete against usages - content and/or workflows
                validateModelDelete(modelName);
                
                Set<NodeRef> pendingModelDeletes = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_PENDING_DELETE_MODELS);
                if (pendingModelDeletes == null)
                {
                    //pendingModelDeletes = Collections.newSetFromMap(new ConcurrentHashMap()); // Java 6
                    pendingModelDeletes = new CopyOnWriteArraySet<NodeRef>();
                    AlfrescoTransactionSupport.bindResource(KEY_PENDING_DELETE_MODELS, pendingModelDeletes);
                }
                pendingModelDeletes.add(tenantService.getName(nodeRef));
                
                AlfrescoTransactionSupport.bindListener(this.transactionListener);
            }
        }
        else
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("beforeDeleteNode: nodeRef="+nodeRef+ " ignored ("+(workingCopy ? " workingCopy " : "")+(isVersionNode ? " isVersionNode " : "")+") ["+AlfrescoTransactionSupport.getTransactionId()+"]");
            }
        }
    }
    
    //@SuppressWarnings("unchecked")
    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived)
    {
        /*
        NodeRef nodeRef = childAssocRef.getChildRef();
        
        if (logger.isTraceEnabled())
        {
            logger.trace("onDeleteNode: nodeRef="+nodeRef+ " ["+AlfrescoTransactionSupport.getTransactionId()+"]");
        }
        
        Set<NodeRef> pendingDeleteModels = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_PENDING_DELETE_MODELS);
        
        if (pendingDeleteModels != null)
        {
            if (pendingDeleteModels.contains(nodeRef))
            {
                String tenantDomain = tenantService.getDomain(nodeRef.getStoreRef().getIdentifier());
                String tenantSystemUserName = tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain);
                
                AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork()
                    {
                        // invalidate - to force lazy re-init
                        dictionaryDAO.destroy();
                        
                        return null; 
                    }
                }, tenantSystemUserName);
                
                if (logger.isTraceEnabled())
                {
                    logger.trace("onDeleteNode: Dictionary destroyed ["+AlfrescoTransactionSupport.getTransactionId()+"]");
                }
            }
        }
        */
    }
    
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        NodeRef nodeRef = childAssocRef.getChildRef();
        
        if (logger.isTraceEnabled())
        {
            logger.trace("onCreateNode: nodeRef="+nodeRef+ " ["+AlfrescoTransactionSupport.getTransactionId()+"]");
        }
        
        if (nodeService.getType(nodeRef).equals(ContentModel.TYPE_DICTIONARY_MODEL))
        {
            Boolean value = (Boolean)nodeService.getProperty(nodeRef, ContentModel.PROP_MODEL_ACTIVE);
            if ((value != null) && (value == true))
            {
                queueModel(nodeRef);
            }
        }
    }
    
    /**
     * Dictionary model type transaction listener class.
     */
    public class DictionaryModelTypeTransactionListener extends TransactionListenerAdapter
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
        
        @SuppressWarnings("unchecked")
        @Override
        public void afterCommit()
        {
            Set<NodeRef> pendingModels = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_PENDING_MODELS);
            Set<NodeRef> pendingDeleteModels = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_PENDING_DELETE_MODELS);
            
            if (logger.isTraceEnabled())
            {
                logger.trace("afterCommit: "+Thread.currentThread().getName()+" pendingModelsCnt="+(pendingModels != null ? pendingModels.size() : "0")+
                                        ", pendingDeleteModelsCnt="+(pendingDeleteModels != null ? pendingDeleteModels.size() : "0"));
            }
            
            Set<String> systemTenants = new HashSet<String>(10);
            
            if (pendingModels != null)
            {
                // unbind the resource from the transaction
                AlfrescoTransactionSupport.unbindResource(KEY_PENDING_MODELS);
                
                for (NodeRef nodeRef : pendingModels)
                {
                    String tenantDomain = tenantService.getDomain(nodeRef.getStoreRef().getIdentifier());
                    String tenantSystemUserName = tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain);
                    systemTenants.add(tenantSystemUserName);
                }
            }
            
            if (pendingDeleteModels != null)
            {
                // unbind the resource from the transaction
                AlfrescoTransactionSupport.unbindResource(KEY_PENDING_DELETE_MODELS);
                
                for (NodeRef nodeRef : pendingDeleteModels)
                {
                    String tenantDomain = tenantService.getDomain(nodeRef.getStoreRef().getIdentifier());
                    String tenantSystemUserName = tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain);
                    systemTenants.add(tenantSystemUserName);
                }
            }
            
            if (systemTenants.size() > 0)
            {
                for (final String tenantSystemUserName : systemTenants)
                {
                    RetryingTransactionCallback<Void> work = new RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            AuthenticationUtil.runAs(new RunAsWork<Object>()
                                    {
                                        public Object doWork()
                                        {
                                            // invalidate - to force lazy re-init
                                            // note: since afterCommit - need to either clear shared cache or destroy in separate txn
                                            dictionaryDAO.destroy();
                                            
                                            if (logger.isTraceEnabled())
                                            {
                                                logger.trace("afterCommit: Dictionary destroyed ["+AlfrescoTransactionSupport.getTransactionId()+"]");
                                            }
                                            
                                            return null; 
                                        }
                                    }, tenantSystemUserName);
                            
                            return null;
                        }
                    };
                    
                    transactionService.getRetryingTransactionHelper().doInTransaction(work, true, true);
                }
            }
        }
        
        /**
         * @see org.alfresco.repo.transaction.TransactionListener#beforeCommit(boolean)
         */
        @SuppressWarnings("unchecked")
        @Override
        public void beforeCommit(boolean readOnly)
        {
            if (jobLockService != null)
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("beforeCommit: "+Thread.currentThread().getName()+" attempt to get transactional lock ["+AlfrescoTransactionSupport.getTransactionId()+"]");
                }
                
                try
                {
                    jobLockService.getTransactionalLock(LOCK_QNAME, (1000*60), 3000, 10);
                }
                catch (LockAcquisitionException lae)
                {
                    throw new ConcurrencyFailureException(lae.getMessage());
                }
                
                if (logger.isTraceEnabled())
                {
                    logger.trace("beforeCommit: "+Thread.currentThread().getName()+" got transactional lock ["+AlfrescoTransactionSupport.getTransactionId()+"]");
                }
            }
            
            Set<NodeRef> pendingModels = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(KEY_PENDING_MODELS);
            
            if (pendingModels != null)
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("beforeCommit: pendinpolicy-context.xmlgModelsCnt="+pendingModels.size()+" ["+AlfrescoTransactionSupport.getTransactionId()+"]");
                }
                
                for (NodeRef pendingNodeRef : pendingModels)
                {
                    String tenantDomain = tenantService.getDomain(pendingNodeRef.getStoreRef().getIdentifier());
                    String tenantSystemUserName = tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain);
                    
                    final NodeRef nodeRef = tenantService.getBaseName(pendingNodeRef);
                    
                    AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork()
                        {
                            // Ignore if the node no longer exists
                            if (! nodeService.exists(nodeRef))
                            {
                                return null;
                            }
                            
                            // Find out whether the model is active
                            boolean isActive = false;
                            Boolean value = (Boolean)nodeService.getProperty(nodeRef, ContentModel.PROP_MODEL_ACTIVE);
                            if (value != null)
                            {
                                isActive = value.booleanValue();
                            }
                            
                            // Ignore if the node is a working copy
                            if (! (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)))
                            {
                                if (isActive == true)
                                {
                                    // 1. Compile the model and update the details on the node
                                    // 2. Re-put the model
                                    
                                    ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                                    if (contentReader != null)
                                    {
                                        // Create a model from the current content
                                        M2Model m2Model = null;
                                        InputStream is = null;
                                        try
                                        {
                                            is = contentReader.getContentInputStream();
                                            m2Model = M2Model.createModel(is);
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
                                        
                                        // Try and compile the model
                                        CompiledModel compiledModel= m2Model.compile(dictionaryDAO, namespaceDAO);
                                        ModelDefinition modelDefinition = compiledModel.getModelDefinition();
                                        
                                        // Update the meta data for the model
                                        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
                                        props.put(ContentModel.PROP_MODEL_NAME, modelDefinition.getName());
                                        props.put(ContentModel.PROP_MODEL_DESCRIPTION, modelDefinition.getDescription());
                                        props.put(ContentModel.PROP_MODEL_AUTHOR, modelDefinition.getAuthor());
                                        props.put(ContentModel.PROP_MODEL_PUBLISHED_DATE, modelDefinition.getPublishedDate());
                                        props.put(ContentModel.PROP_MODEL_VERSION, modelDefinition.getVersion());
                                        nodeService.setProperties(nodeRef, props);
                                        
                                        // Validate model against dictionary - could be new, unchanged or updated
                                        if (doValidation == true)
                                        {
                                        	validateModel(modelDefinition.getName(), m2Model, compiledModel);
                                        }
                                        
                                        // invalidate - to force lazy re-init
                                        //dictionaryDAO.destroy();
                                        
                                        if (logger.isTraceEnabled())
                                        {
                                            logger.trace("beforeCommit: activating nodeRef="+nodeRef+" ("+modelDefinition.getName()+") ["+AlfrescoTransactionSupport.getTransactionId()+"]");
                                        }
                                    }
                                }
                                else
                                {
                                    QName modelName = (QName)nodeService.getProperty(nodeRef, ContentModel.PROP_MODEL_NAME);
                                    if (modelName != null)
                                    {
                                        // Validate model delete against usages - content and/or workflows
                                        validateModelDelete(modelName);
                                        
                                        // invalidate - to force lazy re-init
                                        //dictionaryDAO.destroy();
                                        
                                        if (logger.isTraceEnabled())
                                        {
                                            logger.trace("beforeCommit: deactivating nodeRef="+nodeRef+" ("+modelName+") ["+AlfrescoTransactionSupport.getTransactionId()+"]");
                                        }
                                    }
                                }
                            }
                            
                            return null; 
                        }
                    }, tenantSystemUserName);
                }
            }
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
    private void validateModelDelete(final QName modelName)
    {
        // TODO add model locking during delete (would need to be tenant-aware & cluster-aware) to avoid potential 
        //      for concurrent addition of new content/workflow as model is being deleted
        
        final Collection<NamespaceDefinition> namespaceDefs;
        final Collection<TypeDefinition> typeDefs;
        final Collection<AspectDefinition> aspectDefs;
        
        try
        {
            namespaceDefs = dictionaryDAO.getNamespaces(modelName);
            typeDefs = dictionaryDAO.getTypes(modelName);
            aspectDefs = dictionaryDAO.getAspects(modelName);
        }
        catch (DictionaryException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Dictionary model '" + modelName + "' does not exist ... skip delete validation : " + e);
            }
            return;
        }
        
        // TODO - in case of MT we do not currently allow deletion of an overridden model (with usages) ... but could allow if (re-)inherited model is equivalent to an incremental update only ?
        validateModelDelete(namespaceDefs, typeDefs, aspectDefs, false);
        
        if (tenantService.isEnabled() && tenantService.isTenantUser() == false)
        {
            // shared model - need to check all tenants (whether enabled or disabled) unless they have overridden
            List<Tenant> tenants = tenantAdminService.getAllTenants();
            for (Tenant tenant : tenants)
            {
                // validate model delete within context of tenant domain
                AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork()
                    {
                        if (dictionaryDAO.isModelInherited(modelName))
                        {
                            validateModelDelete(namespaceDefs, typeDefs, aspectDefs, true);
                        }
                        return null;
                    }
                }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain()));
            }
        }
    }
    
    private void validateModelDelete(Collection<NamespaceDefinition> namespaceDefs, Collection<TypeDefinition> typeDefs, Collection<AspectDefinition> aspectDefs, boolean sharedModel)
    {
        String tenantDomain = TenantService.DEFAULT_DOMAIN;
        if (sharedModel)
        {
            tenantDomain = " for tenant [" + tenantService.getCurrentUserDomain() + "]";
        }
        
        List<WorkflowDefinition> workflowDefs = workflowService.getDefinitions();
        
        if (workflowDefs.size() > 0)
        {
            if (namespaceDefs.size() > 0)
            {
                // check workflow namespace usage
                for (WorkflowDefinition workflowDef : workflowDefs)
                {
                    String workflowDefName = workflowDef.getName();
                    
                    String workflowNamespaceURI = null;
                    try
                    {
                        workflowNamespaceURI = QName.createQName(BPMEngineRegistry.getLocalId(workflowDefName), namespaceService).getNamespaceURI();
                    }
                    catch (NamespaceException ne)
                    {
                        logger.warn("Skipped workflow when validating model delete - unknown namespace: "+ne);
                        continue;
                    }
                    
                    for (NamespaceDefinition namespaceDef : namespaceDefs)
                    {
                        if (workflowNamespaceURI.equals(namespaceDef.getUri()))
                        {
                            throw new AlfrescoRuntimeException("Failed to validate model delete" + tenantDomain + " - found workflow process definition " + workflowDefName + " using model namespace '" + namespaceDef.getUri() + "'");
                        }
                    }
                }
            }
        }
        
        // check for type usages
        for (TypeDefinition type : typeDefs)
        {
            validateClass(tenantDomain, type);
        }
        
        // check for aspect usages
        for (AspectDefinition aspect : aspectDefs)
        {
            validateClass(tenantDomain, aspect);
        }
    }
    
    private void validateClass(String tenantDomain, ClassDefinition classDef)
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
            try
            {
                if (rs.length() > 0)
                {
                    throw new AlfrescoRuntimeException("Failed to validate model delete" + tenantDomain + " - found " + rs.length() + " nodes in store " + store + " with " + classType + " '" + className + "'" );
                }
            }
            finally
            {
                rs.close();
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
                    throw new AlfrescoRuntimeException("Failed to validate model delete" + tenantDomain + " - found task definition in workflow " + workflowDef.getName() + " with " + classType + " '" + className + "'");
                }
            }
        }
    }
    
    /**
     * validate against dictionary
     * 
     * if new model 
     * then nothing to validate
     * 
     * else if an existing model 
     * then could be updated (or unchanged) so validate to currently only allow incremental updates
     *   - addition of new types, aspects (except default aspects), properties, associations
     *   - no deletion of types, aspects or properties or associations
     *   - no addition, update or deletion of default/mandatory aspects
     * 
     * @paramn modelName
     * @param newOrUpdatedModel
     */
    private void validateModel(QName modelName, M2Model model, CompiledModel compiledModel)
    {
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(model);
        
        for (M2ModelDiff modelDiff : modelDiffs)
        {
            if (modelDiff.getDiffType().equals(M2ModelDiff.DIFF_DELETED))
            {
                // TODO - check tenants if model is shared / inherited
                if (modelDiff.getElementType().equals(M2ModelDiff.TYPE_PROPERTY))
                {
                    validatePropertyDelete(modelName, modelDiff.getElementName(), false);
                    
                    continue;
                }
                else if (modelDiff.getElementType().equals(M2ModelDiff.TYPE_CONSTRAINT))
                {
                    validateConstraintDelete(compiledModel, modelDiff.getElementName(), false);
                    continue;
                }
                else
                {
                    throw new AlfrescoRuntimeException("Failed to validate model update - found deleted " + modelDiff.getElementType() + " '" + modelDiff.getElementName() + "'");
                }
            }
            
            if (modelDiff.getDiffType().equals(M2ModelDiff.DIFF_UPDATED))
            {
                throw new AlfrescoRuntimeException("Failed to validate model update - found non-incrementally updated " + modelDiff.getElementType() + " '" + modelDiff.getElementName() + "'");
            }
        }
        
        // TODO validate that any deleted constraints are not being referenced - else currently will become anon - or push down into model compilation (check backwards compatibility ...)
    }
    
    private void validatePropertyDelete(QName modelName, QName propertyName, boolean sharedModel)
    {
        String tenantDomain = TenantService.DEFAULT_DOMAIN;
        if (sharedModel)
        {
            tenantDomain = " for tenant [" + tenantService.getCurrentUserDomain() + "]";
        }
        
        boolean found = false;
        
        // check for property usages
        for (PropertyDefinition prop : dictionaryDAO.getProperties(modelName, null))
        {
            // TODO ... match property
            if (prop.getName().equals(propertyName))
            {
                // found
                found = true;
                validateIndexedProperty(tenantDomain, prop);
                break;
            }
        }
        
        if (! found)
        {
            throw new AlfrescoRuntimeException("Failed to validate property delete" + tenantDomain + " - property definition '" + propertyName + "' not defined in model '" + modelName + "'");
        }
    }
    
    private void validateIndexedProperty(String tenantDomain, PropertyDefinition propDef)
    {
        QName propName = propDef.getName();
        
        if (! propDef.isIndexed())
        {
            // TODO ... implement DB-level referential integrity
            throw new AlfrescoRuntimeException("Failed to validate property delete" + tenantDomain + " - cannot delete unindexed property definition '" + propName);
        }
        
        for (String storeUrl : this.storeUrls)
        {
            StoreRef store = new StoreRef(storeUrl);
            
            // search for indexed PROPERTY
            String escapePropName = propName.toPrefixString().replace(":", "\\:");
            ResultSet rs = searchService.query(store, SearchService.LANGUAGE_LUCENE, "@"+escapePropName+":*");
            try
            {
                if (rs.length() > 0)
                {
                    throw new AlfrescoRuntimeException("Failed to validate property delete" + tenantDomain + " - found " + rs.length() + " nodes in store " + store + " with PROPERTY '" + propName + "'" );
                }
            }
            finally
            {
                rs.close();
            }
        }
    }
    
    // validate delete of a referencable constraint def
    private void validateConstraintDelete(CompiledModel compiledModel, QName constraintName, boolean sharedModel)
    {
        String tenantDomain = TenantService.DEFAULT_DOMAIN;
        if (sharedModel)
        {
            tenantDomain = " for tenant [" + tenantService.getCurrentUserDomain() + "]";
        }
        
        Set<QName> referencedBy = new HashSet<QName>(0);
        
        // check for references to constraint definition
        // note: could be anon prop constraint (if no referenceable constraint)
        Collection<QName> allModels = dictionaryDAO.getModels();
        for (QName model : allModels)
        {
            Collection<PropertyDefinition> propDefs = null;
            if (compiledModel.getModelDefinition().getName().equals(model))
            {
                // TODO deal with multiple pending model updates
                propDefs = compiledModel.getProperties();
            }
            else
            {
                propDefs = dictionaryDAO.getProperties(model);
            }
            
            for (PropertyDefinition propDef : propDefs)
            {
                for (ConstraintDefinition conDef : propDef.getConstraints())
                {
                    if (constraintName.equals(conDef.getRef()))
                    {
                        referencedBy.add(conDef.getName());
                    }
                }
            }
        }
        
        if (referencedBy.size() == 1)
        {
            throw new AlfrescoRuntimeException("Failed to validate constraint delete" + tenantDomain + " - constraint definition '" + constraintName + "' is being referenced by '" + referencedBy.toArray()[0] + "' property constraint");
        }
        else if (referencedBy.size() > 1)
        {
            throw new AlfrescoRuntimeException("Failed to validate constraint delete" + tenantDomain + " - constraint definition '" + constraintName + "' is being referenced by " + referencedBy.size() + " property constraints");
        }
    }
}
