/**
 * Get and parse arguments
 *
 * @method getParsedArgs
 * @return {array|null} Array containing the validated input parameters
 */
function getParsedArgs()
{
   var rootNode = null;
   var parentNode = null;

   if (url.templateArgs.store_type !== null)
   {
      // nodeRef input
      var storeType = url.templateArgs.store_type;
      var storeId = url.templateArgs.store_id;
      var id = url.templateArgs.id;
      var nodeRef = storeType + "://" + storeId + "/" + id;
      
      if (nodeRef == "alfresco://company/home")
      {
         rootNode = companyhome;
      }
      else if (nodeRef == "alfresco://user/home")
      {
         rootNode = userhome;
      }
      else if (nodeRef == "alfresco://sites/home")
      {
         rootNode = companyhome.childByNamePath("Sites");
      }
      else
      {
         rootNode = search.findNode(nodeRef);
      	if (rootNode === null)
      	{
            status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + nodeRef + "'");
            return null;
      	}
      }
   }
   else
   {
      // site, component container, path input
      var site = url.templateArgs.site;
      var container = url.templateArgs.container;

      var siteNode = siteService.getSite(site);
      if (siteNode === null)
      {
         status.setCode(status.STATUS_NOT_FOUND, "Site not found: '" + site + "'");
         return null;
      }

      rootNode = siteNode.getContainer(container);
      if (rootNode === null)
      {
      	 rootNode = siteNode.createContainer(container);
      	 if (rootNode === null)
      	 {
         	status.setCode(status.STATUS_NOT_FOUND, "Document Library container '" + container + "' not found in '" + site + "'. (No permission?)");
         	return null;
         }
      }
   }

   // path input
   var path = url.templateArgs.path;
   if ((path !== null) && (path != ""))
   {
      parentNode = rootNode.childByNamePath(path);
   }
   else
   {
      parentNode = rootNode;
      path = "";
   }
   if (parentNode === null)
   {
      parentNode = rootNode;
   }

   // Resolve site, container and path for parentNode
   var location =
   {
      site: null,
      container: null,
      path: null
   };

   var site, siteNode, container;
   var qnamePaths = parentNode.qnamePath.split("/");
   var displayPaths = parentNode.displayPath.split("/");
   if ((qnamePaths.length > 4) && (qnamePaths[2] == "st:sites"))
   {
      site = qnamePaths[3].substr(3);
      siteNode = siteService.getSite(site);
      container = qnamePaths[4].substr(3);
      
      location = 
      {
         site: site,
         siteNode: siteNode,
         container: container,
         containerNode: siteNode.getContainer(container),
         path: "/" + displayPaths.slice(5, displayPaths.length).join("/")
      }
   }

   var objRet =
   {
      rootNode: rootNode,
      parentNode: parentNode,
      path: path,
      location: location
   }

   // Multiple input files in the JSON body?
   var files = getMultipleInputValues("nodeRefs");
   if (typeof files != "string")
   {
      objRet.files = files;
   }

   return objRet;
}

/**
 * Get multiple input files
 *
 * @method getMultipleInputValues
 * @param param {string} Property name containing the files array
 * @return {array|string} Array containing the files, or string error
 */
function getMultipleInputValues(param)
{
   var values = [];
   var error = null;
   
   try
   {
      // Was a JSON parameter list supplied?
      if (typeof json == "object")
      {
         if (!json.isNull(param))
         {
            var jsonValues = json.get(param);
            // Convert from JSONArray to JavaScript array
            for (var i = 0, j = jsonValues.length(); i < j; i++)
            {
               values.push(jsonValues.get(i));
            }
         }
      }
   }
   catch(e)
   {
      error = e.toString();
   }
   
	// Return the values array, or the error string if it was set
	return (error !== null ? error : values);
}
