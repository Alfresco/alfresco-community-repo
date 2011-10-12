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
package org.alfresco.repo.web.scripts.blogs.posts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.web.scripts.blogs.AbstractBlogWebScript;
import org.alfresco.repo.web.scripts.blogs.BlogPostLibJs;
import org.alfresco.service.cmr.blog.BlogPostInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the blog-posts.post web script.
 * 
 * @author Neil Mc Erlean (based on existing JavaScript webscript controllers)
 * @since 4.0
 */
public class BlogPostsPost extends AbstractBlogWebScript
{
    private static final Log log = LogFactory.getLog(BlogPostsPost.class);

    // Injected services
    private TaggingService taggingService;
    
    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
    }
    
    @Override
    protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nodeRef,
         BlogPostInfo blog, WebScriptRequest req, JSONObject json, Status status, Cache cache) 
    {
        Map<String, Object> model = new HashMap<String, Object>();
        
        // If they're doing Path Based rather than Site Based, ensure
        //  that the Container is a Tag Scope
        if(site == null && nodeRef != null)
        {
           ensureTagScope(nodeRef);
        }

        // Have the Blog Post created
        JsonParams jsonPostParams = parsePostParams(json);
        BlogPostInfo post = createBlogPost(jsonPostParams, site, nodeRef);
        
        Map<String, Object> blogPostData = BlogPostLibJs.getBlogPostData(post.getNodeRef(), services);
        model.put(ITEM, blogPostData);
        model.put(EXTERNAL_BLOG_CONFIG, BlogPostLibJs.hasExternalBlogConfiguration(nodeRef, services));
        
        boolean isDraft = blogPostData.get(ITEM) != null &&
                          ((Boolean)blogPostData.get(ITEM)).booleanValue();
        if (jsonPostParams.getSite() != null &&
                jsonPostParams.getContainer() != null &&
                jsonPostParams.getPage() != null &&
                !isDraft)
        {
            addActivityEntry("created", post, site, req, json);
        }
        
        return model;
    }
    
    private JsonParams parsePostParams(JSONObject json)
    {
       JsonParams result = new JsonParams();
       if (json.containsKey(TITLE))
       {
          result.setTitle((String)json.get(TITLE));
       }
       if (json.containsKey(CONTENT))
       {
          result.setContent((String)json.get(CONTENT));
       }
       if (json.containsKey(DRAFT))
       {
          result.setIsDraft((Boolean)json.get(DRAFT));
       }
       
       // If there are no tags, this is a java.lang.String "".
       // If there are any tags, it's a JSONArray of strings. One or more.
       if (json.containsKey(TAGS))
       {
          Object tagsObj = json.get(TAGS);
          List<String> tags = new ArrayList<String>();
          if (tagsObj instanceof JSONArray)
          {
             JSONArray tagsJsonArray = (JSONArray)tagsObj;
             for (int i = 0; i < tagsJsonArray.size(); i++)
             {
                tags.add( (String)tagsJsonArray.get(i) );
             }
          }
          else
          {
             tags.add(tagsObj.toString());
          }
          result.setTags(tags);
       }
       if (json.containsKey(SITE))
       {
          result.setSite((String)json.get(SITE));
       }
       if (json.containsKey(PAGE))
       {
          result.setPage((String)json.get(PAGE));
       }
       if (json.containsKey(CONTAINER))
       {
          result.setContainer((String)json.get(CONTAINER));
       }

       return result;
    }
    
    /**
     * Taken from JS
     * @param node
     */
    private void ensureTagScope(NodeRef node)
    {
       if (!taggingService.isTagScope(node))
       {
           taggingService.addTagScope(node);
       }
       
       
       // also check the parent (the site!)
       NodeRef parent = nodeService.getPrimaryParent(node).getParentRef();
       if (!taggingService.isTagScope(parent))
       {
           taggingService.addTagScope(parent);
       }
    }
    
    /**
     * Creates a blog post
     */
    private BlogPostInfo createBlogPost(JsonParams jsonParams, SiteInfo site, NodeRef blogNode)
    {
        String titleParam = jsonParams.getTitle() == null ? "" : jsonParams.getTitle();
        String contentParam = jsonParams.getContent() == null ? "" : jsonParams.getContent();
        boolean isDraftParam = jsonParams.getIsDraft();
        
        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Creating blog-post '").append(titleParam).append("'");
            if (isDraftParam)
            {
                msg.append(" DRAFT");
            }
            log.debug(msg.toString());
        }
        
        List<String> tagsParam = new ArrayList<String>();
        if (jsonParams.getTags() != null)
        {
            tagsParam.addAll(jsonParams.getTags());
        }
        
        BlogPostInfo newPostNode;
        if(site != null)
        {
           newPostNode = blogService.createBlogPost(
                 site.getShortName(), titleParam, contentParam, isDraftParam);
        }
        else
        {
           newPostNode = blogService.createBlogPost(
                 blogNode, titleParam, contentParam, isDraftParam);
        }
        
        // Ignore empty string tags
        List<String> nonEmptyTags = new ArrayList<String>();
        for (String tag : tagsParam)
        {
            if (!tag.trim().isEmpty())
            {
                nonEmptyTags.add(tag);
            }
        }
        if (!nonEmptyTags.isEmpty())
        {
            taggingService.setTags(newPostNode.getNodeRef(), nonEmptyTags);
        }
        
        return newPostNode;
    }
    
    /**
     * A simple POJO class for the parsed JSON from the POST body.
     */
    class JsonParams
    {
        private String title;
        private String content;
        private boolean isDraft = false;
        private List<String> tags;
        private String site;
        private String container;
        private String page;
        
        public String getTitle()
        {
            return title;
        }
        public void setTitle(String title)
        {
            this.title = title;
        }
        public String getContent()
        {
            return content;
        }
        public void setContent(String content)
        {
            this.content = content;
        }
        public boolean getIsDraft()
        {
            return isDraft;
        }
        public void setIsDraft(boolean isDraft)
        {
            this.isDraft = isDraft;
        }
        public List<String> getTags()
        {
            return tags;
        }
        public void setTags(List<String> tags)
        {
            this.tags = tags;
        }
        public String getSite()
        {
            return site;
        }
        public void setSite(String site)
        {
            this.site = site;
        }
        public String getContainer()
        {
            return container;
        }
        public void setContainer(String container)
        {
            this.container = container;
        }
        public String getPage()
        {
            return page;
        }
        public void setPage(String page)
        {
            this.page = page;
        }
    }
}
