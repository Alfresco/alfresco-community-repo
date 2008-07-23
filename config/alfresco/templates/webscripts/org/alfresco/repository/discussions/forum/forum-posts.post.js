<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/nodenameutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">

function ensureTagScope(node)
{
   if (! node.isTagScope)
   {
      node.isTagScope = true;
   }
   
   // also check the parent (the site!)
   if (! node.parent.isTagScope)
   {
      node.parent.isTagScope = true;
   }
}

/**
 * Adds a post to the passed forum node.
 */
function createPost(forumNode)
{
   // fetch the data required to create a topic
   var title = "";
   if (json.has("title"))
   {
      title = json.get("title");
   }
   var content = "";
   if (json.has("title"))
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
   
   // create the topic node, and add the first child node representing the topic text
   // NOTE: this is a change from the old web client, where the topic title was used as name
   //       for the topic node. We will use generated names to make sure we won't have naming
   //       clashes.
   var name = getUniqueChildName(forumNode, "post");
   var topicNode = forumNode.createNode(name, "fm:topic");

   // We use twice the same name for the topic and the post in it
   var contentNode = topicNode.createNode(name, "fm:post");
   contentNode.mimetype = "text/html";
   contentNode.properties.title = title;
   contentNode.content = content;
   contentNode.save();

   // add the tags to the topic node for now
   topicNode.tags = tags;

   return topicNode;
}

function main()
{
   // get requested node
   var node = getRequestNode();
   if (status.getCode() != status.STATUS_OK)
   {
      return;
   }

   ensureTagScope(node);

   var topicPost = createPost(node);
   
   model.postData = getTopicPostData(topicPost);
   
   // post an activitiy item, but only if we got a site
   if (url.templateArgs.site != null)
   {
      var browseTopicUrl = '/share/page/site/' + url.templateArgs.site + '/discussions-postview?container=' + url.templateArgs.container +
                           + '&path=' + url.templateArgs.path + '&postId=' + topicPost.name;
      var data = {
          title: model.postData.post.properties.title,
          browseTopicUrl: browseTopicUrl
      }
      activities.postActivity("org.alfresco.discussions.post-created", url.templateArgs.site, url.templateArgs.container, jsonUtils.toJSONString(data));
   }
   
}

main();
