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

import java.util.Map;

/**
 * Blog integration implementation interface
 * 
 * @author Roy Wetherall
 */
public interface BlogIntegrationImplementation
{
    /**
     * Gets the name of the blog integration
     * 
     * @return String   the name of the blog integration
     */
    String getName();
    
    /**
     * Gets the display name of the blog integration
     * 
     * @return String the display name of the blog integration
     */
    String getDisplayName();
    
    /**
     * Create a new post on the blog.
     * 
     * @param blogDetails   the blog details
     * @param title         the title of the post
     * @param body          the body of the post
     * @param publish       indicates whether the post is published or not
     * @return String       the newly created post id
     */
    String newPost(BlogDetails blogDetails, String title, String body, boolean publish);
    
    /**
     * Update an exisiting blog post
     * 
     * @param blogDetails
     * @param postId
     * @param title
     * @param body
     * @param publish
     * @return
     */
    boolean updatePost(BlogDetails blogDetails, String postId, String title, String body, boolean publish);
    
    /**
     * Get the details of an existing blog post
     * 
     * @param blogDetails
     * @param postId
     * @return
     */
    Map<String, Object> getPost(BlogDetails blogDetails, String postId);
    
    /**
     * Delete an existing blog post
     * 
     * @param blogDetails
     * @param postId
     * @return
     */
    boolean deletePost(BlogDetails blogDetails, String postId);
}
