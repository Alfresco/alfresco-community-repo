/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
     * @param blogDetails BlogDetails
     * @param postId String
     * @param title String
     * @param body String
     * @param publish boolean
     * @return boolean
     */
    boolean updatePost(BlogDetails blogDetails, String postId, String title, String body, boolean publish);
    
    /**
     * Get the details of an existing blog post
     * 
     * @param blogDetails BlogDetails
     * @param postId String
     * @return Map
     */
    Map<String, Object> getPost(BlogDetails blogDetails, String postId);
    
    /**
     * Delete an existing blog post
     * 
     * @param blogDetails BlogDetails
     * @param postId String
     * @return boolean
     */
    boolean deletePost(BlogDetails blogDetails, String postId);
}
