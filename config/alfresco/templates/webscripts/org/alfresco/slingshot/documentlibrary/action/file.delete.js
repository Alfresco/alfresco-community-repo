<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/action.lib.js">

/**
 * Delete file action
 * @method DELETE
 * @param uri {string} /{siteId}/{containerId}/{filepath}
 */

/**
 * Entrypoint required by action.lib.js
 *
 * @method runAction
 * @param p_rootNode {NodeRef} root node within which to perform the action
 * @param p_params {object} common parameters
 * @return {object|null} object representation of action result
 */
function runAction(p_rootNode, p_params)
{
   var results =
   {
      status: {}
   };
   
   try
   {
      var assetNode = getAssetNode(p_rootNode, p_params.filePath);

      // Must have assetNode by this point
      if (typeof assetNode == "string")
      {
         status.setCode(status.STATUS_NOT_FOUND, "Not found.");
         return;
      }
      
      var rId = assetNode.name;
      var rNodeRef = assetNode.nodeRef.toString();

      // Delete the asset
      if (!assetNode.remove())
      {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Could not delete.");
         return;
      }

      // Construct the result object
      results = [
      {
         id: rId,
         nodeRef: rNodeRef,
         action: "deleteFile",
         success: true
      }];
   }
   catch(e)
   {
		status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, e.toString());
		return;
   }
   
   return results;
}
