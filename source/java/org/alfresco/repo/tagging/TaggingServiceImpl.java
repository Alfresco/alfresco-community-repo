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
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.tagging.TagDetails;
import org.alfresco.service.cmr.tagging.TagScope;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.util.ISO9075;

/**
 * Tagging service implementation
 * 
 * @author Roy Wetherall
 */
public class TaggingServiceImpl implements TaggingService
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
    
    /** Tag Details Delimiter */
    private static final String TAG_DETAILS_DELIMITER = "|";
    
    /**
     * Set the cateogry service
     * 
     * @param categoryService       trhe category service
     */
    public void setCategoryService(CategoryService categoryService)
    {
        this.categoryService = categoryService;
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
     * Set the search service
     * 
     * @param searchService     the search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * Set the action service
     * 
     * @return  ActionService   action service
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    /**
     * Set the content service
     * 
     * @param contentService    content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
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
    public void createTag(StoreRef storeRef, String tag)
    {
        // Lower the case of the tag
        tag = tag.toLowerCase();
        
        if (isTag(storeRef, tag) == false)
        {
            this.categoryService.createRootCategory(storeRef, ContentModel.ASPECT_TAGGABLE, tag);
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
     * @see org.alfresco.service.cmr.tagging.TaggingService#addTag(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public void addTag(NodeRef nodeRef, String tag)
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
        
        List<NodeRef> tagNodeRefs = new ArrayList(5);
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TAGGABLE) == false)
        {
            // Add the aspect
            this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_TAGGABLE, null);
        }
        else
        {
            // Get the current tags
            List<NodeRef> currentTagNodes = (List<NodeRef>)this.nodeService.getProperty(nodeRef, ContentModel.PROP_TAGS);
            if (currentTagNodes != null)
            {
                tagNodeRefs = currentTagNodes;
            }
        }
        
        // Add the new tag (assuming it's not already been added
        if (tagNodeRefs.contains(newTagNodeRef) == false)
        {
            tagNodeRefs.add(newTagNodeRef);
            this.nodeService.setProperty(nodeRef, ContentModel.PROP_TAGS, (Serializable)tagNodeRefs);
            updateTagScope(nodeRef, tag, true);
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
    private NodeRef getTagNodeRef(StoreRef storeRef, String tag)
    {
        NodeRef tagNodeRef = null;
        String query = "+PATH:\"cm:taggable/cm:" + ISO9075.encode(tag) + "\"";
        ResultSet resultSet = this.searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, query);
        if (resultSet.length() != 0)
        {
            tagNodeRef = resultSet.getNodeRef(0);
        }
        
        return tagNodeRef;
    }
    
    /**
     * Update the relevant tag scopes when a tag is added or removed from a node.
     * 
     * @param nodeRef       node reference
     * @param tag           tag
     * @param add           if true then the tag is added, false if the tag is removed
     */
    private void updateTagScope(NodeRef nodeRef, String tag, boolean add)
    {
        Action action = this.actionService.createAction(UpdateTagScopesActionExecuter.NAME);
        action.setParameterValue(UpdateTagScopesActionExecuter.PARAM_TAG_NAME, tag);
        action.setParameterValue(UpdateTagScopesActionExecuter.PARAM_ADD_TAG, add);
        this.actionService.executeAction(action, nodeRef, false, true);
    }

    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#removeTag(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
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
                    updateTagScope(nodeRef, tag, false);
                }
            }
        }
    }    

    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#getTags(org.alfresco.service.cmr.repository.NodeRef)
     */
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
     * @see org.alfresco.service.cmr.tagging.TaggingService#addTagScope(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void addTagScope(NodeRef nodeRef)
    {
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TAGSCOPE) == false)
        {
            this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_TAGSCOPE, null);
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
    public List<NodeRef> findTaggedNodes(String tag)
    {
        // Lower the case of the tag
        tag = tag.toLowerCase();
        
        // TODO 
        return null;
    }

    /**
     * @see org.alfresco.service.cmr.tagging.TaggingService#findTaggedNodes(java.lang.String, org.alfresco.service.cmr.tagging.TagScope)
     */
    public List<NodeRef> findTaggedNodes(String tag, TagScope tagScope)
    {
        // Lower the case of the tag
        tag = tag.toLowerCase();
        
        // TODO 
        return null;
    }
    
    /**
     * 
     * @param is
     * @return
     */
    /*package*/ static List<TagDetails> readTagDetails(InputStream is)
    {
        List<TagDetails> result = new ArrayList<TagDetails>(25);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try
        {
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
     * 
     * @param tagDetails
     * @param os
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
            
            result.append(details.getTagName());
            result.append(TAG_DETAILS_DELIMITER);
            result.append(details.getTagCount());
        }
            
        return result.toString();
    }
}
