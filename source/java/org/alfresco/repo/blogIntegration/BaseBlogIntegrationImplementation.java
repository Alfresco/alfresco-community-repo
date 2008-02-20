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

/**
 * Base blog implementation class.  Extend this when writting a blog integration implementation.
 * 
 * @author Roy Wetherall
 */
public abstract class BaseBlogIntegrationImplementation implements BlogIntegrationImplementation
{
    /** Blog integration service */
    private BlogIntegrationService blogIntegrationService;
    
    /** Integration name */
    private String name;
    
    /** Display name */
    private String displayName;
    
    /**
     * Sets the blog integration service
     * 
     * @param blogIntegrationService    the blog integration service
     */
    public void setBlogIntegrationService(BlogIntegrationService blogIntegrationService)
    {
        this.blogIntegrationService = blogIntegrationService;
    }
    
    /**
     * Registers the blog implementation with the blog integration service.
     */
    public void register()
    {
        this.blogIntegrationService.register(this);
    }
    
    /**
     * Sets the name of the blog integration service
     * 
     * @param name  the name
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * @see org.alfresco.module.blogIntegration.BlogIntegrationImplementation#getName()
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * Sets the display name
     * 
     * @param displayName   the display name
     */
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }
    
    /**
     * @see org.alfresco.module.blogIntegration.BlogIntegrationImplementation#getDisplayName()
     */
    public String getDisplayName()
    {
       return this.displayName;
    }
}
