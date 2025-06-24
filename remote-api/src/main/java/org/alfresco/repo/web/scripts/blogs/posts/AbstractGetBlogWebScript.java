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
package org.alfresco.repo.web.scripts.blogs.posts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.web.scripts.blogs.AbstractBlogWebScript;
import org.alfresco.repo.web.scripts.blogs.BlogPostLibJs;
import org.alfresco.service.cmr.blog.BlogPostInfo;
import org.alfresco.service.cmr.blog.BlogService.RangedDateProperty;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.util.Pair;
import org.alfresco.util.ScriptPagingDetails;

/**
 * @author Neil Mc Erlean (based on existing JavaScript webscript controllers)
 * @since 4.0
 */
public abstract class AbstractGetBlogWebScript extends AbstractBlogWebScript
{
    private static final Log log = LogFactory.getLog(AbstractGetBlogWebScript.class);

    @Override
    protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nonSiteContainer,
            BlogPostInfo blog, WebScriptRequest req, JSONObject json, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        // process additional parameters. <index, count>
        PagingRequest pagingReq = parsePagingParams(req);
        pagingReq.setRequestTotalCountMax(pagingReq.getSkipCount() + pagingReq.getRequestTotalCountMax());

        // begin and end date.
        // Legacy note: these dates are URL query parameters in int form.
        Date fromDate = parseDateParam(req, "fromDate");
        Date toDate = parseDateParam(req, "toDate");

        String tag = req.getParameter("tag");
        if (tag == null || tag.length() == 0)
        {
            tag = null;
        }
        else
        {
            // Tags can be full unicode strings, so decode
            tag = URLDecoder.decode(tag);
        }

        // One webscript (blog-posts-new.get) uses a 'numdays' parameter as a 'fromDate'.
        // This is a hacky solution to this special case. FIXME
        if (this.getClass().equals(BlogPostsNewGet.class))
        {
            // Default is for 'now' minus seven days.
            final long oneDayInMilliseconds = 24 * 60 * 60 * 1000;
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

        // Fetch and assign the data
        PagingResults<BlogPostInfo> blogPostList = getBlogPostList(site, nonSiteContainer, fromDate, toDate, tag, pagingReq);

        // We need the container for various bits
        NodeRef container = nonSiteContainer;
        if (container == null)
        {
            // Container mustn't exist yet
            // Fake it with the site for permissions checking reasons
            container = site.getNodeRef();
        }

        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Retrieved ").append(blogPostList.getPage().size()).append(" blog posts in page.");
            log.debug(msg.toString());
        }

        createFtlModel(req, model, container, pagingReq, blogPostList);

        return model;
    }

    protected void createFtlModel(WebScriptRequest req, Map<String, Object> model, NodeRef node, PagingRequest pagingReq,
            PagingResults<BlogPostInfo> blogPostList)
    {
        Map<String, Object> blogPostsData = new HashMap<String, Object>();

        final Pair<Integer, Integer> totalResultCount = blogPostList.getTotalResultCount();
        int total = blogPostList.getPage().size();
        if (totalResultCount != null && totalResultCount.getFirst() != null)
        {
            total = totalResultCount.getFirst();
        }
        // FIXME What to do? null
        blogPostsData.put("total", total);
        blogPostsData.put("pageSize", pagingReq.getMaxItems());
        blogPostsData.put("startIndex", pagingReq.getSkipCount());
        blogPostsData.put("itemCount", blogPostList.getPage().size());

        if (total == pagingReq.getRequestTotalCountMax())
        {
            blogPostsData.put("totalRecordsUpper", true);
        }
        else
        {
            blogPostsData.put("totalRecordsUpper", false);
        }

        List<Map<String, Object>> blogPostDataSets = new ArrayList<Map<String, Object>>(blogPostList.getPage().size());
        for (BlogPostInfo postInfo : blogPostList.getPage())
        {
            Map<String, Object> data = BlogPostLibJs.getBlogPostData(postInfo.getNodeRef(), services);
            blogPostDataSets.add(data);
        }
        blogPostsData.put("items", blogPostDataSets);

        model.put("data", blogPostsData);

        // fetch the contentLength param
        String contentLengthStr = req.getParameter("contentLength");
        int contentLength = contentLengthStr == null ? -1 : Integer.parseInt(contentLengthStr);
        model.put("contentLength", contentLength);

        // assign the blog node
        model.put("blog", node);
        model.put("externalBlogConfig", BlogPostLibJs.hasExternalBlogConfiguration(node, services));
    }

    private PagingRequest parsePagingParams(WebScriptRequest req)
    {
        return ScriptPagingDetails.buildPagingRequest(req, 1000);
    }

    private Date parseDateParam(WebScriptRequest req, String paramName)
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String dateStr = templateVars.get(paramName);
        if (dateStr == null)
        {
            // Try on the parameters instead
            dateStr = req.getParameter(paramName);
        }

        // Parse if available
        Date result = null;
        if (dateStr != null)
        {
            result = new Date(Long.parseLong(dateStr));
        }
        return result;
    }

    /**
     * Fetches all posts of the given blog
     */
    private PagingResults<BlogPostInfo> getBlogPostList(SiteInfo site, NodeRef nonSiteContainer,
            Date fromDate, Date toDate, String tag, PagingRequest pagingReq)
    {
        // Currently we only support CannedQuery-based gets without tags:
        if (tag == null || tag.trim().isEmpty())
        {
            return getBlogResultsImpl(site, nonSiteContainer, fromDate, toDate, pagingReq);
        }
        else
        {
            RangedDateProperty dateRange = new RangedDateProperty(fromDate, toDate, ContentModel.PROP_CREATED);
            if (site != null)
            {
                return blogService.findBlogPosts(site.getShortName(), dateRange, tag, pagingReq);
            }
            else
            {
                return blogService.findBlogPosts(nonSiteContainer, dateRange, tag, pagingReq);
            }
        }
    }

    protected abstract PagingResults<BlogPostInfo> getBlogResultsImpl(
            SiteInfo site, NodeRef nonSiteContainer, Date fromDate, Date toDate, PagingRequest pagingReq);
}
