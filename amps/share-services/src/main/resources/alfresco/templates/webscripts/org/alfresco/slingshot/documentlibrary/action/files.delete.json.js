<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/action.lib.js">

/**
 * Delete multiple files action
 * @method DELETE
 */

/**
 * Entrypoint required by action.lib.js
 *
 * @method runAction
 * @param p_params {object} Object literal containing files array
 * @return {object|null} object representation of action results
 */
function runAction(p_params)
{
   var results = [],
      files = p_params.files,
      linkNodes = [],
      nodes = [],
      file, nodeRef, node;

   // Must have array of files
   if (!files || files.length == 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "No files.");
      return;
   }

   // Loop through nodes, check the existence of each node and create an array of valid ScriptNodes ready for deletion
   for (file in files)
   {
      try
      {
         nodeRef = files[file];
         node = search.findNode(nodeRef);

         // Check if the node actually exists, before deleting it
         // If it doesn't exist mark the deletion of it as failed
         if (node === null)
         {
            results.push(
               {
                  id: file,
                  nodeRef: nodeRef,
                  action: "deleteFile",
                  success: false
               }
            );
            continue;
         }
         else if (node.isLinkToDocument || node.isLinkToContainer)
         {
            linkNodes.push(node);
         }
         else
         {
            nodes.push(node);
         }
      }
      catch (e)
      {
         results.push(
            {
               id: file,
               nodeRef: nodeRef,
               action: "deleteFile",
               success: false
            }
         );
      }
   }

   nodes = linkNodes.concat(nodes);

   // Loop through ScriptNodes and delete them, one by one
   for (var i = 0; i < nodes.length; i++)
   {
      results.push(
         {
            id: nodes[i].name,
            nodeRef: nodes[i].nodeRef.toString(),
            action: "deleteFile",
            type: nodes[i].isContainer ? "folder" : "document",
            success: nodes[i].remove(true)
         }
      );
   }

   return results;
}

/* Bootstrap action script */
main();
