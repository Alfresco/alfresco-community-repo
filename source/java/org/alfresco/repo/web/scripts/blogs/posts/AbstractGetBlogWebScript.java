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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.web.scripts.blogs.AbstractBlogWebScript;
import org.alfresco.repo.web.scripts.blogs.BlogPostLibJs;
import org.alfresco.repo.web.scripts.blogs.RequestUtilsLibJs;
import org.alfresco.service.cmr.blog.BlogPostInfo;
import org.alfresco.service.cmr.blog.BlogService.RangedDateProperty;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Neil Mc Erlean (based on existing JavaScript webscript controllers)
 * @since 4.0
 */
public abstract class AbstractGetBlogWebScript extends AbstractBlogWebScript
{
    private static final Log log = LogFactory.getLog(AbstractGetBlogWebScript.class);

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        
        // get requested node
        NodeRef node = RequestUtilsLibJs.getRequestNode(req, services);
        
        // process additional parameters. <index, count>
        PagingRequest pagingReq = parsePagingParams(req);
        
        // begin and end date.
        // Legacy note: these dates are URL query parameters in int form.
        Date fromDate = parseDateParam(req, "fromDate");
        Date toDate = parseDateParam(req, "toDate");
        
        String tag = req.getParameter("tag");
        if (tag != null && tag.length() == 0) tag = null;
        
        // One webscript (blog-posts-new.get) uses a 'numdays' parameter as a 'fromDate'.
        // This is a hacky solution to this special case. FIXME
        if (this.getClass().equals(BlogPostsNewGet.class))
        {
            // Default is for 'now' minus seven days.
            final int oneDayInMilliseconds = 24 * 60 * 60 * 1000;
            final long sevenDaysInMilliseconds = 7 * oneDayInMilliseconds;
            fromDate = new Date(System.currentTimeMillis() - sevenDaysInMilliseconds);
            
            // But if there is a numdays parameter then that changes the fromDate
            String numDays = req.getServiceMatch().getTemplateVars().get("numdays");
            if (numDays != null)
            {
                Integer numDaysInt = Integer.parseInt(numDays);
                fromDate = new Date(System.currentTimeMillis() - (numDaysInt * oneDayInMilliseconds));
            }
        }
        
        // fetch and assign the data
        PagingResults<BlogPostInfo> blogPostList = getBlogPostList(node, fromDate, toDate,
                                          tag, pagingReq);
                                          
        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Retrieved ").append(blogPostList.getPage().size()).append(" blog posts in page.");
            log.debug(msg.toString());
        }
        
        createFtlModel(req, model, node, pagingReq, blogPostList);
        
        return model;
    }

    protected void createFtlModel(WebScriptRequest req, Map<String, Object> model, NodeRef node, PagingRequest pagingReq,
                                  PagingResults<BlogPostInfo> blogPostList)
    {
        Map<String, Object> blogPostsData = new HashMap<String, Object>();
        
        final Pair<Integer, Integer> totalResultCount = blogPostList.getTotalResultCount();
        //FIXME What to do? null
        blogPostsData.put("total", totalResultCount.getFirst());
        blogPostsData.put("pageSize", pagingReq.getMaxItems());
        blogPostsData.put("startIndex", pagingReq.getSkipCount());
        blogPostsData.put("itemCount", blogPostList.getPage().size());
        
        List<Map<String, Object>> blogPostDataSets = new ArrayList<Map<String, Object>>(blogPostList.getPage().size());
        for (BlogPostInfo postInfo : blogPostList.getPage())
        {
            Map<String, Object> data = BlogPostLibJs.getBlogPostData(postInfo.getNodeRef(), services);
            blogPostDataSets.add(data);
        }
        blogPostsData.put("items", blogPostDataSets);

        model.put("data", blogPostsData);

        // fetch the contentLength param
        String contentLengthStr = req.getServiceMatch().getTemplateVars().get("contentLength");
        int contentLength = contentLengthStr == null ? -1 : Integer.parseInt(contentLengthStr);
        model.put("contentLength", contentLength);
        
        // assign the blog node
        model.put("blog", node);
        model.put("externalBlogConfig", BlogPostLibJs.hasExternalBlogConfiguration(node, services));
    }
    
    private PagingRequest parsePagingParams(WebScriptRequest req)
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String startIndexStr = templateVars.get("startIndex");
        String pageSizeStr = templateVars.get("pageSize");
        
        int startIndex = 0;
        int pageSize = 10;
        if (startIndexStr != null)
        {
            startIndex = Integer.parseInt(startIndexStr);
        }
        if (pageSizeStr != null)
        {
            pageSize = Integer.parseInt(pageSizeStr);
        }
        return new PagingRequest(startIndex, pageSize, null);
    }

    private Date parseDateParam(WebScriptRequest req, String paramName)
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String dateStr = templateVars.get(paramName);
        
        Date result = null;
        if (dateStr != null)
        {
            result = new Date(Integer.parseInt(dateStr));
        }
        return result;
    }
    
    
    /**
     * Fetches all posts of the given blog
     */
    private PagingResults<BlogPostInfo> getBlogPostList(NodeRef node, Date fromDate, Date toDate, String tag, PagingRequest pagingReq)
    {
        // Currently we only support CannedQuery-based gets without tags:
        if (tag == null || tag.trim().isEmpty())
        {
            return getBlogResultsImpl(node, fromDate, toDate, pagingReq);
        }
        else
        {
            return blogService.findBlogPosts(node, new RangedDateProperty(fromDate, toDate, ContentModel.PROP_CREATED), tag, pagingReq);
        }
    }
    
    protected abstract PagingResults<BlogPostInfo> getBlogResultsImpl(NodeRef node, Date fromDate, Date toDate, PagingRequest pagingReq);
}
