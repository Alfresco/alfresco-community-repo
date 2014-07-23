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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
    
    /** The tenant service */
    private TenantService tenantService;
    
    private TransactionService transactionService;
    
    private JobLockService jobLockService;
    
    /** Transaction listener */
    private DictionaryModelTypeTransactionListener transactionListener;

    private ModelValidator modelValidator;

    /** Validation marker */
    private boolean doValidation = true;
    
    /**
     * Set the dictionary DAO
     */
    public void setDictionaryDAO(DictionaryDAO dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }

    public void setModelValidator(ModelValidator modelValidator)
    {
		this.modelValidator = modelValidator;
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
     * Set the tenant service
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
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

    public void setDoValidation(boolean doValidation)
    {
    	this.doValidation = doValidation;
    }    
    
    /**
     * The initialise method
     */
    public void init()
    {
    	if(logger.isDebugEnabled())
    	{
    		logger.debug("init : bind class behaviours for " + ContentModel.TYPE_DICTIONARY_MODEL);
    	}
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
        
        Boolean value = (Boolean)nodeService.getProperty(nodeRef, ContentModel.PROP_MODEL_ACTIVE);
        if ((value != null) && (value == true))
        {
        	queueModel(nodeRef);
        }
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
                modelValidator.validateModelDelete(modelName);
                
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
                    systemTenants.add(tenantDomain);
                }
            }
            
            if (pendingDeleteModels != null)
            {
                // unbind the resource from the transaction
                AlfrescoTransactionSupport.unbindResource(KEY_PENDING_DELETE_MODELS);
                
                for (NodeRef nodeRef : pendingDeleteModels)
                {
                    String tenantDomain = tenantService.getDomain(nodeRef.getStoreRef().getIdentifier());
                    systemTenants.add(tenantDomain);
                }
            }
            
            if (systemTenants.size() > 0)
            {
                for (final String tenantName : systemTenants)
                {
                    RetryingTransactionCallback<Void> work = new RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            return TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
                            {
                                public Void doWork()
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
                            }, tenantName);
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
                                        CompiledModel compiledModel= m2Model.compile(dictionaryDAO, namespaceDAO, true);
                                        ModelDefinition modelDefinition = compiledModel.getModelDefinition();
                                        
                                        // Update the meta data for the model
                                        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
                                        props.put(ContentModel.PROP_MODEL_NAME, modelDefinition.getName());
                                        props.put(ContentModel.PROP_MODEL_DESCRIPTION, modelDefinition.getDescription(null));
                                        props.put(ContentModel.PROP_MODEL_AUTHOR, modelDefinition.getAuthor());
                                        props.put(ContentModel.PROP_MODEL_PUBLISHED_DATE, modelDefinition.getPublishedDate());
                                        props.put(ContentModel.PROP_MODEL_VERSION, modelDefinition.getVersion());
                                       
                                        nodeService.setProperties(nodeRef, props);
                                        
                                        // Validate model against dictionary - could be new, unchanged or updated
                                        if (doValidation == true)
                                        {
                                        	modelValidator.validateModel(compiledModel);
                                        }

                                        // invalidate - to force lazy re-init
                                        // TODO
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
                                    	modelValidator.validateModelDelete(modelName);
                                        
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
}
