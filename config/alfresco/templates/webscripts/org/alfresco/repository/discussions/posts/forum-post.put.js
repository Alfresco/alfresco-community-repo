<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">

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
   post.properties.title = title;
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
   if (node.type == "{http://www.alfresco.org/model/forum/1.0}post")
   {
      postNode = node;
   }
   else if (node.type == "{http://www.alfresco.org/model/forum/1.0}topic")
   {
      topicNode = node;
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
   if (topicNode == null)
   {
      model.post = node;
      
      // add an activity item
      if (json.has("site"))
      {
         // fetch the topic (and with it the root post
         var topicData = getTopicPostData(model.post.parent);       
         var site = json.get("site");
         var container = json.get("container");
         var path = json.has("path") ? json.get("path") : '';
         var browseTopicUrl = '/page/site/' + site + '/discussions-topicview?container=' + container +
                          + '&path=' + path + '&postId=' +  topicData.topic.name;
         var data = {
            title: topicData.post.properties.title,
            browseTopicUrl: browseTopicUrl
         }
         activities.postActivity("org.alfresco.discussions.reply-updated", site, container, jsonUtils.toJSONString(data));
      }
   }
   else
   {
      // we will do the search here as we have to reuse the lucene result later
      // See above, use getTopicPostDataFromTopicAndPosts instead of getTopicPostData
      //model.topicpost = getTopicPostData(node);
      model.topicpost = getTopicPostDataFromTopicAndPosts(topicNode, nodes);
      
      // post an activitiy item, but only if we got a site
      if (url.templateArgs.site != null)
      {
         var browseTopicUrl = '/page/site/' + url.templateArgs.site + '/discussions-topicview?container=' + url.templateArgs.container +
                             + '&path=' + url.templateArgs.path + '&postId=' + model.topicpost.topic.name;
         var data = {
            title: model.topicpost.post.properties.title,
            browseTopicUrl: browseTopicUrl
         }
         activities.postActivity("org.alfresco.discussions.post-updated", url.templateArgs.site, url.templateArgs.container, jsonUtils.toJSONString(data));
      }
   }
}

main();
