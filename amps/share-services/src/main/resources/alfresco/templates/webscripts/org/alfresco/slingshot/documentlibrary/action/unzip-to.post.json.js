<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/action.lib.js">

/**
 * Unzip files action
 * @method POST
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
      destNode = p_params.destNode,
      files = p_params.files,
      parent = null,
      file, fileNode, result, nodeRef,
      fromSite;

   // Must have array of files
   if (!files || files.length == 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "No files.");
      return;
   }
   
   for (file in files)
   {
      nodeRef = files[file];
      result =
      {
         nodeRef: nodeRef,
         action: "unzipFile",
         success: false
      }
      
      try
      {
         fileNode = search.findNode(nodeRef);
         if (fileNode == null)
         {
            result.id = file;
            result.nodeRef = nodeRef;
            result.success = false;
         }
         else
         {
            if (p_params.parent && p_params.parent != null)
            {
               parent = search.findNode(p_params.parent);
            }
            result.id = fileNode.name;
            result.type = fileNode.isContainer ? "folder" : "document";
            
            // Retain the name of the site the node is currently in. Null if it's not in a site.
            fromSite = fileNode.siteShortName;
            
            // unzip the node
            var unzipAction = actions.create("import");
            unzipAction.parameters.destination = destNode;
            unzipAction.execute(fileNode);
            result.success = true;
         }
      }
      catch (e)
      {
         result.id = file;
         result.nodeRef = nodeRef;
         result.success = false;

         //MNT-7514 Uninformational error message on move when file name conflicts
         result.fileExist = false;
         
         error = e.toString();
         if (error.indexOf("FileExistsException") != -1)
         {
            result.fileExist = true;
         }
      }
      
      results.push(result);
   }

   return results;
}

/* Bootstrap action script */
main();
