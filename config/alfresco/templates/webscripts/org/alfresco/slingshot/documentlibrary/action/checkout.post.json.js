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
 * @param p_params {object} standard action parameters: nodeRef, siteId, containerId, path
 * @return {object|null} object representation of action result
 */
function runAction(p_params)
{
   var results;

   try
   {
      var assetNode = p_params.destNode;

      // Ensure the file is versionable
      if (!assetNode.hasAspect("cm:versionable"))
      {
         var props = new Array(1);
         props["cm:autoVersionOnUpdateProps"] = false;
         assetNode.addAspect("cm:versionable", props);
      }
 
      if (assetNode.versionHistory == null)
      {
         // Create the first version manually so we have 1.0 before checkout
         assetNode.createVersion("", true);
      }

      // Checkout the asset
      var workingCopy = assetNode.checkout();
      if (workingCopy === null)
      {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Could not checkout: " + p_params.path);
         return;
      }
      
      // Extra property to allow the full series of actions via the Explorer client
      utils.disableRules();
      workingCopy.properties["cm:workingCopyMode"] = "offlineEditing";
      workingCopy.save();
      utils.enableRules();

      var resultId = assetNode.name,
         resultNodeRef = workingCopy.nodeRef.toString();

      // Construct the result object
      results = [
      {
         id: resultId,
         nodeRef: resultNodeRef,
         downloadUrl: "api/node/content/" + resultNodeRef.replace(":/", "") + "/" + encodeURIComponent(workingCopy.name) + "?a=true",
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

/* Bootstrap action script */
main();
