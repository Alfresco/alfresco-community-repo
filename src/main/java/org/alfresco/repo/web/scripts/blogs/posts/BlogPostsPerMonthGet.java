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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.blog.BlogPostInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the blog-posts-mypublished.get web script.
 * 
 * @author Neil Mc Erlean (based on existing JavaScript webscript controllers)
 * @since 4.0
 */
public class BlogPostsPerMonthGet extends AbstractGetBlogWebScript
{
    @Override
    protected PagingResults<BlogPostInfo> getBlogResultsImpl(
          SiteInfo site, NodeRef node, Date fromDate, Date toDate, PagingRequest pagingReq)
    {
        if(site != null)
        {
           return blogService.getPublished(site.getShortName(), fromDate, toDate, null, pagingReq);
        }
        else
        {
           return blogService.getPublished(node, fromDate, toDate, null, pagingReq);
        }
    }

    @Override
    protected void createFtlModel(WebScriptRequest req, Map<String, Object> model, NodeRef node, PagingRequest pagingReq, PagingResults<BlogPostInfo> blogPostList)
    {
        model.put(DATA, getBlogPostMonths(blogPostList));
    }
    
    
    /**
     * Ported from blog-posts-per-month.get.js
     */
    @SuppressWarnings("deprecation")
    private Date getBeginOfMonthDate(Date date)
    {
        //TODO These date processing methods are copied almost verbatim from JavaScript to preserve behaviour.
        // However they should be updated to use java.util.Calendar as the current implementation assumes a Gregorian calendar.
        Calendar calendar = Calendar.getInstance();
        calendar.set(date.getYear(), date.getMonth(), 1);
        return calendar.getTime();
    }
    
    /**
     * Returns the date representing the last second of a month (23:59:59)
     * Ported from blog-posts-per-month.get.js
     */
    @SuppressWarnings("deprecation")
    private Date getEndOfMonthDate(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(date.getYear(), date.getMonth(), date.getDay());
        // In Gregorian calendar, this would be 31 for January, 30 for March, 28 or 29 for February.
        int lastDayOfSpecifiedMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(date.getYear(), date.getMonth(), lastDayOfSpecifiedMonth, 23, 59, 59);
        
        return calendar.getTime();
    }
    
    /**
     * Create an object containing information about the month specified by date.
     * Ported from blog-posts-per-month.get.js
     */
    @SuppressWarnings("deprecation")
    private Map<String, Object> getMonthDataObject(Date date)
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("year", date.getYear() + 1900);
        data.put("month", date.getMonth());
        data.put("firstPostInMonth", date);
        data.put("beginOfMonth", getBeginOfMonthDate(date));
        data.put("endOfMonth", getEndOfMonthDate(date));
        data.put("count", 1);
        
        return data;
    }
    
    /**
     * Fetches data for each month for which posts exist, plus the count of each.
     * Note: If no posts could be found, this method will return the current month
     *       but with a count of posts equals zero.
     * Ported from blog-posts-per-month.get.js
     */
    @SuppressWarnings("deprecation")
    private List<Map<String, Object>> getBlogPostMonths(PagingResults<BlogPostInfo> nodes)
    {
        // will hold the months information
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
       
        // do we have posts?
        if (!nodes.getPage().isEmpty())
        {
            int currYear = -1;
            int currMonth = -1;
            Map<String, Object> currData = null;
            for (int x = 0; x < nodes.getPage().size(); x++)
            {
                NodeRef node = nodes.getPage().get(x).getNodeRef();
                Date date = (Date) nodeService.getProperty(node, ContentModel.PROP_PUBLISHED);
                
                // is this a new month?
                if (currYear != date.getYear() + 1900 || currMonth != date.getMonth())
                {
                    currYear = date.getYear() + 1900;
                    currMonth = date.getMonth();
                    currData = getMonthDataObject(date);
                    data.add(currData);
                }
                // otherwise just increment the counter
                else
                {
                    Object countObj = currData.get("count");
                    Integer countInt = countObj == null ? 0 : (Integer)countObj;
                    
                    currData.put("count", countInt + 1);
                }
            }
        }
        // if not, add the current month with count = 0
        else
        {
            Map<String, Object> emptyData = getMonthDataObject(new Date());
            emptyData.put("count", 0);
            data.add(emptyData);
        }
        
        return data;
    }
}
