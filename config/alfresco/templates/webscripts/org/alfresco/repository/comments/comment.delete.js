<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/comments/comments.lib.js">

/**
 * Delete a comment.
 */
function deleteComment(node)
{
   // we simply delete the topic
   var nodeRef = node.nodeRef;
   var isDeleted = node.remove();
   if (!isDeleted)
   {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Unable to delete node: " + nodeRef);
      return;
   }
   
   model.message = "Node " + nodeRef + " deleted";
}

function main()
{
   // get requested node
   var node = getRequestNode();
   if (status.getCode() != status.STATUS_OK)
   {
      return;
   }

   deleteComment(node);
   
   // post an activity item, but only if we got a site
   if ((args["site"] != undefined) && (args["itemTitle"] != undefined && (args["page"] != undefined)))
   {
      var params = jsonUtils.toObject(decodeURIComponent(args["pageParams"])), strParams = "";
      for (param in params)
      {
         strParams += param + "=" + encodeURIComponent(params[param]) + "&";
      }
      var data =
      {
         title: args["itemTitle"],
         page: args["page"] + (strParams != "" ? "?" + strParams.substring(0, strParams.length - 1) : "")
      }
      activities.postActivity("org.alfresco.comments.comment-deleted", args["site"], "comments", jsonUtils.toJSONString(data));
   }
}

main();
