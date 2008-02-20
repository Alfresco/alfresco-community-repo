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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.blogIntegration;

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
