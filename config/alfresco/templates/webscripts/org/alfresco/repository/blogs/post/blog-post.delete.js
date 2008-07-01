<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">

/**
 * Deletes a blog post node.
 */
function deleteBlogPost(postNode)
{
   // delete the node
   var nodeRef = postNode.nodeRef;
   var isDeleted = postNode.remove();
   if (! isDeleted)
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

   deleteBlogPost(node);
}

main();
