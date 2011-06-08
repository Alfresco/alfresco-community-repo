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
package org.alfresco.repo.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.BeforeAddAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeCreateNodeAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeCreateStorePolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeRemoveAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeUpdateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodeAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateStorePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnRemoveAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnRestoreNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.AssociationPolicyDelegate;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides common functionality for
 * {@link org.alfresco.service.cmr.repository.NodeService} implementations.
 * <p>
 * Some of the overloaded simpler versions of methods are implemented by passing
 * through the defaults as required.
 * <p>
 * The callback handling is also provided as a convenience for implementations.
 * 
 * @author Derek Hulley
 */
public abstract class AbstractNodeServiceImpl implements NodeService
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(AbstractNodeServiceImpl.class);
    
    /** a uuid identifying this unique instance */
    private String uuid;
    /** controls policy delegates */
    private PolicyComponent policyComponent;
    protected DictionaryService dictionaryService;
    protected TransactionService transactionService;
    protected TenantService tenantService;
    protected List<String> storesToIgnorePolicies = new ArrayList<String>(0);

    /*
     * Policy delegates
     */
    private ClassPolicyDelegate<BeforeCreateStorePolicy> beforeCreateStoreDelegate;
    private ClassPolicyDelegate<OnCreateStorePolicy> onCreateStoreDelegate;
    private ClassPolicyDelegate<BeforeCreateNodePolicy> beforeCreateNodeDelegate;
    private ClassPolicyDelegate<OnCreateNodePolicy> onCreateNodeDelegate;
    private ClassPolicyDelegate<OnMoveNodePolicy> onMoveNodeDelegate;
    private ClassPolicyDelegate<BeforeUpdateNodePolicy> beforeUpdateNodeDelegate;
    private ClassPolicyDelegate<OnUpdateNodePolicy> onUpdateNodeDelegate;
    private ClassPolicyDelegate<OnUpdatePropertiesPolicy> onUpdatePropertiesDelegate;
    private ClassPolicyDelegate<BeforeDeleteNodePolicy> beforeDeleteNodeDelegate;
    private ClassPolicyDelegate<OnDeleteNodePolicy> onDeleteNodeDelegate;
    private ClassPolicyDelegate<OnRestoreNodePolicy> onRestoreNodePolicy;
    private ClassPolicyDelegate<BeforeAddAspectPolicy> beforeAddAspectDelegate;
    private ClassPolicyDelegate<OnAddAspectPolicy> onAddAspectDelegate;
    private ClassPolicyDelegate<BeforeRemoveAspectPolicy> beforeRemoveAspectDelegate;
    private ClassPolicyDelegate<OnRemoveAspectPolicy> onRemoveAspectDelegate;
    private AssociationPolicyDelegate<BeforeCreateNodeAssociationPolicy> beforeCreateNodeAssociationDelegate;
    private AssociationPolicyDelegate<OnCreateNodeAssociationPolicy> onCreateNodeAssociationDelegate;
    private AssociationPolicyDelegate<OnCreateChildAssociationPolicy> onCreateChildAssociationDelegate;
    private AssociationPolicyDelegate<BeforeDeleteChildAssociationPolicy> beforeDeleteChildAssociationDelegate;
    private AssociationPolicyDelegate<OnDeleteChildAssociationPolicy> onDeleteChildAssociationDelegate;
    private AssociationPolicyDelegate<OnCreateAssociationPolicy> onCreateAssociationDelegate;
    private AssociationPolicyDelegate<OnDeleteAssociationPolicy> onDeleteAssociationDelegate;

    /**
     * 
     */
    protected AbstractNodeServiceImpl()
    {
        this.uuid = GUID.generate();
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setStoresToIgnorePolicies(List<String> storesToIgnorePolicies)
    {
        this.storesToIgnorePolicies = storesToIgnorePolicies;
    }

    /**
     * Checks equality by type and uuid
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (!(obj instanceof AbstractNodeServiceImpl))
        {
            return false;
        }
        AbstractNodeServiceImpl that = (AbstractNodeServiceImpl) obj;
        return this.uuid.equals(that.uuid);
    }
    
    /**
     * @see #uuid
     */
    public int hashCode()
    {
        return uuid.hashCode();
    }

    /**
     * Registers the node policies as well as node indexing behaviour if the
     * {@link #setIndexer(Indexer) indexer} is present.
     */
    public void init()
    {
        // Register the various policies
        beforeCreateStoreDelegate = policyComponent.registerClassPolicy(NodeServicePolicies.BeforeCreateStorePolicy.class);
        onCreateStoreDelegate = policyComponent.registerClassPolicy(NodeServicePolicies.OnCreateStorePolicy.class);
        beforeCreateNodeDelegate = policyComponent.registerClassPolicy(NodeServicePolicies.BeforeCreateNodePolicy.class);
        onCreateNodeDelegate = policyComponent.registerClassPolicy(NodeServicePolicies.OnCreateNodePolicy.class);
        onMoveNodeDelegate = policyComponent.registerClassPolicy(NodeServicePolicies.OnMoveNodePolicy.class);
        beforeUpdateNodeDelegate = policyComponent.registerClassPolicy(NodeServicePolicies.BeforeUpdateNodePolicy.class);
        onUpdateNodeDelegate = policyComponent.registerClassPolicy(NodeServicePolicies.OnUpdateNodePolicy.class);
        onUpdatePropertiesDelegate = policyComponent.registerClassPolicy(NodeServicePolicies.OnUpdatePropertiesPolicy.class);
        beforeDeleteNodeDelegate = policyComponent.registerClassPolicy(NodeServicePolicies.BeforeDeleteNodePolicy.class);
        onDeleteNodeDelegate = policyComponent.registerClassPolicy(NodeServicePolicies.OnDeleteNodePolicy.class);
        onRestoreNodePolicy = policyComponent.registerClassPolicy(NodeServicePolicies.OnRestoreNodePolicy.class);

        beforeAddAspectDelegate = policyComponent.registerClassPolicy(NodeServicePolicies.BeforeAddAspectPolicy.class);
        onAddAspectDelegate = policyComponent.registerClassPolicy(NodeServicePolicies.OnAddAspectPolicy.class);
        beforeRemoveAspectDelegate = policyComponent.registerClassPolicy(NodeServicePolicies.BeforeRemoveAspectPolicy.class);
        onRemoveAspectDelegate = policyComponent.registerClassPolicy(NodeServicePolicies.OnRemoveAspectPolicy.class);

        beforeCreateNodeAssociationDelegate = policyComponent.registerAssociationPolicy(NodeServicePolicies.BeforeCreateNodeAssociationPolicy.class);
        onCreateNodeAssociationDelegate = policyComponent.registerAssociationPolicy(NodeServicePolicies.OnCreateNodeAssociationPolicy.class);
        onCreateChildAssociationDelegate = policyComponent.registerAssociationPolicy(NodeServicePolicies.OnCreateChildAssociationPolicy.class);
        beforeDeleteChildAssociationDelegate = policyComponent.registerAssociationPolicy(NodeServicePolicies.BeforeDeleteChildAssociationPolicy.class);
        onDeleteChildAssociationDelegate = policyComponent.registerAssociationPolicy(NodeServicePolicies.OnDeleteChildAssociationPolicy.class);

        onCreateAssociationDelegate = policyComponent.registerAssociationPolicy(NodeServicePolicies.OnCreateAssociationPolicy.class);
        onDeleteAssociationDelegate = policyComponent.registerAssociationPolicy(NodeServicePolicies.OnDeleteAssociationPolicy.class);
    }
    
    private boolean ignorePolicy(StoreRef storeRef)
    {
        return (storesToIgnorePolicies.contains(tenantService.getBaseName(storeRef).toString()));
    }
    
    private boolean ignorePolicy(NodeRef nodeRef)
    {
        return (storesToIgnorePolicies.contains(tenantService.getBaseName(nodeRef.getStoreRef()).toString()));
    }
    
    /**
     * @see NodeServicePolicies.BeforeCreateStorePolicy#beforeCreateStore(QName,
     *      StoreRef)
     */
    protected void invokeBeforeCreateStore(QName nodeTypeQName, StoreRef storeRef)
    {
        if (ignorePolicy(storeRef))
        {
            return;
        }
        
        NodeServicePolicies.BeforeCreateStorePolicy policy = this.beforeCreateStoreDelegate.get(nodeTypeQName);
        policy.beforeCreateStore(nodeTypeQName, storeRef);
    }

    /**
     * @see NodeServicePolicies.OnCreateStorePolicy#onCreateStore(NodeRef)
     */
    protected void invokeOnCreateStore(NodeRef rootNodeRef)
    {
        if (ignorePolicy(rootNodeRef))
        {
            return;
        }
        
        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(rootNodeRef);
        // execute policy for node type and aspects
        NodeServicePolicies.OnCreateStorePolicy policy = onCreateStoreDelegate.get(qnames);
        policy.onCreateStore(rootNodeRef);
    }

    /**
     * @see NodeServicePolicies.BeforeCreateNodePolicy#beforeCreateNode(NodeRef,
     *      QName, QName, QName)
     */
    protected void invokeBeforeCreateNode(NodeRef parentNodeRef, QName assocTypeQName, QName assocQName, QName childNodeTypeQName)
    {
        if (ignorePolicy(parentNodeRef))
        {
            return;
        }
        
        // execute policy for node type
        NodeServicePolicies.BeforeCreateNodePolicy policy = beforeCreateNodeDelegate.get(childNodeTypeQName);
        policy.beforeCreateNode(parentNodeRef, assocTypeQName, assocQName, childNodeTypeQName);
    }

    /**
     * @see NodeServicePolicies.OnCreateNodePolicy#onCreateNode(ChildAssociationRef)
     */
    protected void invokeOnCreateNode(ChildAssociationRef childAssocRef)
    {
        NodeRef childNodeRef = childAssocRef.getChildRef();
        
        if (ignorePolicy(childNodeRef))
        {
            return;
        }
        
        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(childNodeRef);
        // execute policy for node type and aspects
        NodeServicePolicies.OnCreateNodePolicy policy = onCreateNodeDelegate.get(childNodeRef, qnames);
        policy.onCreateNode(childAssocRef);
    }

    /**
     * @see NodeServicePolicies.OnMoveNodePolicy#onMoveNode(ChildAssociationRef, ChildAssociationRef)
     */
    protected void invokeOnMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        NodeRef childNodeRef = newChildAssocRef.getChildRef();
        
        if (ignorePolicy(childNodeRef))
        {
            return;
        }
        
        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(childNodeRef);
        // execute policy for node type and aspects
        NodeServicePolicies.OnMoveNodePolicy policy = onMoveNodeDelegate.get(childNodeRef, qnames);
        policy.onMoveNode(oldChildAssocRef, newChildAssocRef);
    }

    /**
     * @see NodeServicePolicies.BeforeUpdateNodePolicy#beforeUpdateNode(NodeRef)
     */
    protected void invokeBeforeUpdateNode(NodeRef nodeRef)
    {
        if (ignorePolicy(nodeRef))
        {
            return;
        }
        
        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(nodeRef);
        // execute policy for node type and aspects
        NodeServicePolicies.BeforeUpdateNodePolicy policy = beforeUpdateNodeDelegate.get(nodeRef, qnames);
        policy.beforeUpdateNode(nodeRef);
    }

    /**
     * @see NodeServicePolicies.OnUpdateNodePolicy#onUpdateNode(NodeRef)
     */
    protected void invokeOnUpdateNode(NodeRef nodeRef)
    {
        if (ignorePolicy(nodeRef))
        {
            return;
        }
        
        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(nodeRef);
        // execute policy for node type and aspects
        NodeServicePolicies.OnUpdateNodePolicy policy = onUpdateNodeDelegate.get(nodeRef, qnames);
        policy.onUpdateNode(nodeRef);
    }
    
    /**
     * @see NodeServicePolicies.OnUpdateProperties#onUpdatePropertiesPolicy(NodeRef, Map, Map)
     */
    protected void invokeOnUpdateProperties(
            NodeRef nodeRef,
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        if (ignorePolicy(nodeRef))
        {
            return;
        }
        
        // Some logging so we can see which properties have been modified
        if (logger.isDebugEnabled() == true)
        {
            if (before == null)
            {
                logger.debug("The properties are being set for the first time.  (nodeRef=" + nodeRef.toString() + ")");
            }
            else if (after == null)
            {
                logger.debug("All the properties are being cleared.  (nodeRef=" + nodeRef.toString() + ")");                
            }
            else
            {
                logger.debug("The following properties have been updated:  (nodeRef=" + nodeRef.toString() + ")");
                for (Map.Entry<QName, Serializable> entry : after.entrySet())
                {
                    Serializable beforeValue = before.get(entry.getKey());
                    if (beforeValue == null)
                    {
                        // Property has been set for the first time
                        logger.debug("   - The property " + entry.getKey().toString() + " has been set for the first time.");
                    }
                    else
                    {
                        // Compare the before and after value
                        if (beforeValue.equals(entry.getValue()) == false)
                        {
                            logger.debug("   - The property " + entry.getKey().toString() + " has been updated.");
                        }
                    }
                    
                }
            }
            
        }
                
        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(nodeRef);
        // execute policy for node type and aspects
        NodeServicePolicies.OnUpdatePropertiesPolicy policy = onUpdatePropertiesDelegate.get(nodeRef, qnames);
        policy.onUpdateProperties(nodeRef, before, after);
    }

    /**
     * @see NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(NodeRef)
     */
    protected void invokeBeforeDeleteNode(NodeRef nodeRef)
    {
        if (ignorePolicy(nodeRef))
        {
            return;
        }
        
        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(nodeRef);
        // execute policy for node type and aspects
        NodeServicePolicies.BeforeDeleteNodePolicy policy = beforeDeleteNodeDelegate.get(nodeRef, qnames);
        policy.beforeDeleteNode(nodeRef);
    }

    /**
     * @see NodeServicePolicies.OnDeleteNodePolicy#onDeleteNode(ChildAssociationRef)
     */
    protected void invokeOnDeleteNode(ChildAssociationRef childAssocRef, QName childNodeTypeQName, Set<QName> childAspectQnames, boolean isArchivedNode)
    {
        NodeRef childNodeRef = childAssocRef.getChildRef();
        
        Set<QName> qnames = null;
        
        if (ignorePolicy(childNodeRef))
        {
            // special case
            if (childAspectQnames.contains(ContentModel.ASPECT_VERSIONABLE) || childNodeTypeQName.equals(ContentModel.ASPECT_VERSIONABLE))
            {
                qnames = new HashSet<QName>(1);
                qnames.add(ContentModel.ASPECT_VERSIONABLE);
            }
        }
        else
        {
            // get qnames to invoke against
            qnames = new HashSet<QName>(childAspectQnames.size() + 1);
            qnames.addAll(childAspectQnames);
            qnames.add(childNodeTypeQName);
        }
        
        if (qnames != null)
        {
            // execute policy for node type and aspects
            NodeServicePolicies.OnDeleteNodePolicy policy = onDeleteNodeDelegate.get(childAssocRef.getChildRef(), qnames);
            policy.onDeleteNode(childAssocRef, isArchivedNode);
        }
    }

    /**
     * @see NodeServicePolicies.OnRestoreNodePolicy#onDeleteNode(ChildAssociationRef)
     */
    protected void invokeOnRestoreNode(ChildAssociationRef childAssocRef)
    {
        NodeRef childNodeRef = childAssocRef.getChildRef();
        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(childNodeRef);
        // execute policy for node type and aspects
        NodeServicePolicies.OnRestoreNodePolicy policy = onRestoreNodePolicy.get(childAssocRef.getChildRef(), qnames);
        policy.onRestoreNode(childAssocRef);
    }
    
    /**
     * @see NodeServicePolicies.BeforeAddAspectPolicy#beforeAddAspect(NodeRef,
     *      QName)
     */
    protected void invokeBeforeAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (ignorePolicy(nodeRef))
        {
            return;
        }
        
        NodeServicePolicies.BeforeAddAspectPolicy policy = beforeAddAspectDelegate.get(nodeRef, aspectTypeQName);
        policy.beforeAddAspect(nodeRef, aspectTypeQName);
    }

    /**
     * @see NodeServicePolicies.OnAddAspectPolicy#onAddAspect(NodeRef, QName)
     */
    protected void invokeOnAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (ignorePolicy(nodeRef))
        {
            return;
        }
        
        NodeServicePolicies.OnAddAspectPolicy policy = onAddAspectDelegate.get(nodeRef, aspectTypeQName);
        policy.onAddAspect(nodeRef, aspectTypeQName);
    }

    /**
     * @see NodeServicePolicies.BeforeRemoveAspectPolicy#BeforeRemoveAspect(NodeRef,
     *      QName)
     */
    protected void invokeBeforeRemoveAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (ignorePolicy(nodeRef))
        {
            return;
        }
        
        NodeServicePolicies.BeforeRemoveAspectPolicy policy = beforeRemoveAspectDelegate.get(nodeRef, aspectTypeQName);
        policy.beforeRemoveAspect(nodeRef, aspectTypeQName);
    }

    /**
     * @see NodeServicePolicies.OnRemoveAspectPolicy#onRemoveAspect(NodeRef,
     *      QName)
     */
    protected void invokeOnRemoveAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (ignorePolicy(nodeRef))
        {
            return;
        }
        
        NodeServicePolicies.OnRemoveAspectPolicy policy = onRemoveAspectDelegate.get(nodeRef, aspectTypeQName);
        policy.onRemoveAspect(nodeRef, aspectTypeQName);
    }
    
    /**
     * @see NodeServicePolicies.BeforeCreateNodeAssociationPolicy#beforeCreateChildAssociation(NodeRef,
     *      NodeRef, QName, QName)
     */
    protected void invokeBeforeCreateNodeAssociation(NodeRef parentNodeRef, QName assocTypeQName, QName assocQName)
    {
        if (ignorePolicy(parentNodeRef))
        {
            return;
        }
        
        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(parentNodeRef);
        // execute policy for node type
        NodeServicePolicies.BeforeCreateNodeAssociationPolicy policy = beforeCreateNodeAssociationDelegate.get(parentNodeRef, qnames, assocTypeQName);
        policy.beforeCreateNodeAssociation(parentNodeRef, assocTypeQName, assocQName);
    }

    /**
     * @see NodeServicePolicies.OnCreateNodeAssociationPolicy#onCreateChildAssociation(ChildAssociationRef)
     */
    protected void invokeOnCreateNodeAssociation(ChildAssociationRef childAssocRef)
    {
        // Get the parent reference and the assoc type qName
        NodeRef parentNodeRef = childAssocRef.getParentRef();
        
        if (ignorePolicy(parentNodeRef))
        {
            return;
        }
        
        QName assocTypeQName = childAssocRef.getTypeQName();
        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(parentNodeRef);
        // execute policy for node type and aspects
        NodeServicePolicies.OnCreateNodeAssociationPolicy policy = onCreateNodeAssociationDelegate.get(parentNodeRef, qnames, assocTypeQName);
        policy.onCreateNodeAssociation(childAssocRef);
    }

    /**
     * @see NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(ChildAssociationRef)
     */
    protected void invokeOnCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        // Get the parent reference and the assoc type qName
        NodeRef parentNodeRef = childAssocRef.getParentRef();
        
        if (ignorePolicy(parentNodeRef))
        {
            return;
        }
        
        QName assocTypeQName = childAssocRef.getTypeQName();
        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(parentNodeRef);
        // execute policy for node type and aspects
        NodeServicePolicies.OnCreateChildAssociationPolicy policy = onCreateChildAssociationDelegate.get(parentNodeRef, qnames, assocTypeQName);
        policy.onCreateChildAssociation(childAssocRef, isNewNode);
    }

    /**
     * @see NodeServicePolicies.BeforeDeleteChildAssociationPolicy#beforeDeleteChildAssociation(ChildAssociationRef)
     */
    protected void invokeBeforeDeleteChildAssociation(ChildAssociationRef childAssocRef)
    {
        NodeRef parentNodeRef = childAssocRef.getParentRef();
        
        if (ignorePolicy(parentNodeRef))
        {
            return;
        }
        
        QName assocTypeQName = childAssocRef.getTypeQName();
        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(parentNodeRef);
        // execute policy for node type and aspects
        NodeServicePolicies.BeforeDeleteChildAssociationPolicy policy = beforeDeleteChildAssociationDelegate.get(parentNodeRef, qnames, assocTypeQName);
        policy.beforeDeleteChildAssociation(childAssocRef);
    }

    /**
     * @see NodeServicePolicies.OnDeleteChildAssociationPolicy#onDeleteChildAssociation(ChildAssociationRef)
     */
    protected void invokeOnDeleteChildAssociation(ChildAssociationRef childAssocRef)
    {
        NodeRef parentNodeRef = childAssocRef.getParentRef();
        
        if (ignorePolicy(parentNodeRef))
        {
            return;
        }
        
        QName assocTypeQName = childAssocRef.getTypeQName();
        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(parentNodeRef);
        // execute policy for node type and aspects
        NodeServicePolicies.OnDeleteChildAssociationPolicy policy = onDeleteChildAssociationDelegate.get(parentNodeRef, qnames, assocTypeQName);
        policy.onDeleteChildAssociation(childAssocRef);
    }

    /**
     * @see NodeServicePolicies.OnCreateAssociationPolicy#onCreateAssociation(NodeRef, NodeRef, QName)
     */
    protected void invokeOnCreateAssociation(AssociationRef nodeAssocRef)
    {
        NodeRef sourceNodeRef = nodeAssocRef.getSourceRef();
        
        if (ignorePolicy(sourceNodeRef))
        {
            return;
        }
        
        QName assocTypeQName = nodeAssocRef.getTypeQName();
        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(sourceNodeRef);
        // execute policy for node type and aspects
        NodeServicePolicies.OnCreateAssociationPolicy policy = onCreateAssociationDelegate.get(sourceNodeRef, qnames, assocTypeQName);
        policy.onCreateAssociation(nodeAssocRef);
    }

    /**
     * @see NodeServicePolicies.OnDeleteAssociationPolicy#onDeleteAssociation(AssociationRef)
     */
    protected void invokeOnDeleteAssociation(AssociationRef nodeAssocRef)
    {
        NodeRef sourceNodeRef = nodeAssocRef.getSourceRef();
        
        if (ignorePolicy(sourceNodeRef))
        {
            return;
        }
        
        QName assocTypeQName = nodeAssocRef.getTypeQName();
        // get qnames to invoke against
        Set<QName> qnames = getTypeAndAspectQNames(sourceNodeRef);
        // execute policy for node type and aspects
        NodeServicePolicies.OnDeleteAssociationPolicy policy = onDeleteAssociationDelegate.get(sourceNodeRef, qnames, assocTypeQName);
        policy.onDeleteAssociation(nodeAssocRef);
    }

    /**
     * Get all aspect and node type qualified names
     * 
     * @param nodeRef
     *            the node we are interested in
     * @return Returns a set of qualified names containing the node type and all
     *         the node aspects, or null if the node no longer exists
     */
    protected Set<QName> getTypeAndAspectQNames(NodeRef nodeRef)
    {
        Set<QName> qnames = null;
        try
        {
            Set<QName> aspectQNames = getAspects(nodeRef);
            
            QName typeQName = getType(nodeRef);
            
            qnames = new HashSet<QName>(aspectQNames.size() + 1);
            qnames.addAll(aspectQNames);
            qnames.add(typeQName);
        }
        catch (InvalidNodeRefException e)
        {
            qnames = Collections.emptySet();
        }
        // done
        return qnames;
    }

    /**
     * Fetches any pre-defined node uuid from the properties, but <b>does not generate a new uuid</b>.
     * 
     * @param preCreationProperties the properties that will be applied to the node
     * @return Returns the ID to create the node with, or <tt>null</tt> if a standard GUID should be used
     */
    protected String generateGuid(Map<QName, Serializable> preCreationProperties)
    {
        String uuid = (String) preCreationProperties.get(ContentModel.PROP_NODE_UUID);
        if (uuid != null && uuid.length() > 50)
        {
            throw new IllegalArgumentException("Explicit UUID may not be greater than 50 characters: " + uuid);
        }
        // done
        return uuid;
    }
    
    /**
     * Defers to the pattern matching overload
     * 
     * @see RegexQNamePattern#MATCH_ALL
     * @see NodeService#getParentAssocs(NodeRef, QNamePattern, QNamePattern)
     */
    public List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef) throws InvalidNodeRefException
    {
        return getParentAssocs(nodeRef, RegexQNamePattern.MATCH_ALL, RegexQNamePattern.MATCH_ALL);
    }

    /**
     * Defers to the pattern matching overload
     * 
     * @see RegexQNamePattern#MATCH_ALL
     * @see NodeService#getChildAssocs(NodeRef, QNamePattern, QNamePattern)
     */
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef) throws InvalidNodeRefException
    {
        return getChildAssocs(nodeRef, RegexQNamePattern.MATCH_ALL, RegexQNamePattern.MATCH_ALL);
    }
    
    protected Map<QName, Serializable> getDefaultProperties(QName typeQName)
    {
        ClassDefinition classDefinition = this.dictionaryService.getClass(typeQName);
        if (classDefinition == null)
        {
            return Collections.emptyMap();
        }
        return getDefaultProperties(classDefinition);
    }
    
    /**
     * Sets the default property values
     * 
     * @param classDefinition       the model type definition for which to get defaults
     * @param properties            the properties of the node
     */
    protected Map<QName, Serializable> getDefaultProperties(ClassDefinition classDefinition)
    {
        PropertyMap properties = new PropertyMap();
        for (Map.Entry<QName, Serializable> entry : classDefinition.getDefaultValues().entrySet())
        {
            Serializable value = entry.getValue();
            
            // Check the type of the default property
            PropertyDefinition prop = this.dictionaryService.getProperty(entry.getKey());
            if (prop == null)
            {
                // dictionary doesn't have a default value present
                continue;
            }

            // TODO: what other conversions are necessary here for other types of default values ?
            
            // ensure that we deliver the property in the correct form
            if (DataTypeDefinition.BOOLEAN.equals(prop.getDataType().getName()) == true)
            {
                if (value instanceof String)
                {
                    if (((String)value).toUpperCase().equals("TRUE") == true)
                    {
                        value = Boolean.TRUE;
                    }
                    else if (((String)value).toUpperCase().equals("FALSE") == true)
                    {
                        value = Boolean.FALSE;
                    }
                }
            }
            
            // Set the default value of the property
            properties.put(entry.getKey(), value);
        }
        return properties;
    }

    @Override
    public List<NodeRef> findNodes(FindNodeParameters params)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean removeSeconaryChildAssociation(ChildAssociationRef childAssocRef)
    {
        return removeSecondaryChildAssociation(childAssocRef);
    }
}
