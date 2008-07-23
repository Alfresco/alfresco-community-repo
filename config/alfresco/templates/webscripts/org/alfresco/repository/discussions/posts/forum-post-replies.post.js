<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/nodenameutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">

/**
 * Creates a post inside the passed forum node.
 */
function createPostReplyImpl(topicNode, parentPostNode)
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

   // create the post node using a unique name
   var name = getUniqueChildName(topicNode, "post");
   var postNode = topicNode.createNode(name, "fm:post");
   postNode.mimetype = "text/html";
   postNode.properties.title = title;
   postNode.content = content;
   postNode.save();
   
   // link it to the parent post
   postNode.addAspect("cm:referencing");
   postNode.createAssociation(parentPostNode, "cm:references");
   postNode.save(); // probably not necessary here

   return getReplyPostData(postNode);
}

/**
 * Creates a reply to a post.
 * @param node The parent post node
 */
function createPostReply(node)
{
   // we have to differentiate here whether this is a top-level post or a reply
   if (node.type == "{http://www.alfresco.org/model/forum/1.0}topic")
   {
      // find the primary post node
      var topic = node;
      var post = findPostNode(node);
      return createPostReplyImpl(topic, post);
   }
   else if (node.type == "{http://www.alfresco.org/model/forum/1.0}post")
   {
      // the forum is the parent of the node
      var topic = node.parent;
      var post = node;
      return createPostReplyImpl(topic, post);
   }
   else
   {
      status.setCode(STATUS_BAD_REQUEST, "Incompatible node type. Required either fm:topic or fm:post. Received: " + node.type);
      return null;
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
   
   model.postData = createPostReply(node);
   
   // add an activity item
   if (json.has("site") && json.has("container") && json.has("browseTopicUrl"))
   {
      // fetch the topic (and with it the root post
      var topicData = getTopicPostData(model.postData.post.parent);
      var browseTopicUrl = json.get("browseTopicUrl");
      browseTopicUrl = browseTopicUrl.replace("{post.name}", topicData.topic.name);
      var data = {
         topicTitle: topicData.post.properties.title,
         browseTopicUrl: browseTopicUrl
      }
      activities.postActivity("org.alfresco.discussions.reply-created", json.get("site"), json.get("container"), jsonUtils.toJSONString(data));
   }
}

main();
