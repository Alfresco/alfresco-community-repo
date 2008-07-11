<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/action.lib.js">

/**
 * Checkout file action
 * @method POST
 * @param uri {string} /{siteId}/{containerId}/{filepath}
 * @param json.destination {string} Optional path to checkout to
 */

/**
 * Entrypoint required by action.lib.js
 *
 * @method runAction
 * @param p_params {object} standard action parameters: nodeRef, siteId, containerId, filePath
 * @return {object|null} object representation of action result
 */
function runAction(p_params)
{
   var results;

   try
   {
      var assetNode = getAssetNode(p_params.rootNode, p_params.filePath);

      // Must have assetNode by this point
      if (typeof assetNode == "string")
      {
         status.setCode(status.STATUS_NOT_FOUND, "Not found: " + p_params.filePath);
         return;
      }

      // Checkout the asset
      var workingCopy = assetNode.checkout();
      if (workingCopy === null)
      {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Could not checkout: " + p_params.filePath);
         return;
      }

      var resultId = assetNode.name;
      var resultNodeRef = workingCopy.nodeRef.toString();

      // Construct the result object
      results = [
      {
         id: resultId,
         nodeRef: resultNodeRef,
         downloadUrl: "api/node/content/" + resultNodeRef.replace(":/", "") + "/" + workingCopy.name + "?a=true",
         action: "checkoutAsset",
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
