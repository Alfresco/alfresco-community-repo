<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/generic-paged-results.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/comments/comments.lib.js">

/**
 * Get all comments for a node
 */
function getCommentsList(node, index, count, reverse)
{
   // Get all the comments on the node. These should be in date order,
   //  oldest to newest, as they're in repo order
   var nodes = getComments(node);

   // If they want newest first, sort that before we page it
   if (reverse == "true" && nodes.length > 0)
   {
      // Don't want to mutate the result.items array. Therefore we use slice(0) to get a (shallow) copy of it.
      nodes = nodes.slice(0).reverse();
   }

   // Now do any paging that's required
   var result = getPagedResultsData(nodes, index, count, getCommentData);

   // All done!
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
