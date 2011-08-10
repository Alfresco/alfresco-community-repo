<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">

const ASPECT_SYNDICATION = "cm:syndication";
const PROP_PUBLISHED = "cm:published";
const PROP_UPDATED = "cm:updated";

/**
 * Updates the passed forum post node.
 * @param topic the topic node if the post is the top level post
 * @param post the post node.
 */
function updatePost(topic, post)
{
   var title = "";
   if (json.has("title"))
   {
      title = json.get("title");
   }
   var content = "";
   if (json.has("content"))
   {
      content = json.get("content");
   }
   var tags = [];
   if (json.has("tags"))
   {
      // get the tags JSONArray and copy it into a real javascript array object
      var tmp = json.get("tags");
      for (var x=0; x < tmp.length(); x++)
      {
          tags.push(tmp.get(x));
      }
   }
   
   // update the topic title
   post.properties["cm:title"] = title;
   if(title != undefined && title.length > 0 && topic != undefined)
   {
       topic.properties["cm:title"] = title;
       topic.save();
   }

   // make sure the syndication aspect has been added
   if (! post.hasAspect(ASPECT_SYNDICATION))
   {
      var params = [];
      params[PROP_PUBLISHED] = new Date();
      params[PROP_UPDATED] = params[PROP_PUBLISHED];
      post.addAspect(ASPECT_SYNDICATION, params);
   }
   else
   {
      post.properties[PROP_UPDATED] = new Date();
   }
   post.mimetype = "text/html";
   post.content = content;
   post.save();
   
   // Only set the tags if it is a topic post
   // as we currently don't support individual post tagging
   if (topic != null)
   {
      topic.tags = tags;
   }
}

function main()
{
   // get requested node
   var node = getRequestNode();
   if (status.getCode() != status.STATUS_OK)
   {
      return;
   }
   
   // find the post node if this is a topic node
   var topicNode = null;
   var postNode = null;
   var updatedTopic = false;
   if (node.type == "{http://www.alfresco.org/model/forum/1.0}post")
   {
      postNode = node;
      topicNode = node.parent;
      updatedTopic = false;
   }
   else if (node.type == "{http://www.alfresco.org/model/forum/1.0}topic")
   {
      topicNode = node;
      updatedTopic = true;

      var nodes = getOrderedPosts(node);
      if (nodes.length > 0)
      {
         postNode = nodes[0];
      }
      else
      {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "First post of topic node" + node.nodeRef + " missing");
         return;
      }
   }
   else
   {
      status.setCode(STATUS_BAD_REQUEST, "Incompatible node type. Required either fm:topic or fm:post. Received: " + node.type);
      return;
   }
   
   // update
   updatePost(topicNode, postNode);

   // Due to https://issues.alfresco.com/browse/ALFCOM-1775
   // we have to reuse the search results from before altering the nodes,
   // that's why we don't use the function fetchPostData here (which would
   // do another lucene search in case of a topic post
   if (! updatedTopic)
   {
      model.postData = getReplyPostData(postNode);
      
      // add an activity item
      if (json.has("site") && json.has("page"))
      {
         var topicData = getTopicPostData(model.postData.post.parent);
         var data =
         {
            title: topicData.post.properties.title,
            page: json.get("page") + "?topicId=" + topicData.topic.name,
            params:
            {
               topicId: topicData.topic.name
            }
         }
         activities.postActivity("org.alfresco.discussions.reply-updated", json.get("site"), "discussions", jsonUtils.toJSONString(data));
      }
   }
   else
   {
      // we will do the search here as we have to reuse the lucene result later
      // See above, use getTopicPostDataFromTopicAndPosts instead of getTopicPostData
      //model.topicpost = getTopicPostData(node);
      model.postData = getTopicPostDataFromTopicAndPosts(topicNode, nodes);
      
      // add an activity item
      if (json.has("site") && json.has("page"))
      {
         var topicData = getTopicPostData(model.postData.post.parent);
         var data =
         {
            title: topicData.post.properties.title,
            page: json.get("page") + "?topicId=" + topicData.topic.name
         }
         activities.postActivity("org.alfresco.discussions.post-updated", json.get("site"), "discussions", jsonUtils.toJSONString(data));
      }
   }
}
main();
