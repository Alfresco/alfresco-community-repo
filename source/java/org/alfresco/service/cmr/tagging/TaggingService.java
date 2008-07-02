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
package org.alfresco.service.cmr.tagging;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * @author Roy Wetherall
 */
public interface TaggingService
{
    /**
     * Indicates whether the tag already exists
     * 
     * @param tag
     * @return
     */
    boolean isTag(StoreRef storeRef, String tag);
    
    /**
     * Get all the tags currently available
     * 
     * @return
     */
    List<String> getTags(StoreRef storeRef);
    
    /**
     * Create a new tag
     * 
     * @param tag
     */
    void createTag(StoreRef storeRef, String tag);
    
    /**
     * Add a tag to a node.  Creating the tag if it does not already exist.
     * 
     * @param nodeRef
     * @param tag
     */
    void addTag(NodeRef nodeRef, String tag);
    
    /**
     * Remove a tag from a node.
     * 
     * @param nodeRef
     * @param tag
     */
    void removeTag(NodeRef nodeRef, String tag);
    
    /**
     * Get all the tags on a node
     * 
     * @param nodeRef
     * @return
     */
    List<String> getTags(NodeRef nodeRef);
    
    /**    
     * Adds a tag scope to the specified node
     * 
     * @param nodeRef   node reference
     */
    void addTagScope(NodeRef nodeRef);
    
    /**
     * 
     * @param nodeRef
     */
    void removeTagScope(NodeRef nodeRef);
    
    /**
     * Finds the 'nearest' tag scope for the specified node.
     * <p>
     * The 'nearest' tag scope is discovered by walking up the primary parent path
     * untill a tag scope is found or the root node is reached.
     * <p>
     * If no tag scope if found then a null value is returned.
     * 
     * @param nodeRef       node reference
     * @return              the 'nearest' tag scope or null if none found
     */
    TagScope findTagScope(NodeRef nodeRef);
    
    /**
     * 
     * @param nodeRef
     * @return
     */
    List<TagScope> findAllTagScopes(NodeRef nodeRef);
    
    /**
     * Find all nodes that have been tagged with the specified tag.
     * 
     * @param  tag              tag name
     * @return List<NodeRef>    list of nodes tagged with specified tag, empty of none found
     */
    List<NodeRef> findTaggedNodes(String tag);
    
    /**
     * Find all nodes that have been tagged with the specified tag and reside within
     * the tag scope.
     * 
     * @param tag
     * @param tagScope
     * @return
     */
    List<NodeRef> findTaggedNodes(String tag, TagScope tagScope);
}


