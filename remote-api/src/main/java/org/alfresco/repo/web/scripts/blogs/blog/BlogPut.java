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
package org.alfresco.repo.web.scripts.blogs.blog;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.BlogIntegrationModel;
import org.alfresco.repo.blog.BlogServiceImpl;
import org.alfresco.repo.web.scripts.blogs.AbstractBlogWebScript;
import org.alfresco.repo.web.scripts.blogs.BlogLibJs;
import org.alfresco.service.cmr.blog.BlogPostInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the blog.put web script.
 * 
 * TODO Push most of the logic from this into the BlogService
 * 
 * @author Neil Mc Erlean (based on existing JavaScript webscript controllers)
 * @since 4.0
 */
public class BlogPut extends AbstractBlogWebScript
{
    @Override
    protected Map<String, Object> executeImpl(SiteInfo site, NodeRef containerNodeRef,
         BlogPostInfo blog, WebScriptRequest req, JSONObject json, Status status, Cache cache) 
    {
       if (blog != null)
       {
          // They appear to have supplied a blog post itself...
          // Oh well, let's hope for the best!
           throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Blog post should not be updated via this web script.");
       }
       
       if (site != null && containerNodeRef == null)
       {
          // Force the lazy creation
          // This is a bit icky, but it'll have to do for now...
          containerNodeRef = siteService.createContainer(
                site.getShortName(), BlogServiceImpl.BLOG_COMPONENT, null, null);
       }
        
       // Do the work
       updateBlog(containerNodeRef, json);

       // Record it as done
       Map<String, Object> model = new HashMap<String, Object>();
       model.put("item", containerNodeRef);

       return model;
    }
    
    /**
     * Creates a post inside the passed forum node.
     */
    @SuppressWarnings("deprecation")
    private void updateBlog(NodeRef node, JSONObject json)
    {
        Map<QName, Serializable> arr = BlogLibJs.getBlogPropertiesArray(json);
        
        if (nodeService.hasAspect(node, BlogIntegrationModel.ASPECT_BLOG_DETAILS))
        {
            Map<QName, Serializable> properties = nodeService.getProperties(node);
            properties.putAll(arr);
            nodeService.setProperties(node, properties);
        }
        else
        {
            nodeService.addAspect(node, BlogIntegrationModel.ASPECT_BLOG_DETAILS, arr);
        }
    }
}
