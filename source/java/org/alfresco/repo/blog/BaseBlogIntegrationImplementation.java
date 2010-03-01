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
