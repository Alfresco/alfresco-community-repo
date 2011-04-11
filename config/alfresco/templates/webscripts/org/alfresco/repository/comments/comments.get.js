<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/generic-paged-results.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/comments/comments.lib.js">

/**
 * Get all comments for a node
 */
function getCommentsList(node, index, count, reverse)
{
   var nodes = getComments(node);
   var result = getPagedResultsData(nodes, index, count, getCommentData);

   if (reverse == "true" && result.items.length > 0)
   {
      // Don't want to mutate the result.items array. Therefore we use slice(0) to get a (shallow) copy of it.
      result.items = result.items.slice(0).reverse();
   }
   return result;
}

function main()
{
   // get requested node
   var node = getRequestNode();
   if (status.getCode() != status.STATUS_OK)
   {
      return;
   }

   // process additional parameters
   var reverse = args["reverse"] != undefined ? args["reverse"] : "false";
   var index = args["startIndex"] != undefined ? parseInt(args["startIndex"]) : 0;
   var count = args["pageSize"] != undefined ? parseInt(args["pageSize"]) : 10;

   model.data = getCommentsList(node, index, count, reverse);
   model.node = node;
}

main();
