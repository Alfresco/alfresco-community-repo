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
 * @param p_params {object} standard action parameters: nodeRef, siteId, containerId, path
 * @return {object|null} object representation of action result
 */
function runAction(p_params)
{
   var results;
   
   try
   {
      var assetNode = p_params.destNode,
         resultId = assetNode.name,
         resultNodeRef = assetNode.nodeRef.toString();

      // Delete the asset
      if (!assetNode.remove())
      {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Could not delete.");
         return;
      }

      // Construct the result object
      results = [
      {
         id: resultId,
         nodeRef: resultNodeRef,
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

/* Bootstrap action script */
main();
