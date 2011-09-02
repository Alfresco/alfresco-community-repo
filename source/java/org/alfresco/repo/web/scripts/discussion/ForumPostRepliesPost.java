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

import org.alfresco.model.ContentModel;
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
 * This class is the controller for the discussions page creating forum-post-replies.post webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class ForumPostRepliesPost extends AbstractDiscussionWebScript
{
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nodeRef,
         TopicInfo topic, PostInfo post, WebScriptRequest req, JSONObject json,
         Status status, Cache cache) 
   {
      // If they're trying to create a reply to a topic, they actually
      //  mean to create the reply on the primary post
      if(post == null)
      {
         post = discussionService.getPrimaryPost(topic);
         if(post == null)
         {
            throw new WebScriptException(Status.STATUS_PRECONDITION_FAILED,
                  "First (primary) post was missing from the topic, can't fetch");
         }
      }
      else if(topic == null)
      {
         String error = "Node was of the wrong type, only Topic and Post are supported";
         throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
      }
      
      // Have the reply created
      PostInfo reply = doCreatePost(post, topic, req, json);
      
      // Add the activity entry for the reply change
      addActivityEntry("reply", "created", topic, reply, site, req, json);
      
      // Build the common model parts
      Map<String, Object> model = buildCommonModel(site, topic, reply, req);
      
      // Build the JSON for the new reply post
      model.put(KEY_POSTDATA, renderPost(reply, site));
      
      // All done
      return model;
   }
   
   private PostInfo doCreatePost(PostInfo post, TopicInfo topic, WebScriptRequest req, 
         JSONObject json)
   {
      // Fetch the details from the JSON
      String title = null;
      if(json.containsKey("title"))
      {
         title = (String)json.get("title");
      }
      
      String contents = null;
      if(json.containsKey("content"))
      {
         contents = (String)json.get("content");
      }
         
      
      // Create the reply
      PostInfo reply = discussionService.createReply(post, contents);
      
      // Set the title if needed (it normally isn't)
      if(title != null && title.length() > 0)
      {
         nodeService.setProperty(reply.getNodeRef(), ContentModel.PROP_TITLE, title);
         reply = discussionService.getPost(topic, reply.getSystemName());
      }
      
      // All done
      return reply;
   }
}
