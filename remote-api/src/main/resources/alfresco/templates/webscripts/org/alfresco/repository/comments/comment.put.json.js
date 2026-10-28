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

   // Validate and sanitize comment content to prevent XSS (same policy as CommentsPost.java)
   if (content == null || String(content).replace(/\s/g, "") === "")
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Comment content must not be empty");
      return;
   }

   // Allowed HTML elements and attributes in comment content e.g. Text formatting, Lists and Structure & Styling
   var allowedElements = ["b", "i", "u", "strong", "em", "ul", "ol", "li", "p", "br", "span", "div"];

   var policy = new Packages.org.owasp.html.HtmlPolicyBuilder()
         .allowElements(allowedElements)
         .allowAttributes("style").onElements("span", "div", "p", "ul")
         .allowStyling()
         .allowStandardUrlProtocols()
         .toFactory();

   content = String(policy.sanitize(content));

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
   
   // MNT-16446 (pending future ACE-5437)
   // TODO: commentedOn node is locked or pwc
   var commentData = getCommentData(node);
   var canEdit = commentData.canEditComment;
   if (! canEdit)
   {
      status.setCode(status.STATUS_FORBIDDEN, "Cannot edit comment");
      return null;
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
