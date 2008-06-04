<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/action.lib.js">

/**
 * Create folder action
 * @method POST
 * @param uri {string} /{siteId}/{componentId}/{filepath}
 * @param json.name {string} New folder name
 * @param json.title {string} Title metadata
 * @param json.description {string} Description metadata
 */

/**
 * Entrypoint required by action.lib.js
 *
 * @method runAction
 * @param p_rootNode {NodeRef} root node within which to perform the action
 * @param p_params {object} common parameters
 * @return {object|null} object representation of action result
 */
function runAction(p_rootNode, p_params)
{
   var results =
   {
      status: {}
   };
   
   try
   {
      // Mandatory: json.name
      if (json.isNull("name"))
      {
         results.status.code = status.STATUS_BAD_REQUEST;
         results.status.message = "Folder name is a mandatory parameter.";
         return results;
      }
      var folderName = json.get("name");
      
      // Fix-up parent path to have no leading or trailing slashes
      var parentPath = p_params.filePath;
      if (parentPath.length > 0)
      {
         var aPaths = parentPath.split("/");
         while (aPaths[0] === "")
         {
            aPaths.shift();
         }
         while (aPaths[aPaths.length-1] === "")
         {
            aPaths.pop();
         }
         parentPath = aPaths.join("/") + "/";
      }
      var folderPath = parentPath + folderName;

      // Check folder doesn't already exist
      var existsNode = getAssetNode(p_rootNode, folderPath);
      if (typeof existsNode == "object")
      {
         results.status.code = status.STATUS_BAD_REQUEST;
         results.status.message = "Folder '" + folderPath + "' already exists.";
         return results;
      }

      // Check parent exists
      var parentNode = getAssetNode(p_rootNode, parentPath);
      if (typeof parentNode == "string")
      {
         results.status.code = status.STATUS_NOT_FOUND;
         results.status.message = "Parent folder '" + parentPath + "' not found.";
         return results;
      }
      
      // Title and description
      var folderTitle = "";
      var folderDescription = "";
      if (!json.isNull("title"))
      {
         folderTitle = json.get("title");
      }
      if (!json.isNull("description"))
      {
         folderDescription = json.get("description");
      }

      // Create the folder and apply metadata
      var folderNode = parentNode.createFolder(folderName);
      // Always add title & description, default icon
      folderNode.properties["cm:title"] = folderTitle;
      folderNode.properties["cm:description"] = folderDescription.substr(0, 100);
      folderNode.properties["app:icon"] = "space-icon-default";
      folderNode.save();
      // Add uifacets aspect for the web client
      folderNode.addAspect("app:uifacets");
      
      // Construct the result object
      results = [
      {
         id: folderName,
         action: "createFolder",
         success: true
      }];
   }
   catch(e)
   {
		results.status.code = status.STATUS_INTERNAL_SERVER_ERROR;
		results.status.message = e.toString();
   }
   
   return results;
}
