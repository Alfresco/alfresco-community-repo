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

import java.io.IOException;
import java.util.Map;

import org.alfresco.repo.blog.BlogServiceImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.blog.BlogPostInfo;
import org.alfresco.service.cmr.blog.BlogService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONStringer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

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
    protected static final String POST                 = "post";
    protected static final String ITEM                 = "item";
    protected static final String NODE                 = "node";
    protected static final String PAGE                 = "page";
    protected static final String SITE                 = "site";
    protected static final String TAGS                 = "tags";
    protected static final String TITLE                = "title";
    
    private static Log logger = LogFactory.getLog(AbstractBlogWebScript.class);
    
    // Injected services
    protected Repository repository;
    protected BlogService blogService;
    protected NodeService nodeService;
    protected SiteService siteService;
    protected ActivityService activityService;
    
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
    
    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }

    /**
     * Generates an activity entry for the discussion item
     * 
     * @param thing Either post or reply
     * @param event One of created, updated, deleted
     */
    protected void addActivityEntry(String event, BlogPostInfo blog, 
          SiteInfo site, WebScriptRequest req, JSONObject json)
    {
       // We can only add activities against a site
       if (site == null)
       {
          logger.info("Unable to add activity entry for blog " + event + " as no site given");
          return;
       }
       
       // What page is this for?
       String page = req.getParameter("page");
       if (page == null && json != null)
       {
          if (json.containsKey("page"))
          {
             page = (String)json.get("page");
          }
       }
       if (page == null)
       {
          // Default
          page = "blog-postview";
       }
       if (page.indexOf('?') == -1)
       {
          page += "?postId=" + blog.getSystemName();
       }
       
       // Get the title
       String title = blog.getTitle();
       
       try
       {
          String data = new JSONStringer()
              .object()
                  .key(TITLE).value(title)
                  .key(PAGE).value(page)
              .endObject().toString();
          
          activityService.postActivity(
                "org.alfresco.blog.post-" + event,
                site.getShortName(),
                "blog", data);
       }
       catch(Exception e)
       {
          // Warn, but carry on
          logger.warn("Error adding blog post " + event + " to activities feed", e);
       }
    }
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) 
    {
       Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
       if (templateVars == null)
       {
          String error = "No parameters supplied";
          throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
       }
       
       
       // Parse the JSON, if supplied
       JSONObject json = null;
       String contentType = req.getContentType();
       if (contentType != null && contentType.indexOf(';') != -1)
       {
          contentType = contentType.substring(0, contentType.indexOf(';'));
       }
       if (MimetypeMap.MIMETYPE_JSON.equals(contentType))
       {
          JSONParser parser = new JSONParser();
          try
          {
             json = (JSONObject)parser.parse(req.getContent().getContent());
          }
          catch (IOException io)
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + io.getMessage());
          }
          catch (ParseException pe)
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + pe.getMessage());
          }
       }
       
       
       // Did they request it by node reference or site?
       NodeRef nodeRef = null;
       SiteInfo site = null;
       BlogPostInfo blog = null;
       
       if (templateVars.containsKey("site"))
       {
          // Site, and Optionally Blog Post
          String siteName = templateVars.get("site");
          site = siteService.getSite(siteName);
          if (site == null)
          {
             String error = "Could not find site: " + siteName;
             throw new WebScriptException(Status.STATUS_NOT_FOUND, error);
          }
          
          // Did they give a blog post name too?
          if (templateVars.containsKey("path"))
          {
             String name = templateVars.get("path");
             blog = blogService.getBlogPost(siteName, name);
             
             if (blog == null)
             {
                String error = "Could not find blog '" + name + "' for site '" + 
                               site.getShortName() + "'";
                throw new WebScriptException(Status.STATUS_NOT_FOUND, error);
             }
             nodeRef = blog.getNodeRef();
          }
          else
          {
             // The NodeRef is the container (if it exists)
             if (siteService.hasContainer(siteName, BlogServiceImpl.BLOG_COMPONENT))
             {
                nodeRef = siteService.getContainer(siteName, BlogServiceImpl.BLOG_COMPONENT);
             }
          }
       }
       else if (templateVars.containsKey("store_type") && 
                templateVars.containsKey("store_id") &&
                templateVars.containsKey("id"))
       {
          // NodeRef, should be a Blog Post
          StoreRef store = new StoreRef(
                templateVars.get("store_type"),
                templateVars.get("store_id"));
          
          nodeRef = new NodeRef(store, templateVars.get("id"));
          if (! nodeService.exists(nodeRef))
          {
             String error = "Could not find node: " + nodeRef;
             throw new WebScriptException(Status.STATUS_NOT_FOUND, error);
          }
          
          // Try to build the appropriate object for it
          blog = blogService.getForNodeRef(nodeRef);
          
          // See if it's actually attached to a site
          if (blog != null)
          {
             NodeRef container = blog.getContainerNodeRef();
             if (container != null)
             {
                NodeRef maybeSite = nodeService.getPrimaryParent(container).getParentRef();
                if (maybeSite != null)
                {
                   // Try to make it a site, will return Null if it isn't one
                   site = siteService.getSite(maybeSite);
                }
             }
          }
       }
       else
       {
          String error = "Unsupported template parameters found";
          throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
       }
       
       // Have the real work done
       return executeImpl(site, nodeRef, blog, req, json, status, cache); 
    }
    
    protected abstract Map<String, Object> executeImpl(SiteInfo site,
          NodeRef nodeRef, BlogPostInfo blog, WebScriptRequest req, 
          JSONObject json, Status status, Cache cache);
}
