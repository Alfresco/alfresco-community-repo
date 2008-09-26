<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/action.lib.js">

/**
 * Cancel checkout file action
 * @method POST
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
      var assetNode = getAssetNode(p_params.rootNode, p_params.path);

      // Must have assetNode by this point
      if (typeof assetNode == "string")
      {
         status.setCode(status.STATUS_NOT_FOUND, "Not found: " + p_params.path);
         return;
      }

      // Checkin the asset
      var originalDoc = assetNode.cancelCheckout();
      if (originalDoc === null)
      {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Could not cancel checkout: " + p_params.path);
         return;
      }

      var resultId = originalDoc.name;
      var resultNodeRef = originalDoc.nodeRef.toString();

      // Construct the result object
      results = [
      {
         id: resultId,
         nodeRef: resultNodeRef,
         action: "cancelCheckoutAsset",
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
