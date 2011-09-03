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
package org.alfresco.repo.web.scripts.blogs.blog;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.BlogIntegrationModel;
import org.alfresco.repo.web.scripts.blogs.AbstractBlogWebScript;
import org.alfresco.repo.web.scripts.blogs.BlogLibJs;
import org.alfresco.repo.web.scripts.blogs.RequestUtilsLibJs;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the blog.get web script.
 * 
 * @author Neil Mc Erlean (based on existing JavaScript webscript controllers)
 * @since 4.0
 */
public class BlogPut extends AbstractBlogWebScript
{
    @SuppressWarnings("deprecation")
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        
        // get requested node
        NodeRef node = RequestUtilsLibJs.getRequestNode(req, services);
        
        // parse the JSON
        JSONObject json = null;
        try
        {
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));
        } 
        catch (JSONException jsonX)
        {
            throw new AlfrescoRuntimeException("Could not parse JSON", jsonX);
        } 
        catch (IOException iox)
        {
            throw new AlfrescoRuntimeException("Could not parse JSON", iox);
        }
        
        updateBlog(node, json);
        
        model.put("item", node);
        
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
            nodeService.setProperties(node, arr);
        }
        else
        {
            nodeService.addAspect(node, BlogIntegrationModel.ASPECT_BLOG_DETAILS, arr);
        }
    }
}
