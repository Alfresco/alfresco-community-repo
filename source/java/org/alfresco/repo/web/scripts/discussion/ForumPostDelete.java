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
 * This class is the controller for the forum post deleting forum-post.delete webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class ForumPostDelete extends AbstractDiscussionWebScript
{
   private static final String DELETED_POST_TEXT = "[[deleted]]";
   
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nodeRef,
         TopicInfo topic, PostInfo post, WebScriptRequest req, JSONObject json,
         Status status, Cache cache) 
   {
      // Build the common model parts
      Map<String, Object> model = buildCommonModel(site, topic, post, req);
      
      
      // Are we deleting a topic, or a post in it?
      String message = null;
      if(post != null)
      {
         message = doDeletePost(topic, post);
      }
      else if(topic != null)
      {
         message = doDeleteTopic(topic, site, req, json);
      }
      else
      {
         String error = "Node was of the wrong type, only Topic and Post are supported";
         throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
      }
   
      
      // Finish the model and return
      model.put("message", message);
      return model;
   }
   
   private String doDeleteTopic(TopicInfo topic, SiteInfo site, 
         WebScriptRequest req, JSONObject json)
   {
      // Delete the topic, which removes all its posts too
      discussionService.deleteTopic(topic);
      
      // Add an activity entry for this if it's site based
      if(site != null)
      {
         addActivityEntry("post", "deleted", topic, null, site, req, json);
      }
      
      // All done
      return "Node " + topic.getNodeRef() + " deleted";
   }
   
   /**
    * We can't just delete posts with replies attached to them,
    *  as that breaks the reply threading.
    * For that reason, we mark deleted posts with a special
    *  text contents.
    * TODO If a post has no replies, then delete it fully
    */
   private String doDeletePost(TopicInfo topic, PostInfo post)
   {
      // Set the marker text and save
      post.setTitle(DELETED_POST_TEXT);
      post.setContents(DELETED_POST_TEXT);
      discussionService.updatePost(post);
      
      // Note - we don't add activity feed entries for deleted posts
      //        Only deleted whole topic qualify for that at the moment
      
      return "Node " + post.getNodeRef() + " marked as removed";
   }
}
