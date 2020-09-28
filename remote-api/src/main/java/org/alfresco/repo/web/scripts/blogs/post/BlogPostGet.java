/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.blogs.post;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.web.scripts.blogs.AbstractBlogWebScript;
import org.alfresco.repo.web.scripts.blogs.BlogPostLibJs;
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
public class BlogPostGet extends AbstractBlogWebScript
{
    @Override
    protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nodeRef,
         BlogPostInfo blog, WebScriptRequest req, JSONObject json, Status status, Cache cache) 
    {
        if (blog == null)
        {
           throw new WebScriptException(Status.STATUS_NOT_FOUND, "Blog Post Not Found");
        }

        // Build the response
        Map<String, Object> model = new HashMap<String, Object>();
        
        // TODO Fetch this from the BlogPostInfo object
        NodeRef node = blog.getNodeRef();
        Map<String, Object> item = BlogPostLibJs.getBlogPostData(node, services);
        model.put(ITEM, item);
        model.put(POST, blog);
        
        model.put("externalBlogConfig", BlogPostLibJs.hasExternalBlogConfiguration(node, services));
        
        int contentLength = -1;
        String arg = req.getParameter("contentLength");
        if (arg != null)
        {
            try
            {
                contentLength = Integer.parseInt(arg);
            }
            catch (NumberFormatException ignored)
            {
                // Intentionally empty
            }
        }
        
        model.put("contentLength", contentLength);
        
        return model;
    }
}
