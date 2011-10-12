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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.blogs.AbstractBlogWebScript;
import org.alfresco.repo.web.scripts.blogs.BlogPostLibJs;
import org.alfresco.repo.web.scripts.blogs.RequestUtilsLibJs;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.blog.BlogPostInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
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
    private ActivityService activityService;
    private TaggingService taggingService;
    
    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }
    
    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
    }
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        
        JsonParams jsonPostParams = parsePostParams(req);
        
        NodeRef node = RequestUtilsLibJs.getRequestNode(req, services);
        ensureTagScope(node);
        
        NodeRef post = createBlogPost(jsonPostParams, node);
        
        Map<String, Object> blogPostData = BlogPostLibJs.getBlogPostData(post, services);
        model.put(ITEM, blogPostData);
        model.put(EXTERNAL_BLOG_CONFIG, BlogPostLibJs.hasExternalBlogConfiguration(node, services));
        
        boolean isDraft = blogPostData.get(ITEM) != null &&
                          ((Boolean)blogPostData.get(ITEM)).booleanValue();
        if (jsonPostParams.getSite() != null &&
                jsonPostParams.getContainer() != null &&
                jsonPostParams.getPage() != null &&
                !isDraft)
        {
            final NodeRef nodeParam = (NodeRef)blogPostData.get(NODE);
            String postNodeName = (String)nodeService.getProperty(nodeParam, ContentModel.PROP_NAME);
            String postNodeTitle = (String)nodeService.getProperty(nodeParam, ContentModel.PROP_TITLE);
            String data = null;
            try
            {
                data = new JSONStringer()
                    .object()
                        .key(TITLE).value(postNodeTitle)
                        .key(PAGE).value(jsonPostParams.getPage() + "?postId=" + postNodeName)
                    .endObject().toString();
            } catch (JSONException e)
            {
                // Intentionally empty
            }
            if (data != null)
            {
                activityService.postActivity("org.alfresco.blog.post-created", jsonPostParams.getSite(), "blog", data);
            }
        }
        
        return model;
    }
    
    private JsonParams parsePostParams(WebScriptRequest req)
    {
        try
        {
            JSONObject json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            
            JsonParams result = new JsonParams();
            if (json.has(TITLE))
            {
                result.setTitle(json.getString(TITLE));
            }
            if (json.has(CONTENT))
            {
                result.setContent(json.getString(CONTENT));
            }
            if (json.has(DRAFT))
            {
                result.setIsDraft(json.getString(DRAFT));
            }
            // If there are no tags, this is a java.lang.String "".
            // If there are any tags, it's a JSONArray of strings. One or more.
            if (json.has(TAGS))
            {
                Object tagsObj = json.get(TAGS);
                List<String> tags = new ArrayList<String>();
                if (tagsObj instanceof JSONArray)
                {
                    JSONArray tagsJsonArray = (JSONArray)tagsObj;
                    for (int i = 0; i < tagsJsonArray.length(); i++)
                    {
                        tags.add(tagsJsonArray.getString(i));
                    }
                }
                else
                {
                    tags.add(tagsObj.toString());
                }
                result.setTags(tags);
            }
            if (json.has(SITE))
            {
                result.setSite(json.getString(SITE));
            }
            if (json.has(PAGE))
            {
                result.setPage(json.getString(PAGE));
            }
            if (json.has(CONTAINER))
            {
                result.setContainer(json.getString(CONTAINER));
            }
            
            return result;
        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from req.", je);
        }
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
    private NodeRef createBlogPost(JsonParams jsonParams, NodeRef blogNode)
    {
        String titleParam = jsonParams.getTitle() == null ? "" : jsonParams.getTitle();
        String contentParam = jsonParams.getContent() == null ? "" : jsonParams.getContent();
        boolean isDraftParam = jsonParams.getIsDraft() == null ? false : Boolean.parseBoolean(jsonParams.getIsDraft());
        
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
        
        BlogPostInfo newPostNode = blogService.createBlogPost(blogNode, titleParam, contentParam, isDraftParam);
        
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
        
        return newPostNode.getNodeRef();
    }
    
    /**
     * A simple POJO class for the parsed JSON from the POST body.
     */
    class JsonParams
    {
        private String title;
        private String content;
        private String isDraft; //This is a String, not a boolean
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
        public String getIsDraft()
        {
            return isDraft;
        }
        public void setIsDraft(String isDraft)
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
