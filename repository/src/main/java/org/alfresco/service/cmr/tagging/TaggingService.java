/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.service.cmr.tagging;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.Auditable;
import org.alfresco.service.Experimental;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;

/**
 * Tagging Service Interface
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface TaggingService
{
    /**
     * Indicates whether the tag already exists
     * 
     * @param storeRef      store reference
     * @param tag           tag name
     * @return boolean      true if the tag exists, false otherwise
     */
    @NotAuditable
    boolean isTag(StoreRef storeRef, String tag);
    
    public String getTagName(NodeRef nodeRef);

    /**
     * Get all the tags currently available
     * 
     * @return list of tags
     */
    @NotAuditable
    List<String> getTags(StoreRef storeRef);
    
    /**
     * Get a paged list of all the tags currently available 
     * 
     * @param storeRef StoreRef
     * @param pagingRequest PagingRequest
     * @return PagingResults
     */
    @NotAuditable
    PagingResults<Pair<NodeRef, String>> getTags(StoreRef storeRef, PagingRequest pagingRequest);

    /**
     * Get a paged list of tags filtered by name
     *
     * @param storeRef StoreRef
     * @param pagingRequest PagingRequest
     * @param exactNamesFilter PagingRequest
     * @param alikeNamesFilter PagingRequest
     * @return PagingResults
     */
    @NotAuditable
    default PagingResults<Pair<NodeRef, String>> getTags(StoreRef storeRef, PagingRequest pagingRequest, Collection<String> exactNamesFilter, Collection<String> alikeNamesFilter)
    {
        return new EmptyPagingResults<>();
    }
    
    /** 
     * Get all the tags currently available that match the provided filter.
     * 
     * @param storeRef      store reference
     * @param filter        tag filter
     * @return list of tags
     */
    @NotAuditable
    List<String> getTags(StoreRef storeRef, String filter);
    
    /**
     * Create a new tag
     * 
     * @param storeRef  store reference
     * @param tag       tag name
     */
    @Auditable(parameters = {"tag"})
    NodeRef createTag(StoreRef storeRef, String tag);
    
    /**
     * Delete an existing tag and any references to it(cascade delete).
     * 
     * @param storeRef  store reference
     * @param tag       tag name
     */
    @Auditable(parameters = {"tag"})
    void deleteTag(StoreRef storeRef, String tag);
    
    @Auditable(parameters = {"existingTag", "newTag"})
    NodeRef changeTag(StoreRef storeRef, String existingTag, String newTag);
    
    /**
     * Indicates whether a node has the specified tag or not.
     * 
     * @param nodeRef   node reference
     * @param tag       tag name
     * @return boolean  true if the node has the tag, false otherwise
     */
    @Auditable(parameters = {"tag"})
    boolean hasTag(NodeRef nodeRef, String tag);
    
    /**
     * Add a tag to a node.  Creating the tag if it does not already exist.
     * 
     * @param nodeRef   node reference
     * @param tag       tag name
     */
    @Auditable(parameters = {"tag"})
    NodeRef addTag(NodeRef nodeRef, String tag);

    /**
     * Gets the node reference for a given tag.
     * <p>
     * Returns null if tag is not present.
     * 
     * @param storeRef      store reference
     * @param tag           tag
     * @return NodeRef      tag node reference or null not exist
     */
    @NotAuditable
    NodeRef getTagNodeRef(StoreRef storeRef, String tag);

    /**
     * Adds a list of tags to a node.
     * <p>
     * Tags are created if they do not exist.
     * 
     * @param nodeRef   node reference
     * @param tags      list of tags
     */
    @Auditable(parameters = {"tags"})
    List<Pair<String, NodeRef>> addTags(NodeRef nodeRef, List<String> tags);
    
    /**
     * Remove a tag from a node.
     * 
     * @param nodeRef   node reference
     * @param tag       tag name
     */
    @Auditable(parameters = {"tag"})
    void removeTag(NodeRef nodeRef, String tag);
    
    /**
     * Removes a list of tags from a node.
     * 
     * @param nodeRef   node reference
     * @param tags      list of tags
     */
    @Auditable(parameters = {"tags"})
    void removeTags(NodeRef nodeRef, List<String> tags);
    
    /**
     * Get all the tags on a node
     * 
     * @param nodeRef           node reference
     * @return list of tags on the node
     */
    @NotAuditable
    List<String> getTags(NodeRef nodeRef);
    
    /**
     * Get a paged list of all the tags on a node
     * 
     * @param nodeRef                    node reference
     * @return list of tags on the node
     */
    @NotAuditable
    PagingResults<Pair<NodeRef, String>> getTags(NodeRef nodeRef, PagingRequest pagingRequest);
    
    /**
     * Sets the list of tags that are applied to a node, replaces any existing
     * tags with those provided.
     * 
     * @param nodeRef   node reference
     * @param tags      list of tags
     */
    @Auditable(parameters = {"tags"})
    void setTags(NodeRef nodeRef, List<String> tags);
    
    /**
     * Clears all tags from an already tagged node.
     * 
     * @param nodeRef   node reference
     */
    @Auditable
    void clearTags(NodeRef nodeRef);
    
    /** 
     * Indicates whether the node reference is a tag scope
     * 
     * @param nodeRef   node reference
     * @return boolean  true if node is a tag scope, false otherwise
     */
    @NotAuditable
    boolean isTagScope(NodeRef nodeRef);
    
    /**    
     * Adds a tag scope to the specified node
     * 
     * @param nodeRef   node reference
     */
    @Auditable
    void addTagScope(NodeRef nodeRef);
    
    /**
     * Refreshes the tag count of the passed tag scope by recounting all the tags of the children
     * of the scope.
     *
     * @param nodeRef       tag scope node reference
     * @param async         indicates whether the tag scope refresh should happen asynchronously or not
     */
    @Auditable
    void refreshTagScope(NodeRef nodeRef, boolean async);
    
    /**
     * Removes a tag scope from a specified node.
     * 
     * Note that any tag count information will be lost when the scope if removed.
     * 
     * @param nodeRef   node reference
     */
    @Auditable
    void removeTagScope(NodeRef nodeRef);
    
    /**
     * Finds the 'nearest' tag scope for the specified node.
     * <p>
     * The 'nearest' tag scope is discovered by walking up the primary parent path
     * until a tag scope is found or the root node is reached.
     * <p>
     * If no tag scope if found then a null value is returned.
     * 
     * @param nodeRef       node reference
     * @return              the 'nearest' tag scope or null if none found
     */
    @NotAuditable
    TagScope findTagScope(NodeRef nodeRef);
    
    /**
     * Finds all the tag scopes for the specified node.
     * <p>
     * The resulting list of tag scopes is ordered with the 'nearest' at the bedining of the list.
     * <p>
     * If no tag scopes are found an empty list is returned.
     * 
     * @param nodeRef           node reference
     * @return list of tag scopes
     */
    @NotAuditable
    List<TagScope> findAllTagScopes(NodeRef nodeRef);
    
    /**
     * Find all nodes that have been tagged with the specified tag.
     * 
     * @param  tag              tag name
     * @return list of nodes tagged with specified tag, empty of none found
     */
    @NotAuditable
    List<NodeRef> findTaggedNodes(StoreRef storeRef, String tag);
    
    /**
     * Find all nodes that have been tagged with the specified tag and reside within
     * the context of the node reference provided.
     * 
     * @param tag               tag name
     * @param nodeRef           node providing context for the search
     * @return list of nodes tagged in the context specified, empty if none found
     */
    @NotAuditable
    List<NodeRef> findTaggedNodes(StoreRef storeRef, String tag, NodeRef nodeRef);

    /**
     * Get page of the tags currently available
     *  
     * @param  storeRef                       node reference
     * @param  fromTag                        offset
     * @param  pageSize                       page size
     * @return pair of tag names and total count
     */
    @NotAuditable
    Pair<List<String>, Integer> getPagedTags(StoreRef storeRef, int fromTag, int pageSize);
    
    /**
     * 
     * @param  storeRef                       node reference
     * @param  filter                         tag filter
     * @param  fromTag                        page offset
     * @param  pageSize                       page size
     * @return pair of tag names and total count
     */
    @NotAuditable
    Pair<List<String>, Integer> getPagedTags(StoreRef storeRef, String filter, int fromTag, int pageSize);
    
    
    /**
     * Get tagged nodes and count of nodes group by tag name
     * 
     * @param storeRef
     * @return
     */
    @NotAuditable
    List<Pair<String, Integer>> findTaggedNodesAndCountByTagName(StoreRef storeRef);

    /**
     * Creates orphan tags. Tag names case will be lowered.
     *
     * @param storeRef Reference to node store.
     * @param tagNames List of tag names.
     * @return {@link List} of {@link Pair}s of tag names and node references.
     * @throws org.alfresco.service.cmr.repository.DuplicateChildNodeNameException if tag already exists.
     */
    @Experimental
    @Auditable(parameters = {"tagNames"})
    default List<Pair<String, NodeRef>> createTags(StoreRef storeRef, List<String> tagNames)
    {
        return Collections.emptyList();
    }
}


