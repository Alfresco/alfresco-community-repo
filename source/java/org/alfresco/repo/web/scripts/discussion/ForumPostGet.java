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
 * This class is the controller for the discussions page fetching forum-post.get webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class ForumPostGet extends AbstractDiscussionWebScript
{
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nodeRef,
         TopicInfo topic, PostInfo post, WebScriptRequest req, JSONObject json,
         Status status, Cache cache) 
   {
      // Build the common model parts
      Map<String, Object> model = buildCommonModel(site, topic, post, req);
      
      // Did they want just one post, or the whole of the topic?
      if (post != null)
      {
         model.put(KEY_POSTDATA, renderPost(post, site));
      }
      else if (topic != null)
      {
         model.put(KEY_POSTDATA, renderTopic(topic, site));
      }
      else
      {
         String error = "Node was of the wrong type, only Topic and Post are supported";
         throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
      }
      
      // All done
      return model;
   }
}
