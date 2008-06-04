/**
 * Document List Component: action
 *
 * Parameters are either supplied as JSON object literal in content body,
 * or on request URI (e.g. HTTP DELETE methods must use URI)
 *
 * @param uri {string} /{siteId}/{componentId}/{filepath} : full path to file or folder name involved in the action
 * @param content {json} /{siteId}/{componentId}/{filepath} : full path to file or folder name involved in the action
 */

/* Bootstrap action script */
main();


/**
 * Main script entry point
 * @method main
 */
function main()
{
   // Params object contains commonly-used arguments
   var params = getInputParams();
   if (typeof params == "string")
   {
      status.setCode(status.STATUS_BAD_REQUEST, params);
      return;
   }
   
   // Try to get the root node from the parameters passed-in
   var node = getRootNode(params);
   if (typeof node == "string")
   {
      status.setCode(status.STATUS_NOT_FOUND, node);
      return;
   }

   // Check runAction function is provided the action's webscript
   if (typeof runAction != "function")
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Action webscript must provide runAction() function.");
      return;
   }

   // Actually run the action
   var results = runAction(node, params);
   if (results !== null)
   {
      if (typeof results == "string")
      {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, results);
      }
      else if (typeof results.status == "object")
      {
         for (var s in results.status)
         {
            status[s] = results.status[s];
         }
      }
      else
      {
         model.results = results;
      }
   }
}


/**
 * Get and check existence of mandatory input parameter
 *
 * @method getInputParams
 * @return {object|string} object literal containing parameters value or string error
 */
function getInputParams()
{
   var params = {};
   var error = null;
   
   try
   {
      // First try to get the parameters from the URI
      var siteId = "" + url.templateArgs.siteid;
      var componentId = "" + url.templateArgs.componentid;
      var filePath = "" + url.templateArgs.filepath;

      // Was a JSON parameter list supplied?
      // TODO: Also handle multiple files
      if (typeof json == "object")
      {
         if (!json.isNull("siteid"))
         {
            siteId = json.get("siteid");
         }
         if (!json.isNull("componentid"))
         {
            componentId = json.get("componentid");
         }
         if (!json.isNull("filepath"))
         {
            filePath = json.get("filepath");
         }
      }

   	if ((siteId === null) || (siteId.length === 0))
   	{
   		return "'siteId' parameter is missing.";
   	}

      // componentId
   	if ((componentId === null) || (componentId.length === 0))
   	{
   		return "'componentId' parameter is missing.";
   	}

      // Populate the return object
   	params.componentId = componentId;
   	params.siteId = siteId;
   	params.filePath = filePath;
   }
   catch(e)
   {
      error = e.toString();
   }
   
	// Return the params object, or the error string if it was set
	return (error !== null ? error : params);
}

/**
 * Obtain the root node for the given site and component
 *
 * @method getRootNode
 * @param p_params {object} Object literal containing mandatory parameters
 * @return {object|string} valid repository node or string error
 */
function getRootNode(p_params)
{
   var rootNode = null;
   var error = null;

   try
   {
      // Find the site
      var siteNode = siteService.getSite(p_params.siteId);
      if (siteNode === null)
      {
   		/* return ("Site '" + p_siteId + "' not found."); */
      }
         
      // Find the component container
      rootNode = siteNode.getContainer(p_params.componentId);
      if (rootNode === null)
      {
   		return "Component container '" + p_params.componentId + "' not found in '" + p_params.siteId + "'.";
      }
   }
   catch(e)
   {
		error = e.toString();
   }

	// Return the node object, or the error string if it was set
	return (error !== null ? error : rootNode);
}


/**
 * Obtain the asset node for the given rootNode and filepath
 *
 * @method getAssetNode
 * @param p_rootNode {object} valid repository node
 * @param p_filePath {string} rootNode-relative path to asset
 * @return {object|string} valid repository node or string error
 */
function getAssetNode(p_rootNode, p_filePath)
{
   var assetNode = p_rootNode;
   var error = null;

   try
   {
      // Remove any leading "/" from the path
      var path = p_filePath;
      if (path.substr(0, 1) == "/")
      {
         path = path.substr(1);
      }

      if (path.length > 0)
      {
         assetNode = p_rootNode.childByNamePath(path);
      }
      
      if (assetNode === null)
      {
         return "Asset '" + path + " not found.";
      }
   }
   catch(e)
   {
		error = e.toString();
   }

	// Return the node object, or the error string if it was set
	return (error !== null ? error : assetNode);
}
