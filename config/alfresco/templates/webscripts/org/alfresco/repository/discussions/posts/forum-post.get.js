<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">

/*
 * Fetches the correct post data (either topic post or post depending on the node type
 */
function fetchPostData(node)
{
   // we have to differentiate here whether this is a top-level post or a reply
   // only in the first case we fetch all information (as returned by forum/.../posts)
   if (node.type == "{http://www.alfresco.org/model/forum/1.0}topic")
   {
      model.postData = getTopicPostData(node);
   }
   else if (node.type == "{http://www.alfresco.org/model/forum/1.0}post")
   {
      model.postData = getReplyPostData(node);
   }
   else
   {
      status.setCode(STATUS_BAD_REQUEST, "Incompatible node type. Required either fm:topic or fm:post. Received: " + node.type);
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
   
   fetchPostData(node);
   
   // fetch the contentLength param
   var contentLength = args["contentLength"] != undefined ? parseInt(args["contentLength"]) : -1;
   model.contentLength = isNaN(contentLength) ? -1 : contentLength;
}

main();
