/**
 * Document List Component: action
 * for createFolder: name, (title), (description)
 *
 * @param path {string} Folder relative to root store
 * @param name {string} File or folder name involved in the action
 */
var node = getRootNode();
if (node !== null)
{
   var results = runAction(node);
   if (results !== null)
   {
      model.results = results;
   }
}

/**
 * Get and check existence of input parameter
 *
 * @method getInput
 * @param name {string} parameter name
 * @param isMandatory {boolean} is set and parameter is missing, status object is set
 * @return {object|null} parameter value or null
 */
function getInput(name, isMandatory)
{
   var value = json.get(name);
	if ((value === null) || (value.length == 0))
	{
	   if (isMandatory)
	   {
   		status.setCode(status.STATUS_BAD_REQUEST, "'" + name + "' parameter is missing.");
	   }
		return null;
	}
	
	return value;
}

/**
 * Obtain the root node for the given site and component
 *
 * @method getRootNode
 * @return {object|string} valid repository node or string error
 */
function getRootNode()
{
   try
   {
      // "site" input
      var site = getInput("site", true);
   	if (site !== null)
   	{
         var siteNode = siteService.getSite(site);
         if (siteNode === null)
         {
      		status.setCode(status.STATUS_BAD_REQUEST, "Site '" + site + "' not found.");
      		return null;
         }
   
         // "componentId" input
         var componentId = getInput("componentId", true);
      	if (componentId !== null)
      	{
            var rootNode = siteNode.getContainer(componentId);
            if (rootNode === null)
            {
         		status.setCode(status.STATUS_BAD_REQUEST, "Component container '" + componentId + "' not found in '" + siteId + "'. (No write permission?)");
         		return null;
            }
      
            return rootNode;
         }
      }
      return null;
   }
   catch(e)
   {
		status.setCode(status.STATUS_BAD_REQUEST, e.toString());
      return null;
   }
}

/**
 * Perform the requested action
 *
 * @method runAction
 * @param rootNode {NodeRef} root node within which to perform the action
 * @return {object} object representation of action result
 */
function runAction(rootNode)
{
   try
   {
      var results = [];
      var site = json.get("site");
      var action = json.get("action");
      
      // "action" input
      action = getInput("action", true);
      if (action === null)
      {
         return null;
      }
      
      switch (String(action).toLowerCase())
      {
         case "createfolder":
            results.push(createFolder(rootNode));
            break;

         case "delete":
            results.push(deleteAsset(rootNode));
            break;
         
         default:
      		status.setCode(status.STATUS_BAD_REQUEST, "'" + action + "' is not a recognised action.");
            result = null;
      }
      
      return results;
   }
   catch(e)
   {
		status.setCode(status.STATUS_BAD_REQUEST, e.toString());
      return null;
   }
}


/**
 * Create Folder action
 *
 * @method createFolder
 * @param rootNode {NodeRef} root node within which to perform the action
 * @return {object} object representation of action result
 */
function createFolder(rootNode)
{
   // "path" input
   var path = getInput("path", true);
   if (path == null)
   {
      return null;
   }
   // Remove any leading "/" from the path
   if (path.substr(0, 1) == "/")
   {
      path = path.substr(1);
   }
   // Ensure path ends with "/" if not the root folder
   if ((path.length > 0) && (path.substring(path.length - 1) != "/"))
   {
      path = path + "/";
   }

   var parentNode = rootNode;
   if (path.length > 0)
   {
      parentNode = rootNode.childByNamePath(path);
   }

   // Must have a parentNode by this point
   if (parentNode === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Parent path '" + path + "' not found in '" + json.get("site") + "'. (No write permission?)");
      return null;
   }
   
   // "name" is mandatory
   var name = getInput("name", true);
   if (name === null)
   {
      return null;
   }
   
   // "title" is optional
   var title = getInput("title");
   title = (title === null) ? "" : title;
   
   // "description" is optional
   var description = getInput("description");
   description = (description === null) ? "" : description;

   var newNode = parentNode.createFolder(name);
   // Always add title & description, default icon
   newNode.properties["cm:title"] = title;
   newNode.properties["cm:description"] = description.substr(0, 100);
   newNode.properties["app:icon"] = "space-icon-default";
   newNode.save();
   // Add uifacets aspect for the web client
   newNode.addAspect("app:uifacets");

   // Construct the result object
   return (
   {
      id: name,
      action: "createFolder",
      success: true
   });
}

/**
 * Delete asset action
 * Expects 'path' argument. If 'file' is also supplied, 'file' is deleted
 * otherwise the folder at 'path' is deleted.
 * @method deleteAsset
 * @param rootNode {NodeRef} root node within which to perform the action
 * @return {object} object representation of action result
 */
function deleteAsset(rootNode)
{
   // "path" input
   var path = getInput("path", true);
   if (path == null)
   {
      return null;
   }
   // Remove any leading "/" from the path
   if (path.substr(0, 1) == "/")
   {
      path = path.substr(1);
   }

   // "file" is optional
   var file = getInput("file");
   if (file !== null)
   {
      // Ensure path ends with "/" if not the root folder
      if ((path.length > 0) && (path.substring(path.length - 1) != "/"))
      {
         path += "/";
      }
      path += file;
   }

   var parentNode = null;
   if (path.length > 0)
   {
      parentNode = rootNode.childByNamePath(path);
   }

   // Must have a parentNode by this point
   if (parentNode === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "'" + path + "' not found in '" + json.get("site") + "'. (No write permission?)");
      return null;
   }
   
   if (!parentNode.remove())
   {
      status.setCode(status.STATUS_BAD_REQUEST, "'" + path + "' could not be deleted. (No write permission?)");
      return null;
   }
   

   // Construct the result object
   return (
   {
      id: path,
      action: "delete",
      success: true
   });
}
