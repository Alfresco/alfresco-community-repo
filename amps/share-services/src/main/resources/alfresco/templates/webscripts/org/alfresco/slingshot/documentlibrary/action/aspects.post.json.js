<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/action.lib.js">

/**
 * Add / Remove Aspects action
 * @method POST
 */

function jsonToArray(p_name)
{
   var array = [];
   
   if (json.has(p_name))
   {
      var jsonArray = json.get(p_name);
      for (var i = 0, ii = jsonArray.length(); i < ii; i++)
      {
         array.push(jsonArray.get(i));
      }
   }
   
   return array;
}


/**
 * Entrypoint required by action.lib.js
 *
 * @method runAction
 * @param p_params {object} Object literal containing files array
 * @return {object|null} object representation of action results
 */
function runAction(p_params)
{
   var result,
      assetNode = p_params.destNode;

   try
   {
      result =
      {
         nodeRef: assetNode.nodeRef.toString(),
         action: "manageAspects",
         success: false
      }

      result.id = assetNode.name;
      result.type = assetNode.isContainer ? "folder" : "document";

      var added = jsonToArray("added"),
         removed = jsonToArray("removed"),
         isTaggable = false,
         i, ii;

      // Aspects to be removed
      for (i = 0, ii = removed.length; i < ii; i++)
      {
         if (assetNode.hasAspect(removed[i]))
         {
            assetNode.removeAspect(removed[i]);
            isTaggable = isTaggable || (removed[i] == "cm:taggable");
         }
      }

      // Aspects to be added
      for (i = 0, ii = added.length; i < ii; i++)
      {
         if (!assetNode.hasAspect(added[i]))
         {
            assetNode.addAspect(added[i]);
            isTaggable = isTaggable || (added[i] == "cm:taggable");
         }
      }

      // Update the tag scope? This would be nice, but will be quite slow & synchronous.
      // We'll send back the flag anyway and the client can fire off a REST API call to do it.
      if (isTaggable)
      {
         // assetNode.tagScope.refresh();
         result.tagScope = true;
      }

      result.success = true;
   }
   catch (e)
   {
      result.success = false;
   }
   
   return [result];
}

/* Bootstrap action script */
main();
