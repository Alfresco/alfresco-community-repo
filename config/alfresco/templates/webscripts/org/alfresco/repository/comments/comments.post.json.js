<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/nodenameutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/comments/comments.lib.js">

/**
 * Creates a post inside the passed forum node.
 */
function addComment(node)
{
   // fetch the data required to create a comment
   var title = "";
   if (json.has("title"))
   {
      title = json.get("title");
   }
   var content = json.get("content");

   // fetch the parent to add the node to
   var commentsFolder = getOrCreateCommentsFolder(node);

   // get a unique name
   var name = getUniqueChildName(commentsFolder, "comment");
   
   // create the comment
   var commentNode = commentsFolder.createNode(name, "fm:post");
   commentNode.mimetype = "text/html";
   commentNode.properties.title = title;
   commentNode.content = content;
   commentNode.save();
    
   return commentNode;
}

function main()
{
   // get requested node
   var node = getRequestNode();
   if (status.getCode() != status.STATUS_OK)
   {
      return;
   }

   var comment = addComment(node);
   model.item = getCommentData(comment);
   model.node = node;
   
   // post an activity item, but only if we've got a site
   if (json.has("site") && json.has("itemTitle") && json.has("page"))
   {
      var siteId = json.get("site");
      if ((siteId != null) && (siteId != ""))
      {
         var params = jsonUtils.toObject(json.get("pageParams")), strParams = "";
         for (param in params)
         {
            strParams += param + "=" + encodeURIComponent(params[param]) + "&";
         }
         var data =
         {
            title: json.get("itemTitle"),
            page: json.get("page") + (strParams != "" ? "?" + strParams.substring(0, strParams.length - 1) : ""),
            nodeRef: node.getNodeRef()
         }
         activities.postActivity("org.alfresco.comments.comment-created", siteId, "comments", jsonUtils.toJSONString(data));
      }
   }
}

main();
