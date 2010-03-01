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
package org.alfresco.repo.blog;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Blog integration service.
 * 
 * @author Roy Wetherall
 *
 */
public interface BlogIntegrationService
{
    /**
     * Register a new blog integration implementation with the service
     * 
     * @param implementation    the implementation
     */
    void register(BlogIntegrationImplementation implementation);
    
    /**
     * Get the named blog integration implementation, null if name not recognised
     * 
     * @param implementationName                the implementation name
     * @return BlogIntegrationImplementation    the blog integration implementation
     */
    BlogIntegrationImplementation getBlogIntegrationImplementation(String implementationName);
    
    /**
     * Get a list of the registered integration implementations.
     * 
     * @return List<BlogIntegrationImplementaion>   list of registered blog integration implementations
     */
    List<BlogIntegrationImplementation> getBlogIntegrationImplementations();
    
    /**
     * Given a node reference, gets a list of 'in scope' BlogDetails. 
     * 
     * The node itself and then the primary parent hierarchy is searched and any blog details found returned in 
     * a list, with the 'nearest' first.
     * 
     * @param nodeRef               the node reference
     * @return List<BlogDetails>    list of the blog details found 'in scope' for the node, empty if none found
     */
    List<BlogDetails> getBlogDetails(NodeRef nodeRef);
    
    /**
     * Posts the content of a node to the blog specified
     * 
     * @param blogDetails
     * @param nodeRef
     * @param contentProperty
     * @param publish
     */
    void newPost(BlogDetails blogDetails, NodeRef nodeRef, QName contentProperty, boolean publish);
    
    /**
     * 
     * @param postId
     * @param nodeRef
     * @param contentProperty
     * @param publish
     */
    void updatePost(NodeRef nodeRef, QName contentProperty, boolean publish);
    
    /**
     * 
     * @param postId
     * @param nodeRef
     */
    void deletePost(NodeRef nodeRef);
}
