<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/action.lib.js">

/**
 * Create folder action
 * @method POST
 * @param uri {string} /{siteId}/{containerId}/{filepath}
 * @param json.name {string} New folder name
 * @param json.title {string} Title metadata
 * @param json.description {string} Description metadata
 */

/**
 * Entrypoint required by action.lib.js
 *
 * @method runAction
 * @param p_params {object} common parameters
 * @return {object|null} object representation of action result
 */
function runAction(p_params)
{
   var results;
   
   try
   {
      // Mandatory: json.name
      if (json.isNull("name"))
      {
         status.setCode(status.STATUS_BAD_REQUEST, "Folder name is a mandatory parameter.");
         return;
      }
      var folderName = json.get("name");
      
      var parentPath = p_params.path;
      var folderPath = parentPath + "/" + folderName;

      // Check folder doesn't already exist
      var existsNode = getAssetNode(p_params.rootNode, folderPath);
      if (typeof existsNode == "object")
      {
         status.setCode(status.STATUS_BAD_REQUEST, "Folder '" + folderPath + "' already exists.");
         return;
      }

      // Check parent exists
      var parentNode = getAssetNode(p_params.rootNode, parentPath);
      if (typeof parentNode == "string")
      {
         status.setCode(status.STATUS_NOT_FOUND, "Parent folder '" + parentPath + "' not found.");
         return;
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
         id: folderPath,
         name: folderName,
         parentPath: parentPath,
         nodeRef: folderNode.nodeRef.toString(),
         action: "createFolder",
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
