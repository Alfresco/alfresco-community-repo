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
package org.alfresco.repo.web.scripts.discussion;

import java.util.Date;
import java.util.Map;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.discussion.PostInfo;
import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the discussions topics fetching forum-posts-new.get webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class ForumTopicsRecentGet extends AbstractDiscussionWebScript
{
   protected static final int RECENT_SEARCH_PERIOD_DAYS = 30;
   protected static final long ONE_DAY_MS = 24*60*60*1000;
   
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nodeRef,
         TopicInfo topic, PostInfo post, WebScriptRequest req, JSONObject json,
         Status status, Cache cache) 
   {
      // They shouldn't be trying to list of an existing Post or Topic
      if(topic != null || post != null)
      {
         String error = "Can't list Topics inside an existing Topic or Post";
         throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
      }
      
      // Grab the date range to search over
      String numDaysS = req.getParameter("numdays");
      int numDays = RECENT_SEARCH_PERIOD_DAYS;
      if(numDaysS != null)
      {
         numDays = Integer.parseInt(numDaysS);  
      }
      
      Date now = new Date();
      Date from = new Date(now.getTime() - numDays*ONE_DAY_MS);
      Date to = new Date(now.getTime() + ONE_DAY_MS);
      
      // Get the recent topics, newest first
      PagingResults<TopicInfo> topics = null;
      PagingRequest paging = buildPagingRequest(req);
      if(site != null)
      {
         topics = discussionService.listTopics(site.getShortName(), from, to, false, paging);
      }
      else
      {
         topics = discussionService.listTopics(nodeRef, from, to, false, paging);
      }
      
      
      // If they did a site based search, and the component hasn't
      //  been created yet, use the site for the permissions checking
      if(site != null && nodeRef == null)
      {
         nodeRef = site.getNodeRef();
      }
      
      
      // Build the common model parts
      Map<String, Object> model = buildCommonModel(site, topic, post, req);
      model.put("forum", nodeRef);
      
      // Have the topics rendered
      model.put("data", renderTopics(topics, paging, site));
      
      // All done
      return model;
   }
}
