<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/comments/comments.lib.js">

/**
 * Update a comment
 */
function updateComment(node)
{
   var title = "";
   if (json.has("title"))
   {
      title = json.get("title");
   }
   var content = json.get("content");
   
   // update the topic title
   node.properties.title = title;
   node.mimetype = "text/html";
   node.content = content;
   node.save();
}

function main()
{
   // get requested node
   var node = getRequestNode();
   if (status.getCode() != status.STATUS_OK)
   {
      return;
   }

   // update comment
   updateComment(node);
   
   model.item = getCommentData(node);
}

main();
