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
package org.alfresco.repo.web.scripts.blogs.post;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.web.scripts.blogs.AbstractBlogWebScript;
import org.alfresco.service.cmr.blog.BlogPostInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the blog-posts.get web script.
 * 
 * @author Neil Mc Erlean (based on existing JavaScript webscript controllers)
 * @since 4.0
 */
public class BlogPostDelete extends AbstractBlogWebScript
{
    @Override
    protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nodeRef,
         BlogPostInfo blog, WebScriptRequest req, JSONObject json, Status status, Cache cache) 
    {
        if (blog == null)
        {
           throw new WebScriptException(Status.STATUS_NOT_FOUND, "Blog Post Not Found");
        }
        
        // TODO Get this from the BlogPostInfo Object
        final boolean isDraftBlogPost = blogService.isDraftBlogPost(blog.getNodeRef());
        
        // Have it deleted
        blogService.deleteBlogPost(blog);
        
        // If we're in a site, and it isn't a draft, add an activity
        if (site != null && !isDraftBlogPost)
        {
            addActivityEntry("deleted", blog, site, req, json, nodeRef);
        }

        // Report it as deleted
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("message", "Blog " + blog.getNodeRef() + " deleted");
        return model;
    }
}
