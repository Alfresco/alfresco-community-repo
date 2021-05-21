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
   var results, resultId, resultNodeRef;

   try
   {
      // Initialise to the destNode (will be updated to the original if a working copy)...
      var originalDoc = p_params.destNode;
      resultId = originalDoc.name;
      resultNodeRef = originalDoc.nodeRef.toString();

      if (p_params.destNode.hasAspect("cm:workingcopy"))
      {
         // If the node is a working copy then cancel the checkout and set the original...
         var checkedoutNode = p_params.destNode.getCheckedOut();
         if (checkedoutNode === null)
         {
            status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Could not find original node: " + url.extension);
            return;
         }

         if (checkedoutNode.hasAspect("cm:cmisCreatedCheckedOut"))
         {
            var parent = checkedoutNode.parent;
            resultId = parent.name;
            resultNodeRef = parent.nodeRef.toString();
         }
         else
         {
            resultId = checkedoutNode.name;
            resultNodeRef = checkedoutNode.nodeRef.toString();
         }

         originalDoc = p_params.destNode.cancelCheckout();
         if (originalDoc === null)
         {
            status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Could not cancel checkout: " + url.extension);
            return;
         }
      }
      else if (p_params.destNode.isLocked && !p_params.destNode.hasAspect("trx:transferred"))
      {
         var assocs = p_params.destNode.getAssocs();
         if (assocs["{http://www.alfresco.org/model/content/1.0}workingcopylink"] !==null && assocs["{http://www.alfresco.org/model/content/1.0}workingcopylink"][0])
         {
            // original document: edit offline case
            originalDoc = assocs["{http://www.alfresco.org/model/content/1.0}workingcopylink"][0].cancelCheckout();

            resultId = originalDoc.name;
            resultNodeRef = originalDoc.nodeRef.toString();
         }
         else
         {
            // original document: edit online case
            // ...or, if the node is locked then just unlock it...
            p_params.destNode.unlock();
         }
      }

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
