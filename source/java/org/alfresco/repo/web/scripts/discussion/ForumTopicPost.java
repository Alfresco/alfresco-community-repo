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

import java.util.List;
import java.util.Map;

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
 * This class is the controller for the discussions page editing forum-posts.post webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class ForumTopicPost extends AbstractDiscussionWebScript
{
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nodeRef,
         TopicInfo topic, PostInfo post, WebScriptRequest req, JSONObject json,
         Status status, Cache cache) 
   {
      // They shouldn't be adding to an existing Post or Topic
      if (topic != null || post != null)
      {
         String error = "Can't create a new Topic inside an existing Topic or Post";
         throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
      }
      
      
      // Grab the details of the new Topic and Post
      String title = "";
      String contents = "";
      if (json.containsKey("title"))
      {
         title = (String)json.get("title");
      }
      if (json.containsKey("content"))
      {
         contents = (String)json.get("content");
      }
      List<String> tags = getTags(json);
      
      
      // Have the topic created
      if (site != null)
      {
         topic = discussionService.createTopic(site.getShortName(), title);
      }
      else
      {
         topic = discussionService.createTopic(nodeRef, title);
      }
      if (tags != null && tags.size() > 0)
      {
         topic.getTags().clear();
         topic.getTags().addAll(tags);
         discussionService.updateTopic(topic);
      }
      
      
      // Have the primary post created
      post = discussionService.createPost(topic, contents);
      
      
      // Record the activity
      addActivityEntry("post", "created", topic, post, site, req, json);
      
      
      // Build the common model parts
      Map<String, Object> model = buildCommonModel(site, topic, post, req);
      
      // Build the JSON for the whole topic
      model.put(KEY_POSTDATA, renderTopic(topic, site));
      
      // All done
      return model;
   }
}
