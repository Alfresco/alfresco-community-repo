<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/action.lib.js">

/**
 * Delete file action
 * @method DELETE
 * @param uri {string} /{siteId}/{componentId}/{filepath}
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
      results.status.redirect = false;
      var assetNode = getAssetNode(p_rootNode, p_params.filePath);

      // Must have assetNode by this point
      if (typeof assetNode == "string")
      {
         results.status.code = status.STATUS_NOT_FOUND;
         return results;
      }

      // Delete the asset
      if (!assetNode.remove())
      {
         results.status.code = status.STATUS_INTERNAL_SERVER_ERROR;
         return results;
      }

      results.status.code = status.STATUS_NO_CONTENT;
   }
   catch(e)
   {
		results.status.code = status.STATUS_INTERNAL_SERVER_ERROR;
   }
   
   return results;
}
