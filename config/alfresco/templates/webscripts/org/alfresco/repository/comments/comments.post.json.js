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
   
   // post an activitiy item, but only if we got a site
   if (json.has("site") &&
       json.has("container") &&
       json.has("itemTitle") &&
       json.has("browseItemUrl"))
   {
      var data = {
          itemTitle: json.get("itemTitle"),
          browseItemUrl: json.get("browseItemUrl")
      }
      activities.postActivity("org.alfresco.comments.comment-created", json.get("site"), json.get("container"), jsonUtils.toJSONString(data));
   }
}

main();
