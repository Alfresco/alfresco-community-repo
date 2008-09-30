<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">

const DELETED_REPLY_POST_MARKER = "[[deleted]]";

/**
 * Deletes a topic node.
 */
function deleteTopicPost(topicNode)
{
   // fetch the topic info as we need to get the title for the post for the activity
   var data = getTopicPostData(topicNode);
   var topicTitle = data.post.properties.title;
    
   // we simply delete the complete topic
   var nodeRef = topicNode.nodeRef;
   var isDeleted = topicNode.remove();
   if (! isDeleted)
   {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Unable to delete node: " + nodeRef);
      return;
   }
   model.message = "Node " + nodeRef + " deleted";
   
   // post an activitiy item, but only if we got a site
   if (url.templateArgs["site"] != undefined && args["page"] != undefined)
   {
      var data =
      {
         title: topicTitle,
         page: decodeURIComponent(args["page"])
      }
      activities.postActivity("org.alfresco.discussions.post-deleted", url.templateArgs["site"], "discussions", jsonUtils.toJSONString(data));
   }
}
 
/**
 * Delete a reply post.
 * Note: Because posts are recursive, we can't simply delete the node.
 *       For now we set a marker text [[delete]] as title and content.
 */
function deleteReplyPost(node)
{
   var title = DELETED_REPLY_POST_MARKER;
   var content = DELETED_REPLY_POST_MARKER;
   
   // update the topic title
   node.properties.title = title;
   node.mimetype = "text/html";
   node.content = content;
   node.save();
   
   model.message = "Node " + node.nodeRef + " marked as removed";
}
 
/**
 * Deletes a post.
 * Note: this function also deletes all replies of the post
 */
function deletePost(node)
{
   // simple case: topic post
   if (node.type == "{http://www.alfresco.org/model/forum/1.0}topic")
   {
      deleteTopicPost(node);
   }
   else if (node.type == "{http://www.alfresco.org/model/forum/1.0}post")
   {
      deleteReplyPost(node);
   }
   else
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Node is not of type fm:topic or fm:post");
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

   deletePost(node);
}

main();
