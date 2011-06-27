/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.blogs;

import org.alfresco.repo.blog.BlogService;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

/**
 * @author Neil Mc Erlean
 * @since 4.0
 */
public abstract class AbstractBlogWebScript extends DeclarativeWebScript
{
    // Various common parameter strings in the blog webscripts.
    protected static final String CONTAINER            = "container";
    protected static final String CONTENT              = "content";
    protected static final String DATA                 = "data";
    protected static final String DRAFT                = "draft";
    protected static final String EXTERNAL_BLOG_CONFIG = "externalBlogConfig";
    protected static final String ITEM                 = "item";
    protected static final String NODE                 = "node";
    protected static final String PAGE                 = "page";
    protected static final String SITE                 = "site";
    protected static final String TAGS                 = "tags";
    protected static final String TITLE                = "title";
    
    // Injected services
    protected Repository repository;
    protected BlogService blogService;
    protected NodeService nodeService;
    protected SiteService siteService;
    
    //TODO Remove this after full refactor
    protected ServiceRegistry services;
    
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }
    
    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }
    
    public void setBlogService(BlogService blogService)
    {
        this.blogService = blogService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
}
