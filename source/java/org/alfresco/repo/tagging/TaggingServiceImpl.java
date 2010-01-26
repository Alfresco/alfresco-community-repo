/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
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

/**
 * Tagging service implementation
 * 
 * @author Roy Wetherall
 */
public class TaggingServiceImpl implements TaggingService, 
                                           TransactionListener,
                                           NodeServicePolicies.BeforeDeleteNodePolicy
{
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
    
    /** Tag Details Delimiter */
    private static final String TAG_DETAILS_DELIMITER = "|";
    
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
     * Init method
     */
    public void init()
    {
        // Register policy behaviours
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                ContentModel.ASPECT_TAGGABLE, 
                new JavaBehaviour(this, "beforeDeleteNode", NotificationFrequency.FIRST_EVENT));
    }
    
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void beforeDeleteNode(NodeRef nodeRef)
    {
       if (this.nodeService.exists(nodeRef) == true &&          
           this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TAGGABLE) == true &&
           this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) == false)
       {
           ChildAssociationRef assocRef = this.nodeService.getPrimaryParent(nodeRef);
           if (assocRef != null)
           {
               NodeRef parent = assocRef.getParentRef();
               if (parent != null)
               {
                   List<String> tags = getTags(nodeRef);
                   Map<String, Boolean> tagUpdates = new HashMap<String, Boolean>(tags.size());
                   for (String tag : tags)
                   {
                       tagUpdates.put(tag, Boolean.FALSE);
                   }
                   updateTagScope(parent, tagUpdates, false);
               }
           }
       }
    }
    
    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#isTag(java.lang.String)
     */
    public boolean isTag(StoreRef storeRef, String tag)
    {
        // Lower the case of the tag
        tag = tag.toLowerCase();
        
        return (getTagNodeRef(storeRef, tag) != null);
    }

    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#createTag(java.lang.String)
     */
    public NodeRef createTag(StoreRef storeRef, String tag)
    {
        // Lower the case of the tag
        tag = tag.toLowerCase();
        
        if (isTag(storeRef, tag) == false)
        {
            return this.categoryService.createRootCategory(storeRef, ContentModel.ASPECT_TAGGABLE, tag);
        }
        return null;
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
     * @see org.alfresco.service.cmr.tagging.TaggingService#addTag(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public void addTag(final NodeRef nodeRef, final String tagName)
    {        
        // Lower the case of the tag
        String tag = tagName.toLowerCase();
        
        // Get the tag node reference
        NodeRef newTagNodeRef = getTagNodeRef(nodeRef.getStoreRef(), tag);
        if (newTagNodeRef == null)
        {
            // Create the new tag
            newTagNodeRef = categoryService.createRootCategory(nodeRef.getStoreRef(), ContentModel.ASPECT_TAGGABLE, tag);
        }        
        
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
        NodeRef tagNodeRef = null;
        String query = "+PATH:\"cm:taggable/cm:" + ISO9075.encode(tag) + "\"";
        ResultSet resultSet = this.searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, query);
        if (resultSet.length() != 0)
        {
            tagNodeRef = resultSet.getNodeRef(0);
        }
        resultSet.close();
        
        return tagNodeRef;
    }

    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#removeTag(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public void removeTag(NodeRef nodeRef, String tag)
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
            NodeRef newTagNodeRef = getTagNodeRef(nodeRef.getStoreRef(), tag);
            if (newTagNodeRef == null)
            {
                // Create the new tag
                newTagNodeRef = this.categoryService.createRootCategory(nodeRef.getStoreRef(), ContentModel.ASPECT_TAGGABLE, tag);
            } 
            
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
            getTagScopes(nodeRef, tagScopeNodeRefs);
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
     * Traverses up the node's primary parent placing all tag scope's in a list.
     * <p>
     * If none are found then the list is empty.
     * 
     * @param nodeRef      node reference
     * @param tagScopes    list of tag scopes
     */
    private void getTagScopes(NodeRef nodeRef, List<NodeRef> tagScopes)
    {
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TAGSCOPE) == true)
        {
            tagScopes.add(nodeRef);
        }
        
        ChildAssociationRef assoc = this.nodeService.getPrimaryParent(nodeRef);
        if (assoc != null)
        {
            NodeRef parent = assoc.getParentRef();
            if (parent != null)
            {
                getTagScopes(parent, tagScopes);
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
        
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
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
     * Update the relevant tag scopes when a tag is added or removed from a node.
     * 
     * @param nodeRef       node reference
     * @param updates
     * @param async         indicates whether the action is execute asynchronously
     */
    private void updateTagScope(NodeRef nodeRef, Map<String, Boolean> updates, boolean async)
    {
        // The map must be serializable
        if (updates instanceof HashMap)
        {        
            Action action = this.actionService.createAction(UpdateTagScopesActionExecuter.NAME);
            action.setParameterValue(UpdateTagScopesActionExecuter.PARAM_TAG_UPDATES, (HashMap<String, Boolean>)updates);
            this.actionService.executeAction(action, nodeRef, false, async);
        }
    }
    
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
                    updateTagScope(nodeRef, tagUpdates, true);
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
    @SuppressWarnings("deprecation")
    public void flush()
    {
    }   
}
