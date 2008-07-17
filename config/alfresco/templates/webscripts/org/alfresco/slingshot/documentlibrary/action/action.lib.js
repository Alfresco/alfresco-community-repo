/**
 * Document List Component: action
 *
 * Parameters are either supplied as JSON object literal in content body,
 * or on request URI (e.g. HTTP DELETE methods must use URI)
 *
 * @param uri {string} /{siteId}/{containerId}/{filepath} : full path to file or folder name involved in the action
 * @param content {json} /{siteId}/{containerId}/{filepath} : full path to file or folder name involved in the action
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
   var params = {};
   var files, rootNode;
   
   if (url.templateArgs.store_type != undefined)
   {
      params = getNodeRefInputParams();
   }
   else if (url.templateArgs.site != undefined)
   {
      params = getSiteInputParams();
   }
   if (typeof params == "string")
   {
      status.setCode(status.STATUS_BAD_REQUEST, params);
      return;
   }

   // Try to get the root node if templateArg parameters passed-in
   if (params.hasRootNode)
   {
      rootNode = getRootNode(params);
      if (typeof node == "string")
      {
         status.setCode(status.STATUS_NOT_FOUND, node);
         return;
      }
      params.rootNode = rootNode;
   }

   // Multiple input files in the JSON body?
   files = getMultipleInputFiles();
   if (typeof files != "string")
   {
      params.files = files;
   }
   
   // Check runAction function is provided the action's webscript
   if (typeof runAction != "function")
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Action webscript must provide runAction() function.");
      return;
   }

   // Actually run the action
   var results = runAction(params);
   if ((results !== null) && (results !== undefined))
   {
      if (typeof results == "string")
      {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, results);
      }
      else if (typeof results.status == "object")
      {
         // Status fields have been manually set
         status.redirect = true;
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
 * Get and check existence of mandatory input parameters (Site-based)
 *
 * @method getSiteInputParams
 * @return {object|string} object literal containing parameters value or string error
 */
function getSiteInputParams()
{
   var params = {};
   var error = null;
   
   try
   {
      // First try to get the parameters from the URI
      var siteId = url.templateArgs.site;
      var containerId = url.templateArgs.container;
      var filePath = url.templateArgs.path;

   	if ((siteId === null) || (siteId.length === 0))
   	{
   		return "'site' parameter is missing.";
   	}

      // containerId
   	if ((containerId === null) || (containerId.length === 0))
   	{
   		return "'container' parameter is missing.";
   	}
   	
   	// filePath might be null for the root folder
   	if (filePath === null)
   	{
   	   filePath = "";
   	}
      // Remove any leading or trailing "/" from the path
      // Fix-up parent path to have no leading or trailing slashes
      if (filePath.length > 0)
      {
         var aPaths = filePath.split("/");
         while (aPaths[0] === "")
         {
            aPaths.shift();
         }
         while (aPaths[aPaths.length-1] === "")
         {
            aPaths.pop();
         }
         filePath = aPaths.join("/");
      }

      // Populate the return object
      params =
      {
      	containerId: containerId,
      	siteId: siteId,
      	filePath: filePath,
      	usingNodeRef: false,
      	hasRootNode: true
      }
   }
   catch(e)
   {
      error = e.toString();
   }
   
	// Return the params object, or the error string if it was set
	return (error !== null ? error : params);
}

/**
 * Get and check existence of mandatory input parameters (nodeRef-based)
 *
 * @method getNodeRefInputParams
 * @return {object|string} object literal containing parameters value or string error
 */
function getNodeRefInputParams()
{
   var params = {};
   var error = null;
   
   try
   {
      // First try to get the parameters from the URI
      var storeType = url.templateArgs.store_type;
      var storeId = url.templateArgs.store_id;
      var id = url.templateArgs.id;

      // Was a JSON parameter list supplied?
      // TODO: Also handle multiple files
      if (typeof json == "object")
      {
         if (!json.isNull("store_type"))
         {
            storeType = json.get("store_type");
         }
         if (!json.isNull("store_id"))
         {
            storeId = json.get("store_id");
         }
         if (!json.isNull("id"))
         {
            id = json.get("id");
         }
      }
      
      var nodeRef = storeType + "://" + storeId + "/" + id;
      var node = search.findNode(nodeRef);

   	if (node === null)
   	{
   		return "'" + nodeRef  + "' is not valid.";
   	}

      // Populate the return object
      params =
      {
         nodeRef: nodeRef,
         node: node,
         usingNodeRef: true,
      	hasRootNode: true
      }
   }
   catch(e)
   {
      error = e.toString();
   }
   
	// Return the params object, or the error string if it was set
	return (error !== null ? error : params);
}

/**
 * Get multiple input files (nodeRef-based)
 *
 * @method getMultipleInputFiles
 * @return {array|string} Array containing multiple files, or string error
 */
function getMultipleInputFiles()
{
   var files = [];
   var error = null;
   
   try
   {
      // Was a JSON parameter list supplied?
      if (typeof json == "object")
      {
         if (!json.isNull("nodeRefs"))
         {
            var jsonFiles = json.get("nodeRefs");
            // Convert from JSONArray to JavaScript array
            for (var i = 0, j = jsonFiles.length(); i < j; i++)
            {
               files.push(jsonFiles.get(i));
            }
         }
      }
   }
   catch(e)
   {
      error = e.toString();
   }
   
	// Return the files array, or the error string if it was set
	return (error !== null ? error : files);
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

   if (p_params.usingNodeRef)
   {
      return p_params.node;
   }

   try
   {
      // Find the site
      var siteNode = siteService.getSite(p_params.siteId);
      if (siteNode === null)
      {
   		return "Site '" + p_params.siteId + "' not found.";
      }
         
      // Find the component container
      rootNode = siteNode.getContainer(p_params.containerId);
      if (rootNode === null)
      {
        rootNode = siteNode.createContainer(p_params.containerId);
        if (rootNode === null)
        {
   			return "Component container '" + p_params.containerId + "' not found in '" + p_params.siteId + "'.";
   		}
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
 * @param p_assetPath {string} rootNode-relative path to asset
 * @return {object|string} valid repository node or string error
 */
function getAssetNode(p_rootNode, p_assetPath)
{
   var assetNode = p_rootNode;
   var error = null;

   try
   {
      if (p_assetPath && (p_assetPath.length > 0))
      {
         assetNode = assetNode.childByNamePath(p_assetPath);
      }
      
      if (assetNode === null)
      {
         return "Asset '" + p_assetPath + " not found.";
      }
   }
   catch(e)
   {
		error = e.toString();
   }

	// Return the node object, or the error string if it was set
	return (error !== null ? error : assetNode);
}
