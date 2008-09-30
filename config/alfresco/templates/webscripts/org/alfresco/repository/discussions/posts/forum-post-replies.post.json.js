<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/nodenameutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">

const ASPECT_SYNDICATION = "cm:syndication";
const PROP_PUBLISHED = "cm:published";

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
   
   // add the cm:syndication aspect
   var props = new Array();
   props[PROP_PUBLISHED] = new Date();
   postNode.addAspect(ASPECT_SYNDICATION, props);
   
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
   if (json.has("site") && json.has("page"))
   {
      // fetch the topic (and with it the root post
      var topicData = getTopicPostData(model.postData.post.parent);
      var data =
      {
         title: topicData.post.properties.title,
         page: json.get("page") + "?topicId=" + topicData.topic.name
      }
      activities.postActivity("org.alfresco.discussions.reply-created", json.get("site"), "discussions", jsonUtils.toJSONString(data));
   }
}

main();
