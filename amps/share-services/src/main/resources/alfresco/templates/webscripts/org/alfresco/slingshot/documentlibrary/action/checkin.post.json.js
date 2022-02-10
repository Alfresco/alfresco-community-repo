<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/action.lib.js">

/**
 * Checkin file action
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
      var assetNode = p_params.destNode,
         originalDoc = assetNode.checkin();
      if (originalDoc === null)
      {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Could not checkin: " + p_params.path);
         return;
      }

      var resultId = originalDoc.name,
         resultNodeRef = originalDoc.nodeRef.toString();

      // Construct the result object
      results = [
      {
         id: resultId,
         nodeRef: resultNodeRef,
         action: "checkinAsset",
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
