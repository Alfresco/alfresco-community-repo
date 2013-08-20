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
   model.node = node;
   
   // post an activity item, but only if we got a site
   if (json.has("site") && json.has("itemTitle") && json.has("page"))
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

      activities.postActivity("org.alfresco.comments.comment-updated", json.get("site"), "comments", jsonUtils.toJSONString(data));
   }
}

main();
