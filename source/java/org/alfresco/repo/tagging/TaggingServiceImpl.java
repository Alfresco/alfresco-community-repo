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
package org.alfresco.repo.tagging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.CopyServicePolicies.BeforeCopyPolicy;
import org.alfresco.repo.copy.CopyServicePolicies.OnCopyCompletePolicy;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.tagging.TagDetails;
import org.alfresco.service.cmr.tagging.TagScope;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Tagging service implementation
 * 
 * @author Roy Wetherall
 */
public class TaggingServiceImpl implements TaggingService, 
                                           TransactionListener,
                                           NodeServicePolicies.BeforeDeleteNodePolicy,
                                           NodeServicePolicies.OnMoveNodePolicy,
                                           CopyServicePolicies.OnCopyCompletePolicy,
                                           CopyServicePolicies.BeforeCopyPolicy
{
    protected static final String TAGGING_AUDIT_APPLICATION_NAME = "Alfresco Tagging Service";
    protected static final String TAGGING_AUDIT_ROOT_PATH = "/tagging";
    protected static final String TAGGING_AUDIT_KEY_NODEREF = "node";
    protected static final String TAGGING_AUDIT_KEY_TAGS = "tags";
    
    private static Log logger = LogFactory.getLog(TaggingServiceImpl.class);

    /** Node service */
    private NodeService nodeService;
    
    /** Categorty Service */
    private CategoryService categoryService;
    
    /** Search Service */
    private SearchService searchService;
    
    /** Action Service */
    private ActionService actionService;
    
    /** Content Service */
    private ContentService contentService;
    
    /** Namespace Service */
    private NamespaceService namespaceService;
    
    /** Policy componenet */
    private PolicyComponent policyComponent;
    
    /** Audit Service */
    private AuditService auditService;
    
    /** Audit Component, used with Audit Service */
    private AuditComponent auditComponent;
    
    /** Tag Details Delimiter */
    private static final String TAG_DETAILS_DELIMITER = "|";
    
    /** Policy behaviour */
    private JavaBehaviour updateTagBehaviour;
    private JavaBehaviour createTagBehaviour;
    
    /**
     * Set the cateogry service
     */
    public void setCategoryService(CategoryService categoryService)
    {
        this.categoryService = categoryService;
    }

    /**
     * Set the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * Set the action service
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    /**
     * Set the content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * Set the namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * Policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Set the audit service
     */
    public void setAuditService(AuditService auditService)
    {
        this.auditService = auditService;
    }
    
    /**
     * Set the audit component
     */
    public void setAuditComponent(AuditComponent auditComponent)
    {
        this.auditComponent = auditComponent;
    }
    
    /**
     * Init method
     */
    public void init()
    {
        // Register policy behaviours
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                ContentModel.ASPECT_TAGGABLE, 
                new JavaBehaviour(this, "beforeDeleteNode", NotificationFrequency.EVERY_EVENT));
        
        // Create tag behaviour
        createTagBehaviour = new JavaBehaviour(this, "createTags", NotificationFrequency.FIRST_EVENT);
        this.policyComponent.bindClassBehaviour(
                OnCreateNodePolicy.QNAME, 
                ContentModel.ASPECT_TAGGABLE, 
                createTagBehaviour);
        
        // We need to register on content and folders, rather than
        //  tagable, so we can pick up when things start and
        //  stop being tagged
        updateTagBehaviour = new JavaBehaviour(this, "updateTags", NotificationFrequency.EVERY_EVENT);
        this.policyComponent.bindClassBehaviour(
                OnUpdatePropertiesPolicy.QNAME,
                ContentModel.TYPE_CONTENT, 
                updateTagBehaviour);
        this.policyComponent.bindClassBehaviour(
                OnUpdatePropertiesPolicy.QNAME,
                ContentModel.TYPE_FOLDER, 
                updateTagBehaviour);
        
        // We need to know when you move or copy nodes
        this.policyComponent.bindClassBehaviour(
              OnMoveNodePolicy.QNAME,
              ContentModel.ASPECT_TAGGABLE, 
              new JavaBehaviour(this, "onMoveNode", NotificationFrequency.EVERY_EVENT));
        this.policyComponent.bindClassBehaviour(
              BeforeCopyPolicy.QNAME,
              ContentModel.ASPECT_TAGGABLE, 
              new JavaBehaviour(this, "beforeCopy", NotificationFrequency.EVERY_EVENT));
        this.policyComponent.bindClassBehaviour(
              OnCopyCompletePolicy.QNAME,
              ContentModel.ASPECT_TAGGABLE, 
              new JavaBehaviour(this, "onCopyComplete", NotificationFrequency.EVERY_EVENT));
    }
    
    /**
     * Called after a copy / delete / move, to trigger a 
     *  tag scope update of all the tags on the node.
     * Will update all parent tag scopes for the node, by either 
     *  adding or removing all tags from the node (based on the 
     *  isAdd parameter).
     */
    private void updateAllScopeTags(NodeRef nodeRef, Boolean isAdd)
    {
       ChildAssociationRef assocRef = this.nodeService.getPrimaryParent(nodeRef);
       if (assocRef != null)
       {
          updateAllScopeTags(nodeRef, assocRef.getParentRef(), isAdd);
       }
    }
    @SuppressWarnings("unchecked")
    private void updateAllScopeTags(NodeRef nodeRef, NodeRef parentNodeRef, Boolean isAdd)
    {
        if (parentNodeRef != null)
        {
            // Grab an currently pending changes for this node
            Map<NodeRef, Map<String, Boolean>> allQueuedUpdates = (Map<NodeRef, Map<String, Boolean>>)AlfrescoTransactionSupport.getResource(TAG_UPDATES);
            Map<String, Boolean> nodeQueuedUpdates = null;
            if (allQueuedUpdates != null)
            {
                nodeQueuedUpdates = allQueuedUpdates.get(nodeRef);
            }
            
            // Record the changes for the node, cancelling out existing
            //  changes if needed
            List<String> tags = getTags(nodeRef);
            Map<String, Boolean> tagUpdates = new HashMap<String, Boolean>(tags.size());
            for (String tag : tags)
            {
                tagUpdates.put(tag, isAdd);

                if (nodeQueuedUpdates != null)
                {
                    Boolean queuedOp = (Boolean)nodeQueuedUpdates.get(tag);
                    if ((queuedOp != null) && (queuedOp.booleanValue() == isAdd.booleanValue()))
                    {
                        // dequeue - will be handled synchronously
                        nodeQueuedUpdates.remove(tag);
                    }
                }
            }
            
            // Find the parent tag scopes and update them
            updateTagScope(parentNodeRef, tagUpdates);
        }
    }
    
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void beforeDeleteNode(NodeRef nodeRef)
    {
       if (this.nodeService.exists(nodeRef) == true &&          
           this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TAGGABLE) == true)
       {
           updateAllScopeTags(nodeRef, Boolean.FALSE);
       }
    }
    
    /**
     * Fired once per node, before a copy overrides one node (which is possibly newly created) with the contents
     * of another one.
     * We should remove any tags from the scope, as they'll shortly be overwritten.
     */
    public void beforeCopy(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef)
    {
        if (this.nodeService.hasAspect(targetNodeRef, ContentModel.ASPECT_TAGGABLE))
        {
            updateAllScopeTags(targetNodeRef, Boolean.FALSE);
        }
    }

    /**
     * Fired once per node that was copied, after the copy has completed. 
     * We need to add in all the tags to the scope.
     */
    public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean copyToNewNode,
            Map<NodeRef, NodeRef> copyMap)
    {
        if (this.nodeService.hasAspect(targetNodeRef, ContentModel.ASPECT_TAGGABLE))
        {
            updateAllScopeTags(targetNodeRef, Boolean.TRUE);
        }
    }

    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        NodeRef oldRef = oldChildAssocRef.getChildRef();
        NodeRef oldParent = oldChildAssocRef.getParentRef();
        NodeRef newRef = newChildAssocRef.getChildRef();
        NodeRef newParent = newChildAssocRef.getParentRef();
        
        // Do nothing if it's a "rename" not a move
        if (oldParent.equals(newParent))
        {
            return;
        }
        
        // It has moved somewhere
        // Remove the tags from the old location
        if (this.nodeService.hasAspect(oldRef, ContentModel.ASPECT_TAGGABLE))
        {
            // Use the parent we were passed in, rather than re-fetching
            // via the node, as we need to reference the old scope!
            ChildAssociationRef scopeParent;
            if (oldChildAssocRef.isPrimary())
            {
                scopeParent = oldChildAssocRef;
            }
            else
            {
                scopeParent = this.nodeService.getPrimaryParent(oldParent);
            }
            if (scopeParent != null)
            {
                updateAllScopeTags(oldRef, scopeParent.getParentRef(), Boolean.FALSE);
            }
        }
        // Add the tags at its new location
        if (this.nodeService.hasAspect(newRef, ContentModel.ASPECT_TAGGABLE))
        {
            updateAllScopeTags(newRef, Boolean.TRUE);
        }
    }

    public void createTags(ChildAssociationRef childAssocRef)
    {
        NodeRef nodeRef = childAssocRef.getChildRef();
        Map<QName, Serializable> before = new HashMap<QName, Serializable>(0);
        Map<QName, Serializable> after = nodeService.getProperties(nodeRef);
        
        updateTags(nodeRef, before, after);
    }
    
    /**
     * Update tag policy behaviour
     */
    @SuppressWarnings("unchecked")
    public void updateTags(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        List<NodeRef> beforeNodeRefs = (List<NodeRef>)before.get(ContentModel.PROP_TAGS);
        List<NodeRef> afterNodeRefs = (List<NodeRef>)after.get(ContentModel.PROP_TAGS);
        
        if (beforeNodeRefs == null && afterNodeRefs != null)
        {
            // Queue all the after's for addition to the tag scopes
            for (NodeRef afterNodeRef : afterNodeRefs)
            {
                String tagName = getTagName(afterNodeRef);
                queueTagUpdate(nodeRef, tagName, true);
            }
        }
        else if (afterNodeRefs == null && beforeNodeRefs != null)
        {
            // Queue all the before's for removal to the tag scope
            for (NodeRef beforeNodeRef : beforeNodeRefs)
            {
                String tagName = getTagName(beforeNodeRef);
                queueTagUpdate(nodeRef, tagName, false);
            }
        }
        else if (afterNodeRefs != null && beforeNodeRefs != null)
        {
            //Create a copy of the afterNodeRefs so we don't affect the properties we were given
            afterNodeRefs = new ArrayList<NodeRef>(afterNodeRefs);
            for (NodeRef beforeNodeRef : beforeNodeRefs)
            {
                if (afterNodeRefs.contains(beforeNodeRef) == true)
                {
                    // remove the node ref from the after list
                    afterNodeRefs.remove(beforeNodeRef);
                }
                else
                {
                    String tagName = getTagName(beforeNodeRef);
                    queueTagUpdate(nodeRef, tagName, false);
                }
            }
            for (NodeRef afterNodeRef : afterNodeRefs)
            {
                String tagName = getTagName(afterNodeRef);
                queueTagUpdate(nodeRef, tagName, true);
            }
        }
    }
    
    private String getTagName(NodeRef nodeRef)
    {
        return (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    }
    
    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#isTag(java.lang.String)
     */
    public boolean isTag(StoreRef storeRef, String tag)
    {
        // Lower the case of the tag
        return (getTagNodeRef(storeRef, tag.toLowerCase()) != null);
    }

    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#createTag(java.lang.String)
     */
    public NodeRef createTag(StoreRef storeRef, String tag)
    {
        // Lower the case of the tag
        tag = tag.toLowerCase();
		
		return getTagNodeRef(storeRef, tag, true);
    }  

    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#deleteTag(org.alfresco.service.cmr.repository.StoreRef, java.lang.String)
     */
    public void deleteTag(StoreRef storeRef, String tag)
    {
        // Lower the case of the tag
        tag = tag.toLowerCase();
        
        NodeRef tagNodeRef = getTagNodeRef(storeRef, tag);
        if (tagNodeRef != null)
        {
            this.categoryService.deleteCategory(tagNodeRef);
        }
    }
    
    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#getTags()
     */
    public List<String> getTags(StoreRef storeRef)
    {
        Collection<ChildAssociationRef> rootCategories = this.categoryService.getRootCategories(storeRef, ContentModel.ASPECT_TAGGABLE);
        List<String> result = new ArrayList<String>(rootCategories.size());
        for (ChildAssociationRef rootCategory : rootCategories)
        {
            String name = (String)this.nodeService.getProperty(rootCategory.getChildRef(), ContentModel.PROP_NAME);
            result.add(name);
        }
        return result;
    }
    
    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#getTags(org.alfresco.service.cmr.repository.StoreRef, java.lang.String)
     */
    public List<String> getTags(StoreRef storeRef, String filter)
    {
        Collection<ChildAssociationRef> rootCategories = this.categoryService.getRootCategories(storeRef, ContentModel.ASPECT_TAGGABLE);
        List<String> result = new ArrayList<String>(rootCategories.size());
        for (ChildAssociationRef rootCategory : rootCategories)
        {
            String name = (String)this.nodeService.getProperty(rootCategory.getChildRef(), ContentModel.PROP_NAME);
            if (name.contains(filter.toLowerCase()) == true)
            {
                result.add(name);
            }
        }
        return result;
    }
    
    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#hasTag(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public boolean hasTag(NodeRef nodeRef, String tag)
    {
        List<String> tags = getTags(nodeRef);
        return (tags.contains(tag.toLowerCase()));        
    }
    
    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#addTag(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public void addTag(final NodeRef nodeRef, final String tagName)
    {        
        updateTagBehaviour.disable();
        createTagBehaviour.disable();
        try
        {
            // Lower the case of the tag
            String tag = tagName.toLowerCase();
            
            // Get the tag node reference
            NodeRef newTagNodeRef = getTagNodeRef(nodeRef.getStoreRef(), tag, true);
            
            List<NodeRef> tagNodeRefs = new ArrayList<NodeRef>(5);
            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TAGGABLE) == false)
            {
                // Add the aspect
                nodeService.addAspect(nodeRef, ContentModel.ASPECT_TAGGABLE, null);
            }
            else
            {
                // Get the current tags
                List<NodeRef> currentTagNodes = (List<NodeRef>)nodeService.getProperty(nodeRef, ContentModel.PROP_TAGS);
                if (currentTagNodes != null)
                {
                    tagNodeRefs = currentTagNodes;
                }
            }
            
            // Add the new tag (assuming it's not already been added
            if (tagNodeRefs.contains(newTagNodeRef) == false)
            {
                tagNodeRefs.add(newTagNodeRef);
                nodeService.setProperty(nodeRef, ContentModel.PROP_TAGS, (Serializable)tagNodeRefs);
                queueTagUpdate(nodeRef, tag, true);
            }
        }
        finally
        {
            updateTagBehaviour.enable();
            createTagBehaviour.enable();
        }
    }
    
    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#addTags(org.alfresco.service.cmr.repository.NodeRef, java.util.List)
     */
    public void addTags(NodeRef nodeRef, List<String> tags)
    {
        for (String tag : tags)
        {
            addTag(nodeRef, tag);
        }
    }
    
    /**
     * Gets the node reference for a given tag.
     * <p>
     * Returns null if tag is not present.
     * 
     * @param storeRef      store reference
     * @param tag           tag
     * @return NodeRef      tag node reference or null not exist
     */
    public NodeRef getTagNodeRef(StoreRef storeRef, String tag)
    {
        return getTagNodeRef(storeRef, tag, false); 
    }

    /**
     * Gets the node reference for a given tag.
     * <p>
     * Returns null if tag is not present and not created.
     * 
     * @param storeRef      store reference
     * @param tag           tag
     * @param create        create a node if one doesn't exist?
     * @return NodeRef      tag node reference or null not exist
     */
    private NodeRef getTagNodeRef(StoreRef storeRef, String tag, boolean create)
    {
        NodeRef tagNodeRef = null;
        Collection<ChildAssociationRef> results = this.categoryService.getRootCategories(storeRef, ContentModel.ASPECT_TAGGABLE, tag, create);
        if (!results.isEmpty())
        {
            tagNodeRef = results.iterator().next().getChildRef();
        }
        return tagNodeRef;
    }

    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#removeTag(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public void removeTag(NodeRef nodeRef, String tag)
    {
        updateTagBehaviour.disable();
        createTagBehaviour.disable();
        try
        {
            // Lower the case of the tag
            tag = tag.toLowerCase();
            
            // Check for the taggable aspect
            if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TAGGABLE) == true)
            {        
                // Get the tag node reference
                NodeRef newTagNodeRef = getTagNodeRef(nodeRef.getStoreRef(), tag);
                if (newTagNodeRef != null)
                {
                    // Get the current tags
                    List<NodeRef> currentTagNodes = (List<NodeRef>)this.nodeService.getProperty(nodeRef, ContentModel.PROP_TAGS);
                    if (currentTagNodes != null &&
                        currentTagNodes.size() != 0 &&
                        currentTagNodes.contains(newTagNodeRef) == true)
                    {
                        currentTagNodes.remove(newTagNodeRef);
                        this.nodeService.setProperty(nodeRef, ContentModel.PROP_TAGS, (Serializable)currentTagNodes);
                        queueTagUpdate(nodeRef, tag, false);
                    }
                }
            }
        }
        finally
        {
            updateTagBehaviour.enable();
            createTagBehaviour.enable();
        }
    }  
    
    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#removeTags(org.alfresco.service.cmr.repository.NodeRef, java.util.List)
     */
    public void removeTags(NodeRef nodeRef, List<String> tags)
    {
        for (String tag : tags)
        {
            removeTag(nodeRef, tag);
        }
    }

    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#getTags(org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    public List<String> getTags(NodeRef nodeRef)
    {
        List<String> result = new ArrayList<String>(10);
        
        // Check for the taggable aspect
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TAGGABLE) == true)
        {
            // Get the current tags
            List<NodeRef> currentTagNodes = (List<NodeRef>)this.nodeService.getProperty(nodeRef, ContentModel.PROP_TAGS);
            if (currentTagNodes != null)
            {
                for (NodeRef currentTagNode : currentTagNodes)
                {
                    String tag = (String)this.nodeService.getProperty(currentTagNode, ContentModel.PROP_NAME);
                    result.add(tag);
                }
            }
        }
        
        return result;
    }
    
    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#setTags(org.alfresco.service.cmr.repository.NodeRef, java.util.List)
     */
    public void setTags(NodeRef nodeRef, List<String> tags)
    {  
        updateTagBehaviour.disable();
        createTagBehaviour.disable();
        try
        {
            List<NodeRef> tagNodeRefs = new ArrayList<NodeRef>(tags.size());
            if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TAGGABLE) == false)
            {
                // Add the aspect
                this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_TAGGABLE, null);
            }
            
            // Get the current list of tags
            List<String> oldTags = getTags(nodeRef);
            
            for (String tag : tags)
            {   
                // Lower the case of the tag
                tag = tag.toLowerCase();
                
                // Get the tag node reference
                NodeRef newTagNodeRef = getTagNodeRef(nodeRef.getStoreRef(), tag, true);
                
                if (tagNodeRefs.contains(newTagNodeRef) == false)
                {            
                    // Add to the list
                    tagNodeRefs.add(newTagNodeRef);
                    
                    // Trigger scope update
                    if (oldTags.contains(tag) == false)
                    {
                        queueTagUpdate(nodeRef, tag, true);
                    }
                    else
                    {
                        // Remove the tag from the old list
                        oldTags.remove(tag);
                    }
                }
            }
            
            // Remove the old tags from the tag scope
            for (String oldTag : oldTags)
            {
                queueTagUpdate(nodeRef, oldTag, false);
            }
            
            // Update category property
            this.nodeService.setProperty(nodeRef, ContentModel.PROP_TAGS, (Serializable)tagNodeRefs);
        }
        finally
        {
            updateTagBehaviour.enable();
            createTagBehaviour.enable();
        }
    }
    
    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#clearTags(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void clearTags(NodeRef nodeRef)
    {
        setTags(nodeRef, Collections.<String>emptyList());
    }
    
    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#isTagScope(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isTagScope(NodeRef nodeRef)
    {
        // Determines whether the node has the tag scope aspect
        return this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TAGSCOPE);
    }
     
    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#addTagScope(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void addTagScope(NodeRef nodeRef)
    {
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TAGSCOPE) == false)
        {
            // Add the tag scope aspect
            this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_TAGSCOPE, null);
            
            // Refresh the tag scope
            refreshTagScope(nodeRef, false);
        }        
    }
    
    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#refreshTagScopt(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public void refreshTagScope(NodeRef nodeRef, boolean async)
    {
        if (this.nodeService.exists(nodeRef) == true && 
            this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TAGSCOPE) == true)
        {            
            Action action = this.actionService.createAction(RefreshTagScopeActionExecuter.NAME);
            this.actionService.executeAction(action, nodeRef, false, async);
        }
    }

    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#removeTagScope(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void removeTagScope(NodeRef nodeRef)
    {
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TAGSCOPE) == true)
        {
            this.nodeService.removeAspect(nodeRef, ContentModel.ASPECT_TAGSCOPE);
        }
    }

    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#findTagScope(org.alfresco.service.cmr.repository.NodeRef)
     */
    public TagScope findTagScope(NodeRef nodeRef)
    {
        TagScope tagScope = null;
        
        if (this.nodeService.exists(nodeRef) == true)
        {
            List<NodeRef> tagScopeNodeRefs = new ArrayList<NodeRef>(3);
            getTagScopes(nodeRef, tagScopeNodeRefs, true);
            if (tagScopeNodeRefs.size() != 0)
            {                
                tagScope = new TagScopeImpl(tagScopeNodeRefs.get(0), getTagDetails(tagScopeNodeRefs.get(0)));
            }
        }
        
        return tagScope;
    }
    
    /**
     * Gets the tag details list for a given tag scope node reference
     * 
     * @param nodeRef               tag scope node reference
     * @return List<TagDetails>     ordered list of tag details for the tag scope
     */
    private List<TagDetails> getTagDetails(NodeRef nodeRef)
    {
        List<TagDetails> tagDetails = new ArrayList<TagDetails>(13);
        ContentReader reader = this.contentService.getReader(nodeRef, ContentModel.PROP_TAGSCOPE_CACHE);
        if (reader != null)
        {
            tagDetails = TaggingServiceImpl.readTagDetails(reader.getContentInputStream());
        }
        return tagDetails;
    }

    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#findAllTagScopes(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<TagScope> findAllTagScopes(NodeRef nodeRef)
    {
        List<TagScope> result = null;
        
        if (this.nodeService.exists(nodeRef) == true)
        {
            List<NodeRef> tagScopeNodeRefs = new ArrayList<NodeRef>(3);
            getTagScopes(nodeRef, tagScopeNodeRefs);
            if (tagScopeNodeRefs.size() != 0)
            {
                result = new ArrayList<TagScope>(tagScopeNodeRefs.size());
                for (NodeRef tagScopeNodeRef : tagScopeNodeRefs)
                {
                    result.add(new TagScopeImpl(tagScopeNodeRef, getTagDetails(tagScopeNodeRef)));
                }
            }
            else
            {
                result = Collections.emptyList();
            }
        }
        
        return result;
    }

    /**
     * Traverses up the node's primary parent placing ALL found tag scope's in a list.
     * <p>
     * 
     * @param nodeRef      node reference
     * @param tagScopes    list of tag scopes
     */
    private void getTagScopes(NodeRef nodeRef, List<NodeRef> tagScopes)
    {
        getTagScopes(nodeRef, tagScopes, false);
    }
    
    /**
     * Traverses up the node's primary parent placing found tag scope's in a list.
     * <p>
     * If none are found then the list is empty.
     * 
     * @param nodeRef      node reference
     * @param tagScopes    list of tag scopes
     * @param firstOnly    true => only return first tag scope that is found
     */
    private void getTagScopes(NodeRef nodeRef, List<NodeRef> tagScopes, boolean firstOnly)
    {
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TAGSCOPE) == true)
        {
            tagScopes.add(nodeRef);
            if (firstOnly)
                return;
        }
        
        ChildAssociationRef assoc = this.nodeService.getPrimaryParent(nodeRef);
        if (assoc != null)
        {
            NodeRef parent = assoc.getParentRef();
            if (parent != null)
            {
                getTagScopes(parent, tagScopes, firstOnly);
            }
        }
    }

    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#findTaggedNodes(java.lang.String)
     */
    public List<NodeRef> findTaggedNodes(StoreRef storeRef, String tag)
    {
        // Lower the case of the tag
        tag = tag.toLowerCase();
        
        // Do the search for nodes
        ResultSet resultSet = this.searchService.query(
                storeRef, 
                SearchService.LANGUAGE_LUCENE, 
                "+PATH:\"/cm:taggable/cm:" + ISO9075.encode(tag) + "/member\"");
        List<NodeRef> nodeRefs = resultSet.getNodeRefs();
        resultSet.close();
        return nodeRefs;
    }

    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#findTaggedNodes(java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<NodeRef> findTaggedNodes(StoreRef storeRef, String tag, NodeRef nodeRef)
    {
        // Lower the case of the tag
        tag = tag.toLowerCase();
        
        // Get path
        Path nodePath = this.nodeService.getPath(nodeRef);
        String pathString = nodePath.toPrefixString(this.namespaceService);
        
        // Do query
        ResultSet resultSet = this.searchService.query(
                storeRef, 
                SearchService.LANGUAGE_LUCENE, 
                "+PATH:\"" + pathString + "//*\" +PATH:\"/cm:taggable/cm:" + ISO9075.encode(tag) + "/member\"");
        List<NodeRef> nodeRefs = resultSet.getNodeRefs();
        resultSet.close();
        return nodeRefs;
    }
    
    /**
     * Helper method that takes an input stream and converts it into a list of tag details
     * 
     * @param is                    input stream
     * @return List<TagDetails>     list of tag details
     */
    /*package*/ static List<TagDetails> readTagDetails(InputStream is)
    {
        List<TagDetails> result = new ArrayList<TagDetails>(25);
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String nextLine = reader.readLine();
            while (nextLine != null)
            {
                String[] values = nextLine.split("\\" + TAG_DETAILS_DELIMITER);
                result.add(new TagDetailsImpl(values[0], Integer.parseInt(values[1])));
                
                nextLine = reader.readLine();
            }
        }
        catch (IOException exception)
        {
            throw new AlfrescoRuntimeException("Unable to read tag details", exception);
        }
        finally
        {
            try { reader.close(); } catch (Exception e) {}
        }
        
        return result;        
    }
    
    /**
     * Helper method to convert a list of tag details into a string.
     * 
     * @param tagDetails    list of tag details
     * @return String       string of tag details
     */
    /*package*/ static String tagDetailsToString(List<TagDetails> tagDetails)
    {
        StringBuffer result = new StringBuffer(255);
        
        boolean bFirst = true;
        for (TagDetails details : tagDetails)
        {
            if (bFirst == false)
            {
                result.append("\n");
            }
            else
            {
                bFirst = false;
            }
            
            result.append(details.getName());
            result.append(TAG_DETAILS_DELIMITER);
            result.append(details.getCount());
        }
            
        return result.toString();
    }

    // ===== Methods Dealing with TagScope Updates ==== //
    
    public static final String TAG_UPDATES = "tagUpdates"; 
    
    /**
     * Triggers an async update of all the relevant tag scopes when a tag is 
     *  added or removed from a node.
     * Uses the audit service as a persisted queue to hold the list of changes,
     *  and triggers an sync action to work on the entries in the queue for us.
     *  This should avoid contention problems and race conditions.
     * 
     * @param nodeRef       node reference
     * @param updates
     */
    private void updateTagScope(NodeRef nodeRef, Map<String, Boolean> updates)
    {
       // Warn if auditing is disabled - we need it!
       if(! auditService.isAuditEnabled(TAGGING_AUDIT_APPLICATION_NAME, TAGGING_AUDIT_ROOT_PATH))
       {
          logger.warn("Tag updates won't propogate to the TagScope caches as auditing is disabled");
          return;
       }
       
       // First up, locate all the tag scopes for this node
       // (Need to do a recursive search up to the root)
       ArrayList<NodeRef> tagScopeNodeRefs = new ArrayList<NodeRef>(3);
       getTagScopes(nodeRef, tagScopeNodeRefs);
       
       if(tagScopeNodeRefs.size() == 0)
       {
          if(logger.isDebugEnabled())
          {
             logger.debug("No tag scopes found for " + nodeRef + " so no scope updates needed");
          }
          return;
       }
       
       // Turn from tag+yes/no into tag+1/-1
       // (Later we may roll things up better to be tag+#/-#)
       HashMap<String,Integer> changes = new HashMap<String, Integer>(updates.size());
       for(String tag : updates.keySet())
       {
          int val = -1;
          if(updates.get(tag))
             val = 1;
          changes.put(tag, val);
       }
       
       // Next, queue the updates for each tag scope
       for(NodeRef tagScopeNode : tagScopeNodeRefs)
       {
          Map<String,Serializable> auditValues = new HashMap<String, Serializable>();
          auditValues.put(TAGGING_AUDIT_KEY_TAGS, changes);
          auditValues.put(TAGGING_AUDIT_KEY_NODEREF, tagScopeNode.toString());
          auditComponent.recordAuditValues(TAGGING_AUDIT_ROOT_PATH, auditValues);
       }
       if(logger.isDebugEnabled())
       {
          logger.debug("Queueing async tag scope updates to tag scopes " + tagScopeNodeRefs + " of " + changes);
       }
       
       // Finally, trigger the action to process the updates
       // This will happen asynchronously
       Action action = this.actionService.createAction(UpdateTagScopesActionExecuter.NAME);
       action.setParameterValue(UpdateTagScopesActionExecuter.PARAM_TAG_SCOPES, tagScopeNodeRefs); 
       this.actionService.executeAction(action, null, false, true);
    }
    
    /**
     * Records the fact that the given tag for the given node will need to
     *  be added or removed from its parent tags scopes.
     * {@link #updateTagScope(NodeRef, Map, boolean)} will schedule the update
     *  to occur, and an async action will do it. 
     */
    @SuppressWarnings("unchecked")
    private void queueTagUpdate(NodeRef nodeRef, String tag, boolean add)
    {
        // Get the updates map        
        Map<NodeRef, Map<String, Boolean>> updates = (Map<NodeRef, Map<String, Boolean>>)AlfrescoTransactionSupport.getResource(TAG_UPDATES);
        if (updates == null)
        {
            updates = new HashMap<NodeRef, Map<String,Boolean>>(10);
            AlfrescoTransactionSupport.bindResource(TAG_UPDATES, updates);
            AlfrescoTransactionSupport.bindListener(this);
        }
        
        // Add the details of the update to the map
        Map<String, Boolean> nodeDetails = updates.get(nodeRef);
        if (nodeDetails == null)
        {
            nodeDetails = new HashMap<String, Boolean>(10);
            nodeDetails.put(tag, Boolean.valueOf(add));
            updates.put(nodeRef, nodeDetails);
        }
        else
        {
            Boolean currentValue = nodeDetails.get(tag);
            if (currentValue == null)
            {
                nodeDetails.put(tag, Boolean.valueOf(add));
                updates.put(nodeRef, nodeDetails);
            }
            else if (currentValue.booleanValue() != add)
            {
                // If the boolean value is different then the tag had been added and removed or
                // removed and then added in the same transaction.  In both cases the net change is none.
                // So remove the entry in the update map
                updates.remove(tag);
            }
            // Otherwise do nothing because we have already noted the update
        }
        
    }
    
    // ===== Transaction Listener Callback Methods ===== //
    
    /**
     * @see org.alfresco.repo.transaction.TransactionListener#afterCommit()
     */
    public void afterCommit()
    {
        
    }

    /**
     * @see org.alfresco.repo.transaction.TransactionListener#afterRollback()
     */
    public void afterRollback()
    {
    }

    /**
     * @see org.alfresco.repo.transaction.TransactionListener#beforeCommit(boolean)
     */
    @SuppressWarnings("unchecked")
    public void beforeCommit(boolean readOnly)
    {
        Map<NodeRef, Map<String, Boolean>> updates = (Map<NodeRef, Map<String, Boolean>>)AlfrescoTransactionSupport.getResource(TAG_UPDATES);
        if (updates != null)
        {
            for (NodeRef nodeRef : updates.keySet())
            {
                Map<String, Boolean> tagUpdates = updates.get(nodeRef);
                if (tagUpdates != null && tagUpdates.size() != 0)
                {
                    updateTagScope(nodeRef, tagUpdates);
                }
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
     * @see org.alfresco.repo.transaction.TransactionListener#flush()
     */
    public void flush()
    {
    }   
}
