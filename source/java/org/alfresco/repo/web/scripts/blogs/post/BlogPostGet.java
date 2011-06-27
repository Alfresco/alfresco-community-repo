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
import org.alfresco.repo.web.scripts.blogs.BlogPostLibJs;
import org.alfresco.repo.web.scripts.blogs.RequestUtilsLibJs;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the blog-posts.get web script.
 * 
 * @author Neil Mc Erlean (based on existing JavaScript webscript controllers)
 * @since 4.0
 */
public class BlogPostGet extends AbstractBlogWebScript
{
    @SuppressWarnings("deprecation")
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        
        // get requested node
        NodeRef node = RequestUtilsLibJs.getRequestNode(req, services);
        Map<String, Object> item = BlogPostLibJs.getBlogPostData(node, services);
        model.put("item", item);
        
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
