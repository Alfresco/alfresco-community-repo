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
 * This class is the controller for the discussions page editing forum-post.put webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class ForumPostPut extends AbstractDiscussionWebScript
{
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nodeRef,
         TopicInfo topic, PostInfo post, WebScriptRequest req, JSONObject json,
         Status status, Cache cache) 
   {
      // Build the common model parts
      Map<String, Object> model = buildCommonModel(site, topic, post, req);
      
      // Did they want to change a reply or the whole topic?
      if (post != null)
      {
         // Update the specified post
         doUpdatePost(post, post.getTopic(), req, json);
         
         // Add the activity entry for the reply change
         addActivityEntry("reply", "updated", post.getTopic(), post, site, req, json);
         
         // Build the JSON for just this post
         model.put(KEY_POSTDATA, renderPost(post, site));
      }
      else if (topic != null)
      {
         // Update the primary post of the topic
         post = discussionService.getPrimaryPost(topic);
         if (post == null)
         {
            throw new WebScriptException(Status.STATUS_PRECONDITION_FAILED,
                  "First (primary) post was missing from the topic, can't fetch");
         }
         doUpdatePost(post, topic, req, json);
         
         // Add the activity entry for the topic change
         addActivityEntry("post", "updated", topic, null, site, req, json);
         
         // Build the JSON for the whole topic
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
   
   private void doUpdatePost(PostInfo post, TopicInfo topic, WebScriptRequest req, 
         JSONObject json)
   {
       boolean updateTopic = false;
      // Fetch the details from the JSON

       // Update the titles on the post and it's topic
      if (json.containsKey("title"))
      {
         String title = (String)json.get("title");
         post.setTitle(title);
         if (title.length() > 0)
         {
            updateTopic = true;
            topic.setTitle(title);
         }
      }
      
      // Contents is on the post
      if (json.containsKey("content"))
      {
         post.setContents((String)json.get("content"));
      }
      
      // Tags are on the topic
      if (json.containsKey("tags"))
      {
         topic.getTags().clear();
         
         List<String> tags = getTags(json);
         if (tags != null)
         {
            topic.getTags().addAll(tags);
         }
         updateTopic = true;
      }
      
      // Save the topic and the post
      if (updateTopic == true) 
      {
          discussionService.updateTopic(topic);
      }
      discussionService.updatePost(post);
   }
}
