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
package org.alfresco.repo.web.scripts.discussion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.discussion.PostInfo;
import org.alfresco.service.cmr.discussion.PostWithReplies;
import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the discussions page creating forum-post-replies.get webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class ForumPostRepliesGet extends AbstractDiscussionWebScript
{
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nodeRef,
         TopicInfo topic, PostInfo post, WebScriptRequest req, JSONObject json,
         Status status, Cache cache) 
   {
      // How many levels did they want?
      int levels = 1;
      String levelsS = req.getParameter("levels");
      if (levelsS != null)
      {
         try
         {
            levels = Integer.parseInt(levelsS);
         }
         catch (NumberFormatException e)
         {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Level depth parameter invalid");
         }
      }
      
      // Fetch the replies
      PostWithReplies replies;
      if (post != null)
      {
         replies = discussionService.listPostReplies(post, levels);
      }
      else if (topic != null)
      {
         replies = discussionService.listPostReplies(topic, levels);
      }
      else 
      {
         String error = "Node was of the wrong type, only Topic and Post are supported";
         throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
      }
      
      // Build the common model parts
      Map<String, Object> model = buildCommonModel(site, topic, post, req);
      
      // Build the JSON for the replies
      model.put("data", renderReplies(replies, site).get("children"));  
      
      // All done
      return model;
   }
   
   private Map<String, Object> renderReplies(PostWithReplies replies, SiteInfo site)
   {
      Map<String, Object> reply = renderPost(replies.getPost(), site);
      reply.put("childCount", replies.getReplies().size());
      
      List<Map<String,Object>> r = new ArrayList<Map<String,Object>>();
      for (PostWithReplies child : replies.getReplies())
      {
         r.add(renderReplies(child, site));
      }
      reply.put("children", r);
      
      return reply;
   }
}
