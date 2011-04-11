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
      // Checkin the asset
      var originalDoc = p_params.destNode.cancelCheckout();
      if (originalDoc === null)
      {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Could not cancel checkout: " + url.extension);
         return;
      }

      var resultId = originalDoc.name,
         resultNodeRef = originalDoc.nodeRef.toString();

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
      e.code = status.STATUS_INTERNAL_SERVER_ERROR;
      e.message = e.toString();      
      throw e;
   }

   return results;
}

/* Bootstrap action script */
main();
