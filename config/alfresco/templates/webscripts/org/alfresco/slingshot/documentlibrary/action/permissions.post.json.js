<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/action.lib.js">

const VALID_OPERATIONS =
{
   "set": true,
   "reset-all": true
};

/**
 * Set Permissions on single/multiple files
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
      files = p_params.files,
      i, j, file, fileNode, nodeRef;

   // Must have array of files
   if (!files || (files.length == 0))
   {
      status.setCode(status.STATUS_BAD_REQUEST, "No files.");
      return;
   }

   // Which permissions operation?
   var operation = url.templateArgs.operation;
   if (!operation || !(operation in VALID_OPERATIONS))
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Invalid or missing operation ('" + operation + "').");
      return;
   }
   
   // Permissions to set
   var jsonPermissions = getMultipleInputValues("permissions");

   // We need the site node to perform some of the operations
   var site = p_params.siteNode;
   
   // Set permissions on each file
   for (file in files)
   {
      nodeRef = files[file];
      result =
      {
         nodeRef: nodeRef,
         action: operation,
         success: false
      }
      
      try
      {
         fileNode = search.findNode(nodeRef);
         if (fileNode === null)
         {
            result.id = file;
            result.nodeRef = nodeRef;
            result.success = false;
         }
         else
         {
            result.id = fileNode.name;
            result.type = fileNode.isContainer ? "folder" : "document";
            
            // Execute the operation
            switch (operation)
            {
               case "set":
                  if (typeof jsonPermissions == "string")
                  {
                     status.setCode(status.STATUS_BAD_REQUEST, "Invalid or missing permissions set.");
                     return;
                  }
                  // Convert permissions from JSONArray
                  var permissions = [];
                  for (var i = 0, j = jsonPermissions.length; i < j; i++)
                  {
                     permissions[jsonPermissions[i].get("group")] = String(jsonPermissions[i].get("role"));
                  }
                  site.setPermissions(fileNode, permissions);
                  break;
                  
               case "reset-all":
                  site.resetAllPermissions(fileNode);   
                  break;
            }
            result.success = true;
         }
      }
      catch (e)
      {
         result.id = file;
         result.nodeRef = nodeRef;
         result.success = false;
      }
      
      results.push(result);
   }
   
   return results;
}

/* Bootstrap action script */
main();