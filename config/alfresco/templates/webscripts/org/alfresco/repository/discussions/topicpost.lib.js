
/**
 * Returns the fm:post node given a fm:topic or fm:post node.
 * 
 * This function makes sure that a post is returned in case the passed node is a topic.
 */
function findPostNode(node)
{
   if (node.type == "{http://www.alfresco.org/model/forum/1.0}post")
   {
      return node;
   }
   else if (node.type == "{http://www.alfresco.org/model/forum/1.0}topic")
   {
      var nodes = getOrderedPosts(node);
      if (nodes.length > 0)
      {
         return nodes[0];
      }
      else
      {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "First post of topic node" + node.nodeRef + " missing");
      }
   }
   else
   {
      status.setCode(STATUS_BAD_REQUEST, "Incompatible node type. Required either fm:topic or fm:post. Received: " + node.type);
      return null;
   }
}

 
/** Returns the posts of a topic, ordered by creation date.
 * We use this for two things: To find the root node and the last reply
 */
function getOrderedPosts(topic)
{
   var query = " +TYPE:\"{http://www.alfresco.org/model/forum/1.0}post\"" +
               " +PATH:\"" + topic.qnamePath + "/*\" ";
   var sortAttribute = "@{http://www.alfresco.org/model/content/1.0}published";
   return search.luceneSearch(topic.nodeRef.storeRef.toString(), query, sortAttribute, true) ;
}

/*
 * Returns a JavaScript object that is used by the freemarker template
 * to render a topic post
 */
function getTopicPostData(topicNode)
{  
   // fetch the posts
   var posts = getOrderedPosts(topicNode);
   
   return getTopicPostDataFromTopicAndPosts(topicNode, posts);
}

/*
 * Returns a JavaScript object that is used by the freemarker template
 * to render a topic post
 */
function getTopicPostDataFromTopicAndPosts(topicNode, posts)
{
   // check the first post (which is the main post)
   if (posts.length < 1)
   {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "First post of topic node" + topicNode.nodeRef + " missing");
      return;
   }
   
   var item = {};
   
   // fetch the data
   item.isTopicPost = true;
   item.topic = topicNode;
   item.post = posts[0];
   item.author = people.getPerson(item.post.properties["cm:creator"]);
   item.totalReplyCount = posts.length - 1;
   // in case of replies, find the last reply
   if (posts.length > 1)
   {
      item.lastReply = posts[posts.length - 1];
      item.lastReplyBy = people.getPerson(item.lastReply.properties["cm:creator"]);
   }

   // tags
   if (topicNode.tags != undefined)
   {
       item.tags = topicNode.tags;
   }
   else
   {
       item.tags = [];
   }

   return item;
}

/**
 * Returns the data object that is used by the freemarker template to render a reply post
 */
function getReplyPostData(post)
{
   var item = {};
   item.isTopicPost = false;
   item.post = post;
   item.author = people.getPerson(item.post.properties["cm:creator"]);
   return item;
}
