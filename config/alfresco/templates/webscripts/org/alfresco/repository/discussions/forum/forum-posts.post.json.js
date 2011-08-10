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
   
   // create the topic node, and add the first child node representing the topic text
   // NOTE: this is a change from the old web client, where the topic title was used as name
   //       for the topic node. We will use generated names to make sure we won't have naming
   //       clashes.
   var name = getUniqueChildName(forumNode, "post");
   var topicNode = forumNode.createNode(name, "fm:topic");
   topicNode.properties.title = title;
   topicNode.save();

   // We use twice the same name for the topic and the post in it
   var contentNode = topicNode.createNode(name, "fm:post");
   contentNode.mimetype = "text/html";
   contentNode.properties.title = title;
   contentNode.content = content;
   contentNode.save();

   // add the cm:syndication aspect
   var props = new Array();
   props["cm:published"] = new Date();
   contentNode.addAspect("cm:syndication", props);

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
   
   // create an activity entry
   if (json.has("site") && json.has("page"))
   {
      var data =
      {
         title: model.postData.post.properties.title,
         page: json.get("page") + "?topicId=" + model.postData.topic.name
      }
      activities.postActivity("org.alfresco.discussions.post-created", json.get("site"), "discussions", jsonUtils.toJSONString(data));
   }   
}

main();
